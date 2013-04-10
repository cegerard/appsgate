
/*
__BANNER__
*/
// this file was generated at 22-March-2013 11:22 AM by ${author}
package appsgate.lig.upnp.media;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPException;

	
public interface ConnectionManager {	

	
	public java.lang.String getSourceProtocolInfo();
	
	public java.lang.String getSinkProtocolInfo();
	
	public java.lang.String getCurrentConnectionIDs();
	
	/**
	 * This method is "add description here"	
 * source out  parameter

 * sink out  parameter


	 */
	public void getProtocolInfo(
		StringHolder source,

StringHolder sink
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * remoteProtocolInfo in  parameter

 * peerConnectionManager in  parameter

 * peerConnectionID in  parameter

 * direction in  parameter

 * connectionID out  parameter

 * aVTransportID out  parameter

 * rcsID out  parameter


	 */
	public void prepareForConnection(
		java.lang.String remoteProtocolInfo,

java.lang.String peerConnectionManager,

int peerConnectionID,

java.lang.String direction,

IntegerHolder connectionID,

IntegerHolder aVTransportID,

IntegerHolder rcsID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * connectionID in  parameter


	 */
	public void connectionComplete(
		int connectionID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * connectionIDs out  parameter


	 */
	public void getCurrentConnectionIDs(
		StringHolder connectionIDs
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * connectionID in  parameter

 * rcsID out  parameter

 * aVTransportID out  parameter

 * protocolInfo out  parameter

 * peerConnectionManager out  parameter

 * peerConnectionID out  parameter

 * direction out  parameter

 * status out  parameter


	 */
	public void getCurrentConnectionInfo(
		int connectionID,

IntegerHolder rcsID,

IntegerHolder aVTransportID,

StringHolder protocolInfo,

StringHolder peerConnectionManager,

IntegerHolder peerConnectionID,

StringHolder direction,

StringHolder status
	) throws UPnPException;
	// TODO return the type of the return argument when specified

	

}
