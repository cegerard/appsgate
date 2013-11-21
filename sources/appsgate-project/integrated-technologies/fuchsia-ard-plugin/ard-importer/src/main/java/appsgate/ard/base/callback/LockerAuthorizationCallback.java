package appsgate.ard.base.callback;

import appsgate.ard.dao.AuthorizationRequest;
import appsgate.ard.dao.AuthorizationResponse;
import appsgate.ard.dao.AuthorizationResponseAck;

public interface LockerAuthorizationCallback {

    public AuthorizationResponse authorizationRequested(AuthorizationRequest ar);

    public void authorizationAckReceived(AuthorizationResponseAck ack);

}
