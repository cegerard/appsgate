package appsgate.lig.ehmi.trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jr
 */
public class TraceFile implements TraceHistory {
    /**
     * The logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TraceFile.class);

    private int cptTrace = 0;
        /**
     * The printWriter for the trace file on the hard drive
     */
    private PrintWriter traceFileWriter;

    /**
     * Constructor
     */
    public TraceFile() {
    }
    
    @Override
    public void close() {
        if (this.traceFileWriter != null) {
            this.traceFileWriter.println();
            this.traceFileWriter.print("]");
            this.traceFileWriter.flush();
            this.traceFileWriter.close();
        }
    }

    @Override
    public synchronized void trace(JSONObject o) {
        if (cptTrace > 0) { //For all trace after the first 
            traceFileWriter.println(",");
            traceFileWriter.print(o.toString());
        } else { //For the first trace
            traceFileWriter.print(o.toString());
        }
        this.traceFileWriter.flush();
        
        cptTrace++;
    }

    @Override
    public JSONArray get(Long timestamp, Integer count) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    @Override
    public JSONArray getInterval(Long start, Long end) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public JSONArray getLastState(JSONArray ids, Long timestamp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean init() {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(Calendar.getInstance().getTime());
            traceFileWriter = new PrintWriter("traceMan-" + date + ".json");
            traceFileWriter.println("[");
            return true;
        } catch (FileNotFoundException ex) {
            this.traceFileWriter = null;
            LOGGER.error("Unable to open trace file");
        }
        return false;
    }

    @Override
    public void addExecutionTrace(Long timestamp, String pid, String node_id) {
    }

    
}
