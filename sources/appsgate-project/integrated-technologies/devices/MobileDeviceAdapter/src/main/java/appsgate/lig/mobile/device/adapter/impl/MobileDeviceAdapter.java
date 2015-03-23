package appsgate.lig.mobile.device.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsgate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.mobile.device.adapter.spec.MobileDeviceAdapterServices;

public class MobileDeviceAdapter implements MobileDeviceAdapterServices {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static final Logger logger = LoggerFactory.getLogger(MobileDeviceAdapter.class);
	
	private ListenerService listenerService;
	private SendWebsocketsService sendToClientService;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New color mobile device adapter");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("A color light device adapter");
	}
}
