package appsgate.lig.upnp.generator;


/**
 * This class is a configuration parameter used to specify the location of the SCDP description
 * associated with a given type
 */
public class Service {

	/**
	 * The URN of the service type
	 */
	private String serviceType;
	
	/**
	 * The associated SCPD url
	 */
	private String sCPDURL;
	
	/**
	 * The service id if this is an service definition embedded in a device
	 */
	private String serviceId;
	
	/**
	 * The associated java mapping
	 */
	private Mapping mapping;
	
	public String getType() {
		return serviceType;
	}
	
	public String getServiceId() {
		return serviceId;
	}
	
	public String getDescription() {
		return sCPDURL;
	}
	
	public Mapping getMapping() {
		return mapping;
	}
	
}
