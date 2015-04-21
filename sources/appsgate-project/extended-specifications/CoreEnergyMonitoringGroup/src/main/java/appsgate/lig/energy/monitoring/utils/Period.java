package appsgate.lig.energy.monitoring.utils;

import org.json.JSONObject;

/** 
 * Helper class to handle mmonitoring periods and serialize/deserialize them as JSONObject
 * Only getters method (attributes can only be set using constructors)
 * @author thibaud
 *
 */
public class Period {

	long startDate;
	long endDate;
	long periodOffset;
	long periodInterval;
	JSONObject json;
	
	public static final String START_KEY = "start";
	public static final String END_KEY = "end";
	public static final String OFFSET_KEY = "offset";
	public static final String INTERVAL_KEY = "interval";

	/**
	 * Return the starting date of the Energy monitoring period for this group
	 * @return the starting date in millisecs from the epoch (01/01/1970) 
	 */
	public long getStartDate() {
		return startDate;
	}

	/**
	 * Return the ending date of the Energy monitoring period for this group
	 * @return the ending date in millisecs from the epoch (01/01/1970) 
	 */
	public long getEndDate() {
		return endDate;
	}

	/**
	 * Return the interval offset since the epoch. Each interval will apply
	 * @return the interval in millisecs
	 */
	public long getPeriodOffset() {
		return periodOffset;
	}

	/**
	 * Return the interval defined
	 * @return the interval in millisecs
	 */
	public long getPeriodInterval() {
		return periodInterval;
	}
	
	
	
	/**
	 * Budget will only be measured between the start and the end of the period, the period can repeat during time
	 * at each interval.
	 * Note that changing the period reset the budget measure.
	 * @param startDate -1 if no period
	 * @param endDate -1 if no period
	 * @param periodOffset by default 0 (used if relative to reals days, week, month, ...) 
	 * @param periodInterval in ms (for instance 24 x 60 x 60 x 1000 define an interval of a day),
	 * if any -1 value will indicate that there is no period defined (energy during period = energy total)  
	 */
	public Period(long startDate, long endDate, long periodOffset, long periodInterval) {
		this.startDate= startDate;
		this.endDate = endDate;
		this.periodInterval = periodInterval;
		this.periodOffset = periodOffset;
		
		json = toJSON(this);
	}
	
	/**
	 * Default constructor creates a Period with defaults values
	 * (such a Period is equivalent to always or never, depending on the usage) 
	 */
	public Period() {
		this(-1, -1, 0, -1);
	}
	
	public Period(JSONObject periodAsJSON) {
		this();
		if(periodAsJSON != null ) {
			startDate = (periodAsJSON.has(START_KEY)?periodAsJSON.getLong(START_KEY):-1);
			endDate = (periodAsJSON.has(END_KEY)?periodAsJSON.getLong(END_KEY):-1);
			periodInterval = (periodAsJSON.has(INTERVAL_KEY)?periodAsJSON.getLong(INTERVAL_KEY):-1);
			periodOffset = (periodAsJSON.has(OFFSET_KEY)?periodAsJSON.getLong(OFFSET_KEY):0);
		}
		json = toJSON(this);
	}
	
	public static JSONObject toJSON(Period period) {
		JSONObject result = new JSONObject();
		result.put(START_KEY, period.startDate);
		result.put(END_KEY, period.endDate);
		result.put(INTERVAL_KEY, period.periodInterval);
		result.put(OFFSET_KEY, period.periodOffset);
		
		return result;
	}

	@Override
	public String toString() {
		return "Period [startDate=" + startDate + ", endDate=" + endDate
				+ ", periodOffset=" + periodOffset + ", periodInterval="
				+ periodInterval + ", json=" + json + "]";
	}	

}
