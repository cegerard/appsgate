package appsgate.lig.google.impl;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.Instance;
import appsgate.lig.google.helpers.GoogleCalendarReader;
import appsgate.lig.google.helpers.GoogleCalendarWriter;
import appsgate.lig.google.helpers.GoogleOpenAuthent;
import appsgate.lig.google.helpers.GooglePropertiesHolder;
import appsgate.lig.google.services.GoogleAdapter;
import appsgate.lig.google.services.GoogleEvent;

/**
 */
public class GoogleAdapterImpl extends GooglePropertiesHolder implements GoogleAdapter{

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(GoogleAdapterImpl.class);

	BundleContext context;
	Instance mySelf;

	Object lock;


	
	String currentAccessToken=null;



	public GoogleAdapterImpl(BundleContext context) {
		this.context=context;
		lock=new Object();
	}

	public void start(Instance myself) {
		logger.trace("start()");
		this.mySelf=myself;

		try {
			String configFile = context.getProperty(CONFIGURATION_FILE);
			logger.trace("Configuration file for GoogleAdapterImpl: "+configFile);
			loadFromFile(configFile);

		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}
	}
	
	
	/**
	 * Check Access token, if it was not created or expired, try to renew it
	 * @return true if the access token (renewed or not) is still valid
	 */
	private boolean renewAccessToken() {
		if(!isCompleteConfig()) {
			return false;
		}
		
		if(currentAccessToken == null
				||GoogleOpenAuthent.checkAccessToken(GoogleCalendarReader.SCOPE_CALENDAR_READONLY, currentAccessToken)<MIN_ACCESSTOKEN_EXPIRATION) {
			
			
			JSONObject result = GoogleOpenAuthent.getAccessToken(
					GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTID),
					GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTSECRET),
					GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_REFRESHTOKEN));
			String token = result.optString(GoogleOpenAuthent.PARAM_ACCESSTOKEN);
			
			if(token != null &&
					GoogleOpenAuthent.checkAccessToken(GoogleCalendarReader.SCOPE_CALENDAR_READONLY, token)>MIN_ACCESSTOKEN_EXPIRATION) {
				currentAccessToken =token;
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
		
	}
	
	@Override
	public Set<GoogleEvent> getEvents(String calendarId, Map<String, String> requestParameters) {
		logger.trace("getEvents(String calendarId="+calendarId,
				", Map<String, String> requestParameters="+requestParameters+")");
		if(!renewAccessToken()) {
			logger.error("Cannot get an access token for the calendar");
			return null;
		}
		requestParameters.put(GoogleCalendarReader.PARAM_MAX_RESULTS, GoogleCalendarReader.PARAM_MAX_RESULTS_DEFAULT);
		
		JSONObject result = GoogleCalendarReader.getEvents(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_APIKEY),
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.RESP_TOKENTYPE),
				currentAccessToken,
				calendarId,
				requestParameters);
		
		Set<GoogleEvent> response = new HashSet<GoogleEvent>();
		fillEventResponse( result, response);
		
		String nextPage = result.optString(GoogleCalendarReader.PARAM_NEXT_PAGE_TOKEN);			
		while (nextPage != null && !nextPage.equals("")) {
			logger.trace("next Page : "+nextPage);
			requestParameters.put(GoogleCalendarReader.PARAM_PAGE_TOKEN, nextPage);
			result = GoogleCalendarReader.getEvents(
					GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_APIKEY),
					GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.RESP_TOKENTYPE),
					currentAccessToken,
					calendarId,
					requestParameters);
			fillEventResponse( result, response);

			
			nextPage = result.optString(GoogleCalendarReader.PARAM_NEXT_PAGE_TOKEN);
		}


		
		return response;
	}
	
	private void fillEventResponse(JSONObject result, Set<GoogleEvent> response) {
		if(result==null ||result.has(GoogleOpenAuthent.RESP_ERROR)) {
			logger.error("No results for the request: "+result);
			return ;
		}
		
		JSONArray array = result.optJSONArray(GoogleCalendarReader.RESP_ITEMS);
		
		for(int index=0; array!=null && index<array.length(); index++) {
			JSONObject item = array.optJSONObject(index);
			if(item!= null) {
				try {
					GoogleEvent evt = new GoogleEvent(item);
					response.add(evt);
					logger.trace("getEvents(...), GoogleEvent successfully added");
				} catch (InstantiationException e) {
					logger.warn("Item is not a valid google event , "+item+"  (skipping this item)");
				}
			}
		}		
		
	}

	@Override
	public GoogleEvent addEvent(String calendarId, String requestContent) {
		logger.debug("addEvent(String calendarId="+calendarId,
				", String requestContent="+requestContent+")");
		if(!renewAccessToken()) {
			logger.error("Cannot get an access token for the calendar");
			return null;
		}		
		
		
		JSONObject result = GoogleCalendarWriter.addEvent(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_APIKEY),
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.RESP_TOKENTYPE),
				currentAccessToken,
				calendarId,
				requestContent);
		
		if(result==null ||result.has(GoogleOpenAuthent.RESP_ERROR)) {
			logger.error("No result for the request: "+result);
			return null;
		}
		
		try {
			GoogleEvent event = new GoogleEvent(result);
			logger.trace("addEvent(...), GoogleEvent successfully created : "+event.toString());
			return event;
		} catch (InstantiationException e) {
			logger.warn("Result is not a valid google event : "+result);
			return null;
		}
	}

	@Override
	public boolean deleteEvent(String calendarId, String eventId) {
		logger.debug("deleteEvent(String calendarId="+calendarId,
				", String eventId="+eventId+")");
		if(!renewAccessToken()) {
			logger.error("Cannot get an access token for the calendar");
			return false;
		}		
		
		
		String result = GoogleCalendarWriter.deleteEvent(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_APIKEY),
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.RESP_TOKENTYPE),
				currentAccessToken,
				calendarId,
				eventId);
		
		if(result==null) {
			logger.error("No result for the request: "+result);
			return false;
		} else {
			return true;
		}
		

	}
}
