package appsgate.lig.smartplug.sensor.watteco.impl;



public interface SmartPlugServices {

	/**
	 * Sends the read attribute command to the smartplug.
	 * The smartplug will send back a message containing its internal measures.
	 *
	 * @return an object containing the measures of this smartplug
	 */	
	public SmartPlugValue readAttribute();
}
