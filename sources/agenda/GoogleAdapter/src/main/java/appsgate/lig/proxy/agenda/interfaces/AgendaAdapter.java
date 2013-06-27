package appsgate.lig.proxy.agenda.interfaces;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Generic interface for agenda service
 * @author Cédric Gérard
 * @since May 6, 2013
 */
public interface AgendaAdapter {
	
	/**
	 * Get a remote agenda
	 * @param agenda name of the agenda to get
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param startDate date from when to get the agenda events
	 * @param endDate date to when to get agenda events
	 * @return iCalendar format of the remote agenda
	 */
	public Calendar getAgenda(String agenda, String account, String password, java.util.Date startDate, java.util.Date endDate);
	
	/**
	 * Add an event to the remote agenda
	 * @param agenda the targeted agenda
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param newEvent iCalendar format of the new event
	 * @return the added event
	 */
	public VEvent addEvent(String agenda, String account, String password, VEvent newEvent);
	
	/**
	 * Delete an event to the remote agenda
	 * @param agenda the targeted agenda
	 * @param account the remote service account
	 * @param password the remote service password
	 * @param newEvent iCalendar format of the event to delete
	 * @return true if the event has been deleted false otherwise
	 */
	public boolean delEvent (String agenda, String account, String password, VEvent newEvent);
	
	/**
	 * Add an alarm to the remote agenda
	 * @param newAlarm the alarm to add
	 * @return true if the alarm has been added false otherwise
	 */
	public boolean addAlarm(VAlarm newAlarm);
	
	/**
	 * Delete an alarm to the remote agenda
	 * @param oldAlarm the alarm to delete
	 * @return true if the alarm has been deleted false otherwise
	 */
	public boolean delAlarm(VAlarm oldAlarm);

}
