package appsgate.lig.button_switch.sensor.swing.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.button_switch.sensor.spec.CoreSwitchSensorSpec;
import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

public class TestCoreSwitch implements CoreSwitchSensorSpec,
CoreObjectSpec, ActionListener {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(TestCoreSwitch.class);

	private String switchNumber;
	private String buttonStatus;

	private String appsgateDeviceName;
	private String appsgateObjectId;
	private String appsgateSensorType;
	private String appsgateUserType;
	private String appsgateStatus;

	boolean configured = true;
	Object lock = new Object();


	private void initAppsgateFields() {
		switchNumber = "-1";
		buttonStatus = "false";
		appsgateDeviceName = "Unknown";
		appsgateUserType = "2";
		appsgateStatus = "0";
		appsgateObjectId = appsgateUserType + String.valueOf(this.hashCode());
		appsgateSensorType = "SwingSwitchSensor";

	}

	private boolean waitForConfiguration() {
		synchronized (lock) {
			while (!configured) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return configured;
		}
	}

	// private List<JToggleButton> theButtons;
	// private JSpinner spinNbSwitchs;
	private JToggleButton buttonOn;
	private JToggleButton buttonOff;

	private JFrame frameMultiSwitchButtons;
	//    private JPanel panelButtons;

	@Override
	public JSONObject getDescription() throws JSONException {
		logger.debug("getDescription()");
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
		if(waitForConfiguration()) {
			logger.debug("getDescription(), description ready");
			return descr;
		} else {
			logger.debug("getDescription(), no description ");
			return null;
		}
	}

	@Override
	public Action getLastAction() {
		Integer switchButton = new Integer(switchNumber);
		return new Action(switchButton.byteValue(),
				buttonStatus);
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

	JRadioButton status0;
	JRadioButton status2;
	JToggleButton buttonBlockDescription;



	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void show() {
		initAppsgateFields();
		logger.info("New swing switch sensor added, " + appsgateObjectId);
		frameMultiSwitchButtons = new JFrame("AppsGate Swing Switch Sensor "
				+ appsgateObjectId);
		frameMultiSwitchButtons.setLayout(new BorderLayout());


		ButtonGroup statusGroup = new ButtonGroup();
		status0 = new JRadioButton("Status : 0");
		status0.setSelected(true);
		status0.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				appsgateStatus = "0";			
				notifyChanges(CoreObjectSpec.KEY_STATUS, appsgateStatus);
			}
		});

		status2 = new JRadioButton("Status : 2");
		status2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				appsgateStatus = "2";	
				notifyChanges(CoreObjectSpec.KEY_STATUS, appsgateStatus);
			}
		});
		statusGroup.add(status0);
		statusGroup.add(status2);
		JPanel panelStatus = new JPanel();
		panelStatus.add(status0);
		panelStatus.add(status2);


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


		buttonBlockDescription = new JToggleButton("getDescription not blocked");
		buttonBlockDescription.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configured = true;
					buttonBlockDescription.setText("getDescription blocked");
				} else {
					buttonBlockDescription.setText("getDescription not blocked");
					configured = false;
				}
			}
		});

		frameMultiSwitchButtons.add(buttonOff,BorderLayout.NORTH);
		frameMultiSwitchButtons.add(buttonOn,BorderLayout.CENTER);
		frameMultiSwitchButtons.add(panelStatus,BorderLayout.EAST);
		frameMultiSwitchButtons.add(buttonBlockDescription,BorderLayout.WEST);


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

	/**
	 * This method uses the ApAM message model. Each call produce a
	 * SwitchNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(String varName, String value) {
		return new CoreNotificationMsg(varName, value, appsgateObjectId);
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

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}

	@Override
	public JSONObject getBehaviorDescription() {
		return null;
	}

}
