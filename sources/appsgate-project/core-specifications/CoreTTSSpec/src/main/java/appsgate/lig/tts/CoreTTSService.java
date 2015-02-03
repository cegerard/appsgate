package appsgate.lig.tts;

import org.json.JSONObject;

public interface CoreTTSService {

	/**
	 * Check if a TTS item matching the desired sentence already exists
	 * (used for optimization as TTS generation can takes long time)
	 */
	public int getTTSItemMatchingText(String text);

	public int asynchronousTTSGeneration(String text);

	public int waitForTTSGeneration(String text);
	
	/**
	 * Return the Audio URL associated to a book_id if already generated
	 * @param book_id current book_id (identifier from TTS Generation)
	 * @param track 0 is the default (the first track, the case mos of the time), 
	 * @return the URL if generated, null otherwise
	 */
	public String getAudioURL(int book_id, int track);

	public JSONObject deleteSpeechText(int book_id);

	public JSONObject getSpeechTextStatus(int book_id);

}