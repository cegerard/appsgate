package appsgate.lig.proxy.MQTTAdapter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import java.net.URI;
import org.fusesource.mqtt.client.FutureConnection;

/**
 * Temporary MQTT client for DomiCube message receiving
 *
 * @author Cédric Gérard
 * @version 0.0.5 SNAPSHOT
 * @since April 16, 2014
 *
 */
public class DomiCubeMQTTAdapterImpl {

    /**
     * MQTT broker address
     */
    private String host;

    /**
     * MQTT broker port
     */
    private String port;

    /**
     * MQTT DomiCube face topic
     */
    private String faceTopic;

    /**
     * MQTT DomiCube battery topic
     */
    private String batteryTopic;

    /**
     * MQTT DomiCube dim topic
     */
    private String dimTopic;

    /**
     * MQTT DomiCube old face topic use to unsubscribe when ApAM update the face
     * topic property
     */
    private String oldFaceTopic;

    /**
     * MQTT DomiCube old battery topic use to unsubscribe when ApAM update the
     * battery topic property
     */
    private String oldBatteryTopic;

    /**
     * MQTT DomiCube old dim topic use to unsubscribe when ApAM update the dim
     * topic property
     */
    private String oldDimTopic;

    /**
     * MQTT attribute to initiate the connection
     */
    private MQTT mqtt;

    /**
     * Executor scheduler for MQTT message reception
     */
    private ScheduledExecutorService listenningService;

    /**
     * All schedule task for MQTT broker reception
     */
    private Future<?> scheduledTasks;

    /**
     * Connection member for MQTT broker
     */
    FutureConnection connection;

    /**
     * subscribed topic
     */
    private ArrayList<Topic> localTopic = new ArrayList<Topic>();

    /**
     * All QoS
     */
    private org.fusesource.mqtt.client.Future<byte[]> qoses;

    /**
     * Static class member uses to log what happened in each instances
     */
    private static Logger logger = LoggerFactory.getLogger(DomiCubeMQTTAdapterImpl.class);

