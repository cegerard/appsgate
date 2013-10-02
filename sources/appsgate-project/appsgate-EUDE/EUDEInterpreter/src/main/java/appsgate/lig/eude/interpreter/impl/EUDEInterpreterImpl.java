package appsgate.lig.eude.interpreter.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.follower.listeners.CoreListener;
import appsgate.lig.context.follower.spec.ContextFollowerSpec;
import appsgate.lig.context.history.services.DataBasePullService;
import appsgate.lig.context.history.services.DataBasePushService;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.eude.interpreter.langage.components.EndEvent;
import appsgate.lig.eude.interpreter.langage.components.EndEventListener;
import appsgate.lig.eude.interpreter.langage.components.StartEvent;
import appsgate.lig.eude.interpreter.langage.components.StartEventListener;
import appsgate.lig.eude.interpreter.langage.nodes.NodeEvent;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram;
import appsgate.lig.eude.interpreter.langage.nodes.NodeProgram.RUNNING_STATE;
import appsgate.lig.eude.interpreter.spec.EUDE_InterpreterSpec;
import appsgate.lig.router.spec.GenericCommand;
import appsgate.lig.router.spec.RouterApAMSpec;

/**
 * This class is the interpreter component for end user development environment. 
 * 
 * @author Cédric Gérard
 * @author Rémy Dautriche
 * 
 * @since  April 26, 2013
 * @version 1.0.0
 *
 */
public class EUDEInterpreterImpl implements EUDE_InterpreterSpec, StartEventListener, EndEventListener {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(EUDEInterpreterImpl.class);

	/** 
	 * Reference to the ApAM context follower. Used to be notified when
	 * something happen.
	 */
	private ContextFollowerSpec contextFollower;
	
	/**
	 * Reference to the ApAM router. Used to send action to the objects
	 */
	private RouterApAMSpec router;
	
	/**
	 * Context history pull service to get past table state
	 */
	private DataBasePullService contextHistory_pull;
	
	/**
	 * Context history push service to save the current state
	 */
	private DataBasePushService contextHistory_push;
	
	/** 
	 * Hash map containing the nodes and the events they are listening 
	 */
	private HashMap<CoreEventListener, ArrayList<NodeEvent>> mapCoreNodeEvent;
	
	/**
	 * HashMap that contains all the existing programs under a JSON format
	 */
	private HashMap<String, NodeProgram> mapPrograms;
	
