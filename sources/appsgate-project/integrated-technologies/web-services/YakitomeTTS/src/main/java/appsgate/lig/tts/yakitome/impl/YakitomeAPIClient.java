package appsgate.lig.tts.yakitome.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tts.yakitome.YakitomeAPI;
import appsgate.lig.tts.yakitome.utils.HttpUtils;

/**
 * This class is a mapping of main Yakitome API function to java methods
 * @see https://www.yakitome.com/documentation/tts_api
 * It should not handle the lifecycle of audio files (should be used as stateless)
 * @author thibaud
 *
 */
public class YakitomeAPIClient implements YakitomeAPI {
	public YakitomeAPIClient() {
		voice = DEFAULT_VOICE;
		speed = DEFAULT_SPEED;
	}

	private static Logger logger = LoggerFactory
			.getLogger(YakitomeAPIClient.class);

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
	public static final String TEXT_PARAM = TEXT_KEY+"=";
	public static final String SPEED_PARAM = "speed=";
	public static final String BOOK_ID_PARAM = "book_id=";
	public static final String FORMAT_PARAM = "format=";
	public static final String FORMAT_MP3_VALUE = "mp3";
	
	/**
	 * will only search for the tts generation and audio file for a limited times
	 */
	public static final int MAX_COUNTER = 20;

	/**
	 * Will only perform the api call between two sleep period
	 */
	public static final int SLEEP_PERIOD = 2*1000;	


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
	private static boolean isProperlyConfigured = false;

	/**
	 * This flag is set to true at first connexion with web service, and false,
	 * when api call fails
	 */
	private static boolean isAvailable = false;

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#testService()
	 */
	@Override	
	public boolean testService() {
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

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#configure(java.lang.String, java.lang.String, int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#checkVoice(java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#getVoices()
	 */
	@Override
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

		YakitomeAPI testing = new YakitomeAPIClient();
		testing.configure("5otuvhvboadAgcLPwy69P", null, -1);
		testing.getSpeechTextStatus(0);
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#textToSpeech(java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#getSpeechTextStatus(java.lang.String)
	 */
	@Override
	public JSONObject getSpeechTextStatus(int speechTextId) {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(speechTextId));

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ STATUS_SERVICE, initHeaders(), urlParameters, null);

		return new JSONObject(result);
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#getAudioFileURL(java.lang.String)
	 */
	@Override
	public JSONObject getAudioFileURL(int speechTextId)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(speechTextId));
		urlParameters.put(PARAM_SEPARATOR + FORMAT_PARAM, FORMAT_MP3_VALUE);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ AUDIO_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}	
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#deleteSpeechText(java.lang.String)
	 */
	@Override
	public JSONObject deleteSpeechText(int speechTextId)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(speechTextId));

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ DELETE_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}		
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#waitForTTS(String book_id)
	 */
	@Override
	public JSONObject waitForTTS(int book_id) {
		JSONObject response = null;
		int testCounter = 0;
		boolean found = false;
		while(testCounter<MAX_COUNTER && !found) {
			try {
				Thread.sleep(SLEEP_PERIOD);
			} catch (InterruptedException e) {
				logger.trace("waitForTTS(), sleep interrupted : "+e.getMessage());
			}
			
			try {
				response = getSpeechTextStatus(book_id);
			} catch (ServiceException e) {
				logger.trace("waitForTTS(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			if(!response.has(YakitomeAPI.STATUS_RESPONSE_KEY)
					||YakitomeAPI.STATUS_RUNNING_RESPONSE_VALUE.equals(
							response.getString(YakitomeAPI.STATUS_RESPONSE_KEY))) {
				testCounter++;
				logger.trace("waitForTTS(), still not generated, research counter : "+testCounter);

			} else if (YakitomeAPIClient.STATUS_DONE_RESPONSE_VALUE.equals(
					response.getString(YakitomeAPI.STATUS_RESPONSE_KEY))){
				found = true;
			}
		}
		
		if(!found) {
			logger.warn("waitForTTS(), Text To Speech not generated "
					+ "for {} after {}secs, giving up the research",book_id,SLEEP_PERIOD*MAX_COUNTER/1000);
			return null;
		}
		
		logger.trace("waitForTTS(), step two generation of audio file for id : "+book_id);

		found = false;
		testCounter=0;
		while(testCounter<MAX_COUNTER && !found) {
			try {
				Thread.sleep(SLEEP_PERIOD);
			} catch (InterruptedException e) {
				logger.trace("waitForTTS(), sleep interrupted : "+e.getMessage());
			}
			
			try {
				response = getAudioFileURL(book_id);
			} catch (ServiceException e) {
				logger.trace("waitForTTS(), Service Exception : "+e.getMessage());
				response = new JSONObject();
			}
			if(!response.has(YakitomeAPI.STATUS_RESPONSE_KEY)
					||YakitomeAPI.STATUS_RUNNING_RESPONSE_VALUE.equals(
							response.getString(YakitomeAPI.STATUS_RESPONSE_KEY))
					||!response.has(YakitomeAPIClient.AUDIOS_RESPONSE_KEY)
					||response.getJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY).length()<1) {
				testCounter++;
				logger.trace("waitForTTS(), still no audio file, research counter : "+testCounter);

			} else if (YakitomeAPIClient.STATUS_DONE_RESPONSE_VALUE.equals(
					response.getString(YakitomeAPI.STATUS_RESPONSE_KEY))
					&& response.getJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY).length()>0){
				found = true;
			}
		}
		
		if(!found) {
			logger.warn("waitForTTS(), Audio File not found {} after {}secs, giving up the research",book_id,SLEEP_PERIOD*MAX_COUNTER/1000);
			return null;
		}
		
		return response;
	}

}
