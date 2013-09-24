package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.router.spec.GenericCommand;

/**
 * Node for the actions
 * 
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * 
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
		
		//TODO remove the pool nodeAction execute the command right now
		//The router manage execution thread by its own
		//pool = Executors.newSingleThreadExecutor();
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
		started = false;
		fireEndEvent(new EndEvent(this));
	}

	@Override
	public Integer call() {    
	    fireStartEvent(new StartEvent(this));
	    started = true;
		if (targetType.equals("device")) {
			// get the runnable from the interpreter
			command = interpreter.executeCommand(targetId, methodName, args);
			command.run();
			//pool.submit(command);
			
			// manage the pool
			//super.call();
		} else if (targetType.equals("program")) {
			NodeProgram p = interpreter.getNodeProgram(targetId);
			
			if (p != null) {
				if(methodName.contentEquals("callProgram") && p.getRunningState() != RUNNING_STATE.STARTED) {
					// listen to the end of the program
					p.addEndEventListener(this);
					// launch the program
					interpreter.callProgram(targetId);
				}else if(methodName.contentEquals("stopProgram") && p.getRunningState() == RUNNING_STATE.STARTED) {
					//stop the running program
					interpreter.stopProgram(targetId);
				}
			}
		}
		
		started = false;
		fireEndEvent(new EndEvent(this));
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
		if(started && targetType.equals("program")) {
			stopping = true;
			NodeProgram p = interpreter.getNodeProgram(targetId);
			p.stop();
			started = false;
			stopping = false;
		}
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
