package appsgate.lig.watteco.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.ListenerService;
import appsgate.lig.watteco.adapter.listeners.WattecoConfigListener;
import appsgate.lig.watteco.adapter.services.WattecoDiscoveryService;
import appsgate.lig.watteco.adapter.services.WattecoIOService;
import appsgate.lig.watteco.adapter.services.WattecoTunSlipManagement;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

/**
 * This class is used to connect the Watteco WSN to the ApAM environment. An
 * instance of this class instantiate Watteco sensors with ApAM implementation.
 * 
 * This class it is also an OSGi/iPOJO bundle, that provide two services and
 * instantiate itself automatically.
 * 
 * @author Cédric Gérard
 * @since August 13, 2013
 * @version 1.0.0
 * 
 * 
 * @provide WattecoIOService to manage input and output datagram from WSN
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
@Provides(specifications = { WattecoIOService.class, WattecoDiscoveryService.class })
public class WattecoAdapter implements WattecoIOService,
		WattecoDiscoveryService, WattecoTunSlipManagement {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoAdapter.class);
	
	private static String CONFIG_TARGET = "WATTECO";
	
	/**
	 * Executor scheduler for sensor instantiation
	 */
	private ScheduledExecutorService instanciationService;
	
	/**
	 * Border router member
	 */
	private BorderRouter borderRouter;

	/**
	 * Stand for the SLIP tunnel status
	 */
	private boolean slipTunnelOn;
	
	/**
	 * The border router identifier
	 */
	private String BR_ID = "A701QQEY";
	
	/**
	 * Keep the Ipv6 sensor Addresses
	 */
	private HashMap<String, Instance> ipv6AddressToInstance;
	
	/**
	 * Service to be notified when clients send commands
	 */
	private ListenerService listenerService;
	
	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;

	/**
	 * Default constructor
	 */
	public WattecoAdapter() {
		super();
		this.borderRouter = new BorderRouter(this);
		instanciationService = Executors.newScheduledThreadPool(1);
		slipTunnelOn = false;
		ipv6AddressToInstance = new HashMap<String, Instance>();
		logger.debug("Appsgate Watteco adapter instanciated.");
	}
	
	/* ***********************************************************************
	 * 						  APAM CALL BACK                                 *
	 *********************************************************************** */

	/**
	 * Method call by ApAM when a new instance of Watteco adapter is created
	 */
	@Validate
	public void newInst() {
		//Initiate the SLIP tunnel from C source from Watteco
		// /!\ this use native source code so it must be compile to the targeted platform
		// 1- Find where the Watteco border router USB serial port
		String osName =System.getProperty("os.name");
		if (osName.contentEquals("Linux")) {
			try {
				boolean notFound = true;
				int port = 0;
				//while(notFound && port < 10) {
				//Process searchCMD = Runtime.getRuntime().exec("udevadm info -q symlink --name=ttyUSB"+port);
				Process searchCMD = Runtime.getRuntime().exec("ls -l /dev/serial/by-id/");
				BufferedReader read = new BufferedReader(new InputStreamReader(searchCMD.getInputStream()));
				try {
					searchCMD.waitFor();
		       } catch (InterruptedException e) {logger.error(e.getMessage());}
		        
		       while(notFound && port < 10) {
		            String result = read.readLine();
		            if(result.contains(BR_ID)) {
		            	notFound = false;
		            	port = Integer.valueOf(result.substring(result.length()-1));
		            }else{
		            	port++;
		            }
				}
		       
	          // 2- Run the tunslip configuration program to the previous found serial port
	          String cmd = "./conf/watteco/tunslip6 -L -v2 -s ttyUSB"+port+" aaaa::1/64 &";
	          logger.debug("Set up slip tunel with command: " +cmd);
	          Runtime.getRuntime().exec(cmd);
	          logger.debug("tunslip set up");
	          //4- launch the discovery phase of Watteco sensors desynchronized by 15 seconds to give 
	          //enough time to the tun0 network interface to be up.
	          instanciationService.schedule(new sensorDiscovery(BORDER_ROUTER_ADDR), 15, TimeUnit.SECONDS);
	          
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("This bundle embbeded native C lib executing code and only Linux system are supported.");
		}  
		
		logger.info("Appsgate Watteco adapter initiated.");
	}

	/**
	 * method call by ApAM when a instance of Watteco adapter disappeared
	 */
	@Invalidate
	public void delInst() {
		//slipTunnelOn = false;
		borderRouter.stopBorderRouter();
		instanciationService.shutdown();
		try {
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.debug("Watteco Adapter instanciation service thread crash at termination");
		}
		
		if(listenerService.removeConfigListener(CONFIG_TARGET)){
			logger.info("Watteco configuration listener removed.");
		}else{
			logger.warn("Watteco configuration listener remove failed.");
		}
		
//		Implementation impl = CST.apamResolver.findImplByName(null, WattecoAdapter.SMART_PLUG_IMPL);
//		Set<Instance> insts = impl.getInsts();
//		for(Instance inst : insts) {
//			ComponentBrokerImpl.disappearedComponent(inst.getName());
//		}
		logger.info("Appsgate Watteco adapter stopped.");	
	}

	/**
	 * Get the subscribe service form OSGi/iPOJO. This service is optional.
	 * 
	 * @param listenerService
	 *            , the subscription service
	 */
	@Bind(optional = true)
	public void bindSubscriptionService(ListenerService listenerService) {
		this.listenerService = listenerService;
		logger.debug("Communication subscription service dependency resolved");
		logger.info("Getting the listeners services...");
		if(listenerService.addConfigListener(CONFIG_TARGET, new WattecoConfigListener(this))){
			logger.info("Listeners services dependency resolved.");
		}else{
			logger.info("Listeners services dependency resolution failed.");
		}
	}

	/**
	 * Call when the Watteco adapter release the optional subscription service.
	 * 
	 * @param listenerService
	 *            , the released subscription service
	 */
	@Unbind(optional = true)
	public void unbindSubscriptionService(ListenerService listenerService) {
		this.listenerService = null;
		logger.debug("Subscription service dependency not available");
	}
	
	/**
	 * Get the HTTP service form OSGi/iPojo. This service is optional.
	 * 
	 * @param httpService the HTTP service
	 */
	@Bind(optional = true)
	public void bindHTTPService(HttpService httpService) {
		this.httpService = httpService;
		logger.debug("HTTP service dependency resolved");
		//if (httpService != null) {
		final HttpContext httpContext = httpService.createDefaultHttpContext();
		final Dictionary<String, String> initParams = new Hashtable<String, String>();
		initParams.put("from", "HttpService");
		try {
			httpService.registerResources("/configuration/sensors/watteco", "/WEB", httpContext);
			logger.info("Sensors configuration HTML GUI sources registered.");
		} catch (NamespaceException ex) {
			logger.error("NameSpace exception");
		}
		//}
	}
	
	/**
	 * Call when the EnOcean proxy release the HTTP service.
	 * 
	 * @param httpService the HTTP service
	 */
	@Unbind(optional = true)
	public void unbindHTTPService(HttpService httpService) {
		this.httpService = null;
		logger.debug("HTTP service dependency not available");
	}
	
	/* ***********************************************************************
	 * 						  PUBLIC METHODS                                 *
	 *********************************************************************** */
	
	@Override
	public byte[] sendCommand(String addr, String cmd, boolean resp) {
		if(slipTunnelOn) {
			InetAddress address;
			try {
				address = InetAddress.getByName(addr);
				return borderRouter.sendCommand(borderRouter.new BorderRouterCommand(address, cmd, resp));
			} catch (UnknownHostException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void discover(String borderRouterAddress) {
		ArrayList<String> ip6 = getSensorList(borderRouterAddress);
		
		//Strip exiting sensor IPv6 address
		for(String key : ipv6AddressToInstance.keySet()) {
			ip6.remove(key);
		}
		
		//If a sensor is not yet instantiate, instantiate it
		if(! ip6.isEmpty()){
			//instanciationService.execute(new sensorInstanciation(ip6));
			// the thread cause Watteco adapter instance failure since iPojo 1.11
			new sensorInstanciation(ip6).run();
		}else {
			logger.info("No new Watteco sensor detected. All are already set up or are out of range from the border router");
		}
	}
	
	@Override
	public void discover() {
		discover("http://["+BORDER_ROUTER_ADDR+"]/");
	}
	
	@Override
	public void setBorderRouterId(String newBrId) {
		BR_ID = newBrId;
	}
	
	@Override
	public String getBorderRouterId() {
		return BR_ID;
	}
	
	@Override
	public ArrayList<String> getSensorList (String borderRouterAddress) {
		ArrayList<String> ip6 = new ArrayList<String>();
		if(slipTunnelOn) {
			try {
				String s;
				String indexPage = "";
			
				//get the index.html page of the border router
				BufferedReader r = new BufferedReader(new InputStreamReader(new URL(borderRouterAddress).openStream()));
				while ((s = r.readLine()) != null) {
					indexPage += s;
				}
				r.close();
			
				//split the index.html string by <h2> HTML tag
				String[] splitH2 = indexPage.split("h2>");
				//split the result of the previous split with <br> HTML tag to get the ipv6 sensor addresses
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
		}
		return ip6;
	}
	
	/**
	 * Get the ApAm instance corresponding to an IPv6 address
	 * @param address the sensor Ipv6 address
	 * @return the ApAM instance if exist, null otherwise
	 */
	public Instance getInstanceFormAddress(String address) {
		return ipv6AddressToInstance.get(address);
	}
	
	/**
	 * Convert bytes array to integer
	 * @param bytes the bytes array
	 * @return the value as an integer
	 */
	public int bytesToInt(byte[] bytes) {
        int integer = 0;
        for(int i = 0; i < bytes.length; i ++) {
            integer += ((256 + bytes[i]) % 256) * (int)Math.pow(256, i);
        }
        return integer;
    }

	/* ***********************************************************************
	 * 						   	 INNER CLASS                                 *
	 *********************************************************************** */
	
	/**
	 * Inner class for Watteco sensor instanciation thread
	 * @author Cédric Gérard
	 * @since August 17, 2013
	 * @version 1.0.0
	 */
	private class sensorInstanciation implements Runnable {

		ArrayList<String> addressList;
		
		public sensorInstanciation(ArrayList<String> addressList) {
			super();
			this.addressList = addressList;
			logger.debug(addressList.size()+ " Watteco sensor(s) to instanciate.");
		}

		public void run() {
			for (String address : addressList) {
				instantiateSensor(address);
			}
		}
	}
	
	/**
	 * Inner class for Watteco sensor discovery
	 * @author Cédric Gérard
	 * @since August 22, 2013
	 * @version 1.0.0
	 */
	private class sensorDiscovery implements Runnable {

		String address;
		
		public sensorDiscovery(String address) {
			super();
			this.address = address;
		}

		public void run() {
			try {
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
				while(nets.hasMoreElements()){
		        	  NetworkInterface netInt = nets.nextElement();
		        	  if(netInt.getDisplayName().contentEquals("tun0")){
		        		  slipTunnelOn = true;
		        		  logger.info("tun0 interface for Watteco sensors UP");
		        		  logger.info("searching for new Watteco sensors...");
		        		  //launch sensor discovery thread
		        		  discover("http://["+address+"]/");
		        		  break;
		        	  }
		         }
			} catch (SocketException e) {logger.error(e.getMessage());}
		}
	}
	
	
	/* ***********************************************************************
	 * 							PRIVATE METHODS                              *
	 *********************************************************************** */
	
	/**
	 * Instantiate a sensor from its ipv6 address
	 * @param address the ipv6 address of the sensor to instantiate
	 */
	private void instantiateSensor(String address) {
		
		CharSequence subaddress = address.subSequence(4, address.length());
		String route = "aaaa"+subaddress;
		
		//Sensor type identification
		byte[] ret = sendCommand(route, WattecoAdapter.CONFIGURATION_DESC, true);
		if(ret != null) {
			
			//Sensor type recognition
			ArrayList<String> clusterList = extractInputClusterFromConfDescription(ret);
			
			//ApAM implementation recovery
			String implName = getApAMImplFromClusterList(route, clusterList);
			Implementation impl = CST.apamResolver.findImplByName(null, implName);
		
			//ApAM instance initialization
			Map<String, String> initialproperties = new HashMap<String, String>();
			initialproperties.put("deviceId", address);
			initialproperties.put("networkRoute", route);
			initialproperties.put("isPaired", "true");
			initiateSensorValues(implName, route, initialproperties);
			
			//Sensor ApAM instantiation
			int nbTry = 0;
			while(nbTry < 5){
				if(impl != null) {
					Instance inst = impl.createInstance(null, initialproperties);
					nbTry = 5;
					//Keep the correspondence between address and ApAm instance
					ipv6AddressToInstance.put(address, inst);
				}else{
					synchronized(this){try {
						logger.error("No "+implName+" found ! -- "+nbTry+" try");
						wait(3000);
					} catch (InterruptedException e) {e.printStackTrace();}}
					nbTry++;
					impl = CST.apamResolver.findImplByName(null, implName);
				}
			}
		} else {
			logger.error("No sensor configuration available for "+address);
		}
	}
	
	/**
	 * Extract input cluster list from sensor configuration description
	 * @param datagram configuration description response
	 * @return the cluster list as an array list of string
	 */
	private ArrayList<String> extractInputClusterFromConfDescription(byte [] datagram) {
		//Get the number of byte for the payload
		byte[] nbBytes = new byte[2];
		nbBytes[1] = datagram[8];
		nbBytes[0] = datagram[9];
		//int size = bytesToInt(nbBytes);
		
		//Get the number of end point;
		nbBytes = new byte[1];
		nbBytes[0] = datagram[10];
		//int nbEndPoint = bytesToInt(nbBytes);
		//byte endpointNumber = datagram[11];
		
		//Get the number of input cluster
		nbBytes[0] = datagram[12];
		int nbInputC = bytesToInt(nbBytes);
		
		//Get input cluster id
		nbBytes = new byte[2];
		int index = 13;
		String cluster;
		int clustInt;
		ArrayList<String> clusterList = new ArrayList<String>();
		for (int i = 0; i < nbInputC; i++) {
			nbBytes[1] = datagram[index];
			nbBytes[0] = datagram[index+1];
			clustInt = bytesToInt(nbBytes);
			cluster = Integer.toHexString(clustInt);
			clusterList.add(cluster);
			index += 2;		
		}
		
		return clusterList;
	}
	
	/**
	 * Get the ApAM implementation corresponding to the Watteco cluster list
	 * @param route the sensor route over the network
	 * @param clusterList the Watteco cluster list
	 * @return an ApAM implementation name
	 */
	private String getApAMImplFromClusterList(String route, ArrayList<String> clusterList) {
		if(clusterList.contains(ON_OFF_CLUSTER) && clusterList.contains(SIMPLE_METERING_CLUSTER)) {
			return WattecoAdapter.SMART_PLUG_IMPL;
		} else if(clusterList.contains(OCCUPANCY_SENSING_CLUSTER)) {
			return WattecoAdapter.OCCUPANCY_IMPL;
		} else if(clusterList.contains(TEMPERATURE_MEASUREMENT_CLUSTER)) {
			return WattecoAdapter.TEMPERATURE_IMPL;
		} else if(clusterList.contains(ANALOG_INPUT_CLUSTER)) {
			//Get the sensor type
			byte[] b = null;
			int temp;
			b = sendCommand(route, WattecoAdapter.ANALOG_INPUT_APPLICATION_ASK, true);
			Byte readByte = new Byte(b[8]);
			temp = (readByte << 32);
			readByte = new Byte(b[9]);
			temp += (readByte << 16);
			readByte = new Byte(b[10]);
			temp += (readByte << 8);
			readByte = new Byte(b[11]);
			temp += readByte;
			if(temp == APPLICATION_TYPE_CO2) {
				return WattecoAdapter.CO2_IMPL;
			}
		}
		return "NOIMPLEM";
	}
	
	/**
	 * Add property values for sensor attributes 
	 * @param implName the corresponding ApAM implementation
	 * @param route the route on the WSN to reach the sensor
	 * @param initialproperties the properties map
	 */
	private void initiateSensorValues(String implName, String route, Map<String, String> initialproperties) {
		byte[] ret; 
		
		if(implName.contentEquals(WattecoAdapter.SMART_PLUG_IMPL)) {
			
			ret = sendCommand(route, WattecoAdapter.ON_OFF_READ_ATTRIBUTE, true);
			//Extract On/Off state
			String state = "false";
			if(ret[8] == Byte.parseByte("1")) {
				state = "true";
			}
			initialproperties.put("plugState", state);
			//Configure ON/OFF cluster reporting
			sendCommand(route, WattecoAdapter.ON_OFF_CONF_REPORTING, false);
			
			//Extract consumption value
			ret = sendCommand(route, WattecoAdapter.SIMPLE_METERING_READ_ATTRIBUTE, true);
			Byte b = new Byte(ret[17]);
			int cons = (b << 8);
			b = new Byte(ret[18]);
			cons += b;
			initialproperties.put("consumption", String.valueOf(cons));
			//Configure simple metering cluster reporting
			sendCommand(route, WattecoAdapter.SIMPLE_METERING_CONF_REPORTING, false);
			
		}else if(implName.contentEquals(WattecoAdapter.OCCUPANCY_IMPL)) {
			ret = sendCommand(route, WattecoAdapter.OCCUPANCY_SENSING_READ_ATTRIBUTE, true);
			String occupied = "false";
			if(new Byte("1").byteValue() == ret[ret.length-1]) {
				occupied = "true";
			}
			initialproperties.put("occupied", occupied);
			//Configure occupancy cluster reporting
			//sendCommand(route, WattecoAdapter.OCCUPANCY_CONF_REPORTING, false);
			
		} else if(implName.contentEquals(WattecoAdapter.TEMPERATURE_IMPL)) {
			int temp = 999;
			ret = sendCommand(route, WattecoAdapter.TEMPERATURE_MEASUREMENT_READ_ATTRIBUTE, true);
			Byte readByte = new Byte(ret[8]);
			temp = (readByte << 8);
			readByte = new Byte(ret[9]);
			temp += readByte;
			
			initialproperties.put("currentTemperature", String.valueOf((Float.valueOf(temp)/100.0)));
			//Keep the default sensor configuration
			
		} else if(implName.contentEquals(WattecoAdapter.CO2_IMPL)) {
			int temp;
			ret = sendCommand(route, WattecoAdapter.ANALOG_INPUT_READ_ATTRIBUTE, true);
			Byte readByte = new Byte(ret[8]);
			temp = (readByte << 32);
			readByte = new Byte(ret[9]);
			temp += (readByte << 16);
			readByte = new Byte(ret[10]);
			temp += (readByte << 8);
			readByte = new Byte(ret[11]);
			temp += readByte;
			
			initialproperties.put("currentCO2Concentration", String.valueOf(temp));
			//Keep the default sensor configuration
			
		}
			
	}

	
	/* ***********************************************************************
	 * 							WATTECO COMMANDS                             *
	 *********************************************************************** */
	
	public static final String BORDER_ROUTER_ADDR 					   = "aaaa::ff:ff00:2bf9";
	
	//ON/OFF cluster commands
	public static final String ON_OFF_CLUSTER				   		   = "6";
	public static final String ON_OFF_OFF 	 				   		   = "$11$50$00$06$00";
	public static final String ON_OFF_ON 	 				   		   = "$11$50$00$06$01";
	public static final String ON_OFF_TOGGLE 				   		   = "$11$50$00$06$02";
	public static final String ON_OFF_READ_ATTRIBUTE  		   		   = "$11$00$00$06$00$00";
	public static final String ON_OFF_CONF_REPORTING  		   		   = "$11$06$00$06$00$00$00$10$00$00$FF$FF$01";
	
	//Simple metering cluster commands
	public static final String SIMPLE_METERING_CLUSTER				   = "52";
	public static final String SIMPLE_METERING_READ_ATTRIBUTE  		   = "$11$00$00$52$00$00";
	public static final String SIMPLE_METERING_CONF_REPORTING  		   = "$11$06$00$52$00$00$00$41$00$00$02$58$0C$00$00$00$00$00$00$00$00$00$02$00$00"; // 10 minutes reporting or 2W variation.
	
	//Temperature measurement cluster commands
	public static final String TEMPERATURE_MEASUREMENT_CLUSTER		   = "402";
	public static final String TEMPERATURE_MEASUREMENT_READ_ATTRIBUTE  = "$11$00$04$02$00$00";
	
	//Humidity measurement cluster commands
	public static final String HUMIDITY_MEASUREMENT_CLUSTER		   	   = "405";
	public static final String HUMIDITY_MEASUREMENT_READ_ATTRIBUTE     = "$11$00$04$05$00$00";
	
	//Occupancy sensing cluster commands
	public static final String OCCUPANCY_SENSING_CLUSTER		   	   = "406";
	public static final String OCCUPANCY_SENSING_READ_ATTRIBUTE  	   = "$11$00$04$06$00$00";
	public static final String OCCUPANCY_SENSING_READ_OCCUPIED  	   = "$11$00$04$06$00$10";
	public static final String OCCUPANCY_SENSING_READ_INOCCUPIED 	   = "$11$00$04$06$00$11";
	public static final String OCCUPANCY_CONF_REPORTING  		   	   = "$11$06$04$06$00$00$00$18$00$02$00$3C$01"; // 2 seconds minimal reporting and 1 minutes each auto-notification

	//Analog input (basic) cluster commands
	public static final String ANALOG_INPUT_CLUSTER				   	   = "c";
	public static final String ANALOG_INPUT_READ_ATTRIBUTE  		   = "$11$00$00$0C$00$55";
	public static final String ANALOG_INPUT_APPLICATION_ASK  		   = "$11$00$00$0C$01$00";
	
	//Binary input (basic) cluster commands
	public static final String BINARY_INPUT_CLUSTER				   	   = "f";
	public static final String BINARY_INPUT_READ_ATTRIBUTE  		   = "$11$00$00$0F$00$55";
	
	//Illumination measurement cluster commands
	public static final String ILLUMINATION_MEASUREMENT_CLUSTER		   = "400";
	public static final String ILLUMINATION_MEASUREMENT_READ_ATTRIBUTE = "$11$00$04$00$00$00";
	
	//Configuration cluster commands
	public static final String CONFIGURATION_CLUSTER		   	   	   = "50";
	public static final String CONFIGURATION_READ_ATTRIBUTE  		   = "$11$00$00$50$00$01";
	public static final String CONFIGURATION_DESC 			   		   = "$11$00$00$50$00$04";
	
	//Basic cluster commands
	public static final String BASIC_CLUSTER		 			  	   = "0";
	public static final String BASIC_READ_ATTRIBUTE  				   = "$11$00$00$00$00$01";
	
	//Application type for Watteco analog input cluster
	public static final int APPLICATION_TYPE_CO2  		   			   = 327680;
	
	
	/* ***********************************************************************
	 * 						  APAM IMPLEMENTATION                            *
	 *********************************************************************** */
	
	public static final String SMART_PLUG_IMPL  				   		= "WattecoSmartPlugImpl";
	public static final String OCCUPANCY_IMPL  				   		    = "WattecoOccupancyImpl";
	public static final String TEMPERATURE_IMPL  				   		= "WattecoTemperatureImpl";
	public static final String CO2_IMPL  				   		    	= "WattecoCO2Impl";
}
