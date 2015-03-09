package appsgate.lig.google.scheduler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.messages.ClockSetNotificationMsg;
import appsgate.lig.clock.sensor.messages.FlowRateSetNotification;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.ehmi.spec.EHMIProxySpec;
import appsgate.lig.google.helpers.GoogleCalendarReader;
import appsgate.lig.google.services.GoogleAdapter;
import appsgate.lig.google.services.GoogleEvent;
import appsgate.lig.scheduler.ScheduledInstruction;
import appsgate.lig.scheduler.SchedulerSpec;
import appsgate.lig.scheduler.SchedulingException;


/**
 * 
 */
public class GoogleScheduler implements SchedulerSpec, AlarmEventObserver {

	private static Logger logger = LoggerFactory.getLogger(GoogleScheduler.class);

	/**
	 * This constant define the t0 -> t1 interval in which we observe the Calendar Events (in ms)
	 */
	private long observationInterval = TIME_INTERVAL;


	/**
	 * Even if no events from the clock, the scheduler will scan again all events in the calendar
	 * at this rate in ms
	 * (it should be defined depends on the time flow rate, 10 minutes intervals in real time should be fine)
	 */
	private long currentRefresh = BASE_REFRESH;


	/**
	 * This one is for optimization purpose,
	 * we will not refresh the calendar unless the Lease
	 * between current Time and timeStamp does not excess this lease (in ms)
	 * except modification of the "simulated" clock time
	 */
	private long refreshLease = 2000;

	/**
	 * timeStamp use System.currentTimeMillis()
	 * the real system clock time
	 */
	private long timeStamp;

	// this one will be injected by ApAM
	private GoogleAdapter serviceAdapter;

	// this one will be injected by ApAM
	CoreClockSpec clock;

	// this one will be injected by ApAM
	EHMIProxySpec ehmiService;


	Object lock;

	Map<Integer, String> onBeginAlarms=new HashMap<Integer, String>();
	Map<Integer, String> onEndAlarms=new HashMap<Integer, String>();

	Map<String, GoogleEvent> eventMap = new HashMap<String, GoogleEvent>();

	/*
	 * By default if the user as granted access to GoogleCalendar service
	 * He should have a primary calendar,
	 * nevertheless this parameter should be overridden with a calendar dedicated to the smart home 
	 */
	String calendarId = "primary";

	public static SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Override
	public String getCalendarId() {
		return calendarId;
	}

	@Override
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public GoogleScheduler() {
		lock=new Object();
		timeStamp = 0;
	}

	public void start() {

		//Next refresh will in 30secs (Clock, EHMI and Google calendar should be available by then)
		refreshTask(30 * 1000);
		logger.trace("started successfully, waiting for ScheduleAutoRefresh to wake up");
	}

	@Override
	public void refreshScheduler() throws SchedulingException {
		logger.trace("refreshScheduler()");
		if(clock==null) {
			logger.error("No clock service registered, aborting reset");
			throw new SchedulingException("No clock service registered, aborting reset");
		}

		synchronized (lock) {
			long currentTime = clock.getCurrentTimeInMillis();
			if((System.currentTimeMillis()-timeStamp)>refreshLease) {
				timeStamp = System.currentTimeMillis();

				Set <GoogleEvent> events = getEvents(currentTime);

				// If no events, we do nothing (expecting the refresh timer to wake up in case of new events registered)
				if(events==null ||events.size()<1) {
					logger.info("No events registered for the next "+observationInterval/(1000*60*60)+" hours");
					return;
				}

				for (GoogleEvent event : events) {
					//if updatedTime have changed we must consider that its content may have changed and therefore register all again
					if(event.getId() !=null) {
						if(eventMap.containsKey(event.getId())
								&& !event.getUpdated().equals(eventMap.get(event.getId()).getUpdated())) {
							eventMap.remove(event.getId());
							// We have to clean all reference to the eventId in the onBegin and onEnd maps
							removeEventFromAlarms(event.getId());
							registerEventAlarms(event, currentTime);
						} else if (!eventMap.containsKey(event.getId())) {
							registerEventAlarms(event, currentTime);
						}
					}
				}
			}
		}
	}

