package appsgate.lig.fairylights.adapter;

import org.json.JSONArray;

public interface FairyLightsStatusListener {
	
	/**
	 * Called when the real fairy lights is available and reachable
	 * provide the host to reach the device (ipv4 address instead of mDNS hostname to improve performance)
	 */
	public void deviceAvailable(String host);
	
	/**
	 * Called when the real fairy lights is no more available
	 */
	public void deviceUnavailable();

	/**
	 * Called when the one or more lights color have been changed
	 * @param the lights that have changed with their new values
	 */
	public void lightChanged(JSONArray lights);
}
