package appsgate.lig.google.helpers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleOpenAuthent {
	
	static Logger logger = LoggerFactory.getLogger(GoogleOpenAuthent.class);

	
	public static final String PARAM_CLIENTID = "client_id";
	public static final String PARAM_CLIENTSECRET = "client_secret";
	public static final String PARAM_CODE = "code";
	public static final String PARAM_SCOPE = "scope";
	public static final String PARAM_GRANTTYPE = "grant_type";
	public static final String PARAM_REFRESHTOKEN = "refresh_token";
	public static final String PARAM_ACCESSTOKEN = "access_token";	
	public final static String PARAM_AUTH = "Authorization";
	public final static String PARAM_APIKEY = "key";	
	
	public final static String RESP_ERROR = "error";

	public final static String RESP_VERIFICATIONURL = "verification_url";
	public final static String RESP_USERCODE = "user_code";
	public final static String RESP_DEVICECODE = "device_code";
	public final static String RESP_EXPIREIN = "expires_in";
	public final static String RESP_INTERVAL = "interval";	
	public final static String RESP_TOKENTYPE = "token_type";	
	public final static String RESP_REFRESHTOKEN = PARAM_REFRESHTOKEN;	
	public final static String RESP_ACCESSTOKEN = PARAM_ACCESSTOKEN;	
	

	
	public static final String GRANT_DEVICE = "http://oauth.net/grant_type/device/1.0";
	public static final String GRANT_REFRESH = PARAM_REFRESHTOKEN;

	public static final String oauthDeviceURL= "https://accounts.google.com/o/oauth2/device/code";
	public static final String oauthTokenURL= "https://accounts.google.com/o/oauth2/token";
	public static final String oauthTokenInfoURL= "https://www.googleapis.com/oauth2/v1/tokeninfo";
	
	/**
	 * Used to get a verification URL and an user code for the end-user to authorize the device (i.e : this application)
	 * Gets a device code to be used with authDevice...) 
	 * @param clientId is obtained from the Google Developers console
	 * @param scopes the scope (i.e: the google apps to authorize)
	 * @return null if the  request is incorrect or a JSONObject containing the verification URL,
	 * the user Code and the device Code 
	 */
	public static JSONObject getDeviceCode(String clientId, String scopes) {
		// No logs because we may not want those parameter values clear
		
		String requestContent = PARAM_CLIENTID + GoogleHTTPRequest.EQUAL + clientId
				+ GoogleHTTPRequest.AND
				+ PARAM_SCOPE +GoogleHTTPRequest.EQUAL + scopes;
		
		return GoogleHTTPRequest.httpsPost(oauthDeviceURL,
				null, 
				requestContent,
				null
				);		
	}
	
	/**
	 * Used to get an access token and a refresh token, once the user have authorized a device
	 * (using a browser with the verification URL and the user code obtained with getDeviceCode)
	 * @param clientId is obtained from the Google Developers console
	 * @param clientSecret is obtained from the Google Developers console
	 * @param deviceCode obtained from getDeviceCode(...)
	 * @return null if the  request is incorrect or a JSONObject containing the error cause
	 * (if the user haven't authorized the application yet, or if the request is repeated in a short interval) 
	 * or a JSON object containing a valid access key and a refresh token
	 */
	public static JSONObject authDevice(String clientId, String clientSecret, String deviceCode) {
		// No logs because we may not want those parameter values clear

		String requestContent = PARAM_CLIENTID + GoogleHTTPRequest.EQUAL + clientId
				+ GoogleHTTPRequest.AND
				+ PARAM_CLIENTSECRET +GoogleHTTPRequest.EQUAL + clientSecret
				+ GoogleHTTPRequest.AND
				+ PARAM_CODE +GoogleHTTPRequest.EQUAL + deviceCode
				+ GoogleHTTPRequest.AND
				+ PARAM_GRANTTYPE +GoogleHTTPRequest.EQUAL + GRANT_DEVICE
				;

		return GoogleHTTPRequest.httpsPost(oauthTokenURL,
				null,
				requestContent,
				null);
	}	
	
	/**
	 * Once a refreshToken is obtained, it can be used to get an access token to use google web services.
	 * @param clientId is obtained from the Google Developers console
	 * @param clientSecret is obtained from the Google Developers console
	 * @param refreshToken is obtained from the previous steps of authentication
	 * @return null if the request is incorrect or a JSONObject holding the access_code
	 */
	public static JSONObject getAccessToken(String clientId, String clientSecret, String refreshToken) {
		// No logs because we may not want those parameter values clear		
		
		String requestContent = PARAM_CLIENTID + GoogleHTTPRequest.EQUAL + clientId
				+ GoogleHTTPRequest.AND
				+ PARAM_CLIENTSECRET +GoogleHTTPRequest.EQUAL + clientSecret
				+ GoogleHTTPRequest.AND
				+ PARAM_REFRESHTOKEN +GoogleHTTPRequest.EQUAL + refreshToken
				+ GoogleHTTPRequest.AND
				+ PARAM_GRANTTYPE +GoogleHTTPRequest.EQUAL + GRANT_REFRESH
				;
		
		return GoogleHTTPRequest.httpsPost(oauthTokenURL,
				null,
				requestContent,
				null);
	}

	
	public static boolean checkAccessToken(String scope,
			String accessTokenValue) {
		
		Map<String, String> urlParameters= new HashMap<String, String>();
		urlParameters.put(PARAM_ACCESSTOKEN, accessTokenValue);
		
				
		JSONObject result = GoogleHTTPRequest.httpsGet(oauthTokenInfoURL, null, urlParameters);
		if(result==null || result.opt(RESP_ERROR) != null) {
			logger.info("access token is not valid");
			return false;
		}
		
		if(result.get(PARAM_SCOPE) != null) {
			if(result.getString(PARAM_SCOPE).contains(scope)) {
				logger.info("access token contains the scope");
				// TODO Should check audience (?) and low expiration date
				return true;
			}
		}
		logger.info("access token is not valid for the scope");
		return false;
	}
	

}
