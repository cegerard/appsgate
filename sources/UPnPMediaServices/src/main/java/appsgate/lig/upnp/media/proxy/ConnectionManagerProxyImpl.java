
/*
__BANNER__
*/
// this file was generated at 3-June-2013 12:55 PM by ${author}
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

import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;


public class ConnectionManagerProxyImpl implements AbstractObjectSpec, ConnectionManager, UPnPEventListener {		

	private String 		userObjectName;
	private int			locationId;
	private String 		pictureId;
	
	private String 		serviceType ;
	private String 		serviceId ;

	private UPnPDevice  upnpDevice;
	private UPnPService upnpService;
	
	ConnectionManagerProxyImpl(){
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
	
	
	private java.lang.String sourceProtocolInfo;
	
	@Override
	public java.lang.String getSourceProtocolInfo() {
		return sourceProtocolInfo;
	}
	
	
	private java.lang.String sinkProtocolInfo;
	
	@Override
	public java.lang.String getSinkProtocolInfo() {
		return sinkProtocolInfo;
	}
	
	
	private java.lang.String currentConnectionIDs;
	
	@Override
	public java.lang.String getCurrentConnectionIDs() {
		return currentConnectionIDs;
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
		
		
		description.put("sourceProtocolInfo", getSourceProtocolInfo());
	
		description.put("sinkProtocolInfo", getSinkProtocolInfo());
	
		description.put("currentConnectionIDs", getCurrentConnectionIDs());
		
		
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
	
	
		if (variable.equals("SourceProtocolInfo"))
			sourceProtocolInfo = (java.lang.String) value;
	
	
		if (variable.equals("SinkProtocolInfo"))
			sinkProtocolInfo = (java.lang.String) value;
	
	
		if (variable.equals("CurrentConnectionIDs"))
			currentConnectionIDs = (java.lang.String) value;
	
		
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
		public AbstractObjectSpec getSource() {
			return ConnectionManagerProxyImpl.this;
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
 * source out  parameter

 * sink out  parameter


	 */
	public void getProtocolInfo(
		StringHolder source,

StringHolder sink
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetProtocolInfo");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetProtocolInfo"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		source.setValue(StringHolder.toValue((java.lang.String)_result.get("Source")));
		sink.setValue(StringHolder.toValue((java.lang.String)_result.get("Sink")));
		
	}


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
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("PrepareForConnection");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"PrepareForConnection"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("RemoteProtocolInfo", 
						(new StringHolder(remoteProtocolInfo)).getObject());
		
		_parameters.put("PeerConnectionManager", 
						(new StringHolder(peerConnectionManager)).getObject());
		
		_parameters.put("PeerConnectionID", 
						(new IntegerHolder(peerConnectionID)).getObject());
		
		_parameters.put("Direction", 
						(new StringHolder(direction)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		connectionID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("ConnectionID")));
		aVTransportID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("AVTransportID")));
		rcsID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("RcsID")));
		
	}


	/**
	 * This method is "add description here"	
 * connectionID in  parameter


	 */
	public void connectionComplete(
		int connectionID
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("ConnectionComplete");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"ConnectionComplete"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ConnectionID", 
						(new IntegerHolder(connectionID)).getObject());
		

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
 * connectionIDs out  parameter


	 */
	public void getCurrentConnectionIDs(
		StringHolder connectionIDs
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetCurrentConnectionIDs");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetCurrentConnectionIDs"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		connectionIDs.setValue(StringHolder.toValue((java.lang.String)_result.get("ConnectionIDs")));
		
	}


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
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("GetCurrentConnectionInfo");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"GetCurrentConnectionInfo"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("ConnectionID", 
						(new IntegerHolder(connectionID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		rcsID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("RcsID")));
		aVTransportID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("AVTransportID")));
		protocolInfo.setValue(StringHolder.toValue((java.lang.String)_result.get("ProtocolInfo")));
		peerConnectionManager.setValue(StringHolder.toValue((java.lang.String)_result.get("PeerConnectionManager")));
		peerConnectionID.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("PeerConnectionID")));
		direction.setValue(StringHolder.toValue((java.lang.String)_result.get("Direction")));
		status.setValue(StringHolder.toValue((java.lang.String)_result.get("Status")));
		
	}

	

	
}
