package appsgate.lig.core.object.spec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cédric Gérard
 * @since February 26, 2014
 *
 * This abstract class is use to push behavior in core object implementation. It
 * is a singleton dependency share through ApAM that guarantee the same shared
 * behavior of each instance whatever the implementation.
 */
public abstract class CoreObjectBehavior implements CoreObjectSpec {

    private final static Logger LOGGER = LoggerFactory.getLogger(CoreObjectBehavior.class);

    private JSONObject grammar = null;

    /**
     * @return the grammar description of an object
     */
    @Override
    public JSONObject getBehaviorDescription() {
        if (this.grammar == null) {
            InputStream stream = this.getClass().getResourceAsStream("grammar.json");
            try {
                this.grammar = loadJSONStream(stream);
            } catch (IOException ex) {
                LOGGER.error("Unable to read the file");
                this.grammar = null;
            } catch (JSONException ex) {
                LOGGER.error("Unable to parse the file");
                this.grammar = null;
            }
        }
        return this.grammar;

    }

    /**
     *
     * @param in
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject loadJSONStream(InputStream in) throws IOException, JSONException {
        if (in == null) {
            LOGGER.debug("No grammar file found for {}", this.getClass().toString());
            return null;
        }
        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();

        while (read != null) {
            //System.out.println(read);
            sb.append(read);
            read = br.readLine();

        }

        return new JSONObject(sb.toString());

    }
}
