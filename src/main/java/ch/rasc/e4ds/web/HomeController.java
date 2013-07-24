package ch.rasc.e4ds.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HomeController {

	@RequestMapping(value= { "/", "/index.html"})
	public ModelAndView home(HttpServletRequest servletRequest, Locale locale) {
		ModelAndView mav = new ModelAndView("index");
		servletRequest.getSession().setAttribute("userPreferences", new UserPreferences("#CCCC"));
		return mav;
	}

}
