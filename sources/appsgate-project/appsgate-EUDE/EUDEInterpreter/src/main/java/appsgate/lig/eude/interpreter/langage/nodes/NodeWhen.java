package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
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
     * The seq of event to catch to have run the when
     */
    private final NodeSeqEvent seqEvent;
    /**
     * The sequence of thing to do once the events are done
     */
    private final NodeSeqRules seqRules;

    /**
     * Default constructor. Instantiate a node when
     *
     * @param interpreter Pointer on the interpreter
     * @param ruleWhenJSON
     * @throws NodeException
     */
    public NodeWhen(EUDEInterpreterImpl interpreter, JSONObject ruleWhenJSON) throws NodeException {
        super(interpreter);

        // initialize the sequences of events and rules
        seqEvent = new NodeSeqEvent(interpreter, getJSONArray(ruleWhenJSON, "events"));
        seqRules = new NodeSeqRules(interpreter, getJSONArray(ruleWhenJSON, "seqRulesThen"));

    }

    @Override
    public Integer call() {
        LOGGER.debug("Call {}", this);
        if (!isStarted()) {
            fireStartEvent(new StartEvent(this));
            setStarted(true);
        }
        seqEvent.addEndEventListener(this);
        seqEvent.call();

        return null;
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node nodeEnded = (Node) e.getSource();
        LOGGER.debug("NWhen end event: {}", nodeEnded);
        if (!isStopping()) {
            // if all the events are received, launch the sequence of rules
            if (nodeEnded == seqEvent) {
                seqRules.addEndEventListener(this);

                LOGGER.debug("###### all the events are received, launching the sequence of rules #######");
                try {
                    seqRules.call();
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                }
                setStarted(false);
                fireEndEvent(new EndEvent(this));
                // if the sequence of rules is terminated, fire the event event
            } else {
                LOGGER.debug("###### Rules are done");
            }
        } else {
            LOGGER.debug("###### is stopping");

        }
    }

    @Override
    public void stop() {
        if (isStarted()) {
            setStopping(true);
            seqEvent.removeEndEventListener(this);
            seqRules.removeEndEventListener(this);
            seqEvent.stop();
            seqRules.stop();

            setStarted(false);
            setStopping(false);
        }

    }

    @Override
    public String toString() {
        return "[Node When: events(" + seqEvent.toString() + "), rules(" + seqRules + ")]";
    }
}
