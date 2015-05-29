/**
 * 
 */
package appsgate.lig.fairylights.service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import appsgate.lig.fairylights.service.LumiPixelImpl;

/**
 * @author thibaud
 *
 */
@Ignore // Disabled (Device must be physically available to run the test)
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

	String host;
	/**
	 * @throws java.lang.Exception
	 */
	// Disabled (Device must be physically available to run the test
	@Before
	public void setUp() throws Exception {
		host=LumiPixelImpl.DEFAULT_PROTOCOL+LumiPixelImpl.DEFAULT_HOST;
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
	@Test
	public void testGetAllLights() {
		assertNotNull("Fairy Ligths not responding", LumiPixelImpl.getAllLights(host));
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#getOneLight(int)}.
	 */
	// Disabled (Device must be physically available to run the test
	@Test
	public void testGetOneLight() {
		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.getOneLight(host,0));
	}


	
	// Disabled (Device must be physically available to run the test
	@Test
	public void testSetColorLight() {
		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(host,0, "#000000"));
		assertEquals("Fairy Ligths color not correct","#000000",LumiPixelImpl.getOneLight(host,0));

		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(host,0, "#ff0000"));
		assertEquals("Fairy Ligths color not correct","#ff0000",LumiPixelImpl.getOneLight(host,0));

		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(host,0, "#00ff00"));
		assertEquals("Fairy Ligths color not correct","#00ff00",LumiPixelImpl.getOneLight(host,0));

		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(host,0, "#0000ff"));
		assertEquals("Fairy Ligths color not correct","#0000ff",LumiPixelImpl.getOneLight(host,0));
		
		assertNotNull("Fairy Ligths not responding",LumiPixelImpl.setOneColorLight(host,0, "#000000"));
		assertEquals("Fairy Ligths color not correct","#000000",LumiPixelImpl.getOneLight(host,0));

	}
		
	

}
