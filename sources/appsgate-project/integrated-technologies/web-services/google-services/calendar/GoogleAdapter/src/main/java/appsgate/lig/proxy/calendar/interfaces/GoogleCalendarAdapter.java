package appsgate.lig.proxy.calendar.interfaces;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Generic interface for calendar service
 * @author Cédric Gérard
 * @since May 6, 2013
 */
public interface GoogleCalendarAdapter {
	
	/**
	 * Get a remote calendar
	 * @param calendarName name of the calendar to get
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param startDate date from when to get the calendar events
	 * @param endDate date to when to get calendar events
	 * @return iCalendar format of the remote calendar
	 */
	public Calendar getCalendar(String calendarName, String account, String password, java.util.Date startDate, java.util.Date endDate);
	
	/**
	 * Get the remote calendar events between a specify date interval
	 * @param calendarName the targeted calendar
	 * @param account the corresponding service account
	 * @param pswd the service password
	 * @param from the starting date
	 * @param to the ending date
	 * @return the calendar in iCal format
	 */
	public Calendar getCalendar(String calendarName, String account, String pswd, long from, long to);
	
	/**
	 * Add an event to the remote calendar
	 * @param calendar the targeted calendar
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param newEvent iCalendar format of the new event
	 * @return the added event
	 */
	public VEvent addEvent(String calendar, String account, String password, VEvent newEvent);
	
	/**
	 * Delete an event to the remote calendar
	 * @param calendar the targeted calendar
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param newEvent iCalendar format of the event to delete
	 * @return true if the event has been deleted false otherwise
	 */
	public boolean delEvent (String calendar, String account, String password, VEvent newEvent);
	
	/**
	 * Add an alarm to the remote calendar
	 * @param newAlarm the alarm to add
	 * @return true if the alarm has been added false otherwise
	 */
	public boolean addAlarm(VAlarm newAlarm);
	
	/**
	 * Delete an alarm to the remote calendar
	 * @param oldAlarm the alarm to delete
	 * @return true if the alarm has been deleted false otherwise
	 */
	public boolean delAlarm(VAlarm oldAlarm);

}
