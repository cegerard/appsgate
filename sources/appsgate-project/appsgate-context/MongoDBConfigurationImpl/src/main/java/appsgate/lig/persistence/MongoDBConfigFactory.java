package appsgate.lig.persistence;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import fr.imag.adele.apam.ManagerModel;
import fr.imag.adele.apam.impl.CompositeTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class MongoDBConfigFactory {

    static private final Logger logger = LoggerFactory
            .getLogger(MongoDBConfigFactory.class);

    public static final String DBHOST_KEY = "DBHost";

    public static final String DBHOST_DEFAULT = "localhost";

    public static final String DBPORT_KEY = "DBPort";

    public static final int DBPORT_DEFAULT = 27017;

    public static final String DBTIMEOUT_KEY = "DBTimeout";

    public static final int DBTIMEOUT_DEFAULT = 3000;

    public static final String MONGO_CONFIG_URL = "MongoDBConfiguration";

    private String dbHost;
    private Integer dbPort;
    private Integer dbTimeOut;

    private MongoClientCreationThread configThread;

    /**
     * Default constructor
     */
    public MongoDBConfigFactory() {
        this(DBHOST_DEFAULT, DBPORT_DEFAULT, DBTIMEOUT_DEFAULT);
    }

    /**
     * Four ways to configure the DB (each one get priority on its
     * predecessors), (each one can omit parameters, previously setted values
     * will be used) 1 - Use default values (the class constants) 2 - Use this
     * parameterized constructor 3 - Use System Properties 4 - Use values
     * provided as parameters of the apam-instance
     *
     * (in all case we wait for a client to create a configuration before trying
     * to connect)
     */
    public MongoDBConfigFactory(String dbHost, int dbPort, int dbTimeOut) {

        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbTimeOut = dbTimeOut;

        if (getConfigurationProperties()) {
            configThread = new MongoClientCreationThread(this.dbHost,this.dbPort,this.dbTimeOut);
            new Thread(configThread).start();
        }
    }

    public void setFactoryParameters(Properties props) {

        String tmp_host = props.getProperty(DBHOST_KEY);
        if (tmp_host != null && !tmp_host.equals(this.dbHost)) {
            this.dbHost = tmp_host;
            configValueChanged(this.dbHost);
        }

        Integer tmp_port = Integer.parseInt(props.getProperty(DBPORT_KEY));
        if (tmp_port != null && !tmp_port.equals(this.dbPort)) {
            this.dbPort = tmp_port;
            configValueChanged(this.dbPort);
        }

        Integer tmp_timeout = Integer.parseInt(props.getProperty(DBTIMEOUT_KEY));
        if (tmp_timeout != null && !tmp_timeout.equals(this.dbTimeOut)) {
            this.dbTimeOut = tmp_timeout;
            configValueChanged(this.dbTimeOut);

        }

    }

    public void configValueChanged(Object newValue) {
        // Warning the new value should be injected automatically, do not make
        // affectation
        logger.debug("A value has changed, new value : " + newValue);
    }


    public boolean getConfigurationProperties() {

        logger.debug("getConfigurationProperties()");

        ManagerModel model = CompositeTypeImpl.getRootCompositeType().getModel(MONGO_CONFIG_URL);
        if (model != null && model.getURL() != null) {
		/*
		 * Try to load the model from the specified location, as a map of properties
		 */
            Properties configuration = null;
            try {
                configuration = new Properties();
                configuration.load(model.getURL().openStream());
                setFactoryParameters(configuration);
                return true;

            } catch (IOException e) {
                logger.warn("Invalid Configuration Model. Cannot read stream " + model.getURL(), e.getCause());
                return false;
            }
        } else {
            logger.info("Cannot load configuration from properties" );
            return false;
        }
    }



}