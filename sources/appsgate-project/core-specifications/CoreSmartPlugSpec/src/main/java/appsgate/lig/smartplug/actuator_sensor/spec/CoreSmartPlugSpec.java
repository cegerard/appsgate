package appsgate.lig.smartplug.actuator_sensor.spec;


public interface CoreSmartPlugSpec {

	/**
	 * Toggles the smartplug, i.e. switches its on/off state.
	 */
	public void toggle();
	
	/**
	 * Toggles the smartplug, i.e. switches its on/off state.
	 */
	public void on();
	
	/**
	 * Toggles the smartplug, i.e. switches its on/off state.
	 */
	public void off();
	
	/**
	 * Active power in Watt
	 * @return the consumption in Watt
	 */
	public int activePower();
	
	/**
	 * Summation of the active energy in W.h
	 * @return consumption in W/h
	 */
	public int activeEnergy();
}