	/**
	 * Initialize the list of programs and of events
	 * @constructor
	 */
	public EUDEInterpreterImpl() {
		mapPrograms = new HashMap<String, NodeProgram>();
		mapCoreNodeEvent = new HashMap<CoreEventListener, ArrayList<NodeEvent>>();
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		
		logger.debug("Restore interpreter program list from database");
		JSONObject userbase = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
		if(userbase != null){
			try {
				JSONArray state = userbase.getJSONArray("state");
				int length = state.length();
				int i = 0;
				NodeProgram np;
				while(i < length) {
					JSONObject obj = state.getJSONObject(i);
					String key = (String)obj.keys().next();
					np = new NodeProgram(this, new JSONObject(obj.getString(key)));
					mapPrograms.put(key, np);
					if(np.getRunningState() == RUNNING_STATE.STARTED) {
						//TODO:Restore complete interpreter and programs state
						this.callProgram(np.getId());
					}
					i++;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		logger.debug("The interpreter component is initialized");
	}
	
	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The router interpreter components has been stopped");
		// delete the event listeners from the context
		for (CoreEventListener listener : mapCoreNodeEvent.keySet()) {
			contextFollower.deleteListener(listener);
		}
		
		//save program map state
		ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String,Object>>();	
		Set<String> keys = mapPrograms.keySet();
		for(String key : keys) {
			properties.add(new AbstractMap.SimpleEntry<String,Object>(key, mapPrograms.get(key).getProgramJSON().toString()));
		}
		contextHistory_push.pushData_change(this.getClass().getSimpleName(), "interpreter", "start", "stop", properties);
	}

	@Override
	public boolean addProgram(JSONObject programJSON) {
		NodeProgram p;
		
		// initialize a program node from the JSON
		try {
			p = new NodeProgram(this, programJSON);
		} catch (JSONException e) {
			logger.error("JSON error detected while loading a program");
			e.printStackTrace();
			return false;
		}
		
		mapPrograms.put(p.getId(), p);
		
		//save program map state
		ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String,Object>>();	
		Set<String> keys = mapPrograms.keySet();
		for(String key : keys) {
			properties.add(new AbstractMap.SimpleEntry<String,Object>(key, mapPrograms.get(key).getProgramJSON().toString()));
		}
		
		if(contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getName(), properties)) {
			p.setDeployed();
			try {
				notifyAddProgram(p.getId(), p.getRunningState().toString(), p.getProgramJSON().getJSONObject("source"), p.getUserInputSource());
			} catch (JSONException e) {e.printStackTrace();}
			return true;
		}else {
			mapPrograms.remove(p.getId());
			return false;
		}
	}
	
	@Override
	public boolean removeProgram(String programId) {
		NodeProgram p = mapPrograms.get(programId);
		
		if(p != null) {
			p.stop();
			p.removeEndEventListener(this);
			
			mapPrograms.remove(programId);
			//save program map state
			ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String,Object>>();	
			Set<String> keys = mapPrograms.keySet();
			for(String key : keys) {
				properties.add(new AbstractMap.SimpleEntry<String,Object>(key, mapPrograms.get(key).getProgramJSON().toString()));
			}
			if(contextHistory_push.pushData_remove(this.getClass().getSimpleName(), p.getId(), p.getName(), properties)) {
				notifyRemoveProgram(p.getId());
				return true;
			}
			
		}else {
			logger.error("The programme "+programId+" does not exist.");
		}
		
		return false;
	}
	
	@Override
	public boolean update(JSONObject jsonProgram) {
		try {
			NodeProgram p = mapPrograms.get(jsonProgram.getString("id"));
			
			if(p != null) {
				
				if(p.getRunningState() == NodeProgram.RUNNING_STATE.STARTED ||
				   p.getRunningState() == NodeProgram.RUNNING_STATE.PAUSED) {
					p.stop();
					p.removeEndEventListener(this);
				}
				
				if(p.update(jsonProgram)) {
					notifyUpdateProgram(p.getId(), p.getRunningState().toString(), p.getProgramJSON().getJSONObject("source"), p.getUserInputSource());
					//save program map state
					ArrayList<Entry<String, Object>> properties = new ArrayList<Entry<String,Object>>();	
					Set<String> keys = mapPrograms.keySet();
					for(String key : keys) {
						properties.add(new AbstractMap.SimpleEntry<String,Object>(key, mapPrograms.get(key).getProgramJSON().toString()));
					}
					
					if(contextHistory_push.pushData_add(this.getClass().getSimpleName(), p.getId(), p.getName(), properties)) {
						return true;
					}
				}
				
			}else {
				logger.error("The programme "+jsonProgram.getString("id") +" does not exist.");
			}
			
		} catch (JSONException e) {
			logger.error("EUDE error - updating programm - NO ID in JSON DESCRIPTION");
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean callProgram(String programId) {
		NodeProgram p = mapPrograms.get(programId);
		int calledStatus = -1;
		
		if (p != null) {
			p.addEndEventListener(this);
			calledStatus = p.call();
		}
		
		return (calledStatus==1);
	}
	
	@Override
	public boolean stopProgram(String programId) {
		NodeProgram p = mapPrograms.get(programId);
		
		if(p!= null){
			p.stop();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean pauseProgram(String programId) {
		NodeProgram p = mapPrograms.get(programId);
		
		if(p!= null){
			return p.pause();
		}
		
		return false;
	}
	
	@Override
	public HashMap<String, JSONObject> getListPrograms() {
		HashMap <String, JSONObject> mapProgramJSON = new HashMap<String, JSONObject>();
		for (NodeProgram p : mapPrograms.values()) {
			mapProgramJSON.put(p.getId(), p.getProgramJSON());
		}
		
		return mapProgramJSON;
	}
	
	@Override
	public boolean isProgramActive(String programId) {
		NodeProgram p = mapPrograms.get(programId);
		
		if(p!= null){
			return (p.getRunningState() == RUNNING_STATE.STARTED);
		}
		
		return false;
	}
	
	/**
	 * Getter for a node program. Used for the event
	 * 
	 * @param programId identifier of program to get
	 * @return Node of the program if found, null otherwise
	 */
	public NodeProgram getNodeProgram(String programId) {
		return mapPrograms.get(programId);
	}
	
	/**
	 * Execute a method call on the router
	 * 
	 * @param objectId
	 * @param methodName
	 * @param args
	 * @param paramType
	 * @return
	 */
	public GenericCommand executeCommand(String objectId, String methodName, JSONArray args) {
		return router.executeCommand(objectId, methodName, args);
	}
	
	/**
	 * Add a node to notify when the specified event has been caught. The node is notified only when the event is received
	 * 
	 * @param nodeEvent Node to notify when the event is received
	 */
	public synchronized void addNodeListening(NodeEvent nodeEvent) {
		// instantiate a core listener
		CoreEventListener listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue(), this);
		
		Set<CoreEventListener> keyset = mapCoreNodeEvent.keySet();
		Iterator<CoreEventListener> it = keyset.iterator();
		boolean contains = false;
		CoreEventListener cel = null;
		
		while(it.hasNext() && !contains) {
			cel = it.next();
			if(cel.equals(listener)) {
				contains = true;
			}
		}
		
		// if the event is already listened by other nodes
		if ( contains ) {
			mapCoreNodeEvent.get(cel).add(nodeEvent);
			logger.debug("Add node event to listener list.");
		// if the event is not listened yet
		} else {
			// create the list of nodes to notify
			ArrayList<NodeEvent> nodeList = new ArrayList<NodeEvent>();
			nodeList.add(nodeEvent);
			
			// add the listener to the context
			contextFollower.addListener(listener);
			
			// fill the map with the new entry
			mapCoreNodeEvent.put(listener, nodeList);
			logger.debug("Add node event listener list.");
		}	
	}
	
	/**
	 * Remove a node to notify
	 * 
	 * @param nodeEvent Node to remove
	 */
	public synchronized void removeNodeListening(NodeEvent nodeEvent) {
		CoreEventListener listener = new CoreEventListener(nodeEvent.getSourceId(), nodeEvent.getEventName(), nodeEvent.getEventValue(), this);
		
		Set<CoreEventListener> keyset = mapCoreNodeEvent.keySet();
		Iterator<CoreEventListener> it = keyset.iterator();
		boolean contains = false;
		CoreEventListener cel = null;
		
		while(it.hasNext() && !contains) {
			cel = it.next();
			if(cel.equals(listener)) {
				contains = true;
			}
		}
		
		if (contains) {
			ArrayList<NodeEvent> nodeEventList = mapCoreNodeEvent.get(cel);
			nodeEventList.remove(nodeEvent);
			logger.debug("Remove nodeEvent from listener list.");
			// remove the listener if there is no node any more to notify
			if (nodeEventList.isEmpty()) {
				contextFollower.deleteListener(cel);
				mapCoreNodeEvent.remove(cel);
				logger.debug("Remove node event listener list.");
			}
		}
	}
	
	private void notifyUpdateProgram(String id, String runningState, JSONObject source, String userInputSource) {
		notifyChanges(new ProgramNotification("updateProgram", id, runningState, source, userInputSource));
	}
	
	private void notifyAddProgram(String id, String runningState, JSONObject source, String userInputSource) {
		notifyChanges(new ProgramNotification("newProgram", id, runningState, source, userInputSource));
	}
	private void notifyRemoveProgram(String id) {
		notifyChanges(new ProgramNotification("removeProgram", id, RUNNING_STATE.STOPPED.toString(), new JSONObject(), ""));
	}
	
	/**
	 * This method uses the ApAM message model. Each call produce a
	 * ProgramStateNotificationMsg object and notifies ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyChanges(NotificationMsg notif) {
		return notif;
	}
	
	public class CoreEventListener implements CoreListener {
		
		private String objectId;
		private String varName;
		private String varValue;
		private EUDEInterpreterImpl eudeInt;
		
	
		public CoreEventListener(String objectId, String varName,
				String varValue, EUDEInterpreterImpl eudeInt) {
			this.objectId = objectId;
			this.varName = varName;
			this.varValue = varValue;
			this.eudeInt = eudeInt;
		}

		@Override
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		@Override
		public void setEvent(String eventVarName) {
			this.varName = eventVarName;
		}

		@Override
		public void setValue(String eventVarValue) {
			this.varValue = eventVarValue;
		}

		@Override
		public String getObjectId() {
			return objectId;
		}

		@Override
		public String getEvent() {
			return varName;
		}

		@Override
		public String getValue() {
			return varValue;
		}

		@Override
		public void notifyEvent() {
			// transmit the core event to the concerned nodes
			synchronized(eudeInt) {
				ArrayList<NodeEvent> nodeEventList =  mapCoreNodeEvent.get(this);
				for (NodeEvent n : nodeEventList) {
					n.coreEventFired();
				}
				contextFollower.deleteListener(this);
				mapCoreNodeEvent.remove(this);
			}
		}

		@Override
		public void notifyEvent(CoreListener listener) {
			logger.debug("The event is catch by the EUDE "+listener);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CoreEventListener)) {
				return false;
			}
			
			CoreEventListener c = (CoreEventListener)o;
			return (objectId.contentEquals(c.objectId) && varName.contentEquals(c.varName) && varValue.contentEquals(c.varValue));
		}
	}

	@Override
	public void endEventFired(EndEvent e) {
		NodeProgram p = (NodeProgram)e.getSource();
		logger.info("program "+p.getName()+" ended.");
//		if(p.getDeamon().contentEquals("true")) {
//			logger.debug("Deamon execution"+ p.getName() +" removing...");
//			if(removeProgram(p.getName())) {
//				logger.debug("Deamon "+ p.getName() +" reloading...");
//				if(addProgram(p.getProgramJSON())) {
//					logger.debug("Deamon "+ p.getName() +" restarting...");
//					if(callProgram(p.getName())) {
//						logger.debug("Deamon "+ p.getName() +" started.");
//					}else {
//						logger.debug("Deamon "+ p.getName() +" execution failed.");
//					}
//				}
//				
//			} else {
//				logger.error("Fail to remove "+ p.getName() +" ! The deamon has not been reloaded.");
//			}
//		}else {
			p.removeEndEventListener(this);
//		}
	}

	@Override
	public void startEventFired(StartEvent e) {
		// TODO Auto-generated method stub
		
	}

}
