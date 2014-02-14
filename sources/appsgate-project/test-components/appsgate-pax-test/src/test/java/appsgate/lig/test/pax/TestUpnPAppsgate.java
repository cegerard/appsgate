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
import appsgate.lig.mail.Mail;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;

/**
 * @author thibaud
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestUpnPAppsgate extends PaxedDistribution {

	private static Logger logger = LoggerFactory
			.getLogger(TestUpnPAppsgate.class);
	
	
	@Test
	public void testMediaServicesBundles() {
		TestCoreAppsgate.testEmptyAppsgate();
		logger.debug("This test checks that the Correct Components of UPnP Media Services are available"
				+"\n !!!! It DOES NOT test if the UPnP protocol (discovery/messages) works as expected  !!!");
		initUPnPAdapter();
		initMediaPlayerFactory();
		initMediaBrowserFactory();
		//TODO

	}
	
	
	public static Object initUPnPAdapter() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,null,
				"AppsgateUPnPAdapter", null);

	}
	
	public static Object initMediaPlayerFactory() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,null,
				"MediaPlayerFactory", null);
	}
	public static Object initMediaBrowserFactory() {
		return PaxedDistribution.testApAMComponent(true, resolveFrom.IMPLEM,null,
				"MediaBrowserFactory", null);
	}


	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		TestCoreAppsgate.fillCoreBundleList(testApps);
		fillUpnpBundleList(testApps);

		return super.configuration(testApps);
	}

	public static void fillUpnpBundleList(Map<String, String> testApps) {
		testApps.put("CyberGarageAdapter", "appsgate.android");
		testApps.put("org.apache.felix.upnp.basedriver", "org.apache.felix");
		testApps.put("org.apache.felix.upnp.extra", "org.apache.felix");
		testApps.put("org.apache.felix.upnp.devicegen.util", "org.apache.felix.sandbox");		
	}

}
