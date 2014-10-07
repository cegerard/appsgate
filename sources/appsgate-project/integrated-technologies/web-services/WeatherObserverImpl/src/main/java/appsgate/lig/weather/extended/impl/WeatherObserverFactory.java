package appsgate.lig.weather.extended.impl;

import appsgate.lig.context.services.DataBasePullService;
import appsgate.lig.context.services.DataBasePushService;
import appsgate.lig.weather.exception.WeatherForecastException;
import appsgate.lig.weather.extended.spec.ExtendedWeatherObserver;
import appsgate.lig.weather.spec.WeatherAdapterSpec;
import appsgate.lig.yahoo.weather.YahooWeather;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import fr.imag.adele.apam.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by thibaud on 02/07/2014.
 */
public class WeatherObserverFactory implements WeatherAdapterSpec {
    private static Logger logger = LoggerFactory
            .getLogger(WeatherObserverFactory.class);
    BundleContext context;

    public WeatherObserverFactory(BundleContext context) {
        this.context = context;
    }

    public static final String LOCATIONS="org.lig.appsgate.weather.locations";
    Timer timer=null;
    Instance mySelf;


    public void start(Instance myself) {
        logger.trace("start()");
        mySelf=myself;


        try {
            for(String location : Util.split(context.getProperty(WeatherObserverFactory.LOCATIONS))) {
                logger.debug("Adding "+location+" to the pending weather observer list");
                pendingLocations.add(location);
            }
        } catch (Exception exc) {
            logger.error(" Exception occured when reading the locations : "+exc.getMessage());
        }
        SynchroObserverTask nextRefresh = new SynchroObserverTask(this);
        timer = new Timer();
        // Next refresh will in 30secs (DB and web service should be available by then)
        timer.schedule(nextRefresh, 30 * 1000);
        logger.trace("started successfully, waiting for SynchroObserverTask to wake up");


    }

    public void synchronizeObservers() {
        logger.trace("synchronizeObservers()");
        restoreFromDB();

        for(String location:pendingLocations.toArray(new String[0]) ) {
            checkRunningLocations();
            addLocationObserver(location);
        }



        SynchroObserverTask nextRefresh = new SynchroObserverTask(this);
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(nextRefresh, 10 * 60 * 1000); // One auto refresh each 10 minutes (in case BD is lost or weather service connection)

    }


    /**
     * This one is the master's list
     * (especially when connection with weather server is broken or DB is down)
     */
    Set<String> pendingLocations=new ConcurrentSkipListSet<String>();

    /**
     * Contains locations that are currently synchronized with DB
     */
    Set<String> dbLocations=new ConcurrentSkipListSet<String>();

    /**
     * contains location for which observers have been successfully created
     */
    Set<String> runningLocations=new ConcurrentSkipListSet<String>();



    /**
     * Context history pull service to get past table state
     */
    private DataBasePullService contextHistory_pull;

    /**
     * Context history push service to save the current state
     */
    private DataBasePushService contextHistory_push;
    
    private YahooWeather weatherService;

    
    @Override
    public JSONObject checkLocation(String location) {
        if(location!= null && location.length()>0 && weatherService != null) {
        	return weatherService.checkLocation(location);
        }
        else {
        	return new JSONObject();
        }
    }


    @Override
    public void addLocationObserver(String location) {
        if(location!= null && location.length()>0) {
            // One annoying thing, if the location is incorrect
            // (i.e: the place does not exist, we cannot check it util the WeatherAdapter is bound indirectly using the instance)
            pendingLocations.add(location);
            WeatherObserverImpl service = createObserver(location);
            String objectId;
            if(service!=null) {
                objectId=service.getAbstractObjectId();
            } else {
                objectId=WeatherObserverImpl.getFakeObjectId(location);
            }
            addDBEntry(objectId, location);
        }
    }

    private void addDBEntry(String objectId, String location) {
        if(restoreFromDB() && !dbLocations.contains(location)) {
            logger.debug("Adding weather location in DB for "+location+"...");
            ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
            for(String loc: pendingLocations) {
                properties.add(new AbstractMap.SimpleEntry<String, Object>("currentLocation", loc));
            }
            contextHistory_push.pushData_add(this.getClass().getSimpleName(), objectId, location, properties);
            dbLocations.add(location);
            logger.debug("... successfully added !");

        }
    }
    private void removeDBEntry(String objectId, String location) {
        if(restoreFromDB() && dbLocations.contains(location)) {
            logger.debug("Removing  weather location in DB for "+location+"...");
            ArrayList<Map.Entry<String, Object>> properties = new ArrayList<Map.Entry<String, Object>>();
            for(String loc: pendingLocations) {
                properties.add(new AbstractMap.SimpleEntry<String, Object>("currentLocation", loc));
            }
            contextHistory_push.pushData_remove(this.getClass().getSimpleName(), objectId, location, properties);
            dbLocations.remove(location);
            logger.debug("... successfully removed !");
        }
    }

    @Override
    public void removeLocationObserver(String location) {
        if(location!= null && location.length()>0) {
            // One annoying thing, if the location is incorrect
            // (i.e: the place does not exist, we cannot check it util the WeatherAdapter is bound indirectly using the instance)
            pendingLocations.remove(location);
            removeDBEntry(WeatherObserverImpl.getFakeObjectId(location), location);
            checkRunningLocations(); // this should destroy the instance
        }
    }

