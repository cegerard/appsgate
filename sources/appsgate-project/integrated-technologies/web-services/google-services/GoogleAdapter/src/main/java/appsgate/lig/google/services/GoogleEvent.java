package appsgate.lig.google.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import appsgate.lig.google.helpers.GoogleHTTPRequest;
import appsgate.lig.scheduler.ScheduledInstruction;
import appsgate.lig.scheduler.SchedulerEvent;

/**
 * Helper Class to build an Object from an Event of the GoogleCalendar (api v3),
 * directly for its json representation (corresponding Google Resource)
 * @author thibaud
 */
public class GoogleEvent implements SchedulerEvent{
		
	private static Logger logger = LoggerFactory.getLogger(GoogleEvent.class);

	public static final String PARAM_ID="id";
	public static final String PARAM_SUMMARY="summary";
	public static final String PARAM_DESCRIPTION="description";
	public static final String PARAM_UPDATED="updated";
	public static final String PARAM_STATUS="status";


	public static final String PARAM_START="start";
	public static final String PARAM_END="end";
	
	public static final String PARAM_DATE="date";
	public static final String PARAM_DATETIME="dateTime";
	
	public static final String PARAM_UNPARSED="unparsedJSON";
	
	public static final String PARAM_STATUS_VALUE_CANCELLED="cancelled";
	public static final String PARAM_STATUS_VALUE_DELETED="deleted";


	public final String SEPARATOR=".";

	


	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getStartTime() {
		return startTime;
	}

	@Override
	public String getEndTime() {
		return endTime;
	}

	@Override
	public String getUpdated() {
		return updated;
	}

	@Override
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
	
	@Override
	public Set<ScheduledInstruction> getOnBeginInstructions() {
		return onBeginInstructions;
	}

	@Override
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
			
			String status = jsonEvent.optString(PARAM_STATUS);
			if(status != null 
					&& ( status.equals(PARAM_STATUS_VALUE_CANCELLED)
							||status.equals(PARAM_STATUS_VALUE_CANCELLED))) {
				throw new InstantiationException("Event status is cancelled : "
						+status);
			}
			
			JSONObject start = jsonEvent.getJSONObject(PARAM_START);
			// TODO= check all day event (then add the start time, and the correct calendar
			// TODO: if dateTime, check the TimeZone and add a default one
			startTime=hackDateTimeZone(start.getString(PARAM_DATETIME));
			
			
			JSONObject end = jsonEvent.getJSONObject(PARAM_END);
			endTime=hackDateTimeZone(end.getString(PARAM_DATETIME));
			
			description=jsonEvent.optString(PARAM_DESCRIPTION); // This one is optional
			onBeginInstructions=parseDescription(description, ScheduledInstruction.ON_BEGIN);
			onEndInstructions=parseDescription(description, ScheduledInstruction.ON_END);
			
			
			
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
	public static String hackDateTimeZone(String inputDate) {
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
	
	/**
	 * @return a JSON representation of the GoogleEvent.
	 * (Please note that this is the full information about the Event coming initially from google Calendar)
	 */
	@Override
	public JSONObject toJSON() {
		return new JSONObject(unparsedJSON);
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
			currentEvent+="\n"+ScheduledInstruction.ON_BEGIN+GoogleHTTPRequest.EQUAL+inst.toString();
		}
		for(ScheduledInstruction inst : onEndInstructions) {
			currentEvent+="\n"+ScheduledInstruction.ON_END+GoogleHTTPRequest.EQUAL+inst.toString();
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
								line.substring(keyword.length()+ScheduledInstruction.SEPARATOR.length()),
										keyword);
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
	
	
	@Override
	public boolean isSchedulingProgram(String programId) {
		logger.trace("isSchedulingProgram(String programId : "+programId+")");
		
		for (ScheduledInstruction inst : getOnBeginInstructions() ) {
			if(programId.equals(inst.getTarget())) {
				logger.trace("isSchedulingProgram(...), found on begin event"
						+", instruction : "+inst);
					return true;
			}
		}
		
		for (ScheduledInstruction inst : getOnEndInstructions() ) {
			if(programId.equals(inst.getTarget())) {
				logger.trace("isSchedulingProgram(...), found on end event"
						+", instruction : "+inst);
					return true;
			}
		}
		
		logger.trace("isSchedulingProgram(...), program id not found in event");
		return false;
		
	}

	@Override
	public Set<ScheduledInstruction> instructionsMatchingPattern(String pattern) {
		logger.trace("instructionsMatchesPattern(String pattern : "+pattern			
				+")");
		
		Set<ScheduledInstruction> result = new HashSet<ScheduledInstruction>();
		
		if(pattern==null ||pattern.length()==0) {
			logger.warn("instructionsMatchesPattern(...), no pattern specified");
			return result ; //none by default
		}
	
		for(ScheduledInstruction inst : onBeginInstructions) {
			if (inst.matchesPattern(pattern)) {
				result.add(inst);
			}
		}
		for(ScheduledInstruction inst : onEndInstructions) {
			if (inst.matchesPattern(pattern)) {
				result.add(inst);
			}
		}		
		return result;
	}

}
