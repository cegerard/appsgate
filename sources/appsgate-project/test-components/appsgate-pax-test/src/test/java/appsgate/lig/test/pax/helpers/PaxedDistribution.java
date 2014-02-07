package appsgate.lig.test.pax.helpers;
/**
 * 
 */


import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.CoreOptions.when;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.Instance;


/**
 * @author thibaud
 *
 */


public abstract class  PaxedDistribution {
	
	Logger logger = (Logger) LoggerFactory
	.getLogger(Logger.ROOT_LOGGER_NAME);
	
	

	@Before
	public void setUp() {
		logger.debug("setUp");
		waitForApam(RESOLVE_TIMEOUT);
	}

	@After
	public void tearDown() {
		logger.debug("tearDown");
	}	
	
	protected CompositeOption packAppForTestBundles(String groupID,
			String artifactID) {

		CompositeOption testAppBundle = new DefaultCompositeOption(
				mavenBundle(groupID, artifactID).versionAsInProject());

		return testAppBundle;

	}		

//	@Configuration
	public Option[] configuration() {
		return configuration(null);
	}
	
	public Option[] configuration(Map<String, String> testApps) {
		System.err.println("configuration()");

		//TODO Bundles provisionning
		List<Option> config = new ArrayList<Option>();

		config.add(packInitialConfig());
		config.add(packOSGi());
		config.add(packPax());
		config.add(packLog());

		
		config.add(packApamCore());
		config.add(packApamShell());
		config.add(packApamObrMan());

		config.add(packDebugConfiguration());		
		
		if (testApps != null && !testApps.isEmpty()) {
			for (String artifactID : testApps.keySet()) {
				config.add(packAppForTestBundles(testApps.get(artifactID),
						artifactID));
			}
		}
		return config.toArray(new Option[0]);
	}		
	
	
    public static final int CONST_DEBUG_PORT = 5007;
	
	private boolean apamReady = false;
	public static long waitPeriod = 200;    
    public static long RESOLVE_TIMEOUT = 3000;
    
