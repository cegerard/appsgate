package appsgate.lig.clock.sensor.spec;

import java.util.Calendar;

/**
 * This java interface is an ApAM specification shared by all ApAM AppsGate
 * application to provide current Time and Date information
 */
public interface CoreClockSpec {

    /**
     * @return a Calendar object whose calendar fields have been initialized
     *         with the current date and time
     */
    Calendar getCurrentDate();

    /**
     * @return the current time as UTC milliseconds from the epoch (January 1,
     *         1970 00:00:00.000 GMT )
     */
    long getCurrentTimeInMillis();

    /**
     * Set a date (might be real or simulated),
     * if date is in the future : events between old time and the setted value will NOT be thrown
     * @param a Calendar object whose calendar fields have been initialized
     *            with the desired date and time
     */
    void setCurrentDate(Calendar calendar);

    /**
     * Sets the clock from the given long value.
     * if date is in the future : events between old time and the setted value will NOT be thrown
     * @param millis
     *            the time as UTC milliseconds from the epoch (January 1, 1970
     *            00:00:00.000 GMT )
     */
    void setCurrentTimeInMillis(long millis);
    
    
    /**
     * Sets a date in the future, events between old time and the new one (currentTime + millis) WILL be thrown. 
     * @param millis the amount of millis to add to current time (3600 0000 is one hour later) 
     */
    void goAlongUntil(long millis);
    
    
    /**
     * register a new Alarm at specified calendar date
     * @param calendar The date when clock event must be generated
     * @param observer The observer that must be called
     * @return An unique identifier for the Alarm (allow unregister and distinction between multiple alarm for a single observer) or -1 if registration impossible
     */
    int registerAlarm(Calendar calendar, AlarmEventObserver observer);
    
    /**
     * Unregister a previously alarm setted
     * @param alarmEventId the Id that has been generated on alarm registration
     */
    void unregisterAlarm(int alarmEventId);
    

    /**
     * Sets the time flow rate (to speed-up or slow down time events)
     * 
     * @param rate Default is "1" (real time),
     * if inferior to "1", simulated time will flow slower (0,5 means that a day will be done in 48 hrs),
     * if superior to "1", simulated time will flow faster (2 means than a day will be done in 12 hrs)
     * if less or equal to "0", value is invalid and will not be taken into account
     * @return the setted time flow (best effort: might be different from the desired value, depends on the implementation and the hardware possibilities)
     */
    double setTimeFlowRate(double rate);
    

    /**
     * Reset the clock to the current local System Time and default speed frequency (1),
     * remove all alarm registered
     */
    void resetClock();

}
