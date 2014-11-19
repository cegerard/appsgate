package appsgate.ard.protocol.controller;

import appsgate.ard.protocol.model.ARDBusState;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class ARDBusMonitor extends Thread {

    private Logger logger= LoggerFactory.getLogger(ARDBusMonitor.class);
    private InputStream is;
    private boolean keepMonitoring;
    private ARDBusState state=ARDBusState.IDLE;
    private ARDMessage responseListener;

    public void kill(){
        keepMonitoring=false;
    }

    public ARDBusMonitor(InputStream is, ARDMessage responseListener, boolean keepMonitoring) {
        this.keepMonitoring=keepMonitoring;
        this.responseListener=responseListener;
        super.setDaemon(true);
        this.is = is;
    }

    public void run() {

        StringBuffer sb = new StringBuffer();

        do{
            try {
                int l;
                while (keepMonitoring && (l = is.read()) != -1) {

                    //System.out.println("Adding:"+l+"(int) "+Integer.toHexString(l)+"(hex)"+(char)l+"(char) " + new String(new byte[]{new Integer(l).byteValue()}, "UTF-8")+"(utf)");
                    System.out.print((char)l);
                    //0 is the marker at the end of JSON messages
                    //10 the new line feed in case of BUSY response
                    if (l == 0 || l == 10) { //NULL marks the end of the response message
                        state=ARDBusState.IDLE;
                        //System.out.println("End of the response message");
                        logger.debug("Bus message detected {}",sb.toString());
                        try {
                            JSONObject json= null;
                            json = new JSONObject(sb.toString());
                            responseListener.ardMessageReceived(json);
                        } catch (JSONException e) {
                            logger.warn("ARD Router returned an invalid JSON",e);
                            //e.printStackTrace();
                        } finally {
                            sb = new StringBuffer();
                        }

                        break;
                    }
                    state=ARDBusState.BUSY;

                    sb.append((char) l);
                }


            } catch (IOException e) {
                logger.warn("Socket is already closed.");
            }

        }while(keepMonitoring);

    }

    public ARDBusState BusState() {
        return state;
    }
}
