package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokExecutionException;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokNodeException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class NodeEventsOr extends NodeEvents {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventsOr.class);

    /**
     * The number of event to occur
     */
    private Integer nbEventToOccur;

    /**
     * The number of events that have ended
     */
    private int nbEventEnded;

    /**
     * Default constructor for copy
     *
     * @param p parent node
     */
    private NodeEventsOr(Node p, String iid) {
        super(p, iid);
    }

    /**
     *
     * @param o
     * @param parent
     * @throws SpokNodeException
     */
    public NodeEventsOr(JSONObject o, Node parent) throws SpokNodeException {
        super(o, parent);
        nbEventToOccur = o.optInt("nbEventToOccur");
        if (nbEventToOccur == null) {
            nbEventToOccur = listOfEvent.size();
        }
        // If we just want one event among the events, no need to wait for a duration
        if (nbEventToOccur > 1) {
            duration = o.optInt("duration");
        }

    }

    @Override
    protected void specificCall() {
        nbEventEnded = 0;
        super.specificCall();
    }

    @Override
    public String getExpertProgramScript() {
        return "[" + StringUtils.join(listOfEvent, " OR ") + "]";
    }

    @Override
    protected Node copy(Node parent) {
        NodeEventsOr ret = new NodeEventsOr(parent, getIID());
        ret.nbEventToOccur = nbEventToOccur;
        return commonCopy(ret);
    }

    @Override
    JSONObject specificDesc(JSONObject ret) throws JSONException {
        ret.put("type", "eventsOr");
        ret.put("nbEventToOccur", nbEventToOccur);
        return ret;
    }

    @Override
    void dealWithClockEvent(NodeEvent e) throws SpokExecutionException {
        nbEventEnded--;
    }

    @Override
    void dealWithNormalEvent(NodeEvent e) throws SpokExecutionException {
        nbEventEnded++;
        if (nbEventEnded >= nbEventToOccur) {
            LOGGER.debug("All the events have been ended");
            stop();
            fireEndEvent(new EndEvent(this));
        } else {
            LOGGER.debug("All the events have not been ended");
            e.addEndEventListener(this);
            e.call();
            startClockEvent(getDuration());
        }
    }
}
