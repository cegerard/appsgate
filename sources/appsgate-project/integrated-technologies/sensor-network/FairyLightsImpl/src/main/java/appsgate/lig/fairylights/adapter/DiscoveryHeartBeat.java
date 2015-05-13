/**
 * 
 */
package appsgate.lig.fairylights.adapter;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.fairylights.utils.HttpUtils;

/**
 * @author thibaud
 *
 */
public class DiscoveryHeartBeat extends TimerTask {
	
	private static Logger logger = LoggerFactory.getLogger(DiscoveryHeartBeat.class);

	
	boolean available;
	FairyLightsDiscoveryListener listener;
	String protocol;
	String hostname;
	String apiUrl;
	
	public DiscoveryHeartBeat(FairyLightsDiscoveryListener listener,
			String protocol, String hostname, String apiUrl) {
		this.listener = listener;
		this.protocol = protocol;
		this.hostname = hostname;
		this.apiUrl = apiUrl;
		available = false;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		logger.trace("run()");
		available = false;
		if(HttpUtils.testURLTimeout(protocol+hostname+apiUrl, 30*1000) ) {		
			try {

				Inet4Address address = (Inet4Address) Inet4Address.getByName(hostname);
				String host=protocol + address.getHostAddress();
				logger.trace("run(), the device is available");
				available = true;
				listener.deviceAvailable(host);

			} catch (UnknownHostException e) {
				logger.info("run(), the device is NOT available,"+ e.getMessage());
			}
		} else {
			logger.info("run(), timeout when calling the lumipixel device, it is NOT available");
		}

	}
	
	/**
	 * return true if the devices was avalable during last heartbeat (last run() of the task)
	 * @return
	 */
	public boolean isAvailable() {
		return available;
	}

}
