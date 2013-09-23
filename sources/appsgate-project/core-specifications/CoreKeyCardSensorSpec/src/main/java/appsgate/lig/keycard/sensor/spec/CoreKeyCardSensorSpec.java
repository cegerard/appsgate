package appsgate.lig.keycard.sensor.spec;

/**
 * This java interface is an ApAM specification shared by all ApAM
 * AppsGate application to handle switch events from sensors.
 * 
 * @author Cédric Gérard
 * @version 1.0.0
 * @since February 5, 2013
 */
public interface CoreKeyCardSensorSpec {

	/**
	 * Get the current state of the key card sensor
	 * @return true if a card is inserted and false otherwise
	 */
	public boolean getCardState();
	
	/**
	 * Get the last card number that has been checked
	 * @return the card number
	 */
	public int getLastCardNumber();
}
