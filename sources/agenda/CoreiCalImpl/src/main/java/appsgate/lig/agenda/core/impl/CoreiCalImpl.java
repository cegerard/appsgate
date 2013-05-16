package appsgate.lig.agenda.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
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
	private AgendaAdapter Adapter;

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
	private Date startDate = null;

	/**
	 * The end date to when get the remote calendar
	 */
	private Date endDate = null;

	/**
	 * The iCal local representation of the remote agenda
	 */
	private Calendar calendar;

	ArrayList<VEvent> startingEventsList;
	Timer nextStartEventTimer = new Timer();

	ArrayList<VEvent> endingEventsList;
	Timer nextEndEventTimer = new Timer();

	HashMap<VAlarm, VEvent> alarmsMap;
	Timer nextAlarmTimer = new Timer();

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
	private void subscribeNextEventNotifications() {

		java.util.Calendar startDate = java.util.Calendar.getInstance();
		startDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
		startDate.clear(java.util.Calendar.MINUTE);
		startDate.clear(java.util.Calendar.SECOND);
		logger.debug("START DATE: "+ String.format("Current Date/Time : %tc", startDate));

		// create a period starting the current day at midnight with a duration
		// of one month because
		// alarm can be triggered one month before an event.
		Period period = new Period(new DateTime(startDate.getTime()), new Dur(30, 0, 0, 0));
		Rule[] rules = new Rule[1];
		rules[0] = new PeriodRule(period);
		Filter filter = new Filter(rules, Filter.MATCH_ANY);

		java.util.Date today = java.util.Calendar.getInstance().getTime();
		Date referenceStartingEventDate = null;
		Date referenceEndingEventDate = null;
		Date referenceTriggeringAlarmDate = null;
		@SuppressWarnings("unchecked")
		Collection<VEvent> eventsToday = filter.filter(calendar.getComponents(Component.VEVENT));
		Iterator<VEvent> it = eventsToday.iterator();
		
		while (it.hasNext()) {
			VEvent event = it.next();
			logger.debug("EVENT: "+event.getSummary().getValue());
			// add next starting events and next ending events to the lists.
			Date eventStartDate = event.getStartDate().getDate();
			Date eventEndDate = event.getEndDate().getDate();

			if (eventStartDate.after(today)) {
				if (referenceStartingEventDate == null) {
					referenceStartingEventDate = eventStartDate;
					startingEventsList.add(event);
				} else if (eventStartDate.before(referenceStartingEventDate)) {
					referenceStartingEventDate = eventStartDate;
					startingEventsList.clear();
					startingEventsList.add(event);
				} else if (eventStartDate.equals(referenceStartingEventDate)) {
					startingEventsList.add(event);
				}
			}

			if (eventEndDate.after(today)) {
				if (referenceEndingEventDate == null) {
					referenceEndingEventDate = eventEndDate;
					endingEventsList.add(event);
				} else if (eventEndDate.before(referenceEndingEventDate)) {
					referenceEndingEventDate = eventEndDate;
					endingEventsList.clear();
					endingEventsList.add(event);
				} else if (eventEndDate.equals(referenceEndingEventDate)) {
					endingEventsList.add(event);
				}
			}

			// Get all alarms for all event in one month;
			ComponentList alarmsList = event.getAlarms();
			@SuppressWarnings("unchecked")
			Iterator<VAlarm> itAlarm = alarmsList.iterator();

			while (itAlarm.hasNext()) {
				VAlarm alarm = itAlarm.next();
				Date triggerAlarmDate = alarm.getTrigger().getDate();
				logger.debug("Reminders for "+event.getSummary().getValue()+", date: "+triggerAlarmDate.getTime());

				logger.debug("TODAY: "+ String.format("Current Date/Time : %tc", today));
				logger.debug("TRIGGER ALARM: "+ String.format("Current Date/Time : %tc", triggerAlarmDate));
				
				if ((triggerAlarmDate.after(today))) {
					if (referenceTriggeringAlarmDate == null) {
						referenceTriggeringAlarmDate = triggerAlarmDate;
						alarmsMap.put(alarm, event);
					} else if (eventStartDate.before(referenceTriggeringAlarmDate)) {
						referenceTriggeringAlarmDate = triggerAlarmDate;
						alarmsMap.clear();
						alarmsMap.put(alarm, event);
					} else if (eventStartDate.equals(referenceTriggeringAlarmDate)) {
						alarmsMap.put(alarm, event);
					}
				}
			}
		}

		// Update Events timers
//		if(!startingEventsList.isEmpty()) {
//			nextStartEventTimer.cancel();
//			nextStartEventTimer.purge();
//		}
//		
//		if(!endingEventsList.isEmpty()) {
//			nextEndEventTimer.cancel();
//			nextEndEventTimer.purge();
//		}
		
		if (referenceStartingEventDate != null) {
			nextStartEventTimer.schedule(notifyStartingEventTask, referenceStartingEventDate);
		}
		if (referenceEndingEventDate != null) {
			nextEndEventTimer.schedule(notifyEndingEventTask, referenceEndingEventDate);
		}

		// Update alarms events
//		if(!alarmsMap.isEmpty()) {
//			nextAlarmTimer.cancel();
//			nextAlarmTimer.purge();
//		}
		
		if (referenceTriggeringAlarmDate != null) {
			nextAlarmTimer.schedule(notifyAlarmRingTask, referenceTriggeringAlarmDate);
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
			return new StartingEventNotificationMsg(event.getSummary().getName());
		} else if (type == 1) {
			return new EndingEventNotificationMsg(event.getSummary().getName());
		} else {
			return new AlarmNotificationMsg(event.getSummary().getName(), alarm
					.getSummary().getName());
		}
	}

	/**
	 * Timer use to trigger notifications when events begin
	 */
	TimerTask notifyStartingEventTask = new TimerTask() {
		@Override
		public void run() {
			Iterator<VEvent> it = startingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(0, event, null);
				logger.debug("Send the starting event status notifcation for "
						+ event.getSummary().getName());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when events end
	 */
	TimerTask notifyEndingEventTask = new TimerTask() {
		@Override
		public void run() {
			Iterator<VEvent> it = endingEventsList.iterator();
			while (it.hasNext()) {
				VEvent event = it.next();
				notifyEventAlarm(1, event, null);
				logger.debug("Send the ending event status notifcation for "+ event.getSummary().getName());
			}
			subscribeNextEventNotifications();
		}
	};

	/**
	 * Timer use to trigger notifications when alarms rings
	 */
	TimerTask notifyAlarmRingTask = new TimerTask() {
		@Override
		public void run() {
			Iterator<Entry<VAlarm, VEvent>> it = alarmsMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<VAlarm, VEvent> entry = it.next();
				notifyEventAlarm(1, entry.getValue(), entry.getKey());
				logger.debug("Send the alarm notifcation "
						+ entry.getKey().getSummary().getName() + " for "
						+ entry.getValue().getSummary().getName());
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
			calendar = Adapter.getAgenda(agendaName, account, pswd, startDate, endDate);
			subscribeNextEventNotifications();
			logger.debug("Agenda \"" + calendar.getProperty("NAME").getValue() + "\" updated.");
			logger.debug("");
		}
	};

}
