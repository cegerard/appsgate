package appsgate.lig.mockup.swingard;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.keycard.sensor.messages.KeyCardNotificationMsg;
import appsgate.lig.keycard.sensor.spec.CoreKeyCardSensorSpec;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import org.json.JSONException;
import org.json.JSONObject;

public class SwingARD implements CoreKeyCardSensorSpec, CoreObjectSpec, ActionListener {

    /**
     * Static class member uses to log what happened in each instances
     */
    private static final Logger logger = LoggerFactory.getLogger(SwingARD.class);

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
        appsgateUserType = "5";
        appsgateStatus = "2";
        appsgateObjectId = "MockupSwingSwitchSensor:00";
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

        buttonOff = new JToggleButton(" GRANT CARD ");
        buttonOff.setPreferredSize(new Dimension(240, 60));
        buttonOff.addActionListener(this);

        buttonOn = new JToggleButton(" NOT GRANTED CARD  ");
        buttonOn.setPreferredSize(new Dimension(240, 60));
        buttonOn.addActionListener(this);

        frameMultiSwitchButtons.add(buttonOff, BorderLayout.NORTH);
        frameMultiSwitchButtons.add(buttonOn, BorderLayout.SOUTH);

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
     * @param status
     *
     * to trigger the state change
     */
    public void switchChanged(String status) {
        notifyChanges("inserted", this.buttonStatus);
        logger.info("New switch value from " + appsgateObjectId + " / "
                + appsgateDeviceName + ", " + this.buttonStatus);
    }

    /**
     * This method uses the ApAM message model. Each call produce a
     * SwitchNotificationMsg object and notifies ApAM that a new message has
     * been released.
     *
     * @return nothing, it just notifies ApAM that a new message has been
     * posted.
     */
    public NotificationMsg notifyChanges(String varName, String value) {
        return new KeyCardNotificationMsg(Boolean.valueOf(true), varName, value, this);
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
            } else {
                buttonStatus = "false";
            }
        } else if (e.getSource() == buttonOff) {
            switchNumber = "0";
            if (buttonOff.isSelected()) {
                buttonOn.setSelected(false);
                buttonStatus = "true";
            } else {
                buttonStatus = "false";
            }
        }
        switchChanged(buttonStatus);
    }

    @Override
    public boolean getCardState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLastCardNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
