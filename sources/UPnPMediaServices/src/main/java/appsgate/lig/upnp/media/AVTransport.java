
/*
__BANNER__
*/
// this file was generated at 22-March-2013 11:22 AM by ${author}
package appsgate.lig.upnp.media;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPException;

	
public interface AVTransport {	

	
	public java.lang.String getLastChange();
	
	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentURI in  parameter

 * currentURIMetaData in  parameter


	 */
	public void setAVTransportURI(
		long instanceID,

java.lang.String currentURI,

java.lang.String currentURIMetaData
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * nextURI in  parameter

 * nextURIMetaData in  parameter


	 */
	public void setNextAVTransportURI(
		long instanceID,

java.lang.String nextURI,

java.lang.String nextURIMetaData
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * nrTracks out  parameter

 * mediaDuration out  parameter

 * currentURI out  parameter

 * currentURIMetaData out  parameter

 * nextURI out  parameter

 * nextURIMetaData out  parameter

 * playMedium out  parameter

 * recordMedium out  parameter

 * writeStatus out  parameter


	 */
	public void getMediaInfo(
		long instanceID,

LongHolder nrTracks,

StringHolder mediaDuration,

StringHolder currentURI,

StringHolder currentURIMetaData,

StringHolder nextURI,

StringHolder nextURIMetaData,

StringHolder playMedium,

StringHolder recordMedium,

StringHolder writeStatus
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentTransportState out  parameter

 * currentTransportStatus out  parameter

 * currentSpeed out  parameter


	 */
	public void getTransportInfo(
		long instanceID,

StringHolder currentTransportState,

StringHolder currentTransportStatus,

StringHolder currentSpeed
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * track out  parameter

 * trackDuration out  parameter

 * trackMetaData out  parameter

 * trackURI out  parameter

 * relTime out  parameter

 * absTime out  parameter

 * relCount out  parameter

 * absCount out  parameter


	 */
	public void getPositionInfo(
		long instanceID,

LongHolder track,

StringHolder trackDuration,

StringHolder trackMetaData,

StringHolder trackURI,

StringHolder relTime,

StringHolder absTime,

IntegerHolder relCount,

IntegerHolder absCount
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * playMedia out  parameter

 * recMedia out  parameter

 * recQualityModes out  parameter


	 */
	public void getDeviceCapabilities(
		long instanceID,

StringHolder playMedia,

StringHolder recMedia,

StringHolder recQualityModes
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * playMode out  parameter

 * recQualityMode out  parameter


	 */
	public void getTransportSettings(
		long instanceID,

StringHolder playMode,

StringHolder recQualityMode
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter


	 */
	public void stop(
		long instanceID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * speed in  parameter


	 */
	public void play(
		long instanceID,

java.lang.String speed
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter


	 */
	public void pause(
		long instanceID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter


	 */
	public void record(
		long instanceID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * unit in  parameter

 * target in  parameter


	 */
	public void seek(
		long instanceID,

java.lang.String unit,

java.lang.String target
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter


	 */
	public void next(
		long instanceID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter


	 */
	public void previous(
		long instanceID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * newPlayMode in  parameter


	 */
	public void setPlayMode(
		long instanceID,

java.lang.String newPlayMode
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * newRecordQualityMode in  parameter


	 */
	public void setRecordQualityMode(
		long instanceID,

java.lang.String newRecordQualityMode
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * actions out  parameter


	 */
	public void getCurrentTransportActions(
		long instanceID,

StringHolder actions
	) throws UPnPException;
	// TODO return the type of the return argument when specified

	

}
