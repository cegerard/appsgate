package appsgate.lig.enocean.ubikit.adapter.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.PIROffEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.PIROnEvent;

/**
 * This class is a wrapper of enocean pem (Ubikit) events for motion sensor.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 * 
 */
public class MotionEvent implements PIROffEvent.Listener, PIROnEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(MotionEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new motion event
	 * 
	 * @param enocean
	 */
	public MotionEvent(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	// @Override
	public void onEvent(PIROnEvent arg0) {
		logger.info("This is a gesture event from " + arg0.getSourceItemUID()
				+ ", something is moving");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.isSingleton();
	}

	// @Override
	public void onEvent(PIROffEvent arg0) {
		logger.info("This is a gesture event from " + arg0.getSourceItemUID()
				+ ", something stop moving");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.isSingleton();
	}

}
