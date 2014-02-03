package appsgate.lig.button_switch.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle switch events from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 5, 2013
 *
 */
public interface CoreSwitchSensorSpec {

	/**
	 * Inner class that define an action when
	 * an end user press the switch button
	 * 
	 * @author Cédric Gérard
	 *
	 */
	public class Action {
		/**
		 * The switch button number
		 * require for multiple buttons switch
		 */
		byte switchNumber;
		
		/**
		 * The state of the button On/Off = Up/Down - none = neutral position
		 */
		String state;

		/**
		 * Constructor for an action object
		 * @param switchNumber the number of the pressed button
		 * @param state the button state
		 */
		public Action(byte switchNumber, String state) {
			super();
			this.switchNumber = switchNumber;
			this.state = state;
		}
	}
	
	/**
	 * Get the last action with this button.
	 * @return the last action as an Action object
	 * @see Action
	 */
	public Action getLastAction();
	
}
