package appsgate.lig.enocean.ubikit.adapter.source.event;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;
import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.CO2ConcentrationEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.ContactCloseEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.ContactOpenEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.TemperatureEvent;
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
public class CO2Event implements CO2ConcentrationEvent.Listener, TemperatureEvent.Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(CO2Event.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;

	/**
	 * Build a new contact event
	 *
	 * @param enocean
	 */
	public CO2Event(UbikitAdapter enocean) {
		super();
		this.enocean = enocean;
	}

	@Override
	public void onEvent(CO2ConcentrationEvent co2ConcentrationEvent) {
		logger.info("CO2 concentration event received {} with the value {} ",co2ConcentrationEvent.getSourceItemUID(),co2ConcentrationEvent.getCO2Concentration());
		Instance instRef = enocean.getSensorInstance(co2ConcentrationEvent.getSourceItemUID());
		instRef.setProperty("concentration", co2ConcentrationEvent.getCO2Concentration());
	}

	@Override
	public void onEvent(TemperatureEvent temperatureEvent) {
		logger.info("CO2 temperature event received {} with the value {} ",temperatureEvent.getSourceItemUID(),temperatureEvent.getTemperature());
		Instance instRef = enocean.getSensorInstance(temperatureEvent.getSourceItemUID());
		instRef.setProperty("temperature", temperatureEvent.getTemperature());
	}
}
