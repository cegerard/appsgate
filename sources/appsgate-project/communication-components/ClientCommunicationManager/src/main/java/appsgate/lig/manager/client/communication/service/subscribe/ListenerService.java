package appsgate.lig.manager.client.communication.service.subscribe;

/**
 * This interface is the specification of service for subscribe notification, message
 * and command response to client application.
 * 
 * @author Cédric Gérard
 * @since February 8, 2013
 * @version 1.0.0
 *
 */
public interface ListenerService {
	
	/**
	 * This method allow all the caller to subscribe for all commands and events except configuration
	 * command.
	 * @param cmdListener the listener for subscription
	 * @param the target corresponding to the listener
	 * @return  true if the command listener is new, false if it has been replaced
	 */
	public boolean addCommandListener(CommandListener cmdListener, String target);
	
	/**
	 * Remove the command listener associated to a target
	 * @param target the target from witch remove the command listener
	 * @return true if the command listener has been removed, false otherwise
	 */
	public boolean removeCommandListener(String target);
	
	/**
	 * Add and create a dedicated server through the specified port 
	 * @param cmdListener the listener json coming messages
	 * @param name a unique human readable connection name
	 * @param port the port where to listen
	 * @return true if the connection has been opened
	 */
	public boolean createDedicatedServer(CommandListener cmdListener, String name, int port);
	
	/**
	 * Remove an existing dedicated connection
	 * @param name the name of the connection to remove
	 * @return true if the connection has been removed, false otherwise
	 */
	public boolean removeDedicatedServer(String name);

}
