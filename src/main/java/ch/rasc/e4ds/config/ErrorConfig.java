package ch.rasc.e4ds.config;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import ch.ralscha.extdirectspring.bean.BaseResponse;
import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.controller.Configuration;
import ch.ralscha.extdirectspring.util.ExtDirectSpringUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

/**
 * {@linkplain http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/htmlsingle/#mvc-exceptionhandlers}
 * 
 * extending from @see ResponseEntityExceptionHandler than @see DefaultHandlerExceptionResolver give us the ability to write in @ResponseBody json objects
 * @author dbs
 *
 */
@ControllerAdvice
public class ErrorConfig extends  /*DefaultHandlerExceptionResolver*/ ResponseEntityExceptionHandler implements InitializingBean{
	
	private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Autowired private Environment environment;
	@Autowired private Configuration configuration;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(environment.acceptsProfiles("dev"))
			Assert.notNull(configuration, "EDS configuration cannot be null");
		if (configuration == null) {
			logger.error("following property ch.ralscha.extdirectspring.controller.Configuration shouldn't be null, wrong configuration");
			configuration = new Configuration();
		}
	}
	
	@ExceptionHandler({ClassNotFoundException.class, IOException.class})//IOException too generic??
   public @ResponseBody ResponseEntity<Object> handleClassNotFoundException(Exception ex, WebRequest request, HttpServletResponse response) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      return handleExceptionInternal(ex, handleEdsException(new BaseResponse(), ex), buildHttpHeaders(null), status, request);
   }

	@ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
   public ModelAndView handleAuthenticationCredentialsNotFoundException(Exception exception, HttpServletResponse response) {
		UriComponents ucb = ServletUriComponentsBuilder.fromCurrentContextPath().path("/index.html").build();
		try {
			response.sendRedirect(ucb.toUriString());
		} catch (IOException e) {
			logger.error("failed to send redirect", e);
		}
		return new ModelAndView("login");
   }

	@SuppressWarnings("unchecked")
	@ExceptionHandler({org.hibernate.exception.ConstraintViolationException.class, javax.validation.ConstraintViolationException.class, org.springframework.dao.DataIntegrityViolationException.class})
   public @ResponseBody ResponseEntity<Object> handleDataAccess(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ExtDirectResponseEx response = buildExtDirectResponse(request,  ex, handleEdsException(new BaseResponse(), ex), status.toString());
		if(ex instanceof javax.validation.ConstraintViolationException)
			((Map<String, Object>)response.getResult()).put("errors" ,((javax.validation.ConstraintViolationException) ex).getConstraintViolations());
		else if(ex instanceof org.hibernate.exception.ConstraintViolationException) {
			((Map<String, Object>)response.getResult()).put("constraintName" ,((org.hibernate.exception.ConstraintViolationException) ex).getConstraintName());
			if(configuration.isSendStacktrace()) {
				((Map<String, Object>)response.getResult()).put("errorCode" ,((org.hibernate.exception.ConstraintViolationException) ex).getErrorCode());
				((Map<String, Object>)response.getResult()).put("sql" ,((org.hibernate.exception.ConstraintViolationException) ex).getSQL());
			}
		}else if(ex instanceof org.springframework.dao.DataIntegrityViolationException) {
			((Map<String, Object>)response.getResult()).put("mostSpecificCause" ,((org.springframework.dao.DataIntegrityViolationException) ex).getMostSpecificCause().getMessage());
		}
		return handleExceptionInternal(ex, response, buildHttpHeaders(null), status, request);
   }

	@ExceptionHandler({AccessDeniedException.class})
   public @ResponseBody ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		logger.warn("possible misconfiguration the issue is denied a method access ressource "); 
		return handleExceptionInternal(ex, handleEdsException(new BaseResponse(), ex), buildHttpHeaders(null), status, request);
   }

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception, java.lang.Object, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
		if( "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")) ) {//we have a Ext.Direct request
			headers.add("X-Requested-With", "XMLHttpRequest");
			if(null != body && body instanceof BaseResponse)
				body = buildExtDirectResponse(request, ex, (BaseResponse) body, status.toString());
			else
				body = buildExtDirectResponse(request, ex, handleEdsException(new BaseResponse(), ex), status.toString());
			return new ResponseEntity<Object>(body, headers, status);
		}
		else{//standard spring processing
			//add additional logic here eventually to build a new body or else...
			return super.handleExceptionInternal(ex, body, headers, status, request);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleBindException(org.springframework.validation.BindException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		ExtDirectResponseEx response = buildExtDirectResponse(request,  ex, handleEdsException(new BaseResponse(), ex), status.toString());
		((Map<String, Object>)response.getResult()).put("errors" , ex.getBindingResult().getAllErrors());
		return handleExceptionInternal(ex, response, buildHttpHeaders(headers), status, request);
	}
	
	/**
	 * @param request {@link org.springframework.web.context.request.WebRequest}
	 * @param ex {@link java.lang.Exception}
	 * @param body should be an instance of {@link ch.ralscha.extdirectspring.bean.BaseResponse}
	 * @param status {@link org.springframework.http.HttpStatus} or Business error id or else
	 * @return a decorated ExtDirectResponse
	 */
	protected ExtDirectResponseEx buildExtDirectResponse(WebRequest request, Exception ex, BaseResponse body, String status) {
		Assert.notNull(body, "misconfiguration! body shouldn't be null at this point");
		ExtDirectResponseEx response = new ExtDirectResponseEx(body);
		if(request.getParameter("extTID") != null && StringUtils.isNumeric(request.getParameter("extTID"))){
			response.setDirectParams(request);
		}
		if(null == response.getResult()) {
			Map<String, Object> result = new HashMap<String, Object>();
			//TODO improve specific payload through the result for better error mgmt on client side
			result.put("success", false);
			result.put("status", status);
			response.setResult(result);
		}
		return response;
	}
	
	/**
	 * based on @see ch.ralscha.extdirectspring.controller.RouterController.handleException(BaseResponse response, Exception e)
	 * @param response 
	 * @param e
	 * @return 
	 */
	protected BaseResponse handleEdsException(BaseResponse response, Exception e) {
		Throwable cause;
		if (e.getCause() != null) {
			cause = e.getCause();
		} else {
			cause = e;
		}

		response.setType("exception");
		response.setMessage(configuration.getMessage(cause));

		if (configuration.isSendStacktrace()) {
			response.setWhere(ExtDirectSpringUtil.getStackTrace(cause));
		} else {
			response.setWhere(null);
		}
		return response;
	}
	
	/**
	 * build standard http headers to be included in response
	 * @return headers
	 */
	protected HttpHeaders buildHttpHeaders(HttpHeaders headers) {
		if (headers == null)
			headers = new HttpHeaders() ;
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAcceptCharset(Lists.newArrayList(StandardCharsets.UTF_8));
		headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
		headers.setDate(new Date().getTime());
		return headers;
	}
	
	//TODO ask the EDS library creator to include  ExtDirectResponse additional setters and remove this stitch
	@JsonInclude(Include.NON_NULL)
	public class ExtDirectResponseEx extends ExtDirectResponse{
		protected int tid;
		protected String action;
		protected String method;
		public ExtDirectResponseEx(BaseResponse baseResponse){
			this.setType(baseResponse.getType());
			this.setMessage(baseResponse.getMessage());
			this.setWhere(baseResponse.getWhere());
		}
		public ExtDirectResponseEx(WebRequest request){
			setType(request.getParameter("extType"));
			setDirectParams(request);
		}
		public ExtDirectResponseEx setDirectParams(WebRequest request) {
			action = request.getParameter("extAction");
			method = request.getParameter("extMethod");
			tid = Integer.parseInt(request.getParameter("extTID"));
			return this;
		}
	}
}