	/**
	 * onBeginAlarm and onEndAlarm entries containing eventId will be removed 
	 * @param eventId
	 */
	private void removeEventFromAlarms(String eventId) {
		logger.trace("removeEventFromAlarms(String eventId : "+eventId+")");
		synchronized (lock) {			

			for(int alarmId: onBeginAlarms.keySet()) {
				if(onBeginAlarms.get(alarmId).equals(eventId)) {
					onBeginAlarms.remove(alarmId);
				}		
			}
			for(int alarmId: onEndAlarms.keySet()) {
				if(onEndAlarms.get(alarmId).equals(eventId)) {
					onEndAlarms.remove(alarmId);
				}		
			}
		}
	}

	private Set <GoogleEvent> getEvents(long startTime, long endTime) throws SchedulingException{
		logger.trace("getEvents(long startTime : "+startTime
				+"long endTime : "+endTime+")");

		if(serviceAdapter==null) {
			logger.error("No GoogleAdapter service registered, unavailable to get events");
			throw new SchedulingException("No GoogleAdapter service registered, unavailable to get events");
		}

		Map<String, String> requestParameters=new HashMap<String, String>();
		if(startTime != -1) {
			requestParameters.put(GoogleCalendarReader.PARAM_TIMEMIN, dateFormat.format(new Date(startTime)));
		}
		if(endTime != -1) {		
			requestParameters.put(GoogleCalendarReader.PARAM_TIMEMAX, dateFormat.format(new Date(endTime)));
		}

		// if request has an open period, we do have to check specifically reccuring events (only the first one is considered)
		if(startTime == -1 || endTime == -1) {
			return serviceAdapter.getEvents(calendarId, requestParameters);
		} else {
			// else all events are retruning, including multple occurence of recuring events
			requestParameters.put(GoogleCalendarReader.PARAM_SINGLE_EVENTS, Boolean.TRUE.toString());
			return serviceAdapter.getEvents(calendarId, requestParameters);			
		}


	}


	private Set <GoogleEvent> getEvents(long startTime) throws SchedulingException{
		logger.trace("getEvents(long startTime : "+startTime+")");

		long endPeriod = startTime + observationInterval;

		return getEvents(startTime, endPeriod);

	}

	@Override	
	public void resetScheduler() throws SchedulingException{
		logger.trace("resetScheduler()");

		if(clock==null) {
			logger.error("No clock service registered, aborting reset");
			throw new SchedulingException("No clock service registered, aborting reset");
		}
		synchronized (lock) {
			// Step 1: unregister alarms and clearing all registered events  
			for(Integer i : onBeginAlarms.keySet()) {
				clock.unregisterAlarm(i.intValue());
			}
			for(Integer i : onEndAlarms.keySet()) {
				clock.unregisterAlarm(i.intValue());
			}
			clock.calculateNextTimer();
			onBeginAlarms.clear();
			onEndAlarms.clear();
			eventMap.clear();
			timeStamp=0; // force the refresh of the Scheduler
		}
		refreshScheduler();

	}

