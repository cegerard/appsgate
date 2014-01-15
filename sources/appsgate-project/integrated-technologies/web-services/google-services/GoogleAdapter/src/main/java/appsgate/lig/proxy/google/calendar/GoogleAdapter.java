package appsgate.lig.proxy.google.calendar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.calendar.interfaces.GoogleCalendarAdapter;

import com.google.gdata.client.Query;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


/**
 * This class is used to communicate, through google calendar API, to Google
 * Calendar web service.
 * 
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 0.0.1
 * 
 * @see GoogleCalendarAdapter
 */
public class GoogleAdapter implements GoogleCalendarAdapter{
	
	private static String baseURL = "https://www.google.com/calendar/feeds/default/";

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(GoogleAdapter.class);
	
	/**
	 * google API object for manage google calendar service connection
	 */
	CalendarService currentGoogleCalendarConnection;
	

	/**
	 * default constructor for Google service adapter
	 */
	public GoogleAdapter() {
		currentGoogleCalendarConnection = new CalendarService("");
		logger.info("Google calendar Adapter instanciated");
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
	 * Get the remote google calendar corresponding to "calendar" with "account" and "password" google account connection
	 * 
	 * If start date is null the getCalendar method return the whole remote calendar, if not it will return the calendar from
	 * startDate to the last register event. It is possible to specify a end date to get the calendar between startDate and endDate.
	 * If startDate is null the endDate will not be consider.
	 * 
	 * @param calendar the remote calendar you want to get
	 * @param account the remote service account
	 * @param password the password corresponding to the account
	 * @param startDate the date from when you want to get events
	 * @param endDate the date to when you want to get events
	 * @return the google calendar convert to iCalendar standard format
	 */
	public synchronized Calendar getCalendar(String calendarName, String account, String password, java.util.Date startDate, java.util.Date endDate) { 
		Calendar calendar = newICal();
		
		try {
			currentGoogleCalendarConnection.setUserCredentials(account, password);
			URL feedUrl = new URL(baseURL+"owncalendars/full");
			CalendarFeed resultFeed = currentGoogleCalendarConnection.getFeed(feedUrl, CalendarFeed.class);
			CalendarEntry calendarEntry = null;
			int size = resultFeed.getEntries().size();
			int i = 0;
			boolean found = false;
			
			while (i < size && !found ) {
				calendarEntry = resultFeed.getEntries().get(i);
				if(calendarEntry.getTitle().getPlainText().contentEquals(calendarName)) {
					found = true;
				}
				i++;
			}
			
			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			
//			System.out.println("************ timezone to:"+calendarEntry.getTimeZone().getValue());
//			System.out.println("************ timezone and:"+registry.getTimeZone(calendarEntry.getTimeZone().getValue()).getVTimeZone());
			
			calendar.getComponents().add(registry.getTimeZone(calendarEntry.getTimeZone().getValue()).getVTimeZone());
			
			if(found) {
				URL eventURL = new URL(calendarEntry.getLink("alternate", null).getHref());
				CalendarEventFeed resulteventFeed;
				
				if(startDate == null) {
					resulteventFeed = currentGoogleCalendarConnection.getFeed(eventURL, CalendarEventFeed.class);
				} else {
					CalendarQuery myQuery = new CalendarQuery(feedUrl);
					myQuery.setMinimumStartTime(new DateTime(startDate.getTime()));
					if(endDate != null) {
						myQuery.setMaximumStartTime(new DateTime(endDate.getTime()));
					}	
					resulteventFeed = currentGoogleCalendarConnection.query(myQuery, CalendarEventFeed.class);
				}
				
				for (i = 0; i < resulteventFeed.getEntries().size(); i++) {
					CalendarEventEntry entry = resulteventFeed.getEntries().get(i);
					//Writing the event in the iCal calendar instant
					List<When> times = entry.getTimes();
					if(!times.isEmpty()) {
						When time = times.get(0);
						net.fortuna.ical4j.model.DateTime start = new net.fortuna.ical4j.model.DateTime(time.getStartTime().getValue());
						net.fortuna.ical4j.model.DateTime end =  new net.fortuna.ical4j.model.DateTime(time.getEndTime().getValue());
						VEvent icalEvent = new VEvent(start, end, entry.getTitle().getPlainText());
					
						//Get reminders from google entry
						ArrayList<VAlarm> icalAlarmList = new ArrayList<VAlarm>();
						List<Reminder> reminders = entry.getReminder();
						Iterator<Reminder> it = reminders.iterator();
						while(it.hasNext()) {
							Reminder reminder = it.next();

							//reminder.getMethod();
						
							long miliOfDay = 0;
							long miliOfHours = 0;
							long miliOfMinutes = 0;
						
							if(reminder.getDays() != null) {
								miliOfDay = (reminder.getDays()*24*3600*1000);
							}
						
							if(reminder.getHours() != null) {
								miliOfHours = (reminder.getHours()*3600*1000);
							}
						
							if(reminder.getMinutes() != null) {
								miliOfMinutes = reminder.getMinutes()*60*1000;
							}
						
							net.fortuna.ical4j.model.DateTime triggerDate = new net.fortuna.ical4j.model.DateTime(start.getTime()-miliOfDay-miliOfHours-miliOfMinutes);

							VAlarm alarm = new VAlarm(triggerDate);
						
							icalAlarmList.add(alarm);
						}
						icalEvent.getAlarms().addAll(icalAlarmList);
					
						// Add the icalendar UID for the event
						icalEvent.getProperties().add(new Uid(entry.getIcalUID()));
						calendar.getComponents().add(icalEvent);
					}
					
//					logger.debug("TIME google  : "+ time.getStartTime().getValue());
//					logger.debug("TIME google  : "+ String.format(time.getStartTime().toStringRfc822()));
//					logger.debug(" ical date   : "+ start.getTime());
//					logger.debug(" ical date   : "+ start.toGMTString());
//					logger.debug(" ical event  : "+ icalEvent.getStartDate().getDate().getTime());
//					logger.debug(" ical event  : "+ icalEvent.getStartDate().getParameter(DtStart.DTSTART));
				}
				
				//Url googleCalendarURL = new Url();
				//googleCalendarURL.setUri(eventURL.toURI());
				//calendar.getProperties().add(googleCalendarURL);
				//calendar.getProperties().add(new Name(calendar));
			}
			
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
		return calendar;
	}
	
	/**
	 * Get the remote google calendar corresponding to "calendar" with "account" and "password" google account connection
	 * 
	 * If start date is null the getCalendar method return the whole remote calendar, if not it will return the calendar from
	 * startDate to the last register event. It is possible to specify a end date to get the calendar between startDate and endDate.
	 * If startDate is null the endDate will not be consider.
	 * 
	 * @param calendar the remote calendar you want to get
	 * @param account the remote service account
	 * @param password the password corresponding to the account
	 * @param startDate the date from when you want to get events
	 * @param endDate the date to when you want to get events
	 * @return the google calendar convert to iCalendar standard format
	 */
	@Override
	public Calendar getCalendar(String calendarName, String account, String pswd, long from, long to) {
		java.util.Calendar startDate = java.util.Calendar.getInstance();
		java.util.Calendar endDate = java.util.Calendar.getInstance();
		
		startDate.setTimeInMillis(from);
		endDate.setTimeInMillis(to);
		
		return getCalendar(calendarName, account, pswd, startDate.getTime(), endDate.getTime());
	}
	
	/**
	 * Add a new event to the remote calendar and return an new VEvent if the event is correctly added
	 * to the remote service
	 * @param newEvent, the new iCalendar event to add
	 * @return the iCalendar event added
	 */
	@Override
	public synchronized VEvent addEvent(String calendar, String account, String password, VEvent newEvent) {
		
		try {
			
			currentGoogleCalendarConnection.setUserCredentials(account, password);
			
			URL postUrl = new URL(String.format("https://www.google.com/calendar/feeds/%s/private/full", account));
			
			CalendarEventEntry entry = new CalendarEventEntry();
			
			entry.setTitle(new PlainTextConstruct(newEvent.getSummary().getValue()));
			
			When eventTimes = new When();
			eventTimes.setStartTime(new DateTime(newEvent.getStartDate().getDate().getTime()));
			eventTimes.setEndTime(new DateTime(newEvent.getEndDate().getDate().getTime()));
			entry.addTime(eventTimes);
			
			@SuppressWarnings("unused")
			CalendarEventEntry insertedEntry = currentGoogleCalendarConnection.insert(postUrl, entry);
			
		} catch (IOException e) {
			logger.error("{}",e.getMessage());
		} catch (ServiceException e) {
			logger.error("{}",e.getMessage());
		}
		
		return newEvent;
	}
	
	/**
	 * Delete specific event to the remote calendar.
	 * @param oldEvent the event to be deleted
	 * @return true if the event is remotely deleted
	 */
	public synchronized boolean delEvent (String calendar, String account, String password, VEvent newEvent){
		
		CalendarEventFeed myResultsFeed;
		try {
		
			currentGoogleCalendarConnection.setUserCredentials(account, password);
			
			URL postUrl = new URL(String.format("https://www.google.com/calendar/feeds/%s/private/full", account));
			
			Query query = new Query(postUrl);
			
			query.setFullTextQuery(newEvent.getSummary().getValue());
			
			myResultsFeed = currentGoogleCalendarConnection.query(query,
				    CalendarEventFeed.class);
			
			if (myResultsFeed.getEntries().size() > 0) {
			  CalendarEventEntry firstMatchEntry = (CalendarEventEntry)
			      myResultsFeed.getEntries().get(0);
			  
			  String entryTitle = firstMatchEntry.getTitle().getPlainText();
			  
			  firstMatchEntry.delete();
			  
			  logger.info("'{}' deleted",entryTitle);
			  
			}
		
		} catch (IOException e) {
			logger.error("{}",e.getMessage());
		} catch (ServiceException e) {
			logger.error("{}",e.getMessage());
		}
		
		return true;
	}
	
	/**
	 * Add a new alarm to the remote google calendar
	 * @param newAlarm the new alarm to add
	 * @return true if the remote calendar has been updated
	 */
	public synchronized boolean addAlarm(VAlarm newAlarm) {
		return true;
	}
	
	/**
	 * Delete an alarm to the remote google calendar
	 * @param oldAlarm the alarm to be deleted
	 * @return true if the alarm has been deleted
	 */
	public synchronized boolean delAlarm(VAlarm oldAlarm) {
		return true;
	}

	/**
	 * Called by iPOJO when all dependencies are available
	 */
	public void validate() {
		logger.info("Google adapter started");
//		Calendar c = this.getCalendar("Calendar boulot", "smarthome.inria@gmail.com", "smarthome2012");
//		logger.debug("Direct access URL"+c.getProperty("URL").getValue());
//		logger.debug("Calendar name"+c.getProperty("NAME").getValue());
	}
	
	/**
	 * Called by iPOJO when the bundle is not available
	 */
	public void Invalidate() {
		logger.info("Google adapter stopped");
	}
}
