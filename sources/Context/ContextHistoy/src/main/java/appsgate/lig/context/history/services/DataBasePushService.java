package appsgate.lig.context.history.services;

import java.util.ArrayList;
import java.util.Map;


public interface DataBasePushService {
	
	public boolean pushData_add(String name, String userID, String objectID, String addedValue, ArrayList<Map.Entry<String, Object>> properties);
	public boolean pushData_remove(String name, String userID, String objectID, String removedValue, ArrayList<Map.Entry<String, Object>> properties); 
	public boolean pushData_change(String name, String userID, String objectID, String oldValue, String newValue, ArrayList<Map.Entry<String, Object>> properties); 
	
	
	public enum OP {
			ADD,
			REMOVE,
			CHANGE; 
	}

}
