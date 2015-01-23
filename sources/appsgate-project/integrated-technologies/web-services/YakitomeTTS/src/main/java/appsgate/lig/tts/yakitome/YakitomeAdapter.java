package appsgate.lig.tts.yakitome;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tts.yakitome.utils.HttpUtils;

public class YakitomeAdapter {
	public YakitomeAdapter(String api_key_value) {
		
		this.api_key_value = api_key_value;
	}

	private static Logger logger = LoggerFactory.getLogger(YakitomeAdapter.class);
		
	public static final String YAKITOME_API_URL = "https://www.yakitome.com/api/";
	public static final String YAKITOME_JSON_URL = YAKITOME_API_URL+"call/json/";
	public static final String YAKITOME_REST_URL = YAKITOME_API_URL+"rest/";
	public static final String YAKITOME_XML_URL = YAKITOME_API_URL+"call/xml/";
	

	public static final String TTS_SERVICE = "tts?";
	public static final String STATUS_SERVICE = "status?";
	public static final String DELETE_SERVICE = "delete?";

	public static final String PARAM_SEPARATOR = "&";
	
	public static final String API_KEY_PARAM = "api_key=";
	public static final String VOICE_PARAM = "voice=";
	public static final String TEXT_PARAM = "text=";
	public static final String SPEED_PARAM = "speed=";
	public static final String BOOK_ID_PARAM = "book_id=";
	
	private static String api_key_value;
	
	
	
	
	/**
	 * for experimentation, testing and debugging purposes
	 * @param args
	 */
	public static void main(String[] args) {
				
		YakitomeAdapter testing = new YakitomeAdapter("5otuvhvboadAgcLPwy69P");
		testing.getSpeechTextStatus("0");
		
	}
	
	public JSONObject getSpeechTextStatus(String speechTextId) {
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(API_KEY_PARAM, api_key_value);
		urlParameters.put(PARAM_SEPARATOR+BOOK_ID_PARAM, speechTextId);
		
		String result = HttpUtils.sendHttpsPost(YAKITOME_JSON_URL+STATUS_SERVICE, initHeaders(), urlParameters, null);
		return new JSONObject(result);
	}

	static Map<String, String> initHeaders() {
		Map<String, String> requestProperties= new HashMap<String, String>();
		requestProperties.put("Content-type", "application/x-www-form-urlencoded");
		requestProperties.put("Accept", "text/plain");
		// This one seems mandatory to be accepted by the server (otherwise we get a 403) 
		requestProperties.put("user-agent","Mozilla/5.0");
		
		return requestProperties;
	}

}
