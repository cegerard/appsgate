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
	 * @param path optional query uri path (if null, we use hostname:port as URL) 
	 * @return an unique serviceId (ObjectID of the AppsGate service)
	 * if the corresponding service has been correctly created, null otherwise
	 */
	public String createTVInstance(String hostname, int port, String path);
	
	/**
	 * Destroy an existing TV instance 
	 * @param serviceId of the corresponding instance (ObjectID of the AppsGate service)
	 * if null, it will remove all TV services
	 */
	public void removeTVInstance(String serviceId);
	
}
