/**
 * 
 */
package appsgate.lig.fairylights.service;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import appsgate.lig.fairylights.adapter.FairyLightsAdapterSpec.LightReservationPolicy;
import appsgate.lig.fairylights.adapter.LightManagement;
import appsgate.lig.fairylights.service.FairyLightsImpl;

/**
 * @author thibaud
 *
 */
@Ignore //  Disabled (Device must be physically available to run the test)
public class FairyLightsImplTest {
	
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

	FairyLightsImpl lights;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		lights = new FairyLightsImpl();
		lights.coreObjectId = "Test";
		JSONArray lightsArray = new JSONArray();
		for(int i = 0; i<25; i++) {
			lightsArray.put(i);
		}
		JSONObject config = new JSONObject();
		config.put(FairyLightsImpl.KEY_LEDS, lightsArray);
		LightManagement lightManager = LightManagement.getInstance();
		lightManager.setPolicy(LightReservationPolicy.ASSIGNED);
		lights.configure(lightManager, config);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#setOneColorLight(int, java.lang.String)}.
	 */
	//
	@Test
	public void testSetAllColorLight() {
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#ff0000"));
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#00ff00"));
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#0000ff"));
	}
	
	@Test
	public void testSetColorPattern() {
		JSONArray array = lights.getLightsStatus();
		int length = array.length();

		
		JSONArray pattern1 = new JSONArray();
		JSONArray pattern2 = new JSONArray();
		for(int i = 0; i< 10; i++) {
			JSONObject obj1 = new JSONObject().put(FairyLightsImpl.KEY_ID, i);
			obj1.put(FairyLightsImpl.KEY_COLOR, "#0000"+String.valueOf(i)+"f");
			pattern1.put(obj1);

			JSONObject obj2 = new JSONObject().put(FairyLightsImpl.KEY_ID, length-i-1);
			obj2.put(FairyLightsImpl.KEY_COLOR, "#"+String.valueOf(i)+"f0000");
			pattern2.put(obj2 );
		}
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#ffffff"));
		assertNotNull("Fairy Lights not responding",lights.setColorPattern(pattern1));
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#ffffff"));
		assertNotNull("Fairy Lights not responding",lights.setColorPattern(pattern2));
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#000000"));
	}	
	
	// Disabled (Device must be physically available to run the test
	@Test
	public void testSingleChaserAnimation() {
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#000000"));
		
		lights.singleChaserAnimation(5, 20, "#ff0000", 0);
		lights.singleChaserAnimation(20, 5, "#ff0000", 4);
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#000000"));
	}
	
	// Disabled (Device must be physically available to run the test
	@Test
	public void testRoundChaserAnimation() {
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#ffffff"));
		
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#000000"));
	}	
	
	// Disabled (Device must be physically available to run the test
	@Test
	public void testSetColorLight() {
		assertNotNull("Fairy Lights not responding",lights.setOneColorLight(0, "#ffffff"));
	}
		
	
	// Disabled (Device must be physically available to run the test
	@Test
	public void testK2000() {
		assertNotNull("Fairy Lights not responding",lights.setAllColorLight("#000000"));

		lights.roundChaserAnimation(24, 0, "#ff0000", 2, 5);
	}

}
