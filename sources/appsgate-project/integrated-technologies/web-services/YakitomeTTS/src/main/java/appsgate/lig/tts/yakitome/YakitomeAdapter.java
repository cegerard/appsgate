package appsgate.lig.tts.yakitome;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tts.yakitome.utils.HttpUtils;

public class YakitomeAdapter {
	public YakitomeAdapter() {
		voice = DEFAULT_VOICE;
		speed = DEFAULT_SPEED;

	}

	private static Logger logger = LoggerFactory
			.getLogger(YakitomeAdapter.class);

	public static final String YAKITOME_API_URL = "https://www.yakitome.com/api/";
	public static final String YAKITOME_JSON_URL = YAKITOME_API_URL
			+ "call/json/";
	public static final String YAKITOME_REST_URL = YAKITOME_API_URL + "rest/";
	public static final String YAKITOME_XML_URL = YAKITOME_API_URL
			+ "call/xml/";

	public static final String TTS_SERVICE = "tts?";
	public static final String STATUS_SERVICE = "status?";
	public static final String DELETE_SERVICE = "delete?";
	public static final String VOICES_SERVICE = "voices?";
	public static final String AUDIO_SERVICE = "audio?";

	public static final String PARAM_SEPARATOR = "&";

	public static final String API_KEY_PARAM = "api_key=";
	public static final String VOICE_PARAM = "voice=";
	public static final String TEXT_PARAM = "text=";
	public static final String SPEED_PARAM = "speed=";
	public static final String BOOK_ID_PARAM = "book_id=";
	public static final String FORMAT_PARAM = "format=";
	public static final String FORMAT_MP3_VALUE = "mp3";

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

	private static String api_key_value;
	private static String voice;
	private static String speed;

	/**
	 * flag asserting that configuration (api key and other properties are
	 * corrects) at first connection with the web service, this value is set to
	 * true. The value will not change, even if the service is unreachable. will
	 * be set to false only if a new configuration is set and/or an explicit
	 * "msg": "INVALID API_KEY" is provdided as api response
	 */
	private boolean isProperlyConfigured = false;

	/**
	 * This flag is set to true at first connexion with web service, and false,
	 * when api call fails
	 */
	private boolean isAvailable = false;

	boolean testService() {
		logger.trace("testService()");
		
		if(!HttpUtils.testURLTimeout(YAKITOME_API_URL,3000)) {
			logger.warn("Yakitome service URL unavailable or unreachable");
			isAvailable= false;
			return false;
		}
		
		try {
			getVoices();
			logger.trace("testService(), responded correctly, the service is available and configuration is correct");
			isAvailable = true;
			isProperlyConfigured = true;
			return true;

		} catch (ServiceException e) {
			logger.warn("Yakitome service not responding correctly");
		}
		return false;
	}

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
	public void configure(String api_key_value, String voice, int speed) {
		isProperlyConfigured = false;
		isAvailable = false;

		if (api_key_value != null) {
			this.api_key_value = api_key_value;
		}

		if (speed >= 1 && speed <= 10) {
			this.speed = String.valueOf(speed);
		}

		testService();

		if (isAvailable && voice != null && checkVoice(voice)) {
			this.voice = voice;
		}
	}
	
	static Map<String, String> initHeaders() {
		Map<String, String> requestProperties = new HashMap<String, String>();
		requestProperties.put("Content-type",
				"application/x-www-form-urlencoded");
		requestProperties.put("Accept", "text/plain");
		// This one seems mandatory to be accepted by the server (otherwise we
		// get a 403)
		requestProperties.put("user-agent", "Mozilla/5.0");

		return requestProperties;
	}

