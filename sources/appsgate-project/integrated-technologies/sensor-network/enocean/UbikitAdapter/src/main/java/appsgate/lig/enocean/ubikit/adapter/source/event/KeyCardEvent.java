package appsgate.lig.enocean.ubikit.adapter.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.KeyCardInsertedEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.KeyCardTakenOutEvent;

/**
 * This class is a wrapper of enocean pem (Ubikit) events
 * for key card sensors.
 * 
 * @author Cédric Gérard
 * @since January 8, 2013
 * @version 1.0.0
 *
 */
public class KeyCardEvent implements KeyCardInsertedEvent.Listener,
		KeyCardTakenOutEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(KeyCardEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new key card event
	 * @param enocean
	 */
	public KeyCardEvent(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	//@Override
	public void onEvent(KeyCardTakenOutEvent arg0) {
		logger.info("This is key card event from " + arg0.getSourceItemUID()
				+ " a card has been taken out.");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("currentStatus", "false");
	}

	//@Override
	public void onEvent(KeyCardInsertedEvent arg0) {
		logger.info("This is key card event from " + arg0.getSourceItemUID()
				+ " a card has been inserted.");
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("currentStatus", "true");
	}

}
