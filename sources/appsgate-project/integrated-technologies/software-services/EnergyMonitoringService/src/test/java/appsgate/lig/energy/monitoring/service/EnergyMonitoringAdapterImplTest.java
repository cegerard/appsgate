package appsgate.lig.energy.monitoring.service;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnergyMonitoringAdapterImplTest {
	EnergyMonitoringAdapterImpl adapter;
	private final static Logger logger = LoggerFactory.getLogger(EnergyMonitoringAdapterImplTest.class);


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		adapter= new EnergyMonitoringAdapterImpl();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateInstanceID() {
		Set<String> results = new HashSet<String>();
		String result = adapter.generateInstanceID(8);

		logger.trace("testGenerateInstanceID() example of : "+result);
		results.add(result);
		
		for(int i = 0; i< 10000; i++) {
			result = adapter.generateInstanceID(8);
			assertFalse("ID are not unique : ",results.contains(result));
			results.add(result);
		}
		logger.trace("tested uniqueness for {} values of ID", results.size());
	}


}
