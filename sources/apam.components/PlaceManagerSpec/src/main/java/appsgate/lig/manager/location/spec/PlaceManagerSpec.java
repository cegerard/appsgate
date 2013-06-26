package appsgate.lig.manager.location.spec;

import org.json.JSONArray;

public interface PlaceManagerSpec {
	
	public void addPlace(String locationId, String name);
	public void removePlace(String locationId);
	public void moveObject(String objId, String oldPlaceID, String newPlaceID);
	public void renameLocation(String locationId, String newName);
	public JSONArray getJSONLocations();
	public String getCoreObjectLocationId(String objId);
}
