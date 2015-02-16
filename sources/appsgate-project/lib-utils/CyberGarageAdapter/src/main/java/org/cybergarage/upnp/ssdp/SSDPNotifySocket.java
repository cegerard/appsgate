/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: SSDPNotifySocket.java
*
*	Revision;
*
*	11/20/02
*		- first revision.
*	05/13/03
*		- Added support for IPv6.
*	02/20/04
*		- Inma Marin Lopez <inma@dif.um.es>
*		- Added a multicast filter using the SSDP pakcet.
*	04/20/05
*		- Mikael Hakman <mhakman@dkab.net>
*		- Handle receive() returning null.
*		- Added close() in stop().
*	08/23/07
*		- Thanks for Kazuyuki Shudo
* 		- Changed run() to catch IOException of HTTPMUSocket::receive().
*	01/31/08
*		- Changed start() not to abort when the interface infomation is null on Android m3-rc37a.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.*;
import java.io.IOException;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;

/**
 * 
 * This class identifies a SSDP socket only for <b>notifing packet</b>.<br>
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 *
 */
public class SSDPNotifySocket extends HTTPMUSocket implements Runnable
{
	private boolean useIPv6Address;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public SSDPNotifySocket(String bindAddr)
	{

		setControlPoint(null);
	}

	////////////////////////////////////////////////
	//	ControlPoint	
	////////////////////////////////////////////////

	private ControlPoint controlPoint = null;
	
	public void setControlPoint(ControlPoint ctrlp)
	{
		this.controlPoint = ctrlp;
	}

	public ControlPoint getControlPoint()
	{
		return controlPoint;
	}

	/**
	 * This method send a {@link SSDPNotifyRequest} over {@link SSDPNotifySocket}
	 * 
	 * @param req the {@link SSDPNotifyRequest} to send
	 * @return true if and only if the trasmission succeced<br>
	 * 	Because it rely on UDP doesn't mean that it's also recieved
	 */
	public boolean post(SSDPNotifyRequest req)
	{

		return false;
		}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread deviceNotifyThread = null;
		
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		
		ControlPoint ctrlPoint = getControlPoint();
		
		while (deviceNotifyThread == thisThread) {
			Thread.yield();


		}
	}
	
	public void start(){

	}
	
	public void stop()
	{

	}
}

