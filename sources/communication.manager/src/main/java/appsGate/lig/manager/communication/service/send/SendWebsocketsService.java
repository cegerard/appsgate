package appsGate.lig.manager.communication.service.send;

import org.json.JSONArray;
import org.json.JSONObject;

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
	 * @param cmd the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(String cmd, JSONObject msg);
	
	/**
	 * the send method with a specific command and parameters 
	 * @param cmd the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(String cmd, JSONArray msg);
	
	/**
	 * the send method with a specific command and parameters 
	 * @param msg JSON stringify message to send
	 */
	public void send(String msg);
	
	/**
	 * the send method with a specific command and parameters to the specify client
	 * @param clientId the targeted client identifier
	 * @param cmd the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(int clientId, String cmd, JSONObject msg);
	
	/**
	 * the send method with a specific command and parameters to the specify client
	 * @param clientId the targeted client identifier
	 * @param cmd the notification or command response
	 * @param msg parameters corresponding to the cmd command
	 */
	public void send(int clientId, String cmd, JSONArray msg);

}
