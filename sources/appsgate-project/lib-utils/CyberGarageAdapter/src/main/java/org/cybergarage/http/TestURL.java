package org.cybergarage.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import org.cybergarage.util.Debug;

public class TestURL {
	
	public static int DEFAULT_TIMEOUT = 3000;
	
	
	public static boolean testURLTimeout(URL url) {
		return testURLTimeout(url, DEFAULT_TIMEOUT);
	}
	
	
	/**
	 * Testing if URL is reachable (if host and port responds correctly)
	 * @param url the url to test
	 * @param timeout a specific timeout value in milliseconds
	 * @return
	 */
	public static boolean testURLTimeout(URL url, int timeout) {
		Debug.message("TestURL.testURLTimeout(URL url : "+url+")");

		if(url == null ) {
			Debug.warning("TestURL.testURLTimeout(...), url is null");
			return false;
		}
		
		String host = url.getHost();
		int port = url.getPort();

		if (port == -1) // Using default HTTP Port
			port = 80;
		
		try {
			Socket testSocket  = new Socket();
			testSocket.connect(new InetSocketAddress(host, port), timeout);
			Debug.message("TestURL.testURLTimeout(...), socket successfully connected");
			testSocket.close();
			return true;
			
		} catch (SocketTimeoutException e) {
			Debug.warning("TestURL.testURLTimeout(...), SocketTimeoutException, "+e.getMessage());
			return false;
		} catch (UnknownHostException e) {
			Debug.warning("TestURL.testURLTimeout(...), UnknownHostException, "+e.getMessage());
			return false;
		} catch (IOException e) {
			Debug.warning("TestURL.testURLTimeout(...), IOException, "+e.getMessage());
			return false;
		}
	}

}
