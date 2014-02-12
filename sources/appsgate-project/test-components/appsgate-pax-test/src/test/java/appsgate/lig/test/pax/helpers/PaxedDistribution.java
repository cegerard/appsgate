package appsgate.lig.test.pax.helpers;
/**
 * 
 */



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;


/**
 * @author thibaud
 *
 */


public abstract class PaxedDistribution {
	
	Logger logger = (Logger) LoggerFactory
	.getLogger(Logger.ROOT_LOGGER_NAME);
	
	

	@Before
	public void setUp() {
		logger.debug("setUp");
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
	}

	@After
	public void tearDown() {
		logger.debug("tearDown");
	}
	
	public Option[] configuration(Map<String, String> testApps) {
		logger.debug("configuration(Map<String, String> testApps), provisioning bundles");

		List<Option> config = new ArrayList<Option>();

		config.add(TestConfiguration.packInitialConfig());
		config.add(TestConfiguration.packOSGi());
		config.add(TestConfiguration.packPax());
		config.add(TestConfiguration.packLog());


		config.add(TestConfiguration.packWireAdmin());
		config.add(TestConfiguration.packApamCore());
		config.add(TestConfiguration.packApamShell());
		config.add(TestConfiguration.packApamObrMan());

		config.add(TestConfiguration.packDebugConfiguration());		
		
		if (testApps != null && !testApps.isEmpty()) {
			for (String artifactID : testApps.keySet()) {
				config.add(TestConfiguration.packAppForTestBundles(testApps.get(artifactID),
						artifactID));
			}
		}
		return config.toArray(new Option[0]);
	}	


	@Inject
	public BundleContext context;





}
