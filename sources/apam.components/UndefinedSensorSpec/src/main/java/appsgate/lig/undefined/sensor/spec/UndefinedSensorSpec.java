package appsgate.lig.undefined.sensor.spec;

import org.json.JSONArray;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle undefined or ambiguous sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 6, 2013
 *
 */
public interface UndefinedSensorSpec {
	
	/**
	 * This method get the available profiles that corresponding to this ambiguous sensor.
	 * If no profiles corresponding this sensor is not yet implemented.
	 * 
	 * @return a JSON description that contain the network profile of this sensor and an user friendly interpretation.
	 * ex: For EnOcean switch this method return the following JSONArray
	 * [{"profile":"EEP-05-02-01", "type":"switch sensor"},{"profile":"EEP-05-04-01", "type":"Key card sensor"}]
	 */
	public JSONArray getCapabilities();
	
	/**
	 * This method send to the EnOcean dump bundle the corresponding profile to
	 * that sensor in order to validate the configuration and receive telegrams from it.
	 * 
	 * @param profile, the chosen sensor profile
	 */
	public void validate(String profile);

}
