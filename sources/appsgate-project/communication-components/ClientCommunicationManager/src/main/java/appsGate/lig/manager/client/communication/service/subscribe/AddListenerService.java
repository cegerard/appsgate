package appsGate.lig.manager.client.communication.service.subscribe;

/**
 * This interface is the specification of service for subscribe notification, message
 * and command response to client application.
 * 
 * @author Cédric Gérard
 * @since February 8, 2013
 * @version 1.0.0
 *
 */
public interface AddListenerService {
	
	/**
	 * This method allow all the caller to subscribe for all commands and events except configuration
	 * command.
	 * @param cmdListener the listener for subscription
	 * @return true if the listener is registered, false otherwise
	 */
	public boolean addCommandListener(CommandListener cmdListener);
	
	
	/**
	 * This method all the caller to subscribe for all configuration command.
	 * @param configListener the listener for subscription
	 * @return true if the listener is registered, false otherwise
	 */
	public boolean addConfigListener(ConfigListener configListener);

}
