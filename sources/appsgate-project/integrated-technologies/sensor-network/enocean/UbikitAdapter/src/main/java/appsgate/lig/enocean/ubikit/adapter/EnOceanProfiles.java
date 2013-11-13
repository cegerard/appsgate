package appsgate.lig.enocean.ubikit.adapter;

import java.util.ArrayList;

import fr.immotronic.ubikit.pems.enocean.ActuatorProfile;

/**
 * Enum type to map the EnOcean profile with a friendly name for end user. 
 * 
 * @author Cédric Gérard
 * @since November 13, 2013
 * @version 0.0.9 
 *
 */
public enum EnOceanProfiles {

	//EnOcean sensors profiles
	
	//######## ENOCEAN SWITCH PROFILE
	
	EEP_05_02_01("Switch Sensor 2 rockers style 1", "EnoceanSwitchSensorImpl"),
	EEP_05_02_02("Switch Sensor 2 rockers style 2", "EnoceanSwitchSensorImpl"),
	EEP_05_03_01("Switch Sensor 4 rockers style 1", "EnoceanSwitchSensorImpl"),
	EEP_05_03_02("Switch Sensor 4 rockers style 2", "EnoceanSwitchSensorImpl"),
	
	//######## ENOCEAN KEYCARD PROFILE
	
	EEP_05_04_01("Key card activated switch", "EnoceanKeyCardSensorImpl"),
	
	//######## ENOCEAN CONTACT PROFILE
	
	EEP_06_00_01("Contact sensor", "EnoceanContactSensorImpl"),
	
	//######## ENOCEAN LIGHT PROFILE
	
	EEP_07_06_01("Light sensor (300...60000)", "EnoceanLuminositySensorImpl"),
	EEP_07_06_02("Light sensor (0...1024)", "EnoceanLuminositySensorImpl"),
	EEP_07_06_03("Light sensor (0...1000 pre)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN TEMPERATURE PROFILE
	
	EEP_07_02_01("Temperature (-40, 0)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_02("Temperature (-30, 10)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_03("Temperature (-20, 20)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_04("Temperature (-10, 30)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_05("Temperature (0, 40)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_06("Temperature (10, 50)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_07("Temperature (20, 60)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_08("Temperature (30, 70)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_09("Temperature (40, 80)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_0A("Temperature (50, 90)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_0B("Temperature (60, 100)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_10("Temperature (-60, 20)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_11("Temperature (-50, 30)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_12("Temperature (-40, 40)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_13("Temperature (-30, 50)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_14("Temperature (-20, 60)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_15("Temperature (-10, 70)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_16("Temperature (0, 80)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_17("Temperature (10, 90)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_18("Temperature (20, 100)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_19("Temperature (30, 110)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_1A("Temperature (40, 120)", "EnoceanTemperatureSensorImpl"),
	EEP_07_02_1B("Temperature (50, 130)", "EnoceanTemperatureSensorImpl"),
	EEP_A5_02_20("Temperature (-10, 41.2 pre)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_02_30("Temperature (_40, 62.3 pre)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN GAS PROFILE
	
	EEP_A5_09_04("Temperature, hydrometra & CO2", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_09_05("COV sensor", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN TEMPERATURE & HYDROMETRA PROFILE
	
	EEP_07_04_01("Temperature & hydrometra (0-40°C, 0-100%)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_04_02("Temperature & hydrometra (-20-60°C, 0-100%)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN ENVIRONMENT CONTROLLER PROFILE

	EEP_07_10_01("Temperature, set point, speed & occupancy", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_02("Temperature, set point, speed & Day/Night", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_03("Temperature & set point", "EnoceanTemperatureSensorImpl"),
	EEP_07_10_04("Temperature, set point & speed", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_05("Temperature, set point & occupancy", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_06("Temperature, set point & Day/Night", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_07("Temperature & speed", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_08("Temperature, speed & occupancy ", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_09("Temperature, speed & Day/Night", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_10("Temperature, Hydrometra, set point & occupancy", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_11("Temperature, hydrometra, set point & day/night", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_12("Temperature, hydrometra & set point", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_13("Temperature, hydrometra & occupancy", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_07_10_14("Temperature, hydrometra & day/night", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN ILLUMINATION, TEMPERATURE & OCCUPANCY PROFILE
	
	EEP_07_08_01("illumination, temeprature & occupancy (0...512lux, 0..51°C, PIR)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN COUNTER PROFILE

	EEP_A5_12_00("AMR counter", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_12_01("electricity counter", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_12_02("gas counter", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_12_03("water counter", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN HVAC PROFILE
	
	EEP_A5_20_10("HVAC bi-directional: Mode, position, speed", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_A5_20_11("HVAC bi-directional; error reporting", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN WINDOW HANDLE PROFILE
	
	EEP_05_10_00("Window Handle", "EnoceanUndefinedSensorImpl"),
	
	//######## ENOCEAN OCCUPANCY PROFILE
	
	EEP_07_07_01("Occupancy PIR (ON, OFF)", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN ACTUATOR WITH CONSUMPTION MEASUREMENT PROFILE
	
	EEP_D2_01_00("Actuator with consumption feedback - 0x00", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_D2_01_02("Actuator with consumption feedback - 0x02", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	EEP_D2_01_06("Actuator with consumption feedback - 0x06", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## ENOCEAN SMOKE PROFILE
	
	Eltako_FRW_WS("Eltako smoke sensor", "EnoceanUndefinedSensorImpl"), //NOT SUPPORTED
	
	//######## THE UNKNOWN SENSOR PROFILE
	
	EEP_00_00_00("Unknown", "EnoceanUndefinedSensorImpl"),
	
	
	
	//EnOcean actuator profiles
	ONOFF_DEVICE("On/Off device", "EnoceanOnOffActuatorImpl");

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
	 * @param profileString the EnOcean profile
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
	 * @param profile the specified profile
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
