package appsgate.lig.calendar.core.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.calendar.service.messages.AlarmNotificationMsg;
import appsgate.lig.calendar.service.messages.EndingEventNotificationMsg;
import appsgate.lig.calendar.service.messages.StartingEventNotificationMsg;
import appsgate.lig.calendar.service.spec.CoreCalendarSpec;
import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.proxy.calendar.interfaces.GoogleCalendarAdapter;

/**
 * This class is used as a core calendar. An instance of this class will match a
 * remote calendar on the cloud
 * 
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 1.0.0
 * 
 */
public class GoogleCalendarImpl extends CoreObjectBehavior implements CoreCalendarSpec, CoreObjectSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(GoogleCalendarImpl.class);

	/**
	 * The adapter for Google account
	 */
	private GoogleCalendarAdapter serviceAdapter;
	
	/**
	 * The current system clock
	 */
	private CoreClockSpec systemClock;

	/**
	 * The name of the corresponding remote calendar
	 */
	private String calendarName;

	/**
	 * Remote account credentials
	 */
	private String account;
	private String pswd;

	/**
	 * The refresh rate of the local calendar
	 */
	private String rate;
	
	/**
	 * the refresh task id in the system clock
	 */
	private int refreshTaskId = -1;

	/**
	 * The start date from when to get remote calendar
	 */
	private java.util.Date startDate = null;

	/**
	 * The end date to when get the remote calendar
	 */
	private java.util.Date endDate = null;

	/**
	 * The iCal local representation of the remote calendar
	 */
	private Calendar calendar;
	
	/**
	 * The service identifier
	 */
	private String serviceId;

	/**
	 * The end user service type
	 */
	private String userType;

	/**
	 * The current service status
	 */
	private String status;

	private int beginEventId = -1;
	ArrayList<VEvent> startingEventsList;
	DateTime referenceStartingEventDate = new DateTime(0);
	
	private int endEventId = -1;
	ArrayList<VEvent> endingEventsList;
	DateTime referenceEndingEventDate = new DateTime(0);

	private int alarmEventId = -1;
	HashMap<VAlarm, VEvent> alarmsMap;
	DateTime referenceTriggeringAlarmDate = new DateTime(0);

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New core calendar instanciated, " + calendarName);
		String target = calendarName+account;
		serviceId = userType+target.hashCode();
		
		startingEventsList = new ArrayList<VEvent>();
		endingEventsList = new ArrayList<VEvent>();
		alarmsMap = new HashMap<VAlarm, VEvent>();

		Long refreshRate = Long.valueOf(rate);
		java.util.Calendar cal = systemClock.getCurrentDate();
		cal.setTimeInMillis(cal.getTimeInMillis()+5000+(long)(Math.random()*10000));
		refreshTaskId = systemClock.registerAlarm(cal, new RefreshTask());
	
		logger.debug("Refresh task initiated to " + refreshRate / 1000 / 60 + " minutes");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		if(refreshTaskId != -1) {
			systemClock.unregisterAlarm(refreshTaskId);
		}
		logger.info("A core calendar instance desapeared, " + calendarName);
	}
	
	@Override
	public JSONObject getCalendar( long from, long to) {
		Calendar subCal = serviceAdapter.getCalendar(calendarName, account, pswd, from, to);
		return calToJSONConvert(subCal);
	}

	@Override
	public Calendar getCalendar(java.util.Date from, java.util.Date to) {
		return serviceAdapter.getCalendar(calendarName, account, pswd, from.getTime(), to.getTime());
	}
	
	@Override
	public String getRate() {
		return rate;
	}

	@Override
	public void setRate(String rate) {
		this.rate = rate;
		reScheduleRefreshTask();
	}
	
	/**
	 * Reschedule the refresh task with the rate
	 * member value
	 */
	private void reScheduleRefreshTask() {
		if(refreshTaskId != -1) {
			systemClock.unregisterAlarm(refreshTaskId);
		}
		
		Long refreshRate = Long.valueOf(rate);
		java.util.Calendar cal = systemClock.getCurrentDate();
		cal.setTimeInMillis(cal.getTimeInMillis()+refreshRate);
		refreshTaskId = systemClock.registerAlarm(cal, new RefreshTask());
		
		logger.debug("Refresh task initiated to " + refreshRate / 1000 / 60 + " minutes");
	}

	/**
	 * This private method is use to prepare the instance to trigger ApAM
	 * notifications concerning the beginning and the end of the next event and
	 * corresponding alarms.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void subscribeNextEventNotifications() {
		java.util.Calendar startDate = systemClock.getCurrentDate();
		startDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
		startDate.clear(java.util.Calendar.MINUTE);
		startDate.clear(java.util.Calendar.SECOND);
		logger.debug("START DATE: "+ String.format("Current Date/Time : %tc", startDate));

		// create a period starting the current day at midnight with a duration
		// of one month because
		// alarm can be triggered one month before an event.
		Period period = new Period(new DateTime(startDate.getTimeInMillis()), new Dur(30, 0, 0, 0));
		Rule[] rules = new Rule[1];
		rules[0] = new PeriodRule(period);
		Filter filter = new Filter(rules, Filter.MATCH_ANY);

		DateTime today = new DateTime(systemClock.getCurrentTimeInMillis());
		DateTime newStartingEventDate = null;
		DateTime newEndingEventDate = null;
		DateTime newTriggeringAlarmDate = null;
		
		Collection<VEvent> eventsToday = filter.filter(calendar.getComponents(Component.VEVENT));
		Iterator<VEvent> it = eventsToday.iterator();
		
		startingEventsList.clear();
		endingEventsList.clear();
		alarmsMap.clear();
		
		while (it.hasNext()) {
			VEvent event = it.next();
			logger.debug("EVENT: "+event.getSummary().getValue());
			// add next starting events and next ending events to the lists.
			DateTime eventStartDate = new DateTime();
			DateTime eventEndDate = new DateTime();
			eventStartDate.setTime(event.getStartDate().getDate().getTime());
			eventEndDate.setTime(event.getEndDate().getDate().getTime());

			if (eventStartDate.after(today)) {
				logger.debug("EVENT START:"+event.getSummary().getValue());
				if (newStartingEventDate == null) {
					newStartingEventDate = eventStartDate;
					startingEventsList.add(event);
				} else if (eventStartDate.before(newStartingEventDate)) {
					newStartingEventDate = eventStartDate;
					startingEventsList.clear();
					startingEventsList.add(event);
				} else if (eventStartDate.equals(newStartingEventDate)) {
					startingEventsList.add(event);
				}
			}

			if (eventEndDate.after(today)) {
				logger.debug("EVENT END:"+event.getSummary().getValue());
				if (newEndingEventDate == null) {
					newEndingEventDate = eventEndDate;
					endingEventsList.add(event);
				} else if (eventEndDate.before(newEndingEventDate)) {
					newEndingEventDate = eventEndDate;
					endingEventsList.clear();
					endingEventsList.add(event);
				} else if (eventEndDate.equals(newEndingEventDate)) {
					endingEventsList.add(event);
				}
			}

			// Get all alarms for all event in one month;
			ComponentList alarmsList = event.getAlarms();
			Iterator<VAlarm> itAlarm = alarmsList.iterator();

			while (itAlarm.hasNext()) {
				VAlarm alarm = itAlarm.next();
				DateTime triggerAlarmDate = new DateTime();
				triggerAlarmDate.setTime(alarm.getTrigger().getDate().getTime());
				logger.debug("Reminders for "+event.getSummary().getValue()+String.format(" Current Date/Time : %tc", triggerAlarmDate));
				
				if ((triggerAlarmDate.after(today))) {
					if (newTriggeringAlarmDate == null) {
						newTriggeringAlarmDate = triggerAlarmDate;
						alarmsMap.put(alarm, event);
					} else if (eventStartDate.before(newTriggeringAlarmDate)) {
						newTriggeringAlarmDate = triggerAlarmDate;
						alarmsMap.clear();
						alarmsMap.put(alarm, event);
					} else if (eventStartDate.equals(newTriggeringAlarmDate)) {
						alarmsMap.put(alarm, event);
					}
				}
			}
		}

		// Update begin Events timers
		if(newStartingEventDate != null) {
			if (! newStartingEventDate.equals(referenceStartingEventDate)) {
				if(! referenceStartingEventDate.equals(new DateTime(0))) {
					systemClock.unregisterAlarm(beginEventId);
				}
				referenceStartingEventDate = newStartingEventDate;
				
				java.util.Calendar calbegin=java.util.Calendar.getInstance();
				calbegin.setTimeInMillis(referenceStartingEventDate.getTime());
				beginEventId = systemClock.registerAlarm(calbegin, new notifyStartingEventTask());
			}
		}
		
		// Update end Events timers
		if(newEndingEventDate != null ) {
			if( ! newEndingEventDate.equals(referenceEndingEventDate)) {
				if(! referenceEndingEventDate.equals(new DateTime(0))) {
					systemClock.unregisterAlarm(endEventId);
				}
				referenceEndingEventDate = newEndingEventDate;
				
				java.util.Calendar calend=java.util.Calendar.getInstance();
				calend.setTimeInMillis(referenceEndingEventDate.getTime());
				endEventId = systemClock.registerAlarm(calend, new notifyEndingEventTask());
			}
		}

		// Update alarms events
		if(newTriggeringAlarmDate != null ) {
			if(! newTriggeringAlarmDate.equals(referenceTriggeringAlarmDate)) {
				if(! referenceTriggeringAlarmDate.equals(new DateTime(0))) {
					systemClock.unregisterAlarm(alarmEventId);
				}
				referenceTriggeringAlarmDate = newTriggeringAlarmDate;
				
				java.util.Calendar calAlarm=java.util.Calendar.getInstance();
				calAlarm.setTimeInMillis(referenceTriggeringAlarmDate.getTime());
				alarmEventId = systemClock.registerAlarm(calAlarm, new notifyAlarmRingTask());
			}
		}
		
		logger.debug("Calendar subscription refresh: Beggin events =  "+startingEventsList.size()+", Endding events "+endingEventsList.size()+", Alarm = "+alarmsMap.size());
	}

	/**
	 * This method uses the ApAM message model. Each call produce a
	 * AlarmNotificationMsg or EventNotificationMsg object, that notify ApAM
	 * that a new message has been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyEventAlarm(int type, VEvent event, VAlarm alarm) {
		if (type == 0) {
			return new  StartingEventNotificationMsg(event.getSummary().getValue());
		} else if (type == 1) {
			return new EndingEventNotificationMsg(event.getSummary().getValue());
		} else {
			return new AlarmNotificationMsg(event.getSummary().getValue(), alarm.getTrigger().getValue());
		}
	}
	
	/**
	 * Timer use to trigger notifications when events begin
	 */
	private class notifyStartingEventTask implements AlarmEventObserver  {

		@Override
		public void alarmEventFired(int alarmEventId) {
			Iterator<VEvent> it = startingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(0, event, null);
				logger.info("Send the starting event status notifcation for "+ event.getSummary().getValue());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when events end
	 */
	private class notifyEndingEventTask implements AlarmEventObserver {

		@Override
		public void alarmEventFired(int alarmEventId) {
			Iterator<VEvent> it = endingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(1, event, null);
				logger.info("Send the ending event status notifcation for "+ event.getSummary().getValue());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when alarms rings
	 */
	private class notifyAlarmRingTask implements AlarmEventObserver {

		@Override
		public void alarmEventFired(int alarmEventId) {
			Iterator<Entry<VAlarm, VEvent>> it = alarmsMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<VAlarm, VEvent> entry = it.next();
				notifyEventAlarm(2, entry.getValue(), entry.getKey());
				logger.info("Send the alarm notifcation at "+ entry.getKey().getTrigger().getValue()+ " for "+ entry.getValue().getSummary().getValue());
			}
			subscribeNextEventNotifications();
		}
	};
	

	/**
	 * The task that is executed automatically to refresh the local calendar
	 */
	private class RefreshTask implements AlarmEventObserver {

		@Override
		public void alarmEventFired(int alarmEventId) {
			refreshTaskId = -1;
			calendar = serviceAdapter.getCalendar(calendarName, account, pswd, startDate, endDate);
			subscribeNextEventNotifications();
			
			//Save the iCal calendar representation in .ics file 
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(calendarName+".ics");
				CalendarOutputter outputter = new CalendarOutputter();
				outputter.output(calendar, fout);
				fout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				e.printStackTrace();
			}	
			
			logger.info("Calendar "+calendarName+" udpated.");
			
			reScheduleRefreshTask();
		}
		
	}

	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.valueOf(status);
	}

	@Override
	public String getPictureId() {
		return null;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		
		descr.put("id", serviceId);
		descr.put("type", userType); //101 for Google calendar
		descr.put("status", status);
		descr.put("calendarName", calendarName);
		descr.put("owner", account);
		descr.put("refreshRate", rate);
		
		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {}
	
	/**
	 * Convert a calendar in iCal format to JSONObject calendar
	 * @param calendar the calendar in iCal format
	 * @return a JSONBject that include the calendar
	 */
	private JSONObject calToJSONConvert(Calendar calendar) {
		JSONObject cal = new JSONObject();
		JSONArray compo = new JSONArray();
		
		try {
			cal.put("PRODID", calendar.getProductId());
			
			PropertyList pList = calendar.getProperties();
			for(Property p : pList) {
				cal.put(p.getName(), p.getValue());
			}
			
			ComponentList<CalendarComponent> cList = calendar.getComponents();
			for(CalendarComponent c : cList) {
				JSONObject event = new JSONObject();
				
				PropertyList cpList = c.getProperties();
				for(Property p : cpList) {
					event.put(p.getName(), p.getValue());
				}
			
				compo.put(event);
			}
			cal.put("components", compo);
			
			
		} catch (JSONException e) {
			logger.debug("Calendar convertion JSON exception.");
			e.printStackTrace();
		}
		
		return cal;
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

}
