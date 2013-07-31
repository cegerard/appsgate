package appsgate.lig.clock.sensor.impl;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.messages.ClockSetNotificationMsg;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This java interface is an ApAM specification shared by all ApAM AppsGate
 * application to provide current Time and Date information
 */
public class SwingClock implements CoreClockSpec, CoreObjectSpec,
	ChangeListener {

    /**
     * The current picture identifier
     */
    private String appsgatePictureId;
    private String appsgateObjectId;
    private String appsgateUserType;
    private String appsgateStatus;

    /**
     * Lag between the real current Date and the one setted in the GUI
     */
    long currentLag;

    // Calendar currentCalendar;

    JFrame frameClock;
    JSpinner spinDay;
    JSpinner spinMonth;
    JSpinner spinYear;

    JSpinner spinHour;
    JSpinner spinMinute;
    JTextField fieldSecond;

    int oldDay;
    int oldMonth;
    int oldYear;
    int oldHour;
    int oldMinute;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory.getLogger(SwingClock.class);

    public SwingClock() {
	oldDay = -1;
	oldMonth = -1;
	oldYear = -1;
	oldHour = -1;
	oldMinute = -1;

	resetClock();
    }
    
    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void start() { 
	logger.info("New swing clock created");
	
	frameClock = new JFrame("AppsGate Swing Clock");

	JPanel datePanel = new JPanel();
	datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
	datePanel.setBorder(BorderFactory.createTitledBorder("Date"));

	JPanel timePanel = new JPanel();
	timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
	timePanel.setBorder(BorderFactory.createTitledBorder("Time"));

	frameClock.setLayout(new BoxLayout(frameClock.getContentPane(),
		BoxLayout.Y_AXIS));
	frameClock.add(datePanel);
	frameClock.add(timePanel);
	


	
	SpinnerModel dayModel = new SpinnerNumberModel(1,
		1,
		31,
		1);
	datePanel.add(new JLabel("Day :"));
	spinDay = new JSpinner(dayModel);
	spinDay.addChangeListener(this);
	datePanel.add(spinDay);

	SpinnerModel monthModel = new SpinnerNumberModel(1,
		1,
		12,
		1);
	datePanel.add(new JLabel(" Month :"));
	spinMonth = new JSpinner(monthModel);
	spinMonth.addChangeListener(this);
	datePanel.add(spinMonth);

	SpinnerModel yearModel = new SpinnerNumberModel(1971,
		1971,
		2050,
		1);
	datePanel.add(new JLabel(" Year :"));
	spinYear = new JSpinner(yearModel);
	spinYear.addChangeListener(this);
	datePanel.add(spinYear);

	SpinnerModel hourModel = new SpinnerNumberModel(0,
		0,
		23,
		1);
	spinHour = new JSpinner(hourModel);
	spinHour.addChangeListener(this);
	timePanel.add(spinHour);
	timePanel.add(new JLabel("H   "));

	SpinnerModel minuteModel = new SpinnerNumberModel(0,
		0,
		59,
		1);
	spinMinute = new JSpinner(minuteModel);
	spinMinute.addChangeListener(this);
	timePanel.add(spinMinute);
	timePanel.add(new JLabel("m   "));

	fieldSecond = new JTextField(2);
	fieldSecond.setEditable(false);
	timePanel.add(fieldSecond);
	timePanel.add(new JLabel("s "));

	refreshClock();
	Timer timer = new Timer();
	timer.scheduleAtFixedRate(refreshtask,
		Calendar.getInstance().getTime(), 1000);
    }
    
    public void stop() {
	logger.info("Swing Clock removed");
	frameClock.dispose();
	
    }
    

    public void show() {
	frameClock.pack();
	frameClock.setVisible(true);
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void hide() {
	frameClock.setVisible(false);
    }

    private void refreshClock() {

	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);

	if (oldDay != cal.get(Calendar.DAY_OF_MONTH)) {
	    spinDay.setValue(cal.get(Calendar.DAY_OF_MONTH));
	}
	if (oldMonth != cal.get(Calendar.MONTH)) {
	    spinMonth.setValue(cal.get(Calendar.MONTH)+1);
	}
	if (oldYear != cal.get(Calendar.YEAR)) {
	    spinYear.setValue(cal.get(Calendar.YEAR));
	}
	if (oldHour != cal.get(Calendar.HOUR_OF_DAY)) {
	    spinHour.setValue(cal.get(Calendar.HOUR_OF_DAY));
	}
	if (oldMinute != cal.get(Calendar.MINUTE)) {
	    spinMinute.setValue(cal.get(Calendar.MINUTE));
	}
	fieldSecond.setText(String.valueOf(cal.get(Calendar.SECOND)));

	oldDay = cal.get(Calendar.DAY_OF_MONTH);
	oldMonth = cal.get(Calendar.MONTH);
	oldYear = cal.get(Calendar.YEAR);
	oldHour = cal.get(Calendar.HOUR_OF_DAY);
	oldMinute = cal.get(Calendar.MINUTE);
    }
    
    
    public NotificationMsg fireClockSetNotificationMsg(Calendar currentTime) {
	return new ClockSetNotificationMsg(this, currentTime.getTime().toString());
    }

    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent event) {
	try {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);
	    if (event.getSource().equals(spinDay))
		cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(spinDay.getValue().toString()));
	    if (event.getSource().equals(spinMonth))
		cal.set(Calendar.MONTH,Integer.parseInt(spinMonth.getValue().toString())-1);
	    if (event.getSource().equals(spinYear))
		cal.set(Calendar.YEAR, Integer.parseInt(spinYear.getValue().toString()));
	    if (event.getSource().equals(spinHour))
		cal.set(Calendar.HOUR_OF_DAY,
			Integer.parseInt(spinHour.getValue().toString()));
	    if (event.getSource().equals(spinMinute))
		cal.set(Calendar.MINUTE,
			Integer.parseInt(spinMinute.getValue().toString()));
	    currentLag = cal.getTimeInMillis()
		    - Calendar.getInstance().getTimeInMillis();

	    logger.info("Lag updated to simulate a false date : "
		    + (long) currentLag / 1000);
	    refreshClock();
	    fireClockSetNotificationMsg(cal);

	} catch (NumberFormatException exc) {
	    logger.warn("Number Format Exception : " + exc.getMessage()
		    + ", reset Clock !");
	    resetClock();
	    refreshClock();
	}	
    }
    

    /* (non-Javadoc)
     * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#resetClock()
     */
    @Override   
    public void resetClock() {
	currentLag = 0;
	fireClockSetNotificationMsg(Calendar.getInstance());
    }

    /**
     * The task that is executed automatically to refresh the local calendar
     */
    TimerTask refreshtask = new TimerTask() {
	@Override
	public void run() {
	    refreshClock();
	}
    };

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.clock.sensor.spec.CoreClockSpec#getCurrentDate()
     */
    @Override
    public Calendar getCurrentDate() {
	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);
	return cal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.clock.sensor.spec.CoreClockSpec#getCurrentTimeInMillis()
     */
    @Override
    public long getCurrentTimeInMillis() {
	Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);
	return cal.getTimeInMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
     */
    @Override
    public String getAbstractObjectId() {
	// TODO Auto-generated method stub
	return appsgateObjectId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getDescription()
     */
    @Override
    public JSONObject getDescription() throws JSONException {
	// TODO Auto-generated method stub
	JSONObject descr = new JSONObject();
	
	// mandatory appsgate properties
	descr.put("id", appsgateObjectId);
	descr.put("type", appsgateUserType); //20 for weather service
	descr.put("status", appsgateStatus);
	return descr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
     */
    @Override
    public int getObjectStatus() {
	// TODO Auto-generated method stub
		return Integer.parseInt(appsgateStatus);
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getPictureId()
     */
    @Override
    public String getPictureId() {
	// TODO Auto-generated method stub
	return appsgatePictureId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
     */
    @Override
    public String getUserType() {
	// TODO Auto-generated method stub
	return appsgateUserType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.core.object.spec.CoreObjectSpec#setPictureId(java.lang.String
     * )
     */
    @Override
    public void setPictureId(String pictureId) {
	this.appsgatePictureId = pictureId;

    }

}