	public void waitForApam(long timeout) {
		long sleep = 0;
		while (!apamReady && sleep < timeout) {
			if (CST.componentBroker != null && CST.apamResolver != null
					&& CST.apam != null) {

				apamReady = true;
			} else {
				try {
					Thread.sleep(waitPeriod);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			sleep += waitPeriod;
		}
		boolean foundAPAM = false;
		while (sleep < timeout && !foundAPAM) {
			try {
				Thread.sleep(waitPeriod);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			sleep += waitPeriod;
			if (CST.apamResolver != null) {
				if (CST.apamResolver.findInstByName(null, "APAM-Instance") != null) {
					// && CST.apamResolver.findInstByName(null,
					// "OSGiMan-Instance") != null
					// && CST.apamResolver.findInstByName(null,
					// "ConflictManager-Instance") != null)
					foundAPAM = true;
				}
			}
		}
	}
	
	protected Component waitForComponentByName(Component client,
			String componentName, long timeout) {
		waitForApam(RESOLVE_TIMEOUT);

		Component comp = CST.apamResolver.findComponentByName(client,
				componentName);
		long sleep = 0;

		while (sleep < timeout && comp == null) {
			try {
				Thread.sleep(waitPeriod);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			sleep += waitPeriod;
			if (CST.apamResolver != null) {
				comp = CST.apamResolver.findComponentByName(client,
						componentName);
			}
		}

		return comp;
	}	

	protected void listInstances() {
		System.out.println(String.format(
				"------------ Instances (Total:%d) -------------", 
				CST.componentBroker.getInsts().size()));
		for (Instance i : CST.componentBroker.getInsts()) {

			System.out.println(String.format(" Instance name %s ( oid: %s ) ",
					i.getName(), i.getServiceObject()));

		}
		System.out.println(String.format(
				"------------ /Instances -------------"));
	}	

	@Inject
	public BundleContext context;

	private boolean isDebugModeOn() {
		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = RuntimemxBean.getInputArguments();

		boolean debugModeOn = false;

		for (String string : arguments) {
			debugModeOn = string.indexOf("jdwp") != -1;
			if (debugModeOn) {
				break;
			}
		}

		return debugModeOn;
	}
	
	protected CompositeOption packOSGi() {
		CompositeOption osgiConfig = new DefaultCompositeOption(mavenBundle()
				.groupId("org.apache.felix")
				.artifactId("org.apache.felix.ipojo").versionAsInProject(),
				mavenBundle().groupId("org.osgi")
						.artifactId("org.osgi.compendium").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.bundlerepository")
						.versionAsInProject(),
				frameworkProperty("ipojo.processing.synchronous").value("false"),
				frameworkProperty("ipojo.internal.dispatcher").value("true"),
				frameworkProperty("org.apache.felix.ipojo.extender.ThreadPoolSize").value("5"));

		return osgiConfig;

	}	

	protected CompositeOption packApamCore() {

		CompositeOption apamCoreConfig = new DefaultCompositeOption(
				mavenBundle().groupId("fr.imag.adele.apam")
						.artifactId("apam-bundle").versionAsInProject());

		return apamCoreConfig;
	}

	protected CompositeOption packApamObrMan() {
		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
				mavenBundle().groupId("fr.imag.adele.apam")
						.artifactId("obrman").versionAsInProject());

		return apamObrmanConfig;
	}

	protected CompositeOption packApamShell() {
		CompositeOption logConfig = new DefaultCompositeOption(
				mavenBundle("fr.imag.adele.apam", "apam-gogo-shell")
						.versionAsInProject(),
				mavenBundle("org.apache.felix", "org.apache.felix.gogo.command")
						.versionAsInProject(),
				mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime")
						.versionAsInProject(),
				mavenBundle("org.apache.felix",
						"org.apache.felix.gogo.shell").versionAsInProject(),
						mavenBundle("org.apache.felix",
								"org.apache.felix.shell").versionAsInProject(),						
				mavenBundle("org.apache.felix",
						"org.apache.felix.ipojo.arch.gogo").versionAsInProject());
		return logConfig;
	}

	protected CompositeOption packDebugConfiguration() {
		CompositeOption debugConfig = new DefaultCompositeOption(
				when(isDebugModeOn())
						.useOptions(
								vmOption(String
										.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%d",
												CONST_DEBUG_PORT)),
								systemTimeout(3600000)));

		return debugConfig;
	}

	protected CompositeOption packInitialConfig() {
		Logger root = (Logger) LoggerFactory
				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);

		String logpath = "file:" + PathUtils.getBaseDir() + "/log/logback.xml";
		File log = new File(logpath);

		boolean includeLog = log.exists() && log.isFile();

		CompositeOption initial = new DefaultCompositeOption(
				org.ops4j.pax.exam.CoreOptions.junitBundles(),
				frameworkProperty("org.osgi.service.http.port").value("8280"), cleanCaches(),
				systemProperty("logback.configurationFile").value(logpath),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("WARN"));

		return initial;
	}

	protected CompositeOption packLog() {
		CompositeOption logConfig = new DefaultCompositeOption(mavenBundle(
				"ch.qos.logback", "logback-core").versionAsInProject(),
				mavenBundle("ch.qos.logback", "logback-classic")
						.versionAsInProject(), mavenBundle("org.slf4j",
						"slf4j-api").versionAsInProject(), mavenBundle(
						"org.apache.felix", "org.apache.felix.log").versionAsInProject());

		return logConfig;
	}

	protected CompositeOption packPax() {
		CompositeOption paxConfig = new DefaultCompositeOption(mavenBundle()
				.groupId("org.ops4j.pax.url").artifactId("pax-url-mvn")
				.versionAsInProject());
		return paxConfig;
	}



}
