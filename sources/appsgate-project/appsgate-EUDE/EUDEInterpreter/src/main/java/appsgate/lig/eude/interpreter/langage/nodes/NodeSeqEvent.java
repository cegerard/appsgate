package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for a list of events
 *
 * <seqEvent> ::= <event> {, <event> }
 *
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 26, 2013
 * @version 1.0.0
 */
public class NodeSeqEvent extends Node {

    //Logger
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSeqAndRules.class.getName());

    /**
     * list of events
     */
    private final ArrayList<NodeEvent> seqEvent;
    /**
     * number of events received
     */
    private int nbEventReceived;

    /**
     * Default constructor. Instantiate a node of a sequence of events
     *
     * @param interpreter Pointer on the interpreter
     * @param seqEventJSON Sequence of event to instantiate in a JSON format
     * @param parent
     * @throws appsgate.lig.eude.interpreter.langage.nodes.NodeException
     */
    public NodeSeqEvent(EUDEInterpreterImpl interpreter, JSONArray seqEventJSON, Node parent) throws NodeException {
        super(interpreter, parent);

        // instantiate the events
        seqEvent = new ArrayList<NodeEvent>();
        for (int i = 0; i < seqEventJSON.length(); i++) {
            try {
                seqEvent.add(new NodeEvent(interpreter, seqEventJSON.getJSONObject(i), this));
            } catch (JSONException ex) {
                throw new NodeException("NodeSeqEvent", "item " + i, ex);
            }
        }
    }
    
    @Override
    public Integer call() {
        // fire the start event
        fireStartEvent(new StartEvent(this));
        setStarted(true);

        // no event has been received yet
        nbEventReceived = 0;

        // add an end event listener to each event
        for (NodeEvent e : seqEvent) {
            e.addEndEventListener(this);
            e.call();
        }
        
        return null;
    }
    
    @Override
    public void stop() {
        if (isStarted()) {
            setStopping(true);
            for (Node n : seqEvent) {
                n.removeEndEventListener(this);
                n.stop();
            }
            setStarted(false);
            setStopping(false);
        }
    }
    
    @Override
    public void endEventFired(EndEvent e) {
        nbEventReceived++;

        // if all the events have been fired, fire the end event of the sequence of events
        if (nbEventReceived == seqEvent.size()) {
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }
    }
    
    @Override
    public String toString() {
        return "[Node SeqEvent: [" + seqEvent.size() + "]]";
    }

    @Override
    public String getExpertProgramScript() {
        String ret = "[";
        for (NodeEvent e : this.seqEvent) {
            ret += e.getExpertProgramScript() + ",";
        }
        return ret.substring(0, ret.length()-1) + "]";
    }
    
    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeEvent e : this.seqEvent) {
            e.collectVariables(s);
        }
    }
    
}
