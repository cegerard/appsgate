package appsgate.lig.smartplug.sensor.watteco.impl;

/**
 * A class containing the different values which are returned after a 'read
 * attribute' type command. 
 * 
 * See Watteco documentation for more information. 
 * 
 * @author thalgott
 */
public class SmartPlugValue {

	/** Summation of the active energy in W.h */
	public int activeEnergy = 0;
	
	/** Number of samples */
	public int nbOfSamples = 0;
	
	/** Active power in W */
	public int activePower = 0;
}
