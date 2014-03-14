package appsgate.lig.manager.place.spec;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import appsgate.lig.manager.space.spec.Space;
import appsgate.lig.manager.space.spec.Space.TYPE;

public class SymbolicSpaceTest {
	
	protected Space rootPlace, living, kitchen, restPlace;
	protected String[] ids = {"0001", "0002", "0003", "0004"};
	protected String[] names = {"livingroom", "kitchen", "bathroom", "restPlace", "Myhouse"};

	@Before
	public void setUp() throws Exception {
		rootPlace = new Space(ids[0], TYPE.ROOT, null);
		rootPlace.addProperty("name", names[4]);
		living = new Space(ids[1], TYPE.PLACE, rootPlace);
		living.addProperty("name", names[0]);
		kitchen = new Space(ids[2], Space.TYPE.PLACE, rootPlace);
		kitchen.addProperty("name", names[1]);
		restPlace = new Space(ids[3], TYPE.PLACE, living);
		restPlace.addProperty("name", names[3]);
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
		assertFalse(rootPlace.removeProperty("category"));
	}

	@Test
	public void testGetChildren() {
		Space[] places = {living, kitchen};
		assertArrayEquals(places, rootPlace.getChildren().toArray());
	}

	@Test
	public void testSetChildren() {
		ArrayList<Space> children = new ArrayList<Space>();
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
	public void testGetSubSpace(){
		Space space = rootPlace.getSubSpace("0002");
		assertSame(living, space);
	}
	
	@Test
	public void testGetSubSpaces(){
		ArrayList<Space> spaces = rootPlace.getSubSpaces();
		assertTrue(spaces.contains(living));
		assertTrue(spaces.contains(kitchen));
		assertTrue(spaces.contains(restPlace));
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
