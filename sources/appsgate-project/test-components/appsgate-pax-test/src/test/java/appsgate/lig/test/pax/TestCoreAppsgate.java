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
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfigFactory;
import appsgate.lig.persistence.MongoDBConfiguration;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;



/**
 * @author thibaud
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestCoreAppsgate extends PaxedDistribution {
	
	private static Logger logger = LoggerFactory.getLogger(TestCoreAppsgate.class);

	
	public static void testEmptyAppsgate() {
		logger.debug("This test as a complete provisionning of Core Appsgate Bundles");
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
		ApAMHelper.waitForComponentByName(null,
				"OBRMAN-Instance", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getInst("OBRMAN-Instance")!= null);
	}	
	
	@Test
	public void testEmptyAppsgateDistribution() {
		testEmptyAppsgate();
	}		

	@Ignore
	@Test
	public void testAppsgateImpl() {
		testEmptyAppsgate();
		
		logger.debug("This test just load the core Appsgate Bundles (EHMIProxyImpl and dependencies)");
		ApAMHelper.waitForComponentByName(null,
				"AppsGate-Application", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getImpls("EHMIProxyImpl")!= null);

		
		//ApAMHelper.waitForIt(10000);
	}	
	
	@Ignore
	@Test // This one is not an automated test, but it allows testings of Mongo and helper classes 
	public void testMongo() {
		testEmptyAppsgate();
		
		logger.debug("This test just load the core Appsgate Bundles (EHMIProxyImpl and dependencies)");
		Implementation mongoFactoryImplem = (Implementation) ApAMHelper.waitForComponentByName(null,
				"MongoDBConfigFactory", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue("ApAM Implementation of MongoDBConfigFactory is null",mongoFactoryImplem!= null);
		
		Instance mongoFactoryInst = mongoFactoryImplem.createInstance(null, null);
		Assert.assertTrue("ApAM Instance of MongoDBConfigFactory is null", mongoFactoryInst!= null);
			
		Instance mongoConfInst = ApAMHelper.waitForInstanceByImplemName(null,
				"MongoDBConfiguration", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue("ApAM Implementation of MongoDBConfiguration is null",mongoConfInst!= null);
		
		
		MongoDBConfiguration mongoConfig = (MongoDBConfiguration) mongoConfInst.getServiceObject();
		Assert.assertTrue("Java MongoConfiguration is null", mongoConfig!= null);

		mongoConfig.dropDB("TestingDB");
		
		logger.trace("DataBases before : "+mongoConfig.getDatabases());
		mongoConfig.getDB("TestingDB");
		DBHelper dbHelp = mongoConfig.getDBHelper("TestingDB", "TestingCollection");
		dbHelp.insertSimpleObject("toto");
		dbHelp.insertSimpleObject("titi");
		dbHelp.insertSimpleObject("tutu");
		Assert.assertEquals("new DB colection should only contains the three entries added",3, dbHelp.getJSONEntries().size());
		logger.trace("Entries : "+dbHelp.getJSONEntries());
		
		dbHelp.remove("tutu");
		logger.trace("Entries (after removal): "+dbHelp.getJSONEntries());
		Assert.assertEquals("new DB collection should only contains the two entries (one removed)",2, dbHelp.getJSONEntries().size());
		
		logger.trace("DataBases after : "+mongoConfig.getDatabases());
	}	

	
		
	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		fillHttpBundleList(testApps);
		fillCoreBundleList(testApps);
		return super.configuration(testApps, null);
	}
	
	public static void fillHttpBundleList(Map<String, String> testApps) {
		testApps.put("org.apache.felix.http.api", "org.apache.felix");
		testApps.put("org.apache.felix.http.jetty", "org.apache.felix");
		
	}
	
	
	public static void fillCoreBundleList(Map<String, String> testApps) {
		testApps.put("org.apache.felix.configadmin", "org.apache.felix");
		testApps.put("org.apache.felix.fileinstall", "org.apache.felix");
		
		testApps.put("json", "org.json");
		testApps.put("mongo-java-driver", "org.mongodb");		
		testApps.put("JavamailAndroidAdapter", "appsgate.libs");	
		testApps.put("JavaWebSocketAdapter", "appsgate.libs");	
		
	}
	

}
