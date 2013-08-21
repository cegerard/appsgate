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
	 * @param borderRouterAddress the Watteco border router address
	 */
	public ArrayList<String> discover(String borderRouterAddress);

}
