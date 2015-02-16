/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HostInterface.java
*
*	Revision;
*
*	05/12/03
*		- first revision.
*	05/13/03
*		- Added support for IPv6 and loopback address.
*	02/15/04
*		- Added the following methods to set only a interface.
*		- setInterface(), getInterfaces(), hasAssignedInterface()
*	06/30/04
*		- Moved the package from org.cybergarage.http to org.cybergarage.net.
*	06/30/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Changed isUseAddress() to isUsableAddress().
*	
******************************************************************/

package org.cybergarage.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.cybergarage.util.Debug;

public class HostInterface
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public static boolean USE_LOOPBACK_ADDR = false;
	public static boolean USE_ONLY_IPV4_ADDR = false;
	public static boolean USE_ONLY_IPV6_ADDR = false;
	 
	////////////////////////////////////////////////
	//	Network Interfaces
	////////////////////////////////////////////////
	
	private static String ifAddress = "";
	public final static int IPV4_BITMASK =  0x0001;
	public final static int IPV6_BITMASK =  0x0010;
	public final static int LOCAL_BITMASK = 0x0100;

	public final static void setInterface(String ifaddr)
	{
		ifAddress = ifaddr;
	}
	
	public final static String getInterface()
	{
		return ifAddress;
	}
	
	private final static boolean hasAssignedInterface()
	{
		return (0 < ifAddress.length()) ? true : false;
	}
	
	////////////////////////////////////////////////
	//	Network Interfaces
	////////////////////////////////////////////////

	// Thanks for Theo Beisch (10/27/04)
	
	private final static boolean isUsableAddress(InetAddress addr)
	{
		if (USE_LOOPBACK_ADDR == false) {
			if (addr.isLoopbackAddress() == true)
				return false;
		}
		if (USE_ONLY_IPV4_ADDR == true) {
			if (addr instanceof Inet6Address)
				return false;
		}
		if (USE_ONLY_IPV6_ADDR == true) {
			if (addr instanceof Inet4Address)
				return false;
		}
		return true;
	}
	
	public final static int getNHostAddresses()
	{

		return 0;
	}

	/**
	 * 
	 * @param ipfilter
	 * @param interfaces
	 * @return
	 * @since 1.8.0
	 * @author Stefano "Kismet" Lenzi &lt;kismet.sl@gmail.com&gt;
	 */
	public final static InetAddress[] getInetAddress(int ipfilter,String[] interfaces){

		return new InetAddress[0];
	}
	
	
	public final static String getHostAddress(int n)
	{
		return "";
	}

	////////////////////////////////////////////////
	//	isIPv?Address
	////////////////////////////////////////////////
	
	public final static boolean isIPv6Address(String host)
	{

		return false;
	}

	public final static boolean isIPv4Address(String host)
	{

		return false;
	}

	////////////////////////////////////////////////
	//	hasIPv?Interfaces
	////////////////////////////////////////////////

	public final static boolean hasIPv4Addresses()
	{

		return false;
	}

	public final static boolean hasIPv6Addresses()
	{

		return false;
	}

	////////////////////////////////////////////////
	//	hasIPv?Interfaces
	////////////////////////////////////////////////

	public final static String getIPv4Address()
	{

		return "";
	}

	public final static String getIPv6Address()
	{
		return "";
	}

	////////////////////////////////////////////////
	//	getHostURL
	////////////////////////////////////////////////
	
	public final static String getHostURL(String host, int port, String uri)
	{
		return 
			"http://" +
			host + 
			":" + Integer.toString(port) +
			uri;
	}
	
}
