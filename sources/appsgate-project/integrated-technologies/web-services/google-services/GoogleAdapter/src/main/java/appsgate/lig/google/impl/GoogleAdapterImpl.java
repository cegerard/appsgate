package appsgate.lig.google.impl;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.Instance;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.google.helpers.GoogleOpenAuthent;
import appsgate.lig.google.helpers.GooglePropertiesHolder;
import appsgate.lig.google.services.GoogleAdapter;

/**
 */
public class GoogleAdapterImpl extends GooglePropertiesHolder implements GoogleAdapter{

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(GoogleAdapterImpl.class);

	BundleContext context;
	Instance mySelf;

	Object lock;

	private CoreClockSpec clock;	



	
	String currentAccessToken=null;



	public GoogleAdapterImpl(BundleContext context) {
		this.context=context;
		lock=new Object();
	}

	public void start(Instance myself) {
		logger.trace("start()");
		this.mySelf=myself;

		try {
			String configFile = context.getProperty(CONFIGURATION_FILE);
			logger.trace("Configuration file for GoogleAdapterImpl: "+configFile);
			loadFromFile(configFile);

		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}
		//        SynchroObserverTask nextRefresh = new SynchroObserverTask(this);
		//        timer = new Timer();
		// Next refresh will in 30secs (DB and web service should be available by then)
		//        timer.schedule(nextRefresh, 30 * 1000);
		//        logger.trace("started successfully, waiting for SynchroObserverTask to wake up");
	}

	@Override
	public void setRefreshToken(String refreshToken) {
		// TODO Auto-generated method stub

	}
}
