/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.ReferenceTable;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeWhile extends Node implements INodeRule {

    /**
     * Logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NodeWhile.class);

    /**
     * The state to check
     */
    private NodeState state;

    /**
     * The rules to apply when a state starts
     */
    private Node rules;

    /**
     * The rules to apply when a state ends
     */
    private Node rulesThen;

    /**
     * private constructor
     *
     * @param p
     */
    private NodeWhile(Node p) {
        super(p);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeWhile(JSONObject o, Node parent) throws SpokNodeException {
        super(parent, o);
        JSONObject stateJSON = o.optJSONObject("state");
        try {
            Node n = Builder.buildFromJSON(stateJSON, this);
            if (n instanceof NodeState) {
                this.state = (NodeState) n;
            } else {
                LOGGER.error("The node state is not a State node.");
                throw new SpokNodeException(this, "NodeWhile", "state", null);
            }
        } catch (SpokTypeException ex) {
            LOGGER.warn("There is no state to node {}", this);
            throw new SpokNodeException(this, "NodeWhile", "state", ex);
        }
        JSONObject rulesJSON = o.optJSONObject("rules");
        try {
            this.rules = Builder.buildFromJSON(rulesJSON, this);
        } catch (SpokTypeException ex) {
            LOGGER.warn("There is no rules to node {}", this);
            throw new SpokNodeException(this, "NodeWhile", "rules", ex);
        }
        JSONObject thenJSON = o.optJSONObject("rulesThen");
        try {
            this.rulesThen = Builder.nodeOrNull(thenJSON, this);
        } catch (SpokTypeException ex) {
            LOGGER.trace("There is no rulesThen");
            this.rulesThen = null;
        }
    }

    @Override
    protected void specificStop() {
        state.removeEndEventListener(this);
        state.removeStartEventListener(this);
        state.stop();
        rules.removeEndEventListener(this);
        rules.stop();
        if (rulesThen != null) {
            rulesThen.removeEndEventListener(this);
            rulesThen.stop();
        }
    }

    @Override
    public JSONObject call() {
        setStarted(true);
        fireStartEvent(new StartEvent(this));
        state.addStartEventListener(this);
        JSONObject ret = state.call();
        return ret;
    }

    @Override
    public String getExpertProgramScript() {
        if (rulesThen == null) {
            return "while(" + state.getExpertProgramScript() + ") {\n" + rules.getExpertProgramScript() + "\n}";

        } else {
            return "while(" + state.getExpertProgramScript() + ") {\n" + rules.getExpertProgramScript() + "\n}\n {\n" + rulesThen.getExpertProgramScript() + "}";
        }
    }

    @Override
    protected Node copy(Node parent) {
        NodeWhile n = new NodeWhile(parent);
        if (rulesThen != null) {
            n.rulesThen = (NodeSeqRules) rulesThen.copy(n);
        }
        n.rules = (NodeSeqRules) rules.copy(n);
        n.state = (NodeState) state.copy(n);
        return n;
    }

    @Override
    public void startEventFired(StartEvent e) {
        Node node = (Node) e.getSource();
        if (node == state) {
            state.addEndEventListener(this);
            rules.addEndEventListener(this);
            rules.call();
        }
    }

    @Override
    public void endEventFired(EndEvent e) {
        Node node = (Node) e.getSource();
        if (node == state) {
            // We got end event from the states node, we start the then branch
            rules.stop();
            if (rulesThen != null) {
                rulesThen.addEndEventListener(this);
                rulesThen.call();
            } else {
                stop();
                setStarted(false);
                fireEndEvent(new EndEvent(this));
            }
        }

        if (node == rules) {
            LOGGER.trace("The rules have been done");
            if (rulesThen == null) {
                stopWaiting();
            }
        }
        if (node == rulesThen) {
            LOGGER.trace("Then rules have been done");
            setStarted(false);
            fireEndEvent(new EndEvent(this));
        }
    }

    @Override
    public JSONObject getJSONDescription() {
        JSONObject o = super.getJSONDescription();
        try {
            o.put("type", "while");
            o.put("state", state.getJSONDescription());
            o.put("rules", rules.getJSONDescription());
            if (rulesThen != null) {
                o.put("rulesThen", rulesThen.getJSONDescription());
            }
        } catch (JSONException e) {
            // Do nothing since 'JSONObject.put(key,val)' would raise an exception
            // only if the key is null, which will never be the case
        }

        return o;
    }

    /**
     * Method to process the end of waiting
     */
    private void stopWaiting() {
        setStarted(false);
        fireEndEvent(new EndEvent(this));
    }

    @Override
    public String getTypeSpec() {
        if (state != null) {
            return "While " + state.getName();
        }
        return "While (null)";
    }

    @Override
    protected void buildReferences(ReferenceTable table) {
        if (this.rules != null) {
            this.rules.buildReferences(table);
        }
        if (this.rulesThen != null) {
            this.rulesThen.buildReferences(table);
        }
        if (this.state != null) {
            this.state.buildReferences(table);
        }
    }
}
