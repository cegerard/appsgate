package appsgate.ard.base.callback;


import appsgate.ard.ARDSwitchMonitor;

public interface DoorMonitorExceptionHandler {

    public void handleException(Throwable exception, ARDSwitchMonitor monitor);

}
