package appsgate.lig.tts.yakitome;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import appsgate.lig.tts.yakitome.impl.TTSServiceImpl;
import appsgate.lig.tts.yakitome.impl.YakitomeAPIClient;

public class YakitomeAdapterTest {

	TTSServiceImpl testing;
	String sample = "Le petit chat est mort";


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		YakitomeAPI api = new YakitomeAPIClient(new AdapterListenerMock());
		// api key registered for smarthome.inria at gmail.com
		api.configure("5otuvhvboadAgcLPwy69P");

		testing = new TTSServiceImpl();
		testing.configure(api,new DAOSpeechTextItemsMock());

	}

	@After
	public void tearDown() throws Exception {
	}

	// Test disabled as it takes too much time @Test
	public void testAsynchronousTTSGenerationAndMatching() {
		int counter=0;
		boolean found = false;
		int book_id = testing.asynchronousTTSGeneration(sample);
		Assert.assertTrue("book_id not valid", book_id>0);
		while(counter<20&& !found) {
			int id = testing.getTTSItemMatchingText(sample, "Juliette", 5);
			if(id==book_id) {
				found = true;
			} else {
				counter++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}		
		Assert.assertTrue("TTS should have been generated and stored by now", found);
		
		JSONObject responseFour = testing.deleteSpeechText(book_id);
		Assert.assertTrue("A MSG shoud be provided in the response ", responseFour.has(YakitomeAPIClient.MSG_KEY));
		Assert.assertEquals("Message shoud be MSG = DELETED",YakitomeAPIClient.DELETED_MSG_VALUE, responseFour.getString(YakitomeAPIClient.MSG_KEY));		
	}
	
	// Test disabled as it takes too much time @Test
	public void testWaitForTTSGenerationAndMatching() {

		int book_id = testing.waitForTTSGeneration(sample);
		Assert.assertTrue("book_id not valid", book_id>0);
		int id = testing.getTTSItemMatchingText(sample, testing.getDefaultVoice(), testing.getDefaultSpeed());
		Assert.assertTrue("TTS should have been generated and stored with same id", id==book_id);
		
		JSONObject responseFour = testing.deleteSpeechText(book_id);
		Assert.assertTrue("A MSG shoud be provided in the response ", responseFour.has(YakitomeAPIClient.MSG_KEY));
		Assert.assertEquals("Message shoud be MSG = DELETED",YakitomeAPIClient.DELETED_MSG_VALUE, responseFour.getString(YakitomeAPIClient.MSG_KEY));
		
		id = testing.getTTSItemMatchingText(sample, testing.getDefaultVoice(), testing.getDefaultSpeed());
		Assert.assertTrue("TTS should have been removed from local list", id==0);
		
		JSONObject response = testing.getSpeechTextStatus(book_id);	
		Assert.assertTrue("A book id shoud be provided in the response ", response.has(YakitomeAPIClient.BOOK_ID_KEY));
		Assert.assertEquals("book_id should be equals to 0 (not existing)",0, response.getInt(YakitomeAPIClient.BOOK_ID_KEY));		

	}
	

}
