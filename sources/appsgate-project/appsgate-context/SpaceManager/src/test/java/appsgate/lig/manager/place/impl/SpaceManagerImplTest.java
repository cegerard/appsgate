package appsgate.lig.manager.place.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.manager.space.impl.SpaceManagerImpl;
import appsgate.lig.manager.space.spec.Space;

public class SpaceManagerImplTest {

	protected Synchroniser synchroniser = new Synchroniser();
	
	Mockery context = new Mockery(){
        {
            setThreadingPolicy(synchroniser);
        }
    };
    
    private DataBasePullService pull_service;
    private DataBasePushService push_service;
    
    private SpaceManagerImpl spaceManager; 
    private String rootId;
	
	@Before
	public void setUp() throws Exception {
		 this.pull_service = context.mock(DataBasePullService.class);
		 this.push_service = context.mock(DataBasePushService.class);

		 context.checking(new Expectations() {
			 {
				 allowing(pull_service).pullLastObjectVersion(with(any(String.class)));
	             will(returnValue(null));
				 allowing(push_service).pushData_change(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
				 will(returnValue(true));
				 allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
	             will(returnValue(true));
				 allowing(push_service).pushData_add(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
	             will(returnValue(true));
	             allowing(push_service).pushData_remove(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
	             will(returnValue(true));
	             allowing(push_service).pushData_remove(with(any(String.class)), with(any(String.class)), with(any(String.class)), (ArrayList<Map.Entry<String, Object>>) with(any(Object.class)));
	             will(returnValue(true));
			 }
	     });
		 spaceManager = new SpaceManagerImpl();
		 spaceManager.initiateMock(pull_service, push_service);
		 spaceManager.newInst();
		 rootId = spaceManager.getRootSpace().getId();
		 assertNotNull(rootId);
	}

	@After
	public void tearDown() throws Exception {
		spaceManager.deleteInst();
	}

	@Test
	public void testAddSpace() {
		assertNotNull(spaceManager.addSpace("NewRoom", Space.CATEGORY.PLACE.toString(), rootId));
	}

	@Test
	public void testRemoveSpace() {
		String placeId = spaceManager.addSpace("NewRoom1", Space.CATEGORY.PLACE.toString(), rootId);
		assertTrue(spaceManager.removeSpace(placeId));
	}

	@Test
	public void testRenameSpace() {
		String placeId = spaceManager.addSpace("NewRoom3", Space.CATEGORY.PLACE.toString(), rootId);
		assertTrue(spaceManager.renameSpace(placeId, "NewPlaceName"));
	}

	@Test
	public void testGetSpace() {
		String placeId = spaceManager.addSpace("NewRoom4", Space.CATEGORY.PLACE.toString(), rootId);
		assertEquals("NewRoom4", spaceManager.getSpace(placeId).getName());
	}

	@Test
	public void getJSONSpaces() {
		assertNotNull(spaceManager.getJSONSpaces());
		System.out.println("testGetJSONPlaces ---- return :"+spaceManager.getJSONSpaces().toString());
	}

	@Test
	public void testMoveSpace() {
		String placeId  = spaceManager.addSpace("livingRoom", Space.CATEGORY.PLACE.toString(), rootId);
		String placeId2 = spaceManager.addSpace("readingPlace", Space.CATEGORY.PLACE.toString(), rootId);
		
		assertNotSame(spaceManager.getSpace(placeId), spaceManager.getSpace(placeId2).getParent());
		assertSame(spaceManager.getRootSpace(), spaceManager.getSpace(placeId2).getParent());
		assertTrue(spaceManager.getRootSpace().hasChild(spaceManager.getSpace(placeId2)));
		assertFalse(spaceManager.getSpace(placeId).hasChild(spaceManager.getSpace(placeId2)));
		
		spaceManager.moveSpace(placeId2, placeId);
		
		assertNotSame(spaceManager.getRootSpace(), spaceManager.getSpace(placeId2).getParent());
		assertSame(spaceManager.getSpace(placeId), spaceManager.getSpace(placeId2).getParent());
		assertTrue(spaceManager.getSpace(placeId).hasChild(spaceManager.getSpace(placeId2)));
		assertFalse(spaceManager.getRootSpace().hasChild(spaceManager.getSpace(placeId2)));
	}

	@Test
	public void testSetTagsList() {
		String placeId  = spaceManager.addSpace("livingRoom1", Space.CATEGORY.PLACE.toString(), rootId);
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("blue");
		tags.add("bedroom");
		spaceManager.setTagsList(placeId, tags);
		
		assertSame(tags, spaceManager.getSpace(placeId).getTags());
	}

	@Test
	public void testClearTagsList() {
		String placeId  = spaceManager.addSpace("livingRoom2",  Space.CATEGORY.PLACE.toString(), rootId);
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("blue");
		tags.add("bedroom");
		spaceManager.setTagsList(placeId, tags);
		assertEquals(2, spaceManager.getSpace(placeId).getTags().size());
		
		spaceManager.clearTagsList(placeId);
		assertEquals(0, spaceManager.getSpace(placeId).getTags().size());
	}

	@Test
	public void testAddTag() {
		assertTrue(spaceManager.addTag(rootId, "plop"));
	}

	@Test
	public void testRemoveTag() {
		assertTrue(spaceManager.addTag(rootId, "plop"));
		assertTrue(spaceManager.getSpace(rootId).getTags().contains("plop"));
		spaceManager.removeTag(rootId, "plop");
		assertFalse(spaceManager.getSpace(rootId).getTags().contains("plop"));
	}

	@Test
	public void testSetProperties() {
		HashMap<String, String> prop = new HashMap<String, String>();
		prop.put("color", "blue");
		prop.put("type", "bedroom");
		spaceManager.setProperties(rootId, prop);
		
		assertSame(prop, spaceManager.getSpace(rootId).getProperties());
	}

	@Test
	public void testClearPropertiesList() {
		HashMap<String, String> prop = new HashMap<String, String>();
		prop.put("color", "blue");
		prop.put("type", "bedroom");
		spaceManager.setProperties(rootId, prop);		
		
		assertEquals(3, spaceManager.getSpace(rootId).getProperties().size());
		
		spaceManager.clearPropertiesList(rootId);
		assertEquals(1, spaceManager.getSpace(rootId).getProperties().size());
	}

	@Test
	public void testAddProperty() {
		assertTrue(spaceManager.addProperty(rootId, "border", "green"));
		assertFalse(spaceManager.addProperty(rootId, "border", "black"));
	}

	@Test
	public void testRemoveProperty() {
		spaceManager.addProperty(rootId, "border", "green");
		assertTrue(spaceManager.removeProperty(rootId, "border"));
	}

	@Test
	public void testGetRootSpace() {
		assertSame(spaceManager.getSpace(rootId), spaceManager.getRootSpace());
	}

	@Test
	public void testGetPlaceWithName() {
		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(spaceManager.getRootSpace());
		
		assertEquals(placeList, spaceManager.getSpacesWithName("root"));
	}

	@Test
	public void testGetPlaceWithTags() {
		spaceManager.getRootSpace().addTag("TEST_TAG");
		spaceManager.getRootSpace().addTag("TEST_TAG_OTHER");
		ArrayList<String> tagsList = new ArrayList<String>();
		tagsList.add("TEST_TAG");
		tagsList.add("TEST_TAG_OTHER");
		
		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(spaceManager.getRootSpace());
		
		assertEquals(placeList, spaceManager.getSpacesWithTags(tagsList));
	}

	@Test
	public void testGetPlaceWithProperties() {
		spaceManager.getRootSpace().addProperty("k1", "val");
		spaceManager.getRootSpace().addProperty("k2", "val");
		
		String placeId = spaceManager.addSpace("plop", Space.CATEGORY.PLACE.toString(), spaceManager.getRootSpace().getId());
		Space place = spaceManager.getSpace(placeId);
		place.addProperty("k1", "val");
		place.addProperty("k2", "val");
		
		ArrayList<String> propertiesKey = new ArrayList<String>();
		propertiesKey.add("k1");
		propertiesKey.add("k2");

		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(place);
		placeList.add(spaceManager.getRootSpace());		

		assertEquals(placeList.size(), spaceManager.getSpacesWithProperties(propertiesKey).size());
	}

	@Test
	public void testGetPlaceWithPropertiesValue() {
		spaceManager.getRootSpace().addProperty("k1", "val1");
		spaceManager.getRootSpace().addProperty("k2", "val2");
		
		String placeId = spaceManager.addSpace("plop", Space.CATEGORY.PLACE.toString(), spaceManager.getRootSpace().getId());
		Space place = spaceManager.getSpace(placeId);
		place.addProperty("k1", "val1");
		place.addProperty("k2", "val3");
		
		HashMap<String, String> propertiesKeyValue = new HashMap<String, String>();
		propertiesKeyValue.put("k1", "val1");
		propertiesKeyValue.put("k2", "val2");

		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(spaceManager.getRootSpace());
		
		assertEquals(placeList, spaceManager.getSpacesWithPropertiesValue(propertiesKeyValue));
	}

}
