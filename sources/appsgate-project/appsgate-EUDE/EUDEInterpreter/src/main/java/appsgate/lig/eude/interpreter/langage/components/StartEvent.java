package appsgate.lig.eude.interpreter.langage.components;

import java.util.EventObject;

/**
 * Class that carries Start event object
 * 
 * @author JR Courtois
 */
public class StartEvent extends EventObject {

    /**
     * Constructor
     * @param source the Object which has just started
     */
    public StartEvent(Object source) {
        super(source);
    }
}
