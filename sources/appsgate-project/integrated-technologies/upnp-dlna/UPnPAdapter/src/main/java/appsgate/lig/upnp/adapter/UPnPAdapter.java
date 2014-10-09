package appsgate.lig.upnp.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.send.SendWebsocketsService;
import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import fr.imag.adele.apam.ApamResolver;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.apform.ApformInstance;
import fr.imag.adele.apam.impl.InstanceImpl;
import fr.imag.adele.apam.util.ApamFilter;

/**
 * This class listen for UPnPDevice service discovery events and creates the APAM proxy for all
 * hosted services in the device.
 * 
 * Proxies are looked up using Apam resolver capabilities, so they may be deployed as a side 
 * effect of a discovery.
 * 
 * @author vega
 * 
 */
public class UPnPAdapter {
	
	private static Logger logger = LoggerFactory.getLogger(UPnPAdapter.class);

	/**
	 * The bundle context of the discovery
	 */
	private final BundleContext context;
	
	private ListenerService listenerService;
	private SendWebsocketsService sendToClientService;

	public UPnPAdapter(BundleContext context) {
		this.context = context;
	}

	/**
	 * The event executor. We use a pool of a threads to notify APAM of
	 * underlying platform events, without blocking the platform thread.
	 */
	static private final Executor executor = Executors.newCachedThreadPool();

	private ApamResolver resolver;

	/**
	 * The list of created proxies for each discovered device. Notice that we use the OSGi service reference as
	 * a key to be able to handle the case of a single physical device that appears/disappears several times .
	 * 
	 */
	private Set<ServiceReference> discoveredDevices = new HashSet<ServiceReference>();
	
	/**
	 * The list of devices discovered before the adapter was initialized
	 */
	private Set<ServiceReference> pending = new HashSet<ServiceReference>();

	/**
	 * Initialize adapter
	 */
	@SuppressWarnings("unused")
	private synchronized void start() {
		
		resolver = CST.apamResolver;
		logger.debug("[UPnP Apam Discovery] Bound to APAM resolver " + resolver);

		/*
		 * Schedule pending device discovery requests
		 */
		for (ServiceReference deviceReference : pending) {
			String udn	= (String) deviceReference.getProperty(UPnPDevice.UDN);
			logger.debug("[UPnP Apam Discovery] ... scheduling pending request for "+ udn);
			boundDevice(deviceReference);
		}

		pending.clear();
		
		/*
		 * Register with appsgate communication manager
		 */
		if(listenerService != null && sendToClientService != null) {
			UPnPConfigListener listener = new UPnPConfigListener(sendToClientService);
			listenerService.addCommandListener(listener, UPnPConfigListener.CONFIG_TARGET);
		}
			

	}
	
	private final boolean isActivated() {
		return resolver != null;
	}

	/**
	 * Dispose adpater
	 */
	@SuppressWarnings("unused")
	private synchronized void dispose() {
		
		pending.clear();
		discoveredDevices.clear();
		
		resolver = null;
		logger.debug("[UPnP Apam Discovery] Unbound from APAM resolver "+ resolver);
		
		/*
		 * Unregister from communication manager
		 */
		if(listenerService != null && sendToClientService != null) {
			listenerService.removeCommandListener(UPnPConfigListener.CONFIG_TARGET);
		}		
	}


	/**
	 * Method invoked with a new device hosting the UPnP services is discovered.
	 * 
	 * WARNING IMPORTANT Notice that this is an iPojo callback, and we should not block inside, so we asynchronously
	 * create all Apam proxies for services hosted by the device..
	 */
	private synchronized void boundDevice(ServiceReference deviceReference) {

		/*
		 * If this adapter is not completely initialized, just hold the request
		 */
		if (! isActivated()) {
			pending.add(deviceReference);
			return;
		}

		/*
		 * first we update synchronously the device table, so we can respect the order of events (bound/unbound)
		 * for each device registration
		 */
		discoveredDevices.add(deviceReference);
		executor.execute(new DeviceDiscoveryRequest(deviceReference,this));
	}


	/**
	 * Method invoked when a device is no longer available. 
	 * 
	 * NOTE The service proxy will be automatically disposed by Apam when its corresponding UPnPDevice is 
	 * unregistered (because if has a mandatory iPOJO dependency towards it). However we must do some 
	 * house-keeping of our internal table here.
	 * 
	 * WARNING IMPORTANT Notice that this is an iPojo callback, and we should not block inside.
	 */
	@SuppressWarnings("unused")
	private synchronized void unboundDevice(ServiceReference deviceReference) {

		String udn			= (String) deviceReference.getProperty(UPnPDevice.UDN);
		String deviceName	= (String) deviceReference.getProperty(UPnPDevice.FRIENDLY_NAME);		
		String deviceType	= (String) deviceReference.getProperty(UPnPDevice.TYPE);


		/*
		 * free the reference to the UPnP device that we could have acquired when we created the proxies
		 */
		context.ungetService(deviceReference);

		/*
		 * If APAM is not available, just remove from the pending request list
		 */
		if (! isActivated())
			pending.remove(deviceReference);

		/*
		 * We update synchronously the device table, so we can respect the order of events (bound/unbound)
		 * for each device
		 */
		discoveredDevices.remove(deviceReference);
		logger.debug("[UPnP Apam Discovery] Device ("+deviceType+") unbound :"+deviceName+"["+udn+"]");
	}
	
	
	/**
	 * Whether handling of processing of the specified device reference has been aborted
	 */
	private synchronized final boolean isProcessingAborted(ServiceReference deviceReference) {

		/*
		 * IMPORTANT Because we are processing this event asynchronously, we need to verify that APAM is
		 * still available, and abort the processing as soon as possible.
		 */
		if (! isActivated())
			return true;

		/*
		 * IMPORTANT Because we are processing this event asynchronously, we need to verify that the device is
		 * still available, and abort the processing as soon as possible.
		 */
		return ! discoveredDevices.contains(deviceReference);
	}
	
