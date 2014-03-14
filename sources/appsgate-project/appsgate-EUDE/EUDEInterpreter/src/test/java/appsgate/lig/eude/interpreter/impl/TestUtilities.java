/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package appsgate.lig.eude.interpreter.impl;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jr
 */
public class TestUtilities {
    
    /**
     * Load a file and return its content
     *
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject loadFileJSON(String filename) throws FileNotFoundException, IOException, JSONException {
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
