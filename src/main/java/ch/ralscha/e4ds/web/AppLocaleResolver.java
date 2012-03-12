package ch.ralscha.e4ds.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import ch.ralscha.e4ds.config.UserPrincipal;

public class AppLocaleResolver extends AbstractLocaleResolver {

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		UserPrincipal principal = (UserPrincipal) SecurityUtils.getSubject().getPrincipal();

		if (principal != null) {
			return principal.getLocale();
		}

		return request.getLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException("Cannot change locale - use a different locale resolution strategy");
	}

}
