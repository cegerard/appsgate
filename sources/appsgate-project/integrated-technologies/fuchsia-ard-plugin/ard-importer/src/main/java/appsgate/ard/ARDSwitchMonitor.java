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
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

@Component(name="SwitchMonitorFactory")
@Provides
public class ARDSwitchMonitor extends AbstractImporterComponent implements Switch, Runnable{

    @ServiceProperty(name = "target")
    private String filter;

    @ServiceProperty(name = "ard.switch.ip")
    private String address;

    @ServiceProperty(name = "ard.switch.authorized_cards")
    private String authorizedCardsString;

    @Requires(filter = "(factory.name=DoorAuthorizationCallbackMockFactory)")
    Factory doorAuthorizationCallbackMockFactory;

    //LockerAuthorizationCallback services with higher priority will take place (high priority = high int values)
    @Requires(policy = BindingPolicy.DYNAMIC_PRIORITY,comparator = AuthorizationCallbackComparator.class, optional = true)
    LockerAuthorizationCallback authorizationCallback;

    @Property
    private Integer port;

    private boolean activated=true;
    private InputStream input;
    private OutputStream output;
    private Socket connection;
    private DoorMonitorExceptionHandler exceptionHandler;

    private Set<String> doorCallback=new HashSet<String>();
    private Set<String> monitoredDoors=new HashSet<String>();

    public ARDSwitchMonitor(){}

    public ARDSwitchMonitor(String address, int port){
        this.address=address;
        this.port=port;
    }


    @Validate
    private void generateAuthorizationCallback() {

        Dictionary<String,Object> authorizedCards=new Hashtable<String,Object>() ;

        authorizedCards.put("ard.switch.authorized_cards", authorizedCardsString);

        InstanceManager im= null;
        try {
            im = (InstanceManager)doorAuthorizationCallbackMockFactory.createComponentInstance(authorizedCards);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopMonitor(){
        this.activated=false;
        try {
            input.close();
            output.close();
            connection.close();
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
            exceptionHandler.handleException(e,this);
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

    private Set<Integer> adaptAuthorizedDoors(String cards){

        Set<Integer> authorizedCards=new HashSet<Integer>();

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

        return authorizedCards;

    }

}
