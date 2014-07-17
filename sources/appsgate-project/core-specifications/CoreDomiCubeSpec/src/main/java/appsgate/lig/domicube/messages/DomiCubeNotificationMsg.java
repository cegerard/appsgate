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
	 * The current battery level
	 */
	private final int batteryLevel;
	
	/**
	 * The current angle of the DomiCube
	 */
	private final float currentDimValue;

	/**
	 * Constructor of an DomiCube notification
	 * @param newFace the new face number
	 * @param varName the state variable that change
	 * @param source the core object reference
	 */
	public DomiCubeNotificationMsg(int newFace, int batteryLevel, float dimValue, String varName, String oldValue, String newValue, CoreObjectSpec source) {
		super(varName, oldValue, newValue , source);
		this.currentFaceNumber = newFace;
		this.batteryLevel = batteryLevel;
		this.currentDimValue = dimValue;
	}
	
	/**
	 * Get the current face number of the DomiCube instance
	 * @return the current face number as an integer
	 */
	public int getCurrentFaceNumber() {
		return currentFaceNumber;
	}
	
	/**
	 * Get the battery level
	 * @return the battery level as an integer
	 */
	public int getBatteryLevel() {
		return batteryLevel;
	}

	/**
	 * Get the current angle of the DomiCube
	 * @return the angle as a float
	 */
	public float getCurrentDimValue() {
		return currentDimValue;
	}

	@Override
    public String getNewValue() {
		if(getVarName().contentEquals("newFace")) {
			return String.valueOf(currentFaceNumber);
		}else if(getVarName().contentEquals("newBatteryLevel")) {
			return String.valueOf(batteryLevel);
		}else if(getVarName().contentEquals("newDimValue")) {
			return String.valueOf(currentDimValue);
		}
        
		return null;
    }

}
