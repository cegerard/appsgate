package appsgate.lig.on_off.actuator.spec;

import org.json.simple.JSONObject;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle On/Off actuator actions.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since March 3, 2013
 *
 */
public interface OnOffActuatorSpec {
	
	/**
	 * Get the virtual state of this actuator.
	 * Nothing is sur thaht the real device is in this corresponding
	 * state.
	 * 
	 * @return a JSON description of the device state
	 */
	public JSONObject getTargetState();
	
	/**
	 * Set the device ON state
	 */
	public void on();
	
	/**
	 * Set the device OFF state
	 */
	public void off();

}
