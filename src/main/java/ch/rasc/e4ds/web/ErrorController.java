package ch.rasc.e4ds.web;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

@Controller
public class ErrorController {

	private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Autowired private Environment environment;
	
	
	/**
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/error.json", produces="application/json")
   @ResponseBody
   public Map<String, Object> errorJson(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("handle: "+request.getAttribute(RequestDispatcher.ERROR_MESSAGE));

       Map<String, Object> map = new HashMap<String, Object>();
       map.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
       map.put("reason", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
       Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
       logger.error("error.json", ex);
       
		return map;
   }
	
	/**
	 * 
	 * @param request
	 * @param locale
	 * @param principal
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value="/error-catchall")
   public ModelAndView errorCatchall(HttpServletRequest request, HttpServletResponse response, Locale locale, Principal principal) throws IOException {

		ModelAndView mav = new ModelAndView("uncaughtException");
		String originalUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
		logger.debug("originalUri: "+originalUri);
		UriComponents ucb = null;
       Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
       switch (status) {
			case HttpServletResponse.SC_NOT_FOUND:
				ucb = ServletUriComponentsBuilder.fromContextPath(request).path("/index.html").build();
				response.sendRedirect(ucb.toUriString());
				//mav.setView(new RedirectView("login", true));
				mav.setViewName("login");
				return mav;
			case HttpServletResponse.SC_NOT_ACCEPTABLE:
			default:
				break;
		}
       Throwable exc = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
       mav.addObject("status", status);
       mav.addObject("reason", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
       mav.addObject("exc", exc);
       
       return mav;
   }
	
	/**
	 * for live test only
	 * @param exceptionName
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = "/error/{exceptionName}", method = RequestMethod.GET)
   public String exceptionMaker(@PathVariable("exceptionName") String exceptionName) throws Throwable {
		if(environment.acceptsProfiles("dev", "test")) {
			Class c = null;
			c = Class.forName(exceptionName);
			if(c.getName().contentEquals("java.lang.NullPointerException"))
				throw  (NullPointerException)c.newInstance();
			else if (c.getName().contentEquals("org.springframework.security.access.AccessDeniedException"))
				throw  new org.springframework.security.access.AccessDeniedException("test");
			else if (c.getName().contentEquals("java.lang.ClassNotFoundException"))
				throw  (java.lang.ClassNotFoundException)c.newInstance();
			else if (c.getName().contentEquals("org.springframework.security.authentication.AuthenticationCredentialsNotFoundException"))
				throw  new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("test");
			//else if (c.isInstance(org.springframework.core.NestedRuntimeException.DataAccessException.class))
			//	throw  (org.springframework.core.NestedRuntimeException.DataAccessException)c.newInstance();
		}
		return "redirect:index";
   }
	
	@RequestMapping(value = "/export/malkovitch.vcf", method = RequestMethod.GET, consumes="text/vcard" , produces="text/vcard")
	public ModelAndView contact(HttpServletRequest request, HttpServletResponse response, Locale locale, Principal principal) throws ServletException {
		//it should failed as mime is not registered
		return new ModelAndView("index");
	}
}
