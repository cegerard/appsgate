/**
 * 
 */
package appsgate.lig.fairylights;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import appsgate.lig.fairylights.service.FairyLightsImpl;
import appsgate.lig.fairylights.service.LumiPixelImpl;

/**
 * @author thibaud
 *
 */
public class LumiPixelImplTest {
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	// Disabled (Device must be physically available to run the test
	// @Before
	public void setUp() throws Exception {
		LumiPixelImpl.setHost(LumiPixelImpl.DEFAULT_PROTOCOL+LumiPixelImpl.DEFAULT_HOST);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#getAllLights()}.
	 */
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testGetAllLights() {
		assertNotNull("Fairy Ligths not responding", LumiPixelImpl.getAllLights());
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#getOneLight(int)}.
	 */
	// Disabled (Device must be physically available to run the test
	//@Test
	public void testGetOneLight() {
		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.getOneLight(0));
	}


	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testSetColorLight() {
		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(0, "#ffffff"));
	}
		
	

}
