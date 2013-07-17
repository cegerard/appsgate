package appsgate.lig.temperature.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle temperatures variations from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 4, 2013
 *
 */
public interface CoreTemperatureSensorSpec {

	/**
	 * Enum type that defines available temperature units
	 * @author Cédric Gérard
	 *
	 */
	public enum TemperatureUnit {
		Kelvin, Celsius, Rankine, Fahrenheit;
	}

	/**
	 * Get the current temperature unit
	 * @return a TemperatureUnit object that represent the temperature unit
	 */
	public TemperatureUnit getTemperatureUnit();
	
	/**
	 * Get the current temperature = the last value sent by the temperature sensor
	 * @return the temperature as a float
	 */
	public float getTemperature();

}
