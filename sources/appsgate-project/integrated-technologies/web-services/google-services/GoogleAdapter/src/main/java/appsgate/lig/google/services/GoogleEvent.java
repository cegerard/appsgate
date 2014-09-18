package appsgate.lig.google.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import appsgate.lig.google.helpers.GoogleHTTPRequest;

/**
 * Helper Class to build an Object from an Event of the GoogleCalendar (api v3),
 * directly for its json representation (corresponding Google Resource)
 * @author thibaud
 */
public class GoogleEvent {
		
	private static Logger logger = LoggerFactory.getLogger(GoogleEvent.class);

	public static final String PARAM_ID="id";
	public static final String PARAM_SUMMARY="summary";
	public static final String PARAM_DESCRIPTION="description";
	public static final String PARAM_UPDATED="updated";

	public static final String PARAM_START="start";
	public static final String PARAM_END="end";
	
	public static final String PARAM_DATE="date";
	public static final String PARAM_DATETIME="dateTime";
	
	public static final String PARAM_UNPARSED="unparsedJSON";
	
	/**
	 * Reserved keyword for AppsGate in the description,
	 * to map appsgate instruction (such as appsgate schedules)
	 * To the beginning of the event
	 * (one instruction per line, must start with the keyword and the '=' separator
	 * Ex: begin=start.program-5524
	 */
	public static final String ON_BEGIN="begin";
	
	/**
	 * Reserved keyword for AppsGate in the description,
	 * to map appsgate instruction (such as appsgate schedules)
	 * To the end of the event
	 * (one instruction per line, must start with the keyword and the '=' separator
	 * Ex: end=stop.program-5524
	 */	
	public static final String ON_END="end";

	public final String SEPARATOR=".";

	


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
	
	public Set<ScheduledInstruction> getOnBeginInstructions() {
		return onBeginInstructions;
	}

	public Set<ScheduledInstruction> getOnEndInstructions() {
		return onEndInstructions;
	}

	public void setOnBeginInstructions(Set<ScheduledInstruction> onBeginInstructions) {
		this.onBeginInstructions = onBeginInstructions;
	}

	public void setOnEndInstructions(Set<ScheduledInstruction> onEndInstructions) {
		this.onEndInstructions = onEndInstructions;
	}	

	String id=null;
	String startTime=null;
	String endTime=null;
	String updated=null;
	String name=null;
	String description=null;
	String unparsedJSON=null;

	Set<ScheduledInstruction> onBeginInstructions=new HashSet<ScheduledInstruction>();
	Set<ScheduledInstruction> onEndInstructions=new HashSet<ScheduledInstruction>();

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
			updated=jsonEvent.getString(PARAM_UPDATED);
			
			JSONObject start = jsonEvent.getJSONObject(PARAM_START);
			// TODO= check all day event (then add the start time, and the correct calendar
			// TODO: if dateTime, check the TimeZone and add a default one
			startTime=hackDateTimeZone(start.getString(PARAM_DATETIME));
			
			
			JSONObject end = jsonEvent.getJSONObject(PARAM_END);
			endTime=hackDateTimeZone(end.getString(PARAM_DATETIME));
			
			description=jsonEvent.optString(PARAM_DESCRIPTION); // This one is optional
			onBeginInstructions=parseDescription(description, ON_BEGIN);
			onEndInstructions=parseDescription(description, ON_END);
			
			
			
		} catch (JSONException exc) {
			throw new InstantiationException("Missing json parameter : "
					+exc.getMessage());
		} catch (NullPointerException exc ) {
			throw new InstantiationException("No json representation of the event provided (or its subelement)"
					+exc.getMessage());
		}
	}
	/**
	 * This one is pretty boring, java handle timezone offset such as "+0200"
	 * But google send TimeZone offset like "+02:00", so we have to remove this ':'
	 * @param inputDate
	 * @return
	 */
	private static String hackDateTimeZone(String inputDate) {
		//  first check the separator between Date and Time
		int index = inputDate.indexOf('T');
		
		if(index<0) {
			return inputDate;
		}		
		// then check the + or - that indicates the Timezone offset
		index = inputDate.indexOf('+',index);
		if(index<0) {
			index = inputDate.indexOf('-', index);
		}
		if(index<0) {
			return inputDate;
		}	
		// To finally get the ':' character to remove
		index= inputDate.indexOf(':', index);
		if(index>0) {
			return inputDate.substring(0,index)+inputDate.substring(index+1);
		} else {
			return inputDate;
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
		for(ScheduledInstruction inst : onBeginInstructions) {
			currentEvent+="\n"+ON_BEGIN+GoogleHTTPRequest.EQUAL+inst.toString();
		}
		for(ScheduledInstruction inst : onEndInstructions) {
			currentEvent+="\n"+ON_END+GoogleHTTPRequest.EQUAL+inst.toString();
		}		
		currentEvent+="\n"+PARAM_UNPARSED+GoogleHTTPRequest.EQUAL+unparsedJSON;

		return currentEvent;
	}
	
	
	/**
	 * Parse the description part of a Calendar Event to fetch specific AppsGate keywords
	 * @param description
	 * @return an set of appsgate instructions may be empty but should not be null
	 */
	public static Set<ScheduledInstruction> parseDescription(String description, String keyword) {
		Set<ScheduledInstruction> instructions=new HashSet<ScheduledInstruction>();
		
		if(description != null &&keyword!=null && description.length()>0
				&& keyword.length()>0) {
			BufferedReader bfr = new BufferedReader(new StringReader(description));
			String line=null;
			try{
				line = bfr.readLine();
				while (line!=null) {
					if (line.startsWith(keyword)) {
						ScheduledInstruction inst = ScheduledInstruction.parseInstruction(
								line.substring(keyword.length()+ScheduledInstruction.SEPARATOR.length()  ));
						if(inst != null ) {
							instructions.add(inst);
							logger.trace("Added instruction on "+keyword+" : "+inst.toString());
						}else {
							logger.info("Unrecognized instruction : "+line);
						}
					}
					line = bfr.readLine();
				}
			} catch (IOException exc) {
				logger.error("Error while parsing "+line+", message : "+exc.getMessage());	
			}
		}
		return instructions;
	}

}
