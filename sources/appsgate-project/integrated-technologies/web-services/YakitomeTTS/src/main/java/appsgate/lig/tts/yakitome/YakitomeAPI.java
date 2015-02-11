package appsgate.lig.tts.yakitome;

import org.json.JSONObject;
import org.osgi.framework.ServiceException;

public interface YakitomeAPI {
	
	public static final String TEXT_KEY = "text";

	public static final String HTTP_STATUS_KEY = "http_status";
	public static final String MSG_KEY = "msg";
	public static final String DELETED_MSG_VALUE = "DELETED";
	public static final String ERROR_CODE_KEY = "error_code";
	public static final int HTTP_STATUS_VALUE_OK = 200;
	public static final int ERROR_CODE_VALUE = 0;

	public static final String FREE_VOICES_VALUE = "free";
	public static final String BOOK_ID_KEY = "book_id";
	public static final String WORD_CNT_KEY = "word_cnt";
	public static final String VOICE_KEY = "voice";
	public static final String SPEED_KEY = "speed";

	
	public static final String STATUS_KEY = "status";
	public static final String STATUS_RUNNING_VALUE = "TTS RUNNING";
	public static final String STATUS_DONE_VALUE = "TTS DONE";
	
	public static final String AUDIOS_KEY = "audios";


		
	/**
	 * This method act as a unique setter for all configuration values,
	 * 
	 * @param api_key_value
	 *            the api_key, if null, keeping the previous value
	 */
	public void configure(String api_key_value);
	
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

	/**
	 * 
	 * @param text
	 * @param voice
	 *            , a valid voice, if not existing the previous value (or the
	 *            default one) is used
	 * @param speed
	 *            , a valid speed between 1 (slow) and 10 (fast), if the value
	 *            provided is not in the interval, keeping the previous value
	 *            (or the default one)
	 * @return
	 * @throws ServiceException
	 */
	JSONObject textToSpeech(String text, String voice, int speed)
			throws ServiceException;

	/**
	 * This one does not throws service exception as error codes may be interesting
	 * @param book_id
	 * @return
	 */
	JSONObject getSpeechTextStatus(int book_id);

	JSONObject getAudioFileURL(int book_id)
			throws ServiceException;

	JSONObject deleteSpeechText(int book_id)
			throws ServiceException;
	
	JSONObject waitForTTS(int book_id);
	
	public int checkAndgetSpeed(int speed);
	
	public boolean testService();
}