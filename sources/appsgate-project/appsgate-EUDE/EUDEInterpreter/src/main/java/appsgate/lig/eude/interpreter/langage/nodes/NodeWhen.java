package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node for the when
 *
 *  // <nodeWhen> ::= when ( <nodeEvent> {, <nodeEvent> } ) then <seqAndRules>*
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 25, 2013
 * @version 1.0.0
 */
public class NodeWhen extends Node {

    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeWhen.class.getName());

    /**
     * list of events
     */
    private ArrayList<NodeEvent> seqEvent;
    /**
     * The number of events that have fired EndEvent
     */
    private int nbEventEnded = 0;

    /**
     * The sequence of thing to do once the events are done
     */
    private NodeSeqRules seqRules;

    /**
     * Default constructor. Instantiate a node when
     *
     * @param ruleWhenJSON
     * @param parent
     * @throws SpokNodeException
     */
    public NodeWhen(JSONObject ruleWhenJSON, Node parent) throws SpokNodeException {
        super(parent);
        seqEvent = new ArrayList<NodeEvent>();
        JSONArray seqEventJSON = getJSONArray(ruleWhenJSON, "events");
        for (int i = 0; i < seqEventJSON.length(); i++) {
            try {
                seqEvent.add(new NodeEvent(seqEventJSON.getJSONObject(i), this));
            } catch (JSONException ex) {
                throw new SpokNodeException("NodeSeqEvent", "item " + i, ex);
            }
        }

        // initialize the sequences of events and rules
        seqRules = new NodeSeqRules(getJSONArray(ruleWhenJSON, "seqRulesThen"), this);

    }

    /**
     * private constructor to copy node
     *
     * @param interpreter
     * @param parent
     */
    private NodeWhen(Node parent) {
        super(parent);
    }

    @Override
    public JSONObject call() {
        LOGGER.debug("Call {}", this);
        if (!isStarted()) {
            nbEventEnded = 0;
            fireStartEvent(new StartEvent(this));
            setStarted(true);
        }
        for (NodeEvent e : seqEvent) {
            e.addEndEventListener(this);
            e.call();
        }
        return null;
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node nodeEnded = (Node) e.getSource();
        LOGGER.debug("NWhen end event: {}", nodeEnded);
        if (!isStopping()) {
            // if all the events are received, launch the sequence of rules
            if (nodeEnded instanceof NodeEvent) {
                nbEventEnded++;
                if (nbEventEnded == seqEvent.size()) {
                    LOGGER.debug("All the events have been ended");
                    seqRules.addEndEventListener(this);
                    seqRules.call();
                }
                return;

            }
            setStarted(false);
            fireEndEvent(new EndEvent(this));

        } else {
            LOGGER.warn("endEvent has been fired while the node was stopping");
        }
    }

    @Override
    protected void specificStop() throws SpokException{
        LOGGER.debug("specific Stop");
        for (NodeEvent e : seqEvent) {
            e.removeEndEventListener(this);
            e.stop();
        }
        seqRules.removeEndEventListener(this);
        seqRules.stop();
    }

    @Override
    public String toString() {
        return "[Node When: events(" + seqEvent.toString() + "), rules(" + seqRules + ")]";
    }
    
    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("seqRulesThen", seqRules.getJSONArrayDescription());
            JSONArray evt = new JSONArray();
            int i = 0;
            for (NodeEvent e: seqEvent){
                evt.put(i, e.getJSONDescription());
            }
            o.put("events", evt);
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    @Override
    public String getExpertProgramScript() {
        String events = "[";
        for (NodeEvent e : seqEvent) {
            events += e.getExpertProgramScript() + ",";
        }
        events = events.substring(0, events.length() - 1) + "]";

        return "when(" + events + "," + seqRules.getExpertProgramScript() + ")";
    }

    @Override
    protected void collectVariables(SymbolTable s) {
        for (NodeEvent e : seqEvent) {
            e.collectVariables(s);
        }
        seqRules.collectVariables(s);
    }

    @Override
    protected Node copy(Node parent) {
        NodeWhen ret = new NodeWhen(parent);
        ret.setSymbolTable(this.getSymbolTable());
        ret.seqEvent = new ArrayList<NodeEvent>();
        for (NodeEvent n : seqEvent) {
            ret.seqEvent.add((NodeEvent) n.copy(ret));
        }
        ret.seqRules = (NodeSeqRules) seqRules.copy(ret);
        return ret;

    }

}
