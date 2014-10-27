package appsgate.lig.proxy.MQTTAdapter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fusesource.mqtt.client.FutureConnection;

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
    private ExecutorService listenningService = Executors.newCachedThreadPool();

    private List<MQTTListeningThread> listenningThreads=new ArrayList<MQTTListeningThread>();

    /**
     * All schedule task for MQTT broker reception
     */
    private Future<?> scheduledTasks;

    /**
     * Connection member for MQTT broker
     */
    private List<FutureConnection> mqttConnections =new ArrayList<FutureConnection>();

    private static Logger logger = LoggerFactory.getLogger(DomiCubeMQTTAdapterImpl.class);

    private FutureConnection connectMQTT(String topic){

        FutureConnection connection=null;

        try {
            ArrayList<Topic> localTopicFace = new ArrayList<Topic>();
            localTopicFace.add(new Topic(topic, QoS.AT_LEAST_ONCE));
            Object[] objectArray = localTopicFace.toArray();
            mqtt = new MQTT();
            logger.info("Connecting to broker {}:{} on topic {} ...",host,port,topic);
            mqtt.setHost(host, Integer.valueOf(port));
            connection = mqtt.futureConnection();
            connection.connect();
            connection.subscribe(Arrays.copyOf(objectArray, objectArray.length, Topic[].class));
            logger.info("Connected to broker {}:{} on topic {}",host,port,topic);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return connection;

    }

    public void newInst() {
        System.out.println("initializing MQTT DomiCube adapter.");
        try {

            logger.debug("MQTT brocker connection success.");

            oldFaceTopic = faceTopic;
            oldDimTopic = dimTopic;
            oldBatteryTopic = batteryTopic;

            FutureConnection face=connectMQTT(faceTopic);
            FutureConnection battery=connectMQTT(batteryTopic);
            FutureConnection dim=connectMQTT(dimTopic);

            mqttConnections.add(face);
            mqttConnections.add(battery);
            mqttConnections.add(dim);

            MQTTListeningThread t1=new MQTTListeningThread(face,"activeFace");
            listenningService.execute(t1);
            MQTTListeningThread t2=new MQTTListeningThread(battery,"batteryLevel");
            listenningService.execute(t2);
            MQTTListeningThread t3=new MQTTListeningThread(dim,"dimValue");
            listenningService.execute(t3);

            listenningThreads.add(t1);
            listenningThreads.add(t2);
            listenningThreads.add(t3);

            logger.debug("Thread initialized.");

        } catch (NumberFormatException e) {
            logger.error("MQTT brocker connection failed.");
            logger.error("Number format exception: " + e.getMessage());
        } catch (Exception e) {
            logger.error("MQTT brocker connection failed.");
            logger.error(e.getMessage());
        }
    }

    public void delInst() {

        try {
            logger.debug("Closing MQTT connection...");
            for(FutureConnection con: mqttConnections){
                con.disconnect();
            }

            for(MQTTListeningThread thread:listenningThreads){
                thread.setListening(false);
            }

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
            logger.info("Host changed to {}", newHost);
            delInst();
            newInst();
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
            logger.info("Port changed to {}", newHost);
            delInst();
            newInst();
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
        logger.info("Face topic Changed.");

    }

    /**
     * Called by ApAM when the battery topic value changed
     *
     * @param batteryTopic the battery topic value.
     */
    public void onBatteryTopicChanged(String batteryTopic) {
        logger.info("Battery topic Changed.");
    }

    /**
     * Called by ApAM when the dim topic value changed
     *
     * @param dimTopic the new dim topic value.
     */
    public void onDimTopicChanged(String dimTopic) {
        logger.info("Dim topic Changed.");
    }

}
