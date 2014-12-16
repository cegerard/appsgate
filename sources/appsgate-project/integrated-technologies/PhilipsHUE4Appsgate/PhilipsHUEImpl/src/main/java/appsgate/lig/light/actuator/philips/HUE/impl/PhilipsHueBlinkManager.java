package appsgate.lig.light.actuator.philips.HUE.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible for managing the thread that controls the blinking
 */
public class PhilipsHueBlinkManager {
    private static final Logger logger = LoggerFactory.getLogger(PhilipsHueBlinkManager.class);
    private ExecutorService timer=Executors.newSingleThreadExecutor();
    private ToggleThread lastStartedThread;

    /**
     * Blinks the lamp
     * @param philips the Lamp implementation
     * @param seconds the time in seconds that the lamp will be blinking
     * @param frequency how long should wait in each state (on <wait time> off <wait time> ...)
     */
    public void blink(PhilipsHUEImpl philips,Long seconds,Long frequency) {

        //Stop blinking (if its the case to take into account the new instruction)
        stopBlinking();

        lastStartedThread=new ToggleThread(philips,seconds,frequency);

        logger.info("Starting blinking thread");

        timer.execute(lastStartedThread);
    }

    public void stopBlinking(){
        logger.info("Stop blinking command received");
        if(lastStartedThread!=null){
            lastStartedThread.desactivate();
        }else {
            logger.info("Stop blinking command ignored, no blinking thread");
        }
    }
}
