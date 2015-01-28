package appsgate.lig.enocean.ubikit.adapter.source.event;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;
import fr.immotronic.ubikit.pems.enocean.event.out.CO2ConcentrationEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.VOCConcentrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper of enocean pem (Ubikit) events for contact sensors.
 *
 * @author Jander Nascimento
 * @since January 27, 2015
 * @version 1.0.0
 * 
 */
public class VOCEvent implements VOCConcentrationEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(VOCEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new contact event
	 *
	 * @param enocean
	 */
	public VOCEvent(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	@Override
	public void onEvent(VOCConcentrationEvent vocConcentrationEvent) {
		logger.info("VOC concentration event received {} ",vocConcentrationEvent.getSourceItemUID());
		//Instance instRef = enocean.getSensorInstance(co2ConcentrationEvent.getSourceItemUID());
		//instRef.setProperty("currentStatus", "false");
	}


}
