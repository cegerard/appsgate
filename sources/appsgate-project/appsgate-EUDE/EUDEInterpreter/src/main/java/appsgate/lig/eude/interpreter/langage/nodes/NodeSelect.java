package appsgate.lig.eude.interpreter.langage.nodes;

import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.SpokPlace;
import appsgate.lig.eude.interpreter.langage.components.SpokType;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokException;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class NodeSelect extends Node {

    private ArrayList<SpokType> what;
    private ArrayList<SpokPlace> where;

    public NodeSelect(Node p) {
        super(p);
    }

    @Override
    protected void specificStop() throws SpokException {
    }

    @Override
    public String getExpertProgramScript() {
        return "SELECT LANGUAGE NOT IMPLEMENTED YET";
    }

    @Override
    public void endEventFired(EndEvent e) {
    }

    @Override
    protected Node copy(Node parent) {
        NodeSelect ret = new NodeSelect(parent);
        ret.what = new ArrayList<SpokType>(what);
        ret.where = new ArrayList<SpokPlace>(where);
        return ret;
    }

    @Override
    public JSONObject call() {
        // TODO
        // depending on the what and the where of the context, it return the correct values
        return null;
    }

    @Override
    JSONObject getJSONDescription() {
        return new JSONObject();
    }

}
