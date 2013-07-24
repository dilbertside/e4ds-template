package ch.rasc.e4ds.dto;

public class Color{
	String myColorButton = "#0000";
	
	public Color() {
	}
	
	public Color(String colorButton) {
		this.myColorButton = colorButton;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Color [" + (myColorButton != null ? "myColorButton=" + myColorButton : "") + "]";
	}
	
}
