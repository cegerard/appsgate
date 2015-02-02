package appsgate.lig.tts.yakitome.utils;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;
import appsgate.lig.tts.yakitome.AdapterListener;
import appsgate.lig.tts.yakitome.DAOSpeechTextItems;
import appsgate.lig.tts.yakitome.SpeechTextItem;

/**
 * Helper class to store/get entries from the DB
 * We use the book_id of each Speech item as key _id in mongoDB
 * @param item
 */
public class DAOSpeechTextItemsMongo implements DAOSpeechTextItems {
	
	private static Logger logger = LoggerFactory
			.getLogger(DAOSpeechTextItemsMongo.class);	
	
	String dbName = CoreObjectSpec.DBNAME_DEFAULT;

	String dbCollectionName = this.getClass().getSimpleName();
	
	/**
	 * @param dbHelper
	 */
	public DAOSpeechTextItemsMongo(MongoDBConfiguration dbConfig,
			AdapterListener adapterListener) {
		this.dbConfig = dbConfig;
		this.adapterListener = adapterListener;
		dbHelper = dbConfig.getDBHelper(dbName, dbCollectionName);
	}

	MongoDBConfiguration dbConfig;
	DBHelper dbHelper;
	AdapterListener adapterListener;
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.utils.DAOSpeechTextItems#testService()
	 */
	@Override
	public boolean testService() {
		logger.trace("testService()");
		
		if(dbConfig == null
				||!dbConfig.isValid()) {
			logger.warn("testService(), dbConfig unavailable");
			adapterListener.serviceUnavailable();
			return false;
		} else {
			logger.trace("testService(), dbConfig OK");
			dbHelper = dbConfig.getDBHelper(dbName, dbCollectionName);
			return true;			
		}
	}	
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.utils.DAOSpeechTextItems#getSpeechItemsFromDB()
	 */
	@Override
	public Set<SpeechTextItem> getSpeechItemsFromDB() {
		logger.trace("getSpeechItemsFromDB()");
		
		if(dbHelper == null
				|| dbHelper.getJSONEntries() == null) {
			logger.warn("getSpeechItemsFromDB(),"
					+ "no DB Helper or no SpeechItems, maybe db offline");
			if(adapterListener != null) {
				adapterListener.serviceUnavailable();
			}			return null;
		} else {		
			Set<JSONObject> entries = dbHelper.getJSONEntries();
			Set<SpeechTextItem> results = new HashSet<SpeechTextItem>();
			SpeechTextItem item;
			
			for(JSONObject entry : entries ) {
				logger.trace("getSpeechItemsFromDB(), entry : "+entry.toString());
				try {
					item = new SpeechTextItem(entry);
					results.add(item);
					logger.trace("getSpeechItemsFromDB(), entry added");
	
				} catch (NullPointerException e) {
					logger.warn("getSpeechItemsFromDB(),"
							+ " error when parsing entry (skipped) "+e.getMessage());
				}			
			}
			logger.trace("getSpeechItemsFromDB(), returning : "+results);
			return results;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.utils.DAOSpeechTextItems#storeSpeechItemsToDB(java.util.Set)
	 */
	@Override
	public void storeSpeechItemsToDB(Set<SpeechTextItem> items) {
		logger.trace("storeSpeechItemsToDB(Set<SpeechTextItem> items : {})",items.toArray());
		
		if(dbHelper == null) {
			logger.warn("storeSpeechItemsToDB(...),"
					+ "no DB Helper, maybe db offline");
			if(adapterListener != null) {
				adapterListener.serviceUnavailable();
			}		} else if (items == null ||items.size() <1){
			logger.warn("storeSpeechItemsToDB(...),"
					+ "no items to add");			
		} else {		
			JSONObject entry;
			
			for(SpeechTextItem item : items ) {
				try {
					logger.trace("storeSpeechItemsToDB(), entry : "+item.toJSON());
					entry = item.toJSON();
					entry.put(DBHelper.ENTRY_ID, String.valueOf(item.getBookId()));
					dbHelper.insertJSON(entry);
					
					logger.trace("storeSpeechItemsToDB(), entry added : "+entry);
	
				} catch (NullPointerException e) {
					logger.warn("storeSpeechItemsToDB(),"
							+ " error when parsing entry (skipped) "+e.getMessage());
				}			
			}
			logger.trace("storeSpeechItemsToDB(), items stored");
		}
	}
	

	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.utils.DAOSpeechTextItems#addUpdateSpeechItem(appsgate.lig.tts.yakitome.SpeechTextItem)
	 */
	@Override
	public void addUpdateSpeechItem(SpeechTextItem item) {
		if(dbHelper == null) {
			logger.warn("addUpdateSpeechItem(...),"
					+ "no DB Helper, maybe db offline");
			if(adapterListener != null) {
				adapterListener.serviceUnavailable();
			}
		} else if (item == null) {
			logger.warn("addUpdateSpeechItem(...),"
					+ "no item to add");			
		} else {		
			logger.trace("addUpdateSpeechItem(SpeechTextItem item : {})",item.toJSON());
			JSONObject entry = item.toJSON();
			logger.trace("addUpdateSpeechItem(), entry : "+entry);
			
			entry.put(DBHelper.ENTRY_ID, String.valueOf(item.getBookId()));
			dbHelper.insertJSON(entry);
					
			logger.trace("addUpdateSpeechItem(), entry added : "+entry);
		}				
	}
	
	/* (non-Javadoc)
	 * @see appsgate.lig.tts.yakitome.utils.DAOSpeechTextItems#removeSpeechItem(int)
	 */
	@Override
	public void removeSpeechItem(int id) {
		if(dbHelper == null) {
			logger.warn("removeSpeechItem(...),"
					+ "no DB Helper, maybe db offline");
			if(adapterListener != null) {
				adapterListener.serviceUnavailable();
			}
		} else if (id < 1) {
			logger.warn("removeSpeechItem(...),"
					+ "no item to remove (wrong id)");			
		} else {		
			logger.trace("removeSpeechItem(SpeechTextItem item id : {})",id);
			
			dbHelper.remove(String.valueOf(id));
					
			logger.trace("removeSpeechItem(), entry removed");
		}				
	}
}
