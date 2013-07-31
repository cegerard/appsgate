package appsgate.lig.clock.sensor.spec;

import java.util.Calendar;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to provide current Time and Date information
 */
public interface CoreClockSpec {
    
    Calendar getCurrentDate();
    
    long getCurrentTimeInMillis();
    
    /**
     * Reset the clock to the current System Time
     */
    void resetClock();
    
}
