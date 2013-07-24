package ch.rasc.e4ds.web;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;



@Component
@Scope(value=WebApplicationContext.SCOPE_SESSION, proxyMode=ScopedProxyMode.TARGET_CLASS)
public class UserPreferences implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2569586198993319961L;

	String myColorButton = "#0000";
	
	public UserPreferences(){
		
	}

	public UserPreferences(String myColorButton) {
		this.myColorButton = myColorButton;
	}

	/**
	 * @return the myColorButton
	 */
	public String getMyColorButton() {
		return myColorButton;
	}

	/**
	 * @param myColorButton the myColorButton to set
	 */
	public void setMyColorButton(String myColorButton) {
		this.myColorButton = myColorButton;
	}
	
}
