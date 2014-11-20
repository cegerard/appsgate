/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package appsgate.ard.protocol.controller;

import appsgate.ard.protocol.model.ARDFutureResponse;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.ard.protocol.model.command.ARDRequest;
import appsgate.ard.protocol.model.Constraint;
import appsgate.ard.protocol.model.command.request.SubscriptionRequest;
import org.apache.felix.ipojo.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@Provides
public class ARDController {

    private Logger logger = LoggerFactory.getLogger(ARDController.class);
    private final Integer SOCKET_TIMEOUT=5000;
    private final Integer STREAM_FLOW_RESTTIME=100;


    private Socket socket;
    private ARDBusMonitor monitor;
    private Object token=new Object();
    private ARDMessage globalMessageReceived;
    private Map<Constraint,ARDMessage> mapRouter=new HashMap<Constraint, ARDMessage>();

    @Property(value = "-1")
    private Integer retry;
    @Property(mandatory = true)
    private String host;
    @Property(mandatory = true)
    private Integer port;

    public ARDController(){
        //to keep ipojo compatility
    }

    public ARDController(String host, int port){
        this.port=port;
        this.host=host;
    }

    public void connect() throws IOException {
        socket = new Socket(); //host, port
        socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
        globalMessageReceived =new ARDMessage() {
            public void ardMessageReceived(JSONObject json) throws JSONException {
                logger.debug("Messages received {}", json.toString());
                for(Constraint cons:new ArrayList<Constraint>(mapRouter.keySet())){
                    Boolean checkResult=false;
                    try {
                        checkResult=cons.evaluate(json);
                    }catch(JSONException e){
                        logger.debug("Exception was raised when evaluating the constraint {}", json.toString());
                    }

                    try {
                        if (checkResult){
                            logger.debug("Forwarding message {} received to higher layer", json.toString());
                            mapRouter.get(cons).ardMessageReceived(json);
                        }else {
                            logger.debug("{} evaluated to {}", new Object[]{Constraint.class.getSimpleName(), checkResult});
                        }
                    }catch(JSONException e){
                        logger.debug("Exception was raised when invoking listener {}", mapRouter.get(cons).toString());
                    }

                }

            }
        };
    }

    public void disconnect() throws IOException {
        logger.info("Controller is closing the socket");
        monitor.kill();
        socket.close();
    }

    private Boolean isConnected(){
        return socket==null?false:socket.isConnected();
    }

    public void monitoring() throws IOException {
        if(isConnected()){
            monitor=new ARDBusMonitor(socket.getInputStream(), globalMessageReceived, true);
            monitor.start();
        }else {
            logger.info("Connection is not open with the host {}:{}, monitoring request ignored.",host,port);
        }

    }

    public ARDFutureResponse sendSyncRequest(ARDRequest command){

        ARDFutureResponse a= null;
        try {
            a = new ARDFutureResponse(this,command);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return a;

    }

    public void sendRequest(ARDRequest command) throws IOException, JSONException {

        synchronized (token) {

            ByteArrayOutputStream boss = new ByteArrayOutputStream();

            if (!socket.isClosed()) {
                logger.debug("Sending command {} for the ARD controller", command.getJSON());

                boss.write((command.getJSON()).getBytes());
                boss.flush();
                socket.getOutputStream().write(boss.toByteArray());

                try {
                    Thread.sleep(STREAM_FLOW_RESTTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                logger.debug("Socket was closed, impossible to send commands to {}:{}", host, port);
            }
        }

    }

    //@Validate
    public void validate() throws JSONException {

        Thread t1=new Thread(){

            public void run(){
                while(!isConnected()){

                    try {
                        connect();
                        monitoring();
                        sendRequest(new SubscriptionRequest());
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(retry==null || retry==-1) break;
                        try {
                            Thread.sleep(retry);
                        } catch (InterruptedException e1) {
                            logger.error("Failed retrying to connect with ARD HUB");
                        }

                    }

                }
            }

        };
        t1.setDaemon(true);
        t1.start();


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ARDController that = (ARDController) o;

        if (!host.equals(that.host)) return false;
        if (!port.equals(that.port)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    @Invalidate
    public void invalidate() throws IOException {
       disconnect();
    }

    public Map<Constraint, ARDMessage> getMapRouter() {
        return mapRouter;
    }

}


