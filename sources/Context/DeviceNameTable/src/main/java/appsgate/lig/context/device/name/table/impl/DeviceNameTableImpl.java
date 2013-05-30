package appsgate.lig.context.device.name.table.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.context.device.name.table.spec.DeviceNameTableSpec;

public class DeviceNameTableImpl implements DeviceNameTableSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory
			.getLogger(DeviceNameTableImpl.class);

	HashMap<Entry, String> userObjectName = new HashMap<Entry, String>();

	@Override
	public void addName(String objectId, String usrId, String newName) {
		userObjectName.put(new Entry(objectId, usrId), newName);
	}

	@Override
	public void deleteName(String objectId, String usrId) {
		Entry eventKey = new Entry(objectId, usrId);

		Set<Entry> keys = userObjectName.keySet();
		Iterator<Entry> keysIt = keys.iterator();

		while (keysIt.hasNext()) {
			Entry key = keysIt.next();
			if (eventKey.equals(key)) {
				userObjectName.remove(key);
				break;
			}
		}
	}
	
	@Override
	public String getUserObjectName(String objectId, String usrId) {
		Entry eventKey = new Entry(objectId, usrId);
		
		Set<Entry> keys = userObjectName.keySet();
		Iterator<Entry> keysIt = keys.iterator();
		String name="";
		while (keysIt.hasNext()) {
			Entry key = keysIt.next();
			if (eventKey.equals(key)) {
				name = userObjectName.get(key);
				break;
			}
		}
		
		return name;
	}

	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.debug("The device name table has been initialized");
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.debug("The device name table has been stopped");
	}

	/**
	 * Inner class use to create a key for device di and user association
	 * 
	 * @author Cédric Gérard
	 * @since May 28, 2013
	 * @version 1.0.0
	 */
	private class Entry {

		/**
		 * Identifier of the source f the event
		 */
		private String objectId;

		/**
		 * The variable name to follow
		 */
		private String userName;

		/**
		 * Constructor for an Entry
		 * 
		 * @param objectId
		 * @param userName
		 */
		public Entry(String objectId, String userName) {
			this.objectId = objectId;
			this.userName = userName;
		}

		public String getObjectId() {
			return objectId;
		}

		public String getUserName() {
			return userName;
		}

		/**
		 * Override the equals method to compare two entry together but compare
		 * their contents
		 */
		@Override
		public boolean equals(Object keyEntry) {
			if (keyEntry instanceof Entry) {
				Entry entry = (Entry) keyEntry;
				return (entry.getObjectId().contentEquals(objectId) && entry
						.getUserName().contentEquals(userName));
			}
			return false;
		}

	}

}
