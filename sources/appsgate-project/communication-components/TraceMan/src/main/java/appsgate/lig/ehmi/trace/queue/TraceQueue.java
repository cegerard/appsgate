package appsgate.lig.ehmi.trace.queue;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.ehmi.trace.TraceMan;

    /*****************************/
    /** Trace Queue inner class **/
    /*****************************/

	/**
     * TraceQueue is a dedicated queue for AppsGate
     * JSON formatted traces.
     * 
     * @author Cedric Gerard
     * @since August 06, 2014
     * @version 0.5.0
     */
    public class TraceQueue extends ArrayBlockingQueue<JSONObject>{

    	
		private static final long serialVersionUID = 1L;
		
	    /**
	     * Thread to manage trace writing and aggregation
	     */
	    private TraceExecutor traceExec;

        /**
         * Timer to wake up the thread each deltaT time interval
         */
        private Timer timer;

        /**
         * The next time to log
         */
        private long logTime;
	    
	    /**
	     * Define the trace time width in milliseconds
	     * value to 0 means no aggregation interval and each change
	     * is trace when it appear
	     */
	    private long deltaTinMillis;
	    
	    /**
	     * Is the trace executor thread initiated or not
	     */
	    private boolean initiated = false;

	    /**
	     * The parent manager
	     */
		private TraceMan manager;

		/**
    	 * Default TraceQueue constructor with max capacity
    	 * set to 50000 elements
    	 * @param deltaT time interval size of packet
    	 */
		public TraceQueue(TraceMan manager, long deltaT) {
			super(50000);
			this.manager = manager;
			this.deltaTinMillis = deltaT;
			traceExec = new TraceExecutor(this);
		}
		
		/**
    	 * TraceQueue constructor with max capacity
    	 * @param capacity the max queue capacity
    	 * @param deltaT time interval size of packet
    	 */
		public TraceQueue(TraceMan manager, int capacity, long deltaT) {
			super(capacity);
			this.manager = manager;
			this.deltaTinMillis = deltaT;
			traceExec = new TraceExecutor(this);
		}

        /**
         * Initiate the trace executor thread/service
         */
        public void initTraceExec() {
            new Thread(traceExec).start();
            timer = new Timer();
            if(deltaTinMillis > 0) {
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized(traceExec) {
                            if(/*traceQueue != null &&*/ !isEmpty() && traceExec.isSleeping()){ //TODO pay attention to the null value
                                logTime = manager.getCurrentTimeInMillis();
                                traceExec.notify();
                            }
                        }
                    }
                }, deltaTinMillis, deltaTinMillis);
            }
            initiated = true;
        }
		
    	@Override
		public boolean offer(JSONObject e) {
			boolean res = super.offer(e);
			
			synchronized(traceExec) {
				if(deltaTinMillis == 0 && traceExec.isSleeping()) {
	    			traceExec.notify();
	    		}
	    	}
			
			return res;
		}

		/**
    	 * Stop the trace queue thread execution
    	 */
    	public void stop() {
    		if(timer != null) {
    			timer.cancel();
    			timer.purge();
    		}
			traceExec.stop();
			synchronized(traceExec) {
				traceExec.notify();
			}
		}
		
		/**
		 * Load the Queue with a collection of traces
		 * @param traces the collection to load
		 * @throws JSONException 
		 */
		public synchronized void loadTraces (JSONArray traces) throws JSONException {
			
			ArrayList<JSONObject> traceCollection = new ArrayList<JSONObject>();
			int nbTrace = traces.length();
			
			for(int i =0; i < nbTrace; i++){
				traceCollection.add(traces.getJSONObject(i));
			}

			this.clear();
			this.addAll(traceCollection);
		}
		
		/**
	     * Get the current delta time for trace aggregation
	     * @return the delta time in milliseconds
	     */
	    public long getDeltaTinMillis() {
			return deltaTinMillis;
		}
	    
	    /**
	     * Set the delta time for traces aggregation
	     * @param deltaTinMillis the new delta time value
	     */
		public void setDeltaTinMillis(long deltaTinMillis) {
			if(deltaTinMillis != this.deltaTinMillis) {
				this.deltaTinMillis = deltaTinMillis;
				reScheduledTraceTimer(deltaTinMillis);
			}
		}

        /**
         * Schedule the trace timer for aggregation
         * @param time the time interval
         */
        public void reScheduledTraceTimer(long time){
        	if(isInitiated()){
        		timer.cancel();
        		timer.purge();

        		timer = new Timer();
        		if(deltaTinMillis > 0) {
        			timer.scheduleAtFixedRate(new TimerTask() {
        				@Override
        				public void run() {
        					synchronized (traceExec) {
        						if (!isEmpty() && traceExec.isSleeping()) {
        							logTime = manager.getCurrentTimeInMillis();
        							traceExec.notify();
        						}
        					}
        				}
        			}, deltaTinMillis, deltaTinMillis);
        		}
        	}
        }
		
		/**
		 * Aggregate traces with the specified policy
		 * @param from the starting date
		 * @param policy the policy to apply to traces
		 * @return a JSONArray of aggregate traces
		 * @throws JSONException 
		 */
		public synchronized JSONArray applyAggregationPolicy(long from, JSONObject policy) throws JSONException{
			
			JSONArray aggregateTraces = new JSONArray();
			
			if(policy == null){ //default aggregation (time and identifiers)

				long beginInt = from;
				long  endInt = from+deltaTinMillis;
                JSONArray tracesPacket = new JSONArray();
                
                while(!isEmpty()) {

                	JSONObject latestTrace = peek();
                	long latestTraceTS = latestTrace.getLong("timestamp");
                	
                	if(latestTraceTS >= beginInt && latestTraceTS < endInt ){
                		tracesPacket.put(poll());
                	}else{
                		beginInt = endInt;
                		endInt +=  deltaTinMillis;
                		
                		if(tracesPacket.length() > 0){ //A packet is complete
                			traceExec.aggregation(aggregateTraces, tracesPacket, beginInt);
                			tracesPacket = new JSONArray();
                		}
                	}
                }
                
                if(tracesPacket.length() > 0){ //A packet is complete
        			traceExec.aggregation(aggregateTraces, tracesPacket, endInt);
                }
                
			} else { //Apply specific aggregation policy
				//generic aggregation mechanism
				//aggregationWithPolicy(aggregateTraces, tracesPacket, logTime, policy)
			}
			return aggregateTraces;
		}
		
		/**
		 * Is the trace executor thread initiated
		 * @return true if the trace executor thread is started, false otherwise
		 */
		public boolean isInitiated() {
			return initiated;
		}

		/**
		 * Set the initiated value
		 * @param isInitiated the new initiated status
		 */
		public void setInitiated(boolean isInitiated) {
			initiated = isInitiated;
		}

		/**
		 * Get the current queue manager
		 * @return the manager as TraceMan instance
		 */
		public TraceMan getManager() {
			return manager;
		}

		/**
		 * Get the log time value
		 * @return the log time as a long
		 */
		public long getLogTime() {
			return logTime;
		}
		
    }