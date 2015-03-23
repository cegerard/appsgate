package appsgate.lig.upnp.media.browser.adapter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.upnp.adapter.event.UPnPEvent;
import appsgate.lig.upnp.media.AVTransport;
import appsgate.lig.upnp.media.ConnectionManager;
import appsgate.lig.upnp.media.ContentDirectory;
import org.apache.felix.upnp.devicegen.holder.LongHolder;
import org.apache.felix.upnp.devicegen.holder.StringHolder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Set;

public class MediaBrowserAdapter extends CoreObjectBehavior implements MediaBrowser, CoreObjectSpec {

    private final static Logger logger = LoggerFactory.getLogger(MediaBrowserAdapter.class);

    /**
     * The associated UPnP device
     */
    private String deviceId;
    private String deviceName;

    /**
     * Core Object Spec properties
     */
    private String appsgateUserType;
    private String appsgateStatus;
    private String appsgateServiceName;
    private Set<Instance> proxies;

    // Unused services
//    private AVTransport aVTransport;
//    private ConnectionManager connectionManager;
    private ContentDirectory contentDirectory;



    private DocumentBuilderFactory factory;

    @SuppressWarnings("unused")
    private void initialize(Instance instance) {
        Implementation implementation = CST.apamResolver.findImplByName(null, "MediaBrowser");

        deviceId 	= instance.getProperty(UPnPDevice.ID);


        deviceId = instance.getProperty(UPnPDevice.ID);
        proxies = implementation.getInsts();

        logger.info("proxies instanciated: "+proxies);

        appsgateServiceName = "Appsgate UPnP Media browser";
        appsgateUserType = "36";
        appsgateStatus = "2";

        factory = DocumentBuilderFactory.newInstance();

    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getAbstractObjectId()
     */
    @Override
    public String getAbstractObjectId() {
        return "browser:" + deviceId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getDescription()
     */
    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();
        // mandatory appsgate properties
        descr.put("id", getAbstractObjectId());
        descr.put("type", getUserType());
        descr.put("status", appsgateStatus);
        descr.put("sysName", appsgateServiceName);
		descr.put("friendlyName", deviceName);

        return descr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getObjectStatus()
     */
    @Override
    public int getObjectStatus() {
        return Integer.parseInt(appsgateStatus);
    }


    /*
     * (non-Javadoc)
     * 
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getUserType()
     */
    @Override
    public String getUserType() {
        return appsgateUserType;
    }


    @Override
    public String browse(String objectID, String browseFlag, String filter,
            long startingIndex, long requestedCount, String sortCriteria) {
        DocumentBuilder builder;
        try {
            //Get the DOM Builder
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException "+ex.getMessage());
            return "<empty/>";
        }
        
        if(contentDirectory == null) {
        	logger.error("No Content Directory service bound");
        	return "<empty/>";
        }

        try {
            String ret = "";
            StringHolder result = new StringHolder();
            LongHolder number = new LongHolder();
            LongHolder totalMatches = new LongHolder();
            LongHolder updateId = new LongHolder();

            contentDirectory.browse(objectID, browseFlag, filter, startingIndex, requestedCount, sortCriteria,
                    result, number, totalMatches, updateId);
            return result.getObject();

        } catch (UPnPException ignored) {
            logger.error("UPNP Exception  "+ignored.getMessage()+" UPnP code : "+ignored.getUPnPError_Code());
            return "<empty/>";
        }
    }

    /**
     * Method that parse a document and print it to jsTree format
     *
     * @param document
     * @param objectID
     * @return
     */
    protected String parseXml(Document document, String objectID) {
        String ret = "";
        NodeList childNodes = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                String id = node.getAttributes().getNamedItem("id").getNodeValue();
                String name = getDirectoryName(node);
                ret += "<item id='" + id + "' parent_id='" + objectID + "'>" + "<content><name>" + name + "</name></content></item>\n";
            }
        }

        return ret;
    }

    private String getDirectoryName(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equalsIgnoreCase("dc:title")) {
                return children.item(i).getTextContent();
            }
        }
        return "";
    }

    private void onUpnPEvent( UPnPEvent event) {
        logger.debug("onUpnPEvent( UPnPEvent event)" +
                ", deviceId = "+event.getDeviceId() +
                ", serviceId = "+event.getServiceId() +
                ", events = "+event.getEvents());

        if(event != null) {
            Dictionary events = event.getEvents();
            Enumeration<String> variables = events.keys();
            while( variables.hasMoreElements()) {

                String variable = variables.nextElement();
                Object value = events.get(variable);

                stateChanged(variable, "", value.toString());
            }
        } else {
            logger.debug("No events to send");
        }

    }

    private NotificationMsg stateChanged(String varName, String oldValue, String newValue) {
        return new CoreNotificationMsg(varName, oldValue, newValue, this.getAbstractObjectId());
    }

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.DEVICE;
	}


}
