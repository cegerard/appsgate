package appsgate.lig.energy.monitoring.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnergySensorPool {
	
	private final static Logger logger = LoggerFactory.getLogger(EnergySensorPool.class);

	
	private Map<String, Double> sensors;
	
	private static EnergySensorPool INSTANCE = new EnergySensorPool();
	
	/**
	 * This is a singleton pattern, constructor hidden
	 */
	private EnergySensorPool() {
		sensors = new HashMap<String, Double>();
	}
	
	public static EnergySensorPool getInstance() {
		return INSTANCE;
	}
	
	public void addEnergyMeasure(String sensorId, double energyIndex) {
		logger.trace("addEnergyMeasure(String sensorID : {}, double energyIndex : {})",
				sensorId, energyIndex);
		if(sensorId != null && energyIndex >=0) {
			sensors.put(sensorId, energyIndex);
		} else {
			logger.warn("addEnergyMeasure(), measure irrelevant sensorID is null or energyIndex negative");
		}
	}
	
	public double getEnergyMeasure(String sensorId) {
		logger.trace("getEnergyMeasure(String sensorID : {})",
				sensorId);
		if(sensorId != null && sensors.containsKey(sensorId)) {
			return sensors.get(sensorId).doubleValue();
		} else {
			logger.warn("getEnergyMeasure(), measure irrelevant sensorID is null or not in the pool");
			return -1;
		}		
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		for(String sensorId : sensors.keySet()){
			result.put(sensorId, getEnergyMeasure(sensorId));
		}
		logger.trace("toJSON(), returning"+result);
		return result;
	}
	
	public void restoreFromJSON(JSONObject measures) {
		logger.trace("restoreFromJSON(JSONObject measures : {})",measures);
		
		if(measures != null) {
			for(Object sensorId : measures.keySet()) {
				if(!sensors.containsKey(sensorId)
						&& sensors.get(sensorId) < 0) {
					double energyIndex = measures.optDouble(sensorId.toString(), -1);
					logger.trace("restoreFromJSON(...), sensor not in the pool or wrong latest index,"
							+ " restoring the value from JSON : "+energyIndex);
					addEnergyMeasure(sensorId.toString(), energyIndex);
				} else {
					logger.trace("restoreFromJSON(...), sensor already in the pool with a valid value, "
							+ " we do not restore the value from JSON (assuming the live index is newer)");
				}
			}
		}else {
			logger.trace("restoreFromJSON(...), no measure to store");
		}
	}

}
