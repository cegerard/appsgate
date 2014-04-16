package appsgate.lig.domicube.messages;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for DomiCube state change notifications
 * 
 * @author Cédric Gérard
 * @since April 16, 2014
 *
 */
public class DomiCubeNotificationMsg extends CoreNotificationMsg {
	
	/**
	 * The current DomiCube status
	 */
	private final int currentFaceNumber;

	/**
	 * Constructor of an DomiCube notification
	 * @param newFace the new face number
	 * @param varName the state variable that change
	 * @param value the new value of the state variable
	 * @param source the core object reference
	 */
	public DomiCubeNotificationMsg(int newFace, String varName, String value, CoreObjectSpec source) {
		super(varName, value, source);
		currentFaceNumber = newFace;
	}
	
	/**
	 * Get the current face number of the DomiCube instance
	 * @return the current face number as an integer
	 */
	public int getCurrentFaceNumber() {
		return currentFaceNumber;
	}

	@Override
    public String getNewValue() {
        return String.valueOf(currentFaceNumber);
    }

}
