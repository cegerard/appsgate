package appsgate.lig.tts;

import org.json.JSONArray;
import org.json.JSONObject;

public interface CoreTTSService {

	/**
	 * Check if a TTS item matching the desired sentence already exists
	 * (used for optimization as TTS generation can takes long time)
	 */
	public int getTTSItemMatchingText(String text, String voice, int speed);

	/**
	 * Generate TTS using default voice and speed,
	 * this method use a separate thread for TTS process
	 * @param text the sentence to be generated
	 * @return the book_id of the TTS that is currently generated
	 */
	public int asynchronousTTSGeneration(String text);
	
	/**
	 * Generate TTS using specified voice and speed,
	 * this method use a separate thread for TTS process
	 * @param text the sentence to be generated
	 * @param voice the voice to use (if null, use the default voice)
	 * @param speed the speed to use (between 1 and 10) if 0 is used, will use the default speed
	 * @return the book_id of the TTS that is currently generated
	 */
	public int asynchronousTTSGeneration(String text, String voice, int speed);	

	/**
	 * Generate TTS using default voice and speed, waits that the TTS is created to return
	 * @param text the sentence to be generated
	 * @return the book_id of the TTS generated
	 */
	public int waitForTTSGeneration(String text);
	
	/**
	 * Generate TTS using specified voice and speed, waits that the TTS is created to return
	 * @param text the sentence to be generated
	 * @param voice the voice to use (if null, use the default voice)
	 * @param speed the speed to use (between 1 and 10) if 0 is used, will use the default speed
	 * @return the book_id of the TTS generated
	 */
	public int waitForTTSGeneration(String text, String voice, int speed);
		
	/**
	 * Return the Audio URL associated to a book_id if already generated
	 * @param book_id current book_id (identifier from TTS Generation)
	 * @param track 0 is the default (the first track, the case most of the time), 
	 * @return the URL if generated, null otherwise
	 */
	public String getAudioURL(int book_id, int track);
	
	/**
	 * Count the Audio URLs associated to a book_id if already generated
	 * @param book_id current book_id (identifier from TTS Generation)
	 * @return 0 if error or not generated, or the number of urls generated (if long text)
	 */
	public int countAudioURLs(int book_id);	

	public JSONObject deleteSpeechText(int book_id);

	public JSONObject getSpeechTextStatus(int book_id);

	public JSONObject getSpeechTextItem(int book_id);

	public JSONArray getSpeechTextItems();
	
	public JSONArray getSpeechTextItemsRunning();
	
	
	/**
	 * @return The current default voice name (which should be unique)
	 * (one voice might refer to one specific language and country)
	 */
	public String getDefaultVoice();
	
	/**
	 * Attempt to set an existing voice name for default
	 * (if no voice explicitely provided for TTS generation)
	 * @param voice (if not existing, keep the previous voice)
	 */
	public void setDefaultVoice(String voice);
	
	/**
	 * @return The speaker's relative speed of speaking. 5 is normal. 1 is slowest. 10 is fastest
	 */
	public int getDefaultSpeed();
	
	/**
	 * The speaker's relative speed of speaking. 5 is normal. 1 is slowest. 10 is fastest
	 * @param speed 
	 */
	public void setDefaultSpeed(int speed);
	
	/**
	 * Return the list of Voices availables as a JSONObject "Langage" : [{"Country", "Male/Female", "Voice"},...]
	 * (the list might depends of the configuration of the service, including or not charged voices)
	 * Example : 
	 * {
        "German": [["de", "Female", "Klara"], ["de", "Male", "Reiner"]],
        "Spanish": [["es", "Female", "Rosa"], ["es", "Male", "Alberto"]],
        "French": [
            ["ca", "Male", "Arnaud"], ["fr", "Female", "Juliette"],
            ["fr", "Male", "Alain"]
            ],
        "English": [
            ["gb", "Female", "Audrey"], ["in", "Female", "Anjali"],
            ["us", "Female", "Crystal"], ["us", "Female", "Julia"],
            ["us", "Female", "Kate"], ["us", "Female", "Lauren"],
            ["us", "Male", "Mike"]
            ]
    * }
    */
	public JSONObject getAvailableVoices();
	
}