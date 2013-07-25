
/*
__BANNER__
*/
// this file was generated at 18-July-2013 04:15 PM by ${author}
package appsgate.lig.upnp.media.proxy;

import org.apache.felix.upnp.devicegen.holder.*;
import appsgate.lig.upnp.media.*;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import org.osgi.framework.Filter;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;

import fr.imag.adele.apam.Instance;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;


public class ContentDirectoryProxyImpl implements CoreObjectSpec, ContentDirectory, UPnPEventListener {		

	private String 		userObjectName;
	private int			locationId;
	private String 		pictureId;
	
	private String 		serviceType ;
	private String 		serviceId ;

	private UPnPDevice  upnpDevice;
	private UPnPService upnpService;
	
	ContentDirectoryProxyImpl(){
		super();
	}
	
	@SuppressWarnings("unused")
	private void initialize(Instance instance) {
		serviceType = instance.getProperty(UPnPService.TYPE);
		serviceId 	= instance.getProperty(UPnPService.ID);
		upnpService	= upnpDevice.getService(serviceId);
	}
	
	public String getAbstractObjectId() {
		return upnpDevice.getDescriptions(null).get(UPnPDevice.ID)+"/"+serviceId;
	}

	public String getUserType() {
		// TODO Generate unique type identifier !!!
		return Integer.toString(serviceType.hashCode());
	}

	public int getObjectStatus() {
		return 2;
	}
	
	
	private java.lang.String transferIDs;
	
	@Override
	public java.lang.String getTransferIDs() {
		return transferIDs;
	}
	
	
	private long systemUpdateID;
	
	@Override
	public long getSystemUpdateID() {
		return systemUpdateID;
	}
	
	
	private java.lang.String containerUpdateIDs;
	
	@Override
	public java.lang.String getContainerUpdateIDs() {
		return containerUpdateIDs;
	}
	
		
	public String getUserObjectName() {
		return userObjectName;
	}

	public void setUserObjectName(String userName) {
		this.userObjectName = userName;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}
	
