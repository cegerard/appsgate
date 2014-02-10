/**
 * 
 */
package appsgate.lig.test.pax;


import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
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
import fr.imag.adele.apam.Specification;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.spec.CoreWeatherServiceSpec;


/**
 * @author thibaud
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestCoreAppsgate extends PaxedDistribution {
	
	private static Logger logger = LoggerFactory.getLogger(TestCoreAppsgate.class);

	
	@Test
	public void testEmptyAppsgate() {
		logger.debug("This test as a complete provisionning of Core Appsgate Bundles"
				+ " (without technology specific as ubikit, watteco, upnp)");
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
		ApAMHelper.waitForComponentByName(null,
				"OBRMAN-Instance", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getInst("OBRMAN-Instance")!= null);
		
	}	

	@Ignore
	@Test
	public void testAppsgateImpl() {
		testEmptyAppsgate();
		
		logger.debug("This test just load the core Appsgate Bundles (AppsgateImpl and dependencies)");
		ApAMHelper.waitForComponentByName(null,
				"AppsGateImpl", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getImpls("AppsGateImpl")!= null);

		
		//ApAMHelper.waitForIt(10000);
	}	
	
	@Test
	public void testYahooWeather() {
		testEmptyAppsgate();
		
		logger.debug("This test creates and runs an instance of Yahoo Weather");
		Implementation weatherService = (Implementation) ApAMHelper.waitForComponentByName(null,
				"YahooWeatherImpl", ApAMHelper.RESOLVE_TIMEOUT);

		Assert.assertTrue("Specification should have been retrieved", CST.componentBroker.getSpec("CoreWeatherServiceSpec")!= null);
		Assert.assertTrue("Implementation should have been retrieved", CST.componentBroker.getImpl("YahooWeatherImpl")!= null);
		
		Instance inst = weatherService.createInstance(null, null);
		Assert.assertTrue("An instance should have been created", inst!= null);
		
		CoreWeatherServiceSpec service= (CoreWeatherServiceSpec) inst.getServiceObject();
		try {
			service.addLocation("Grenoble");
			Assert.assertTrue("Grenoble should be added",service.containLocation("Grenoble"));
			service.fetch();
			Assert.assertNotNull("Weather should have been retrieved",service.getCurrentWeather());
		} catch(Exception exc) {
			Assert.fail(exc.getMessage());
		}
	}	
	
	
		
	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		fillBundleList(testApps);
		return super.configuration(testApps);
	}
	
	public void fillBundleList(Map<String, String> testApps) {
		testApps.put("org.apache.felix.configadmin", "org.apache.felix");
		testApps.put("org.apache.felix.fileinstall", "org.apache.felix");
		testApps.put("org.apache.felix.http.api", "org.apache.felix");
		testApps.put("org.apache.felix.http.jetty", "org.apache.felix");
		
		testApps.put("bcprov-jdk15on", "org.bouncycastle");
		testApps.put("com.google.gdata-calendar", "org.openengsb.wrapped");
		testApps.put("commons-logging", "commons-logging");
		testApps.put("commons-lang3", "org.apache.commons");
		testApps.put("org.apache.servicemix.bundles.kxml2", "org.apache.servicemix.bundles");
		testApps.put("guava-osgi", "com.googlecode.guava-osgi");
		testApps.put("ical4j", "org.mnode.ical4j");

		
		
		testApps.put("json", "org.json");
		testApps.put("mongo-java-driver", "org.mongodb");		
		testApps.put("JavamailAndroidAdapter", "appsgate.android");	
		testApps.put("JavaWebSocketAndroidAdapter", "appsgate.android");	
		
	}
	

}
