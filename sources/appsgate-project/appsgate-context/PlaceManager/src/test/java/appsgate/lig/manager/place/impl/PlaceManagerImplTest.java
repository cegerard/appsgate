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
import appsgate.lig.manager.place.spec.SymbolicPlace;

public class PlaceManagerImplTest {

	protected Synchroniser synchroniser = new Synchroniser();
	
	Mockery context = new Mockery(){
        {
            setThreadingPolicy(synchroniser);
        }
    };
    
    private DataBasePullService pull_service;
    private DataBasePushService push_service;
    
    private PlaceManagerImpl placeManager; 
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
		 placeManager = new PlaceManagerImpl();
		 placeManager.initiateMock(pull_service, push_service);
		 placeManager.newInst();
		 rootId = placeManager.addPlace("home", null);
		 assertNotNull(rootId);
	}

	@After
	public void tearDown() throws Exception {
		placeManager.deleteInst();
	}

	@Test
	public void testAddPlace() {
		assertNotNull(placeManager.addPlace("NewRoom", rootId));
	}

	@Test
	public void testRemovePlace() {
		String placeId = placeManager.addPlace("NewRoom1", rootId);
		assertTrue(placeManager.removePlace(placeId));
	}

	@Test
	public void testMoveObject() {
		String placeId = placeManager.addPlace("NewRoom2", rootId);
		assertTrue(placeManager.moveObject("42", "-1", placeId));
	}

	@Test
	public void testRenamePlace() {
		String placeId = placeManager.addPlace("NewRoom3", rootId);
		assertTrue(placeManager.renamePlace(placeId, "NewPlaceName"));
	}

	@Test
	public void testGetSymbolicPlace() {
		String placeId = placeManager.addPlace("NewRoom4", rootId);
		assertEquals("NewRoom4", placeManager.getSymbolicPlace(placeId).getName());
	}

	@Test
	public void testGetJSONPlaces() {
		assertNotNull(placeManager.getJSONPlaces());
		System.out.println("testGetJSONPlaces ---- return :"+placeManager.getJSONPlaces().toString());
	}

	@Test
	public void testGetCoreObjectPlaceId() {
		String placeId = placeManager.addPlace("NewRoom5", rootId);
		assertTrue(placeManager.moveObject("42", "-1", placeId));
		assertEquals(placeId, placeManager.getCoreObjectPlaceId("42"));
	}

	@Test
	public void testMovePlace() {
		String placeId  = placeManager.addPlace("livingRoom", rootId);
		String placeId2 = placeManager.addPlace("readingPlace", rootId);
		
		assertNotSame(placeManager.getSymbolicPlace(placeId), placeManager.getSymbolicPlace(placeId2).getParent());
		assertSame(placeManager.getRootPlaces().get(0), placeManager.getSymbolicPlace(placeId2).getParent());
		assertTrue(placeManager.getRootPlaces().get(0).hasChild(placeManager.getSymbolicPlace(placeId2)));
		assertFalse(placeManager.getSymbolicPlace(placeId).hasChild(placeManager.getSymbolicPlace(placeId2)));
		
		placeManager.movePlace(placeId2, placeId);
		
		assertNotSame(placeManager.getRootPlaces().get(0), placeManager.getSymbolicPlace(placeId2).getParent());
		assertSame(placeManager.getSymbolicPlace(placeId), placeManager.getSymbolicPlace(placeId2).getParent());
		assertTrue(placeManager.getSymbolicPlace(placeId).hasChild(placeManager.getSymbolicPlace(placeId2)));
		assertFalse(placeManager.getRootPlaces().get(0).hasChild(placeManager.getSymbolicPlace(placeId2)));
	}

	@Test
	public void testSetTagsList() {
		String placeId  = placeManager.addPlace("livingRoom1", rootId);
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("blue");
		tags.add("bedroom");
		placeManager.setTagsList(placeId, tags);
		
		assertSame(tags, placeManager.getSymbolicPlace(placeId).getTags());
	}

	@Test
	public void testClearTagsList() {
		String placeId  = placeManager.addPlace("livingRoom2", rootId);
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("blue");
		tags.add("bedroom");
		placeManager.setTagsList(placeId, tags);
		assertEquals(2, placeManager.getSymbolicPlace(placeId).getTags().size());
		
		placeManager.clearTagsList(placeId);
		assertEquals(0, placeManager.getSymbolicPlace(placeId).getTags().size());
	}

	@Test
	public void testAddTag() {
		assertTrue(placeManager.addTag(rootId, "plop"));
	}

	@Test
	public void testRemoveTag() {
		assertTrue(placeManager.addTag(rootId, "plop"));
		assertTrue(placeManager.getSymbolicPlace(rootId).getTags().contains("plop"));
		placeManager.removeTag(rootId, "plop");
		assertFalse(placeManager.getSymbolicPlace(rootId).getTags().contains("plop"));
	}

	@Test
	public void testSetProperties() {
		HashMap<String, String> prop = new HashMap<String, String>();
		prop.put("color", "blue");
		prop.put("type", "bedroom");
		placeManager.setProperties(rootId, prop);
		
		assertSame(prop, placeManager.getSymbolicPlace(rootId).getProperties());
	}

	@Test
	public void testClearPropertiesList() {
		HashMap<String, String> prop = new HashMap<String, String>();
		prop.put("color", "blue");
		prop.put("type", "bedroom");
		placeManager.setProperties(rootId, prop);		
		
		assertEquals(2, placeManager.getSymbolicPlace(rootId).getProperties().size());
		
		placeManager.clearPropertiesList(rootId);
		assertEquals(0, placeManager.getSymbolicPlace(rootId).getProperties().size());
	}

	@Test
	public void testAddProperty() {
		assertTrue(placeManager.addProperty(rootId, "border", "green"));
		assertFalse(placeManager.addProperty(rootId, "border", "black"));
	}

	@Test
	public void testRemoveProperty() {
		placeManager.addProperty(rootId, "border", "green");
		assertTrue(placeManager.removeProperty(rootId, "border"));
	}

	@Test
	public void testRemoveAllCoreObject() {
		placeManager.moveObject("00001", "-1", rootId);
		placeManager.moveObject("00002", "-1", rootId);
		assertEquals(2, placeManager.getSymbolicPlace(rootId).getCoreObjects().size());
		
		placeManager.removeAllCoreObject(rootId);
		assertEquals(0, placeManager.getSymbolicPlace(rootId).getCoreObjects().size());
	}

	@Test
	public void testGetRootPlace() {
		assertSame(placeManager.getSymbolicPlace(rootId), placeManager.getRootPlaces().get(0));
	}

	@Test
	public void testGetPlaceWithName() {
		ArrayList<SymbolicPlace> placeList = new ArrayList<SymbolicPlace>();
		placeList.add(placeManager.getRootPlaces().get(0));
		
		assertEquals(placeList, placeManager.getPlacesWithName("home"));
	}

	@Test
	public void testGetPlaceWithTags() {
		placeManager.getRootPlaces().get(0).addTag("TEST_TAG");
		placeManager.getRootPlaces().get(0).addTag("TEST_TAG_OTHER");
		ArrayList<String> tagsList = new ArrayList<String>();
		tagsList.add("TEST_TAG");
		tagsList.add("TEST_TAG_OTHER");
		
		ArrayList<SymbolicPlace> placeList = new ArrayList<SymbolicPlace>();
		placeList.add(placeManager.getRootPlaces().get(0));
		
		assertEquals(placeList, placeManager.getPlacesWithTags(tagsList));
	}

	@Test
	public void testGetPlaceWithProperties() {
		placeManager.getRootPlaces().get(0).addProperty("k1", "val");
		placeManager.getRootPlaces().get(0).addProperty("k2", "val");
		
		String placeId = placeManager.addPlace("plop", placeManager.getRootPlaces().get(0).getId());
		SymbolicPlace place = placeManager.getSymbolicPlace(placeId);
		place.addProperty("k1", "val");
		place.addProperty("k2", "val");
		
		ArrayList<String> propertiesKey = new ArrayList<String>();
		propertiesKey.add("k1");
		propertiesKey.add("k2");

		ArrayList<SymbolicPlace> placeList = new ArrayList<SymbolicPlace>();
		placeList.add(place);
		placeList.add(placeManager.getRootPlaces().get(0));		

		assertEquals(placeList.size(), placeManager.getPlacesWithProperties(propertiesKey).size());
	}

	@Test
	public void testGetPlaceWithPropertiesValue() {
		placeManager.getRootPlaces().get(0).addProperty("k1", "val1");
		placeManager.getRootPlaces().get(0).addProperty("k2", "val2");
		
		String placeId = placeManager.addPlace("plop", placeManager.getRootPlaces().get(0).getId());
		SymbolicPlace place = placeManager.getSymbolicPlace(placeId);
		place.addProperty("k1", "val1");
		place.addProperty("k2", "val3");
		
		HashMap<String, String> propertiesKeyValue = new HashMap<String, String>();
		propertiesKeyValue.put("k1", "val1");
		propertiesKeyValue.put("k2", "val2");

		ArrayList<SymbolicPlace> placeList = new ArrayList<SymbolicPlace>();
		placeList.add(placeManager.getRootPlaces().get(0));
		
		assertEquals(placeList, placeManager.getPlacesWithPropertiesValue(propertiesKeyValue));
	}

}
