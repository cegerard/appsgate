package appsgate.lig.proxy.enocean.source.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.enocean.EnOceanProxy;

import fr.imag.adele.apam.Instance;
import fr.immotronic.ubikit.pems.enocean.event.out.PIROffEvent;
import fr.immotronic.ubikit.pems.enocean.event.out.PIROnEvent;

public class MotionEvent implements PIROffEvent.Listener, PIROnEvent.Listener {

	// class logger member
	private static Logger logger = LoggerFactory.getLogger(MotionEvent.class);

	private EnOceanProxy enocean;

	public MotionEvent(EnOceanProxy enocean) {
		super();
		this.enocean = enocean;
	}

	//@Override
	public void onEvent(PIROnEvent arg0) {
		logger.info("This is a gesture event from " + arg0.getSourceItemUID()
				+ ", something is moving");
	}

	//@Override
	public void onEvent(PIROffEvent arg0) {
		logger.info("This is a gesture event from " + arg0.getSourceItemUID()
				+ ", something stop moving");
	}

}
