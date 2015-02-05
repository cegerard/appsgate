package appsgate.lig.light.actuator.philips.HUE.impl.utils;

public class RGBColor {
	
	/**
	 * red component between 0 and 255
	 */
	int red;

	/**
	 * green component between 0 and 255
	 */
	int green;
	
	/**
	 * blue component between 0 and 255
	 */
	int blue;
	
	int checkValue(int value) {
		if(value>255) {
			return 255;
		} else if (value < 0) {
			return 0;
		} else {
			return value;
		}
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public void setRed(int red) {
		this.red = checkValue(red);
	}

	public void setGreen(int green) {
		this.green = checkValue(green);
	}

	public void setBlue(int blue) {
		this.blue = checkValue(blue);
	}

	/**
	 * @param red
	 * @param green
	 * @param blue
	 */
	public RGBColor(int red, int green, int blue) {
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}
	
	public String getHTMLColor() {
        return String.format("#%02X%02X%02X", red, green, blue);
	}
	
}


