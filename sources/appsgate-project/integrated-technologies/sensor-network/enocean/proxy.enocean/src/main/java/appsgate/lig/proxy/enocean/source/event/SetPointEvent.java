package appsgate.lig.proxy.enocean.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.enocean.EnOceanProxy;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.SetPointChangedEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.SetPointChangedEvent.Listener;

/**
 * This class is a wrapper of enocean pem (Ubikit) events for set point sensors.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 * 
 */
public class SetPointEvent implements Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(SetPointEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private EnOceanProxy enocean;

	/**
	 * Build a new set point event
	 * 
	 * @param enocean
	 */
	public SetPointEvent(EnOceanProxy enocean) {
		super();
		this.enocean = enocean;
	}

	// @Override
	public void onEvent(SetPointChangedEvent arg0) {
		logger.info("This is a set point event from " + arg0.getSourceItemUID()
				+ ", its new value is " + arg0.getSetPoint());
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.isSingleton();
	}

}
