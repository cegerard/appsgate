package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.impl.EUDEInterpreterImpl;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;

/**
 * Node for the when
 *
 * @author Rémy Dautriche
 * @author Cédric Gérard
 *
 * @since June 25, 2013
 * @version 1.0.0
 */
public class NodeWhen extends Node {
	// <nodeWhen> ::= when ( <nodeEvent> {, <nodeEvent> } ) then <seqAndRules>

    private final NodeSeqEvent seqEvent;
    private final NodeSeqRules seqRules;

    /**
     * Default constructor. Instantiate a node when
     *
     * @param interpreter Pointer on the interpreter
     * @param ruleWhenJSON
     * @throws JSONException
     */
    public NodeWhen(EUDEInterpreterImpl interpreter, JSONObject ruleWhenJSON) throws JSONException {
        super(interpreter);

        // initialize the sequences of events and rules
        seqEvent = new NodeSeqEvent(interpreter, ruleWhenJSON.getJSONArray("events"));
        seqRules = new NodeSeqRules(interpreter, ruleWhenJSON.getJSONArray("seqRulesThen"));

		// initialize the pool
        //pool = Executors.newCachedThreadPool();
    }

    @Override
    public Integer call() {
        fireStartEvent(new StartEvent(this));
        started = true;

        seqEvent.addEndEventListener(this);
        seqEvent.call();
		//pool.submit(seqEvent);
        // super.call();

        return null;
    }

    @Override
    public void startEventFired(StartEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node nodeEnded = (Node) e.getSource();
        nodeEnded.removeEndEventListener(this);

        if (!stopping) {
            // if all the events are received, launch the sequence of rules
            if (nodeEnded == seqEvent) {
                seqRules.addEndEventListener(this);

                System.out.println("###### all the events are received, launching the sequence of rules #######");
                seqRules.call();
				//pool.submit(seqRules);
                //super.call();
                started = false;
                fireEndEvent(new EndEvent(this));
                // if the sequence of rules is terminated, fire the event event
            } else {
                started = false;
                fireEndEvent(new EndEvent(this));
            }
        }
    }

    @Override
    public void undeploy() {
		// TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        if (started) {
            stopping = true;

            seqEvent.removeEndEventListener(this);
            seqEvent.stop();
            seqRules.stop();

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
