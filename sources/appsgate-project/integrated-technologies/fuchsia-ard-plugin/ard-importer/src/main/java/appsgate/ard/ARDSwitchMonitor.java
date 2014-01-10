package appsgate.ard;

import appsgate.ard.aperio.AperioAccessDecision;
import appsgate.ard.base.AuthorizationCallbackComparator;
import appsgate.ard.base.callback.DoorMonitorExceptionHandler;
import appsgate.ard.base.callback.LockerAuthorizationCallback;
import appsgate.ard.base.iface.Switch;
import appsgate.ard.dao.AuthorizationRequest;
import appsgate.ard.dao.AuthorizationResponse;
import appsgate.ard.dao.AuthorizationResponseAck;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

@Component(name="SwitchMonitorFactory")
@Provides
public class ARDSwitchMonitor extends AbstractImporterComponent implements Switch, Runnable {

    @ServiceProperty(name = "target")
    private String filter;

    @ServiceProperty(name = "ard.switch.ip")
    private String address;

    @Property
    private Integer port;

    @ServiceProperty(name = "ard.switch.authorized_cards")
    private String authorizedCardsString;

    @Requires(filter = "(factory.name=DoorAuthorizationCallbackMockFactory)")
    private Factory doorAuthorizationCallbackMockFactory;

    //LockerAuthorizationCallback services with higher priority will take replace lower priorities ones(high priority = high int values)
    @Requires(policy = BindingPolicy.DYNAMIC_PRIORITY,comparator = AuthorizationCallbackComparator.class, optional = true)
    private LockerAuthorizationCallback authorizationCallback;

    /**
     * This is the authorizationCallback instance that exists always, but will be replaced by any other instance (with the proper interface )
     * with higher priority
     */
    InstanceManager authorizationCallbackMockInstance;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean activated=true;
    private InputStream input;
    private OutputStream output;
    private Socket connection;
    private Set<String> monitoredDoors=new HashSet<String>();

    private BundleContext context;

    public ARDSwitchMonitor(BundleContext context){
        this.context=context;
    }

    public ARDSwitchMonitor(String address, int port){
        this.address=address;
        this.port=port;
    }

    @Validate
    private void generateAuthorizationCallback() {

        Dictionary<String,Object> authorizedCards=new Hashtable<String,Object>() ;

        authorizedCards.put("ard.switch.authorized_cards", authorizedCardsString);

        try {
            authorizationCallbackMockInstance = (InstanceManager)doorAuthorizationCallbackMockFactory.createComponentInstance(authorizedCards);
        } catch (Exception e) {
            logger.error("failed to create authorization callback mock object");
        }

    }

    @Validate
    private void registerMockAuthenticationReceiver() {

        String[] topics = new String[] {
                "fuchsia/ard/mock/authorization_requested"
        };

        Dictionary props = new Hashtable();

        props.put(EventConstants.EVENT_TOPIC, topics);

        context.registerService(EventHandler.class.getName(), new EventHandler(){{}

            public void handleEvent(Event event) {

                System.out.println(String.format("ARD importer: message received on the topic %s with parameters %s",event.getTopic(),event.getPropertyNames().toString()));

                Integer cardId=new Integer(event.getProperty("card-int").toString());
                Byte doorId=new Byte(event.getProperty("door").toString());

                if(monitoredDoors.contains(doorId.toString())){

                    AuthorizationRequest ar=AuthorizationRequest.fromData(doorId,cardId);

                    AuthorizationResponse arr= authorizationCallback.authorizationRequested(ar);

                } else {

                    System.out.println("Authorization request ignored, door is not monitored");

                }

            }
        } , props);

    }

    public void stopMonitor(){
        this.activated=false;
        try {
            input.close();
            output.close();
            connection.close();
            if(authorizationCallbackMockInstance!=null){
                authorizationCallbackMockInstance.dispose();
            }
        } catch (Exception e) {
            System.out.println("failed to force disconnection");
        }
    }

    public void run()  {

        try {

            System.out.println(String.format("Starting monitoring locker in address %s:%s ",address,port));

            connection = new Socket(address, port);
            input = connection.getInputStream();
            output = connection.getOutputStream();

            while(activated){

                System.out.println("waiting..");

                byte[] dataStream=new byte[17];
                input.read(dataStream);

                AuthorizationRequest ar=AuthorizationRequest.fromStream(dataStream);

                if(!monitoredDoors.contains(new Byte(ar.getDoorId()).toString())){
                    System.out.println("door with id "+ar.getDoorId()+" is not monitored");
                    continue;
                }

                System.out.println("request received. "+ar);

                AuthorizationResponse arr=null;

                //LockerAuthorizationCallback callback=doorCallback.get(new Byte(ar.getDoorId()).toString());

                if (authorizationCallback!=null){
                    System.out.println(String.format("Using %s as authorization callback",authorizationCallback.toString()));
                    arr= authorizationCallback.authorizationRequested(ar);
                } else {
                    System.out.println("handler is null, allowing everyone comming in");
                    arr=new AuthorizationResponse(AperioAccessDecision.GRANTED,ar);
                }

                output.write(arr.toStream());
                output.flush();

                System.out.println("response sent.");

                byte[] ackDataStream=new byte[6];
                input.read(ackDataStream);

                AuthorizationResponseAck ack=AuthorizationResponseAck.fromStream(ackDataStream);

                if (authorizationCallback !=null) authorizationCallback.authorizationAckReceived(ack);

                System.out.println("ack received.");

            }

            System.out.println(String.format("Stopping monitoring locker in address %s:%s ",address,port));

        } catch (Exception e) {

            stopMonitor();

        }

    }

    /**
     * This method received the importDeclaration of a Door, that indicates the id, ard.door.id, ard.door.switch.ip
     * @param importDeclaration
     * @throws ImporterException
     */
    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        String doorId=importDeclaration.getMetadata().get("ard.door.id").toString();

        monitoredDoors.add(doorId);

        System.out.println("monitoring door:"+doorId);
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {

        String doorId=importDeclaration.getMetadata().get("ard.door.id").toString();

        monitoredDoors.remove(doorId);

        System.out.println("stop monitoring door:"+doorId);
    }

    public List<String> getConfigPrefix() {
        return null;
    }

    public String getName() {
        return String.format("%s:%s-switchmonitor",address,port);
    }

}
