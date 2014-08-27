package appsgate.ard.protocol;


import appsgate.lig.core.object.messages.NotificationMsg;

/***
 * This class receives all APAM messages related with ARD to make sure all messages have been received in APAM bus
 */
public class ARDMessageGreedyListener {

    public void start(){
        System.out.println("Starting ARD adaptor");
    }

    public void stop(){
        System.out.println("Stopping ARD adaptor");
    }

    public void apamMessageReceived(NotificationMsg mesg){
        System.out.println("Message received: Content:"+mesg.getNewValue().toString());
    }

}
