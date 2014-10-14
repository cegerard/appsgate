/**
 * 
 */
package appsgate.lig.tv.pace;

import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import fr.imag.adele.apam.util.Util;
import appsgate.lig.tv.spec.TVFactory;

/**
 * @author thibaud
 *
 */
public class PaceTVFactoryImpl implements TVFactory{
	
	BundleContext context;
	Instance mySelf;
	
	public final static String TV_HOSTNAME = "pace.tv.hostname";
	public final static String TV_PORT = "pace.tv.port";

	public final static String DEFAULT_HOSTNAME = "localhost";
	public final static int DEFAULT_HTTPPORT = 80;
	
	public String hostname;
	public int port;
	

	private static Logger logger = LoggerFactory
			.getLogger(PaceTVFactoryImpl.class);

	
	public PaceTVFactoryImpl(BundleContext context) {
		this.context = context;
	}
	
	public void start(Instance mySelf) {
		logger.trace("start()");
		this.mySelf = mySelf;
		
		
		// First version, this one is not dynamic, the existing http service is not checked
		// TODO , there should be a discovery Thread, checking with a basic HTTP Request
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
		
		createTVInstance(hostname, port);		
	}

	@Override
	public String createTVInstance(String hostname, int port) {
		logger.trace("createTVInstance(String hostname : "+hostname
				+", int port ="+port+")");
		Instance inst = null;

		try {
			Implementation tvImpl = CST.apamResolver.findImplByName(null,
					PaceTVImpl.IMPL_NAME);

			Map<String, String> configuration = new Hashtable<String, String>();

			inst = tvImpl.createInstance(mySelf.getComposite(),
					configuration);
			PaceTVImpl service = (PaceTVImpl) inst
					.getServiceObject();
			service.setConfiguration(hostname, port, this);// If no Exception, service should
												// be OK
			logger.trace("createTVInstance(...), Instance should be created successfully");

			return service.getAbstractObjectId();

		} catch (Exception exc) {
			logger.warn("Exception when creating PaceTVImpl : " + exc.getMessage());
			if(inst != null) {
				((ComponentBrokerImpl) CST.componentBroker).disappearedComponent(inst);
				logger.trace("createTVInstance(...), removed corresponding instance");
			}
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
					
				} else if (serviceId.equals(service.getAbstractObjectId())) {
					logger.trace("destroyTVInstance(...), found serviceId "+service.getAbstractObjectId()
							+", removing it");
					((ComponentBrokerImpl) CST.componentBroker)
					.disappearedComponent(inst);
				}

			}
		}
	}

	
}
