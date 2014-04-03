/**
 * 
 */
package appsgate.lig.test.pax;


import static org.ops4j.pax.exam.CoreOptions.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
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
		Assert.assertNotNull(CST.componentBroker.getInst("appsgate-instance"));
		Assert.assertNotNull(CST.componentBroker.getInst("AppsGatePersistencyInst"));
		Assert.assertNotNull(CST.componentBroker.getImpl("EHMIImpl").getInst());
		Assert.assertNotNull(CST.componentBroker.getInst("ConfigurableClockInst"));
		Assert.assertNotNull(CST.componentBroker.getInst("AppsGateContextProxyInst"));
		Assert.assertNotNull(CST.componentBroker.getInst("AppsGateGoogleServicesInst"));
		Assert.assertNotNull(CST.componentBroker.getInst("AppsGateHUEServicesInst"));
		Assert.assertNotNull(CST.componentBroker.getInst("UPnPMediaAdapter"));
		Assert.assertNotNull(CST.componentBroker.getInst("UbikitAdapter"));
		Assert.assertNotNull(CST.componentBroker.getInst("YahooWeather"));
	}
	
	private void testExistingStartedByUPnPComposite() {
		
		Assert.assertNotNull(CST.componentBroker.getImpl("AppsgateUPnPAdapter").getInst());
		Assert.assertNotNull(CST.componentBroker.getInst("Global-MediaPlayerFactory"));
		Assert.assertNotNull(CST.componentBroker.getInst("Global-MediaBrowserFactory"));
	}
	
	private void testExistingStartedByHUEComposite() {
		Assert.assertNotNull(CST.componentBroker.getImpl("PhilipsHUEImplFactory").getInst());
		Assert.assertNotNull(CST.componentBroker.getInst("AppsgatePhilipsHUEAdapter"));		
	}
	
	private void testExistingStartedByGoogleComposite() {
		Assert.assertNotNull(CST.componentBroker.getImpl("AppsgateGoogleAdapter").getInst());		
	}

	private void testExistingStartedByPersistencyComposite() {
		Assert.assertNotNull(CST.componentBroker.getInst("Global-PropertyHistoryManager"));		
		Assert.assertNotNull(CST.componentBroker.getInst("Global-ContextHistoryManager"));		
//		Assert.assertNotNull(CST.componentBroker.getInst("Global-ContextManager"));
		Assert.assertNotNull(CST.componentBroker.getInst("Global-PlaceManager"));		
		Assert.assertNotNull(CST.componentBroker.getInst("Global-DevicePropertiesTableManager"));		
		Assert.assertNotNull(CST.componentBroker.getInst("Global-UserBaseManager"));		
	}

	private void testExistingStartedByUbikitComposite() {
		Assert.assertNotNull(CST.componentBroker.getImpl("UbikitAdapterImpl").getInst());		
	}

	private void testExistingStartedByWattecoComposite() {
		Assert.assertNotNull(CST.componentBroker.getInst("WattecoAdapterInst"));		
	}
	
	private void testExistingStartedBySimulatedComposite() {
		Assert.assertNotNull(CST.componentBroker.getInst("SimulatedAdapterInst"));		
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
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/pem-enocean-1.12.0.jar"));
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/rxtx4osgi-1.0.3.jar"));
		bundles.add(new File(PathUtils.getBaseDir(), "bundle/ubikit-1.11.0.jar"));
		
		return super.configuration(testApps,bundles);
	}


}
