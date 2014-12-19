package appsgate.lig.ehmi.impl;

/**
 *
 * @author jr
 */
public class TimeEntry extends Entry {

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
        if (eventEntry instanceof TimeEntry) {
            TimeEntry entry = (TimeEntry) eventEntry;
            return (entry.getObjectId().contentEquals(this.getObjectId())
                    && entry.getVarName().contentEquals(this.getVarName())
                    && entry.timestamp.contentEquals(this.timestamp)
                    && entry.getValue().contentEquals(this.getValue()));
        }
        if (eventEntry instanceof Entry) {
            Entry entry = (Entry) eventEntry;
            return (entry.getObjectId().contentEquals(this.getObjectId())
                    && entry.getVarName().contentEquals(this.getVarName())
                    && entry.getValue().contentEquals(this.timestamp));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        return hash;
    }

}
