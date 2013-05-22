package appsgate.lig.agenda.core.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

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
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.agenda.core.messages.AlarmNotificationMsg;
import appsgate.lig.agenda.core.messages.EndingEventNotificationMsg;
import appsgate.lig.agenda.core.messages.StartingEventNotificationMsg;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.proxy.agenda.interfaces.AgendaAdapter;

/**
 * This class is used as a core agenda. An instance of this class will match a
 * remote agenda on the cloud
 * 
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 0.0.1
 * 
 */
public class CoreiCalImpl {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(CoreiCalImpl.class);

	/**
	 * The adapter for Google account
	 */
	private AgendaAdapter serviceAdapter;

	/**
	 * The name of the corresponding remote agenda
	 */
	private String agendaName;

	/**
	 * Remote account credentials
	 */
	private String account;
	private String pswd;

	/**
	 * The refresh rate of the local agenda
	 */
	private String rate;

	/**
	 * The timer that trigger the refresh task each "rate" milliseconds
	 */
	Timer refreshTimer = new Timer();

	/**
	 * The start date from when to get remote calendar
	 */
	private java.util.Date startDate = null;

	/**
	 * The end date to when get the remote calendar
	 */
	private java.util.Date endDate = null;

	/**
	 * The iCal local representation of the remote agenda
	 */
	private Calendar calendar;

	ArrayList<VEvent> startingEventsList;
	Timer nextStartEventTimer = new Timer();
	DateTime referenceStartingEventDate = new DateTime(0);
	
	ArrayList<VEvent> endingEventsList;
	Timer nextEndEventTimer = new Timer();
	DateTime referenceEndingEventDate = new DateTime(0);

	HashMap<VAlarm, VEvent> alarmsMap;
	Timer nextAlarmTimer = new Timer();
	DateTime referenceTriggeringAlarmDate = new DateTime(0);

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New core agenda instanciated, " + agendaName);
		// calendar = Adapter.getAgenda(agendaName, account, pswd, startDate,
		// endDate);
		// logger.debug("URL for private access to data:"+calendar.getProperty("URL").getValue());
		// logger.debug("Name of remote agenda: "+calendar.getProperty("NAME").getValue());

		startingEventsList = new ArrayList<VEvent>();
		endingEventsList = new ArrayList<VEvent>();
		alarmsMap = new HashMap<VAlarm, VEvent>();

