package appsgate.lig.google.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;


/**
 * Helper class to write data to a google calendar
 * (manage the corresponding https requests, authentication should have been done with the corresponding scope)
 * related to google calendar API v3, use directly REST & JSON (not the java libraries for google calendar)
 * @author thibaud
 *
 */
public class GoogleCalendarWriter extends GoogleCalendarReader{
	
	
	public final static String SCOPE_CALENDAR_READWRITE = "https://www.googleapis.com/auth/calendar";
		
	public static JSONObject addEvent(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			String calendarId,
			String requestContent) {
		
		Map<String, String> urlParameters=new HashMap<String, String>();
		Map<String, String> requestProperties= new HashMap<String, String>();
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
		requestProperties.put("Content-Type","application/json");
		
		String serviceURL = URL_CALENDARS+GoogleHTTPRequest.SLASH;
		
		try {
			serviceURL += URLEncoder.encode(calendarId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error encoding the URL : "+e.getMessage());
			return null;
		}				
		serviceURL+=GoogleHTTPRequest.SLASH+PARAM_EVENTS;

		return GoogleHTTPRequest.httpsPost(serviceURL,
				requestProperties,
				requestContent,
				urlParameters);
	}
	
	public static String deleteEvent(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			String calendarId,
			String eventId) {
		
		Map<String, String> urlParameters=new HashMap<String, String>();
		Map<String, String> requestProperties= new HashMap<String, String>();
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
		
		String serviceURL = URL_CALENDARS+GoogleHTTPRequest.SLASH;
		
		try {
			serviceURL += URLEncoder.encode(calendarId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
			serviceURL+=GoogleHTTPRequest.SLASH+PARAM_EVENTS+GoogleHTTPRequest.SLASH;
			serviceURL += URLEncoder.encode(eventId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error encoding the URL : "+e.getMessage());
			return null;
		}				

		return GoogleHTTPRequest.httpsDelete(serviceURL,
				requestProperties,
				urlParameters);
	}	

	
	/**
	 * Add a new event to the remote calendar and return an new VEvent if the event is correctly added
	 * to the remote service
	 * @param newEvent, the new iCalendar event to add
	 * @return the iCalendar event added
	 */
//	@Override
//	public synchronized VEvent addEvent(String calendar, String account, String password, VEvent newEvent) {
//		return null; //TODO
//	}

	/**
	 * Delete specific event to the remote calendar.
	 * @param oldEvent the event to be deleted
	 * @return true if the event is remotely deleted
	 */
//	public synchronized boolean delEvent (String calendar, String account, String password, VEvent newEvent){
//		return false; //TODO
//	}
	
	
}
