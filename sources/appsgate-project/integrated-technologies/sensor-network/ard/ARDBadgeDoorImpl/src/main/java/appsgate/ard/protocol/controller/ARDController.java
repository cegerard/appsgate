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

import appsgate.ard.protocol.model.command.listener.ARDMessage;
import appsgate.ard.protocol.model.command.ARDRequest;
import appsgate.ard.protocol.model.Constraint;
import appsgate.ard.protocol.model.command.request.SubscriptionRequest;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ARDController {

    private Logger logger = LoggerFactory.getLogger(ARDController.class);

    private String host;

    private Integer port;

    private Socket socket;
    private ARDBusMonitor monitor;
    private Object token=new Object();
    private ARDMessage globalMessageReceived;
    private Map<Constraint,ARDMessage> mapRouter=new HashMap<Constraint, ARDMessage>();

    public ARDController(){
        //to keep ipojo compatility
    }

    public ARDController(String host, int port){
        this.port=port;
        this.host=host;
    }

    public void connect() throws IOException {
        socket = new Socket(); //host, port
        socket.connect(new InetSocketAddress(host, port), 1000);
        globalMessageReceived =new ARDMessage() {
            public void ardMessageReceived(JSONObject json) throws JSONException {
                logger.debug("Messages received {}", json.toString());
                for(Constraint cons:mapRouter.keySet()){
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
        monitor.kill();
        socket.close();
    }

    private Boolean isConnected(){
        return socket.isConnected();
    }

    public void monitoring() throws IOException {
        if(isConnected()){
            monitor=new ARDBusMonitor(socket.getInputStream(), globalMessageReceived, true);
            monitor.start();
        }else {
            logger.info("Connection is not open with the host {}:{}, monitoring request ignored.",host,port);
        }

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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                logger.debug("Socket was closed, impossible to send commands to {}:{}", host, port);
            }


        }

    }

    public void validate() throws IOException, JSONException {
        connect();
        monitoring();
        sendRequest(new SubscriptionRequest());
    }

    public void invalidate() throws IOException {
       disconnect();
    }

    public Map<Constraint, ARDMessage> getMapRouter() {
        return mapRouter;
    }

}


