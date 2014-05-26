package appsgate.lig.core.tests;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.StateDescription;
import appsgate.lig.eude.interpreter.langage.exceptions.SpokTypeException;
import appsgate.lig.eude.interpreter.langage.nodes.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

/**
 *
 * @author jr
 */
public class CoreObjectBehaviorTest {

    private final CoreObjectBehavior instance;

    private int nbError = 0;

    private JSONObject target = null;

    public CoreObjectBehaviorTest(CoreObjectBehavior i, String to) {
        this.instance = i;
        try {
            this.target = new JSONObject("{'type':'device', 'value':'test'}");
        } catch (JSONException ex) {
        }
    }

    public int testMethod() {
        JSONObject desc = instance.getGrammarDescription();
        if (desc == null) {
            Assert.fail("Unable to load description");
        }
        checkStates(desc);
        return nbError;
    }

    private void checkStates(JSONObject o) {
        try {
            JSONArray states = o.getJSONArray("states");
            for (int i = 0; i < states.length(); i++) {
                checkState(new StateDescription(states.getJSONObject(i)));
            }
        } catch (JSONException ex) {
            System.out.println("Unable to load the json");
            System.out.println(ex.getCause());
            Assert.fail("JSON not correctly loaded");
        }
    }

    /**
     *
     * @param desc
     */
    private void checkState(StateDescription desc) {
        Assert.assertNotNull("StateDescription is null", desc);
        String name = desc.getName();
        Assert.assertNotNull("The name is null", name);
        System.out.println("***** Checking state: " + name + " *****");
        checkNode(desc.getStartEvent(), "start event");
        checkNode(desc.getEndEvent(), "end event");
        //checkNode(desc.getSetter(), "Setter");
        checkOk("No state name", desc.getStateName() != null);
        checkOk("No state value", desc.getStateValue() != null);
    }

    private void checkNode(JSONObject o, String ev) {
        try {
            if (o == null) {
                nbError++;
                System.out.println(" - No " + ev);
                return;
            }
            Builder.buildFromJSON(o, null, target);
        } catch (SpokTypeException ex) {
            nbError++;
            System.out.println(" - UNABLE to load node (" + ev + ")");
        }
    }

    private void checkOk(String errorMessage, Boolean b) {
        if (b) {
            return;
        }
        System.out.println(" - " + errorMessage);
        nbError++;
    }
}
