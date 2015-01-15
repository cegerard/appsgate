package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import java.util.HashMap;
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
    private NodeState state;

    /**
     *
     */
    private NodeAction setter;

    /**
     * Private constructor to allow copy
     *
     * @param p the parent node
     */
    private NodeKeepState(Node p) {
        super(p);
    }

    /**
     * 
     * @param o
     * @param parent
     * @throws SpokNodeException 
     */
    public NodeKeepState(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        try {
            state = (NodeState) Builder.buildFromJSON(o.getJSONObject("state"), this);
        } catch (JSONException ex) {
            throw new SpokNodeException(this, "NodeKeepState", "state", ex);
        } catch (SpokTypeException ex) {
            throw new SpokNodeException(this, "NodeKeepState", "state", ex);
        }
    }

    @Override
    protected void specificStop() {
        state.removeEndEventListener(this);
        state.stop();
        if (setter != null) {
            setter.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        try {
            state.call();
            setter = state.getSetter();
        } catch (SpokException ex) {
            LOGGER.error("Unable to get");
        }

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
        LOGGER.debug("not in state anymore, trying to relaunch it");
        setter.call();
        listenEndEvents();
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
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
        state.addEndEventListener(this);
        state.call();
    }

    /**
     *
     * @return the state
     */
    public NodeState getState() {
        return state;
    }
    @Override
    protected void buildReferences(ReferenceTable table, HashMap<String,String> args) {
        if (this.state != null) {
            this.state.buildReferences(table, null);
        }
        if (this.setter != null) {
            this.setter.buildReferences(table, null);
        }
    }

    @Override
    public String getTypeSpec() {
        return "KeepState: " + state.getName();
    }

}
