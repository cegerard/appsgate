package appsgate.lig.button_switch.sensor.swing.impl;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.button_switch.sensor.messages.SwitchNotificationMsg;
import appsgate.lig.button_switch.sensor.spec.CoreSwitchSensorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class SwingSwitchSensorImpl implements CoreSwitchSensorSpec, CoreObjectSpec, ActionListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(SwingSwitchSensorImpl.class);
	
	/**
	 * the system name of this sensor.
	 */
	private String sensorName;

	/**
	 * The network sensor id
	 */
	private String sensorId;

	/**
	 * The sensor type (Actuator or Sensor)
	 */
	private String sensoreType;

	
	/**
	 * the switch number
	 */
	private String switchNumber;
	
	/**
	 * the button last status (On=true / Off=false)
	 */
	private String buttonStatus;
	
	/**
	 * Attribute use to indicate that the status change
	 */
	private boolean switchState;

	/**
	 * The type for user of this sensor
	 */
	private String userType;

	/**
	 * The current sensor status.
	 * 
	 * 0 = Off line or out of range
	 * 1 = In validation mode (test range for sensor for instance)
	 * 2 = In line or connected
	 */
	private String status;

	/**
	 * The current picture identifier
	 */
	private String pictureId;
	
	private JButton swingButton;
	private JFrame frameButton;
	
	
	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();
		descr.put("id", sensorId);
		descr.put("type", userType); //2 for switch sensor
		descr.put("status", status);
		descr.put("switchNumber", switchNumber);
		boolean stateBtn = Boolean.valueOf(buttonStatus);
		if(stateBtn){
			descr.put("buttonStatus", 1);
		} else {
			descr.put("buttonStatus", 0);
		}
		return descr;
	}

	@Override
	public Action getLastAction() {
		Integer switchButton = new Integer(switchNumber);
		return new Action(switchButton.byteValue(), Boolean.valueOf(buttonStatus));
	}
	
	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getSensorId() {
		return sensorId;
	}
	
	@Override
	public String getAbstractObjectId() {
		return getSensorId();
	}

	public String getSensoreType() {
		return sensoreType;
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
	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
		notifyChanges("pictureId", pictureId);
	}
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void show() {
		logger.info("New swing switch sensor added, "+sensorId);
		frameButton=new JFrame("AppsGate SwingSwitchSensor");
		swingButton = new JButton();
		swingButton.setPreferredSize(new Dimension(240, 60));
		swingButton.addActionListener(this);
		refreshButton();
		frameButton.add(swingButton);
		frameButton.pack();
		frameButton.setVisible(true);
	}
	
	private void refreshButton() {
	    swingButton.setText("Button "+sensorId+", n° "+switchNumber+" status "+buttonStatus);
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void hide() {
		logger.info("Swing Switch sensor removed, "+sensorId);
		frameButton.dispose();
	}
	
	/**
	 *  Called by APAM when a switch state changed.
	 * @param justuse to trigger the state change
	 */
	public void switchChanged(String status) {
		if(switchState) {
			notifyChanges("switchNumber", this.switchNumber);
			logger.info("New switch value from "+sensorId+"/"+sensorName+", "+this.switchNumber);
			notifyChanges("buttonStatus", this.buttonStatus);
			logger.info("New switch value from "+sensorId+"/"+sensorName+", "+this.buttonStatus);
			switchState = false;
		}
	}
	
	/**
	 * This method uses the ApAM message model. Each call produce a
	 * SwitchNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new SwitchNotificationMsg(new Integer(switchNumber), Boolean.valueOf(buttonStatus), varName, value, this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    if(Boolean.valueOf(buttonStatus))
		buttonStatus="false";
	    else 
		buttonStatus="true";
	    refreshButton();
	    switchChanged(buttonStatus);
	}

}
