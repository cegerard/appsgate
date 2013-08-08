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
     * @param a Calendar object whose calendar fields have been initialized
     *            with the desired date and time
     */
    void setCurrentDate(Calendar calendar);

    /**
     * Sets the clock from the given long value.
     * 
     * @param millis
     *            the time as UTC milliseconds from the epoch (January 1, 1970
     *            00:00:00.000 GMT )
     */
    void setCurrentTimeInMillis(long millis);

    /**
     * Reset the clock to the current local System Time
     */
    void resetClock();

}
