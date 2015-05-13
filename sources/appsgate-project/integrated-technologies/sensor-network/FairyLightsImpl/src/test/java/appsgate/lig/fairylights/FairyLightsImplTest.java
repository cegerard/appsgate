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

/**
 * @author thibaud
 *
 */
public class FairyLightsImplTest {
	
	FairyLightsImpl lights;

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
		lights = new FairyLightsImpl();
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
		assertNotNull("Fairy Ligths not responding",lights.getAllLights());
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#getOneLight(int)}.
	 */
	// Disabled (Device must be physically available to run the test
	//@Test
	public void testGetOneLight() {
		assertNotNull("Fairy Ligths not responding",lights.getOneLight(0));
	}

	/**
	 * Test method for {@link appsgate.lig.fairylights.service.FairyLightsImpl#setOneColorLight(int, java.lang.String)}.
	 */
	//
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testSetAllColorLight() {
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#ff0000"));
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#00ff00"));
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#0000ff"));
	}
	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testSetColorPattern() {
		JSONObject response = lights.getAllLights();
		JSONArray array = response.getJSONArray("leds");
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
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#ffffff"));
		assertNotNull("Fairy Ligths not responding",lights.setColorPattern(new JSONObject().put(FairyLightsImpl.KEY_LEDS, pattern1)));
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#ffffff"));
		assertNotNull("Fairy Ligths not responding",lights.setColorPattern(new JSONObject().put(FairyLightsImpl.KEY_LEDS, pattern2)));
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#000000"));
	}	
	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testSingleChaserAnimation() {
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#ffffff"));
		
		lights.singleChaserAnimation(10, 20, "#ff0000");
		lights.singleChaserAnimation(20, 10, "#00ff00");
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#000000"));
	}
	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testRoundChaserAnimation() {
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#ffffff"));
		
		lights.roundChaserAnimation(0, 24, "#ff0000",3);
		assertNotNull("Fairy Ligths not responding",lights.setAllColorLight("#000000"));
	}	
	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testSetColorLight() {
		assertNotNull("Fairy Ligths not responding",lights.setOneColorLight(0, "#ffffff"));
	}
		
	
	// Disabled (Device must be physically available to run the test
	// @Test
	public void testK2000() {
		JSONObject response = lights.getAllLights();
		JSONArray array = response.getJSONArray("leds");
		int length = array.length();
		
		// Step One, light off every lamps
		for(int i = 0; i< length; i++) {
			lights.setOneColorLight(i, "#0000ff");
		}		
		
		// Step One, light off every lamps
		for(int i = 0; i< length; i++) {
			lights.setOneColorLight(i, "#000000");
		}
		
		for(int j=0;j<10;j++) {
			// Step two, make the red light goes from lamp to lamp
			for(int i = 0; i< length; i++) {
				lights.setOneColorLight(i, "#ff0000");
				if(i>0) {
					lights.setOneColorLight(i-1, "#000000");
				}
			}		
			
			for(int i = length-1; i>=0; i--) {
				lights.setOneColorLight(i, "#ff0000");
				if(i<length-1) {
					lights.setOneColorLight(i+1, "#000000");
				}
			}
		}
	}

}
