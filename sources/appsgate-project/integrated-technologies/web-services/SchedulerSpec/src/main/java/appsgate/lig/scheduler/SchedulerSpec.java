package appsgate.lig.scheduler;


import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.clock.sensor.messages.ClockSetNotificationMsg;
import appsgate.lig.clock.sensor.messages.FlowRateSetNotification;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.scheduler.SchedulerEvent.BasicRecurrencePattern;

/**
 */
public interface SchedulerSpec extends AlarmEventObserver{

	/**
	 * This constant define the t0 -> t1 interval in which we observe the Calendar Events (in ms)
	 */
	public static final long TIME_INTERVAL = 24*60*60*1000;

	/**
	 * Even if no events from the clock, the scheduler will scan again all events in the calendar
	 * at this rate in ms
	 * (it should be defined depends on the time flow rate, 10 minutes intervals in real time should be fine)
	 */
	public static final long BASE_REFRESH = 2*60*1000;

	/**
	 * A single scheduler monitor a single calendar, this is the getter
	 * @return
	 */
	public String getCalendarId();
	
	/**
	 * List a set of Events that contain in their "description" field the specified regex pattern
	 * @param regexPattern must be a valid java regex pattern (for instance "toto*tutu[0-9]{4}"
	 *  (we do not check if the program-id really exist as we parse Google Calendar events)
	 * @param startPeriod the starting period to observe, if -1 we start from the beginning of the calendar
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @param endPeriod the ending of the period to observe, if -1 we parse until no more events left
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @return The set of Events as a JSONArray (each events is a JSONObject containing all the event fields)
	 */
	public JSONArray listEventsMatchingPattern(String regexPattern, String startPeriod, String endPeriod)
			throws SchedulingException;
	
	/**
	 * Check if a particular program Id is scheduled in the future (on start or on ending)
	 * @param progamId the program-id (not the program name)
	 * @return true if the program is scheduled in the future
	 *  (we do not check if the program-id really exist as we parse Google Calendar events)
	 */
	public boolean checkProgramIdScheduled(String programId)
			throws SchedulingException;
	
	/**
	 * Check programs that are scheduled in the future (on start or on ending)
	 * @return a JSON Array containing all program scheduled in the future
	 *  (we do not check if the program-id really exist as we only parse Google Calendar events)
	 */
	public JSONArray checkProgramsScheduled()
			throws SchedulingException;
	

	/**
	 * A single scheduler monitor a single calendar, this is the setter
	 * @param calendarId
	 */
	public void setCalendarId(String calendarId);
	
	/**
	 * This method implements the main behavior of the scheduler
	 * that polls the calendar to get new events or update existing ones
	 */
	public void refreshScheduler() throws SchedulingException;
	
	/**
	 * Clear all events registered and then refresh 
	 */
	public void resetScheduler() throws SchedulingException;


	/**
	 * (This one is the legacy method only applicable to start/stop program,
	 * the general purpose with command name and parameter should be used instead (it allows all kind of commands
	 * Create a basic Calendar Event, at AppsGate Clock Time, to schedule the start or stop of a program
	 * The Event is created just one hour before current Time, and last for 30 minutes
	 * Changed specification, if start is false AND stop is true, then the program should stop at the beginning of the event
	 * @param eventName is the name as it will appear in the Calendar
	 * @param programId is not checked, but should be a VALID program ID referenced by EUDE Inteprpreter
	 * @param startOnBegin if program should start when Event begin
	 * @param stopOnEnd if program should start when Event end
	 * @return the Event ID
	 * @throws SchedulingException
	 */
	public String createEvent(String eventName, String programId, boolean startOnBegin, boolean stopOnEnd) throws SchedulingException;
	
	
	/**
	 * Create a basic and single Calendar Event, with specified instructions
	 * @param eventName is the name as it will appear in the Calendar
	 * @param onBeginInstructions instruction to be triggered when event start
	 * @param onEndInstructions instruction to be triggered when event stops
	 * @param dateStart the starting date of the event
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @param dateEnd the ending date of the event
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @return the Event ID
	 * @throws SchedulingException
	 */
	public String createEvent(String eventName,
			Set<ScheduledInstruction> onBeginInstructions,
			Set<ScheduledInstruction> onEndInstructions,
			String dateStart, String dateEnd ) throws SchedulingException;
	
	/**
	 * Create a Calendar Event (this one can handle a basic recurrence),
	 * with specified instructions
	 * @param eventName is the name as it will appear in the Calendar
	 * @param onBeginInstructions instruction to be triggered when event start
	 * @param onEndInstructions instruction to be triggered when event stops
	 * @param dateStart the starting date of the event
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @param dateEnd the ending date of the event
	 * (formatted according to RFC 3339 : 2014-09-16T12:45:23+0200) 
	 * @param a reccurrence pattern for the event
	 * @return the Event ID
	 * @throws SchedulingException
	 */
	public String createEvent(String eventName,
			Set<ScheduledInstruction> onBeginInstructions,
			Set<ScheduledInstruction> onEndInstructions,
			String dateStart, String dateEnd,
			BasicRecurrencePattern recurrence ) throws SchedulingException;
	
	/**
	 * Get a particular event in the calendar
	 * @param the unique Event ID 
	 * @return the Scheduler event, that main contains additionnal properties provided by the underlying implementation
	 * or null if the event ID is not found or the event not valid
	 */
	public SchedulerEvent getEvent(String eventID);	
	
	/**
	 * The method try to remove an event using its eventID
	 * @return true if event was successfully removed from calendar
	 */
	public boolean removeEvent(String eventID);

	/**
	 * The method implements what to do when a registered events occurs
	 */
	@Override
	public void alarmEventFired(int alarmId);


	/**
	 * Called by ApAM when clock changes flow rate
	 * part by calling the sendService
	 *
	 * @param notif the notification message from ApAM
	 */
	public void clockFlowRateChanged(FlowRateSetNotification notif);

	/**
	 * Called by ApAM when clock changes changed current time
	 *
	 * @param notif the notification message from ApAM
	 */
	public void clockSetChanged(ClockSetNotificationMsg notif);
}
