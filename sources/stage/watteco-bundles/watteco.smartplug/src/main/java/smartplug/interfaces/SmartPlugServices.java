package smartplug.interfaces;

import java.net.InetAddress;

import smartplug.data.SmartPlugValue;

public interface SmartPlugServices {
	
	/**
	 * Returns the ipv6 address of this smartplug.
	 *
	 * @return the ipv6 address
	 */
	public InetAddress getAddress();
	
	/**
	 * Sets the ipv6 address of this smartplug to the given one.
	 *
	 * @param the ipv6 address as a string
	 */
	public void setAddress(String addr);

	/**
	 * Toggles the smartplug, i.e. switches its on/off state.
	 */
	public void toggle();

	/**
	 * Sends the read attribute command to the smartplug.
	 * The smartplug will send back a message containing its internal measures.
	 *
	 * @return an object containing the measures of this smartplug
	 */	
	public SmartPlugValue readAttribute();
}
