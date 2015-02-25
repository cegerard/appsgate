package appsgate.lig.tts.yakitome.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {
	
	public static final String DEFAULT_ENCODING = "UTF-8";	
	public static final String GET = "GET";
	public static final String POST = "POST";	
	public static final int RESP_200 = 200;
	
	
	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	/**
	 * Send an https POST to a REST webservice,
	 * @param url is a valid URL of a REST service using https protocol
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @param payload of the POST request
	 * @return the response if the connection with the webservice was successfull
	 */
	public static String sendHttpsPost(String url, 
			Map<String,String> requestProperties, Map<String,String> urlParameters, byte[] payload)  {

		try {
			HttpsURLConnection httpConnection = httpsRequest(url, requestProperties, POST, urlParameters);
			
			httpConnection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
			if(payload!= null && payload.length>0)
				wr.write(payload);
			
			wr.flush();
			wr.close();
			

			logger.debug("\nSending 'POST' request to URL : " + url);
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
			logger.debug("sendHttpPost(...) successfull, returning "+response);
			return response.toString();
		} catch (Exception exc) {
			logger.error("Exception occured during http post : "+exc.getMessage());
			return null;
		}
	}
	
	/**
	 * Send an http GET to a REST webservice,
	 * @param url is a valid URL of a REST service using http protocol
	 * @param requestProperties additional parameters name=value to send in the request
	 * @param urlParameters parameters to add in the URL (those will be re-encoded)
	 * @return the response if the connection with the webservice was successfull
	 */
	public static String sendHttpsGet(String url, 
			Map<String,String> requestProperties, Map<String,String> urlParameters)  {

		try {
			HttpsURLConnection httpsConnection = httpsRequest(url, requestProperties, GET, urlParameters);

			logger.debug("\nSending 'GET' request to URL : " + url);
			int responseCode = httpsConnection.getResponseCode();
			logger.debug("Response Code : " + responseCode);
			if(responseCode != RESP_200) {
				logger.warn("HTPP Response not 200 OK, returning null. Response was : "+responseCode);
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
			logger.debug("sendHttpGet(...) successfull, returning "+response);
			return response.toString();
		} catch (Exception exc) {
			logger.error("Exception occured during https get : "+exc.getMessage());
			return null;
		}
	}		

	
	
	/**
	 * Helper method to build an HTTP Connection
	 */
	private static HttpsURLConnection httpsRequest(String url, 
			Map<String,String> requestProperties, String Method, Map<String,String> urlParameters ) {
		
		if(!testURLTimeout(url, 3000)) {
			logger.error("Exception occured during creation of https connection, timeout for url :"+url);
			return null;
		}
		
		try {
			
			if(urlParameters != null && urlParameters.size()>0) {
				for (String key:urlParameters.keySet()) {
					url+=key+URLEncoder.encode(urlParameters.get(key),DEFAULT_ENCODING);
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
