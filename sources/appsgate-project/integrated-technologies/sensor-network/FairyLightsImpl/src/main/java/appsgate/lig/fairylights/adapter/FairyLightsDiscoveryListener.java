package appsgate.lig.fairylights.adapter;

public interface FairyLightsDiscoveryListener {
	
	/**
	 * Called when the real fairy lights is available and reachable
	 * provide the host to reach the device (ipv4 address instead of mDNS hostname to improve performance)
	 */
	public void deviceAvailable(String host);
	
	/**
	 * Called when the real fairy lights is no more available
	 */
	public void deviceUnavailable();


}
