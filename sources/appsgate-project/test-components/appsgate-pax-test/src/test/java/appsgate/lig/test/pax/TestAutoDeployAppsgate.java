/**
 * 
 */
package appsgate.lig.test.pax;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;


/**
 * @author thibaud
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestAutoDeployAppsgate extends PaxedDistribution {
	
	private static Logger logger = LoggerFactory.getLogger(TestAutoDeployAppsgate.class);

	
	@Test
	public void testStartAppsgateCompositeDistribution() {
		TestCoreAppsgate.testEmptyAppsgate();
		
		PaxedDistribution.testApAMComponent(false, resolveFrom.INSTANCE, null, null,
				"appsgate-instance");
		testExistingStartedByAppsGateComposite();
		testExistingStartedByPersistencyComposite();
		testExistingStartedByGoogleComposite();		
		
		testExistingStartedByUbikitComposite();
		testExistingStartedByUPnPComposite();
		testExistingStartedByHUEComposite();
		
		
//		testExistingStartedByWattecoComposite();
//		testExistingStartedBySimulatedComposite();
	}
	
	private void testExistingStartedByAppsGateComposite() {
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "appsgate-instance", 5000));
		ApAMHelper.listInstances();
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "ConfigurableClockInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "WebSocketCommunicationManagerInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "EHMIProxyImplInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "CHMIProxyImplInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "EUDEInterpreterInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "AppsGatePersistencyInst",5000));		
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "AppsGateGoogleServicesInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "AppsGateHUEServicesInst",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "UPnPMediaAdapter",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "WeatherAdapter",5000));
	}
	
	private void testExistingStartedByUPnPComposite() {
		
		Assert.assertNotNull(CST.componentBroker.getImpl("AppsgateUPnPAdapter").getInst());
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-MediaPlayerFactory",5000));
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-MediaBrowserFactory",5000));
	}
	
	private void testExistingStartedByHUEComposite() {
//		Assert.assertNotNull(CST.componentBroker.getImpl("PhilipsHUEImplFactory").getInst());
//		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "AppsgatePhilipsHUEAdapter"));		
	}
	
	private void testExistingStartedByGoogleComposite() {
//		Assert.assertNotNull(CST.componentBroker.getImpl("AppsgateGoogleAdapter").getInst());		
	}

	private void testExistingStartedByPersistencyComposite() {
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-MongoDBConfigFactory",5000));				
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-PropertyHistoryManager",5000));		
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-ContextHistoryManager",5000));		
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-PlaceManager",5000));		
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-DevicePropertiesTableManager",5000));		
		Assert.assertNotNull(ApAMHelper.waitForComponentByName(null, "Global-UserBaseManager",5000));		
	}

	private void testExistingStartedByUbikitComposite() {
		Assert.assertNotNull(CST.componentBroker.getImpl("UbikitAdapterImpl").getInst());		
	}
		
	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		TestCoreAppsgate.fillCoreBundleList(testApps);
		TestCoreAppsgate.fillHttpBundleList(testApps);
		TestWebServicesAppsgate.fillKXMLBundleList(testApps);
		TestWebServicesAppsgate.fillWebServicesBundleList(testApps);
		TestUpnPAppsgate.fillUpnpBundleList(testApps);
		
		List<File> bundles = new ArrayList<File>();
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/enocean-driver-1.12.1.jar"));
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/rxtx4osgi-1.0.3.jar"));
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/ubikit-1.11.0.jar"));
		
		return super.configuration(testApps,bundles);
	}


}
