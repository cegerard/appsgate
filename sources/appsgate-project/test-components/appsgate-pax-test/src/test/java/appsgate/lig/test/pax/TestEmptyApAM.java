/**
 * 
 */
package appsgate.lig.test.pax;


import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import appsgate.lig.test.pax.helpers.ApAMHelper;
import appsgate.lig.test.pax.helpers.PaxedDistribution;


/**
 * @author thibaud
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestEmptyApAM extends PaxedDistribution {
	
	private static Logger logger = LoggerFactory.getLogger(TestEmptyApAM.class);

	
	@Test
	public void testEmpty() {
		logger.debug("This test is to ensure that ApAM alone is running properly");
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
		logger.debug("ApAM should be running by now");		

		ApAMHelper.listInstances();
		Assert.assertTrue(CST.componentBroker.getInst("APAM-Instance")!= null);
		ApAMHelper.waitForComponentByName(null,
				"OBRMAN-Instance", ApAMHelper.RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getInst("OBRMAN-Instance")!= null);
	}
	
	@Test
	public void testOBRMAN() {
		testEmpty();
		logger.debug("This test is to retrieve a basic appsgate component using OBRMan");
		ApAMHelper.listSpecs();
		ApAMHelper.waitForApam(ApAMHelper.RESOLVE_TIMEOUT);
		logger.debug("ApAM should be running by now");	
		
		ApAMHelper.waitForComponentByName(null, "CoreObjectSpec", ApAMHelper.RESOLVE_TIMEOUT);

		ApAMHelper.listComponents();
		
		Assert.assertTrue(CST.componentBroker.getSpec("CoreObjectSpec")!= null);
	}	
	
		
	@Configuration
	public Option[] configuration() {
		Map<String, String> testApps = new HashMap<String, String>();
		testApps.put("json", "org.json");
		return super.configuration(testApps,null);
	}		
	

}
