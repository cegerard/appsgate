package appsgate.lig.proxy.google.agenda;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.agenda.interfaces.AgendaAdapter;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;


/**
 * This class is used to communicate, through google calendar API, to Google
 * Agenda web service.
 * 
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 0.0.1
 */
@Component
@Instantiate
@Provides(specifications = {AgendaAdapter.class})
public class GoogleAdapter implements AgendaAdapter{
	
	private static String baseURL = "https://www.google.com/calendar/feeds/default/";

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(GoogleAdapter.class);
	
	/**
	 * google API object for manage google agenda service connection
	 */
	CalendarService currentGoogleAgendaConnection;
	

	/**
	 * default constructor for Google service adapter
	 */
	public GoogleAdapter() {
		currentGoogleAgendaConnection = new CalendarService("");
		logger.info("Google agenda Adapter instanciated");
	}
	
	/**
	 * Initialize a new instance of local ICalendar format
	 * @return an iCalendar java object
	 */
	private Calendar newICal() {
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//FR"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		return calendar;
	}

	/**
	 * Get the remote google agenda corresponding to "agenda" with "account" and "password" google account connection
	 * 
	 * If start date is null the getAgenda method return the whole remote agenda, if not it will return the agenda from
	 * startDate to the last register event. It is possible to specify a end date to get the agenda between startDate and endDate.
	 * If startDate is null the endDate will not be consider.
	 * 
	 * @param agenda the remote agenda you want to get
	 * @param account the remote service account
	 * @param password the password corresponding to the account
	 * @param startDate the date from when you want to get events
	 * @param endDate the date to when you want to get events
	 * @return the google agenda convert to iCalendar standard format
	 */
	public synchronized Calendar getAgenda(String agenda, String account, String password, java.util.Date startDate, java.util.Date endDate) { 
		Calendar calendar = newICal();
		try {
			currentGoogleAgendaConnection.setUserCredentials(account, password);
			URL feedUrl = new URL(baseURL+"owncalendars/full");
			CalendarFeed resultFeed = currentGoogleAgendaConnection.getFeed(feedUrl, CalendarFeed.class);
			CalendarEntry calendarEntry = null;
			int size = resultFeed.getEntries().size();
			int i = 0;
			boolean found = false;
			
			while (i < size && !found ) {
				calendarEntry = resultFeed.getEntries().get(i);
				if(calendarEntry.getTitle().getPlainText().contentEquals(agenda)) {
					found = true;
				}
				i++;
			}
			
			if(found) {
				URL eventURL = new URL(calendarEntry.getLink("alternate", null).getHref());
				CalendarEventFeed resulteventFeed;
				
				if(startDate == null) {
					resulteventFeed = currentGoogleAgendaConnection.getFeed(eventURL, CalendarEventFeed.class);
				} else {
					CalendarQuery myQuery = new CalendarQuery(feedUrl);
					myQuery.setMinimumStartTime(new DateTime(startDate));
					if(endDate != null) {
						myQuery.setMaximumStartTime(new DateTime(endDate));
					}	
					resulteventFeed = currentGoogleAgendaConnection.query(myQuery, CalendarEventFeed.class);
				}
				
				//UidGenerator ug = new UidGenerator("1");
				for (i = 0; i < resulteventFeed.getEntries().size(); i++) {
					CalendarEventEntry entry = resulteventFeed.getEntries().get(i);
					//Writing the event in the iCal calendar instant
					When time = entry.getTimes().get(0);
					Date start = new Date(time.getStartTime().getValue());
					Date end =  new Date(time.getEndTime().getValue());
					VEvent icalEvent = new VEvent(start, end, entry.getTitle().getPlainText());
					// Generate a UID for the event..
					
					icalEvent.getProperties().add(new Uid(entry.getIcalUID())/*ug.generateUid()*/);
					calendar.getComponents().add(icalEvent);
				}
				Url googleAgendaURL = new Url();
				googleAgendaURL.setUri(eventURL.toURI());
				calendar.getProperties().add(googleAgendaURL);
				calendar.getProperties().add(new Name(agenda));
			}
			
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return calendar;
	}
	
	/**
	 * Add a new event to the remote calendar and return an new VEvent if the event is correctly added
	 * to the remote service
	 * @param newEvent, the new iCalendar event to add
	 * @return the iCalendar event added
	 */
	public synchronized VEvent addEvent(VEvent newEvent) {
		return null;
	}
	
	/**
	 * Delete specific event to the remote calendar.
	 * @param oldEvent the event to be deleted
	 * @return true if the event is remotely deleted
	 */
	public synchronized boolean delEvent (VEvent oldEvent){
		return true;
	}
	
	/**
	 * Add a new alarm to the remote google agenda
	 * @param newAlarm the new alarm to add
	 * @return true if the remote calendar has been updated
	 */
	public synchronized boolean addAlarm(VAlarm newAlarm) {
		return true;
	}
	
	/**
	 * Delete an alarm to the remote google agenda
	 * @param oldAlarm the alarm to be deleted
	 * @return true if the alarm has been deleted
	 */
	public synchronized boolean delAlarm(VAlarm oldAlarm) {
		return true;
	}

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	@Validate
	public void validate() {
		logger.info("Google adapter started");
//		Calendar c = this.getAgenda("Agenda boulot", "smarthome.inria@gmail.com", "smarthome2012");
//		logger.debug("YEAH !!!!! "+c.getProperty("URL").getValue());
//		logger.debug("YEAH !!!!! "+c.getProperty("NAME").getValue());
	}
	
	/**
	 * Called by iPOJO when the bundle is not available
	 */
	@Invalidate
	public void Invalidate() {
		logger.info("Google adapter stopped");
	}
}
