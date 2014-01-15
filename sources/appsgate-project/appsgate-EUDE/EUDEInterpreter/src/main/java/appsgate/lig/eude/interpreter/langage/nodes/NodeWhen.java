package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.exceptions.NodeException;
import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.SymbolTable;
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
     * @param interpreter Pointer on the interpreter
     * @param ruleWhenJSON
     * @param parent
     * @throws NodeException
     */
    public NodeWhen(EUDEInterpreterImpl interpreter, JSONObject ruleWhenJSON, Node parent) throws NodeException {
        super(interpreter, parent);
        seqEvent = new ArrayList<NodeEvent>();
        JSONArray seqEventJSON = getJSONArray(ruleWhenJSON, "events");
        for (int i = 0; i < seqEventJSON.length(); i++) {
            try {
                seqEvent.add(new NodeEvent(interpreter, seqEventJSON.getJSONObject(i), this));
            } catch (JSONException ex) {
                throw new NodeException("NodeSeqEvent", "item " + i, ex);
            }
        }

        // initialize the sequences of events and rules
        seqRules = new NodeSeqRules(interpreter, getJSONArray(ruleWhenJSON, "seqRulesThen"), this);

    }

    /**
     * private constructor to copy node
     *
     * @param interpreter
     * @param parent
     */
    private NodeWhen(EUDEInterpreterImpl interpreter, Node parent) {
        super(interpreter, parent);
    }

    @Override
    public Integer call() {
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
    protected void specificStop() {
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
    Node copy(Node parent) {
        NodeWhen ret = new NodeWhen(getInterpreter(), parent);
        ret.setSymbolTable(this.getSymbolTable());
        ret.seqEvent = new ArrayList<NodeEvent>();
        for (NodeEvent n : seqEvent) {
            ret.seqEvent.add((NodeEvent) n.copy(ret));
        }
        ret.seqRules = (NodeSeqRules) seqRules.copy(ret);
        return ret;

    }

}
