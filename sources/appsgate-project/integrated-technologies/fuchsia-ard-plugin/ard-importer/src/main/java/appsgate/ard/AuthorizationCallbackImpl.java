package appsgate.ard;

import appsgate.ard.aperio.AperioAccessDecision;
import appsgate.ard.base.callback.LockerAuthorizationCallback;
import appsgate.ard.dao.AuthorizationRequest;
import appsgate.ard.dao.AuthorizationResponse;
import appsgate.ard.dao.AuthorizationResponseAck;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
@Instantiate
@Provides
public class AuthorizationCallbackImpl implements LockerAuthorizationCallback {

    @Requires
    EventAdmin eventAdmin;

    public AuthorizationResponse authorizationRequested(AuthorizationRequest ar) {

        AuthorizationResponse response;
        Event eventAdminMessage;

        if(ar.getCardIntCode()==410146306){
            response=new AuthorizationResponse(AperioAccessDecision.GRANTED,ar);
        }else {
            response=new AuthorizationResponse(AperioAccessDecision.NOT_GRANTED,ar);
        }

        String template="fuchsia/ard/locker/%s/authorization_request";
        String eventTopic=String.format(template,ar.getDoorId());
        Dictionary eventProperties=new Hashtable();
        eventProperties.put("timestamp", Calendar.getInstance().getTime());
        eventProperties.put("card-byte", ar.getCard());
        eventProperties.put("card-int", ar.getCardIntCode());
        eventProperties.put("authorization_result", response.getDecision());

        eventAdminMessage = new Event(eventTopic,eventProperties);
        eventAdmin.sendEvent(eventAdminMessage);

        return response;

    }

    public void authorizationAckReceived(AuthorizationResponseAck ack) {
        //Ack is not used for now
    }

}
