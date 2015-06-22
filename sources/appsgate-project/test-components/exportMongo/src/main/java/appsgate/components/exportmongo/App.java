package appsgate.components.exportmongo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import org.json.JSONException;

/**
 *
 * @author jr
 */
public class App {

    private static final String DEFAULT_PROGRAMS_FILE = "programs.csv";
    private static final String DEFAULT_DEVICES_FILE = "devices.csv";

    public static void main(String[] args) {
        PrintWriter programs_w, devices_w;

        try {
            programs_w = openFile(DEFAULT_PROGRAMS_FILE);
            devices_w = openFile(DEFAULT_DEVICES_FILE);
        } catch (IOException ex) {
            return;
        }

        CSVExporter exp;
        try {
            exp = new CSVExporter(programs_w, devices_w);
        } catch (UnknownHostException ex) {
            return;
        }
        try {
            exp.parseTraces("EUDEMediator");
        } catch (JSONException ex) {

        }
    }

    /**
     * Method to open a file
     * 
     * @param filename
     * @return
     * @throws IOException 
     */
    private static PrintWriter openFile(String filename) throws IOException {
        PrintWriter pw;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
        } catch (IOException ex) {
            System.err.println("Error during opening file " + filename);
            System.err.println(ex.getMessage());
            throw ex;
        }
        return pw;

    }
}
