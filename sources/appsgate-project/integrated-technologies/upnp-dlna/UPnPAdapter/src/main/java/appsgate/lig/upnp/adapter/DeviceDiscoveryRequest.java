package appsgate.lig.upnp.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import fr.imag.adele.apam.ApamResolver;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.apform.ApformInstance;
import fr.imag.adele.apam.impl.InstanceImpl;
import fr.imag.adele.apam.util.ApamFilter;

/**
 * The handler of the discovery request, It look for a proxy for each service and create the
 * appropriate instance.
 * 
 */
public class DeviceDiscoveryRequest implements Runnable {

	/**
	 * The discovered device
	 */
	private final UPnPDevice device;
	
	private ApamResolver resolver;
	private final BundleContext context;
	
	private static Logger logger = LoggerFactory.getLogger(DeviceDiscoveryRequest.class);
	
	/**
	 * The list of created proxies for each discovered device
	 * 
	 */
	private Map<UPnPDevice,List<ApformInstance>> discoveredDeviceProxies = new HashMap<UPnPDevice, List<ApformInstance>>() ;
	
	
	public DeviceDiscoveryRequest(UPnPDevice device, Map<UPnPDevice,List<ApformInstance>> discoveredDeviceProxies,
			ApamResolver resolver, BundleContext context) {
		this.device = device;
		this.discoveredDeviceProxies = discoveredDeviceProxies;
		this.resolver = resolver;
		this.context = context;		
	}
	
	@Override
	public void run() {

		String deviceId		= (String)device.getDescriptions(null).get(UPnPDevice.ID);
		String deviceType	= (String)device.getDescriptions(null).get(UPnPDevice.TYPE);
		
		String deviceName  = (String)device.getDescriptions(null).get(UPnPDevice.FRIENDLY_NAME);		
		

		/*
		 * IMPORTANT Because we are processing this event asynchronously, we need to verify that the device is
		 * still available, and abort the processing as soon as possible.
		 */
		synchronized (discoveredDeviceProxies) {
			if (! discoveredDeviceProxies.containsKey(device))
				return;
		}
		
		/*
		 * IMPORTANT Because we are processing this event asynchronously, we need to verify that APAM is
		 * still available, and abort the processing as soon as possible.
		 */
		synchronized (this) {
			if (resolver == null)
				return;
		}

		logger.debug("[UPnP Apam Discovery] Device ("+deviceType+") discovered :"+deviceId);

		/*
		 * Look for an implementation 
		 */
		Implementation implementation = null;
		
		ApamFilter deviceProxyFilter = ApamFilter.newInstance("("+UPnPDevice.TYPE+"="+deviceType+")");
		if (deviceProxyFilter != null) {
			implementation	= resolver.resolveSpecByName(null,CoreObjectSpec.class.getSimpleName(),Collections.singleton(deviceProxyFilter.toString()),null);
		}
		
		if (implementation == null) {
			logger.error("[UPnP Apam Discovery] Device proxy not found for type  "+deviceType);
		}
		else {
			try {
				
				logger.debug("[UPnP Apam Discovery] Device proxy found for type "+deviceType+" : "+implementation.getName());
	
				/*
				 * Create an instance of the proxy, and configure it for the appropriate device id
				 */
	
				Map<String,Object> configuration = new Hashtable<String,Object>();
				configuration.put(UPnPDevice.ID,deviceId);
				configuration.put(UPnPDevice.FRIENDLY_NAME,deviceName);
	
				ApformInstance proxy = implementation.getApformImpl().addDiscoveredInstance(configuration);
				
				/*
				 * Ignore errors creating the proxy
				 */
				if (proxy == null) {
					logger.error("[UPnP Apam Discovery] Device proxy could not be instantiated  "+implementation.getName());
				}
				else {
					/*
					 * Update the service map
					 */
					synchronized (discoveredDeviceProxies) {
						
						/*
						 * If the device is no longer available, just dispose the created proxy and abort processing 
						 */
						if (! discoveredDeviceProxies.containsKey(device)) {
							if (proxy.getApamComponent() != null)
								((InstanceImpl)proxy.getApamComponent()).unregister();
							return;
						}
		
						/*
						 * otherwise add it to the map
						 */
						discoveredDeviceProxies.get(device).add(proxy);
					}
				}
	
			} catch (Exception e) {
				logger.error("[UPnP Apam Discovery] Device proxy could not be instantiated  "+implementation.getName(),e);
			}
		
		}
		
		/*
		 * Iterate over all declared service of the device, creating the associated proxy
		 */
		
		for (UPnPService service : device.getServices()) {
			
			/*
			 * IMPORTANT Because we are processing this event asynchronously, we need to verify that the device is
			 * still available, and abort the processing as soon as possible.
			 */
			synchronized (discoveredDeviceProxies) {
				if (! discoveredDeviceProxies.containsKey(device))
					return;
			}
			
			/*
			 * IMPORTANT Because we are processing this event asynchronously, we need to verify that APAM is
			 * still available, and abort the processing as soon as possible.
			 */
			synchronized (this) {
				if (resolver == null)
					return;
			}


			/*
			 * Look for an implementation 
			 */
			
			implementation = null;
			
			ApamFilter serviceProxyFilter = ApamFilter.newInstance("("+UPnPService.TYPE+"="+service.getType()+")");
			if (serviceProxyFilter != null) {
				implementation	= resolver.resolveSpecByName(null,CoreObjectSpec.class.getSimpleName(),Collections.singleton(serviceProxyFilter.toString()),null);
			}
			
			
			if (implementation == null) {
				logger.error("[UPnP Apam Discovery] Proxy not found for service type  "+service.getType());
				continue;
			}
			
			try {
				
				logger.debug("[UPnP Apam Discovery] Proxy found for service type "+service.getType()+" : "+implementation.getName());

				final String serviceFilter =	"(&" +
													"("+UPnPDevice.ID+"="+deviceId+")" +
													"("+UPnPService.ID+"="+service.getId()+")" +
												")";

				/*
				 * Create an instance of the proxy, and configure it for the appropriate device and service
				 */

				Map<String,Object> configuration = new Hashtable<String,Object>();
				configuration.put(UPnPDevice.ID,deviceId);
				configuration.put(UPnPService.ID,service.getId());
				configuration.put(UPnPEventListener.UPNP_FILTER, context.createFilter(serviceFilter));
				configuration.put("requires.filters", new Hashtable<String,String>(Collections.singletonMap(UPnPDevice.ID,serviceFilter)) );

				ApformInstance proxy = implementation.getApformImpl().addDiscoveredInstance(configuration);
				
				/*
				 * Ignore errors creating the proxy
				 */
				if (proxy == null) {
					logger.error("[UPnP Apam Discovery] Service proxy could not be instantiated  "+implementation.getName());
					continue;
				}
				
				/*
				 * Update the service map
				 */
				synchronized (discoveredDeviceProxies) {
					
					/*
					 * If the device is no longer available, just dispose the created proxy and abort processing 
					 */
					if (! discoveredDeviceProxies.containsKey(device)) {
						if (proxy.getApamComponent() != null)
							((InstanceImpl)proxy.getApamComponent()).unregister();
						return;
					}

					/*
					 * otherwise add it to the map
					 */
					discoveredDeviceProxies.get(device).add(proxy);
				}

			} catch (Exception e) {
				logger.error("[UPnP Apam Discovery] Service proxy could not instantiated  "+implementation.getName(),e);
			}
			
		}
		
	}
	
}
