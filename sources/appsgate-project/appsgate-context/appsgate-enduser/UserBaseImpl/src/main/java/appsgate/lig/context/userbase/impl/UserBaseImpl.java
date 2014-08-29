package appsgate.lig.context.userbase.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.context.user.impl.AppsgateEndUser;
import appsgate.lig.context.userbase.spec.UserBaseSpec;

/**
 * The users base implementation is an ApAM component to save Appsgate users.
 * 
 * @author Cedric Gerard
 * @since July 23, 2013
 * @version 1.0.0
 * 
 * @see UserBaseSpec
 *
 */
public class UserBaseImpl implements UserBaseSpec {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(UserBaseImpl.class);
	
	/**
	 * Context history pull service to get past table state
	 */
	private DataBasePullService contextHistory_pull;
	
	/**
	 * Context history push service to save the current state
	 */
	private DataBasePushService contextHistory_push;
	
	/**
	 * user list hahMap
	 */
	private HashMap<String, AppsgateEndUser> userList = null;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("User base starting");
		
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The users base has been stopped");
	}

	@Override
	public boolean adduser(String id, String password, String lastName, String firstName, String role) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		
		try{
			AppsgateEndUser newUser = new AppsgateEndUser(id, password, lastName, firstName, role);
			userList.put(id, newUser);
		
			// save the new user base
			ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
			Set<String> keys = userList.keySet();
			for(String key : keys) {
				properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
			}
			contextHistory_push.pushData_add(this.getClass().getSimpleName(), id, lastName+"-"+firstName, properties);
		
			return true;
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean removeUser(String id, String password) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		try{
			AppsgateEndUser aeu = userList.get(id);
			if(aeu.authenticate(password)) {
				//TODO delete all related service instances.
				userList.remove(id);

				// save the new user base
				ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
				Set<String> keys = userList.keySet();
				for(String key : keys) {
					properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
				}
				contextHistory_push.pushData_remove(this.getClass().getSimpleName(), id, aeu.getLastName()+"-"+aeu.getFirstName(), properties);
		
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public JSONArray getUsers() {
        if(!restoreUsersFromDb()) {
            return null;
        }
		JSONArray array = new JSONArray();
		for(AppsgateEndUser aeu : userList.values()) {
			array.put(aeu.getDescription());
		}
		return array;
	}

	@Override
	public JSONObject getUserDetails(String id) {
        if(!restoreUsersFromDb()) {
            return null;
        }
		AppsgateEndUser aeu = userList.get(id);
		return aeu.getDescription();
	}

	@Override
	public boolean checkIfIdIsFree(String id) {
        if(!restoreUsersFromDb()) {
            return false;
        }
        return (userList.get(id) == null);
	}

	@Override
	public boolean addAccount(String id, String password,
			JSONObject accountDetails) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		
		AppsgateEndUser aeu = userList.get(id);
		
		if(aeu.authenticate(password)) {
			if( aeu.addAccount(accountDetails)) {
				// save the new user base
				ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
				Set<String> keys = userList.keySet();
				for(String key : keys) {
					properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
				}
				try {
					contextHistory_push.pushData_add(this.getClass().getSimpleName(),id, accountDetails.getString("login"), accountDetails.getString("service"), properties);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean removeAccount(String id, String password,
			JSONObject accountDetails) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		
		AppsgateEndUser aeu = userList.get(id);
		
		if(aeu.authenticate(password)) {
			if (aeu.removeAccount(accountDetails)) {
				// save the new user base
				ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
				Set<String> keys = userList.keySet();
				for(String key : keys) {
					properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
				}
				try {
					contextHistory_push.pushData_remove(this.getClass().getSimpleName(),id, accountDetails.getString("login"), accountDetails.getString("service"), properties);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public JSONArray getAccountsDetails(String id) {
        if(!restoreUsersFromDb()) {
            return null;
        }
		try {
			return userList.get(id).getAccountsDetailsJSONArray();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}

	@Override
	public boolean addDevice(String id, String password, String deviceId) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		AppsgateEndUser aeu = userList.get(id);
		
		if(aeu.authenticate(password)) {
			if (aeu.addDevice(deviceId)) {
				// save the new user base
				ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
				Set<String> keys = userList.keySet();
				for(String key : keys) {
					properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
				}

				contextHistory_push.pushData_add(this.getClass().getSimpleName(),id, deviceId, "device", properties);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean removeDevice(String id, String password, String deviceId) {
        if(!restoreUsersFromDb()) {
            return false;
        }
		AppsgateEndUser aeu = userList.get(id);
		
		if(aeu.authenticate(password)) {
			if (aeu.removeDevice(deviceId)) {
				// save the new user base
				ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();		
				Set<String> keys = userList.keySet();
				for(String key : keys) {
					properties.add(new AbstractMap.SimpleEntry<String,Object>(key, userList.get(key).JSONize().toString()));
				}

				contextHistory_push.pushData_remove(this.getClass().getSimpleName(),id, deviceId, "device", properties);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public JSONArray getAssociatedDevices(String id) {
        if(!restoreUsersFromDb()) {
            return null;
        }
		try {
			return userList.get(id).getDeviceListJSONArray();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}

	@Override
	public JSONObject getHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHierarchy(JSONObject hierarchy) {
		// TODO Auto-generated method stub
		
	}

    private synchronized boolean restoreUsersFromDb() {
        //restore places from data base
        if(userList != null) {
            return true;
        } else if(contextHistory_pull!= null && contextHistory_pull.testDB()) {
            JSONObject userbase = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
            logger.debug("Restore user base from database");
            if(userbase != null){
                try {
                    JSONArray state = userbase.getJSONArray("state");
                    int length = state.length();
                    int i = 0;
                    while(i < length) {
                        JSONObject obj = state.getJSONObject(i);
                        String key = (String)obj.keys().next();
                        userList.put(key, new AppsgateEndUser(new JSONObject(obj.getString(key))));
                        i++;
                    }
                    logger.debug("The users base has been initialized");
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

}
