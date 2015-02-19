package appsgate.lig.ehmi.spec;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsGate.lig.manager.client.communication.service.subscribe.CommandListener;
import appsgate.lig.chmi.spec.GenericCommand;
import appsgate.lig.ehmi.spec.listeners.CoreListener;

/*
 * This is a mock used by test in the test methods
 */
public class EHMIProxyMock implements EHMIProxySpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(EHMIProxyMock.class);

    protected Library lib;

    /**
     * Constructor
     *
     * @param filepath
     * @throws JSONException
     */
    public EHMIProxyMock(String filepath) throws JSONException {
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
    public StateDescription getEventsFromState(String objectId, String stateName) {
        try {
            return new StateDescription(lib.getStateForType("lamp", stateName));
        } catch (JSONException ex) {
            LOGGER.error("unable to find events for the given type [{}/{}]", objectId, stateName);
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
        public void addCoreListener(CoreListener coreListener) {
            list.add(coreListener);
            System.out.println("Listener added: " + coreListener.getObjectId());
        }

        @Override
        public void deleteCoreListener(CoreListener coreListener) {
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
        public ArrayList<String> getDevicesInSpaces(ArrayList<String> typeList,
                ArrayList<String> spaces) {
            return new ArrayList<String>();
        }

    @Override
    public GrammarDescription getGrammarFromDevice(String deviceId) {
        return null;
    }

    @Override
    public JSONObject getGraph(Boolean buildGraph) {
        return null;
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

	@Override
	public JSONArray getDevices() {
		return new JSONArray();
	}


	@Override
	public JSONObject getDevice(String deviceId) {
		return null;
	}


	@Override
	public JSONArray getDevices(String type) {
		return null;
	}


	@Override
	public void setUserObjectName(String objectId, String user, String name) {
	}


	@Override
	public String getUserObjectName(String objectId, String user) {
		return null;
	}


	@Override
	public void deleteUserObjectName(String objectId, String user) {
	}


	@Override
	public boolean addGrammar(String deviceId, String deviceType, GrammarDescription grammarDescription) {
		return false;
	}


	@Override
	public boolean removeGrammar(String deviceType) {
		return false;
	}


	@Override
	public GrammarDescription getGrammarFromType(String deviceType) {
		return null;
	}


	@Override
	public JSONArray getPlaces() {
		return null;
	}


	@Override
	public void newPlace(JSONObject place) {
	}


	@Override
	public void updatePlace(JSONObject place) {
	}


	@Override
	public void removePlace(String id) {
	}


	@Override
	public void moveDevice(String objId, String srcPlaceId, String destPlaceId) {
	}


	@Override
	public void moveService(String serviceId, String srcPlaceId,
			String destPlaceId) {
	}


	@Override
	public String getCoreObjectPlaceId(String objId) {
		return null;
	}


	@Override
	public JSONArray getPlacesByName(String name) {
		return null;
	}


	@Override
	public JSONArray gePlacesWithTags(JSONArray tags) {
		return null;
	}


	@Override
	public JSONArray getPlacesWithProperties(JSONArray keys) {
		return null;
	}


	@Override
	public JSONArray getPlacesWithPropertiesValue(JSONArray properties) {
		return null;
	}


	@Override
	public JSONArray getRootPlaces() {
		return null;
	}


	@Override
	public boolean addTag(String placeId, String tag) {
		return false;
	}


	@Override
	public boolean removeTag(String placeId, String tag) {
		return false;
	}


	@Override
	public boolean addProperty(String placeId, String key, String value) {
		return false;
	}


	@Override
	public boolean removeProperty(String placeId, String key) {
		return false;
	}


	@Override
	public JSONArray getUsers() {
		return null;
	}


	@Override
	public boolean createUser(String id, String password, String lastName,
			String firstName, String role) {
		
		return false;
	}


	@Override
	public boolean deleteUser(String id, String password) {
		return false;
	}


	@Override
	public JSONObject getUserDetails(String id) {
		return null;
	}


	@Override
	public JSONObject getUserFullDetails(String id) {
		
		return null;
	}


	@Override
	public boolean checkIfIdIsFree(String id) {
		return false;
	}


	@Override
	public boolean synchronizeAccount(String id, String password,
			JSONObject accountDetails) {
		return false;
	}


	@Override
	public boolean desynchronizedAccount(String id, String password,
			JSONObject accountDetails) {
		return false;
	}


	@Override
	public boolean associateDevice(String id, String password, String deviceId) {
		return false;
	}


	@Override
	public boolean separateDevice(String id, String password, String deviceId) {
		return false;
	}

    @Override
    public JSONArray addLocationObserver(String location) {
		return null; 

    }

    @Override
    public JSONArray removeLocationObserver(String location) {
		return null; 

    }

    @Override
    public JSONArray getActiveLocationsObservers() {
        return null;
    }

    @Override
    public JSONArray getAllLocationsObservers() {
        return null;
    }


    @Override
	public boolean addProgram(JSONObject jsonProgram) {
		return false;
	}


	@Override
	public boolean removeProgram(String programId) {
		return false;
	}


	@Override
	public boolean updateProgram(JSONObject jsonProgram) {
		return false;
	}


	@Override
	public boolean callProgram(String programId) {
		return false;
	}


	@Override
	public boolean stopProgram(String programId) {
		return false;
	}


	@Override
	public JSONArray getPrograms() {
		return null;
	}


	@Override
	public boolean isProgramActive(String programId) {
		return false;
	}


	@Override
	public void shutdown() {
		
	}


	@Override
	public void restart() {
		
	}


	@Override
	public GenericCommand executeRemoteCommand(String objIdentifier,
			String method, JSONArray args) {
		return null;
	}


	@Override
	public long getCurrentTimeInMillis() {
		return 0;
	}


	@Override
	public boolean addClientConnexion(CommandListener cmdListener, String name,
			int port) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean removeClientConnexion(String name) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void sendFromConnection(String name, String msg) {
		// TODO Auto-generated method stub
	}


	@Override
	public void sendFromConnection(String name, int clientId, String msg) {
		// TODO Auto-generated method stub
	}


	@Override
	public int startDebugger() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean stopDebugger() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public JSONObject getTraceManStatus() {
		// TODO Auto-generated method stub
		return new JSONObject();

        }
        @Override
        public void scheduleProgram(String eventName, String programId,
			boolean startOnBegin, boolean stopOnEnd) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public JSONObject checkLocation(String location) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JSONArray checkLocationsStartingWith(String firstLetters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JSONArray addLocationObserverFromWOEID(String woeid) {
		// TODO Auto-generated method stub
		return null; 
		
	}


	@Override
	public Set<?> listEventsSchedulingProgramId(String programId,
			String startPeriod, String endPeriod) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean checkProgramIdScheduled(String programId) {
		// TODO Auto-generated method stub
		return false;
	}
        
        @Override
	public JSONArray checkProgramsScheduled() {
		// TODO Auto-generated method stub
		return null;
	}

}
