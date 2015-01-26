package appsgate.lig.tts.yakitome;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class YakitomeAdapterTest {
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(YakitomeAdapterTest.class);

	YakitomeAdapter testing;
	
	String sample_obsolete = "Souvent, pour s’amuser, les hommes d’équipage"
			+" Prennent des albatros, vastes oiseaux des mers,"
			+" Qui suivent, indolents compagnons de voyage,"
			+" Le navire glissant sur les gouffres amers.";
	String sample = "Le petit chat est mort";

	
	String[] someValidVoices={"Alain", "Juliette", "Kate", "Mike"};
	String[] someUnvalidVoices={"Norbert", "tutu", "Jude"}; // Note: Jude exists but as metered voice (not free)

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testing = new YakitomeAdapter();
		// api key registered for smarthome.inria at gmail.com
		testing.configure("5otuvhvboadAgcLPwy69P", "Juliette", -1);

	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testCheckVoice() {
	
		
		for(String voice:someValidVoices) {
			Assert.assertTrue("Voice should exists : "+voice, testing.checkVoice(voice));					
		}
		for(String voice:someUnvalidVoices) {
			Assert.assertFalse("Voice should not exists (or not free) : "+voice, testing.checkVoice(voice));			
		}
	}	
	
	/**
	 * done a single test for all the phase of text to speech
	 * 1° create the TTS,
	 * 2° then check status until file is created
	 * 3° then create audio file,
	 * 4° to finally delete corresponding book_id
	 * (test disabled because it may take between 15secs and 1 minute to complete)
	 */
	public void fullTestTTS() {
		try {
			
			logger.debug("\n\n*** Step One : creating text to speech ***");
			JSONObject responseOne = testing.textToSpeech(sample);
			Assert.assertTrue("When text to speech generated, a book id should hav been generated", responseOne.has(YakitomeAdapter.BOOK_ID_RESPONSE_KEY));
			Assert.assertTrue("Book id should be sup to 0", responseOne.getInt(YakitomeAdapter.BOOK_ID_RESPONSE_KEY)>0);
			Assert.assertTrue("There should be a words count in the sample sentence", responseOne.has(YakitomeAdapter.WORD_CNT_RESPONSE_KEY));
			Assert.assertEquals("There should be 5 words in the sample sentence", 5, responseOne.getInt(YakitomeAdapter.WORD_CNT_RESPONSE_KEY));
			
			String book_id = String.valueOf(responseOne.getInt(YakitomeAdapter.BOOK_ID_RESPONSE_KEY));
			
			logger.debug("\n\n*** Step Two : Checking TTS status ***");			
			int testCounter = 0;
			while(testCounter<15) {
				Thread.sleep(2000);
				JSONObject responseTwo= testing.getSpeechTextStatus(book_id);
				Assert.assertTrue("Status for book_id should exists", responseTwo.has(YakitomeAdapter.STATUS_RESPONSE_KEY));
				String status = responseTwo.getString(YakitomeAdapter.STATUS_RESPONSE_KEY);
				if(YakitomeAdapter.STATUS_DONE_RESPONSE_VALUE.equals(status)) {
					testCounter = 99;
				} else if(YakitomeAdapter.STATUS_RUNNING_RESPONSE_VALUE.equals(status)) {
					testCounter++;	
					logger.debug("--> Checking status, still not done, counter = "+testCounter);

				} else {
					fail("Unknown Text to speech status");
				}		
			}
			Assert.assertTrue("Text to speech should be done in less than 10 * 2 secs",testCounter==99);
			
			logger.debug("\n\n*** Step Three : creating Audio file ***");
			JSONObject responseThree = new JSONObject();

			testCounter = 0;
			while(testCounter<10) {
				Thread.sleep(2000);
				responseThree= testing.getAudioFileURL(book_id);
				Assert.assertTrue("Status for book_id should exists", responseThree.has(YakitomeAdapter.STATUS_RESPONSE_KEY));
				String status = responseThree.getString(YakitomeAdapter.STATUS_RESPONSE_KEY);
				if(YakitomeAdapter.STATUS_DONE_RESPONSE_VALUE.equals(status)) {
					Assert.assertTrue("Audio file(s) should be provided", responseThree.has(YakitomeAdapter.AUDIOS_RESPONSE_KEY));
					if(responseThree.getJSONArray(YakitomeAdapter.AUDIOS_RESPONSE_KEY).length()>0) {
						testCounter = 12;
					} else {
						testCounter++;						
					}
				} else if(YakitomeAdapter.STATUS_RUNNING_RESPONSE_VALUE.equals(status)) {
					testCounter++;
				} else {
					fail("Unknown Text to speech status");
				}		
				logger.debug("--> Checking status, still no mp3 file provided, counter = "+testCounter);
			}
			Assert.assertTrue("Text to speech audio file should be generated in less than 10 * 2 secs",testCounter==12);
			Assert.assertTrue("One or mor audio files shoud be provided",responseThree.getJSONArray(YakitomeAdapter.AUDIOS_RESPONSE_KEY).length()>0);

			
			logger.debug("\n\n*** Step Four : Deleting TTS ***");	
			JSONObject responseFour = testing.deleteSpeechText(book_id);
			Assert.assertTrue("A MSG shoud be provided in the responde ", responseFour.has(YakitomeAdapter.MSG_RESPONSE_KEY));
			Assert.assertEquals("Message shoud be MSG = DELETED",YakitomeAdapter.DELETED_MSG_RESPONSE_VALUE, responseFour.getString(YakitomeAdapter.MSG_RESPONSE_KEY));
			
		} catch (Exception e) {
			fail("text to speech should not raise exception : "+e.getMessage());
		}	
	}	
	
	

}
