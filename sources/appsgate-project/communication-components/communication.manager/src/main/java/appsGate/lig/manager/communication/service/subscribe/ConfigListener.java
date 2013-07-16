package appsGate.lig.manager.communication.service.subscribe;

import org.json.JSONObject;

/**
  * This interface is the specification for the listener which allow
 * components to subscribe for configuration events and commands. 
 * 
 * @author Cédric Gérard
 * @since February 12, 2013
 * @version 1.0.0
 *
 */
public interface ConfigListener extends CommandListener {
	
	/**
	 * The call back to be notified when events or commands concerning configuration 
	 * are received.
	 * @param cmd the command or events 
	 * @param obj the core of the cmd parameters
	 */
	public void onReceivedConfig(String cmd, JSONObject obj);

}