    /**
     * Called by ApAM when all dependencies are available
     */
    public void newInst() {
        logger.debug("initializing MQTT DomiCube adapter.");
        try {
            mqtt = new MQTT();
            logger.debug("Trying to connect MQTT broker: " + host + ":" + port);
            mqtt.setHost(host, Integer.valueOf(port));
            connection = mqtt.futureConnection();
            logger.debug("connecting...");
            connection.connect();
            logger.debug("MQTT brocker connection success.");

            localTopic.add(new Topic(faceTopic, QoS.AT_LEAST_ONCE));
            localTopic.add(new Topic(batteryTopic, QoS.AT_LEAST_ONCE));
            localTopic.add(new Topic(dimTopic, QoS.AT_LEAST_ONCE));
            oldFaceTopic = faceTopic;
            oldDimTopic = dimTopic;
            oldBatteryTopic = batteryTopic;

            Object[] objectArray = localTopic.toArray();
            qoses = connection.subscribe(Arrays.copyOf(objectArray, objectArray.length, Topic[].class));
            logger.debug("Topics subscription success.");

            logger.debug("Reception thread intializing...");
            listenningService = Executors.newScheduledThreadPool(1);
            scheduledTasks = listenningService.schedule(new MQTTListeningThread(), 3, TimeUnit.SECONDS);
            logger.debug("Thread initialized.");

        } catch (NumberFormatException e) {
            logger.error("MQTT brocker connection failed.");
            logger.error("Number format exception: " + e.getMessage());
        } catch (URISyntaxException e) {
            logger.error("MQTT brocker connection failed.");
            logger.error("URI syntaxe error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("MQTT brocker connection failed.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Called by ApAM when the bundle become not available
     */
    public void delInst() {

        try {
            logger.debug("Stopping MQTT reception thread...");
            if (listenningService != null) {
                scheduledTasks.cancel(true);
                listenningService.awaitTermination(5, TimeUnit.SECONDS);
            }
            logger.debug("MQTT reception thread stopped.");
        } catch (InterruptedException e) {
            logger.debug("MQTT broker listening service thread crash at termination");
            logger.error(e.getMessage());
        }

        try {
            logger.debug("Closing MQTT connection...");
            connection.disconnect();
            logger.debug("MQTT brocker connection closed.");
        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Called by ApAM when the host value changed
     *
     * @param newHost the new host value.
     */
    public void onHostChanged(String newHost) {
        try {
            delInst();
            newConnection();
        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Called by ApAM when the host value changed
     *
     * @param newHost the new host value.
     */
    public void onPortChanged(String newHost) {
        try {
            delInst();
            newConnection();
        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            logger.error(e.getMessage());
        }
    }

    /**
     * Called by ApAM when the face topic value changed
     *
     * @param faceTopic the new face topic value.
     */
    public void onFaceTopicChanged(String faceTopic) {
        try {
            String[] oldTopics = {oldFaceTopic};
            connection.unsubscribe(oldTopics);

            oldFaceTopic = faceTopic;
            Topic[] topics = {new Topic(faceTopic, QoS.AT_LEAST_ONCE)};
            connection.subscribe(topics);

        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            e.printStackTrace();
        }
    }

    /**
     * Called by ApAM when the battery topic value changed
     *
     * @param batteryTopic the battery topic value.
     */
    public void onBatteryTopicChanged(String batteryTopic) {
        try {
            String[] oldTopics = {oldBatteryTopic};
            connection.unsubscribe(oldTopics);

            oldBatteryTopic = batteryTopic;
            Topic[] topics = {new Topic(batteryTopic, QoS.AT_LEAST_ONCE)};
            connection.subscribe(topics);

        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            e.printStackTrace();
        }
    }

    /**
     * Called by ApAM when the dim topic value changed
     *
     * @param dimTopic the new dim topic value.
     */
    public void onDimTopicChanged(String dimTopic) {
        try {
            String[] oldTopics = {oldDimTopic};
            connection.unsubscribe(oldTopics);

            oldDimTopic = dimTopic;
            Topic[] topics = {new Topic(dimTopic, QoS.AT_LEAST_ONCE)};
            connection.subscribe(topics);

        } catch (Exception e) {
            logger.error("MQTT brocker disconnection failed.");
            e.printStackTrace();
        }
    }

    /**
     * Restart the connection with the newly set parameters
     */
    private void newConnection() {
        logger.debug("Trying to connect MQTT broker: " + host + ":" + port);

        try {
            mqtt.setHost(host, Integer.valueOf(port));
            connection = mqtt.futureConnection();
            logger.debug("connecting...");
            connection.connect();
            logger.debug("MQTT brocker connection success.");

            Object[] objectArray = localTopic.toArray();
            qoses = connection.subscribe(Arrays.copyOf(objectArray, objectArray.length, Topic[].class));
            logger.debug("Topics subscription success.");

            logger.debug("Reception thread intializing...");
            listenningService.schedule(new MQTTListeningThread(), 3, TimeUnit.SECONDS);
            logger.debug("Thread initialized.");

        } catch (NumberFormatException e) {
            logger.error("MQTT brocker connection failed: NumberFormatException ");
            logger.error("Number format exception: " + e.getMessage());
        } catch (URISyntaxException e) {
            logger.error("MQTT brocker connection failed: URISyntaxException");
            logger.error("URI syntaxe error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("MQTT brocker connection failed: Exception");
            e.printStackTrace();
        }
    }

    /* ***********************************************************************
     * 						   	 INNER CLASS                                 *
     *********************************************************************** */
    /**
     * Inner class for MQTT listening thread
     *
     * @author Cédric Gérard
     * @since April 16, 2014
     * @version 1.0.0
     */
    private class MQTTListeningThread implements Runnable {

        /**
         * listening state
         */
        private boolean listening;

        public MQTTListeningThread() {
            super();
            setListening(true);
            logger.debug("MQTTListeningThread instanciated.");
        }

        public void run() {
            Message message;
            try {
                while (listening) {
                    org.fusesource.mqtt.client.Future<Message> receive = connection.receive();
                    message = receive.await();
                    logger.debug("MQTT update received from " + message.getTopic());
                    byte[] payload = message.getPayload();
                    //ApAM DomiCube resolution
                    Implementation impl = CST.apamResolver.findImplByName(null, "DomiCubeImpl");
                    Set<Instance> instances = impl.getInsts();
                    for (Instance inst : instances) {
                        String topic = message.getTopic();
                        if (topic.contentEquals(faceTopic)) {
                            inst.setProperty("activeFace", new String(payload));
                        } else if (topic.contentEquals(batteryTopic)) {
                            inst.setProperty("batteryLevel", new String(payload));
                        } else if (topic.contentEquals(dimTopic)) {
                            inst.setProperty("dimValue", new String(payload));
                        } else {
                            logger.error("Error between topic name and topic property matching.");
                        }
                    }
                    message.ack();
                }
                logger.debug("MQTTListeningThread terminated.");
            } catch (Exception e) {
                logger.error("MQTT brocker message reception faillure.");
                logger.error(e.getMessage());
            }
        }

        public void setListening(boolean state) {
            listening = state;
        }
    }
}
