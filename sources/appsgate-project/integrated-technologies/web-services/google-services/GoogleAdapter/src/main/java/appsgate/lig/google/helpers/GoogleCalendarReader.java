package appsgate.lig.google.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to read data from a google calendar
 * (manage the corresponding https requests, authentication should have been done with the corresponding scope)
 * related to google calendar API v3, use directly REST & JSON (not the java libraries for google calendar) 
 * @author thibaud
 *
 */
public class GoogleCalendarReader {
	
	static Logger logger = LoggerFactory.getLogger(GoogleCalendarReader.class);

	
	public final static String SCOPE_CALENDAR_READONLY = "https://www.googleapis.com/auth/calendar.readonly";
	public final static String URL_CALENDARLIST = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
	public final static String URL_CALENDARS = "https://www.googleapis.com/calendar/v3/calendars";
	
	public final static String PARAM_SINGLE_EVENTS = "singleEvents";	
	public final static String PARAM_EVENTS = "events";	
	public final static String PARAM_TIMEMIN = "timeMin";
	public final static String PARAM_TIMEMAX = "timeMax";
	
	public final static String PARAM_MAX_RESULTS = "maxResults";	
	public final static String PARAM_MAX_RESULTS_DEFAULT = "50";	
	
	public final static String PARAM_NEXT_PAGE_TOKEN = "nextPageToken";
	public final static String PARAM_PAGE_TOKEN = "pageToken";


	public final static String RESP_ITEMS = "items";

	
	/**
	 * Add the additional and mandatory parameters for authentication
	 * @param apiKey
	 * @param accessTokenType
	 * @param accessTokenValue
	 * @param requestProperties
	 * @param urlParameters
	 */
	protected static void initParameters(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			Map<String, String> requestProperties,
			Map<String, String> urlParameters) {
		
		//requestProperties= new HashMap<String, String>();
		requestProperties.put(GoogleOpenAuthent.PARAM_AUTH, accessTokenType+GoogleHTTPRequest.SPACE+accessTokenValue);
		
		//urlParameters= new HashMap<String, String>();
		urlParameters.put(GoogleOpenAuthent.PARAM_APIKEY, apiKey);
		
	}
	
	/**
	 * Get the list of calendars defined or shared with the authenticated user
	 * @param apiKey the application key obtained from the Google Developers console 
	 * @param accessTokenType usually "Bearer"
	 * @param accessTokenValue the access code for this service
	 * @return a JSON Object containing an items JSONArray with the configuration for all calendars 
	 */
	public static JSONObject getAllCalendars(String apiKey,
			String accessTokenType,
			String accessTokenValue) {
		// No logs because we may not want to see the token value clear
		
		Map<String, String> requestProperties= new HashMap<String, String>();
		Map<String, String> urlParameters=new HashMap<String, String>();
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
				
		return GoogleHTTPRequest.httpsGet(URL_CALENDARLIST, requestProperties, urlParameters);
	}
	
	/**
	 * Get a summary of a particular Calendar
	 * @param apiKey the application key obtained from the Google Developers console 
	 * @param accessTokenType usually "Bearer"
	 * @param accessTokenValue the access code for this service
	 * @param calendarId the id of the calendar or "primary" for the main calendar of the authenticated user
	 * @return A JSONObject containing the id, the summary (friendly description), and the timezone for a calendar
	 */
	public static JSONObject getCalendarDescription(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			String calendarId) {
		// No logs because we may not want to see the token value clear
		
		Map<String, String> requestProperties= new HashMap<String, String>();
		Map<String, String> urlParameters=new HashMap<String, String>();
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
		
		String serviceURL = URL_CALENDARS+GoogleHTTPRequest.SLASH;
		
		try {
			serviceURL += URLEncoder.encode(calendarId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error encoding the URL : "+e.getMessage());
			return null;
		}				
		return GoogleHTTPRequest.httpsGet(serviceURL,
				requestProperties,
				urlParameters);
	}
	
	public static JSONObject getEvents(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			String calendarId,
			Map<String, String> urlParameters) {
		if(urlParameters == null) {
			urlParameters=new HashMap<String, String>();
		}
		Map<String, String> requestProperties= new HashMap<String, String>();
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
		
		String serviceURL = URL_CALENDARS+GoogleHTTPRequest.SLASH;
		
		try {
			serviceURL += URLEncoder.encode(calendarId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error encoding the URL : "+e.getMessage());
			return null;
		}				
		serviceURL+=GoogleHTTPRequest.SLASH+PARAM_EVENTS;

		return GoogleHTTPRequest.httpsGet(serviceURL,
				requestProperties,
				urlParameters);
	}
	
	public static JSONObject getEvent(String apiKey,
			String accessTokenType,
			String accessTokenValue,
			String calendarId,
			String eventId) {

		Map<String, String> requestProperties= new HashMap<String, String>();
		Map<String, String> urlParameters=new HashMap<String, String>();		
		initParameters(apiKey,accessTokenType,accessTokenValue,requestProperties,urlParameters);
		
		String serviceURL = URL_CALENDARS+GoogleHTTPRequest.SLASH;
		
		try {
			serviceURL += URLEncoder.encode(calendarId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
			serviceURL+=GoogleHTTPRequest.SLASH+PARAM_EVENTS;
			serviceURL += GoogleHTTPRequest.SLASH+URLEncoder.encode(eventId,
					GoogleHTTPRequest.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error encoding the URL : "+e.getMessage());
			return null;
		}				

		return GoogleHTTPRequest.httpsGet(serviceURL,
				requestProperties,
				urlParameters);
	}	
}
