package appsgate.lig.manager.place.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.manager.space.impl.SpaceManagerImpl;
import appsgate.lig.manager.space.spec.Space;
import appsgate.lig.manager.space.spec.Space.TYPE;

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
    private Space rootSpace;
	
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
		 rootSpace = spaceManager.getRootSpace();
		 assertNotNull(rootSpace);
	}

	@After
	public void tearDown() throws Exception {
		spaceManager.deleteInst();
	}

	@Test
	public void testAddSpace() {
		assertNotNull(spaceManager.addSpace(TYPE.PLACE, rootSpace));
	}

	@Test
	public void testRemoveSpace() {
		String placeId = spaceManager.addSpace(TYPE.PLACE, rootSpace);
		assertTrue(spaceManager.removeSpace(spaceManager.getSpace(placeId)));
	}

	@Test
	public void testGetSpace() {
		HashMap<String, String> prop =  new HashMap<String, String>();
		prop.put("name", "NewRoom4");
		String placeId = spaceManager.addSpace(TYPE.PLACE, prop, rootSpace);
		assertEquals("NewRoom4", spaceManager.getSpace(placeId).getName());
	}

	@Test
	public void getSpaces() {
		assertNotNull(spaceManager.getSpaces());
	}

	@Test
	public void testMoveSpace() {
		String placeId  = spaceManager.addSpace(TYPE.PLACE, rootSpace);
		Space place1 = spaceManager.getSpace(placeId);
		String placeId2 = spaceManager.addSpace(TYPE.PLACE, rootSpace);
		Space place2 = spaceManager.getSpace(placeId2);
		
		assertNotSame(spaceManager.getSpace(placeId), spaceManager.getSpace(placeId2).getParent());
		assertSame(rootSpace, spaceManager.getSpace(placeId2).getParent());
		assertTrue(rootSpace.hasChild(spaceManager.getSpace(placeId2)));
		assertFalse(spaceManager.getSpace(placeId).hasChild(spaceManager.getSpace(placeId2)));
		
		spaceManager.moveSpace(place2, place1);
		
		assertNotSame(rootSpace, spaceManager.getSpace(placeId2).getParent());
		assertSame(spaceManager.getSpace(placeId), spaceManager.getSpace(placeId2).getParent());
		assertTrue(spaceManager.getSpace(placeId).hasChild(spaceManager.getSpace(placeId2)));
		assertFalse(rootSpace.hasChild(spaceManager.getSpace(placeId2)));
	}

	@Test
	public void testGetRootSpace() {
		assertSame(rootSpace, spaceManager.getRootSpace());
	}

	@Test
	public void testGetPlaceWithName() {
		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(rootSpace);
		assertEquals(placeList, spaceManager.getSpacesWithName("root"));
	}

	@Test
	public void testGetPlaceWithTags() {
		rootSpace.addTag("TEST_TAG");
		rootSpace.addTag("TEST_TAG_OTHER");
		ArrayList<String> tagsList = new ArrayList<String>();
		tagsList.add("TEST_TAG");
		tagsList.add("TEST_TAG_OTHER");
		
		ArrayList<Space> placeList = new ArrayList<Space>();
		placeList.add(rootSpace);
		
		assertEquals(placeList, spaceManager.getSpacesWithTags(tagsList));
	}

	@Test
	public void testGetPlaceWithProperties() {
		rootSpace.addProperty("k1", "val");
		rootSpace.addProperty("k2", "val");
		
		String placeId = spaceManager.addSpace(TYPE.PLACE, spaceManager.getRootSpace());
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
		
		String placeId = spaceManager.addSpace(TYPE.PLACE, spaceManager.getRootSpace());
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
	
	@Test
	public void testGetTreeDescription() {
		JSONObject obj = spaceManager.getTreeDescription();
		assertNotNull(obj);
		System.out.println(obj);
	}
	
	@Test
	public void testGetTreeDescriptionSpace() {
		JSONObject obj = spaceManager.getTreeDescription(spaceManager.getDeviceRoot(spaceManager.getCurrentHabitat()));
		assertNotNull(obj);
		System.out.println(obj);
	}

}
