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

import appsgate.lig.tts.yakitome.AdapterListener;
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
	public YakitomeAPIClient(AdapterListener adapter) {

		this.adapter = adapter;
	}
	
	AdapterListener adapter;

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
	public static final String VOICE_PARAM = VOICE_KEY+"=";
	public static final String TEXT_PARAM = TEXT_KEY+"=";
	public static final String SPEED_PARAM = SPEED_KEY+"=";
	public static final String BOOK_ID_PARAM = BOOK_ID_KEY+"=";
	public static final String FORMAT_PARAM = "format=";
	public static final String FORMAT_MP3_VALUE = "mp3";
	
	public static final String DEFAULT_VOICE = "Juliette";
	public static final int DEFAULT_SPEED = 5;
	public static final int MIN_SPEED = 1;
	public static final int MAX_SPEED = 10;
	
	
	/**
	 * will only search for the tts generation and audio file for a limited times
	 */
	public static final int MAX_COUNTER = 20;

	/**
	 * Will only perform the api call between two sleep period
	 */
	public static final int SLEEP_PERIOD = 2*1000;	


	private String api_key_value;


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
			adapter.serviceUnavailable();
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
		adapter.serviceUnavailable();
		return false;
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#configure(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void configure(String api_key_value) {
		isProperlyConfigured = false;
		isAvailable = false;

		if (api_key_value != null) {
			this.api_key_value = api_key_value;
		}

		testService();
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
				JSONObject availableVoices = getVoices();
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

		checkResponse(result);
		
		JSONObject json = new JSONObject(result);
		// TODO: change this if we later integrate chaged voices
		return json.optJSONObject(FREE_VOICES_VALUE);
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
		if (response.has(HTTP_STATUS_KEY)
				&& response.getInt(HTTP_STATUS_KEY) != HTTP_STATUS_VALUE_OK) {
			logger.warn("checkResponse(...), wrong http status : "
					+ response.getInt(HTTP_STATUS_KEY));
			adapter.serviceUnavailable();
			throw new ServiceException(
					"Yakitome service returned wrong http status : "
							+ response.getInt(HTTP_STATUS_KEY));
		}

		if (response.has(ERROR_CODE_KEY)
				&& response.getInt(ERROR_CODE_KEY) != ERROR_CODE_VALUE) {
			logger.warn("checkResponse(...), error code : "
					+ response.getInt(ERROR_CODE_KEY)
					+ ", error message : "
					+ response.optString(MSG_KEY));
			adapter.serviceUnavailable();
			throw new ServiceException(
					"Yakitome service returned error code : "
							+ response.getInt(ERROR_CODE_KEY)
							+ ", error message : "
							+ response.optString(MSG_KEY));
		}

		return response;
	}


	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#textToSpeech(java.lang.String)
	 */
	@Override
	public JSONObject textToSpeech(String text, String voice, int speed) throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		if (!checkVoice(voice)) {
			throw new ServiceException("Voice does not exist cannot generate Text To Speech"+voice);
		}
		
		
		
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + VOICE_PARAM, voice);
		urlParameters.put(PARAM_SEPARATOR + SPEED_PARAM, String.valueOf(checkAndgetSpeed(speed)));
		urlParameters.put(PARAM_SEPARATOR + TEXT_PARAM, text);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ TTS_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}
	
	@Override
	public int checkAndgetSpeed(int speed) {
		logger.trace("checkAndgetSpeed(int speed : {})",speed);
		if(speed ==0) {
			logger.trace("checkAndgetSpeed(...), speed is 0, using default value : "+DEFAULT_SPEED);
			return DEFAULT_SPEED;
		} else if(speed>MAX_SPEED){
			logger.trace("checkAndgetSpeed(...), speed is too high using max value: "+MAX_SPEED);
			return MAX_SPEED;
		} else if(speed < MIN_SPEED) {
			logger.trace("checkAndgetSpeed(...), speed is too low using min value: "+MIN_SPEED);
			return MIN_SPEED;
		} else {
			return speed;
		}
		
	}

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#getSpeechTextStatus(java.lang.String)
	 */
	@Override
	public JSONObject getSpeechTextStatus(int book_id) {
		logger.trace("getSpeechTextStatus(int book_id : {})",book_id);

		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(book_id));

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ STATUS_SERVICE, initHeaders(), urlParameters, null);
		
		// Does not return the checkResponse to keep the interesting error codes 
		try {
			checkResponse(result);
		} catch(ServiceException exc) {
			logger.trace("getSpeechTextStatus(...), service exception occured :"+exc.getMessage());
		}

		return new JSONObject(result);
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#getAudioFileURL(java.lang.String)
	 */
	@Override
	public JSONObject getAudioFileURL(int book_id)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(book_id));
		urlParameters.put(PARAM_SEPARATOR + FORMAT_PARAM, FORMAT_MP3_VALUE);

		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL
				+ AUDIO_SERVICE, initHeaders(), urlParameters, null);

		return checkResponse(result);
	}	
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.YakitomeAPI#deleteSpeechText(java.lang.String)
	 */
	@Override
	public JSONObject deleteSpeechText(int book_id)
			throws ServiceException {
		Map<String, String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR + BOOK_ID_PARAM, String.valueOf(book_id));

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
			if(!response.has(YakitomeAPI.STATUS_KEY)
					||YakitomeAPI.STATUS_RUNNING_VALUE.equals(
							response.getString(YakitomeAPI.STATUS_KEY))) {
				testCounter++;
				logger.trace("waitForTTS(), still not generated, research counter : "+testCounter);

			} else if (YakitomeAPIClient.STATUS_DONE_VALUE.equals(
					response.getString(YakitomeAPI.STATUS_KEY))){
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
			if(!response.has(YakitomeAPI.STATUS_KEY)
					||YakitomeAPI.STATUS_RUNNING_VALUE.equals(
							response.getString(YakitomeAPI.STATUS_KEY))
					||!response.has(YakitomeAPIClient.AUDIOS_KEY)
					||response.getJSONArray(YakitomeAPIClient.AUDIOS_KEY).length()<1) {
				testCounter++;
				logger.trace("waitForTTS(), still no audio file, research counter : "+testCounter);

			} else if (YakitomeAPIClient.STATUS_DONE_VALUE.equals(
					response.getString(YakitomeAPI.STATUS_KEY))
					&& response.getJSONArray(YakitomeAPIClient.AUDIOS_KEY).length()>0){
				found = true;
			}
		}
		
		if(!found) {
			logger.warn("waitForTTS(), Audio File not found {} after {}secs, giving up the research",book_id,SLEEP_PERIOD*MAX_COUNTER/1000);
			return null;
		}
		
		return response;
	}

	@Override
	public String getConfigurationHashkey() {
		logger.trace("getConfigurationHashkey()");
		if(api_key_value == null) {
			logger.trace("getConfigurationHashkey(), no api key defined, returning null");
			return null;
		}
		String hash = this.getClass().getName()+api_key_value;
		hash= String.valueOf(hash.hashCode());
		logger.trace("getConfigurationHashkey(), returning "+hash);
		return hash;
	}

}
