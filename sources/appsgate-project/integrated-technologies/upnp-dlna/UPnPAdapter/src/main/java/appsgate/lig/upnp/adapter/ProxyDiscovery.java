package appsgate.lig.upnp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleContext;
import org.osgi.service.upnp.UPnPDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
	
import fr.imag.adele.apam.ApamResolver;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.apform.ApformInstance;

/**
 * This class listen for UPnPDevice service discovery events and creates the
 * APAM proxy for all hosted services in the device.
 * 
 * Proxies are looked up using Apam resolver capabilities, so they may be
 * deployed as a side effect of a discovery.
 * 
 * @author vega
 * 
 */
public class ProxyDiscovery {
	
	private static Logger logger = LoggerFactory.getLogger(ProxyDiscovery.class);

	/**
	 * The bundle context of the discovery
	 */
	private final BundleContext context;
	
	private ListenerService listenerService;
	private SendWebsocketsService sendToClientService;

	public ProxyDiscovery(BundleContext context) {
		this.context = context;
	}

	/**
	 * The event executor. We use a pool of a threads to notify APAM of
	 * underlying platform events, without blocking the platform thread.
	 */
	static private final Executor executor = Executors.newCachedThreadPool();

	private ApamResolver resolver;

	/**
	 * The list of events registered before binding Apam
	 */
	private List<UPnPDevice> pending = new ArrayList<UPnPDevice>();

	@SuppressWarnings("unused")
	private synchronized void apamBound() {
		resolver = CST.apamResolver;
		logger.debug("[UPnP Apam Discovery] Bound to APAM resolver " + resolver);

		/*
		 * Schedule pending device discovery requests
		 */
		for (UPnPDevice device : pending) {
			logger.debug("[UPnP Apam Discovery] ... scheduling pending request for "+ device.getDescriptions(null).get(UPnPDevice.ID));
			executor.execute(new DeviceDiscoveryRequest(device,
					discoveredDeviceProxies, resolver, context));
		}

		pending.clear();	
		if(listenerService != null && sendToClientService != null) {
			UPnPConfigListener listener = new UPnPConfigListener(sendToClientService);
			listenerService.addConfigListener(UPnPConfigListener.CONFIG_TARGET, listener);
		}
			

	}

	@SuppressWarnings("unused")
	private synchronized void apamUnbound() {
		resolver = null;
		logger.debug("[UPnP Apam Discovery] Unbound to APAM resolver "+ resolver);
		if(listenerService != null && sendToClientService != null) {
			listenerService.removeConfigListener(UPnPConfigListener.CONFIG_TARGET);
		}		
	}

	/**
	 * The list of created proxies for each discovered device
	 * 
	 */
	private Map<UPnPDevice, List<ApformInstance>> discoveredDeviceProxies = new HashMap<UPnPDevice, List<ApformInstance>>();

	/**
	 * Method invoked with a new device hosting the UPnP services is discovered.
	 * 
	 * WARNING IMPORTANT Notice that this is an iPojo callback, and we should
	 * not block inside, so we process the request asynchronously.
	 */
	@SuppressWarnings("unused")
	private void boundDevice(UPnPDevice device) {

		/*
		 * first we update synchronously the device table, so we can respect the
		 * order of events (bound/unbound) for each device
		 */
		synchronized (discoveredDeviceProxies) {
			discoveredDeviceProxies
					.put(device, new ArrayList<ApformInstance>());
		}

		/*
		 * If APAM is not available register the pending request
		 */
		synchronized (this) {
			if (resolver == null) {
				pending.add(device);
				return;
			}
		}

		executor.execute(new DeviceDiscoveryRequest(device,
				discoveredDeviceProxies, resolver, context));
	}

	/**
	 * Method invoked when a device is no longer available. We dispose all
	 * created discoveredDeviceProxies.
	 * 
	 * WARNING IMPORTANT Notice that this is an iPojo callback, and we should
	 * not block inside, so we process the request asynchronously.
	 */
	@SuppressWarnings("unused")
	private void unboundDevice(UPnPDevice device) {

		/*
		 * If APAM is not available, just remove any pending request
		 */
		synchronized (this) {
			if (resolver == null)
				pending.remove(device);
		}

		/*
		 * first we update synchronously the device table, so we can respect the
		 * order of events (bound/unbound) for each device
		 */
		List<ApformInstance> serviceProxies = null;
		synchronized (discoveredDeviceProxies) {
			serviceProxies = discoveredDeviceProxies.remove(device);
		}

		executor.execute(new DeviceLostRequest(device, serviceProxies));
	}


}
