import appsgate.ard.protocol.controller.ARDController;
import appsgate.ard.protocol.model.ARDFutureResponse;
import appsgate.ard.protocol.model.command.ARDRequest;
import appsgate.ard.protocol.model.command.request.GetTimeRequest;
import appsgate.ard.protocol.model.command.request.GetZoneRequest;
import appsgate.ard.protocol.model.command.request.SubscriptionRequest;
import org.json.JSONException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Ignore
public class ARDSuiteTest {

    private Logger logger = LoggerFactory.getLogger(ARDSuiteTest.class);

    ARDController ard;

    @Before
    public void before() throws IOException, JSONException {
        ard=new ARDController("192.168.3.110", 2001);

        //Open the socket with ard box
        ard.connect();

        //connect to listen the binary events from ardbox
        ard.monitoring();

    }

    @After
    public void after() throws IOException {
        ard.disconnect();
    }

    @Test
    public void subscriptionTest() throws JSONException, InterruptedException {
        //subscribe for JSON events (if your are not subscribed you receive only binary messages)
        ARDRequest request=new SubscriptionRequest();
        ARDFutureResponse response=ard.sendSyncRequest(request);
        logger.info("received {}",response.getResponse());
        Assert.assertTrue(response.getResponse() != null);
        Assert.assertTrue(response.getResponse().getInt("request_id")==request.getRequestId());
    }

    @Test
    public void getTimeTest() throws JSONException, InterruptedException {
        subscriptionTest();
        ARDRequest request=new GetTimeRequest();
        ARDFutureResponse response=ard.sendSyncRequest(request);
        logger.info("received {}",response.getResponse());
        Assert.assertTrue(response.getResponse() != null);
        Assert.assertTrue(response.getResponse().getInt("request_id")==request.getRequestId());
    }

    @Test
    public void multipleTimeRequest() throws JSONException, InterruptedException {
        getTimeTest();
        getTimeTest();
        getTimeTest();
    }

    @Test
    public void unknownMethodCall() throws JSONException, InterruptedException {
        subscriptionTest();
        ARDRequest request=new ARDRequest(0,"noMethod"){};
        ARDFutureResponse response=ard.sendSyncRequest(request);
        Assert.assertTrue(response.getResponse()!=null);
        Assert.assertTrue(response.getResponse().getInt("request_id")==request.getRequestId());
    }

    @Test
    public void getZoneTest() throws JSONException, InterruptedException {
        subscriptionTest();
        ARDRequest request=new GetZoneRequest(1);
        ARDFutureResponse response=ard.sendSyncRequest(request);

        Assert.assertTrue(response.getResponse()!=null);
        System.out.println("Response:"+response.getResponse().toString());
        Assert.assertTrue(response.getResponse().getInt("request_id")==request.getRequestId());

    }


}
