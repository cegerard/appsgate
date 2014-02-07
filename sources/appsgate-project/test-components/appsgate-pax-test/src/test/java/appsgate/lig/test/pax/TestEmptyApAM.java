/**
 * 
 */
package appsgate.lig.test.pax;


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
		waitForApam(RESOLVE_TIMEOUT);
		logger.debug("ApAM should be running by now");		

		listInstances();
		Assert.assertTrue(CST.componentBroker.getInst("APAM-Instance")!= null);
		waitForComponentByName(null,
				"OBRMAN-Instance", RESOLVE_TIMEOUT);
		Assert.assertTrue(CST.componentBroker.getInst("OBRMAN-Instance")!= null);

	}
	
		
	@Configuration
	public Option[] configuration() {
//		Map<String, String> testApps = new HashMap<String, String>();
//		testApps.put("CoreObjectSpec", "appsgate.components");
		return super.configuration(null);
	}		
	

}
