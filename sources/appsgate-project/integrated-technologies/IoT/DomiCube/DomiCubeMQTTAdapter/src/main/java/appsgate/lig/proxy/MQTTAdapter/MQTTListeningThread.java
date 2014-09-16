package appsgate.lig.proxy.MQTTAdapter;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MQTTListeningThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MQTTListeningThread.class);

    private boolean listening=true;

    private String propertyName;

    private FutureConnection connection;

    public MQTTListeningThread(FutureConnection connection, String propertyName) {
        this.propertyName=propertyName;
        this.connection=connection;
    }

    public void run() {
        Message message;
        try {

            while (listening) {
                org.fusesource.mqtt.client.Future<Message> receive = connection.receive();
                message = receive.await();
                logger.debug("MQTT update received from topic {}, setting property {}",message.getTopic(),propertyName);
                byte[] payload = message.getPayload();
                Implementation impl = CST.apamResolver.findImplByName(null, "DomiCubeImpl");
                Set<Instance> instances = impl.getInsts();
                for (Instance inst : instances) {
                    inst.setProperty(propertyName,new String(payload));
                }
                message.ack();
            }
            logger.debug("MQTTListeningThread terminated.");
        } catch (Exception e) {
            logger.error("MQTT brocker message reception failure.");
            logger.error(e.getMessage());
        }
    }

    public void setListening(boolean state) {
        listening = state;
    }

}

