package appsgate.lig.tts;

import org.json.JSONObject;

public interface CoreTTSService {

	/**
	 * Check if a TTS item matching the desired sentence already exists
	 * (used for optimization as TTS generation can takes long time)
	 */
	public int getTTSItemMatchingSentence(String sentence);

	public int asynchronousTTSGeneration(String text);

	public int waitForTTSGeneration(String text);

	public JSONObject deleteSpeechText(int book_id);

	public JSONObject getSpeechTextStatus(int book_id);

}