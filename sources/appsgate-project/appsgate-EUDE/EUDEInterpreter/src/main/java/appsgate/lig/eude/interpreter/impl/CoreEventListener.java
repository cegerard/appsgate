package appsgate.lig.eude.interpreter.impl;

import appsgate.lig.ehmi.spec.listeners.CoreListener;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import java.util.EventObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 * @since July 3, 2014
 */
public class CoreEventListener implements CoreListener {

    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CoreEventListener.class);

    /**
     *
     */
    private String objectId;
    /**
     *
     */
    private String varName;
    /**
     *
     */
    private String varValue;

    /**
     *
     */
    private final ConcurrentLinkedQueue<NodeEvent> nodeEventList;

    /**
     *
     * @param objectId
     * @param varName
     * @param varValue
     * @param eudeInt
     */
    public CoreEventListener(String objectId, String varName, String varValue) {
        this.objectId = objectId;
        this.varName = varName;
        this.varValue = varValue;
        this.nodeEventList = new ConcurrentLinkedQueue<NodeEvent>();
    }

    /**
     * @param e the node to add
     */
    public void addNodeEvent(NodeEvent e) {
        nodeEventList.add(e);
    }

    /**
     * @param e, the node to remove
     */
    public void removeNodeEvent(NodeEvent e) {
        boolean goon = true;
        while (goon) {
            goon = nodeEventList.remove(e);
        }
    }

    @Override
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public void setEvent(String eventVarName) {
        this.varName = eventVarName;
    }

    @Override
    public void setValue(String eventVarValue) {
        this.varValue = eventVarValue;
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public String getEvent() {
        return varName;
    }

    @Override
    public String getValue() {
        return varValue;
    }

    @Override
    public void notifyEvent() {
        LOGGER.trace("Event notified: {}", this);
        // transmit the core event to the concerned nodes
        if (nodeEventList == null) {
            LOGGER.warn("No CoreEvent found");
            return;
        }
        int nbListeners = nodeEventList.size();
        for (int i = 0; i < nbListeners; i++) {
            NodeEvent l = nodeEventList.poll();
            l.coreEventFired();
        }

    }

    @Override
    public void notifyEvent(CoreListener listener) {
        LOGGER.debug("The event is catch by the EUDE " + listener);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CoreEventListener)) {
            return false;
        }
        CoreEventListener c = (CoreEventListener) o;
        return (objectId.contentEquals(c.objectId) && varName.contentEquals(c.varName) && varValue.contentEquals(c.varValue));
    }

    public boolean equals(EndEvent e) {
        NodeProgram p = getProgramNodeFromEvent(e);
        if (p == null) {
            return false;
        }
        return (objectId.contentEquals(p.getId()) && varName.contentEquals("runningState") && varValue.contentEquals("stop"));
    }

    public boolean equals(StartEvent e) {
        NodeProgram p = getProgramNodeFromEvent(e);
        if (p == null) {
            return false;
        }
        return (objectId.contentEquals(p.getId()) && varName.contentEquals("runningState") && varValue.contentEquals("start"));
    }

    private NodeProgram getProgramNodeFromEvent(EventObject e) {
        if (e.getSource() instanceof NodeProgram) {
            return (NodeProgram) e.getSource();
        } else {
            return null;
        }
    }

    /**
     * @return true if the list is empty
     */
    public Boolean isEmpty() {
        return nodeEventList.isEmpty();
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.objectId != null ? this.objectId.hashCode() : 0);
        hash = 17 * hash + (this.varName != null ? this.varName.hashCode() : 0);
        hash = 17 * hash + (this.varValue != null ? this.varValue.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "[Event: " + this.objectId + ", " + this.varName + "/" + this.varValue + "]";
    }
}
