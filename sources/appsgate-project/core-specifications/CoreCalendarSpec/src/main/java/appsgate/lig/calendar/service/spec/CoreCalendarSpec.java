package appsgate.lig.calendar.service.spec;

import net.fortuna.ical4j.model.Calendar;

/**
 * This java interface is an ApAM specififcation shared by all ApAM
 * Appsgate application to handle events and alarms from calendar services.
 *  
 * @author Cédric Gérard
 * @version 1.0.0
 * @since August 5, 2013
 *
 */
public interface CoreCalendarSpec {
	
	/**
	 * Get the corresponding calendar events and alerts between a date interval.
	 * @param from the star date
	 * @param to the end date
	 * @return a sub calendar in iCal format.
	 */
	public Calendar getCalendar(java.util.Date from, java.util.Date to);

}
