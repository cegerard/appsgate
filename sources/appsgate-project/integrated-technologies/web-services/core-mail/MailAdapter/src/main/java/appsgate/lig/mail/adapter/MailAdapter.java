package appsgate.lig.mail.adapter;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.mail.javamail.MailService;
import appsgate.lig.mail.javamail.connexion.GMailConstants;
import appsgate.lig.mail.javamail.connexion.JavamailConnexion;
import appsgate.lig.mail.javamail.connexion.JavamailConnexionImpl;
import appsgate.lig.mail.javamail.connexion.MailConfiguration;
import appsgate.lig.mail.javamail.connexion.MailConnexionListener;

public class MailAdapter implements MailConnexionListener{
	
	
	Map<String, JavamailConnexion> mailConnexions = new HashMap<String, JavamailConnexion>();

	private static Logger logger = LoggerFactory.getLogger(MailAdapter.class);
	public static final String CONFIGURATION_FILE="mail.configuration.file";
	
    public void createMailInst(MailConfiguration config) {
    	logger.trace("createMailInst(MailConfiguration config : "+config.toString()+")");
    	JavamailConnexion connexion;

        if(GMailConstants.PROVIDER.equals(config.getProperty(MailConfiguration.PROVIDER_PARAM))) {
        	logger.trace("This is a GMail account, using predefined configuration properties (except for login and password)");
        	GMailConstants gmailConfig = new GMailConstants();
        	
        	gmailConfig.put(MailConfiguration.USER, config.getProperty(MailConfiguration.USER));
        	gmailConfig.put(MailConfiguration.FROM, config.getProperty(MailConfiguration.USER));
        	gmailConfig.put(MailConfiguration.PASSWORD, config.getProperty(MailConfiguration.PASSWORD));
    		
        	connexion=new JavamailConnexionImpl(gmailConfig, this);
        } else {
        	logger.trace("Using specific configuration properties");
        	connexion=new JavamailConnexionImpl(config, this);
            // Set account is useless as the properties (USER, FROM and PASSWORD) should be already embedded in the prop file 
        }    	
    	
    	mailConnexions.put(String.valueOf(connexion.hashCode()), connexion);
    	
    	connexion.start();
    }
    	
    
    public static void removeMailInst(String mailUser) {
    	// TODO (but for the moment, we have a single mail for each appsgate machine)
    }

    
    BundleContext context = null;    
	public MailAdapter(BundleContext context) {
		this.context=context;
		singleton = this;
	}
	
	
	static MailAdapter singleton;
	
	static public void loadAndCreateMail(String fileName) {
		try {	
			logger.trace("Configuration file for MailAdapter: "+fileName);
			MailConfiguration myConfig = loadFromFile(fileName);
			if(myConfig != null) {
				singleton.createMailInst(myConfig);
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

	@Override
	public void connexionAvailable(JavamailConnexion connexion) {
		logger.trace("connexionAvailable(JavamailConnexion connexion (hashcode) : "+(connexion==null?null:connexion.hashCode()) +")");
		
        try {
            Implementation mailImpl = CST.apamResolver.findImplByName(null,"MailService");

            Map<String,String> configuration = new Hashtable<String,String>();


            MailService impl = (MailService) mailImpl.createInstance(null, configuration).getServiceObject();
            impl.setConnexion(connexion);
            
        } catch( Exception exc) {
            logger.error("Exception when creating Mail for "+" : "+exc.getMessage());
        }
		
	}

	@Override
	public void connexionBroken(JavamailConnexion connexion) {
		logger.trace("connexionAvailable(JavamailConnexion connexion (hashcode) : "+(connexion==null?null:connexion.hashCode()) +")");

		// TODO Remove the instance with the corresponding hashcode
		
	}
			
}
