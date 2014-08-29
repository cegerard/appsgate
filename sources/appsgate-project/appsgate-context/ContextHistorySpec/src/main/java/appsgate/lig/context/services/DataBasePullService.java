package appsgate.lig.context.services;

import org.json.JSONObject;

/**
 * The pull service allow a component to pull state from
 * data base.
 * 
 * @author Cédric Gérard
 * @since June 16, 2013
 * @version 1.0.0
 *
 */
public interface DataBasePullService {
	
	/**
	 * Pull the last state of the object corresponding to the name give in parameter.
	 * @param ObjectName the name of the object to get
	 * @return A JSON object that contain the last state of the specify object
	 */
	public JSONObject pullLastObjectVersion(String ObjectName);

    public boolean testDB();


}
