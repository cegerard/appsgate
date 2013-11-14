package appsgate.lig.watteco.adapter.services;

/**
 * This interface describe Watteco TunSlip management service API
 * 
 * @author Cédric Gérard
 * @since November 14, 2013
 * @version 1.0.0
 * 
 */
public interface WattecoTunSlipManagement {
	
	/**
	 * This method allow a third part to set the border router identifier to 
	 * start the SLIP tunnel over it
	 * @param newBrId the new border router identifier as a string
	 */
	public void setBorderRouterId(String newBrId);
	
	/**
	 * This method allow a third part to get the border router identifier.
	 * @return the border router identifier as a String
	 */
	public String getBorderRouterId();
}