		Long refreshRate = Long.valueOf(rate);
		refreshTimer.scheduleAtFixedRate(refreshtask, 0, refreshRate);
		logger.debug("Refresh task initiated to " + refreshRate / 1000 / 60 + " minutes");
		
//		java.util.Calendar calbegin=java.util.Calendar.getInstance();
//		java.util.Calendar calend=java.util.Calendar.getInstance();
		
//		//add
//		calbegin.add(java.util.Calendar.DAY_OF_MONTH, 1);
//		calend.add(java.util.Calendar.DAY_OF_MONTH, 1);
//		calend.add(java.util.Calendar.HOUR, 2);		
//		DateTime inicio=new DateTime(calbegin.getTimeInMillis());
//		DateTime fim=new DateTime(calbegin.getTimeInMillis());
//		VEvent event=new VEvent(inicio,fim,"novo elemento");
//		serviceAdapter.addEvent("Agenda boulot","smarthome.inria@gmail.com","smarthome2012",event);
		
//		//del
//		calbegin.add(java.util.Calendar.DAY_OF_MONTH, 1);
//		calend.add(java.util.Calendar.DAY_OF_MONTH, 1);
//		calend.add(java.util.Calendar.HOUR, 2);		
//		DateTime inicio=new DateTime(calbegin.getTimeInMillis());
//		DateTime fim=new DateTime(calbegin.getTimeInMillis());
//		VEvent event=new VEvent(inicio,fim,"novo elemento");
//		serviceAdapter.delEvent("Agenda boulot","smarthome.inria@gmail.com","smarthome2012",event);
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		refreshTimer.cancel();
		refreshTimer.purge();
		logger.info("A core agenda instance desapeared, " + agendaName);
	}

	/**
	 * This private method is use to prepare the instance to trigger ApAM
	 * notifications concerning the beginning and the end of the next event and
	 * corresponding alarms.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void subscribeNextEventNotifications() {

		java.util.Calendar startDate = java.util.Calendar.getInstance();
		startDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
		startDate.clear(java.util.Calendar.MINUTE);
		startDate.clear(java.util.Calendar.SECOND);
		//logger.debug("START DATE: "+ String.format("Current Date/Time : %tc", startDate));

		// create a period starting the current day at midnight with a duration
		// of one month because
		// alarm can be triggered one month before an event.
		Period period = new Period(new DateTime(startDate.getTimeInMillis()), new Dur(30, 0, 0, 0));
		Rule[] rules = new Rule[1];
		rules[0] = new PeriodRule(period);
		Filter filter = new Filter(rules, Filter.MATCH_ANY);

		DateTime today = new DateTime(java.util.Calendar.getInstance().getTime().getTime());
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
			DateTime eventStartDate = (DateTime)event.getStartDate().getDate();
			DateTime eventEndDate   = (DateTime)event.getEndDate().getDate();

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
				DateTime triggerAlarmDate = (DateTime)alarm.getTrigger().getDate();
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

		// Update Events timers
		if(newStartingEventDate != null) {
			if (! newStartingEventDate.equals(referenceStartingEventDate)) {
				if(! referenceStartingEventDate.equals(new DateTime(0))) {
					nextStartEventTimer.cancel();
					nextStartEventTimer = new Timer();
				}
				referenceStartingEventDate = newStartingEventDate;
				nextStartEventTimer.schedule(new notifyStartingEventTask(), referenceStartingEventDate);
			}
		}
		
		if(newEndingEventDate != null ) {
			if( ! newEndingEventDate.equals(referenceEndingEventDate)) {
				if(! referenceEndingEventDate.equals(new DateTime(0))) {
					nextEndEventTimer.cancel();
					nextEndEventTimer = new Timer();
				}
				referenceEndingEventDate = newEndingEventDate;
				nextEndEventTimer.schedule(new notifyEndingEventTask(), referenceEndingEventDate);
			}
		}

		// Update alarms events
		if(newTriggeringAlarmDate != null ) {
			if(! newTriggeringAlarmDate.equals(referenceTriggeringAlarmDate)) {
				if(! referenceTriggeringAlarmDate.equals(new DateTime(0))) {
					nextAlarmTimer.cancel();
					nextAlarmTimer = new Timer();
				}
				referenceTriggeringAlarmDate = newTriggeringAlarmDate;
				nextAlarmTimer.schedule(new notifyAlarmRingTask(), referenceTriggeringAlarmDate);
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
	private class notifyStartingEventTask extends TimerTask {
		@Override
		public void run() {
			Iterator<VEvent> it = startingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(0, event, null);
				logger.debug("Send the starting event status notifcation for "+ event.getSummary().getValue());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when events end
	 */
	private class notifyEndingEventTask extends TimerTask {
		@Override
		public void run() {
			Iterator<VEvent> it = endingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(1, event, null);
				logger.debug("Send the ending event status notifcation for "+ event.getSummary().getValue());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when alarms rings
	 */
	private class notifyAlarmRingTask extends TimerTask {
		@Override
		public void run() {
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
	 * The task that is executed automatically to refresh the local agenda
	 */
	TimerTask refreshtask = new TimerTask() {
		@Override
		public void run() {
			calendar = serviceAdapter.getAgenda(agendaName, account, pswd, startDate, endDate);
			subscribeNextEventNotifications();
			
			//Save the iCal calendar representation in .ics file 
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(agendaName+".ics");
				CalendarOutputter outputter = new CalendarOutputter();
				outputter.output(calendar, fout);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				e.printStackTrace();
			}	
			logger.debug("");
		}
	};

}
