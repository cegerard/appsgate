package appsgate.ard.protocol;


import appsgate.lig.core.object.messages.NotificationMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This class receives all APAM messages related with ARD to make sure all messages have been received in APAM bus
 */
public class ARDMessageGreedyListener {

    private static Logger logger = LoggerFactory.getLogger(ARDMessageGreedyListener.class);

    private static final String NAME="ARD Greedy Message Listener";

    public void start(){
        System.out.println("Starting "+NAME);
    }

    public void stop(){
        System.out.println("Stopping "+NAME);
    }

    public void apamMessageReceived(NotificationMsg mesg){

        logger.debug("Apam Message received var name {} old value {} new value {}",mesg.getVarName(),mesg.getOldValue(),mesg.getNewValue());

    }

}
