package appsGate.lig.manager.communication.service.send;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This interface is the specification of service for send notification, message
 * and command response to client application.
 * 
 * @author Cédric Gérard
 * @since February 8, 2013
 * @version 1.0.0
 *
 */
public interface SendWebsocketsService {

	/**
	 * the send method with a specific command and parameters 
	 * @param cmd, the notification or command response
	 * @param msg, parameters corresponding to the cmd command
	 */
	public void send(String cmd, JSONObject msg);
	public void send(String cmd, JSONArray msg);
	public void send(String msg);
}
