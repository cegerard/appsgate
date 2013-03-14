package appsgate.lig.manager.location.spec;

import org.json.JSONArray;

import appsgate.lig.logical.object.spec.AbstractObjectSpec;

public interface PlaceManagerSpec {
	
	public void addPlace(String locationId, String name);
	public void removePlace(String locationId);
	public void moveObject(AbstractObjectSpec obj, String oldPlaceID, String newPlaceID);
	public void renameLocation(String locationId, String newName);
	public JSONArray getJSONLocations();
}
