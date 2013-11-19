package appsgate.lig.eude.interpreter.langage.components;

import java.util.EventObject;

/**
 * Class that carries Start event object
 * 
 * @author JR Courtois
 */
public class StartEvent extends EventObject {

	private static final long serialVersionUID = 3054033473176292430L;

	/**
     * Constructor
     * @param source the Object which has just started
     */
    public StartEvent(Object source) {
        super(source);
    }
}
