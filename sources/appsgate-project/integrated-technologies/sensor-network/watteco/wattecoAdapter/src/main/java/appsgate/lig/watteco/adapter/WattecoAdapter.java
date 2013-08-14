package appsgate.lig.watteco.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.watteco.adapter.BorderRouter.BorderRouterCommand;
import appsgate.lig.watteco.adapter.services.WattecoDiscoveryService;
import appsgate.lig.watteco.adapter.services.WattecoIOService;
import appsgate.lig.watteco.adapter.services.WattecoTunSlipManagement;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

/**
 * This class is used to connect the Watteco wsn to the ApAM environment. An
 * instance of this class instanciate Watteco sensors with ApAM implementation.
 * 
 * This class it is also an OSGi/iPOJO bundle, that provide two services and
 * instanciate itself automatically.
 * 
 * @author Cédric Gérard
 * @since August 13, 2013
 * @version 1.0.0
 * 
 * 
 * @provide WattecoIOService to manage input and output datagram from wsn
 *          Watteco network
 * @provide WattecoDiscoveryService to get all the actually paired sensors and
 *          manage the sensor configuration.
 * @provide WattecoTunSlipManagement to manage the network interface for Watteco
 *          border router
 * 
 * @see WattecoIOService
 * @see WattecoDiscoveryService
 * @see WattecoTunSlipManagement
 * 
 */
@Component(publicFactory = false)
@Instantiate(name = "AppsgateWattecoAdapter")
@Provides(specifications = { WattecoIOService.class })
public class WattecoAdapter implements WattecoIOService,
		WattecoDiscoveryService, WattecoTunSlipManagement {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoAdapter.class);
	
	private ScheduledExecutorService instanciationService;
	
	private BorderRouter borderRouter;

	public WattecoAdapter() {
		super();
		this.borderRouter = new BorderRouter();
		instanciationService = Executors.newScheduledThreadPool(1);
		logger.debug("ExecutorService instanciated.");
	}

	@Validate
	public void newInst() {
		logger.info("Appsgate Watteco adapter intiated.");
		ArrayList<String> ipList = discover("http://["+"aaaa::ff:ff00:2bf9]"+"/");
		
		for(String ip : ipList) {
			logger.debug("@@@@@@@@@@@@@ Sensor ip: "+ip);
		}
		
		//Determine sensor type whit configuration ?
		
		//Instantiate sensor with corresponding ApAM implementation
		instanciationService.execute(new sensorInstanciation(ipList));
	}

	@Invalidate
	public void delInst() {
		logger.info("Appsgate Watteco adapter stopped.");
	}

	@Override
	public byte[] sendCommand(String addr, String c) {
		InetAddress address;
		
		try {
			address = InetAddress.getByName(addr);
			return borderRouter.sendCommand(address, BorderRouterCommand.SP_TOGGLE);
			
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return new byte[1];
	}

	/**
	 * Get the Ipv6 address list from the Watteco border router
	 * @param borderRouterAddress the Watteco border router address
	 */
	@Override
	public ArrayList<String> discover(String borderRouterAddress) {

		ArrayList<String> ip6 = new ArrayList<String>();

		try {
			String s;
			String indexPage = "";
			
			//get the index.html page of the border router
			BufferedReader r = new BufferedReader(new InputStreamReader(new URL(borderRouterAddress).openStream()));
			while ((s = r.readLine()) != null) {
				indexPage += s;
			}
			r.close();
			
			//split the index.html string by <h2> html tag
			String[] splitH2 = indexPage.split("h2>");
			//split the result of the previous split with <br> html tag to get the ipv6 sensor addresses
			String[] splitBR = splitH2[2].split("<br>");
			
			//Fill the ArrayList with all ipv6 address get from the border router
			int loopSize = splitBR.length-1;
			int i = 0;
			while(i < loopSize) {
				ip6.add(splitBR[i]);
				i++;
			}

		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
		
		return ip6;
	}
	
	private void instanciateSensor(String address) {
		
		Implementation impl = CST.apamResolver.findImplByName(null, "WattecoSmartPlugImpl");
		
		Map<String, String> initialproperties = new HashMap<String, String>();
		initialproperties.put("deviceId", address);
		CharSequence subaddress = address.subSequence(4, address.length());
		initialproperties.put("networkRoute", "aaaa"+subaddress);
		initialproperties.put("isPaired", "true");
	
		impl.createInstance(null, initialproperties);
	}
	
	/**
	 * Inner class for Ubikit items instanciation thread
	 * @author Cédric Gérard
	 * @since June 25, 2013
	 * @version 1.0.0
	 */
	private class sensorInstanciation implements Runnable {

		ArrayList<String> addressList;
		
		public sensorInstanciation(ArrayList<String> addressList) {
			super();
			this.addressList = addressList;
		}

		public void run() {
			for (String address : addressList) {
				instanciateSensor(address);
			}
		}
	}

}
