package appsgate.lig.proxy.MQTTAdapter;

import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.Executors;
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

/**
 * Temporary MQTT client for DomiCube message receiving
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
	 * MQTT DomiCube topic
	 */
	private String topic;
	
	/**
	 * MQTT DomiCube old topic
	 */
	private String oldTopic;
	
	/**
	 * MQTT attribute to initiate the connexction
	 */
	private MQTT mqtt;
	
	/**
	 * Executor scheduler for MQTT message reception
	 */
	private ScheduledExecutorService listenningService;
	
	/**
	 * Connection member for MQTT broker
	 */
	BlockingConnection connection;
	
	/**
	 * subscribed topic
	 */
	private Topic localTopic;
	
	/**
	 * All QoS
	 */
	private byte[] qoses;
	
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
			logger.debug("Trying to connect MQTT broker: "+host+":"+port);
			mqtt.setHost(host, Integer.valueOf(port));
			connection = mqtt.blockingConnection();
			logger.debug("connecting...");
			connection.connect();
			logger.debug("MQTT brocker connection success.");
			
			localTopic = new Topic(topic, QoS.AT_LEAST_ONCE);
			oldTopic = topic;
			Topic[] topics = { localTopic };
			qoses = connection.subscribe(topics);
			logger.debug("Topics subscription success.");
			
			logger.debug("Reception thread intializing...");
			listenningService = Executors.newScheduledThreadPool(1);
			listenningService.schedule(new MQTTListeningThread(), 3, TimeUnit.SECONDS);
			logger.debug("Thread initialized.");
			
		} catch (NumberFormatException e) {
			logger.error("MQTT brocker connection failed.");
			logger.error("Number format exception: "+e.getMessage());
		} catch (URISyntaxException e) {
			logger.error("MQTT brocker connection failed.");
			logger.error("URI syntaxe error: "+e.getMessage());
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
			if(listenningService != null) {
				listenningService.shutdown();
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
		}catch (Exception e) {
			logger.error("MQTT brocker disconnection failed.");
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Called by ApAM when the host value changed
	 * @param newHost the new host value.
	 */
	public void onHostChanged(String newHost) {
		try {
			delInst();
			newConnection();
		}catch (Exception e) {
			logger.error("MQTT brocker disconnection failed.");
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Called by ApAM when the host value changed
	 * @param newHost the new host value.
	 */
	public void onPortChanged(String newHost) {
		try {
			delInst();
			newConnection();
		}catch (Exception e) {
			logger.error("MQTT brocker disconnection failed.");
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Called by ApAM when the host value changed
	 * @param newHost the new host value.
	 */
	public void onTopicChanged(String newHost) {
		try {
			String[] oldTopics = { oldTopic };
			connection.unsubscribe(oldTopics);
			
			localTopic = new Topic(topic, QoS.AT_LEAST_ONCE);
			oldTopic = topic;
			Topic[] topics = { localTopic };			
			connection.subscribe(topics);
			
		}catch (Exception e) {
			logger.error("MQTT brocker disconnection failed.");
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Restart the connection with the newly set parameters
	 */
	private void newConnection(){
		logger.debug("Trying to connect MQTT broker: "+host+":"+port);
		
		try{
			mqtt.setHost(host, Integer.valueOf(port));
			connection = mqtt.blockingConnection();
			logger.debug("connecting...");
			connection.connect();
			logger.debug("MQTT brocker connection success.");
		
			Topic[] topics = { localTopic };
			qoses = connection.subscribe(topics);
			logger.debug("Topics subscription success.");
		
			logger.debug("Reception thread intializing...");
			listenningService.schedule(new MQTTListeningThread(), 3, TimeUnit.SECONDS);
			logger.debug("Thread initialized.");
			
		} catch (NumberFormatException e) {
			logger.error("MQTT brocker connection failed.");
			logger.error("Number format exception: "+e.getMessage());
		} catch (URISyntaxException e) {
			logger.error("MQTT brocker connection failed.");
			logger.error("URI syntaxe error: "+e.getMessage());
		} catch (Exception e) {
			logger.error("MQTT brocker connection failed.");
			logger.error(e.getMessage());
		}
	}
	
	/* ***********************************************************************
	 * 						   	 INNER CLASS                                 *
	 *********************************************************************** */
	
	/**
	 * Inner class for MQTT listening thread
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
				while(listening) {
					message = connection.receive();
					logger.debug("MQTT update received from "+message.getTopic());
					byte[] payload = message.getPayload();
					//ApAM DomiCube resolution
					Implementation impl = CST.apamResolver.findImplByName(null, "DomiCubeImpl");
					Set<Instance> instances =  impl.getInsts();
					for(Instance inst : instances ) {
						inst.setProperty("currentFace", new String(payload));
					}
					message.ack();
				}
				logger.debug("MQTTListeningThread terminated.");
			} catch (Exception e) {
				logger.error("MQTT brocker message reception faillure.");
				logger.error(e.getMessage());
			}
		}
		
		public void setListening(boolean state){
			listening = state;
		}
	}

}
