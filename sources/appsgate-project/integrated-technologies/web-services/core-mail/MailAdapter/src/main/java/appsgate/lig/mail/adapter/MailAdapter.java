package appsgate.lig.mail.adapter;

import appsgate.lig.mail.javamail.GMailConstants;
import appsgate.lig.mail.javamail.MailConfiguration;
import appsgate.lig.mail.javamail.MailService;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailAdapter {

	private static Logger logger = LoggerFactory.getLogger(MailAdapter.class);
	public static final String CONFIGURATION_FILE="mail.configuration.file";
	
    public static void createMailInst(MailConfiguration config) {
    	logger.trace("createMailInst(MailConfiguration config : "+config.toString()+")");
    	
        try {
            Implementation mailImpl = CST.apamResolver.findImplByName(null,"MailService");

            Map<String,String> configuration = new Hashtable<String,String>();


            MailService impl = (MailService) mailImpl.createInstance(null, configuration).getServiceObject();
            if(GMailConstants.PROVIDER.equals(config.getProperty(MailConfiguration.PROVIDER_PARAM))) {
            	logger.trace("This is a GMail account, using predefined configuration properties (except for login and password)");
                impl.setConfiguration(new GMailConstants());
                impl.setAccount(config.getProperty(MailConfiguration.USER), 
                		config.getProperty(MailConfiguration.PASSWORD));
            } else {
            	logger.trace("Using specific configuration properties");
                impl.setConfiguration(config);
                // Set account is useless as the properties (USER, FROM and PASSWORD) should be already embedded in the prop file 
            }
            impl.start();

        } catch( Exception exc) {
            logger.error("Exception when creating Mail for "+" : "+exc.getMessage());
        }

    }
    
    public static void removeMailInst(String mailUser) {
    	// TODO (but for the moment, we have a single mail for each appsgate machine)
    }

    
    BundleContext context = null;    
	public MailAdapter(BundleContext context) {
		this.context=context;
	}
	
	public static void loadAndCreateMail(String fileName) {
		try {	
			logger.trace("Configuration file for MailAdapter: "+fileName);
			MailConfiguration myConfig = loadFromFile(fileName);
			if(myConfig != null) {
				createMailInst(myConfig);
			} else {
				logger.info(" No valid configuration file, will not create mail service for the moment");
			}
		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}		
	}

	public void start() {
		logger.trace("start()");
		String configFile = context.getProperty(CONFIGURATION_FILE);
		loadAndCreateMail(configFile);
	}
    
	public static MailConfiguration loadFromFile(String fileName) {
		try {
			logger.trace("Try to read configuration file : "+fileName);
			File f = new File(fileName);
			if (f != null && f.isFile()) {
				logger.trace("Configuration file found !");
				MailConfiguration config  = new MailConfiguration();

				FileReader fr = new FileReader(f);
				config.load(fr);
				logger.trace("Mail Configuration file loaded: "+fileName);
				return config;
			} else {
				logger.warn("Configuration file NOT found");
			}
		} catch (Exception exc) {
			logger.error(" Exception occured when reading the configuration file : "+exc.getMessage());
		}
		return null;
	}    
    
    public void stop() {
        
    }
			
}
