package appsgate.lig.eude.interpreter.langage.nodes;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node representing the events
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 * 
 * @since June 25, 2013
 * @version 1.0.0
 */
public class NodeEvent extends Node {
	
	/** Type of the source to listen. Can be "program" or "device" */
	private String sourceType;
	/** ID of the source to listen */
	private String sourceId;
	public String getSourceId() { return sourceId; }
	/** Name of the event to listen */
	private String eventName;
	public String getEventName() { return eventName; }
	/** Value of the event to wait */
	private String eventValue;
	public String getEventValue() { return eventValue; }
	
	/**
	 * Default constructor. Instantiate a node when
	 * 
	 * @param interpreter Pointer on the interpreter
	 * @param eventJSON JSON representation of the event
	 * @throws JSONException 
	 */
	public NodeEvent(EUDEInterpreterImpl interpreter, JSONObject eventJSON) throws JSONException {
		super(interpreter);
		
		sourceType = eventJSON.getString("sourceType");
		sourceId = eventJSON.getString("sourceId");
		eventName = eventJSON.getString("eventName");
		eventValue = eventJSON.getString("eventValue");
		
		//pool = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public Integer call() {
		fireStartEvent(new StartEvent(this));
		started = true;
		// if the source of the event is a program
		if (sourceType.equals("program")) {
			// get the node of the program
			NodeProgram p = interpreter.getNodeProgram(sourceId);
			// if it exists
			if (p != null) {
				// listen to its start event...
				if (eventName.equals("runningState")) {
					interpreter.addNodeListening(this);
				}
			} else { // interpreter does not know the program, then the end event is automatically fired
				started = false;
				fireEndEvent(new EndEvent(this));
			}
		// sourceType is "device"
		} else {
			interpreter.addNodeListening(this);
		}
		
		return null;
	}

	@Override
	public void undeploy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void stop() {
		if(started) {
			stopping = true;
			if (sourceType.equals("program")) {
				NodeProgram p = interpreter.getNodeProgram(sourceId);
				if (eventName.equals("start")) {
					p.removeStartEventListener(this);
				} else if (eventName.equals("end")) {
					p.removeEndEventListener(this);
				}
			} else {
				interpreter.removeNodeListening(this);
			}
			started = false;
			stopping = false;
		}
	}

	@Override
	public void resume() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void getState() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void startEventFired(StartEvent e) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void endEventFired(EndEvent e) {
//		Node nodeEnded = (Node)e.getSource();
//		nodeEnded.removeEndEventListener(this);
//		
//		coreEventFired();
	}

	public void coreEventFired() {
		started = false;
		//interpreter.removeNodeListening(this);
		// the node is done when the relevant event has been caught
		fireEndEvent(new EndEvent(this));
	}
}
