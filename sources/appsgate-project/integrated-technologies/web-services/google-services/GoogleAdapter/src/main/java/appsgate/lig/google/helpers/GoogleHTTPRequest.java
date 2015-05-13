package appsgate.lig.google.helpers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * Created by thibaud on 02/09/2014.
 */
public class GoogleHTTPRequest {

	static Logger logger = LoggerFactory.getLogger(GoogleHTTPRequest.class);

	public static void main(String[] args) {
		System.out.println("Testing connect to google");     


		try {
//			String clientId = "767635155947-d4g4nol633qqtceotb98m6jgu8ia5j2f.apps.googleusercontent.com";
//			String clientSecret = "yrXTCVBz3cR-vkuboXfWXavT";
//			String scope= "https://www.googleapis.com/auth/calendar";
//			String deviceCode = "4/uWExoAh1tG8ZSsJ0f0z8vUWm5l_h";
//			String refreshToken = "1/_cd-v7FNEYsoE2sm1Oewj6B8ov23P1lbkk4NncU635Q";
//			String accessTokenType = "Bearer";
//			String accessTokenValue= "ya29.dQA4zXXCA4Bh6x0AAAB0yY_wR8tdhMrc-U0UYK0Kub_oN5jK0fMNeI6k9gaH-Q";
//			String apiKey = "AIzaSyASWRbL70yXR0rRJHNMUV95oCU9hxb-U_s";
//			
//			String calendarId="primary";
//			
//			Date currentDate = new Date();			
//			Date startDate = new Date(currentDate.getTime() + 1800000);
//			Date endDate = new Date(startDate.getTime() + 3600000);
//			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//			
//			Map<String, String> urlParameters=new HashMap<String, String>();
//
//			JSONObject res = GoogleOpenAuthent.getAccessToken(clientId, clientSecret, refreshToken);
//			logger.debug(" returns : "+res.toString());
//			accessTokenValue = res.getString(GoogleOpenAuthent.PARAM_ACCESSTOKEN);			
//			urlParameters.put("timeMin", dateFormat.format(currentDate));
//			logger.debug(" get returns : "+GoogleCalendarReader.getEvents(apiKey, accessTokenType, accessTokenValue, calendarId, urlParameters));
//			
//			String requestContent;
//			JSONObject content = new JSONObject();
//			content.put("start", new JSONObject().put("dateTime",  dateFormat.format(startDate)));
//			content.put("end", new JSONObject().put("dateTime",  dateFormat.format(endDate)));
//			content.put("summary", "testMAnualAdding");
//			requestContent=content.toString();
//			
//			JSONObject res2=GoogleCalendarWriter.addEvent(apiKey, accessTokenType, accessTokenValue, calendarId, requestContent);
//			logger.debug(" add returns : "+res2.toString());
//					
//			logger.debug(" get returns : "+GoogleCalendarReader.getEvents(apiKey, accessTokenType, accessTokenValue, calendarId, urlParameters));
//			String eventId=res2.getString("id");
//			
//			logger.debug(" delete returns : "+GoogleCalendarWriter.deleteEvent(apiKey, accessTokenType, accessTokenValue, calendarId, eventId));
//			logger.debug(" get returns : "+GoogleCalendarReader.getEvents(apiKey, accessTokenType, accessTokenValue, calendarId, urlParameters));
			
			
//			logger.debug(" returns : "+GoogleOpenAuthent.getDeviceCode(clientId, scope));
//			logger.debug(" returns : "+GoogleOpenAuthent.authDevice(clientId, clientSecret, deviceCode));
//			logger.debug(" returns : "+GoogleOpenAuthent.getAccessToken(clientId, clientSecret, refreshToken));
//			logger.debug(" returns : "+GoogleCalendarReader.getAllCalendars(apiKey, accessTokenType, accessTokenValue));
			
//			logger.debug(" returns : "+GoogleCalendarReader.getCalendarDescription(apiKey, accessTokenType, accessTokenValue, calendarId));
			
//			logger.debug(" returns : "+GoogleOpenAuthent.checkAccessToken(scope, "ya29.dQBCkjovp09YVhwAAADD_SnO6EOVgmnv8q13mEI8PxOOYR8_T4hcPJACreXdvA"));
			
			
//			
//			String deviceCodeURL= "https://accounts.google.com/o/oauth2/device/code";
			
			
			//Step One, we get a verification URL and user code that must be used on a browser by the end-user
//			JSONObject deviceCodeAuthentStep1 = httpsPost(deviceCodeURL, null, 
//					"client_id=" + clientID +
//							"&scope="+scope,null
//					);
			
//			if(deviceCodeAuthentStep1 != null && deviceCodeAuthentStep1.has("device-code")) {
				//Step Two, if the end user has entered the user Code, the device code should work
			
//			String deviceCode = "4/n5lQa91OPRXcTPGDQyRqEVHTXRgK";
//				JSONObject deviceCodeAuthentStep2 =  httpsPost("https://accounts.google.com/o/oauth2/token", null,
//						"client_id=" + clientID +
//						"&client_secret="+clientSecret +
//						"&code="+deviceCode+
//						"&grant_type=http://oauth.net/grant_type/device/1.0",
//						null
//						);
			

			//Step 3 : refresh the token if necessary
//			String refreshToken = "1/LrlxD290y1YdNsD90FifCjg6Xp-pd6Pxf2C5VbcOOq8";
//			JSONObject deviceCodeAuthentStep2 =  httpsPost("https://accounts.google.com/o/oauth2/token", null,
//					"client_id=" + clientID +
//					"&client_secret="+clientSecret +
//					"&refresh_token="+refreshToken+
//					"&grant_type=refresh_token",
//					null);
			
			
			
			//Step 4: use (is access token is still valid
//			Map<String, String> props= new HashMap<String, String>();
//			props.put("Authorization", "Bearer "+"ya29.dACqe9EQ_VS46iEAAAB2ZYQxJrPUmBli2jkuh0_jKP-yx6ekuXrvUXt4AKD4FSqZI2Cmxlv7aqFU-dCi2R0");
//			
//			Map<String, String> urlParams= new HashMap<String, String>();
//			String apiKey = "AIzaSyASWRbL70yXR0rRJHNMUV95oCU9hxb-U_s";
//			urlParams.put("key", apiKey);
//			SimpleDateFormat currentDate=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//			urlParams.put("timeMin", currentDate.format(Calendar.getInstance().getTime()));
//
//			
//			JSONObject deviceCodeAuthentStep4 =  httpsGet("https://www.googleapis.com/calendar/v3/calendars/smarthome.inria%40gmail.com/events", props, urlParams
//						);



		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final String EQUAL = "=";
	public static final String AND = "&";
	public static final String IF = "?";
	public static final String SPACE = " ";
	public static final String SLASH = "/";
	public static final String COMMA = ",";
	
	public static final String PARAM_CONTENTTYPE = "Content-Type";
	public static final String CONTENTTYPE_JSON = "application/json;charset=UTF-8";	


	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String DELETE = "DELETE";
	public static final int RESP_200 = 200;
	public static final int RESP_300 = 300;

	
	/**
	 * Build an HTTPS Connection with the specified parameter
	 * @param url is a valid URL of a REST service using https protocol (as google WebServices)
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param Method one of HTTP Methods, GET, POST, PUT, DELETE, ...
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return a valid https connection or null (if parameters incorrect or no network) 
	 */
	private static HttpsURLConnection httpsRequest(String url, 
			Map<String,String> requestProperties, String Method, Map<String,String> urlParameters ) {
		try {
			
			if(urlParameters != null && urlParameters.size()>0) {
				boolean first=true;
				for (String key:urlParameters.keySet()) {
					if(first) {
						url+=IF;
						first = false;
					} else {
						url+=AND;
					}
					url+=key+"="+URLEncoder.encode(urlParameters.get(key),DEFAULT_ENCODING);
				}
			}
			
			logger.debug("httpsRequest(URL url: " + url + ", ...)");
			HttpsURLConnection httpsConnection = null;
			httpsConnection = (HttpsURLConnection) new URL(url).openConnection();
			logger.debug("httpsRequest(...), url connection opened successfully");

			httpsConnection.setRequestMethod(Method);
			if(requestProperties!= null) {
				for(String key:requestProperties.keySet()) {
					httpsConnection.addRequestProperty(key, requestProperties.get(key));
				}
			}
			return httpsConnection;
		}catch (Exception exc) {
			logger.error("Exception occured during creation of https connection : "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Send an https GET to a REST webservice,
	 * expecting JSON in the response  (as google WebServices)
	 * @param url is a valid URL of a REST service using https protocol (as google WebServices)
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return A JSONObject if the connection with the webservice was successfull
	 */
	public static JSONObject httpsGet(String url, 
			Map<String,String> requestProperties, Map<String,String> urlParameters )  {

		try {

			HttpsURLConnection httpsConnection = httpsRequest(url, requestProperties, GET, urlParameters);

			logger.debug("\nSending 'GET' request to URL : " + url);
			int responseCode = httpsConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode < RESP_200
					|| responseCode>=RESP_300) {
				logger.warn("HTPP Response not 200 OK, returning null");
				return null;
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(httpsConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			logger.debug("httpsGet(...) returning "+response);
			return new JSONObject(response.toString());
		} catch (Exception exc) {
			logger.error("Exception occured during https get : "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Send an https POST to a REST webservice,
	 * expecting JSON in the response  (as google WebServices)
	 * @param url is a valid URL of a REST service using https protocol (as google WebServices)
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param requestContent the content payload of the POST as a String
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return A JSONObject if the connection with the webservice was successfull
	 */
	public static JSONObject httpsPost(String url, 
			Map<String,String> requestProperties, byte[] requestContent, Map<String,String> urlParameters )  {

		try {
			HttpsURLConnection httpsConnection = httpsRequest(url, requestProperties, POST, urlParameters);

			httpsConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(httpsConnection.getOutputStream());
			wr.write(requestContent);
			
			wr.flush();
			wr.close();

			logger.debug("\nSending 'POST' request to URL : " + httpsConnection.getURL().toString());
			int responseCode = httpsConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode < RESP_200
					|| responseCode>=RESP_300) {
				logger.warn("HTPP Response not 200 OK, returning null, message : {}", httpsConnection.getResponseMessage());
				return null;
			}


			BufferedReader in = new BufferedReader(
					new InputStreamReader(httpsConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			logger.debug("httpsPost(...) returning "+response);
			return new JSONObject(response.toString());
		} catch (Exception exc) {
			logger.error("Exception occured during https post : "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Send an https DELETE to a REST webservice,
	 * expecting JSON in the response  (as google WebServices)
	 * @param url is a valid URL of a REST service using https protocol (as google WebServices)
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return A JSONObject if the connection with the webservice was successfull
	 */
	public static String httpsDelete(String url, 
			Map<String,String> requestProperties, Map<String,String> urlParameters )  {

		try {

			HttpsURLConnection httpsConnection = httpsRequest(url, requestProperties, DELETE, urlParameters);

			logger.debug("\nSending 'DELETE' request to URL : " + url);
			int responseCode = httpsConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode < RESP_200
					|| responseCode>=RESP_300) {
				logger.warn("HTPP Response not 200 OK, returning null");
				return null;
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(httpsConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			logger.debug("httpsGet(...) returning "+response);

			return response.toString();
		} catch (Exception exc) {
			logger.error("Exception occured during https delete : "+exc.getMessage());
			return null;
		}
	}
	
}
