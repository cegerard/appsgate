

package appsgate.lig.upnp.media.player.adapter;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.service.upnp.UPnPDevice;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentImpl.InvalidConfiguration;

public class MediaPlayerFactory {
	
	public void mediaRendererBound(Instance device) {
		

		try {
			Implementation adapterImplementtation = CST.apamResolver.findImplByName(null,"MediaPlayer");

			String deviceId = device.getProperty(UPnPDevice.ID);

			Map<String,Object> configuration = new Hashtable<String,Object>();
			configuration.put(UPnPDevice.ID,deviceId);
			adapterImplementtation.getApformImpl().addDiscoveredInstance(configuration);

		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}

	public void mediaRendererUnbound(Instance instance) {
		
	}

}
