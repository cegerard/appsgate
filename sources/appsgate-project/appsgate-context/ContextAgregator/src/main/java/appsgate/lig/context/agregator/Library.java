/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.context.agregator;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public final class Library {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Library.class);

    /**
     *
     */
    private JSONObject root;

    /**
     * Constructor
     */
    public Library() {
        readFile();
    }

    /**
     *
     */
    public void readFile() {
        try {
            root = loadFileJSON("src/test/resources/jsonLibs/toto.json");
        } catch (IOException ex) {
            LOGGER.error("Unable to load file:", ex);
        } catch (JSONException ex) {
            LOGGER.error("File not in the correct format: {}", ex);
        }
    }

    /**
     *
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject loadFileJSON(String filename) throws FileNotFoundException, IOException, JSONException {
        FileInputStream fis = new FileInputStream(filename);
        DataInputStream dis = new DataInputStream(fis);

        byte[] buf = new byte[dis.available()];
        dis.readFully(buf);

        String fileContent = "";
        for (byte b : buf) {
            fileContent += (char) b;
        }

        dis.close();
        fis.close();

        return new JSONObject(fileContent);
    }

    /**
     *
     * @param type
     * @param stateName
     * @return
     * @throws JSONException
     */
    public JSONObject getStateForType(String type, String stateName) throws JSONException {
        try {
            JSONArray array = root.getJSONArray("states");
            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
                    return array.getJSONObject(i);
                }
            }
        } catch (JSONException ex) {
            LOGGER.error("unable to find the states definition.");
        }
        LOGGER.error("State not found: {}", stateName);
        return null;
    }



}
