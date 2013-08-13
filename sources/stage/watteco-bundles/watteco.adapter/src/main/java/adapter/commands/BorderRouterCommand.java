package adapter.commands;

/**
 * Enumeration of the commands it is possible to send to a border router. 
 * 
 * @author thalgott
 */
public enum BorderRouterCommand {
	
	/* ***********************************************************************
	 * 							SMART PLUG COMMANDS                          *
	 *********************************************************************** */
	
	/** Toggle command, switching the smart plug's On/Off state */
	SP_TOGGLE			("$11$50$00$06$02", 	false),
	/** Read attribute command, getting the smart plug's power/energy values */
	SP_READ_ATTRIBUTE	("$11$00$00$52$00$00", 	true);
	
	private String value;
	private boolean response;
	
	/**
	 * Inner constructor for command-type items
	 * 
	 * @param value the hexadecimal value of the command
	 * @param reponse whether this command triggers a response from the border
	 * 		router 
	 */
	private BorderRouterCommand(String value, boolean response) {
		this.value = value;
		this.response = response;
	}
	
	/**
	 * Returns whether the border router sends back a response after receiving 
	 * this command. 
	 * @return whether the border router will send a value back
	 */
	public boolean sendsBack() {
		return this.response;
	}
	
	/**
	 * Returns the command value which is to be sent to the border router, as a
	 * string.
	 * 
	 * @return the value of the command to be sent
	 */
	public String getValue() {
		return this.value;
	}
}
