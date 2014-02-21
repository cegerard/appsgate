package appsgate.lig.context.proxy.spec;

import appsgate.lig.context.proxy.listeners.CoreListener;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextProxyMock implements ContextProxySpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextProxyMock.class);

    protected Library lib;

    /**
     * Constructor
     *
     * @param filepath
     * @throws JSONException
     */
    public ContextProxyMock(String filepath) throws JSONException {
        super();
        lib = new Library();

        try {
            lib.addDesc(loadFileJSON(filepath));
        } catch (IOException ex) {
            LOGGER.error("error while loading file");
        } catch (JSONException ex) {
            LOGGER.error("error while parsing file");
        }
    }

    @Override
    public String getBrickType(String targetId) {
        return "lamp";
    }

    @Override
    public StateDescription getEventsFromState(String type, String stateName) {
        try {
            return new StateDescription(lib.getStateForType(type, stateName));
        } catch (JSONException ex) {
            LOGGER.error("unable to find events for the given type [{}/{}]", type, stateName);
            return null;
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

        private final ConcurrentLinkedQueue<CoreListener> list = new ConcurrentLinkedQueue<CoreListener>();

        @Override
        public void addListener(CoreListener coreListener) {
            list.add(coreListener);
            System.out.println("Listener added: " + coreListener.getObjectId());
        }

        @Override
        public void deleteListener(CoreListener coreListener) {
            System.out.println("removing listener: " + coreListener.getObjectId());
            list.remove(coreListener);
        }

        public void notifAll(String msg) {
            System.out.println("NotifAll Start " + msg);
            ConcurrentLinkedQueue<CoreListener> buf = new ConcurrentLinkedQueue<CoreListener>();
            for (CoreListener l : list) {
                buf.add(l);
            }
            for (CoreListener l1 : buf) {
                l1.notifyEvent();
            }
            System.out.println("NotifAll End " + msg);

        }
        @Override
        public List<String> getSubtypes(List<String> typeList) {
            return null;
        }

        @Override
        public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList,
                ArrayList<String> spaces) {
            return new ArrayList<String>();
        }

    public final class Library {

        /**
         * Static class member uses to log what happened in each instances
         */
        private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Library.class);

        /**
         *
         */
        private final HashMap<String, JSONObject> root;

        /**
         * Constructor
         */
        public Library() {
            root = new HashMap<String, JSONObject>();
        }

        /**
         *
         * @param o
         */
        public void addDesc(JSONObject o) {
            if (!o.has("typename")) {
                LOGGER.error("The description has no type name");
                return;
            }
            addDescForType(o.optString("typename"), o);
        }

        /**
         *
         * @param type
         * @param o
         */
        public void addDescForType(String type, JSONObject o) {
            root.put(type, o);
        }

        /**
         *
         * @param type
         * @param stateName
         * @return
         * @throws JSONException
         */
        public JSONObject getStateForType(String type, String stateName) throws JSONException {
            JSONObject desc = getDescriptionFromType(type);
            if (desc == null) {
                LOGGER.error("No description found for this type");
                return null;
            }
            JSONArray array;
            try {
                array = desc.getJSONArray("states");
            } catch (JSONException ex) {
                LOGGER.error("unable to find the states definition.");
                return null;
            }

            for (int i = 0; i < array.length(); i++) {
                if (array.getJSONObject(i).getString("name").equalsIgnoreCase(stateName)) {
                    return array.getJSONObject(i);
                }
            }
            LOGGER.error("State not found: {}", stateName);
            return null;
        }

        /**
         *
         * @param type
         * @return
         */
        public JSONObject getDescriptionFromType(String type) {

            if (root == null) {
                LOGGER.error("The library is not inited");
                return null;
            }
            if (root.containsKey(type)) {
                return root.get(type);
            }
            LOGGER.error("type [{}] not found in library", type);
            return null;

        }

    }

}
