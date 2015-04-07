/**
 * 
 */
package appsgate.lig.tv.pace;

import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import appsgate.lig.tv.spec.TVFactory;

/**
 * @author thibaud
 *
 */
public class PaceTVFactoryImpl extends TimerTask implements TVFactory{
	
	BundleContext context;
	Instance mySelf;
	
	public final static String TV_HOSTNAME = "pace.tv.hostname";
	public final static String TV_PORT = "pace.tv.port";
	public final static String TV_PATH = "pace.tv.servicepath";
	public final static String TV_PERIOD = "pace.tv.period";

	public final static String DEFAULT_HOSTNAME = "localhost";
	public final static int DEFAULT_HTTPPORT = 80;

	// if no period provided the service polls every 10 minutes
	public final static long DEFAULT_PERIOD = 10 * 60 * 1000; 
	
	String hostname;
	String path;
	int port;
	long period;
	
	String currentServiceId = null;
	

	private static Logger logger = LoggerFactory
			.getLogger(PaceTVFactoryImpl.class);

	
	public PaceTVFactoryImpl(BundleContext context) {
		this.context = context;
	}
	
	public void start(Instance mySelf) {
		logger.trace("start()");
		this.mySelf = mySelf;

		String sPeriod = context.getProperty(TV_PERIOD);
		if(sPeriod == null) {
			logger.info("No polling period specified, using default value : "+DEFAULT_PERIOD);
			period = DEFAULT_PERIOD;
		} else try{
			period = Long.parseLong(sPeriod);
		} catch(NumberFormatException exc) {
			logger.info("Period is not a valid number, using default value : "+DEFAULT_PERIOD);
			period=DEFAULT_PERIOD;
		}

		hostname = context.getProperty(TV_HOSTNAME);
		if(hostname == null) {
			logger.info("No hostname specified, using default value : "+DEFAULT_HOSTNAME);
			hostname = DEFAULT_HOSTNAME;
		}
			
		String sPort = context.getProperty(TV_PORT);
		if(sPort == null) {
			logger.info("No port specified");
		}
		
		try{
			port = Integer.parseInt(sPort);
			if (port<=0 && port > 65535 ) {
				throw new NumberFormatException("Port is not a valid number");
			}
		} catch(NumberFormatException exc) {
			logger.info("Port is not a valid number, using default value : "+DEFAULT_HTTPPORT);
			port=DEFAULT_HTTPPORT;
		}
		
		path = context.getProperty(TV_PATH);
		if(path == null) {
			logger.info("No path specified, using hostname:port as URL ");
		}		

		createTVInstance(hostname, port, path);		

		Timer timer = new Timer();
		timer.schedule(this,period, period); // Try to recreate the service each  polling period
	}

	@Override
	public String createTVInstance(String hostname, int port,String path) {
		logger.trace("createTVInstance(String hostname : "+hostname
				+", int port ="+port
				+", String path ="+path
				+"), check and try to recreates the instance");
		Instance inst = null;
		
		if(!PaceTVImpl.checkConfiguration(hostname, port, path)) {
			logger.trace("createTVInstance(...), TV not available");
			logger.trace("createTVInstance(...), removing apam service with service id : "+currentServiceId
					+ ", if null removing all instances of TV service");
			removeTVInstance(currentServiceId);
			return null;
		}
		
		if(currentServiceId != null) {
			logger.trace("createTVInstance(...), TV instance is working and already an instance, with service Id "
					+currentServiceId+", keeping the service alive");
			return null;
		}

		try {
			logger.trace("createTVInstance(...), no TV instance referenced, creating a new one");
			
			Implementation tvImpl = CST.apamResolver.findImplByName(null,
					PaceTVImpl.IMPL_NAME);

			Map<String, String> configuration = new Hashtable<String, String>();
			
			String objectId = PaceTVImpl.IMPL_NAME+"-"+String.valueOf(hostname.hashCode());
			configuration.put("objectId", objectId);

			inst = tvImpl.createInstance(mySelf.getComposite(),
					configuration);
			PaceTVImpl service = (PaceTVImpl) inst
					.getServiceObject();
			service.setConfiguration(hostname, port, path,  this);// If no Exception, service should
												// be OK
			currentServiceId = service.getAbstractObjectId();
			logger.trace("createTVInstance(...), Instance should be created successfully,"
					+ " with service Id : "+currentServiceId);

			return currentServiceId;

		} catch (Exception exc) {
			logger.warn("Exception when creating PaceTVImpl : " + exc.getMessage());
			if(inst != null) {
				((ComponentBrokerImpl) CST.componentBroker).disappearedComponent(inst);
				logger.trace("createTVInstance(...), removed corresponding instance");
			}
			currentServiceId = null;
			return null;
		}
	}

	@Override
	public void removeTVInstance(String serviceId) {
		logger.trace("removeTVInstance(String serviceId : "+serviceId+")");
		Implementation observerImpl = CST.apamResolver.findImplByName(null,
				PaceTVImpl.IMPL_NAME);

		if (observerImpl != null) {
			for (Instance inst : observerImpl.getInsts()) {
				PaceTVImpl service = (PaceTVImpl) inst
						.getServiceObject();
				if(serviceId == null ) {
					logger.trace("destroyTVInstance(...), as serviceId is null"
							+ " destroying all instances including this one with id "+service.getAbstractObjectId());
					((ComponentBrokerImpl) CST.componentBroker)
					.disappearedComponent(inst);	
					currentServiceId = null;
				} else if (serviceId.equals(service.getAbstractObjectId())) {
					logger.trace("destroyTVInstance(...), found serviceId "+service.getAbstractObjectId()
							+", removing it");
					((ComponentBrokerImpl) CST.componentBroker)
					.disappearedComponent(inst);
					currentServiceId = null;
				}
			}	
		}
	}

	@Override
	public void run() {
		createTVInstance(hostname, port, path);		
	}

	
}
