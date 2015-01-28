package appsgate.lig.tts.yakitome.impl;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import appsgate.lig.tts.yakitome.AdapterListener;
import appsgate.lig.tts.yakitome.YakitomeAPI;

public class YakitomeAdapter extends Thread implements AdapterListener{
	
	
	public YakitomeAdapter() {
		apiClient = new YakitomeAPIClient();
		// api key registered for smarthome.inria at gmail.com
		apiClient.configure("5otuvhvboadAgcLPwy69P", "Juliette", -1);
	}
	
	/**
	 * At each polling period, check if the Yakitome service is available
	 * and create/destroy the apam instance accordingly
	 */
	public static int POLLING_PERIOD = 5*60*1000;
	Object lock = new Object();
	
	boolean running = false;
	
	YakitomeAPI apiClient;
	
	Instance ttsInstance;
	
	@Override
	public void run() {
		logger.trace("run()");
		running = true;
		while(running) {
			checkAndUpdateTTSInstance();	
			try {
				Thread.sleep(POLLING_PERIOD);
			} catch (InterruptedException e) {
				logger.trace("run(), sleep interrupted");	
			}			
		}
	}
	
	public void start() {
		super.start();
	}	
	
	public void stopIt() {
		logger.trace("stopIt()");
		running = false;
		this.interrupt();
	}
	
	
	
	/**
	 * This one check that Yakitome if available:
	 * if not, it destroy previously created apam instance
	 * if yes, check that if exiting a running instance that holds the same configuration
	 */
	public void checkAndUpdateTTSInstance() {
		logger.trace("checkAndUpdateTTSInstance()");
		if(apiClient!= null
				&&apiClient.getConfigurationHashkey() != null
				&&apiClient.testService()) {
			logger.trace("checkAndUpdateTTSInstance(), Yakitome is (still ?) available");
			if(ttsInstance != null
				&& ttsInstance.getName() != null
				&& ttsInstance.getName().equals(TTSServiceImpl.TTS_IMPLEM_NAME+"-"+apiClient.getConfigurationHashkey())) {
				logger.trace("checkAndUpdateTTSInstance(),"
						+ "ApAM instance already existing with the same configuration : does nothing");
			} else {
				logger.trace("checkAndUpdateTTSInstance(),"
						+ "no ApAM instance already existing with the same configuration");
				createTTSInstance();

				
			}			
		} else {
			logger.trace("checkAndUpdateTTSInstance(), Yakitome is not (no more ?) available");
			destroyTTSInstance();

		}
	}
	
	private void createTTSInstance() {
		logger.trace("createTTSInstance()");
		//Just in case, try to erase the previous existing instance
		// (only one instance at the time)
		destroyTTSInstance();
		Implementation implem = CST.apamResolver.findImplByName(null,TTSServiceImpl.TTS_IMPLEM_NAME);
		if(implem == null) {
			return;
		}
		logger.trace("createTTSInstance(), implem found");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("instance.name", TTSServiceImpl.TTS_IMPLEM_NAME+"-"+apiClient.getConfigurationHashkey());
		ttsInstance = implem.createInstance(null, properties);
		if(ttsInstance == null) {
			return;
		}
		
		logger.trace("createTTSInstance(), instance created");
		((TTSServiceImpl)ttsInstance.getServiceObject()).configure(apiClient, this);
		logger.trace("createTTSInstance(), bound to api and adapter");
	}
	
	private void destroyTTSInstance() {
		logger.trace("destroyTTSInstance()");
		if(ttsInstance != null && ttsInstance.getName() != null) {
			logger.trace("destroyTTSInstance(), a tts instance found, removing it");
			((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(ttsInstance.getName());
			logger.trace("destroyTTSInstance(), apam TTS instance removed");
		}
		ttsInstance = null;
		
	}

	private static Logger logger = LoggerFactory
			.getLogger(YakitomeAdapter.class);

	@Override
	public void serviceUnavailable() {
		logger.trace("serviceUnavailable()");
		checkAndUpdateTTSInstance();		
	}	

	
}