	public String getPictureId() {
		return pictureId;
	}

	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
	}

	public JSONObject getDescription() throws JSONException {
		JSONObject description = new JSONObject();
		
		description.put("id", getAbstractObjectId());
		description.put("physical-device", upnpDevice.getDescriptions(null).get(UPnPDevice.ID));
		description.put("name", getUserObjectName());
		description.put("type", getUserType()); 
		description.put("locationId", getLocationId());
		description.put("status", getObjectStatus());
		
		
		description.put("transferIDs", getTransferIDs());
	
		description.put("systemUpdateID", getSystemUpdateID());
	
		description.put("containerUpdateIDs", getContainerUpdateIDs());
		
		
		return description;
	}

	@SuppressWarnings("unused")
	private Filter upnpEventFilter;
	
	@Override
	@SuppressWarnings("unchecked")
	public void notifyUPnPEvent(String deviceId, String serviceId,	@SuppressWarnings("rawtypes") Dictionary events) {

		Enumeration<String> variables = events.keys();
		while( variables.hasMoreElements()) {

			String variable = variables.nextElement();
			Object value	= events.get(variable);
			
			stateChanged(variable,value);
		}
	}		

	private NotificationMsg stateChanged(String variable, Object value) {
	
	
		if (variable.equals("TransferIDs"))
			transferIDs = (java.lang.String) value;
	
	
		if (variable.equals("SystemUpdateID"))
			systemUpdateID = (java.lang.Long) value;
	
	
		if (variable.equals("ContainerUpdateIDs"))
			containerUpdateIDs = (java.lang.String) value;
	
		
		return new Notification(variable, value.toString());
	}

	private  class Notification implements NotificationMsg {

		private final String variable;
		private final String value;
		
		public Notification( String variable, String value) {
			this.variable 	= variable;
			this.value		= value;
		}
		
		@Override
		public CoreObjectSpec getSource() {
			return ContentDirectoryProxyImpl.this;
		}

		@Override
		public String getNewValue() {
			return value;
		}

		@Override
		public JSONObject JSONize() throws JSONException {
			
			JSONObject notification = new JSONObject();
			
			notification.put("objectId", getAbstractObjectId());
			notification.put("varName", variable);
			notification.put("value", value);
		
			return notification;
		}	
	
	}

	
	
	/**
	 * This method is "add description here"	
 * searchCaps out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void getSearchCapabilities(
		StringHolder searchCaps
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetSearchCapabilities");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetSearchCapabilities"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		searchCaps.setValue(StringHolder.toValue((java.lang.String)_result.get("SearchCaps")));
		
	}


	/**
	 * This method is "add description here"	
 * sortCaps out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void getSortCapabilities(
		StringHolder sortCaps
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetSortCapabilities");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetSortCapabilities"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		sortCaps.setValue(StringHolder.toValue((java.lang.String)_result.get("SortCaps")));
		
	}


	/**
	 * This method is "add description here"	
 * id out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void getSystemUpdateID(
		LongHolder id
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetSystemUpdateID");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetSystemUpdateID"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		id.setValue(LongHolder.toValue((java.lang.Long)_result.get("Id")));
		
	}


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
	@SuppressWarnings("rawtypes")
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
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("Browse");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"Browse"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ObjectID", 
						(new StringHolder(objectID)).getObject());
		
		_parameters.put("BrowseFlag", 
						(new StringHolder(browseFlag)).getObject());
		
		_parameters.put("Filter", 
						(new StringHolder(filter)).getObject());
		
		_parameters.put("StartingIndex", 
						(new LongHolder(startingIndex)).getObject());
		
		_parameters.put("RequestedCount", 
						(new LongHolder(requestedCount)).getObject());
		
		_parameters.put("SortCriteria", 
						(new StringHolder(sortCriteria)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		result.setValue(StringHolder.toValue((java.lang.String)_result.get("Result")));
		numberReturned.setValue(LongHolder.toValue((java.lang.Long)_result.get("NumberReturned")));
		totalMatches.setValue(LongHolder.toValue((java.lang.Long)_result.get("TotalMatches")));
		updateID.setValue(LongHolder.toValue((java.lang.Long)_result.get("UpdateID")));
		
	}


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
	@SuppressWarnings("rawtypes")
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
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("Search");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"Search"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ContainerID", 
						(new StringHolder(containerID)).getObject());
		
		_parameters.put("SearchCriteria", 
						(new StringHolder(searchCriteria)).getObject());
		
		_parameters.put("Filter", 
						(new StringHolder(filter)).getObject());
		
		_parameters.put("StartingIndex", 
						(new LongHolder(startingIndex)).getObject());
		
		_parameters.put("RequestedCount", 
						(new LongHolder(requestedCount)).getObject());
		
		_parameters.put("SortCriteria", 
						(new StringHolder(sortCriteria)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		result.setValue(StringHolder.toValue((java.lang.String)_result.get("Result")));
		numberReturned.setValue(LongHolder.toValue((java.lang.Long)_result.get("NumberReturned")));
		totalMatches.setValue(LongHolder.toValue((java.lang.Long)_result.get("TotalMatches")));
		updateID.setValue(LongHolder.toValue((java.lang.Long)_result.get("UpdateID")));
		
	}


	/**
	 * This method is "add description here"	
 * containerID in  parameter

 * elements in  parameter

 * objectID out  parameter

 * result out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void createObject(
		java.lang.String containerID,

java.lang.String elements,

StringHolder objectID,

StringHolder result
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("CreateObject");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"CreateObject"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ContainerID", 
						(new StringHolder(containerID)).getObject());
		
		_parameters.put("Elements", 
						(new StringHolder(elements)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		objectID.setValue(StringHolder.toValue((java.lang.String)_result.get("ObjectID")));
		result.setValue(StringHolder.toValue((java.lang.String)_result.get("Result")));
		
	}


	/**
	 * This method is "add description here"	
 * objectID in  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void destroyObject(
		java.lang.String objectID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("DestroyObject");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"DestroyObject"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ObjectID", 
						(new StringHolder(objectID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		
	}


	/**
	 * This method is "add description here"	
 * objectID in  parameter

 * currentTagValue in  parameter

 * newTagValue in  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void updateObject(
		java.lang.String objectID,

java.lang.String currentTagValue,

java.lang.String newTagValue
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("UpdateObject");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"UpdateObject"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ObjectID", 
						(new StringHolder(objectID)).getObject());
		
		_parameters.put("CurrentTagValue", 
						(new StringHolder(currentTagValue)).getObject());
		
		_parameters.put("NewTagValue", 
						(new StringHolder(newTagValue)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		
	}


	/**
	 * This method is "add description here"	
 * sourceURI in  parameter

 * destinationURI in  parameter

 * transferID out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void importResource(
		java.lang.String sourceURI,

java.lang.String destinationURI,

LongHolder transferID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("ImportResource");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"ImportResource"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("SourceURI", 
						(new StringHolder(sourceURI)).getObject());
		
		_parameters.put("DestinationURI", 
						(new StringHolder(destinationURI)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		transferID.setValue(LongHolder.toValue((java.lang.Long)_result.get("TransferID")));
		
	}


	/**
	 * This method is "add description here"	
 * sourceURI in  parameter

 * destinationURI in  parameter

 * transferID out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void exportResource(
		java.lang.String sourceURI,

java.lang.String destinationURI,

LongHolder transferID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("ExportResource");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"ExportResource"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("SourceURI", 
						(new StringHolder(sourceURI)).getObject());
		
		_parameters.put("DestinationURI", 
						(new StringHolder(destinationURI)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		transferID.setValue(LongHolder.toValue((java.lang.Long)_result.get("TransferID")));
		
	}


	/**
	 * This method is "add description here"	
 * transferID in  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void stopTransferResource(
		long transferID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("StopTransferResource");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"StopTransferResource"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("TransferID", 
						(new LongHolder(transferID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		
	}


	/**
	 * This method is "add description here"	
 * transferID in  parameter

 * transferStatus out  parameter

 * transferLength out  parameter

 * transferTotal out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void getTransferProgress(
		long transferID,

StringHolder transferStatus,

StringHolder transferLength,

StringHolder transferTotal
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetTransferProgress");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetTransferProgress"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("TransferID", 
						(new LongHolder(transferID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		transferStatus.setValue(StringHolder.toValue((java.lang.String)_result.get("TransferStatus")));
		transferLength.setValue(StringHolder.toValue((java.lang.String)_result.get("TransferLength")));
		transferTotal.setValue(StringHolder.toValue((java.lang.String)_result.get("TransferTotal")));
		
	}


	/**
	 * This method is "add description here"	
 * resourceURI in  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void deleteResource(
		java.lang.String resourceURI
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("DeleteResource");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"DeleteResource"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ResourceURI", 
						(new StringHolder(resourceURI)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		
	}


	/**
	 * This method is "add description here"	
 * containerID in  parameter

 * objectID in  parameter

 * newID out  parameter


	 */
	@SuppressWarnings("rawtypes")
	public void createReference(
		java.lang.String containerID,

java.lang.String objectID,

StringHolder newID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("CreateReference");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"CreateReference"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ContainerID", 
						(new StringHolder(containerID)).getObject());
		
		_parameters.put("ObjectID", 
						(new StringHolder(objectID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		newID.setValue(StringHolder.toValue((java.lang.String)_result.get("NewID")));
		
	}

	

	
}
