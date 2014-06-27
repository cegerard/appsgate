package appsgate.lig.persistence;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.UnknownHostException;


/**
 * this one does all the checks (if connection to the server is ok, if mongo client can be retrieved, ...)
 * Created by thibaud on 25/06/2014.
 */
public class MongoClientCreationThread implements Runnable {

    static private final Logger logger = LoggerFactory
            .getLogger(MongoClientCreationThread.class);

    private String dbHost;
    private Integer dbPort;
    private Integer dbTimeOut;

    private boolean isAlive = true;

    private MongoDBConfiguration myConfiguration = null;


    public MongoClientCreationThread(String dbHost, int dbPort, int dbTimeOut) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbTimeOut = dbTimeOut;
    }


    @Override
    public void run() {
        String instName = null;
        while(isAlive) {
            if(myConfiguration == null || !myConfiguration.isValid()) {
                destroyInstance(instName);
                instName = null;
                instName = createInstance(null);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private MongoClient createMongoClient() {
        try {
            logger.debug("Testing Mongo socket address : " +dbHost + " / " + dbPort);

            Socket testingSocket = new Socket(dbHost, dbPort);
            testingSocket.close();
        } catch (Exception e) {
            logger.warn("Cannot connect to "+dbHost+" / "+dbPort
                    +", No MongoDB running or wrong configuration : "+e.getMessage());
            return null;
        }

        try {
            logger.debug("Creating new mongo client");
            MongoClient mongoClient = null;

            MongoClientOptions.Builder options = new MongoClientOptions.Builder();
            options.connectTimeout(dbTimeOut);
            mongoClient = new MongoClient(new ServerAddress(dbHost, dbPort),
                    options.build());

            return mongoClient;

        } catch (UnknownHostException exception) {
            logger.error("Cannot create MongoDB Client "
                    + exception.getStackTrace());
            return null;
        }
    }


    private String createInstance(String name) {
        logger.debug("createInstance(String name = "+name+" )");
        if (name == null) {
            // no instance exists, create all, the configuration and the instance
            myConfiguration = null;

            MongoClient mongoClient = createMongoClient();

            if (mongoClient != null && MongoDBConfiguration.checkMongoClient(mongoClient)) {
                logger.debug("Mongo client works properly");
                Implementation impl = CST.apamResolver.findImplByName(null, MongoDBConfiguration.IMPL_NAME);

                Instance inst = impl.createInstance(null, null);
                myConfiguration = (MongoDBConfiguration) inst.getServiceObject();
                myConfiguration.setConfiguration(dbHost, dbPort, dbTimeOut, mongoClient);
                logger.debug("Instance and MongoDBConfiguration created : "+inst.getName());

                return inst.getName();

            } else {
                logger.debug("Error when creating instance of mongoDBConfiguration");
                return null;
            }
        } else if (CST.componentBroker.getInst(name) != null) {
            // An instance with this name exists, check if it works properly
           // if not we destroy it
            Instance inst = CST.componentBroker.getInst(name);
            myConfiguration = (MongoDBConfiguration) CST.componentBroker.getInst(name).getServiceObject();
            if (!myConfiguration.isValid()) {
                logger.debug("Error during creation, removing the Instance and the configuration");
                destroyInstance(inst.getName());
                myConfiguration = null;
            }
        }
        return null;
    }

    private void destroyInstance(String name) {
        if (name != null) {
            ((ComponentBrokerImpl) CST.componentBroker).disappearedComponent(name);
            myConfiguration = null;
            logger.debug("Instance of MongoDBConfiguration destroyed");
        }
    }
}
