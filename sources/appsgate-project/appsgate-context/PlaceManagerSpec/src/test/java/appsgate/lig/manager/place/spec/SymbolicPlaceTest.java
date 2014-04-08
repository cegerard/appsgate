package appsgate.lig.manager.place.spec;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SymbolicPlaceTest {
	
	protected SymbolicPlace rootPlace, living, kitchen, restPlace;
	protected String[] ids = {"0001", "0002", "0003", "0004"};
	protected String[] names = {"livingroom", "kitchen", "bathroom", "restPlace", "Myhouse"};

	@Before
	public void setUp() throws Exception {
		rootPlace = new SymbolicPlace(ids[0], names[4], null);
		living = new SymbolicPlace(ids[1], names[0], rootPlace);
		kitchen = new SymbolicPlace(ids[2], names[1], rootPlace);
		restPlace = new SymbolicPlace(ids[3], names[3], living);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetId() {
		assertEquals(ids[0], rootPlace.getId());
	}

	@Test
	public void testGetName() {
		assertEquals(names[4], rootPlace.getName());
	}

	@Test
	public void testSetName() {
		rootPlace.setName("testName");
		assertEquals("testName", rootPlace.getName());
		rootPlace.setName(names[4]);
		testGetName();
	}

	@Test
	public void testGetParent() {
		assertSame(rootPlace, living.getParent());
	}

	@Test
	public void testSetParent() {
		living.setParent(kitchen);
		assertNotSame(rootPlace, living.getParent());
		assertSame(kitchen, living.getParent());
		living.setParent(rootPlace);
		testGetParent();
	}

	@Test
	public void testSetGetTags() {
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("tag1");
		tags.add("tag2");
		
		rootPlace.setTags(tags);
		assertSame(tags, rootPlace.getTags());
	}

	@Test
	public void testClearTags() {
		rootPlace.clearTags();
		assertArrayEquals(new ArrayList<String>().toArray(), rootPlace.getTags().toArray());
	}

	@Test
	public void testAddTag() {
		assertTrue(rootPlace.addTag("myTag"));
	}

	@Test
	public void testIsTagged() {
		rootPlace.addTag("myTag");
		assertTrue(rootPlace.isTagged("myTag"));
	}
	
	@Test
	public void testRemoveTag() {
		rootPlace.addTag("myTag");
		assertTrue(rootPlace.removeTag("myTag"));
	}

	@Test
	public void testSetGetProperties() {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		properties.put("prop3", "value3");
		
		rootPlace.setProperties(properties);
		assertSame(properties, rootPlace.getProperties());
	}

	@Test
	public void testClearProperties() {
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("prop1", "value1");
		properties.put("prop2", "value2");
		properties.put("prop3", "value3");
		rootPlace.setProperties(properties);
		assertNotEquals(0, rootPlace.getProperties().size());
		
		rootPlace.clearProperties();
		assertEquals(0, rootPlace.getProperties().size());
	}

	@Test
	public void testAddProperty() {
		assertTrue(rootPlace.addProperty("prop10", "value10"));
		assertFalse(rootPlace.addProperty("prop10", "value11"));
	}

	@Test
	public void testGetPropertyValue() {
		rootPlace.addProperty("prop10", "value10");
		assertEquals("value10", rootPlace.getPropertyValue("prop10"));
	}
	
	@Test
	public void testHasProperty() {
		rootPlace.addProperty("prop10", "value10");
		assertTrue(rootPlace.hasProperty("prop10"));
	}
	
	@Test
	public void testRemoveProperty() {
		rootPlace.addProperty("prop10", "value10");
		assertTrue(rootPlace.removeProperty("prop10"));
	}

	@Test
	public void testGetChildren() {
		SymbolicPlace[] places = {living, kitchen};
		assertArrayEquals(places, rootPlace.getChildren().toArray());
	}

	@Test
	public void testSetChildren() {
		ArrayList<SymbolicPlace> children = new ArrayList<SymbolicPlace>();
		children.add(living);
		children.add(kitchen);
		
		rootPlace.setChildren(children);
		assertSame(children, rootPlace.getChildren());
	}

	@Test
	public void testAddChild() {
		assertTrue(rootPlace.addChild(living));
		assertTrue(rootPlace.addChild(kitchen));
	}

	@Test
	public void testRemoveChild() {
		assertTrue(rootPlace.removeChild(living));
	}

	@Test
	public void testHasChild() {
		assertTrue(rootPlace.hasChild(kitchen));
	}

	@Test
	public void testSetGetCoreObjects() {
		ArrayList<String> coreObject = new ArrayList<String>();
		coreObject.add("0903964");
		coreObject.add("0130638");
		
		rootPlace.setDevices(coreObject);
		assertSame(coreObject, rootPlace.getDevices());
	}
	
	@Test
	public void testSetGetCoreServices() {
		ArrayList<String> coreService = new ArrayList<String>();
		coreService.add("0903964");
		coreService.add("0130638");
		
		rootPlace.setServices(coreService);
		assertSame(coreService, rootPlace.getServices());
	}

	@Test
	public void testClearCoreObjects() {
		ArrayList<String> coreObject = new ArrayList<String>();
		coreObject.add("0903964");
		coreObject.add("0130638");
		rootPlace.setDevices(coreObject);
		assertNotEquals(0, rootPlace.getDevices().size());
		
		rootPlace.clearDevices();
		assertEquals(0, rootPlace.getDevices().size());
	}
	
	@Test
	public void testClearCoreServices() {
		ArrayList<String> coreService = new ArrayList<String>();
		coreService.add("0903964");
		coreService.add("0130638");
		rootPlace.setServices(coreService);
		assertNotEquals(0, rootPlace.getServices().size());
		
		rootPlace.clearServices();
		assertEquals(0, rootPlace.getServices().size());
	}

	@Test
	public void testAddcoreObject() {
		assertTrue(rootPlace.addDevice("0903964"));
		assertTrue(rootPlace.addDevice("0130638"));
	}
	
	@Test
	public void testAddcoreService() {
		assertTrue(rootPlace.addService("0903964"));
		assertTrue(rootPlace.addService("0130638"));
	}

	@Test
	public void testRemoveCoreObject() {
		rootPlace.addDevice("0130638");
		assertTrue(rootPlace.removeDevice("0130638"));
	}
	
	@Test
	public void testRemoveCoreService() {
		rootPlace.addService("0130638");
		assertTrue(rootPlace.removeService("0130638"));
	}

	@Test
	public void testHasCoreObject() {
		rootPlace.addDevice("0903964");
		assertTrue(rootPlace.hasDevice("0903964"));
	}
	
	@Test
	public void testHasCoreService() {
		rootPlace.addService("0903964");
		assertTrue(rootPlace.hasService("0903964"));
	}

	@Test
	public void testGetDescription() {
		System.out.println("root:"+rootPlace.getDescription());
		System.out.println("living:"+living.getDescription());
		System.out.println("kitchen:"+kitchen.getDescription());
		assertTrue(kitchen.addChild(restPlace));
		System.out.println("restPlace:"+restPlace.getDescription());
	}

}
