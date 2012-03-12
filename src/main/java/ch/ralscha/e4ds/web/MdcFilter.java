package ch.ralscha.e4ds.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.slf4j.MDC;

import ch.ralscha.e4ds.config.UserPrincipal;

public class MdcFilter implements Filter {

	@Override
	public void init(FilterConfig config) {
		//no action
	}

	@Override
	public void destroy() {
		//no action
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();
		if (principal != null) {
			MDC.put("userName", principal.getUsername());
		}

		MDC.put("ip", request.getRemoteAddr());
		chain.doFilter(request, response);
	}
}