	/**
	 * Creates an Apam proxy for each service hosted by the specified device.
	 * 
	 * NOTE IMPORTANT notice that implementations are searched using the Apam resolver, then proxies are
	 * dynamically deployed when the associated devices are discovered. Deployment and instantiation in
	 * Apam can potentially block, so we avoid synchronization to allow discovery events to be processed
	 * concurrently.
	 */
	protected void createProxies(ServiceReference deviceReference) {

		/*
		 * IMPORTANT Because we are processing this event asynchronously, we need to verify that the device is
		 * still available and registered, and abort the processing as soon as possible.
		 */
		UPnPDevice device	= (UPnPDevice) context.getService(deviceReference);
		if (device == null)
			return;
		
		String udn			= (String)device.getDescriptions(null).get(UPnPDevice.UDN);
		String deviceName	= (String)device.getDescriptions(null).get(UPnPDevice.FRIENDLY_NAME);		
		String deviceType	= (String)device.getDescriptions(null).get(UPnPDevice.TYPE);


		logger.debug("[UPnP Apam Discovery] Device ("+deviceType+") discovered :"+deviceName+"["+udn+"]");
		
		/*
		 * Iterate over all declared service of the device, creating the associated proxy
		 */

		List<ApformInstance> proxies = new ArrayList<ApformInstance>();
		
		for (UPnPService service : device.getServices()) {
			
			/*
			 * Try to fail fast in case the device is no longer available
			 */
			if (isProcessingAborted(deviceReference))
				break;
			
			/*
			 * Look for an implementation 
			 */
			
			Implementation implementation = null;
			
			ApamFilter serviceProxyFilter = ApamFilter.newInstance("("+UPnPService.TYPE+"="+service.getType()+")");
			if (serviceProxyFilter != null) {
				implementation	= resolver.resolveSpecByName(null,"UPnPService",Collections.singleton(serviceProxyFilter.toString()),null);
			}
			
			
			if (implementation == null) {
				logger.error("[UPnP Apam Discovery] Proxy not found for service type  "+service.getType());
				continue;
			}
			
			try {
				
				logger.debug("[UPnP Apam Discovery] Proxy found for service type "+service.getType()+" : "+implementation.getName());

				final String serviceFilter =	"(&" +
													"("+UPnPDevice.ID+"="+udn+")" +
													"("+UPnPService.ID+"="+service.getId()+")" +
												")";

				/*
				 * Create an instance of the proxy, and configure it for the appropriate device and service
				 */

				Map<String,Object> configuration = new Hashtable<String,Object>();
				configuration.put(UPnPDevice.ID,udn);
                configuration.put(UPnPDevice.FRIENDLY_NAME,deviceName);
				configuration.put(UPnPService.ID,service.getId());
				configuration.put(UPnPEventListener.UPNP_FILTER, context.createFilter(serviceFilter));
				configuration.put("requires.filters", new Hashtable<String,String>(Collections.singletonMap(UPnPDevice.ID,"("+Constants.SERVICE_ID+"="+deviceReference.getProperty(Constants.SERVICE_ID)+")")) );

				ApformInstance proxy = implementation.getApformImpl().addDiscoveredInstance(configuration);
				
				/*
				 * Ignore errors creating the proxy
				 */
				if (proxy == null) {
					logger.error("[UPnP Apam Discovery] Service proxy could not be instantiated  "+implementation.getName());
					continue;
				}
			
				proxies.add(proxy);

			} catch (Exception e) {

				/*
				 * Ignore errors creating the proxy
				 */
				logger.error("[UPnP Apam Discovery] Service proxy could not be instantiated  "+implementation.getName(),e);
			}
			
		}
		
		/*
		 * destroy all proxies created in case of aborted processing 
		 */
		if (isProcessingAborted(deviceReference)) {
			for (ApformInstance proxy : proxies) {
				try {
					if (proxy.getApamComponent() != null) {
						((InstanceImpl)proxy.getApamComponent()) .unregister();
					}
				} catch (Exception e) {
					logger.error("[UPnP Apam Discovery] Service proxy could not be disposed  "+proxy.getDeclaration().getName(),e);
				}
			}
		}
		
	}
	


}
