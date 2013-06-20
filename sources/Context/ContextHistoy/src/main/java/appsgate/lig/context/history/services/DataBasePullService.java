package appsgate.lig.context.history.services;

import org.json.JSONObject;

public interface DataBasePullService {
	
	public JSONObject pullLastObjectVersion(String ObjectName);

}
