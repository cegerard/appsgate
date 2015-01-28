package appsgate.lig.tts.yakitome;

import org.json.JSONObject;
import org.osgi.framework.ServiceException;

public interface YakitomeAPI {
	
	public static final String TEXT_KEY = "text";

	public static final String HTTP_STATUS_RESPONSE_KEY = "http_status";
	public static final String MSG_RESPONSE_KEY = "msg";
	public static final String DELETED_MSG_RESPONSE_VALUE = "DELETED";
	public static final String ERROR_CODE_RESPONSE_KEY = "error_code";
	public static final int HTTP_STATUS_RESPONSE_VALUE_OK = 200;
	public static final int ERROR_CODE_RESPONSE_VALUE_OK = 0;

	public static final String FREE_VOICES_RESPONSE_VALUE = "free";
	public static final String BOOK_ID_RESPONSE_KEY = "book_id";
	public static final String WORD_CNT_RESPONSE_KEY = "word_cnt";
	
	public static final String STATUS_RESPONSE_KEY = "status";
	public static final String STATUS_RUNNING_RESPONSE_VALUE = "TTS RUNNING";
	public static final String STATUS_DONE_RESPONSE_VALUE = "TTS DONE";
	
	public static final String AUDIOS_RESPONSE_KEY = "audios";


	
	public static final String DEFAULT_VOICE = "Juliette";
	public static final String DEFAULT_SPEED = "5";
	
	/**
	 * This method act as a unique setter for all configuration values,
	 * 
	 * @param api_key_value
	 *            the api_key, if null, keeping the previous value
	 * @param voice
	 *            , a valid voice, if not existing the previous value (or the
	 *            default one) is used
	 * @param speed
	 *            , a valid speed between 1 (slow) and 10 (fast), if the value
	 *            provided is not in the interval, keeping the previous value
	 *            (or the default one)
	 */
	public void configure(String api_key_value, String voice, int speed);
	
	/**
	 * Get an unique code (that may be used as identif
	 * @return
	 */
	public String getConfigurationHashkey();

	/**
	 * Check if the voice name is existing and freely available
	 * 
	 * @param voice
	 * @return
	 */
	boolean checkVoice(String voice);

	JSONObject getVoices() throws ServiceException;

	JSONObject textToSpeech(String text)
			throws ServiceException;

	/**
	 * This one does not throws service exception as error codes may be interesting
	 * @param speechTextId
	 * @return
	 */
	JSONObject getSpeechTextStatus(int speechTextId);;

	JSONObject getAudioFileURL(int speechTextId)
			throws ServiceException;

	JSONObject deleteSpeechText(int speechTextId)
			throws ServiceException;
	
	JSONObject waitForTTS(int book_id);
	
	public boolean testService();
}