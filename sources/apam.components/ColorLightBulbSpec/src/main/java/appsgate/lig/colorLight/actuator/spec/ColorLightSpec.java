package appsgate.lig.colorLight.actuator.spec;

import org.json.JSONObject;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle color light actuator
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since May 22, 2013
 *
 */
public interface ColorLightSpec {
	
	/**
	 * Get the current light status
	 * @return a JSON description of the light (JSON attribute depends on color light implementation)
	 */
	public JSONObject getLightStatus();
	
	/**
	 * Get the current light color
	 * @return the current color as a long integer
	 */
	public long getLightColor();
	
	/**
	 * Get the current light brightness
	 * @return the current brightness as an integer
	 */
	public int getLightBrightness();
	
	/**
	 * Get the current light color saturation
	 * @return the current light color saturation as an integer
	 */
	public int getLightColorSaturation();
	
	/**
	 * Get the current light state (On/Off)
	 * @return true if the light is On and false otherwise
	 */
	public boolean getCurrentState();
	
	/**
	 * Get light manufacturer details
	 * @return a JSON object that contain details from manufacturer.
	 */
	public JSONObject getManufacturerDetails();
	
	/**
	 * Set the complete status of the color light. This method allow to set
	 * several attribute at the same time.
	 * @param newStatus the new status as a JSON object
	 * @return true if the new status is set, false otherwise
	 */
	public boolean setStatus(JSONObject newStatus);
	
	/**
	 * Switch the light on
	 * @return true if the light turn on, false otherwise
	 */
	public boolean On();
	
	/**
	 * Switch the light off
	 * @return true if the light turn off, false otherwise 
	 */
	public boolean Off();
	
	/**
	 * Put the light color
	 * @param color the new light color as a long integer
	 * @return true if the new color is set, false otherwise 
	 */
	public boolean setColor(long color);
	
	/**
	 * Put the color light brightness 
	 * @param brightness as a long integer
	 * @return true if the new brightness is set, false otherwise
	 */
	public boolean setBrightness(long brightness);
	
	/**
	 * Put the color saturation 
	 * @param saturation as an integer
	 * @return true if the new saturation is set, false otherwise
	 */
	public boolean setSaturation(int saturation);
	
	/**
	 * Set the light color to red
	 * @return true if the light turn red, false otherwise
	 */
	public boolean setRed();
	
	/**
	 * Set the light color to blue
	 * @return true if the light turn blue, false otherwise
	 */
	public boolean setBlue();
	
	/**
	 * Set the light color to green
	 * @return true if the light turn green, false otherwise
	 */
	public boolean setGreen();
	
	/**
	 * Set the light color to yellow
	 * @return true if the light turn yellow, false otherwise
	 */
	public boolean setYellow();
	
	/**
	 * Set the light color to orange
	 * @return true if the light turn orange, false otherwise
	 */
	public boolean setOrange();
	
	/**
	 * Set the light color to purple
	 * @return true if the light turn purple, false otherwise
	 */
	public boolean setPurple();
	
	/**
	 * Set the light color to pink
	 * @return true if the light turn pink, false otherwise
	 */
	public boolean setPink();
	
	/**
	 * Increase the light brightness by step
	 * @param step the increase brightness step as an integer
	 * @return true if the light brightness has increased, false otherwise
	 */
	public boolean increaseBrightness(int step);
	
	/**
	 * Decrease the light brightness by step
	 * @param step the decrease brightness step as an integer
	 * @return true if the light brightness has decreased, false otherwise
	 */
	public boolean decreaseBrightness(int step);
	
	/**
	 * Invert the light mode
	 * @return true if the light state changed, false otherwise
	 */
	public boolean toggle();
}
