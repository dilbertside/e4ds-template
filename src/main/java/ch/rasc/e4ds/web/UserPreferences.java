package ch.rasc.e4ds.web;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import ch.rasc.e4ds.dto.Color;



@Component
@Scope(value=WebApplicationContext.SCOPE_SESSION, proxyMode=ScopedProxyMode.TARGET_CLASS)
public class UserPreferences implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2569586198993319961L;

	Color color;
	
	public UserPreferences(){
		
	}

	public UserPreferences(Color myColorButton) {
		this.color = myColorButton;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	
}
