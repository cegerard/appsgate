package appsgate.lig.enocean.ubikit.adapter.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.enocean.ubikit.adapter.UbikitAdapter;
import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.MeteringEvent.Listener;

public class MeteringEvent implements Listener {

	/**
	 * class logger member
	 */
	private static Logger logger = LoggerFactory.getLogger(MeteringEvent.class);

	/**
	 * EnOcean iPojo Adapter
	 */
	private UbikitAdapter enocean;
	
	public MeteringEvent(UbikitAdapter ubikitAdapter) {
		super();
		enocean = ubikitAdapter;
	}

	@Override
	public void onEvent(fr.immotronic.ubikit.pems.enocean.event.out.MeteringEvent arg0) {
		logger.info("This active energy for "+ arg0.getSourceItemUID() + " change to "+arg0.getValue()+" "+arg0.getMeasurementUnit().name());
		
		Instance instRef = enocean.getSensorInstance(arg0.getSourceItemUID());
		instRef.setProperty("lastRequest", String.valueOf(arg0.getDate().getTime()));
		instRef.setProperty("activeEnergy", String.valueOf(arg0.getValue()));
	}

}
