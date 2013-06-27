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
import appsgate.lig.router.spec.GenericCommand;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the actions
 * 
 * @author Rémy Dautriche
 * @since May 22, 2013
 * @version 1.0.0
 *
 */
public class NodeAction extends Node {
	
	private String targetType;
	private String targetId;
	private String methodName;
	private JSONArray args;
	private GenericCommand command;

	/**
	 * Default constructor
	 * 
	 * @constructor
	 * @param action
	 * @throws JSONException 
	 */
	public NodeAction(EUDEInterpreterImpl interpreter, JSONObject ruleJSON) throws JSONException {
		super(interpreter);

		targetType = ruleJSON.getString("targetType");
		targetId = ruleJSON.getString("targetId");
		methodName = ruleJSON.getString("methodName");
		args = ruleJSON.getJSONArray("args");
		
		pool = Executors.newSingleThreadExecutor();
		command = null;
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endEventFired(EndEvent e) {
		System.out.println("##### NodeAction - End event received!");
		((Node)e.getSource()).removeEndEventListener(this);
		fireEndEvent(new EndEvent(this));
	}

	@Override
	public Integer call() {    
	    fireStartEvent(new StartEvent(this));
	
		if (targetType.equals("device")) {
			// get the runnable from the interpreter
			command = interpreter.executeCommand(targetId, methodName, args);
			pool.submit(command);

			// manage the pool
			super.call();

			fireEndEvent(new EndEvent(this));
		} else if (targetType.equals("program")) {
			NodeProgram p = interpreter.getNodeProgram(targetId);
			if (p != null) {	
				// listen to the end of the program
				p.addEndEventListener(this);
				
				// launch the program
				interpreter.callProgram(targetId);
			} else {
				fireEndEvent(new EndEvent(this));
			}
		}
		
		return null;
	}
	
	public Object getResult() {
	    if (command != null) {
			return command.getReturn();
	    } else {
			return null;
	    }
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
