package appsgate.lig.core.object.spec;

import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class CoreObjectBehaviorTest {

    private final CoreObjectBehavior instance;

    public CoreObjectBehaviorTest(CoreObjectBehavior i, String to) {
        this.instance = i;
    }

    public String testMethod() {
        JSONObject desc = instance.getGrammarDescription();
        if (desc == null) {
            return "Unable to load description";
        }
        return "";
    }

}
