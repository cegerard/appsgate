package appsgate.ard.protocol;


import appsgate.lig.core.object.messages.NotificationMsg;

/***
 * This class receives all APAM messages related with ARD to make sure all messages have been received in APAM bus
 */
public class ARDMessageGreedyListener {

    private static final String NAME="ARD Greedy Message Listener";

    public void start(){
        System.out.println("Starting "+NAME);
    }

    public void stop(){
        System.out.println("Stopping "+NAME);
    }

    public void apamMessageReceived(NotificationMsg mesg){
        System.out.println("Message received: Content:"+mesg.getNewValue().toString());
    }

}
