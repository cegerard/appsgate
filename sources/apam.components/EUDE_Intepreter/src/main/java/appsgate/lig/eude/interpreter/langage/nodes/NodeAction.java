package appsgate.lig.eude.interpreter.langage.nodes;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node for the actions
 * 
 * @author Rémy Dautriche
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeAction extends Node {
	
	private String deviceId;
	private String methodName;
	private JSONArray args;
	
	/**
	 * Default constructor
	 * 
	 * @constructor
	 * @param action
	 * @throws JSONException 
	 */
	public NodeAction(EUDEInterpreterImpl interpreter, JSONObject ruleJSON) throws JSONException {
		super(interpreter);

		deviceId = ruleJSON.getString("deviceId");
		methodName = ruleJSON.getString("methodName");
		args = ruleJSON.getJSONArray("args");
		
		pool = Executors.newSingleThreadExecutor();
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endEventFired(EndEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		
		Future<?> f = pool.submit(interpreter.executeCommand(deviceId, methodName, args));
		try {
			f.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fireEndEvent(new EndEvent(this));

		return null;
	}

	@Override
	public void undeploy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void getState() {
		// TODO Auto-generated method stub
	}

}
