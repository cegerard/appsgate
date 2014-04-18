package appsgate.lig.domicube.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.domicube.messages.DomiCubeNotificationMsg;
import appsgate.lig.domicube.spec.CoreDomiCubeSpec;

/**
 * This is a class thaht represent the DomiCube ApAM implementation.
 * 
 * @author Cédric Gérard
 * @since April 16, 2014
 * @version 1.0.0
 * 
 * @see CoreObjectBehavior
 * @see CoreObjectSpec
 * @see CoreDomiCubeSpec
 *
 */
public class DomiCubeImpl extends CoreObjectBehavior implements CoreObjectSpec, CoreDomiCubeSpec{

	/**
	 * The system name of this device
	 */
	private String deviceName;
	
	/**
	 * The system identifier of this device
	 */
	private String deviceId;
	
	/**
	 * The system device type 
	 */
	private String deviceType;
	
	/**
	 * The icon of this device
	 */
	private String pictureId;
	
	/**
	 * The user type of this device
	 */
	private String userType;
	
	/**
	 * The current status of this device
	 */
	private String status;

	/**
	 * The current face of the DomiCube
	 */
	private String currentFace;
	
	/**
	 * The older face of the DomiCube
	 */
	private String olderFace;
	
	/**
	 * boolean to know if the device has received a MQTT message 
	 * from the real DomiCube
	 */
	private boolean detected;
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static final Logger logger = LoggerFactory.getLogger(DomiCubeImpl.class);
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		olderFace = "-1";
		logger.info("The DomiCube has been detected");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("The DomiCube desapeared");
	}
	
	/**
	 * This method uses the ApAM message model. Each call produce a
	 * DomiCubeNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new DomiCubeNotificationMsg(Integer.valueOf(currentFace), varName, value, this);
	}
	

	@Override
	public String getAbstractObjectId() {
		return deviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.valueOf(status);
	}

	@Override
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException { 
		
		JSONObject descr = new JSONObject();
		
		descr.put("id", deviceId);
		descr.put("type", userType); //210 for DomiCube
		descr.put("status", status);
		descr.put("currentFace", currentFace);
		descr.put("deviceType", deviceType);
		descr.put("systemName", deviceName);
		
		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
		notifyChanges("pictureId", pictureId);
	}
	
	/**
	 * Called by ApAM when the status value changed
	 * @param newStatus the new status value.
	 * its a string the represent a integer value for the status code.
	 */
	public void statusChanged(String newStatus) {
		logger.info("The DomiCube, "+ deviceId+" status changed to "+newStatus);
		notifyChanges("status", newStatus);
	}
	
	/**
	 * Called by ApAM when the face value changed
	 * @param newFace the new face value.
	 * its a string the represent a integer value for the face of the DomiCube.
	 */
	public void faceChanged(String newFace) {
		if(!olderFace.contentEquals("-1")){ // Usual behavior
			faceLeaved();
			olderFace = currentFace;
		}else { //First notification received
			this.olderFace = this.currentFace;
		}
		
		logger.info("The DomiCube, "+ deviceId+" face changed to "+newFace);
		notifyChanges("newFace", newFace);

	}
	
	/**
	 * Called to notify that the cube leave its older face
	 */
	private void faceLeaved() {
		logger.info("The DomiCube, "+ deviceId+" leaved the face "+olderFace);
		notifyChanges("leaveFace", olderFace);
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

	@Override
	public boolean hasReceived() {
		return detected;
	}

	@Override
	public int getCurrentFaceNumber() {
		return Integer.valueOf(currentFace);
	}
	
	/**
	 * Set the current face of this DomiCube
	 * @param currentFace the new face
	 */
	public void setCurrentFace(String currentFace) {
		this.currentFace = currentFace;
		faceChanged(currentFace);
	}

}
