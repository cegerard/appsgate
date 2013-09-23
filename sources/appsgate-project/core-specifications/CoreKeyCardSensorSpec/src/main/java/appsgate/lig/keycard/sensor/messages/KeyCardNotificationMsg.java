package appsgate.lig.keycard.sensor.messages;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;

/**
 * This class is an ApAM message for key card event notifications.
 * 
 @author Cédric Gérard
 * version 1.0.0
 * @since February 5, 2013
 */
public class KeyCardNotificationMsg implements NotificationMsg{
	
	/**
	 * The source sensor of this notification
	 */
	private CoreObjectSpec source;
	
	/**
	 * The new key card status.
	 * True if a card has been inserted and false otherwise
	 */
	private boolean isCardInserted;
	
	/**
	 * The card number that as as been pass on or inserted.
	 */
	private int cardNumber; 
	
	/**
	 * The name of the change variable
	 */
	private String varName; 
	
	/**
	 * The value corresponding to the varName variable
	 */
	private String value;

	/**
	 * Constructor for this ApAM message
	 * @param isCardInserted the new key card status
	 * @param cardNumber the inserted card number (-1 if no id)
	 * @param varName the variable that changed
	 * @param value the new value for the variable that changed
	 * @param the source object reference
	 */
	public KeyCardNotificationMsg(boolean isCardInserted, int cardNumber, String varName, String value, CoreObjectSpec source) {
		this.isCardInserted = isCardInserted;
		this.cardNumber = cardNumber;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Constructor for this ApAM message
	 * @param isCardInserted the new key card status
	 * @param varName the variable that changed
	 * @param value the new value for the variable that changed
	 * @param the source object reference
	 */
	public KeyCardNotificationMsg(boolean isCardInserted, String varName, String value, CoreObjectSpec source) {
		this.isCardInserted = isCardInserted;
		this.cardNumber = -1;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Constructor for this ApAM message
	 * @param isCardInserted the new key card status
	 * @param varName the variable that changed
	 * @param value the new value for the variable that changed
	 * @param the source object reference
	 */
	public KeyCardNotificationMsg(int cardNumber, String varName, String value, CoreObjectSpec source) {
		this.isCardInserted = false;
		this.cardNumber = cardNumber;
		this.source = source;
		this.varName = varName;
		this.value = value;
	}
	
	/**
	 * Method that returns if a card is inserted 
	 * @return  the new key card status
	 */
	public boolean getInsertedValue(){
		return isCardInserted;
	}
	
	/**
	 * Method that returns the card number that trigger the notification
	 * @return the card number
	 */
	public int getCardNumber() {
		return cardNumber;
	}

	@Override
	public String getNewValue() {
		return String.valueOf(isCardInserted)+"/"+String.valueOf(cardNumber);
	}

	@Override
	public JSONObject JSONize() throws JSONException{
		
		JSONObject notif = new JSONObject();
		
		notif.put("objectId", source.getAbstractObjectId());
		notif.put("varName", varName);
		notif.put("value", value);
		
		return notif;
	}

	@Override
	public CoreObjectSpec getSource() {
		return source;
	}
	
}
