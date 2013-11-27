package appsgate.lig.occupancy.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle occupancy variations from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since November 25, 2013
 *
 */
public interface CoreOccupancySpec {

	/**
	 * Get the occupied status
	 * @return true if the occupied status is true, false otherwise
	 */
	public boolean getOccupied();

}
