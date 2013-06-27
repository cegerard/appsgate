package appsgate.lig.proxy.enocean.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import appsgate.lig.proxy.enocean.EnOceanProxy;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.TemperatureEvent;

/**
 * This class is a wrapper of enocean pem (Ubikit) events for temperature
 * sensors.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 * 
 */
public class TempEvent implements TemperatureEvent.Listener {

	/**
	 * EnOcean iPojo Adapter
	 */
	private EnOceanProxy enocean;

	/**
	 * Build a new temperature event
	 * 
	 * @param enocean
	 */
	public TempEvent(EnOceanProxy enocean) {
		super();
		this.enocean = enocean;
	}

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(TempEvent.class);

	// @Override
	public void onEvent(TemperatureEvent arg0) {
		logger.info("This is the temperature from " + arg0.getSourceItemUID()
				+ " and in the desk the temp is about " + arg0.getTemperature()
				+ " Celsius");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("currentTemperature",
				String.valueOf(arg0.getTemperature()));
	}

}
