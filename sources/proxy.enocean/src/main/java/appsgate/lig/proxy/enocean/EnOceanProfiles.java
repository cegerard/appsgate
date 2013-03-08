package appsgate.lig.proxy.enocean;

import java.util.ArrayList;

import fr.immotronic.ubikit.pems.enocean.ActuatorProfile;

/**
 * Enum type to map the EnOcean profile with a friendly name for end user. 
 * 
 * @author Cédric Gérard
 * @since February 7, 2013
 * @version 0.0.6 
 *
 */
public enum EnOceanProfiles {

	//EnOcean sensors profiles
	EEP_05_02_01("Switch Sensor", "SwitchSensorImpl"),
	EEP_05_04_01("Key card sensor", "KeyCardSensorImpl"),
	EEP_06_00_01("Contact sensor", "ContactSensorImpl"),
	EEP_07_06_02("Light sensor", "LuminositySensorImpl"),
	EEP_07_10_03("Temperature + set point sensor", "TemperatureSensorImpl"),
	EEP_A5_12_00("AMR counter", "UndefinedSensorImpl"),
	EEP_00_00_00("Unknown", "UndefinedSensorImpl"),
	
	//EnOcean actuator profiles
	ONOFF_DEVICE("On/Off device", "OnOffActuatorImpl");

	/**
	 * the name that a end user can understand.
	 */
	private String userFriendlyName;
	
	/**
	 * The ApAM implementation corresponding to this EnOcean profile
	 */
	private String ApAMImplementation;

	/**
	 * All actuator profile support by ubikit and appsgate
	 */
	private static ActuatorProfile[] actuatorProfiles = {ActuatorProfile.ONOFF_DEVICE, ActuatorProfile.RH_DEVICE, ActuatorProfile.BLIND_AND_SHUTTER_MOTOR_DEVICE};

	/**
	 * Private constructor to initiate enumerate value
	 * @param userFriendlyName, the friendly name for this EnOcean profile
	 */
	private EnOceanProfiles(String userFriendlyName, String ApAMImplementation) {
		this.userFriendlyName = userFriendlyName;
		this.ApAMImplementation = ApAMImplementation;
	}

	/**
	 * Get the user name for this enumerate value.
	 * @return the user name for this EnOcean profile
	 */
	public String getUserFriendlyName() {
		return userFriendlyName;
	}
	
	/**
	 * Get the ApAM implementation corresponding to this EnOcean profile
	 * @return the String name of an ApAM implementation
	 */
	public String getApAMImplementation(){
		return ApAMImplementation;
	}

	/**
	 * Get the EnOcean profile object corresponding to parameter profile string or EEP_00_00_00 if no
	 * EnOcean profile match the parameter string.
	 * 
	 * @param profileString, the EnOcean profile
	 * @return an EnOcean profile object
	 */
	public static EnOceanProfiles getEnOceanProfile(String profileString){
		EnOceanProfiles prof = EEP_00_00_00;
		for (EnOceanProfiles ep : EnOceanProfiles.values()){
			if(ep.name().contentEquals(profileString)){
				prof = ep;
				break;
			}
		}
		return prof;
	}
	
	/**
	 * Get the actuator ubikit profile for the specified actuator.
	 * 
	 * If no corresponding ubikit profile were found the getActuatorProfile
	 * method return null.
	 * 
	 * @param profile, the specified profile
	 * @return an ActuatorProfile
	 */
	public static ActuatorProfile getActuatorProfile(String profile) {
		int l = actuatorProfiles.length;
		boolean found = false;
		int i = 0;
		ActuatorProfile ap = null;
		
		while(!found && i < l){
			ap = actuatorProfiles[i];
			if(ap.name().equalsIgnoreCase(profile)){
				found = true;
			}
			i++;
		}
		return ap;
	}
	
	/**
	 * Return all actuator profiles support by ApAM and AppsGate.
	 * 
	 * @return an ArrayList<String> of all the actuator profile name support
	 */
	public static ArrayList<String> getActuatorProfiles() {
		int l = actuatorProfiles.length;
		int i = 0;
		ActuatorProfile ap = null;
		ArrayList<String> profileList = new ArrayList<String>();
		
		while(i < l){
			ap = actuatorProfiles[i];
			profileList.add(ap.name());
			i++;
		}
		return profileList;
	}
	
}
