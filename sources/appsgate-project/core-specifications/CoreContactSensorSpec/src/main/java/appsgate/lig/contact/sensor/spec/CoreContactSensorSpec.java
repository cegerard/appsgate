package appsgate.lig.contact.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle contact events from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 5, 2013
 *
 */
public interface CoreContactSensorSpec {

	/**
	 * Get the current contact sensor state
	 * @return false if no contact has been detected (Opened) and true otherwise (Closed)
	 */
	public boolean getContactStatus();
}