    @Override
    public Set<String> getActiveLocationsObservers() {
        checkRunningLocations();
        return runningLocations;
    }

    @Override
    public Set<String> getAllLocationsObservers() {
        return pendingLocations;
    }

    private synchronized WeatherObserverImpl getObserver(String location) {
        Implementation observerImpl = CST.apamResolver.findImplByName(null, WeatherObserverImpl.IMPL_NAME);

        if (observerImpl != null) {
            for (Instance inst : observerImpl.getInsts()) {
                WeatherObserverImpl service = (WeatherObserverImpl) inst.getServiceObject();
                if (service != null &&location.equals(service.getCurrentLocation())) {
                    return service;
                }
            }
        }
        return null;
    }


    private synchronized WeatherObserverImpl createObserver(String location) {
        logger.debug("createObserver(String location : "+location+")");
        if(runningLocations.contains(location)) {
            logger.warn("location already registered and should be running, new Observer not created");
            return getObserver(location);
        }
        Instance inst=null;

        try {
            Implementation observerImpl = CST.apamResolver.findImplByName(null,WeatherObserverImpl.IMPL_NAME);

            Map<String,String> configuration = new Hashtable<String,String>();
            configuration.put("currentLocation", location);

            inst = observerImpl.createInstance(mySelf.getComposite(), configuration);
            WeatherObserverImpl service = (WeatherObserverImpl) inst.getServiceObject();
            service.getCurrentWeatherCode(); // If no Exception, service should be OK
            runningLocations.add(location);

            return service;

        } catch( Exception exc) {
            logger.warn("Exception when creating WeatherObserver for "+location+" : "+exc.getMessage());
            exc.printStackTrace();
            if(inst!=null) {
                ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst);
            }
            return null;
        }
    }



    /**
     * Checks and eventually destroy observer that are not running correctly
     * or destroy runningLocations string that does not match an existing observer
     */
    public synchronized void checkRunningLocations() {
        Set<String> workingLocations=new HashSet<String>();
        Set<String> toRemove=new HashSet<String>();

        Implementation observerImpl = CST.apamResolver.findImplByName(null,WeatherObserverImpl.IMPL_NAME);
        if(observerImpl == null) {
            logger.warn("WeatherObserverImpl.IMPL_NAME not found, so there should be no running instances");
            runningLocations=new ConcurrentSkipListSet<String>();
        }

        // Step 1 getting all reals instances of observers
        for(Instance inst : observerImpl.getInsts())  {
            String loc = null;
            try {

                ExtendedWeatherObserver service = (ExtendedWeatherObserver)inst.getServiceObject();
                service.getCurrentWeatherCode();
                loc = service.getCurrentLocation();
                workingLocations.add(loc);

                if(!pendingLocations.contains(loc)) {
                    throw new Exception("this working location should be removed");
                }


                if (!runningLocations.contains(loc)) {
                    if(pendingLocations.contains(loc)) {
                        runningLocations.add(loc);
                    } else {
                        throw new Exception("this working location should not be there");
                    }
                }
            } catch (Exception exc) {
                logger.warn("Removing an instance. Exception : "+exc.getMessage());
                exc.printStackTrace();
                if(inst!=null) {
                    ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(inst);
                }
                if(loc != null) {
                    runningLocations.remove(loc);
                }
            }
        }

        // Step 2 removing the instances that does not exist anymore
        for(String loc : runningLocations) {
            if(!workingLocations.contains(loc)) {
                toRemove.add(loc);
            }
        }
        for(String loc : toRemove) {
            runningLocations.remove(loc);
        }
    }




    private boolean synchroDB =false;
    /**
     * @return
     */
    private synchronized boolean restoreFromDB() {
        if(synchroDB) {
            return true;
        } else if (contextHistory_pull !=null && contextHistory_pull.testDB()) {
            synchroDB =true;
            getDBLocationList();
            pendingLocations.addAll(dbLocations);
            logger.debug("DB locations have been restored");
            return true;
        }
        logger.debug("DB not available");
        return false;
    }

    private void getDBLocationList() {
        JSONObject table = contextHistory_pull.pullLastObjectVersion(this.getClass().getSimpleName());
        if(table !=null &&table.getJSONArray("state")!=null) {
            try {
                JSONArray states = table.getJSONArray("state");
                int length = states.length();
                for(int i=0; i<states.length(); i++) {
                    JSONObject obj = states.getJSONObject(i);
                    String loc= obj.getString("currentLocation");
                    logger.debug("Reading "+loc+" from the DB");

                    if(loc!= null) {
                        dbLocations.add(loc);
                    }
                }
                logger.debug("The weather locations have been red from the DB");

            } catch (JSONException e) {
                logger.warn("Error when reading DB : " + e.getMessage());
            }
        }
    }

	@Override
	public JSONArray checkLocationsStartingWith(String firstLetters) {
        if(firstLetters!= null && firstLetters.length()>0 && weatherService != null) {
        	return weatherService.checkLocationsStartingWith(firstLetters);
        }
        else {
        	return new JSONArray();
        }
	}


}
