package appsgate.lig.watteco.adapter.services;

/**
 * This interface describe Watteco I/O IPojo service API
 * 
 * @author Cédric Gérard
 * @since August 13, 2013
 * @version 1.0.0
 * 
 */
public interface WattecoIOService {

	/**
	 * Sends the given command to the sensor corresponding to the given IPv6 
	 * address, and retrieves the returned value, if any.
	 * 
	 * @param addr the IPv6 address of the sensor
	 * @param cmd the command to be sent to the sensor
	 * @param resp if the border router have to wait for response
	 * @return an array of bytes corresponding to the hexadecimal value returned
	 * 		by the sensor, if any
	 */
	public byte[] sendCommand(String addr, String cmd, boolean resp);

}
