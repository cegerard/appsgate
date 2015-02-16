/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPMU.java
*
*	Revision;
*
*	11/20/02
*		- first revision.
*	12/12/03
*		- Inma Mar?n <inma@DIF.UM.ES>
*		- Changed open(addr, port) to send IPv6 SSDP packets.
*		- The socket binds only the port without the interface address.
*		- The full binding socket can send SSDP IPv4 packets. Is it a bug of J2SE v.1.4.2-b28 ?.
*	01/06/04
*		- Oliver Newell <olivern@users.sourceforge.net>
*		- Added to set a current timestamp when the packet are received.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.cybergarage.util.Debug;

public class HTTPUSocket
{
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private DatagramSocket ssdpUniSock = null;
	//private MulticastSocket ssdpUniSock = null;

	public DatagramSocket getDatagramSocket()
	{
		return ssdpUniSock;
	}
		
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPUSocket()
	{
		open();
	}
	
	public HTTPUSocket(String bindAddr, int bindPort)
	{
		open(bindAddr, bindPort);
	}

	public HTTPUSocket(int bindPort)
	{
		open(bindPort);
	}

	protected void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	bindAddr
	////////////////////////////////////////////////

	private String localAddr = "";

	public void setLocalAddress(String addr)
	{
		localAddr = addr;
	}

	/**
	 * 
	 * @return {@link DatagramSocket} open for receieving packets
	 * @since 1.8
	 */
	public DatagramSocket getUDPSocket(){
		return ssdpUniSock;
	}	
	
	public String getLocalAddress()
	{

			return localAddr;
	}

	////////////////////////////////////////////////
	//	open
	////////////////////////////////////////////////
	
	public boolean open()
	{

		return true;
	}
	
	public boolean open(String bindAddr, int bindPort)
	{


		
		return true;
	}

	public boolean open(int bindPort)
	{

		
		return true;
	}
		
	////////////////////////////////////////////////
	//	close
	////////////////////////////////////////////////

	public boolean close()
	{

		return true;
	}

	////////////////////////////////////////////////
	//	send
	////////////////////////////////////////////////

	public boolean post(String addr, int port, String msg)
	{

		return true;
	}

	////////////////////////////////////////////////
	//	reveive
	////////////////////////////////////////////////

	public SSDPPacket receive()
	{
 		return null;
	}

	////////////////////////////////////////////////
	//	join/leave
	////////////////////////////////////////////////

/*
	boolean joinGroup(String mcastAddr, int mcastPort, String bindAddr)
	{
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.joinGroup(mcastGroup, mcastIf);
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}

	boolean leaveGroup(String mcastAddr, int mcastPort, String bindAddr)
	 {
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.leaveGroup(mcastGroup, mcastIf);
		 }
		 catch (Exception e) {
			 Debug.warning(e);
			 return false;
		 }
		 return true;
	 }
*/
}

