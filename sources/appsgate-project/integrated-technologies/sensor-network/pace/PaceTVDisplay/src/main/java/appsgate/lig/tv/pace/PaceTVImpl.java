/**
 * 
 */
package appsgate.lig.tv.pace;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.tv.spec.CoreTVSpec;
import appsgate.lig.tv.spec.TVFactory;

/**
 * @author thibaud
 *
 */
public class PaceTVImpl extends CoreObjectBehavior implements CoreTVSpec, CoreObjectSpec{
	
	static Logger logger = LoggerFactory.getLogger(PaceTVImpl.class);

	/*
	 * Object information
	 */
	private String serviceId;
	private String userType;
	private String status;
	private String pictureId;	
	
	public final static String IMPL_NAME = "PaceTVImpl";
	

	public PaceTVImpl() {
		serviceUrl = PROTOCOL+DEFAULT_HOSTNAME+PORT_SEPARATOR+DEFAULT_HTTPPORT+VIDEO_SERVICE;
		serviceId = IMPL_NAME+"-"+DEFAULT_HOSTNAME;
	}
	
	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.parseInt(status);
	}

	@Override
	public String getPictureId() {
		return pictureId;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();

		descr.put("id", serviceId);
		descr.put("type", userType); // 124 for TV
		descr.put("status", status);

		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
		this.pictureId=pictureId;
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}
	
	public final static String DEFAULT_HOSTNAME = "localhost";
	public final static int DEFAULT_HTTPPORT = 80;
	String hostname;
	int port;
	String path;
	TVFactory factory;
	String serviceUrl;
	
	public static final String DEFAULT_ENCODING = "UTF-8";	
	final static String PROTOCOL = "http://";
	final static String PORT_SEPARATOR = ":";

	/**
	 * WebService Name
	 */
	public final static String VIDEO = "video";
	public final static String VIDEO_SERVICE = "/"+VIDEO+"?";
	
	public final static String SYSTEM = "system";
	public final static String SYSTEM_SERVICE = "/"+SYSTEM+"?";
	
	/**
	 * TV Web service command, first parameter of the REST Command
	 */
	public final static String COMMAND_PARAM = "command";
	public final static String COMMAND_PARAM_NAME = COMMAND_PARAM+"=";

	
	public final static String ID_PARAM = "id";
	public final static String ID_PARAM_NAME = "&"+ID_PARAM+"=";
	public final static String SCREEN_PARAM = "screen";
	public final static String SCREEN_PARAM_NAME = "&"+SCREEN_PARAM+"=";
	public final static String SENDER_PARAM = "sender";
	public final static String SENDER_PARAM_NAME = "&"+SENDER_PARAM+"=";
	public final static String MESSAGE_PARAM = "message";
	public final static String MESSAGE_PARAM_NAME = "&"+MESSAGE_PARAM+"=";
	
	public final static String COMMAND_CHANNELUP = "channelUp";
	public final static String COMMAND_CHANNELDOWN = "channelDown";
	public final static String COMMAND_RESUME = "resume";
	public final static String COMMAND_STOP = "stop";
	public final static String COMMAND_PAUSE = "pause";
	public final static String COMMAND_RESIZE = "resize";
	public final static String COMMAND_NOTIFY = "notify";
	
	public final static String COMMAND_ISALIVE = "isAlive";
	
	
	public final static String COMMA_SEPARATOR = ",";
	
	public static final String GET = "GET";
	public static final int RESP_200 = 200;
	

	public NotificationMsg fireNotificationMessage(String varName, String oldValue, String newValue) {
		return new CoreNotificationMsg(varName, oldValue, newValue, this);
	}

	@Override
	public void notify(int id, String sender, String message) {
		logger.trace("notify(int id : "+id
				+", String sender : "+sender
				+", String message : "+message
				+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_NOTIFY);
		urlParameters.put(SENDER_PARAM_NAME, sender);
		urlParameters.put(MESSAGE_PARAM_NAME, message);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void channelUp(int id) {
		logger.trace("channelUp(int id : "+id+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_CHANNELUP);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void channelDown(int id) {
		logger.trace("channelDown(int id : "+id+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_CHANNELDOWN);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void resume(int id) {
		logger.trace("resume(int id : "+id+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_RESUME);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void stop(int id) {
		logger.trace("stop(int id : "+id+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_STOP);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void pause(int id) {
		logger.trace("pause(int id : "+id+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_PAUSE);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);
	}

	@Override
	public void resize(int id, int x, int y, int width, int height) {
		logger.trace("resize(int id : "+id
				+", int x : "+x
				+", int y : "+y
				+", int width : "+width
				+", int height : "+height
				+")");
		if(!checkConfiguration(hostname, port, path)) {
			logger.warn("TV not available, shutting down the service");
			factory.removeTVInstance(serviceId);
			return;
		}
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_RESIZE);
		urlParameters.put(SCREEN_PARAM_NAME,
				String.valueOf(x)+COMMA_SEPARATOR
				+String.valueOf(y)+COMMA_SEPARATOR
				+String.valueOf(width)+COMMA_SEPARATOR
				+String.valueOf(height)				
				);
		urlParameters.put(ID_PARAM_NAME, String.valueOf(id));
		sendHttpGet(serviceUrl, null, urlParameters);		
	}

	@Override
	public void setConfiguration(String hostname, int port, String path, TVFactory factory) {
		this.hostname = hostname;
		this.port = port;
		this.factory = factory;
		this.path = path;
		
		serviceUrl = PROTOCOL+hostname+PORT_SEPARATOR+port;
		if(path!=null) {
			serviceUrl+=path;
		}
		serviceUrl+=VIDEO_SERVICE;
		serviceId = IMPL_NAME+"-"+hostname;
	}
	
	
	public static boolean checkConfiguration(String hostname, int port, String path) {
		logger.trace("checkConfiguration(String hostname : {}, int port : {} , String path :{})",
				hostname, port, path);

		String tmpURL = PROTOCOL+hostname+PORT_SEPARATOR+port;
		if(path!=null) {
			tmpURL+=path;
		}
		tmpURL+=SYSTEM_SERVICE;
		
		Map<String,String> urlParameters = new LinkedHashMap<String, String>();
		urlParameters.put(COMMAND_PARAM_NAME, COMMAND_ISALIVE);
		String response = sendHttpGet(tmpURL, null, urlParameters);
		
		if (response != null) {
			logger.trace("checkConfiguration(...), response was not null (should be 200 OK) returning true");
			return true;
		}
		
		logger.trace("checkConfiguration(...), response was null (not 200 OK or no response at all) returning false");
		return false;
	}
	
	
	/**
	 * Send an http GET to a REST webservice,
	 * @param url is a valid URL of a REST service using http protocol
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return the response if the connection with the webservice was successfull
	 */
	public static String sendHttpGet(String url, 
			Map<String,String> requestProperties, Map<String,String> urlParameters)  {

		try {
			HttpURLConnection httpConnection = httpRequest(url, requestProperties, GET, urlParameters);

			logger.debug("\nSending 'GET' request to URL : " + url);
			int responseCode = httpConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode != RESP_200) {
				logger.warn("HTPP Response not 200 OK, returning null. Response was : "+responseCode);
				return null;
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			logger.debug("sendHttpGet(...) successfull, returning "+response);
			return response.toString();
		} catch (Exception exc) {
			logger.error("Exception occured during http get : "+exc.getMessage());
			return null;
		}
	}	
	
	/**
	 * Helper method to build an HTTP Connection
	 */
	private static HttpURLConnection httpRequest(String url, 
			Map<String,String> requestProperties, String Method, Map<String,String> urlParameters ) {
		try {
			
			if(urlParameters != null && urlParameters.size()>0) {
				for (String key:urlParameters.keySet()) {
					url+=key+URLEncoder.encode(urlParameters.get(key),DEFAULT_ENCODING);
				}
			}
			
			logger.debug("httpRequest(URL url: " + url + ", ...)");
			HttpURLConnection httpConnection = null;
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			logger.debug("httpRequest(...), url connection opened successfully");

			httpConnection.setRequestMethod(Method);
			if(requestProperties!= null) {
				for(String key:requestProperties.keySet()) {
					httpConnection.addRequestProperty(key, requestProperties.get(key));
				}
			}
			return httpConnection;
		}catch (Exception exc) {
			logger.error("Exception occured during creation of http connection : "+exc.getMessage());
			return null;
		}
	}

}