	/**
	 * Check if the voice name is existing and freely available
	 * 
	 * @param voice
	 * @return
	 */
	public boolean checkVoice(String voice) {
		logger.trace("checkVoice(String voice : {})", voice);
		if (voice == null || voice.length() < 1) {
			logger.warn("no valid voice name provided : " + voice);
			return false;
		}

		if (isAvailable) {
			try {
				JSONObject availableVoices = getVoices().optJSONObject(
						FREE_VOICES_RESPONSE_VALUE);
				if (availableVoices != null) {
					Iterator it = availableVoices.keys();
					while (it.hasNext()) {
						String language = (String) it.next();
						JSONArray arrayLang = availableVoices
								.optJSONArray(language);
						for (int i = 0; arrayLang != null
								&& i < arrayLang.length(); i++) {
							JSONArray arrayVoice = arrayLang.getJSONArray(i);
							if (voice.equals(arrayVoice.opt(2))) {
								logger.debug("checkVoice(...), found a matching voice name :"
										+ voice
										+ " with language : "
										+ language
										+ ", country : "
										+ arrayVoice.opt(0)
										+ " and gender : " + arrayVoice.opt(1));
								return true;
							}
						}
					}
				}
			} catch (ServiceException e) {
				logger.warn("checkVoice(...), Unable to check voice :"
						+ e.getMessage());
				return false;
			}
			logger.debug("checkVoice(...), no voice found matching name : "+voice);
			return false;

		} else {
			logger.info("checkVoice(...), service not available, voice is not checked");
			return false;
		}
	}

	public JSONObject getVoices() throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ VOICES_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}

	JSONObject checkResponse(String result) throws ServiceException {
		logger.trace("checkResponse(String result : {})", result);
		if (result == null) {
			testService();
			logger.error("getVoices(), Yakitome service not available or misconfigured");
			throw new ServiceException(
					"Yakitome service not available or misconfigured");
		}

		JSONObject response = new JSONObject(result);
		if (response.has(HTTP_STATUS_RESPONSE_KEY)
				&& response.getInt(HTTP_STATUS_RESPONSE_KEY) != HTTP_STATUS_RESPONSE_VALUE_OK) {
			logger.warn("checkResponse(...), wrong http status : "
					+ response.getInt(HTTP_STATUS_RESPONSE_KEY));
			throw new ServiceException(
					"Yakitome service returned wrong http status : "
							+ response.getInt(HTTP_STATUS_RESPONSE_KEY));
		}

		if (response.has(ERROR_CODE_RESPONSE_KEY)
				&& response.getInt(ERROR_CODE_RESPONSE_KEY) != ERROR_CODE_RESPONSE_VALUE_OK) {
			logger.warn("checkResponse(...), error code : "
					+ response.getInt(ERROR_CODE_RESPONSE_KEY)
					+ ", error message : "
					+ response.optString(MSG_RESPONSE_KEY));
			throw new ServiceException(
					"Yakitome service returned error code : "
							+ response.getInt(ERROR_CODE_RESPONSE_KEY)
							+ ", error message : "
							+ response.optString(MSG_RESPONSE_KEY));
		}

		return response;
	}

	/**
	 * for experimentation, testing and debugging purposes
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		YakitomeAdapter testing = new YakitomeAdapter();
		testing.configure("5otuvhvboadAgcLPwy69P", null, -1);
		testing.getSpeechTextStatus("0");
	}

	public JSONObject textToSpeech(String text) throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + VOICE_PARAM, voice);
		urlParameters.put(PARAM_SEPARATOR + SPEED_PARAM, speed);
		urlParameters.put(PARAM_SEPARATOR + TEXT_PARAM, text);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ TTS_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}

	public JSONObject getSpeechTextStatus(String speechTextId)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, speechTextId);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ STATUS_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}
	
	public JSONObject getAudioFileURL(String speechTextId)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, speechTextId);
		urlParameters.put(PARAM_SEPARATOR + FORMAT_PARAM, FORMAT_MP3_VALUE);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ AUDIO_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}	
	
	public JSONObject deleteSpeechText(String speechTextId)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, speechTextId);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ DELETE_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}		



}
