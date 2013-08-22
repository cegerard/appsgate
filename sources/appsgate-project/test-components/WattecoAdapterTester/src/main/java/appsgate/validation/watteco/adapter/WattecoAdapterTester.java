package appsgate.validation.watteco.adapter;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.watteco.adapter.services.WattecoDiscoveryService;



/**
 * This class is use to validate the virtual adapter return call
 * 
 * @author Cédric Gérard
 * 
 */
@Component
@Instantiate (name="WattecoAdapterTester")
public class WattecoAdapterTester {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(WattecoAdapterTester.class);
	
	@Requires(optional=false)
	private WattecoDiscoveryService wattecoService;

	public WattecoAdapterTester() {
		super();
		logger.info("###### Watteco adapter tester instanciated.");
	}
	
	@Validate
	public void validate() {
		logger.info("###### Watteco adapter tester validated.");
		wattecoService.discover();
	}
	
	@Invalidate
	public void Invalidate () {
		logger.info("###### Watteco adapter tester invalidated.");
	}
	

}
