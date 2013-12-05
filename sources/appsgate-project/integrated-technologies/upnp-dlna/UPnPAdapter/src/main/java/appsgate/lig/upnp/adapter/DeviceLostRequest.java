package appsgate.lig.upnp.adapter;

import java.util.List;

import org.osgi.service.upnp.UPnPDevice;

import fr.imag.adele.apam.apform.ApformInstance;
import fr.imag.adele.apam.impl.InstanceImpl;

/**
 * The handler of the unbound request, It dispose all the created proxies associated to the device.
 * 
 */
public class DeviceLostRequest implements Runnable {

	/**
	 * The unbound device
	 */
	private final UPnPDevice device;
	/**
	 * The proxies to dispose
	 */
	private final List<ApformInstance> proxies;
	
	public DeviceLostRequest(UPnPDevice device, List<ApformInstance> proxies) {
		this.device		= device;
		this.proxies	= proxies;
	}
	
	@Override
	public void run() {

		String deviceId		= (String)device.getDescriptions(null).get(UPnPDevice.ID);
		String deviceType	= (String)device.getDescriptions(null).get(UPnPDevice.TYPE);
		
		System.err.println("[UPnP Apam Discovery] Device ("+deviceType+") unbound :"+deviceId);
		
		if (proxies == null)
			return;
		
		for (ApformInstance proxy : proxies) {
			if (proxy.getApamComponent() != null)
				((InstanceImpl)proxy.getApamComponent()).unregister();
		}
	}
	
}