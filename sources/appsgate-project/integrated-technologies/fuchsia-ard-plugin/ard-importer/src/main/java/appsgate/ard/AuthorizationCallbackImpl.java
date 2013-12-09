package appsgate.ard;

import appsgate.ard.aperio.AperioAccessDecision;
import appsgate.ard.base.callback.LockerAuthorizationCallback;
import appsgate.ard.dao.AuthorizationRequest;
import appsgate.ard.dao.AuthorizationResponse;
import appsgate.ard.dao.AuthorizationResponseAck;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.*;

@Component (name="DoorAuthorizationCallbackFactory")
@Provides
public class AuthorizationCallbackImpl implements LockerAuthorizationCallback {

    @Property(name = "ard.switch.authorized_cards",value = "")
    private String authorizedCardsString;

    private Set<Integer> authorizedCards=new HashSet<Integer>();

    @Requires
    EventAdmin eventAdmin;

    @Validate
    public void adaptAuthorizedDoors(){

        if(authorizedCardsString.trim().length()>0){

            StringTokenizer st=new StringTokenizer(authorizedCardsString," ");

            while(st.hasMoreTokens()){

                String val=st.nextToken();

                try{
                    Integer intval=Integer.parseInt(val);
                    authorizedCards.add(intval);
                    System.out.println("Adding "+intval+" into the list of authorized cards");
                }catch(NumberFormatException e){
                    System.err.println("Not possible to parse value "+val);
                }

            }

        }

    }

    public AuthorizationResponse authorizationRequested(AuthorizationRequest ar) {

        AuthorizationResponse response;
        Event eventAdminMessage;

        if(authorizedCards.contains(ar.getCardIntCode())){
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
