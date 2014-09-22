package appsgate.lig.google.services;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

/**
 * 
 */
public interface GoogleAdapter  {
		
	
	/**
	 * Set the refresh Token used to get or renew the access token
	 * @param refreshToken
	 */
	public void setRefreshToken(String refreshToken);
	
	
	/**
	 * If the access token expires in less than this value, it should be renewed
	 */
	public static int MIN_ACCESSTOKEN_EXPIRATION = 10;
	
	
	public Set<GoogleEvent> getEvents(String calendarId, Map<String, String> requestParameters);
	
		
	/**
	 * Add an an event to the calendar and return the associated GoogleEvent Object
	 * @param calendarId
	 * @param requestContent
	 * @return
	 */
	public GoogleEvent addEvent(String calendarId, String requestContent);	
	
	public boolean deleteEvent(String calendarId, String eventId);
	
}
