package testingmongo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

import fr.imag.adele.apam.Component;
import fr.imag.adele.apam.PropertyManager;
import appsgate.lig.manager.propertyhistory.PropertyHistoryManagerMongoImpl;
import appsgate.lig.manager.propertyhistory.services.PropertyHistoryManager;
import appsgate.lig.persistence.MongoDBConfigFactory;
import appsgate.lig.persistence.MongoDBConfiguration;

/**
 * This test is only runnable if you have a local mongo db started locally with
 * standard configuration this mongo db must be populated with appsgate property
 * entries. This test class IS NOT designed for extensive unit tests, the
 * purpose is to play with the DB and provides some examples.
 * 
 * @author thibaud
 * 
 */
@Ignore
public class TestMongoLocally {
	
	
	private static Logger logger = LoggerFactory
			.getLogger(TestMongoLocally.class);

	PropertyHistoryManager dbQuery;
	PropertyManager dbPropertyChanges;
	PropertyHistoryManagerMongoImpl dbImpl;
	MongoDBConfigFactory myFactory;
	MongoDBConfiguration myConfig;

	long startingTestTime;
	long intermediateTestTime;

	private Map<String, Object> defaultProp;

	// Hqrdcoded value for the DB Configuration
	// These should work for a default mongo installed locally
	public static String DBHOST = "localhost";
	public static int DBPORT = 27017;
	public static String DBNAME = "AppsGateDBTesting";
	public static int DBTIMEOUT = 3000;

	static final String DEV_ID = "deviceId";
	static final String MEASURED_VAL = "measuredValue";

	@Before
	public void setUp() throws Exception {
		// Factory to default host
        MongoClient mongoClient = null;

        MongoClientOptions.Builder options = new MongoClientOptions.Builder();
        options.connectTimeout(DBTIMEOUT);
        mongoClient = new MongoClient(new ServerAddress(DBHOST, DBPORT),
                options.build());

		myConfig = new MongoDBConfiguration();
        myConfig.setConfiguration(DBHOST, DBPORT, DBTIMEOUT, mongoClient, null, null);

		dbImpl = new PropertyHistoryManagerMongoImpl();
		dbImpl.setMyConfiguration(myConfig);

		dbQuery = dbImpl;
		dbPropertyChanges = dbImpl;

		defaultProp = new HashMap<String, Object>();
		defaultProp.put(DEV_ID, new Object());
		defaultProp.put(MEASURED_VAL, new Object());

		startingTestTime = System.currentTimeMillis();
	}

	@After
	public void tearDown() throws Exception {
		// Clearing the created DB for tests
		if (myConfig != null && myConfig.getDB(DBNAME) != null) {
			myConfig.getDB(DBNAME).dropDatabase();
		}
	}

	private boolean assertDBEmpty(DBCollection collection) {
		if (collection == null) {
			System.err.println("DBCollection is null");
			return false;
		} else if (collection.count() > 0) {
			return false;
		}
		return true;
	}

	enum EntryStatus {
		ADD, REMOVE, CHANGE
	}

	/**
	 * This method can be used for inspiration but it is not generic, designed
	 * to have the minimal values sufficient for the method insertDBEntry(...)
	 */
	private void addPropEntry(String source, String deviceId,
			String propertyName, String propertyValue, EntryStatus status) {
		Component mockComponent = mock(Component.class);
		when(mockComponent.getAllProperties()).thenReturn(defaultProp);

		when(mockComponent.getProperty(DEV_ID)).thenReturn(deviceId);
		when(mockComponent.getProperty(propertyName)).thenReturn(propertyValue);
		when(mockComponent.getName()).thenReturn(source);
		switch (status) {
		case ADD:
			dbPropertyChanges.attributeAdded(mockComponent, propertyName, null);
			break;
		case REMOVE:
			dbPropertyChanges.attributeRemoved(mockComponent, propertyName,
					null);
			break;
		case CHANGE:
			dbPropertyChanges.attributeChanged(mockComponent, propertyName,
					null, null);
			break;
		}

	}

	@Test
	public void testAddingEntries() {
		if (myConfig != null && myConfig.getDB(DBNAME) != null
				&& dbPropertyChanges != null) {
			// Test 0 assert that the DB is empty
			DBCollection changedAttr = myConfig.getDB(DBNAME).getCollection(
					PropertyHistoryManagerMongoImpl.ChangedAttributes);
			assertTrue("DB should be empty before the test",
					assertDBEmpty(changedAttr));

			// Test 1 insert one single added attribute
			addPropEntry("InstanceTesting01", "Testing-01", MEASURED_VAL, "42",
					EntryStatus.ADD);

			DBObject dbEntry = changedAttr.findOne();

			testEntry(dbEntry, "InstanceTesting01", "Testing-01", MEASURED_VAL,
					"42", "added");

			// Test 2 change the value and retrieve it
			addPropEntry("InstanceTesting01", "Testing-01", MEASURED_VAL, "45",
					EntryStatus.CHANGE);
			DBObject query = new BasicDBObject();
			query.put(DEV_ID, "Testing-01");
			query.put("status", "changed");

			dbEntry = changedAttr.findOne(query);
			testEntry(dbEntry, "InstanceTesting01", "Testing-01", MEASURED_VAL,
					"45", "changed");

			// Test 3 removing the attribute
			addPropEntry("InstanceTesting01", "Testing-01", MEASURED_VAL, "45",
					EntryStatus.REMOVE);
			query.put("status", "removed");
			dbEntry = changedAttr.findOne(query);
			testEntry(dbEntry, "InstanceTesting01", "Testing-01", MEASURED_VAL,
					"45", "removed");

			// Test 4, as we deal with history, the first added entry should
			// still be there (with the old value)
			query.put("status", "added");
			dbEntry = changedAttr.findOne(query);
			testEntry(dbEntry, "InstanceTesting01", "Testing-01", MEASURED_VAL,
					"42", "added");

		} else {
			fail("Database Configuration not properly intitialized");
		}
	}

