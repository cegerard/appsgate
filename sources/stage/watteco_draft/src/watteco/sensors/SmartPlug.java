package watteco.sensors;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class representing a smart plug sensor.
 * 
 * @author thalgott
 */
public class SmartPlug extends Sensor {

	private InetAddress address;
	
	/**
	 * Constructs a SmartPlug using the given ipv6 address.<br>
	 * The address is calculated using the genuine SmartPlug's MAC address,
	 * such that for instance the MAC address '0200000000100294' would give the
	 * inet address 'aaaa::10:294'.<br>
	 * See the documentation for more details about address creation.
	 * 
	 * @param inetAddress the inet address of the smart plug, as a string
	 * 
	 * @throws UnknownHostException if the given address is not a proper inet
	 * 		address
	 */
	public SmartPlug(String inetAddress) throws UnknownHostException {
		this.address = InetAddress.getByName(inetAddress);
	}

	@Override
	public InetAddress getAddress() {
		return this.address;
	}
}
