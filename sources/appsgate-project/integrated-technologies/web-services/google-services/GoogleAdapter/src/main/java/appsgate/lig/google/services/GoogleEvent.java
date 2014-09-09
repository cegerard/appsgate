package appsgate.lig.google.services;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.google.helpers.GoogleHTTPRequest;

/**
 * Helper Class to build an Object from an Event of the GoogleCalendar (api v3),
 * directly for its json representation (corresponding Google Resource)
 * @author thibaud
 */
public class GoogleEvent {
	
	public static final String PARAM_ID="id";
	public static final String PARAM_SUMMARY="summary";
	public static final String PARAM_DESCRIPTION="description";
	public static final String PARAM_UPDATED="updated";

	public static final String PARAM_START="start";
	public static final String PARAM_END="end";
	
	public static final String PARAM_DATE="date";
	public static final String PARAM_DATETIME="dateTime";
	
	public static final String PARAM_UNPARSED="unparsedJSON";


	public String getId() {
		return id;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getUpdated() {
		return updated;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	String id=null;
	String startTime=null;
	String endTime=null;
	String updated=null;
	String name=null;
	String description=null;
	String unparsedJSON=null;
		
	Set<String> appsGateInstructions=new HashSet<String>();
	
	

	/**
	 * Parse a google calendar event to build the corresponding java object
	 * Restrictions: Does not work for allday events, or may not work properly if the timezone Offset is not set properly
	 * @param jsonEvent
	 * @throws InstantiationException
	 */
	public GoogleEvent(JSONObject jsonEvent) throws InstantiationException{
		try {			
			unparsedJSON = jsonEvent.toString();
			
			id=jsonEvent.getString(PARAM_ID);
			name=jsonEvent.getString(PARAM_SUMMARY);
			description=jsonEvent.optString(PARAM_DESCRIPTION); // This one is optional
			updated=jsonEvent.getString(PARAM_UPDATED);
			
			JSONObject start = jsonEvent.getJSONObject(PARAM_START);
			// TODO= check all day event (then add the start time, and the correct calendar
			// TODO: if dateTime, check the TimeZone and add a default one
			startTime=start.getString(PARAM_DATETIME);
			
			JSONObject end = jsonEvent.getJSONObject(PARAM_END);
			endTime=end.getString(PARAM_DATETIME);
			
			
		} catch (JSONException exc) {
			throw new InstantiationException("Missing json parameter : "
					+exc.getMessage());
		} catch (NullPointerException exc ) {
			throw new InstantiationException("No json representation of the event provided (or its subelement)"
					+exc.getMessage());
		}
	}
	
	@Override
	public String toString() {
		String currentEvent = "";
		currentEvent+=PARAM_ID+GoogleHTTPRequest.EQUAL+id;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_SUMMARY+GoogleHTTPRequest.EQUAL+name;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_DESCRIPTION+GoogleHTTPRequest.EQUAL+description;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_UPDATED+GoogleHTTPRequest.EQUAL+updated;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_START+GoogleHTTPRequest.EQUAL+startTime;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_END+GoogleHTTPRequest.EQUAL+endTime;
		currentEvent+=GoogleHTTPRequest.COMMA+PARAM_UNPARSED+GoogleHTTPRequest.EQUAL+unparsedJSON;
		return currentEvent;
	}
	

}
