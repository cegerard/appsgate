package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeWhen.class);

    /**
     * list of events
     */
    private NodeEvents seqEvent;

    /**
     * The sequence of thing to do once the events are done
     */
    private Node seqRules;

    /**
     * Default constructor. Instantiate a node "when"
     *
     * @param ruleWhenJSON the json description
     * @param parent the parent node
     * @throws SpokNodeException
     */
    public NodeWhen(JSONObject ruleWhenJSON, Node parent) throws SpokException {
        super(parent);
        seqEvent = new NodeEvents(getJSONObject(ruleWhenJSON, "events"), this);
        // initialize the sequences of events and rules
        seqRules = Builder.buildFromJSON(ruleWhenJSON.optJSONObject("seqRulesThen"), this);

    }

    /**
     * private constructor to copy node
     *
     * @param parent
     */
    private NodeWhen(Node parent) {
        super(parent);
    }

    @Override
    public JSONObject call() {
        LOGGER.debug("Call {}", this);
        if (!isStarted()) {
            fireStartEvent(new StartEvent(this));
            setStarted(true);
        }
        seqEvent.addEndEventListener(this);

        return seqEvent.call();
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node nodeEnded = (Node) e.getSource();
        LOGGER.debug("NWhen end event: {}", nodeEnded);
        if (!isStopping()) {
            // if all the events are received, launch the sequence of rules
            if (nodeEnded instanceof NodeEvents) {
                seqRules.addEndEventListener(this);
                seqRules.call();
            } else {
                setStarted(false);
                fireEndEvent(new EndEvent(this));
            }
        } else {
            LOGGER.warn("endEvent has been fired while the node was stopping");
        }
    }

    @Override
    protected void specificStop() {
        seqEvent.removeEndEventListener(this);
        seqEvent.stop();
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
            o.put("type", "when");
            o.put("seqRulesThen", seqRules.getJSONDescription());
            o.put("events", seqEvent.getJSONDescription());
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    @Override
    public String getExpertProgramScript() {
        return "when " + seqEvent.getExpertProgramScript() + " then \n{" + seqRules.getExpertProgramScript() + "\n}";
    }

    @Override
    protected Node copy(Node parent) {
        NodeWhen ret = new NodeWhen(parent);
        ret.setSymbolTable(this.getSymbolTable());
        ret.seqEvent = (NodeEvents) seqEvent.copy(ret);
        ret.seqRules = seqRules.copy(ret);
        return ret;

    }

}
