package appsgate.lig.ehmi.impl.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.clock.sensor.spec.AlarmEventObserver;

/**
 * Inner class to register time notification through the core clock
 *
 * @author Cédric Gérard
 * @since April 29, 2014
 * @version 1.0.0
 */
public class TimeObserver implements AlarmEventObserver{
	
	/**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger logger = LoggerFactory.getLogger(TimeObserver.class);
    
    /**
     * The message associated to this alarm event
     */
    private String message = "";
    
    /**
     * Build a TimeObserver with message
     * @param message
     */
	public TimeObserver(String message) {
		super();
		this.message = message;
	}

	/**
	 * Call back when a alarm is fired
	 * @param alarmEventId the identifier of the triggered alarm
	 */
    public void alarmEventFired(int alarmEventId) {
    	logger.trace("alarm event is from local EHMI clock");
    }
    
    /**
     * Get a message associated to this clock event
     * @return the message as a String
     */
    public String getMessage(){
    	return message;
    }

}
