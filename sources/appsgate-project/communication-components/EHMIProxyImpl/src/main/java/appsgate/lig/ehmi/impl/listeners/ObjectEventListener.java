package appsgate.lig.ehmi.impl.listeners;

import appsgate.lig.chmi.spec.listeners.CoreEventsListener;
import appsgate.lig.ehmi.impl.EHMIProxyImpl;

public class ObjectEventListener implements CoreEventsListener {
	
	private String sourceId = "";
	private String varName = "";
	private String value ="";
	
	private EHMIProxyImpl EHMIProxy;

	public ObjectEventListener(EHMIProxyImpl eHMIProxy) {
		super();
		EHMIProxy = eHMIProxy;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	@Override
	public String varName() {
		return varName;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void notifyEvent(String srcId, String varName, String value) {
		this.sourceId = srcId;
		this.varName = varName;
		this.value = value;
		
		//TODO notify context for event
		//TODO 000003 add context proxy logic here
		
	}

}
