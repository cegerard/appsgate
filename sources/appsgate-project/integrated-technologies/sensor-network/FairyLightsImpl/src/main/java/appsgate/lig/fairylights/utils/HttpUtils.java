package appsgate.lig.fairylights.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {
	
	public static final String DEFAULT_ENCODING = "UTF-8";	
	public static final String GET = "GET";
	public static final String POST = "POST";		
	public static final String PUT = "PUT";		
	public static final int RESP_200 = 200;
	
	
	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	/**
	 * Send an http PUT to a REST webservice,
	 * @param url is a valid URL of a REST service using http protocol
	 * @param payload of the PUT request
	 * @return the response if the connection with the webservice was successfull
	 */
	public static String sendHttpsPut(String url, 
			byte[] payload)  {

		try {
			Map<String, String> requestProperties = new HashMap<String, String>();
			requestProperties.put("Content-type",
					"application/json");
			
			HttpURLConnection httpConnection = httpRequest(url, requestProperties, PUT, null);
			
			httpConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
			if(payload!= null && payload.length>0)
				wr.write(payload);
			
			wr.flush();
			wr.close();
			

			logger.debug("\nSending '{}' request to URL : {}", PUT, url);
			int responseCode = httpConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode != RESP_200) {
				logger.warn("HTTP Response not 200 OK, returning null. Response was : "+responseCode);
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
			logger.debug("sendHttpPut(...) successfull, returning "+response);
			return response.toString();
		} catch (Exception exc) {
			logger.error("Exception occured during http put : "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Send an http GET to a REST webservice,
	 * @param url is a valid URL of a REST service using http protocol
	 * @return the response if the connection with the webservice was successfull
	 */
	public static String sendHttpGet(String url)  {

		try {
			HttpURLConnection httpConnection = httpRequest(url, null, GET, null);

			logger.debug("\nSending '{}' request to URL : {}",GET,url);
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
		
		if(!testURLTimeout(url, 3000)) {
			logger.error("Exception occured during creation of http connection, timeout for url :"+url);
			return null;
		}
		
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
	
	
	/**
	 * Testing if URL is reachable (if host and port responds correctly)
	 * @param url the url to test
	 * @param timeout a specific timeout value in milliseconds
	 * @return
	 */
	public static boolean testURLTimeout(String stringUrl, int timeout) {
		logger.trace("testURLTimeout(URL url : "+stringUrl+")");

		if(stringUrl == null ) {
			logger.warn("testURLTimeout(...), url is null");
			return false;
		}
		
		try {
			URL url = new URL(stringUrl);
			
			
			String host = url.getHost();
			int port = url.getPort();

			if (port == -1) // Using default HTTP Port
				port = 80;
			
			Socket testSocket  = new Socket();
			testSocket.connect(new InetSocketAddress(host, port), timeout);
			logger.trace("testURLTimeout(...), socket successfully connected");
			testSocket.close();
			return true;
			
		} catch (SocketTimeoutException e) {
			logger.warn("testURLTimeout(...), SocketTimeoutException, "+e.getMessage());
			return false;
		} catch (UnknownHostException e) {
			logger.warn("testURLTimeout(...), UnknownHostException, "+e.getMessage());
			return false;
		} catch (IOException e) {
			logger.warn("testURLTimeout(...), IOException, "+e.getMessage());
			return false;
		} catch (Exception e) {
			logger.warn("testURLTimeout(...), Exception, "+e.getMessage());
			return false;
		}	}

}
