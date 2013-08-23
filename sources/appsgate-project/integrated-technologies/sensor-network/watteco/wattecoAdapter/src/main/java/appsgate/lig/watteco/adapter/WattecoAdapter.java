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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.watteco.adapter.services.WattecoDiscoveryService;
import appsgate.lig.watteco.adapter.services.WattecoIOService;
import appsgate.lig.watteco.adapter.services.WattecoTunSlipManagement;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

/**
 * This class is used to connect the Watteco WSN to the ApAM environment. An
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
	
	/**
	 * Executor scheduler for sensor instanciation
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
	 * Keep the Ipv6 sensor Adresses
	 */
	private HashMap<String, Instance> ipv6AddressToInstance;

	/**
	 * Default constructor
	 */
	public WattecoAdapter() {
		super();
		this.borderRouter = new BorderRouter();
		instanciationService = Executors.newScheduledThreadPool(2);
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
		// 1- Find where the EnOcean transceiver is plug (USB0 or USB1  serial port
		String osName =System.getProperty("os.name");
		if (osName.contentEquals("Linux")) {
			try {
				char port ='0';
				Process searchCMD = Runtime.getRuntime().exec("ls -l /dev");
				BufferedReader read = new BufferedReader(new InputStreamReader(searchCMD.getInputStream()));
	            try {
	            	searchCMD.waitFor();
	            } catch (InterruptedException e) {
	                logger.error(e.getMessage());
	            }
	            while (read.ready()) {
	            	String result = read.readLine();
	            	if(result.contains("tty.usbserial")) {
	            		int invalidPort = result.charAt(result.length()-1);
	            		if(invalidPort == port) {
	            			port++;
	            		}
	            		
	            	}
	            }
	          // 2- Run the tunslip configuration program to the free serial port
	          String cmd = "./conf/watteco/tunslip6 -L -v2 -s ttyUSB"+port+" aaaa::1/64 &";
	          Runtime.getRuntime().exec(cmd);
	          
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
		instanciationService.shutdown();
		try {
			instanciationService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.debug("Watteco Adapter instanciation service thread crash at termination");
		}
		//TODO delete all sensor instances
//		Implementation impl = CST.apamResolver.findImplByName(null, WattecoAdapter.SMART_PLUG_IMPL);
//		Set<Instance> insts = impl.getInsts();
//		for(Instance inst : insts) {
//			ComponentBrokerImpl.disappearedComponent(inst.getName());
//		}
		//TODO stop slip tunnel
		logger.info("Appsgate Watteco adapter stopped.");	
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
				return borderRouter.sendCommand(address, borderRouter.new BorderRouterCommand(cmd, resp));
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
		
		//If a sensor is not yet instanciate, instanciate it
		if(! ip6.isEmpty()){
			instanciationService.execute(new sensorInstanciation(ip6));
		}else {
			logger.info("No new Watteco sensor detected. All are already set up or are out of range from the border router");
		}
	}
	
	@Override
	public void discover() {
		discover("http://["+BORDER_ROUTER_ADDR+"]/");
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
		}

		public void run() {
			for (String address : addressList) {
				instanciateSensor(address);
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
	 * Instanciate a sensor from its ipv6 address
	 * @param address the ipv6 address of the sensor to instanciate
	 */
	private void instanciateSensor(String address) {
		
		CharSequence subaddress = address.subSequence(4, address.length());
		String route = "aaaa"+subaddress;
		
		//Sensor type identification
		byte[] ret = sendCommand(route, WattecoAdapter.CONFIGURATION_DESC, true);
		if(ret != null) {
			
			//Sensor type recognition
			ArrayList<String> clusterList = extractInputClusterFromConfDescription(ret);
			
			//ApAM implementation recovery
			String implName = getApAMImplFromClusterList(clusterList);
			Implementation impl = CST.apamResolver.findImplByName(null, implName);
		
			//ApAM instance initialization
			Map<String, String> initialproperties = new HashMap<String, String>();
			initialproperties.put("deviceId", address);
			initialproperties.put("networkRoute", route);
			initialproperties.put("isPaired", "true");
			initiateSensorValues(implName, route, initialproperties);
			
			//Sensor ApAM instanciation
			Instance inst = impl.createInstance(null, initialproperties);
			
			//Keep the correspondence between address and ApAm instance
			ipv6AddressToInstance.put(address, inst);
		}
	}

	/**
	 * Convert bytes array to integer
	 * @param bytes the bytes array
	 * @return the value as an integer
	 */
	private int bytesToInt(byte[] bytes) {
        int integer = 0;
        for(int i = 0; i < bytes.length; i ++) {
            integer += ((256 + bytes[i]) % 256) * (int)Math.pow(256, i);
        }
        return integer;
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
	 * @param clusterList the Watteco cluster list
	 * @return an ApAM implementation name
	 */
	private String getApAMImplFromClusterList(ArrayList<String> clusterList) {
		if(clusterList.contains(ON_OFF_CLUSTER) && clusterList.contains(SIMPLE_METERING_CLUSTER)) {
			return WattecoAdapter.SMART_PLUG_IMPL;
		} else if(clusterList.contains(OCCUPANCY_SENSING_CLUSTER)) {
			return WattecoAdapter.OCCUPANCY_IMPL;
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
			
			//Extract consumption value
			ret = sendCommand(route, WattecoAdapter.SIMPLE_METERING_READ_ATTRIBUTE, true);
			Byte b = new Byte(ret[17]);
			int cons = (b << 8);
			b = new Byte(ret[18]);
			cons += b;
			initialproperties.put("consumption", String.valueOf(cons));
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
	
	//Simple metering cluster commands
	public static final String SIMPLE_METERING_CLUSTER				   = "52";
	public static final String SIMPLE_METERING_READ_ATTRIBUTE  		   = "$11$00$00$52$00$00";
	
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
	
	
	//Analog input (basic) cluster commands
	public static final String ANALOG_INPUT_CLUSTER				   	   = "C";
	public static final String ANALOG_INPUT_READ_ATTRIBUTE  		   = "$11$00$00$0C$00$55";
	
	//Binary input (basic) cluster commands
	public static final String BINARY_INPUT_CLUSTER				   	   = "F";
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
	
	
	/* ***********************************************************************
	 * 						  APAM IMPLEMENTATION                            *
	 *********************************************************************** */
	
	public static final String SMART_PLUG_IMPL  				   		= "WattecoSmartPlugImpl";
	public static final String OCCUPANCY_IMPL  				   		    = "WattecoOccupancyImpl";
}
