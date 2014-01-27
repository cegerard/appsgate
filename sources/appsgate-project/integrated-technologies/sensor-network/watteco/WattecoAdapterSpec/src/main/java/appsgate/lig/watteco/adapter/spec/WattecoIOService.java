package appsgate.lig.watteco.adapter.spec;

/**
 * This interface describe Watteco I/O IPojo service API
 * 
 * @author Cédric Gérard
 * @since August 13, 2013
 * @version 1.0.0
 * 
 */
public interface WattecoIOService {

	/**
	 * Sends the given command to the sensor corresponding to the given IPv6 
	 * address, and retrieves the returned value, if any.
	 * 
	 * @param addr the IPv6 address of the sensor
	 * @param cmd the command to be sent to the sensor
	 * @param resp if the border router have to wait for response
	 * @return an array of bytes corresponding to the hexadecimal value returned
	 * 		by the sensor, if any
	 */
	public byte[] sendCommand(String addr, String cmd, boolean resp);
	
	/* ***********************************************************************
	 * 							WATTECO COMMANDS                             *
	 *********************************************************************** */
	
	//ON/OFF cluster commands
	public static final String ON_OFF_CLUSTER				   		   = "6";
	public static final String ON_OFF_OFF 	 				   		   = "$11$50$00$06$00";
	public static final String ON_OFF_ON 	 				   		   = "$11$50$00$06$01";
	public static final String ON_OFF_TOGGLE 				   		   = "$11$50$00$06$02";
	public static final String ON_OFF_READ_ATTRIBUTE  		   		   = "$11$00$00$06$00$00";
	public static final String ON_OFF_CONF_REPORTING  		   		   = "$11$06$00$06$00$00$00$10$00$00$FF$FF$01";
	
	//Simple metering cluster commands
	public static final String SIMPLE_METERING_CLUSTER				   = "52";
	public static final String SIMPLE_METERING_READ_ATTRIBUTE  		   = "$11$00$00$52$00$00";
	public static final String SIMPLE_METERING_CONF_REPORTING  		   = "$11$06$00$52$00$00$00$41$00$00$02$58$0C$00$00$00$00$00$00$00$00$00$02$00$00"; // 10 minutes reporting or 2W variation.
	
	//Temperature measurement cluster commands
	public static final String TEMPERATURE_MEASUREMENT_CLUSTER		   = "402";
	public static final String TEMPERATURE_MEASUREMENT_READ_ATTRIBUTE  = "$11$00$04$02$00$00";
	
	//Humidity measurement cluster commands
	public static final String HUMIDITY_MEASUREMENT_CLUSTER		   	   = "405";
	public static final String HUMIDITY_MEASUREMENT_READ_ATTRIBUTE     = "$11$00$04$05$00$00";
	
	//Occupancy sensing cluster commands
	public static final String OCCUPANCY_SENSING_CLUSTER		   	   = "406";
	public static final String OCCUPANCY_SENSING_READ_ATTRIBUTE  	   = "$11$00$04$06$00$00";
	public static final String OCCUPANCY_SENSING_READ_OCCUPIED  	   = "$11$00$04$06$00$10";
	public static final String OCCUPANCY_SENSING_READ_INOCCUPIED 	   = "$11$00$04$06$00$11";
	public static final String OCCUPANCY_CONF_REPORTING  		   	   = "$11$06$04$06$00$00$00$18$00$02$00$3C$01"; // 2 seconds minimal reporting and 1 minutes each auto-notification

	//Analog input (basic) cluster commands
	public static final String ANALOG_INPUT_CLUSTER				   	   = "c";
	public static final String ANALOG_INPUT_READ_ATTRIBUTE  		   = "$11$00$00$0C$00$55";
	public static final String ANALOG_INPUT_APPLICATION_ASK  		   = "$11$00$00$0C$01$00";
	
	//Binary input (basic) cluster commands
	public static final String BINARY_INPUT_CLUSTER				   	   = "f";
	public static final String BINARY_INPUT_READ_ATTRIBUTE  		   = "$11$00$00$0F$00$55";
	
	//Illumination measurement cluster commands
	public static final String ILLUMINATION_MEASUREMENT_CLUSTER		   = "400";
	public static final String ILLUMINATION_MEASUREMENT_READ_ATTRIBUTE = "$11$00$04$00$00$00";
	
	//Configuration cluster commands
	public static final String CONFIGURATION_CLUSTER		   	   	   = "50";
	public static final String CONFIGURATION_READ_ATTRIBUTE  		   = "$11$00$00$50$00$01";
	public static final String CONFIGURATION_DESC 			   		   = "$11$00$00$50$00$04";
	
	//Basic cluster commands
	public static final String BASIC_CLUSTER		 			  	   = "0";
	public static final String BASIC_READ_ATTRIBUTE  				   = "$11$00$00$00$00$01";
	
	//Application type for Watteco analog input cluster
	public static final int APPLICATION_TYPE_CO2  		   			   = 327680;

}
