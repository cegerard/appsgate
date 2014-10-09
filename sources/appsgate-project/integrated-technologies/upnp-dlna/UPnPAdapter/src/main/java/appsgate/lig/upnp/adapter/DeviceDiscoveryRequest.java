package appsgate.lig.upnp.adapter;

import org.osgi.framework.ServiceReference;

/**
 * The asynchronous handler for the discovery request.
 * 
 */
public class DeviceDiscoveryRequest implements Runnable {

	/**
	 * The discovered device
	 */
	private final ServiceReference deviceReference;
	
	
	/**
	 * The appsgate adapter
	 */
	private final UPnPAdapter adapter;
	
	
	public DeviceDiscoveryRequest(ServiceReference deviceReference, UPnPAdapter adapter) {
		this.deviceReference = deviceReference;
		this.adapter 		= adapter;
	}
	
	@Override
	public void run() {
		adapter.createProxies(deviceReference);
	}
	
}
