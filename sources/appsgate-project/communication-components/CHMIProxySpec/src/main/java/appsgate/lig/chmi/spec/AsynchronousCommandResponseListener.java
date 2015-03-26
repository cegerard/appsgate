package appsgate.lig.chmi.spec;

public interface AsynchronousCommandResponseListener {
	
	public void notifyResponse(String objectId, String value, String callId, int clientId);
}
