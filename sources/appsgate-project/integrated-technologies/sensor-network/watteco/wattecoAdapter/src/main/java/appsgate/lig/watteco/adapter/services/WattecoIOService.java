package appsgate.lig.watteco.adapter.services;


public interface WattecoIOService {

	/**
	 * Sends the given command to the sensor corresponding to the given IPv6 
	 * address, and retrieves the returned value, if any.
	 * 
	 * @param addr the IPv6 address of the sensor
	 * @param c the command to be sent to the sensor
	 * @return an array of bytes corresponding to the hexadecimal value returned
	 * 		by the sensor, if any
	 */
	public byte[] sendCommand(String addr, String c);

}
