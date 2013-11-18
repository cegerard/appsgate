package appsgate.lig.eude.interpreter.langage.components;

/**
 * Interface that allow to listen to StartEvent(s)
 * 
 */
public interface StartEventListener {

    /**
     * Fire a StartEvent
     * @param e the start event that has been fired
     */
    public void startEventFired(StartEvent e);
}
