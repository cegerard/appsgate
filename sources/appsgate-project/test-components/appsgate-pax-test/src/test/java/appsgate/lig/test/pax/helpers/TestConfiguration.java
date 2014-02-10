package appsgate.lig.test.pax.helpers;
/**
 * 
 */


import static org.ops4j.pax.exam.CoreOptions.bundle;
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
import java.util.List;

import org.junit.Assert;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.util.PathUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 * @author thibaud
 *
 */


public class  TestConfiguration {
	
    public static final int CONST_DEBUG_PORT = 5007;
    
	static public  CompositeOption packAppForTestBundles(String groupID,
			String artifactID) {

		System.err.println("Provisionning Bundle, groupID : "+groupID
				+", artifactID : "+artifactID
				+", version : "+mavenBundle(groupID, artifactID).versionAsInProject());
		CompositeOption testAppBundle = new DefaultCompositeOption(
				mavenBundle(groupID, artifactID).versionAsInProject());

		return testAppBundle;

	}    
    
	

    static public boolean isDebugModeOn() {
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
	
	static public CompositeOption packOSGi() {
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

	static public CompositeOption packApamCore() {

		CompositeOption apamCoreConfig = new DefaultCompositeOption(
				mavenBundle().groupId("fr.imag.adele.apam")
						.artifactId("apam-bundle").versionAsInProject());

		return apamCoreConfig;
	}
	
	static public CompositeOption packWireAdmin() {

		try {
			CompositeOption wireConfig = new DefaultCompositeOption(bundle((new File(PathUtils.getBaseDir(),
			    "bundle/wireadmin.jar")).toURI().toURL().toExternalForm()));
			return wireConfig;

		} catch (Exception error) {
		    Assert.assertTrue("Error deploying WireAdmin", false);
		    return null;
		}
	}
	
	static public CompositeOption packApamObrMan() {
		CompositeOption apamObrmanConfig = new DefaultCompositeOption(
				mavenBundle().groupId("fr.imag.adele.apam")
						.artifactId("obrman").versionAsInProject());

		return apamObrmanConfig;
	}

	static public CompositeOption packApamShell() {
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

	static public CompositeOption packDebugConfiguration() {
		CompositeOption debugConfig = new DefaultCompositeOption(
				when(isDebugModeOn())
						.useOptions(
								vmOption(String
										.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%d",
												CONST_DEBUG_PORT)),
								systemTimeout(3600000)));

		return debugConfig;
	}

	static public CompositeOption packInitialConfig() {
		Logger root = (Logger) LoggerFactory
				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);

		String logpath = "file:" + PathUtils.getBaseDir() + "/log/logback.xml";
//		File log = new File(logpath);
		
		CompositeOption initial = new DefaultCompositeOption(
				org.ops4j.pax.exam.CoreOptions.junitBundles(),
				frameworkProperty("org.osgi.service.http.port").value("8280"), cleanCaches(),
				systemProperty("logback.configurationFile").value(logpath),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("WARN"));

		return initial;
	}

	static public CompositeOption packLog() {
		CompositeOption logConfig = new DefaultCompositeOption(mavenBundle(
				"ch.qos.logback", "logback-core").versionAsInProject(),
				mavenBundle("ch.qos.logback", "logback-classic")
						.versionAsInProject(), mavenBundle("org.slf4j",
						"slf4j-api").versionAsInProject(), mavenBundle(
						"org.apache.felix", "org.apache.felix.log").versionAsInProject());

		return logConfig;
	}

	static public CompositeOption packPax() {
		CompositeOption paxConfig = new DefaultCompositeOption(mavenBundle()
				.groupId("org.ops4j.pax.url").artifactId("pax-url-mvn")
				.versionAsInProject());
		return paxConfig;
	}



}
