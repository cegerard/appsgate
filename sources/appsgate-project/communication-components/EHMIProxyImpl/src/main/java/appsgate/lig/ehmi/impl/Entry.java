package appsgate.lig.ehmi.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.ehmi.impl.listeners.ObjectEventListener;
import appsgate.lig.ehmi.spec.listeners.CoreListener;

/**
 * Inner class use to create an event notify condition
 * 
 * @author Cédric Gérard
 * @since May 28, 2013
 * @version 1.0.0
 */
public class Entry {
	
    private final static Logger logger = LoggerFactory.getLogger(Entry.class);


	/**
	 * Identifier of the source f the event
	 */
	private String objectId;

	/**
	 * The variable name to follow
	 */
	private String varName;

	/**
	 * The threshold value
	 */
	private String value;

	/**
	 * The kind of entry "eventValue": for specific value of an event
	 * "eventName" : for a specific event but don't care about the value
	 */
	private String entryType;

	/**
	 * Constructor for an Entry
	 * 
	 * @param objectId
	 * @param varName
	 * @param value
	 */
	public Entry(String objectId, String varName, String value) {
		initWith(objectId, varName, value);
	}

	public Entry(JSONObject event) {
		initWith(event.optString("objectId"), event.optString("varName"),
				event.optString("value"));
	}

	public Entry(CoreListener core) {
		initWith(core.getObjectId(), core.getEvent(), core.getValue());
	}

//  Use only with former space manager use the updateCoreListener method instead.
//	public Entry(String vName, JSONArray a) throws JSONException {
//		String v = null;
//		String id = null;
//		for (int i = 0; i < a.length(); i++) {
//			JSONObject jsonObject = a.getJSONObject(i);
//			if (jsonObject.has("deviceType")) {
//				v = jsonObject.getString("deviceType");
//			}
//			if (jsonObject.has("ref")) {
//				id = jsonObject.getString("ref");
//			}
//		}
//		initWith(vName, v, id);
//	}

	/**
	 * 
	 * @param objectId
	 * @param varName
	 * @param value
	 */
	private void initWith(String objectId, String varName, String value) {
		this.objectId = objectId;
		this.varName = varName;
		this.value = value;
		if (value.contentEquals("")) {
			entryType = "eventName";
		} else {
			entryType = "eventValue";
		}
	}

	/**
	 * 
	 * @return the object Id
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * get the var name
	 * 
	 * @return
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value
	 * 
	 * @param value
	 *
	public void setValue(String value) {
		this.value = value;
	}
*/
	public String getEntryType() {
		return entryType;
	}

	/**
	 * Test if the Entry is only considering the event name and not the event
	 * value
	 * 
	 * @return true if the entry doesn't take in consideration the event value,
	 *         false otherwise
	 */
	public boolean isEventOnly() {
		return entryType.contentEquals("eventName");
	}

	/**
	 * Override the equals method to compare two entry together but compare
	 * their contents
	 */
	@Override
	public boolean equals(Object eventEntry) {
		if (eventEntry instanceof Entry) {
			Entry entry = (Entry) eventEntry;
			return (entry.getObjectId().contentEquals(objectId)
                                && entry.getVarName().contentEquals(varName)
                                && (entry.getValue().contentEquals(value)
                                    || entry.getValue().contentEquals("")));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash
				+ (this.objectId != null ? this.objectId.hashCode() : 0);
		hash = 23 * hash + (this.varName != null ? this.varName.hashCode() : 0);
		hash = 23 * hash + (this.value != null ? this.value.hashCode() : 0);
		return hash;
	}
	
	static final String WILDCARD="*";

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean match(Entry key) {
		// if value does not match
		if((key.value == null && this.value == null)
				||WILDCARD.equals(key.value)
				||WILDCARD.equals(this.value)
				||key.value.equals(this.value) ) {
			logger.trace("match(Entry key), values matches,"
					+ " this.value : {}, key.value : {}",this.value, key.value);
		} else {
			return false;
		}
		
		if((key.varName == null && this.varName == null)
				||WILDCARD.equals(key.varName)
				||WILDCARD.equals(this.varName)
				||key.varName.equals(this.varName) ) {
			logger.trace("match(Entry key), keys matches,"
					+ " this.varName : {}, key.varName : {}",this.varName, key.varName);
		} else {
			return false;
		}
		
		return key.objectId.equals(this.objectId);
	}
}
