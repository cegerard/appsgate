package appsgate.lig.google.services;

import java.util.Map;
import java.util.Set;


/**
 * 
 */
public interface GoogleAdapter  {
	
	
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
