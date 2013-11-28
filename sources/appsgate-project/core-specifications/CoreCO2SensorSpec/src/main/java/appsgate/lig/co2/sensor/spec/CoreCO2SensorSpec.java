package appsgate.lig.co2.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * Appsgate application to handle CO2 variation from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 28, 2013
 *
 */
public interface CoreCO2SensorSpec {
	
	/**
	 * Get the current CO2 concentration
	 * @return the Co2 concentration as a float
	 */
	public float getCO2Concentration();

}
