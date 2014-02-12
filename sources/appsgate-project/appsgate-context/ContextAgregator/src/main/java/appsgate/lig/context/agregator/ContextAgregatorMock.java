package appsgate.lig.context.agregator;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextAgregatorMock extends ContextAgregatorImpl {
    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextAgregatorMock.class);


    /**
     * Constructor
     * @param filepath
     */

    public ContextAgregatorMock(String filepath) {
        super();
        try {
            lib.addDesc(loadFileJSON(filepath));
        } catch (IOException ex) {
            LOGGER.error("error while loading file");
        } catch (JSONException ex) {
            LOGGER.error("error while parsing file");
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


}
