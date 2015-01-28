package appsgate.lig.enocean.ubikit.adapter.source.event;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;
import fr.immotronic.ubikit.pems.enocean.event.out.RelativeHumidityEvent;
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
public class HumidityEvent implements RelativeHumidityEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(HumidityEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new contact event
	 *
	 * @param enocean
	 */
	public HumidityEvent(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	@Override
	public void onEvent(RelativeHumidityEvent relativeHumidityEvent) {
		logger.info("Humidity event received {} ",relativeHumidityEvent.getSourceItemUID());
		//Instance instRef = enocean.getSensorInstance(co2ConcentrationEvent.getSourceItemUID());
		//instRef.setProperty("currentStatus", "false");
	}

}
