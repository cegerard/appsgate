package appsgate.lig.enocean.ubikit.adapter.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.ContactCloseEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.ContactOpenEvent;

/**
 * This class is a wrapper of enocean pem (Ubikit) events for contact sensors.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 * 
 */
public class ContactEvent implements ContactCloseEvent.Listener,
		ContactOpenEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(ContactEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new contact event
	 * 
	 * @param enocean
	 */
	public ContactEvent(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	// @Override
	public void onEvent(ContactOpenEvent arg0) {
		logger.info("This is contact state change from "
				+ arg0.getSourceItemUID() + " the contact sensor is now open.");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("currentStatus", "false");
	}

	// @Override
	public void onEvent(ContactCloseEvent arg0) {
		logger.info("This is contact state change from "
				+ arg0.getSourceItemUID() + " the contact sensor is now close.");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("currentStatus", "true");
	}

}
