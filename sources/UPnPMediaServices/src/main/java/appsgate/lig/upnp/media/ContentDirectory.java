
/*
__BANNER__
*/
// this file was generated at 22-March-2013 11:22 AM by ${author}
package appsgate.lig.upnp.media;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPException;

	
public interface ContentDirectory {	

	
	public java.lang.String getTransferIDs();
	
	public long getSystemUpdateID();
	
	public java.lang.String getContainerUpdateIDs();
	
	/**
	 * This method is "add description here"	
 * searchCaps out  parameter


	 */
	public void getSearchCapabilities(
		StringHolder searchCaps
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * sortCaps out  parameter


	 */
	public void getSortCapabilities(
		StringHolder sortCaps
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * id out  parameter


	 */
	public void getSystemUpdateID(
		LongHolder id
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * objectID in  parameter

 * browseFlag in  parameter

 * filter in  parameter

 * startingIndex in  parameter

 * requestedCount in  parameter

 * sortCriteria in  parameter

 * result out  parameter

 * numberReturned out  parameter

 * totalMatches out  parameter

 * updateID out  parameter


	 */
	public void browse(
		java.lang.String objectID,

java.lang.String browseFlag,

java.lang.String filter,

long startingIndex,

long requestedCount,

java.lang.String sortCriteria,

StringHolder result,

LongHolder numberReturned,

LongHolder totalMatches,

LongHolder updateID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * containerID in  parameter

 * searchCriteria in  parameter

 * filter in  parameter

 * startingIndex in  parameter

 * requestedCount in  parameter

 * sortCriteria in  parameter

 * result out  parameter

 * numberReturned out  parameter

 * totalMatches out  parameter

 * updateID out  parameter


	 */
	public void search(
		java.lang.String containerID,

java.lang.String searchCriteria,

java.lang.String filter,

long startingIndex,

long requestedCount,

java.lang.String sortCriteria,

StringHolder result,

LongHolder numberReturned,

LongHolder totalMatches,

LongHolder updateID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * containerID in  parameter

 * elements in  parameter

 * objectID out  parameter

 * result out  parameter


	 */
	public void createObject(
		java.lang.String containerID,

java.lang.String elements,

StringHolder objectID,

StringHolder result
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * objectID in  parameter


	 */
	public void destroyObject(
		java.lang.String objectID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * objectID in  parameter

 * currentTagValue in  parameter

 * newTagValue in  parameter


	 */
	public void updateObject(
		java.lang.String objectID,

java.lang.String currentTagValue,

java.lang.String newTagValue
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * sourceURI in  parameter

 * destinationURI in  parameter

 * transferID out  parameter


	 */
	public void importResource(
		java.lang.String sourceURI,

java.lang.String destinationURI,

LongHolder transferID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * sourceURI in  parameter

 * destinationURI in  parameter

 * transferID out  parameter


	 */
	public void exportResource(
		java.lang.String sourceURI,

java.lang.String destinationURI,

LongHolder transferID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * transferID in  parameter


	 */
	public void stopTransferResource(
		long transferID
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * transferID in  parameter

 * transferStatus out  parameter

 * transferLength out  parameter

 * transferTotal out  parameter


	 */
	public void getTransferProgress(
		long transferID,

StringHolder transferStatus,

StringHolder transferLength,

StringHolder transferTotal
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * resourceURI in  parameter


	 */
	public void deleteResource(
		java.lang.String resourceURI
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * containerID in  parameter

 * objectID in  parameter

 * newID out  parameter


	 */
	public void createReference(
		java.lang.String containerID,

java.lang.String objectID,

StringHolder newID
	) throws UPnPException;
	// TODO return the type of the return argument when specified

	

}
