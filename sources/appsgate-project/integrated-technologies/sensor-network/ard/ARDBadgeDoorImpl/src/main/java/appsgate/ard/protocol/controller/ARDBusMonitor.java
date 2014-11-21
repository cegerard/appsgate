package appsgate.ard.protocol.controller;

import appsgate.ard.protocol.model.ARDBusState;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ARDBusMonitor extends Thread {

    private Logger logger= LoggerFactory.getLogger(ARDController.ARD_LOGNAME);
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
                    //System.out.print((char) l);
                    //0 is the marker at the end of JSON messages
                    //10 the new line feed in case of BUSY response

                    /**
                     * Replace this implem by a buffer
                    try {
                        JSONObject json = new JSONObject(sb.toString());
                        if(json.getString("request")!=null && json.getString("request").equals("BUZY")){
                            System.err.println("Waiting..");
                            Thread.sleep(20);
                        }
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    **/

                    if (l == 0 || l == 10) { //NULL marks the end of the response message
                        state=ARDBusState.IDLE;
                        //System.out.println("End of the response message");
                        logger.debug("Bus message detected {}",sb.toString());
                        try {
                            JSONObject json= null;
                            json = new JSONObject(sb.toString());
                            responseListener.ardMessageReceived(json);
                        } catch (JSONException e) {
                            logger.warn("ARD Router returned an invalid JSON", e);
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
                //Not necessary to throw exception, this is normal because the timeout for read was changed in order to better control the disconnection
            }

        }while(keepMonitoring);

    }

    public ARDBusState BusState() {
        return state;
    }
}
