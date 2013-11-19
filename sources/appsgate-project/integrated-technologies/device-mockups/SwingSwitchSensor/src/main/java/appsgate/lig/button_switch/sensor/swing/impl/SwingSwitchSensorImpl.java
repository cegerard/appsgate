package appsgate.lig.button_switch.sensor.swing.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.button_switch.sensor.messages.SwitchNotificationMsg;
import appsgate.lig.button_switch.sensor.spec.CoreSwitchSensorSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class SwingSwitchSensorImpl implements CoreSwitchSensorSpec,
	CoreObjectSpec, ActionListener {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory
	    .getLogger(SwingSwitchSensorImpl.class);

    private String switchNumber;
    private String buttonStatus;

    private String appsgateDeviceName;
    private String appsgateObjectId;
    private String appsgateSensorType;
    private String appsgateUserType;
    private String appsgateStatus;
    private String appsgatePictureId;

    private void initAppsgateFields() {
	appsgatePictureId = null;
	switchNumber = "-1";
	buttonStatus = "false";
	appsgateDeviceName = "Unknown";
	appsgateUserType = "2";
	appsgateStatus = "2";
	appsgateObjectId = appsgateUserType + String.valueOf(this.hashCode());
	appsgateSensorType = "SwingSwitchSensor";

    }

    // private List<JToggleButton> theButtons;
    // private JSpinner spinNbSwitchs;
    private JToggleButton buttonOn;
    private JToggleButton buttonOff;

    private JFrame frameMultiSwitchButtons;
//    private JPanel panelButtons;

    @Override
    public JSONObject getDescription() throws JSONException {
	JSONObject descr = new JSONObject();
	descr.put("id", appsgateObjectId);
	descr.put("type", appsgateUserType); // 2 for switch sensor
	descr.put("status", appsgateStatus);
	descr.put("switchNumber", switchNumber);
	boolean stateBtn = Boolean.valueOf(buttonStatus);
	if (stateBtn) {
	    descr.put("buttonStatus", 1);
	} else {
	    descr.put("buttonStatus", 0);
	}
	return descr;
    }

    @Override
    public Action getLastAction() {
	Integer switchButton = new Integer(switchNumber);
	return new Action(switchButton.byteValue(),
		Boolean.valueOf(buttonStatus));
    }

    public String getSensorName() {
	return appsgateDeviceName;
    }

    public void setSensorName(String sensorName) {
	this.appsgateDeviceName = sensorName;
    }

    public String getSensorId() {
	return appsgateObjectId;
    }

    @Override
    public String getAbstractObjectId() {
	return getSensorId();
    }

    public String getSensoreType() {
	return appsgateSensorType;
    }

    @Override
    public String getUserType() {
	return appsgateUserType;
    }

    @Override
    public int getObjectStatus() {
	return Integer.valueOf(appsgateStatus);
    }

    @Override
    public String getPictureId() {
	return appsgatePictureId;
    }

    @Override
    public void setPictureId(String pictureId) {
	this.appsgatePictureId = pictureId;
	notifyChanges("pictureId", pictureId);
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void show() {
	initAppsgateFields();
	logger.info("New swing switch sensor added, " + appsgateObjectId);
	frameMultiSwitchButtons = new JFrame("AppsGate Swing Switch Sensor "
		+ appsgateObjectId);
	frameMultiSwitchButtons.setLayout(new BorderLayout());

//	panelButtons = new JPanel();
//	panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.Y_AXIS));
	// panelButtons.setBorder(BorderFactory
	// .createTitledBorder("Switch Buttons"));

	// JPanel panelControl = new JPanel();
	// panelControl.setLayout(new BoxLayout(panelControl,
	// BoxLayout.X_AXIS));
	//
	//
	// SpinnerModel nbModel = new SpinnerNumberModel(1, 1, 9, 1);
	// panelControl.add(new JLabel("Nb of buttons:"));
	// spinNbSwitchs = new JSpinner(nbModel);
	// nbModel.addChangeListener(this);
	// panelControl.add(spinNbSwitchs);

	buttonOff = new JToggleButton(" OFF ");
	buttonOff.setPreferredSize(new Dimension(240, 60));
	buttonOff.addActionListener(this);


	buttonOn = new JToggleButton(" ON  ");
	buttonOn.setPreferredSize(new Dimension(240, 60));
	buttonOn.addActionListener(this);
	
	frameMultiSwitchButtons.add(buttonOff,BorderLayout.NORTH);
	frameMultiSwitchButtons.add(buttonOn,BorderLayout.SOUTH);

	//frameMultiSwitchButtons.add(panelButtons);
	// frameMultiSwitchButtons.add(panelControl);

	// changeNbOfSwitchs(2);

	frameMultiSwitchButtons.pack();
	frameMultiSwitchButtons.setVisible(true);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void hide() {
	logger.info("Swing Switch sensor removed, " + appsgateObjectId);
	frameMultiSwitchButtons.dispose();
    }

    /**
     * Called by APAM when a switch state changed.
     * 
     * @param justuse
     *            to trigger the state change
     */
    public void switchChanged(String status) {
	notifyChanges("switchNumber", this.switchNumber);
	logger.info("New switch value from " + appsgateObjectId + " / "
		+ appsgateDeviceName + ", " + this.switchNumber);
	notifyChanges("buttonStatus", this.buttonStatus);
	logger.info("New switch value from " + appsgateObjectId + " / "
		+ appsgateDeviceName + ", " + this.buttonStatus);
    }

    // Previous attempt to make multi-buttons switch, but quite useless
    // public void changeNbOfSwitchs(int newNumber) {
    // if (newNumber > 0 || newNumber < 10)
    // nbOfSwitchs = newNumber;
    // else
    // nbOfSwitchs = 2;
    // for(int i=0;i<nbOfSwitchs;i++) {
    // JToggleButton button= new JToggleButton();
    // button.setText("Button n° "
    // + i + ", status " + buttonStatus);
    // button.addActionListener(this);
    //
    // }
    //
    // }

    /**
     * This method uses the ApAM message model. Each call produce a
     * SwitchNotificationMsg object and notifies ApAM that a new message has
     * been released.
     * 
     * @return nothing, it just notifies ApAM that a new message has been
     *         posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
	return new SwitchNotificationMsg(new Integer(switchNumber),
		Boolean.valueOf(buttonStatus), varName, value, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == buttonOn) {
		switchNumber = "1";
	    if (buttonOn.isSelected()) {
		buttonOff.setSelected(false);
		buttonStatus = "true";
	    } else
		buttonStatus = "false";
	} else if (e.getSource() == buttonOff) {
	    switchNumber = "0";
	    if (buttonOff.isSelected()) {
		buttonOn.setSelected(false);
		buttonStatus = "true";
	    } else
		buttonStatus = "false";
	}
	switchChanged(buttonStatus);
    }

}
