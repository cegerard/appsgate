/**
 * 
 */
package appsgate.lig.tv.spec;

/**
 * @author thibaud
 *
 */
public interface TVFactory {
	/**
	 * Try to discover an existing TV service and create the corresponding instance
	 * @param hostname
	 * @param port
	 * @return an unique serviceId (ObjectID of the AppsGate service)
	 * if the corresponding service has been correctly created, null otherwise
	 */
	public String createTVInstance(String hostname, int port);
	
	/**
	 * Destroy an existing TV instance 
	 * @param serviceId of the corresponding instance (ObjectID of the AppsGate service)
	 */
	public void destroyTVInstance(String serviceId);
	
}
