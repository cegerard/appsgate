package appsgate.lig.light.actuator.philips.HUE.impl;

/**
 * Toggles the lamp state during a certain time and frequency
 */
public class ToggleThread implements Runnable {

    private final Long durationMs;
    private final Long frequency;
    private Boolean active=true;
    private final Boolean stateBeforeStartBlink;
    private Long startTimeMs;
    private PhilipsHUEImpl philips;

    public ToggleThread(PhilipsHUEImpl philips,Long seconds,Long frequency){
        this.durationMs =seconds*1000;
        this.frequency=frequency;
        this.philips=philips;
        stateBeforeStartBlink=philips.getState();
    }

    public void desactivate(){
        active=false;
    }

    private void loadPreviousState(){
        if(stateBeforeStartBlink)
            philips.on();
        else philips.off();
    }

    @Override
    public void run() {
        startTimeMs =System.currentTimeMillis();
        Long elapsedTimeMs=0l;
        while(active&&elapsedTimeMs< durationMs){
            if(!philips.getState()){
                philips.on(1);

            }else {
                philips.off(1);
            }
            elapsedTimeMs=System.currentTimeMillis()- startTimeMs;
            try {
                Thread.sleep(frequency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loadPreviousState();
    }
}
