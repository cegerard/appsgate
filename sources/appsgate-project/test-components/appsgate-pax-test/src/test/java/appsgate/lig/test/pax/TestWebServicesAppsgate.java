/**
 * 
 */
package appsgate.lig.test.pax;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import appsgate.lig.calendar.service.spec.CoreCalendarSpec;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.mail.Mail;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;

/**
 * @author thibaud
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestWebServicesAppsgate extends PaxedDistribution {

	private static Logger logger = LoggerFactory
			.getLogger(TestWebServicesAppsgate.class);
	
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
		service.resetClock();
		systemTime = System.currentTimeMillis();
		testTimeEqual(systemTime, service.getCurrentTimeInMillis());

	}
	
	public static Object initTestClock() {	
		
		Implementation clockService = (Implementation) ApAMHelper
				.waitForComponentByName(null, "ConfigurableClockImpl",
						ApAMHelper.RESOLVE_TIMEOUT);

		Assert.assertTrue("Specification should have been retrieved",
				CST.componentBroker.getSpec("CoreClockSpec") != null);
		Assert.assertTrue("Implementation should have been retrieved",
				CST.componentBroker.getImpl("ConfigurableClockImpl") != null);

		Instance inst = clockService.createInstance(null, null);
		Assert.assertTrue("An instance should have been created", inst != null);

		CoreClockSpec service = (CoreClockSpec) inst.getServiceObject();
		Assert.assertNotNull("Clock service should not be null", service != null);
		
		return service;
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
		
		CoreWeatherServiceSpec service = (CoreWeatherServiceSpec) initWeather();
		
		try {
			service.addLocation("Grenoble");
			Assert.assertTrue("Grenoble should be added",
					service.containLocation("Grenoble"));
			service.fetch();
			Assert.assertNotNull("Weather should have been retrieved",
					service.getCurrentWeather());
		} catch (Exception exc) {
			Assert.fail(exc.getMessage());
		}
	}
	
	public static Object initWeather() {

		logger.debug("This test creates and runs an instance of Yahoo Weather");
		Implementation weatherService = (Implementation) ApAMHelper
				.waitForComponentByName(null, "YahooWeatherImpl",
						ApAMHelper.RESOLVE_TIMEOUT);

		Assert.assertTrue("Specification should have been retrieved",
				CST.componentBroker.getSpec("CoreWeatherServiceSpec") != null);
		Assert.assertTrue("Implementation should have been retrieved",
				CST.componentBroker.getImpl("YahooWeatherImpl") != null);

		Instance inst = weatherService.createInstance(null, null);
		Assert.assertTrue("An instance should have been created", inst != null);

		CoreWeatherServiceSpec service = (CoreWeatherServiceSpec) inst
				.getServiceObject();
		Assert.assertNotNull("Weather should not be null", service);
		
		return service;	
	}

	@Test
	public void testGoogleCalendar() {
		TestCoreAppsgate.testEmptyAppsgate();

		logger.debug("This test is about Google calendar modules (adapter and instance)");
		Implementation googleAdapter = (Implementation) ApAMHelper
				.waitForComponentByName(null, "AppsgateGoogleAdapter",
						ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(
				"Implementation of google adapter should have been retrieved",
				CST.componentBroker.getImpl("AppsgateGoogleAdapter") != null);

		Implementation googleCalendar = (Implementation) ApAMHelper
				.waitForComponentByName(null, "GoogleCalendarImpl",
						ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(
				"Implementation of google calendar should have been retrieved",
				CST.componentBroker.getImpl("GoogleCalendarImpl") != null);

		Instance instAdapter = googleAdapter.createInstance(null, null);
		Assert.assertTrue("An instance of Adapter should have been created",
				instAdapter != null);
		
		// The calendar needs a clock
		initTestClock();

		Instance instCalendar = googleCalendar.createInstance(null, null);
		 Assert.assertTrue("An instance of Calendar should have been created",
		 instCalendar!= null);
		 
		 CoreCalendarSpec calendar = (CoreCalendarSpec) instCalendar.getServiceObject();
		 Assert.assertNotNull("Calendar service should not be null", calendar != null);
		 
		// TODO : Test the calendar with a valid account (a simple set/get event)
		 
	}

	
	@Test
	public void testGoogleMail() {
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test is for the Configurable Clock");
		Mail service = (Mail) initGoogleMail();
		
		// TODO : Test the mail with a valid account (a simple set/get a mail)
	}
	
	
	public static Object initGoogleMail() {
		Implementation mailService = (Implementation) ApAMHelper
				.waitForComponentByName(null, "GmailImpl",
						ApAMHelper.RESOLVE_TIMEOUT);

		Assert.assertTrue("Specification should have been retrieved",
				CST.componentBroker.getSpec("mail-service-specification") != null);
		Assert.assertTrue("Implementation should have been retrieved",
				CST.componentBroker.getImpl("GmailImpl") != null);

		Instance inst = mailService.createInstance(null, null);
		Assert.assertTrue("An instance should have been created", inst != null);

		Mail service = (Mail) inst.getServiceObject();
		Assert.assertNotNull("Clock service should not be null", service != null);	
		return service;
	}
	
	
	
	

	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		TestCoreAppsgate.fillCoreBundleList(testApps);
		fillWebServicesBundleList(testApps);

		return super.configuration(testApps);
	}

	public static void fillWebServicesBundleList(Map<String, String> testApps) {

		testApps.put("bcprov-jdk15on", "org.bouncycastle");
		testApps.put("com.google.gdata-calendar", "org.openengsb.wrapped");
		testApps.put("commons-logging", "commons-logging");
		testApps.put("commons-lang3", "org.apache.commons");
		testApps.put("org.apache.servicemix.bundles.xmlpull",
				"org.apache.servicemix.bundles");
		testApps.put("org.apache.servicemix.bundles.kxml2",
				"org.apache.servicemix.bundles");
		testApps.put("guava-osgi", "com.googlecode.guava-osgi");
		testApps.put("ical4j", "org.mnode.ical4j");
	}

}
