package appsgate.lig.developer.console.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.Instance;

import appsgate.lig.logical.object.spec.AbstractObjectSpec;

public class devConsoleImpl {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(devConsoleImpl.class);

	/**
	 * Undefined sensors list, resolved by ApAM
	 */
	Set<AbstractObjectSpec> abstractDevice;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The developer console component has been initialized");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The developer console component has been stopped");
	}

	/**
	 * Called by ApAM when an undefined instance is added to the set.
	 * 
	 * @param inst
	 *            , the new undefined instance
	 */
	public void addAbstractObject(Instance inst) {
		logger.debug("New abstract device added: " + inst.getName());
		
		//notify that a new object appeared
		//AbstractObjectSpec newObj = (AbstractObjectSpec)inst.getServiceObject();
		//sendToClientService.send("newDevice", newObj.getDescription());
	}

	/**
	 * Called by ApAM when an undefined instance is removed from the set
	 * 
	 * @param inst
	 *            , the removed undefined instance
	 */
	public void removedAbstractObject(Instance inst) {
		logger.debug("Abstract device removed: " + inst.getName());
	}
}
