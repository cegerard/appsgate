package appsgate.lig.upnp.generator;

import java.util.List;


/**
 * This class is a configuration parameter used to specify the services exposed by a device
 * for which we want to generate proxies
 */
public class Device {

	/**
	 * The URN of the device type
	 */
	private String deviceType;
	
	/**
	 * The associated java mapping
	 */
	private Mapping mapping;
	
	/**
	 * The list of declared services
	 */
	private List<Service> serviceList;
	
	public String getDeviceType() {
		return deviceType;
	}
	
	public Mapping getMapping() {
		return mapping;
	}
	
	public List<Service> getServices() {
		return serviceList;
	}
	
}
