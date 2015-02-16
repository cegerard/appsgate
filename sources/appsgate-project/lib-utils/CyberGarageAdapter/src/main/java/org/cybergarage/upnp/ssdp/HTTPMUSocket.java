/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: HTTPMU.java
*
*	Revision;
*
*	11/18/02
*		- first revision.
*	09/03/03
*		- Changed to open the socket using setReuseAddress().
*	12/10/03
*		- Fixed getLocalAddress() to return a valid interface address.
*	02/28/04
*		- Added getMulticastInetAddress(), getMulticastAddress().
*	11/19/04
*		- Theo Beisch <theo.beisch@gmx.de>
*		- Changed send() to set the TTL as 4.
*	08/23/07
*		- Thanks for Kazuyuki Shudo
*		- Changed receive() to throw IOException.
*	01/10/08
*		- Changed getLocalAddress() to return a brank string when the ssdpMultiGroup or ssdpMultiIf is null on Android m3-rc37a.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.io.IOException;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.util.Debug;

// Dummy Class for Android m3-rc37a
// import org.cybergarage.android.MulticastSocket;

public class HTTPMUSocket
{
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private InetSocketAddress ssdpMultiGroup = null;
	private MulticastSocket ssdpMultiSock = null;
	private NetworkInterface ssdpMultiIf = null;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public HTTPMUSocket()
	{
	}
	
	public HTTPMUSocket(String addr, int port, String bindAddr)
	{
		open(addr, port, bindAddr);
	}

	protected void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	bindAddr
	////////////////////////////////////////////////

	public String getLocalAddress()
	{

		return "";
	}

	/**
	 * 
	 * @return the destination port for multicast packet
	 * @since 1.8
	 */
	public int getMulticastPort(){
		return ssdpMultiGroup.getPort();
	}
	
	/**
	 * 
	 * @return the source port for multicast packet
	 * @since 1.8
	 */
	public int getLocalPort(){
		return ssdpMultiSock.getLocalPort();
	}
	
	/**
	 * 
	 * @return the opened {@link MulticastSocket}
	 * @since 1.8 
	 */
	public MulticastSocket getSocket(){
		return ssdpMultiSock;
	}
	
	
	////////////////////////////////////////////////
	//	MulticastAddr
	////////////////////////////////////////////////
	
	public InetAddress getMulticastInetAddress()
	{
		return ssdpMultiGroup.getAddress();
	}
	
	public String getMulticastAddress()
	{
		return getMulticastInetAddress().getHostAddress();
	}
	
	/**
	 * @param addr {@link String} rappresenting the multicast hostname to join into.
	 * @param port int rappresenting the port to be use poth as source and destination
	 * @param bindAddr {@link InetAddress} which identify the hostname of the interface 
	 * 		to use for sending and recieving multicast packet
	 */
	public boolean open(String addr,int port, InetAddress bindAddr){

		
		return true;		
	}
	
	public boolean open(String addr, int port, String bindAddr)
	{
		return true;		

	}

	public boolean close()
	{

		return true;
	}

	////////////////////////////////////////////////
	//	send
	////////////////////////////////////////////////

	public boolean send(String msg, String bindAddr, int bindPort)
	{

		return true;
	}

	public boolean send(String msg)
	{
		return send(msg, null, -1);
	}

	////////////////////////////////////////////////
	//	post (HTTPRequest)
	////////////////////////////////////////////////

	public boolean post(HTTPRequest req, String bindAddr, int bindPort)
	{
		return send(req.toString(), bindAddr, bindPort);
	}

	public boolean post(HTTPRequest req)
	{
		return send(req.toString(), null, -1);
	}

	////////////////////////////////////////////////
	//	reveive
	////////////////////////////////////////////////

	public SSDPPacket receive() throws IOException
	{
 		
		return null;
	}
}

