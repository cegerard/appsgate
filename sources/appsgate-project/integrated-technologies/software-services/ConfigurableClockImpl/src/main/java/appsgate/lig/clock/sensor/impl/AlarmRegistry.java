package appsgate.lig.clock.sensor.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;
import appsgate.lig.clock.sensor.spec.CoreClockSpec;

/**
 * Single repository that references all the alarms information
 * This one is not optimized as the previous one, but should be by far more simple
 * @author thibaud
 */
public class AlarmRegistry {
	
	CoreClockSpec clock;
	
	
	/**
	 * 
	 */
	public AlarmRegistry(CoreClockSpec clock) {
		this.clock = clock;
		
	}
	Map<Integer, AlarmDescriptor> alarms = new ConcurrentHashMap<Integer, AlarmDescriptor>();
	private static int currentAlarmId;
	

	private static Logger logger = LoggerFactory
			.getLogger(AlarmRegistry.class);
	
	
	public synchronized int addNewAlarm(long timeStamp, long period,
			AlarmEventObserver observer) {
		logger.trace("addNewAlarm(long timeStamp : {}, long period : {},"
			+" AlarmEventObserver observer : {})", timeStamp, period, observer);
		if(observer != null) {
			alarms.put(currentAlarmId, new AlarmDescriptor(currentAlarmId, timeStamp, period, observer));
			return currentAlarmId++;
		} else {
			return -1;
		}	
	}
	
	public synchronized long getNextAlarmLapse(long now) {
		logger.trace("getNextAlarmLapse(long now : {})", now);
		
		long minimal = -1;
		
		for(AlarmDescriptor alarm: alarms.values()) {
			long tmp = alarm.getNextEventLapse(now);
			if(alarm.isEnabled() &&( minimal == -1
					|| tmp < minimal )) {
				minimal = tmp;
			}
		}
		logger.trace("getNextAlarmLapse(...), minimal value found (in ms) : "+ minimal );
		logger.trace("getNextAlarmLapse(...), next event should occurs at "
				+ AlarmDescriptor.dateFormat.format(new Date(now+minimal)));
		
		return minimal;
	}
	
	/**
	 * Get all the alarm IDs that occurs between the specified timestamp and timestamp+tolerance in ms 
	 * @param timeStamp if 0 retrieves all the alarmsId
	 * @param tolerance if 0 retrieves all the alarmIds AFTER the timeStamp
	 * @return
	 */
	public synchronized Set<Integer> getFutureAlarms (long timeStamp, long tolerance) {
		Set<Integer> near = new HashSet<Integer>();
		
		logger.trace("getNearAlarms(long timeStamp : {}, long tolerance : {})", timeStamp, tolerance);
				
		for(AlarmDescriptor alarm: alarms.values()) {
			long tmp = alarm.getNextEventLapse(timeStamp);
			if(timeStamp == 0) {
				logger.trace("getNearAlarms(...), as timeStamp is 0,"
						+ " getting all the alarms (even the disabled), adding alarmId: "+alarm.getAlarmId());
				near.add(alarm.getAlarmId());
			} else if (tolerance == 0 && (alarm.getPeriod()>0 || alarm.getTimeStamp() >= timeStamp)) {
				logger.trace("getNearAlarms(...), as tolerance is 0,"
						+ " getting all the alarms after timeStamp and all periodics,"
						+ " adding alarmId: "+alarm.getAlarmId());
				near.add(alarm.getAlarmId());				
			} else if( tmp >= 0
					&& alarm.isEnabled()
					&& tmp < tolerance) {
				logger.trace("getNearAlarms(...), regular use case, "
						+ " the alarm should happen in a near future (and if periodic, it is not disabled)"
						+ " adding alarmId: "+alarm.getAlarmId());				
				near.add(alarm.getAlarmId());
			} else {
				logger.trace("getNearAlarms(...), skipping alarmId: "+alarm.getAlarmId());
			}
		}
		logger.trace("getNearAlarms(...), returning "+ near );
		
		return near;
	}
	
	public synchronized AlarmDescriptor removeAlarm(int alarmId) {
		logger.trace("removeAlarm(int alarmId : {})", alarmId);
		return alarms.remove(alarmId);
	}
	
	public AlarmDescriptor getAlarmFromId(int alarmId) {
		logger.trace("getAlarmFromId(int alarmId : {})", alarmId);
		return alarms.get(alarmId);
	}
	
	public void enableAlarm(int alarmId) {
		logger.trace("enableAlarm(int alarmId : {})", alarmId);
		setAlarmEnabled( alarmId, true);
		clock.calculateNextTimer();
	}
	
	public void disableAlarm(int alarmId, long disarmedPeriod) {
		logger.trace("disableAlarm(int alarmId : {})", alarmId);
		setAlarmEnabled(alarmId, false);
		
		// Can only disarm a periodic alarm for a period of time, asking a thread to enable it again
		RearmingTask rearmingTask = new RearmingTask(this, alarmId);
		Timer timer = new Timer();
		timer.schedule(rearmingTask, disarmedPeriod);
	}
	
	private synchronized void setAlarmEnabled(int alarmId, boolean enabled) {
		AlarmDescriptor alarm = alarms.get(alarmId);
		if(alarm != null ) {
			alarm.setEnabled(enabled);
		} else {
			logger.error("Cannot enable or disable the alarm, the id does not exists");
		}
	}

}
