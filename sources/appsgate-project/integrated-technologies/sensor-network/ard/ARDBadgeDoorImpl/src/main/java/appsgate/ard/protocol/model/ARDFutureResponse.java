package appsgate.ard.protocol.model;

import appsgate.ard.protocol.controller.ARDController;
import appsgate.ard.protocol.model.command.ARDRequest;
import appsgate.ard.protocol.model.command.listener.ARDMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ARDFutureResponse extends Thread implements ARDMessage,Constraint {

    ARDController ard;
    ARDRequest command;
    JSONObject response;
    Integer requestId;

    public ARDFutureResponse(ARDController ard,ARDRequest command) throws JSONException {
        this.ard=ard;
        this.command=command;
        requestId=command.getRequestId();
    }

    public void run(){
        try {

            ard.getMapRouter().put(this,this);

            ard.sendRequest(command);

            synchronized (this){
                this.wait();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ardMessageReceived(JSONObject json) throws JSONException {
        System.out.println("response received for " + json.getInt("request_id"));
        ard.getMapRouter().remove(this);
        response=json;
        synchronized (this){
            this.notify();
        }

    }

    @Override
    public boolean evaluate(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt("request_id")==requestId;
    }

    public JSONObject getResponse(){

        synchronized (this){

            //Called only if we never executed this request
            if(getState()==State.NEW){

                this.start();

                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

        return response;

    }
}
