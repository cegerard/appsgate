package watteco.sensors;

import java.net.InetAddress;

/**
 * Abstract class representing any sensor that may be used with a watteco 
 * border router.
 * 
 * @author thalgott
 */
public abstract class Sensor {
	
	/**
	 * Returns the address ipv6 address associated with the sensor.
	 * 
	 * @return the address of the sensor
	 */
	public abstract InetAddress getAddress();
}
