package appsgate.lig.luminosity.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * Appsgate application to handle illumination variation from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 5, 2013
 *
 */
public interface CoreLuminositySensorSpec {
	
	/**
	 * Enum type that defines available luminosity units
	 * @author Cédric Gérard
	 *
	 */
	public enum LuminosityUnit {
		Lux;
	}
	
	/**
	 * Get the current luminosity unit
	 * @return a LuminosityUnit object that represent the luminosity unit
	 */
	public LuminosityUnit getLuminosityUnit();
	
	/**
	 * Get the current illumination = the last value sent by the illumination sensor
	 * @return the illumination as an integer
	 */
	public int getIllumination();

}
