package appsgate.lig.proxy.MQTTAdapter;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Set;

public class MQTTListeningThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MQTTListeningThread.class);

    private final String host;
    private final String port;
    private final String topic;
    private boolean ready=false;

    private MQTT mqtt = new MQTT();

    private String propertyName;

    private FutureConnection connection;

    public MQTTListeningThread(String host,String port,String topic,String propertyName){
        this.host=host;
        this.port=port;
        this.topic=topic;
        this.propertyName=propertyName;
    }

    private void connectMQTT(){
        try {
            logger.info("Connecting to broker {}:{} on topic {} ...", host, port, topic);
            mqtt.setHost(host, Integer.valueOf(port));
            connection = mqtt.futureConnection();
            connection.connect().await();
            subscribe();
            ready=true;
            logger.info("Connected to broker {}:{} on topic {}",host,port,topic);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        connectMQTT();
        while(!connection.isConnected()){
            logger.debug("Connection not yet established topic {} host {} port {}",new Object[]{topic,host,port});
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //Ignored
            }
        }

        try {
            while (ready) {
                org.fusesource.mqtt.client.Future<Message> receive = connection.receive();
                Message message = receive.await();
                message.ack();
                logger.debug("MQTT update received from topic {}, setting property {}", message.getTopic(), propertyName);
                byte[] payload = message.getPayload();
                Implementation impl = CST.apamResolver.findImplByName(null, "DomiCubeImpl");
                Set<Instance> instances = impl.getInsts();
                for (Instance inst : instances) {
                    inst.setProperty(propertyName,new String(payload));
                }

            }
            logger.debug("MQTTListeningThread terminated.");
        } catch (Exception e) {
            logger.error("MQTT brocker message reception failure.");
            logger.error(e.getMessage());
        }
    }

    public void stopMQTT() {
        if(connection!=null){
            connection.disconnect();
        }
        ready=false;
    }

    public void subscribe() {
        logger.debug("Connected on host {} port {}", new Object[]{host, port});
        Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
        try {
            logger.debug("Subscribing on the topic {} host {} port {}..",new Object[]{topic,host,port});
            connection.subscribe(topics);
            logger.debug("Subscribed");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Impossible to subscribe on the topic {} host {} port {}",new Object[]{topic,host,port});
        }
    }

}

