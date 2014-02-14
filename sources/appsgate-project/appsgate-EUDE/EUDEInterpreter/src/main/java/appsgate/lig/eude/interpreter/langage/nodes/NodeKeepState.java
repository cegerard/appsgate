package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeKeepState extends Node {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeKeepState.class);

    /**
     *
     */
    protected NodeState state;

    /**
     *
     */
    private NodeAction setter;

    /**
     * Private constructor to allow copy
     *
     * @param p
     */
    private NodeKeepState(Node p) {
        super(p);
    }

    public NodeKeepState(JSONObject o, Node parent) throws SpokNodeException {
        super(parent);
        try {
            state = new NodeState(o.getJSONObject("state"), this);
            setter = state.getSetter();
        } catch (JSONException ex) {
            throw new SpokNodeException("NodeKeepState", "state", ex);
        } catch (SpokExecutionException ex) {
            LOGGER.error("Unable to get setter from state");
            throw new SpokNodeException("NodeKeepState", "state", ex);
        }
    }

    @Override
    protected void specificStop() {
        state.removeEndEventListener(this);
        state.stop();
        setter.stop();
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        if (!state.isOnRules()) {
            setter.call();
        }
        listenEndEvents();
        return null;
    }

    @Override
    public String getExpertProgramScript() {
        return "keep(" + state.getObjectId() + "," + state.getName() + ")";
    }

    @Override
    protected Node copy(Node parent) {
        NodeKeepState n = new NodeKeepState(parent);
        n.setter = (NodeAction) setter.copy(n);
        n.state = (NodeState) state.copy(n);
        return n;
    }

    @Override
    public void endEventFired(EndEvent e) {
        LOGGER.trace("An event has been catched");
        if (state.isOnRules()) {
            LOGGER.trace("We are still in state");
            return;
        }
        LOGGER.debug("no more in state, trying to relaunch it");
        setter.call();
        listenEndEvents();
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = new JSONObject();
        try {
            o.put("type", "keepState");
            o.put("state", state.getJSONDescription());
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }
        return o;
    }

    /**
     *
     */
    private void listenEndEvents() {
        state.call();
        state.addEndEventListener(this);
    }

}