	/**
	 * Deprecated test
	 * @param args
	 */
	@Deprecated
	public static void main(String[] args) {
		try {
			//dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			SimpleDateFormat dateFormatbis=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			System.out.println(dateFormatbis.parse("2014-09-16T12:45:23+0200"));
			dateFormat.parse("2014-09-16T18:27:00+0200");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerEventAlarms(GoogleEvent event, long currentTime) {
		logger.trace("registerEventAlarms for : "+event.toString());

		// Check onBegin date, if already passed, skip all begin instructions
		synchronized (lock) {			
			if(event.getOnBeginInstructions().size()>0) {
				try {
					Date begin = dateFormat.parse(event.getStartTime());
					if(begin.after(new Date(currentTime))) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(begin);
						int alarmId = clock.registerAlarm(cal, this);
						onBeginAlarms.put(alarmId, event.getId());
						eventMap.put(event.getId(), event);
						logger.trace("Adding on begin alarm with id "+alarmId+" on event : "+event.toString());
					} else {
						logger.trace("Event has already begun :"+event.getStartTime()+"skipping it");
					}

				} catch (ParseException e) {
					logger.error("Error while parsing the start date : "+e.getMessage()
							+", start date : "+event.getStartTime());
				}
			}

			// End date should not be passed, always register end instructions
			if(event.getOnEndInstructions().size()>0) {
				try {
					Date end=dateFormat.parse(event.getEndTime());
					Calendar cal = Calendar.getInstance();
					cal.setTime(end);
					int alarmId = clock.registerAlarm(cal, this);
					onEndAlarms.put(alarmId, event.getId());
					eventMap.put(event.getId(), event);
					logger.trace("Adding on end alarm with id "+alarmId+" on event : "+event.toString());
				} catch (ParseException e) {
					logger.error("Error while parsing the end date : "+e.getMessage()
							+", start date : "+event.getEndTime());
				}
			}
		}

	}


	private void triggerInstruction(String command, String target) throws SchedulingException {
		logger.trace("triggerInstruction(String command : "+command
				+", String target : "+target+")");

		if(ehmiService==null) {
			throw new SchedulingException("No EHMI Proxy service registered, instruction will not be triggered");
		}
		if(ScheduledInstruction.CALL_PROGRAM.equals(command)) {
			ehmiService.callProgram(target);			
		} else if (ScheduledInstruction.STOP_PROGRAM.equals(command)) {
			ehmiService.stopProgram(target);
		} else {
			logger.warn("Command unknown : " +command); 
		}


	}

	@Override
	public void alarmEventFired(int alarmId) {
		logger.trace("alarmEventFired(int alarmId : "+alarmId+")");

		synchronized (lock) {
			String eventId = null;
			if(onBeginAlarms.containsKey(alarmId)) {
				eventId = onBeginAlarms.get(alarmId);
				logger.debug("alarmId :"+alarmId+", occured triggering onBegin instructions for Google Calendar EventId : "+eventId);
				GoogleEvent event = eventMap.get(eventId);
				for(ScheduledInstruction instruction : event.getOnBeginInstructions()) {
					try {
						triggerInstruction(instruction.getCommand(), instruction.getTarget());
					} catch(SchedulingException exc) {
						logger.error("Error while triggering instruction : "+instruction.toString()+", cause : "+exc.getMessage());
					}
				}
				onBeginAlarms.remove(alarmId);

			} else if (onEndAlarms.containsKey(alarmId)) { 
				eventId = onEndAlarms.get(alarmId);
				logger.debug("alarmId :"+alarmId+", occured triggering onEnd instructions for Google Calendar EventId : "+eventId);
				GoogleEvent event = eventMap.get(eventId);
				for(ScheduledInstruction instruction : event.getOnEndInstructions()) {
					try {
						triggerInstruction(instruction.getCommand(), instruction.getTarget());
					} catch(SchedulingException exc) {
						logger.error("Error while triggering instruction : "+instruction.toString()+", cause : "+exc.getMessage());
					}
				}
				onEndAlarms.remove(alarmId);
			} else {
				logger.warn("alarmId : "+alarmId+" unknown, doing nothing");
			}
			// If this event is no more used we can remove it
			if(eventId != null 
					&& !onBeginAlarms.containsValue(eventId)
					&& !onEndAlarms.containsValue(eventId)) {
				logger.trace("Removing unused event : "+eventId);
				eventMap.remove(eventId);
			}	
		}
		// In case, we refresh the scheduler
		try{
			refreshScheduler();
		} catch(SchedulingException exc) {
			logger.error("Error occured, scheduler has not been refreshed : "+exc.getMessage());
		}
	}



	/**
	 * Called by ApAM when clock changes flow rate
	 * part by calling the sendService
	 *
	 * @param notif the notification message from ApAM
	 */
	@Override	
	public void clockFlowRateChanged(FlowRateSetNotification notif) {
		logger.trace("clockFlowRateChanged(FlowRateSetNotification notif =  " + notif.JSONize()+")");
		double refresh = BASE_REFRESH  * Double.parseDouble(notif.getNewValue());
		currentRefresh = (long)refresh;
		refreshTask(currentRefresh);
		logger.debug("refresh Task successfully changed");
		try {
			resetScheduler();
			logger.debug("All schedules have been computed again");
		} catch(SchedulingException exc) {
			logger.error("Error occured, scheduler has not been refreshed : "+exc.getMessage());
		}
	}

	/**
	 * default and public method
	 * that schedule an auto-refresh with the currentRefresh value
	 */
	public void refreshTask() {
		refreshTask(currentRefresh);
	}

	Timer timer=null;

	private synchronized void refreshTask(long nextRefresh) {

		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		ScheduleAutoRefresh refreshTask = new ScheduleAutoRefresh(this);
		timer = new Timer();
		timer.schedule(refreshTask, nextRefresh);
	}    


	/**
	 * Called by ApAM when clock changes changed current time
	 *
	 * @param notif the notification message from ApAM
	 */
	@Override	
	public void clockSetChanged(ClockSetNotificationMsg notif) {
		logger.trace("clockSetChanged(ClockSetNotificationMsg notif =  " + notif.JSONize()+")");
		try {
			resetScheduler();
			logger.debug("All schedules have been computed again");
		} catch(SchedulingException exc) {
			logger.error("Error occured, scheduler has not been refreshed : "+exc.getMessage());
		}
	}

	public String createEvent(String eventName, 
			Set<ScheduledInstruction> onBeginInstructions,
			Set<ScheduledInstruction> onEndInstructions,
			String dateStart, String dateEnd ) throws SchedulingException {
		logger.trace("String createEvent(String eventName : "+eventName 
				+ ", Set<ScheduledInstruction> onBeginInstructions : "+onBeginInstructions
				+ ", Set<ScheduledInstruction> onEndInstructions : "+onEndInstructions
				+",String dateStart : "+dateStart
				+",String dateEnd : "+dateEnd	
				+")");
		if(serviceAdapter==null) {
			logger.error("No GoogleAdapter service registered, unable to add events");
			throw new SchedulingException("No GoogleAdapter service registered, unable to add events");
		}
		try {		
			dateFormat.parse(GoogleEvent.hackDateTimeZone(dateStart)).getTime();
		}catch (Exception exc ) {
			logger.error("Cannot Parse dateStart : "+exc.getMessage());
			throw new SchedulingException("Cannot Parse dateStart : "+exc.getMessage());			
		}
		try {		 	
			dateFormat.parse(GoogleEvent.hackDateTimeZone(dateEnd)).getTime();
		}catch (Exception exc ) {
			logger.error("Cannot Parse dateEnd : "+exc.getMessage());
			throw new SchedulingException("Cannot Parse dateEnd : "+exc.getMessage());			
		}

		String requestContent;

		JSONObject content = new JSONObject();
		content.put("start", new JSONObject().put("dateTime",  dateStart));
		content.put("end", new JSONObject().put("dateTime",  dateEnd));

		content.put("summary", eventName);

		String description="";
		if(onBeginInstructions != null ) {
			for(ScheduledInstruction inst : onBeginInstructions) {
				description += GoogleEvent.ON_BEGIN
						+ ScheduledInstruction.SEPARATOR;			
				description+=inst.toString();
				description+="\n";
			}		
		}

		if(onEndInstructions != null ) {		
			for(ScheduledInstruction inst : onEndInstructions) {
				description += GoogleEvent.ON_END
						+ ScheduledInstruction.SEPARATOR;			
				description+=inst.toString();
				description+="\n";
			}
		}

		content.put("description", description);

		requestContent=content.toString();

		GoogleEvent event = serviceAdapter.addEvent(calendarId, requestContent);

		if( event == null) {
			throw new SchedulingException("Error during creation of the Calender Event, returned null event");
		}
		logger.trace("String createEvent(...), event created successfully, returning eventId : "+event.getId());

		return event.getId();


	}

	@Override
	public String createEvent(String eventName, String programId, boolean startOnBegin,
			boolean stopOnEnd) throws SchedulingException {
		logger.trace("String createEvent(String eventName : "+eventName 
				+ ", String programId : "+programId
				+", boolean startOnBegin: "+startOnBegin
				+", boolean stopOnEnd: "+stopOnEnd
				+")");

		if(serviceAdapter==null) {
			logger.error("No GoogleAdapter service registered, unable to add events");
			throw new SchedulingException("No GoogleAdapter service registered, unable to add events");
		}

		long currentTime;
		if (clock==null) {
			logger.info("No CoreClock service registered, using system time to create the Event");
			currentTime = System.currentTimeMillis();
		} else {
			currentTime = clock.getCurrentTimeInMillis();
		}

		String requestContent;

		//Creating an event starting one hour before
		Date startDate = new Date(currentTime - (60*60*1000));

		//The event should end half an hour before current Time
		Date endDate = new Date(currentTime - (30*60*1000));

		JSONObject content = new JSONObject();
		content.put("start", new JSONObject().put("dateTime",  dateFormat.format(startDate)));
		content.put("end", new JSONObject().put("dateTime",  dateFormat.format(endDate)));


		content.put("summary", eventName);

		String description="";
		if(startOnBegin) {
			description += GoogleEvent.ON_BEGIN
					+ ScheduledInstruction.SEPARATOR
					+ ScheduledInstruction.CALL_PROGRAM
					+ ScheduledInstruction.SEPARATOR
					+ programId
					+ "\n";
		}
		if(stopOnEnd && startOnBegin) {
			description += GoogleEvent.ON_END
					+ ScheduledInstruction.SEPARATOR
					+ ScheduledInstruction.STOP_PROGRAM
					+ ScheduledInstruction.SEPARATOR
					+ programId
					+ "\n";
		}else if (stopOnEnd && !startOnBegin) {
				description += GoogleEvent.ON_BEGIN
						+ ScheduledInstruction.SEPARATOR
						+ ScheduledInstruction.STOP_PROGRAM
						+ ScheduledInstruction.SEPARATOR
						+ programId
						+ "\n";
		}

		content.put("description", description);

		requestContent=content.toString();

		GoogleEvent event = serviceAdapter.addEvent(calendarId, requestContent);

		if( event == null) {
			throw new SchedulingException("Error during creation of the Calender Event, returned null event");
		}
		logger.trace("String createEvent(...), event created successfully, returning eventId : "+event.getId());

		return event.getId();
	}

	@Override
	public Set<?> listEventsSchedulingProgramId(String programId,
			String startPeriod, String endPeriod) throws SchedulingException {
		logger.trace("listEventsSchedulingProgramId(String programId : "+programId
				+",String startPeriod : "+startPeriod
				+",String endPeriod : "+endPeriod				
				+")");

		if(programId==null ||programId.length()==0) {
			logger.warn("listEventsSchedulingProgramId(...), no programId specified");
			return null;
		}

		long starting;
		try {		
			starting = dateFormat.parse(GoogleEvent.hackDateTimeZone(startPeriod)).getTime();
		}catch (Exception exc ) {
			logger.debug("Cannot Parse startPeriod : "+exc.getMessage());
			starting = -1;
		}
		long stopping;
		try {		 	
			stopping = dateFormat.parse(GoogleEvent.hackDateTimeZone(endPeriod)).getTime();
		}catch (Exception exc ) {
			logger.debug("Cannot Parse endPeriod : "+exc.getMessage());
			stopping = -1;
		}

		Set <GoogleEvent> bigList = getEvents(starting, stopping);
		Set <GoogleEvent> results = new HashSet<GoogleEvent>();
		
		if(bigList == null) {
			logger.trace("checkProgramIdScheduled(...), no Events registered,");
			return results;
		}

		for(GoogleEvent event : bigList) {
			if (event.isSchedulingProgram(programId)) {
				logger.trace("checkProgramIdScheduled(...), adding event : "+event.getName());
				results.add(event);
			}
		}

		return results;
	}


	// The check implementation stop at first scheduling (optimization)
	@Override
	public boolean checkProgramIdScheduled(String programId) throws SchedulingException {
		logger.trace("checkProgramIdScheduled(String programId : "+programId+")");

		if(programId==null ||programId.length()==0) {
			logger.warn("checkProgramIdScheduled(...), no programId specified");
			return false;
		}

		long time;
		if(clock==null) {
			logger.debug("No clock service registered, using system time");
			time = System.currentTimeMillis();
		} else {
			time = clock.getCurrentTimeInMillis();
		}

		Set <GoogleEvent> bigList = getEvents(time, -1);
		
		if(bigList == null) {
			logger.trace("checkProgramIdScheduled(...), no Events registered,");
			return false;
		}
		
		for(GoogleEvent event : bigList) {
			if (event.isSchedulingProgram(programId)) {
				logger.trace("checkProgramIdScheduled(...), first found on event : "+event.getName());
				return true;
			}
		}
		logger.trace("checkProgramIdScheduled(...), program id not found in events");
		return false;
	}

	@Override
	public JSONArray checkProgramsScheduled() throws SchedulingException {
		logger.trace("checkProgramsScheduled()");
		
		long time;
		if(clock==null) {
			logger.debug("No clock service registered, using system time");
			time = System.currentTimeMillis();
		} else {
			time = clock.getCurrentTimeInMillis();
		}

		Set <GoogleEvent> bigList = getEvents(time, -1);
		
		if(bigList == null) {
			logger.trace("checkProgramsScheduled(...), no Events registered,");
			return null;
		}
		JSONArray response = new JSONArray();
		
		for(GoogleEvent event : bigList) {
			
			for (ScheduledInstruction inst : event.getOnBeginInstructions() ) {
				logger.trace("checkProgramsScheduled(...), adding "+inst.getTarget());
				response.put(inst.getTarget());
			}
			for (ScheduledInstruction inst : event.getOnEndInstructions() ) {
				logger.trace("checkProgramsScheduled(...), adding "+inst.getTarget());
				response.put(inst.getTarget());
			}
		}
		logger.trace("checkProgramsScheduled(...), returning "+response.toString());
		return response;
	}	
}
