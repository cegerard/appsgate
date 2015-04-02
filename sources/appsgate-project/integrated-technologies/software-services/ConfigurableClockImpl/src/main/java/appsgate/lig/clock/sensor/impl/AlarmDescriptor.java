package appsgate.lig.clock.sensor.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;

/**
 * Handy holder for all related alarm properties
 * If no period provided (0), this is a single event, otherwise this is a periodic event
 * @author thibaud
 *
 */
public class AlarmDescriptor {
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");;

	
	/**
	 * @param alarmId
	 * @param timeStamp
	 * @param period if 0, this is a single event, otherwise the period in millisecs
	 * @param observer
	 */
	public AlarmDescriptor(int alarmId, long timeStamp, long period,
			AlarmEventObserver observer) {
		this.alarmId = alarmId;
		this.timeStamp = timeStamp;
		this.period = period;
		this.observer = observer;
		enabled = true;
	}

	public int getAlarmId() {
		return alarmId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getPeriod() {
		return period;
	}

	public AlarmEventObserver getObserver() {
		return observer;
	}
	
	/**
	 * get the number of ms to wait unitl next alarm
	 * Trivial for single event, more tricky for periodic event
	 * @param now -1 if alarm is past, or the number of ms to go
	 * @return
	 */
	public long getNextEventLapse(long now) {
		if(period >0 ) {
			return getNextPeriodicLapse( now, timeStamp, period);

		} else {
			return (now>timeStamp?-1:timeStamp-now);
		}	
	}
	
	/**
	 * This one is a trickiest method -> To check if something is going wrong  
	 * @param now
	 * @param ts
	 * @param period
	 * @return
	 */
	private final static long getNextPeriodicLapse(long now, long ts, long period) {
		long tTimeStamp = ts%period;
		long tNow = now%period;
		
		if(tNow <= tTimeStamp) {
			return tTimeStamp-tNow;
		} else {
			return tTimeStamp+period-tNow;
		}		
	}

	@Override
	public String toString() {
		return "AlarmDescriptor [alarmId=" + alarmId + ", timeStamp="
				+ timeStamp + "( representing "+dateFormat.format(new Date(timeStamp))+" )"
				+ ", period=" + period + ", observer=" + observer
				+ ", enabled=" + enabled
				+ "]";
	}

	int alarmId;
	
	long timeStamp;
	
	long period;
	
	AlarmEventObserver observer;
	
	boolean enabled;


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
