package appsGate.lig.manager.client.communication.service.send;

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
	 * The send method with a specific key and parameters 
	 * @param key the notification or key response
	 * @param msg parameters corresponding to the key
	 */
	public void send(String key, JSONObject msg);
	
	/**
	 * The send method with a specific key and parameters 
	 * @param key the notification or key response
	 * @param msg parameters corresponding to the key
	 */
	public void send(String key, JSONArray msg);
	
	/**
	 * The send method with a specific key and parameters 
	 * @param msg JSON stringify message to send
	 */
	public void send(String msg);
	
	/**
	 * The send method with a specific key and parameters to the specify client
	 * @param clientId the targeted client identifier
	 * @param key the notification or key response
	 * @param msg parameters corresponding to the key
	 */
	public void send(int clientId, String key, JSONObject msg);
	
	/**
	 * The send method with a specific key and parameters to the specify client
	 * @param clientId the targeted client identifier
	 * @param key the notification or key response
	 * @param msg parameters corresponding to the key
	 */
	public void send(int clientId, String key, JSONArray msg);
	
	/**
	 * The send method to a specific client 
	 * @param msg JSON stringify message to send
	 */
	public void send(int clientId, String msg);

}
