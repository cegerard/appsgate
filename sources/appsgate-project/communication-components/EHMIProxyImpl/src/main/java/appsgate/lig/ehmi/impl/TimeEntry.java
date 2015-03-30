package appsgate.lig.ehmi.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.chmi.spec.GenericCommand;

/**
 *
 * @author jr
 */
public class TimeEntry extends Entry {
	private static Logger logger = LoggerFactory.getLogger(TimeEntry.class);

	
    @Override
	public String toString() {
		return super.toString()+", TimeEntry [timestamp=" + timestamp + "]";
	}

	/**
     *
     */
    private final String timestamp;

    /**
     *
     * @param e
     * @param time
     */
    public TimeEntry(Entry e, int id) {
        super(e.getObjectId(), e.getVarName(), String.valueOf(id));
        timestamp = e.getValue();
    }

    /**
     * Override the equals method to compare two entry together but compare
     * their contents
     */
    @Override
    public boolean equals(Object eventEntry) {
    	logger.trace("equals(Object eventEntry), comparing with this : "+toString());
    	
        if (eventEntry instanceof TimeEntry) {
            TimeEntry entry = (TimeEntry) eventEntry;
        	logger.trace("equals(...), eventEntry is a Time Entry :  : "+entry.toString());

            return (entry.getObjectId().contentEquals(this.getObjectId())
                    && entry.getVarName().contentEquals(this.getVarName())
                    && entry.timestamp.contentEquals(this.timestamp)
                    && entry.getValue().contentEquals(this.getValue()));
        } else if (eventEntry instanceof Entry) {
            Entry entry = (Entry) eventEntry;
        	logger.trace("equals(...), eventEntry is a regular Entry :  : "+entry.toString());
            return (entry.getObjectId().contentEquals(this.getObjectId())
                    && entry.getVarName().contentEquals(this.getVarName())
                    && entry.getValue().contentEquals(this.timestamp));
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        return hash;
    }

}