	public void testEntry(DBObject dbEntry, String source, String deviceId,
			String propertyName, String propertyValue, String status) {
		System.out.println("Entry retrieved, " + dbEntry);

		assertNotNull("One Entry should have been successfully added", dbEntry);
		assertTrue("Attribute source must be present (ApAM Instance name)",
				dbEntry.containsField("source"));
		assertTrue("Attribute deviceId must be present (appsgate property)",
				dbEntry.containsField("deviceId"));
		assertTrue("Attribute time must be present (appsgate property)",
				dbEntry.containsField("time"));
		assertTrue("Attribute property must be present (appsgate property)",
				dbEntry.containsField("property"));
		assertTrue("Attribute value must be present (appsgate property)",
				dbEntry.containsField("value"));
		assertTrue("Attribute status must be present (appsgate property)",
				dbEntry.containsField("status"));

		assertEquals("source should be " + source, source,
				dbEntry.get("source"));
		assertEquals("deviceId should be " + deviceId, deviceId,
				dbEntry.get("deviceId"));

		assertTrue(
				"time should be between starting time and current time",
				(((Long) dbEntry.get("time")).longValue() >= startingTestTime && ((Long) dbEntry
						.get("time")).longValue() <= System.currentTimeMillis()));

		assertEquals("property should be " + propertyName, propertyName,
				dbEntry.get("property"));
		assertEquals("value should be " + propertyValue, propertyValue,
				dbEntry.get("value"));
		assertEquals("status should be ", status, dbEntry.get("status"));

	}

	String sources[] = { "inst-dev-01", "inst-dev-02", "inst-dev-03" };
	String devicesIds[] = { "dev-01", "dev-02", "dev-03" };

	String propNames[] = {"propAll","propPair","propOdd", "propToTen"};

	private void populateDB() {

		for (int i = 0; i < 10; i++) {
			// device 01 log all properties
			// device 02 log onlyPair/Odd
			// device 03 logs onlyPropToTen (when i is odd)

			addPropEntry(sources[0], devicesIds[0], propNames[0], String.valueOf(i),
					EntryStatus.CHANGE);
			addPropEntry(sources[0], devicesIds[0], propNames[3],
					String.valueOf(i * 10), EntryStatus.CHANGE);
			if (i % 2 == 0) {
				addPropEntry(sources[0], devicesIds[0], propNames[1],
						String.valueOf(i), EntryStatus.CHANGE);
				addPropEntry(sources[1], devicesIds[1], propNames[1],
						String.valueOf(i), EntryStatus.CHANGE);
			} else {
				addPropEntry(sources[0], devicesIds[0], propNames[2],
						String.valueOf(i), EntryStatus.CHANGE);
				addPropEntry(sources[1], devicesIds[1], propNames[2],
						String.valueOf(i), EntryStatus.CHANGE);
				addPropEntry(sources[2], devicesIds[2], propNames[3],
						String.valueOf(i * 10), EntryStatus.CHANGE);
			}
			if (i==5) // We take a top in the middle to be sure to have previous values already stored in the DB
				intermediateTestTime=System.currentTimeMillis();
			//wait before each measure to make sure we have distinct times
			try {
				wait(10);
			} catch (Exception exc) {
			}

		}

	}

	@Test
	public void testQuery() {
		if (myConfig != null && myConfig.isValid() && dbQuery != null) {
			
			// Test 0 assert that the DB is empty
			DBCollection changedAttr = myConfig.getDB(DBNAME).getCollection(
					PropertyHistoryManagerMongoImpl.ChangedAttributes);
			assertTrue("DB should be empty before the test",
					assertDBEmpty(changedAttr));
			
			// Test 2
			for(String propName : propNames) {
				defaultProp.put(propName, new Object());		
			}
			populateDB();
			
			try {
			JSONObject resultsTestOne = dbQuery.getDevicesStatesHistoryAsJSON( null, propNames[0], intermediateTestTime,
					System.currentTimeMillis());
			
			logger.debug(resultsTestOne.toString());
			assertNotNull("There should be valid results",resultsTestOne);
			assertEquals("There should be only one Device Id matching", 1, resultsTestOne.length());
			assertEquals("Device Id should be dev-01", "dev-01", resultsTestOne.names().getString(0));
			assertNotNull("There should be a set of prop values changes",resultsTestOne.get("dev-01"));
			assertEquals("There should be 5 results", 5, resultsTestOne.getJSONArray("dev-01").length());
			
			JSONObject entry = resultsTestOne.getJSONArray("dev-01").getJSONObject(0);
			assertNotNull("An entry should not be null",entry);
			assertEquals("An entry should contain exactly two elements", 2, entry.length());
			assertNotNull("There must be time",  entry.get("time"));
			assertNotNull("There must be value", entry.get("value"));

			}catch (JSONException exc) {
				fail("There should not be JSON Exception : "+exc.getMessage());
			}
		} else {
			fail("Database Configuration not properly initialized");
		}
	}

}
