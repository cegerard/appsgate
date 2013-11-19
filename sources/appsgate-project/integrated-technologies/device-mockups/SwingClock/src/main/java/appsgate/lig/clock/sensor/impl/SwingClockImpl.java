package appsgate.lig.clock.sensor.impl;

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
public class SwingClockImpl extends ConfigurableClockImpl implements CoreClockSpec, CoreObjectSpec,
	ChangeListener {

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
    private static Logger logger = LoggerFactory
	    .getLogger(SwingClockImpl.class);

    public SwingClockImpl() {
	oldDay = -1;
	oldMonth = -1;
	oldYear = -1;
	oldHour = -1;
	oldMinute = -1;
	initAppsgateFields();

	resetClock();
    }

    /**
     * Called by APAM when an instance of this implementation is created
     */
    public void start() {
	logger.info("New swing clock created");

	refreshClock();
	Timer refreshTimer = new Timer();
	refreshTimer.scheduleAtFixedRate(refreshtask, Calendar.getInstance()
		.getTime(), 1000);
    }

    public void stop() {
	logger.info("Swing Clock removed");
	hide();
	timer.cancel();
	timer=null;

    }

    public void show() {
	if (frameClock == null || !frameClock.isVisible()) {

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

	    SpinnerModel dayModel = new SpinnerNumberModel(1, 1, 31, 1);
	    datePanel.add(new JLabel("Day :"));
	    spinDay = new JSpinner(dayModel);
	    spinDay.addChangeListener(this);
	    datePanel.add(spinDay);

	    SpinnerModel monthModel = new SpinnerNumberModel(1, 1, 12, 1);
	    datePanel.add(new JLabel(" Month :"));
	    spinMonth = new JSpinner(monthModel);
	    spinMonth.addChangeListener(this);
	    datePanel.add(spinMonth);

	    SpinnerModel yearModel = new SpinnerNumberModel(1971, 1971, 2050, 1);
	    datePanel.add(new JLabel(" Year :"));
	    spinYear = new JSpinner(yearModel);
	    spinYear.addChangeListener(this);
	    datePanel.add(spinYear);

	    SpinnerModel hourModel = new SpinnerNumberModel(0, 0, 23, 1);
	    spinHour = new JSpinner(hourModel);
	    spinHour.addChangeListener(this);
	    timePanel.add(spinHour);
	    timePanel.add(new JLabel("H   "));

	    SpinnerModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
	    spinMinute = new JSpinner(minuteModel);
	    spinMinute.addChangeListener(this);
	    timePanel.add(spinMinute);
	    timePanel.add(new JLabel("m   "));

	    fieldSecond = new JTextField(2);
	    fieldSecond.setEditable(false);
	    timePanel.add(fieldSecond);
	    timePanel.add(new JLabel("s "));

	    refreshClock();

	    frameClock.pack();
	    frameClock.setVisible(true);
	}
    }

    /**
     * Called by APAM when an instance of this implementation is removed
     */
    public void hide() {
	if (frameClock != null && frameClock.isVisible()) {
	    frameClock.setVisible(false);
	    frameClock.dispose();
	    spinDay = null;
	    spinMinute = null;
	    spinMonth = null;
	    spinYear = null;
	    spinHour = null;
	    fieldSecond = null;
	    frameClock = null;
	}
    }

    private void refreshClock() {

	if (frameClock != null && frameClock.isVisible()) {

	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);

	    if (oldDay != cal.get(Calendar.DAY_OF_MONTH)) {
		spinDay.setValue(cal.get(Calendar.DAY_OF_MONTH));
	    }
	    if (oldMonth != cal.get(Calendar.MONTH)) {
		spinMonth.setValue(cal.get(Calendar.MONTH) + 1);
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

    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
     * )
     */
    @Override
    public void stateChanged(ChangeEvent event) {
	try {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(cal.getTimeInMillis() + currentLag);
	    if (event.getSource().equals(spinDay))
		cal.set(Calendar.DAY_OF_MONTH,
			Integer.parseInt(spinDay.getValue().toString()));
	    if (event.getSource().equals(spinMonth))
		cal.set(Calendar.MONTH,
			Integer.parseInt(spinMonth.getValue().toString()) - 1);
	    if (event.getSource().equals(spinYear))
		cal.set(Calendar.YEAR,
			Integer.parseInt(spinYear.getValue().toString()));
	    if (event.getSource().equals(spinHour))
		cal.set(Calendar.HOUR_OF_DAY,
			Integer.parseInt(spinHour.getValue().toString()));
	    if (event.getSource().equals(spinMinute))
		cal.set(Calendar.MINUTE,
			Integer.parseInt(spinMinute.getValue().toString()));
	    currentLag = cal.getTimeInMillis()
		    - Calendar.getInstance().getTimeInMillis();

	    if (currentLag > 500) {
		logger.info("Clock lag updated to simulate a false date : "
			+ (long) currentLag / 1000);
		refreshClock();
		fireClockSetNotificationMsg(cal);
	    }

	} catch (NumberFormatException exc) {
	    logger.warn("Number Format Exception : " + exc.getMessage()
		    + ", reset Clock !");
	    resetClock();
	    refreshClock();
	}
    }
    


    public NotificationMsg fireClockSetNotificationMsg(Calendar currentTime) {
	return new ClockSetNotificationMsg(this, currentTime.getTime()
		.toString());
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



}
