package appsgate.lig.test.pax.helpers;

/**
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.Specification;

/**
 * @author thibaud
 * 
 */

public abstract class PaxedDistribution {

	Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	@Before
	public void setUp() {
		logger.debug("setUp");
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
	}

	@After
	public void tearDown() {
		logger.debug("tearDown");
	}

	public Option[] configuration(Map<String, String> testApps, List<File> externalBundles) {
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
				config.add(TestConfiguration.packAppForTestBundles(
						testApps.get(artifactID), artifactID));
			}
		}
		
		if (externalBundles != null && !externalBundles.isEmpty()) {
			for (File bundle : externalBundles) {
				config.add(TestConfiguration.packExternalBundle(bundle));
			}
		}
		
		return config.toArray(new Option[0]);
	}

	@Inject
	public BundleContext context;

	public static enum resolveFrom {
		SPEC, IMPLEM, INSTANCE;
	}

	public static Object testApAMComponent(boolean createInstance,
			resolveFrom from, String specName, String implemName,
			String instanceName) {

		Specification spec = null;
		Implementation impl = null;
		Instance inst = null;

		switch (from) {
		case SPEC:
			if (specName != null)
				spec = (Specification) ApAMHelper.waitForComponentByName(null,
						specName, ApAMHelper.RESOLVE_TIMEOUT);
			break;
		case IMPLEM:
			if (implemName != null)
				impl = (Implementation) ApAMHelper.waitForComponentByName(null,
						implemName, ApAMHelper.RESOLVE_TIMEOUT);
			break;
		case INSTANCE:
			if (instanceName != null)
				inst = (Instance) ApAMHelper.waitForComponentByName(null,
						instanceName, ApAMHelper.RESOLVE_TIMEOUT);
			break;
		}
		
		if(specName != null)
			Assert.assertNotNull("Specification "+specName+" should exists",
					CST.componentBroker.getSpec(specName));
		if(implemName != null)
			Assert.assertNotNull("Implementation "+implemName+" should exists",
					CST.componentBroker.getImpl(implemName));
		if(instanceName != null)
			Assert.assertNotNull("Instance "+instanceName+" should exists",
					CST.componentBroker.getInst(instanceName));
		
		
		// Please note that we only create instance upon implementation 
		Object obj = null;
		if(createInstance && impl != null) {
			inst = null;
			inst = impl.createInstance(null, null);
			Assert.assertNotNull("Instance "+instanceName+" should exists",
					inst);
			
			obj = inst.getServiceObject();
			Assert.assertNotNull("Service Object of "+inst.getName()+" should exists",
					obj);			
		}
			
		return obj;
	}

}
