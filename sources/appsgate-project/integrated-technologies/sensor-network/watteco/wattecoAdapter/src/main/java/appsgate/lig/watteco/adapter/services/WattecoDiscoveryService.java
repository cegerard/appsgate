package appsgate.lig.watteco.adapter.services;

import java.util.ArrayList;

/**
 * This interface describe Watteco descovery IPojo service API.
 * 
 * @author Cédric Gérard
 * @since August 13, 2013
 * @version 1.0.0
 * 
 */
public interface WattecoDiscoveryService {
	
	/**
	 * Get the Ipv6 address list from the Watteco border router
	 * and create an instance of each supported Watteco sensor
	 * 
	 * @param borderRouterAddress the Watteco border router IPv6 address
	 */
	public void discover(String borderRouterAddress);
	
	/**
	 * Get the Ipv6 address list from the default Watteco border router
	 * and create an instance of each supported Watteco sensor
	 */
	public void discover();
	
	/**
	 * Get the IPv6 address list from Watteco border router 
	 * @param borderRouterAddress the Watteco border router IPv6 address
	 * @return the sensor IPv6 addresses as an array list of string
	 */
	public ArrayList<String> getSensorList (String borderRouterAddress); 

}
