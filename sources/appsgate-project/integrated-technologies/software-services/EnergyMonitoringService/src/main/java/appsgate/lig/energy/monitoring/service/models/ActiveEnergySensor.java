/**
 * 
 */
package appsgate.lig.energy.monitoring.service.models;

/**
 * Helper class to handle energyIndex
 * manage directly watts/secs
 * handle when the energyIndex is reseted (when some smartplug are unplugged, they reset their internal counters)
 * Note: this is not very accurate as we miss some measures when the plug is turned off
 * (because the sensor reports only periodically, every 10 secs, it does report when turned on, not when turned off) 
 * @author thibaud
 *
 */
public class ActiveEnergySensor {
	
	String sensorId;
	/**
	 * @return the sensorId
	 */
	public String getSensorId() {
		return sensorId;
	}
	
	/**
	 * We initiate all the counter at 0
	 * @param sensorId
	 */
	public ActiveEnergySensor(String sensorId,
			double energyIndex,
			boolean insideMonitoringPeriod) {
		this.sensorId = sensorId;
		lastEnergyIndex = energyIndex;
		wasInsideMonitoringPeriod = insideMonitoringPeriod;
		resetEnergy();
	}
	/**
	 * @return the totalEnergy
	 */
	public double getTotalEnergy() {
		return totalEnergy;
	}
	
	/**
	 * @return the energyDuringPeriod
	 */
	public double getEnergyDuringPeriod() {
		return energyDuringPeriod;
	}
	
	public void resetEnergy() {
		totalEnergy = 0;
		energyDuringPeriod = 0;
	}
	
	public void newEnergyMeasure(double energyIndex,
			boolean insideMonitoringPeriod) {
		double diff = 0;
		if(energyIndex < lastEnergyIndex) {
			// This is likely that Plug has been reseted since last measure (for instance unplugged)
			// But the energy from 0 to the current index have been consumed for sure since last time
			diff = energyIndex;
			// TODO: maybe we should trigger an event indicating that we may have lost data (plug not paired or whatever) ? 
		} else if (energyIndex >= lastEnergyIndex) {
			// Plug does seems to work properly since last time and energy have changed
			diff = energyIndex-lastEnergyIndex;
		}
		totalEnergy+=diff;
		
		if (insideMonitoringPeriod) {
			if(wasInsideMonitoringPeriod) {
				// if we are in a period and we were previously in a period, we add the value
				energyDuringPeriod+=diff;
			}// no need for else, we consider that we just entered in the period
		} else {
			if(wasInsideMonitoringPeriod) {
				// to be consistent with the previous case, we consider that we just exited from the period
				energyDuringPeriod+=diff;
			}
		}
		
		lastEnergyIndex = energyIndex;
		wasInsideMonitoringPeriod = insideMonitoringPeriod;		
	}

	double totalEnergy;
	double energyDuringPeriod;
	double lastEnergyIndex;
	boolean wasInsideMonitoringPeriod;
	
	/**
	 * @return the wasInsideMonitoringPeriod
	 */
	public boolean isInsideMonitoringPeriod() {
		return wasInsideMonitoringPeriod;
	}

	/**
	 * Useful if we know that we entered/exited a period but there was no measure since  
	 * @param wasInsideMonitoringPeriod the wasInsideMonitoringPeriod to set
	 */
	public void setInsideMonitoringPeriod(boolean insideMonitoringPeriod) {
		this.wasInsideMonitoringPeriod = insideMonitoringPeriod;
	}

}
