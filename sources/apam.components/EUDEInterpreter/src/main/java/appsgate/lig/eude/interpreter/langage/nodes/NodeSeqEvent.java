package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Node for a list of events
 *
 * @author Rémy Dautriche
 * @since June 26, 2013
 * @version 1.0.0
 */
public class NodeSeqEvent extends Node {
	// <seqEvent> ::= <event> {, <event> }
	
	/** list of events */
	private ArrayList<NodeEvent> seqEvent;
	/** number of events received */
	private int nbEventReceived;
	
	/**
	 * Default constructor. Instantiate a node of a sequence of events
	 * 
	 * @param interpreter Pointer on the interpreter
	 * @param seqEventJSON Sequence of event to instantiate in a JSON format
	 */
	public NodeSeqEvent(EUDEInterpreterImpl interpreter, JSONArray seqEventJSON) throws JSONException {
		super(interpreter);
		
		// instantiate the events
		seqEvent = new ArrayList<NodeEvent>();
		for (int i = 0; i < seqEventJSON.length(); i++) {
			seqEvent.add(new NodeEvent(interpreter, seqEventJSON.getJSONObject(i)));
		}
		
		// initialize the thread pool
		pool = Executors.newFixedThreadPool(seqEvent.size());
	}
	
	@Override
	public Integer call() {
		// fire the start event
		fireStartEvent(new StartEvent(this));
		
		// no event has been received yet
		nbEventReceived = 0;
		
		// add an end event listener to each event
		for (NodeEvent e : seqEvent) {
			e.addEndEventListener(this);
		}
		
		try {
			pool.invokeAll(seqEvent);
		} catch (InterruptedException ex) {
			Logger.getLogger(NodeSeqEvent.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	@Override
	public void undeploy() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void stop() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
		((Node)e.getSource()).removeEndEventListener(this);
		nbEventReceived++;
		
		// if all the events have been fired, fire the end event of the sequence of events
		if (nbEventReceived == seqEvent.size()) {
			fireEndEvent(new EndEvent(this));
		}
	}
	
}
