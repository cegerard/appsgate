/**
 * 
 */
package appsgate.lig.test.pax;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.Instance;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.google.scheduler.GoogleScheduler;
import appsgate.lig.google.services.GoogleAdapter;
import appsgate.lig.google.services.GoogleEvent;
import appsgate.lig.mail.Mail;
import appsgate.lig.mail.adapter.MailAdapter;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;
import appsgate.lig.weather.spec.WeatherAdapterSpec;
import appsgate.lig.yahoo.weather.YahooWeather;

/**
 * @author thibaud
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestWebServicesAppsgate extends PaxedDistribution {

	private static Logger logger = LoggerFactory
			.getLogger(TestWebServicesAppsgate.class);
	

/**
 * This test is not automated as it require valid informations (refresh token) according to a connected user
 * 
 */
	public void testGoogleAdapter() {
		String mailAccount= "smarthome.adele@gmail.com";
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test is for the Google Adapter");
		
		GoogleAdapter ga = (GoogleAdapter) initGoogleAdapter();

		Date currentDate = new Date();			
		Date startDate = new Date(currentDate.getTime() + 1800000);
		Date endDate = new Date(startDate.getTime() + 3600000);
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		Map<String, String> urlParameters=new HashMap<String, String>();

		urlParameters.put("timeMin", dateFormat.format(currentDate));
		logger.debug("*** FIRST get returns : "+ga.getEvents(mailAccount, urlParameters));
		
		String requestContent;
		JSONObject content = new JSONObject();
		content.put("start", new JSONObject().put("dateTime",  dateFormat.format(startDate)));
		content.put("end", new JSONObject().put("dateTime",  dateFormat.format(endDate)));
		content.put("summary", "testMAnualAdding");
		requestContent=content.toString();
		
		GoogleEvent res2=ga.addEvent(mailAccount, requestContent);
		logger.debug("*** add returns : "+res2.toString());
		String eventId=res2.getId();
				
		logger.debug("*** SECOND get returns : "+ga.getEvents(mailAccount, urlParameters));
		
		logger.debug("*** delete returns : "+ga.deleteEvent( mailAccount, eventId));
		logger.debug("*** THIRD get returns : "+ga.getEvents(mailAccount, urlParameters));		
		
		//Set<GoogleEvent> events= ga.getEvents("smarthome.adele@gmail.com", null);

		logger.debug("test finished");
	}
	
	public void testCleanGoogleAgenda() {
		String mailAccount= "smarthome.adele@gmail.com";
		
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("testCleanGoogleAgenda");
		
		GoogleAdapter ga = (GoogleAdapter) initGoogleAdapter();

		Date currentDate = new Date();			
		Date endDate = new Date(currentDate.getTime() + 24*3600000);
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T00:00:00.000'Z");
		
		Map<String, String> urlParameters=new HashMap<String, String>();
		urlParameters.put("timeMin", dateFormat.format(currentDate));
		urlParameters.put("timeMax", dateFormat.format(endDate));
		
		Set<GoogleEvent> events= ga.getEvents(mailAccount, urlParameters);
		for(GoogleEvent evt : events) {
			ga.deleteEvent(mailAccount, evt.getId());
		}

		logger.debug("test finished");		
		
	}
	
	
	public void testGoogleScheduler() {
		String mailAccount= "appsgate.minikitA@gmail.com";
		
		TestCoreAppsgate.testEmptyAppsgate();
		
		CoreClockSpec service = (CoreClockSpec) initTestClock();
		GoogleAdapter ga = (GoogleAdapter) initGoogleAdapter();
		logger.debug("init Google Adapter OK");
		
		GoogleScheduler toto = (GoogleScheduler) initGoogleScheduler();
		logger.debug("init Google Scheduler OK");

		try {
			toto.refreshScheduler();
			
			logger.debug("Scheduler refreshed");
			
			//toto.listEventsSchedulingProgramId("barnabee", "2014-10-02T14:17:25.423+02:00", "2014-10-03T14:17:25.423+02:00");
			//toto.listEventsSchedulingProgramId("barnabee", null, "2014-10-03T14:17:25.423+02:00");
			//toto.listEventsSchedulingProgramId("barnabee", "2014-10-02T14:17:25.423+02:00", null);
			//toto.listEventsSchedulingProgramId("barnabee", null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Scheduler reset OK");
	}

	@Test
	public void testAddSchedule() {
		String mailAccount= "smarthome.adele@gmail.com";
		
		TestCoreAppsgate.testEmptyAppsgate();
		
		CoreClockSpec service = (CoreClockSpec) initTestClock();
		GoogleAdapter ga = (GoogleAdapter) initGoogleAdapter();
		logger.debug("init Google Adapter OK");
		
		GoogleScheduler toto = (GoogleScheduler) initGoogleScheduler();
		logger.debug("init Google Scheduler OK");

		try {
			toto.createEvent("JplanifieMonTest", "monprogramme", true, true);
			logger.debug("Scheduler add event ok");

		} catch(Exception exc) {
			exc.printStackTrace();
		}
		//toto.resetScheduler();
	}	
	
	public static Object initGoogleScheduler() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,null,
				"GoogleScheduler", null);
	}		
	
	public static Object initGoogleAdapter() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,"GoogleAdapter",
				"AppsgateGoogleAdapterImpl", null);
	}	
	
	@Test
	public void testConfigurableClock() {
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test is for the Configurable Clock");
		CoreClockSpec service = (CoreClockSpec) initTestClock();
		
		logger.debug("Only testing basic system time (get/set/reset),"
				+ " refers to JunitTest of configurableClock for other functions");
		long systemTime = System.currentTimeMillis();
		long clockTime = service.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		logger.debug("Wait for 2 secs");
		try {
			Thread.sleep(2000);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		systemTime = System.currentTimeMillis();
		clockTime = service.getCurrentTimeInMillis();
		testTimeEqual(systemTime, clockTime);

		logger.debug("Set a particular Time");
		Calendar cal = Calendar.getInstance();
		cal.set(1977, 4, 1, 9, 54, 10);

		service.setCurrentDate(cal);
		testTimeEqual(cal.getTimeInMillis(), service.getCurrentTimeInMillis());

		logger.debug("Test the reset");
		service.resetSystemTime();
		systemTime = System.currentTimeMillis();
		testTimeEqual(systemTime, service.getCurrentTimeInMillis());

	}
	
	public static Object initTestClock() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,"CoreClockSpec",
				"ConfigurableClockImpl", null);
	}

	long errorTolerance = 10;

	void testTimeEqual(long systemTime, long clockTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(systemTime);
		logger.debug("Current System Time : " + cal.get(Calendar.SECOND) + "s "
				+ cal.get(Calendar.MILLISECOND) + "ms");

		cal.setTimeInMillis(clockTime);
		logger.debug("Current clock Time : " + cal.get(Calendar.SECOND) + "s "
				+ cal.get(Calendar.MILLISECOND) + "ms");

		if (clockTime - systemTime > errorTolerance
				|| clockTime - systemTime < -errorTolerance)
			Assert.fail("Latency between system time and clock time too high");
	}
	

	@Test
	public void testYahooWeather() {
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test creates and runs an instance of Yahoo Weather");
		
		YahooWeather service = (YahooWeather) initWeather();
		
		try {
			service.addLocation("Grenoble");
			Assert.assertTrue("Grenoble should be added",
					service.containLocation("Grenoble"));
			service.fetch();
			Assert.assertNotNull("Weather should have been retrieved",
					service.getCurrentWeather("Grenoble"));
		} catch (Exception exc) {
			Assert.fail(exc.getMessage());
		}
	}
	
	public static Object initWeather() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,"YahooWeatherSpec",
				"YahooWeatherImpl", null);
	}

	public void testGoogleCalendar() {
		TestCoreAppsgate.testEmptyAppsgate();

		logger.debug("This test is about Google calendar modules (adapter and instance)");
		// The calendar needs a clock
		initTestClock();

		initGoogleAdapter();
		initGoogleCalendar();
		// TODO : Test the calendar with a valid account (a simple set/get event)
		 
	}
	

	public static Object initGoogleCalendar() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,"CoreCalendarSpec",
				"GoogleCalendarImpl", null);
	}

	
	@Test
	public void testGoogleMail() {
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test is for the Configurable Clock");
		Mail service = (Mail) initGoogleMail();
		Assert.assertNotNull("Mail service should have been instanciated correctly ", service);
		//System.out.println("mails: "+service.getMails());
		
	}
	
	
	public static Object initGoogleMail() {
		
		MailAdapter adapter = (MailAdapter)PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,null,
				"MailAdapter", null);
		if (adapter==null) return null;
		adapter.loadAndCreateMail("conf/mail.cfg");
		Instance inst = ApAMHelper.waitForInstanceByImplemName(null,
				"MailService", 3000);
		if(inst!= null)
			return inst.getServiceObject();
		return null;
		
	}

	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		TestCoreAppsgate.fillCoreBundleList(testApps);
		fillKXMLBundleList(testApps);
		fillWebServicesBundleList(testApps);

		return super.configuration(testApps, null);
	}
	
	public static void fillKXMLBundleList(Map<String, String> testApps) {

		testApps.put("commons-logging", "commons-logging");
		testApps.put("commons-lang3", "org.apache.commons");
		
		testApps.put("commons-collections4", "org.apache.commons");
		testApps.put("org.apache.servicemix.bundles.xmlpull", "org.apache.servicemix.bundles");
		testApps.put("org.apache.servicemix.bundles.xpp3", "org.apache.servicemix.bundles");
		testApps.put("org.apache.servicemix.bundles.kxml2", "org.apache.servicemix.bundles");
	}
	

	public static void fillWebServicesBundleList(Map<String, String> testApps) {

		testApps.put("guava-osgi", "com.googlecode.guava-osgi");
	}

}
