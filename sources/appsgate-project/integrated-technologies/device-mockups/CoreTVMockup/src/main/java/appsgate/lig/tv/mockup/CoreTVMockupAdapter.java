package appsgate.lig.tv.mockup;


import org.osgi.framework.*;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * This is a mockup of the TV WebService
 * (allowing to change TV Channel, play/pause/stop, or show a notification message).
 */
public class CoreTVMockupAdapter {

	/**
	 * 
	 * static class logger member
	 */
	private final static Logger logger = LoggerFactory
			.getLogger(CoreTVMockupAdapter.class);
	
	public static final String SERVLET_PATH = "/TV-Mockup";
	public static final String MOCKUP_REQUEST = "mock-status";

	/**
	 * HTTP service dependency resolve by iPojo. Allow to register HTML
	 * resources to the Felix HTTP server
	 */
	private HttpService httpService;


	private final BundleContext context;


	Object lock = new Object();

	/**
	 * Default constructor 
	 * 
	 */
	public CoreTVMockupAdapter(BundleContext context) {
		logger.debug("CoreTVMockup(BundleContext context : " + context);
		this.context = context;
		logger.info("CoreTVMockup instanciated");
	}

	/**
	 * Called by iPojo when HTTP Service is found
	 */
	public void registerHttp() {
		logger.debug("registerHttp()");

		if (httpService != null) {
			final HttpContext httpContext = httpService
					.createDefaultHttpContext();
			final Dictionary<String, String> initParams = new Hashtable<String, String>();
			initParams.put("from", "HttpService");
			try {
				httpService.registerServlet(SERVLET_PATH, new TVMockupServlet(), initParams, httpContext);
//				httpService.registerResources(SERVLET_PATH, "/WEB/TV-Mockup",
//						httpContext);
//				logger.debug("Registered URL : "
//						+ httpContext.getResource("/WEB/TV-Mockup"));
//				logger.info("TV-Mockup HTML pages registered.");
			} catch (Exception ex) {
				logger.error("Exception occured : "+ex.getMessage());
			}
		}

	}
	
	/**
	 * Called by iPojo when HTTP Service is lost
	 */
	public void unregisterHttp() {
		logger.debug("unregisterHttp()");
		
		httpService.unregister(SERVLET_PATH);
		logger.info("CoreTVMockup has been stopped.");
	}


}
