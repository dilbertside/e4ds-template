package ch.rasc.e4ds.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import ch.rasc.e4ds.dto.Color;
import ch.rasc.e4ds.web.UserPreferences;


public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	/* (non-Javadoc)
	 * @see org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler#onAuthenticationSuccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException,
			ServletException {
		request.getSession().setAttribute("userPreferences", new UserPreferences(new Color("#CCCC")));
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
