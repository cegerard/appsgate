package appsgate.lig.proxy.enocean.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.enocean.EnOceanProxy;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.KeyCardInsertedEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.KeyCardTakenOutEvent;

public class KeyCardEvent implements KeyCardInsertedEvent.Listener,
		KeyCardTakenOutEvent.Listener {

	// class logger member
	private static Logger logger = LoggerFactory.getLogger(KeyCardEvent.class);

	private EnOceanProxy enocean;

	public KeyCardEvent(EnOceanProxy enocean) {
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
