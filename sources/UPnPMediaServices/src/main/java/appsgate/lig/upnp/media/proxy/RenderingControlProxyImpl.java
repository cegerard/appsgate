
/*
__BANNER__
*/
// this file was generated at 22-March-2013 11:22 AM by ${author}
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


public class RenderingControlProxyImpl implements AbstractObjectSpec, RenderingControl, UPnPEventListener {		

	private int			locationId;
	private String 		pictureId;
	
	private String 		serviceType ;
	private String 		serviceId ;

	private UPnPDevice  upnpDevice;
	private UPnPService upnpService;
	
	RenderingControlProxyImpl(){
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
	
	
	private java.lang.String lastChange;
	
	@Override
	public java.lang.String getLastChange() {
		return lastChange;
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
		description.put("type", getUserType()); 
		description.put("locationId", getLocationId());
		description.put("status", getObjectStatus());
		
		
		description.put("lastChange", getLastChange());
		
		
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
	
	
		if (variable.equals("LastChange"))
			lastChange = (java.lang.String) value;
	
		
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
			return RenderingControlProxyImpl.this;
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
 * instanceID in  parameter

 * currentPresetNameList out  parameter


	 */
	public void listPresets(
		long instanceID,

StringHolder currentPresetNameList
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("listPresets");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentPresetNameList.setValue(StringHolder.toValue((java.lang.String)_result.get("CurrentPresetNameList")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * presetName in  parameter


	 */
	public void selectPreset(
		long instanceID,

java.lang.String presetName
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("selectPreset");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("PresetName", 
						(new StringHolder(presetName)).getObject());
		

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
 * instanceID in  parameter

 * currentBrightness out  parameter


	 */
	public void getBrightness(
		long instanceID,

IntegerHolder currentBrightness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getBrightness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentBrightness.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentBrightness")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBrightness in  parameter


	 */
	public void setBrightness(
		long instanceID,

int desiredBrightness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setBrightness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredBrightness", 
						(new IntegerHolder(desiredBrightness)).getObject());
		

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
 * instanceID in  parameter

 * currentContrast out  parameter


	 */
	public void getContrast(
		long instanceID,

IntegerHolder currentContrast
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getContrast");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentContrast.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentContrast")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredContrast in  parameter


	 */
	public void setContrast(
		long instanceID,

int desiredContrast
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setContrast");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredContrast", 
						(new IntegerHolder(desiredContrast)).getObject());
		

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
 * instanceID in  parameter

 * currentSharpness out  parameter


	 */
	public void getSharpness(
		long instanceID,

IntegerHolder currentSharpness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getSharpness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentSharpness.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentSharpness")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredSharpness in  parameter


	 */
	public void setSharpness(
		long instanceID,

int desiredSharpness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setSharpness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredSharpness", 
						(new IntegerHolder(desiredSharpness)).getObject());
		

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
 * instanceID in  parameter

 * currentRedVideoGain out  parameter


	 */
	public void getRedVideoGain(
		long instanceID,

IntegerHolder currentRedVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getRedVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentRedVideoGain.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentRedVideoGain")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredRedVideoGain in  parameter


	 */
	public void setRedVideoGain(
		long instanceID,

int desiredRedVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setRedVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredRedVideoGain", 
						(new IntegerHolder(desiredRedVideoGain)).getObject());
		

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
 * instanceID in  parameter

 * currentGreenVideoGain out  parameter


	 */
	public void getGreenVideoGain(
		long instanceID,

IntegerHolder currentGreenVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getGreenVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentGreenVideoGain.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentGreenVideoGain")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredGreenVideoGain in  parameter


	 */
	public void setGreenVideoGain(
		long instanceID,

int desiredGreenVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setGreenVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredGreenVideoGain", 
						(new IntegerHolder(desiredGreenVideoGain)).getObject());
		

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
 * instanceID in  parameter

 * currentBlueVideoGain out  parameter


	 */
	public void getBlueVideoGain(
		long instanceID,

IntegerHolder currentBlueVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getBlueVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentBlueVideoGain.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentBlueVideoGain")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBlueVideoGain in  parameter


	 */
	public void setBlueVideoGain(
		long instanceID,

int desiredBlueVideoGain
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setBlueVideoGain");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredBlueVideoGain", 
						(new IntegerHolder(desiredBlueVideoGain)).getObject());
		

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
 * instanceID in  parameter

 * currentRedVideoBlackLevel out  parameter


	 */
	public void getRedVideoBlackLevel(
		long instanceID,

IntegerHolder currentRedVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getRedVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentRedVideoBlackLevel.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentRedVideoBlackLevel")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredRedVideoBlackLevel in  parameter


	 */
	public void setRedVideoBlackLevel(
		long instanceID,

int desiredRedVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setRedVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredRedVideoBlackLevel", 
						(new IntegerHolder(desiredRedVideoBlackLevel)).getObject());
		

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
 * instanceID in  parameter

 * currentGreenVideoBlackLevel out  parameter


	 */
	public void getGreenVideoBlackLevel(
		long instanceID,

IntegerHolder currentGreenVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getGreenVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentGreenVideoBlackLevel.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentGreenVideoBlackLevel")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredGreenVideoBlackLevel in  parameter


	 */
	public void setGreenVideoBlackLevel(
		long instanceID,

int desiredGreenVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setGreenVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredGreenVideoBlackLevel", 
						(new IntegerHolder(desiredGreenVideoBlackLevel)).getObject());
		

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
 * instanceID in  parameter

 * currentBlueVideoBlackLevel out  parameter


	 */
	public void getBlueVideoBlackLevel(
		long instanceID,

IntegerHolder currentBlueVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getBlueVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentBlueVideoBlackLevel.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentBlueVideoBlackLevel")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBlueVideoBlackLevel in  parameter


	 */
	public void setBlueVideoBlackLevel(
		long instanceID,

int desiredBlueVideoBlackLevel
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setBlueVideoBlackLevel");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredBlueVideoBlackLevel", 
						(new IntegerHolder(desiredBlueVideoBlackLevel)).getObject());
		

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
 * instanceID in  parameter

 * currentColorTemperature out  parameter


	 */
	public void getColorTemperature(
		long instanceID,

IntegerHolder currentColorTemperature
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getColorTemperature");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentColorTemperature.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentColorTemperature")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredColorTemperature in  parameter


	 */
	public void setColorTemperature(
		long instanceID,

int desiredColorTemperature
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setColorTemperature");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredColorTemperature", 
						(new IntegerHolder(desiredColorTemperature)).getObject());
		

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
 * instanceID in  parameter

 * currentHorizontalKeystone out  parameter


	 */
	public void getHorizontalKeystone(
		long instanceID,

IntegerHolder currentHorizontalKeystone
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getHorizontalKeystone");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentHorizontalKeystone.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentHorizontalKeystone")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredHorizontalKeystone in  parameter


	 */
	public void setHorizontalKeystone(
		long instanceID,

int desiredHorizontalKeystone
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setHorizontalKeystone");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredHorizontalKeystone", 
						(new IntegerHolder(desiredHorizontalKeystone)).getObject());
		

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
 * instanceID in  parameter

 * currentVerticalKeystone out  parameter


	 */
	public void getVerticalKeystone(
		long instanceID,

IntegerHolder currentVerticalKeystone
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getVerticalKeystone");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentVerticalKeystone.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentVerticalKeystone")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredVerticalKeystone in  parameter


	 */
	public void setVerticalKeystone(
		long instanceID,

int desiredVerticalKeystone
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setVerticalKeystone");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("DesiredVerticalKeystone", 
						(new IntegerHolder(desiredVerticalKeystone)).getObject());
		

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
 * instanceID in  parameter

 * channel in  parameter

 * currentMute out  parameter


	 */
	public void getMute(
		long instanceID,

java.lang.String channel,

BooleanHolder currentMute
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getMute");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentMute.setValue(BooleanHolder.toValue((java.lang.Boolean)_result.get("CurrentMute")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredMute in  parameter


	 */
	public void setMute(
		long instanceID,

java.lang.String channel,

boolean desiredMute
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setMute");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		
		_parameters.put("DesiredMute", 
						(new BooleanHolder(desiredMute)).getObject());
		

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
 * instanceID in  parameter

 * channel in  parameter

 * currentVolume out  parameter


	 */
	public void getVolume(
		long instanceID,

java.lang.String channel,

IntegerHolder currentVolume
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getVolume");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentVolume.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentVolume")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredVolume in  parameter


	 */
	public void setVolume(
		long instanceID,

java.lang.String channel,

int desiredVolume
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setVolume");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		
		_parameters.put("DesiredVolume", 
						(new IntegerHolder(desiredVolume)).getObject());
		

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
 * instanceID in  parameter

 * channel in  parameter

 * currentVolume out  parameter


	 */
	public void getVolumeDB(
		long instanceID,

java.lang.String channel,

IntegerHolder currentVolume
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getVolumeDB");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentVolume.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("CurrentVolume")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredVolume in  parameter


	 */
	public void setVolumeDB(
		long instanceID,

java.lang.String channel,

int desiredVolume
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setVolumeDB");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		
		_parameters.put("DesiredVolume", 
						(new IntegerHolder(desiredVolume)).getObject());
		

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
 * instanceID in  parameter

 * channel in  parameter

 * minValue out  parameter

 * maxValue out  parameter


	 */
	public void getVolumeDBRange(
		long instanceID,

java.lang.String channel,

IntegerHolder minValue,

IntegerHolder maxValue
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getVolumeDBRange");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		minValue.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("MinValue")));
		maxValue.setValue(IntegerHolder.toValue((java.lang.Integer)_result.get("MaxValue")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * currentLoudness out  parameter


	 */
	public void getLoudness(
		long instanceID,

java.lang.String channel,

BooleanHolder currentLoudness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("getLoudness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		currentLoudness.setValue(BooleanHolder.toValue((java.lang.Boolean)_result.get("CurrentLoudness")));
		
	}


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredLoudness in  parameter


	 */
	public void setLoudness(
		long instanceID,

java.lang.String channel,

boolean desiredLoudness
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("setLoudness");

		Dictionary _parameters = new Hashtable();
		
		
		_parameters.put("InstanceID", 
						(new LongHolder(instanceID)).getObject());
		
		_parameters.put("Channel", 
						(new StringHolder(channel)).getObject());
		
		_parameters.put("DesiredLoudness", 
						(new BooleanHolder(desiredLoudness)).getObject());
		

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		
	}

	

	
}
