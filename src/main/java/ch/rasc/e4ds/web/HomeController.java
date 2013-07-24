package ch.rasc.e4ds.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {
	
	@Autowired private UserPreferences userPreferences;

	@RequestMapping(value= { "/", "/index.html"})
	public ModelAndView home(HttpServletRequest servletRequest, Locale locale) {
		ModelAndView mav = new ModelAndView("index");
		setScopeSession(servletRequest.getSession());
		return mav;
	}

	/**
	 * 
	 * @param httpSession
	 */
	private void setScopeSession(HttpSession httpSession) {
		UserPreferences up = (UserPreferences) httpSession.getAttribute("userPreferences");
		userPreferences.setColor(up.getColor());
	}
}
