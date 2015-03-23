package appsgate.lig.manager.client.communication.service.subscribe;

import org.json.JSONObject;

/**
 * This interface is the specification for the listener which allow
* components to subscribe for command events. 
* 
* @author Cédric Gérard
* @since February 12, 2013
* @version 1.0.0
*
*/
public interface CommandListener {
	
	/**
	 * The call back to be notified when events or commands concerning abstract object 
	 * are received.
	 * 
	 * @param obj the core of the cmd parameters
	 */
	public void onReceivedCommand(JSONObject obj);
}
