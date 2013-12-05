package appsgate.lig.upnp.media.browser.adapter;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.felix.upnp.devicegen.holder.LongHolder;
import org.apache.felix.upnp.devicegen.holder.StringHolder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
import appsgate.lig.upnp.media.proxy.MediaServerProxyImpl;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

public class MediaBrowserAdapter implements MediaBrowser, CoreObjectSpec {

    private MediaServerProxyImpl mediaServer;

    /**
     * The associated UPnP device
     */
    private String deviceId;

    /**
     * Core Object Spec properties
     */
    private String appsgatePictureId;
    private String appsgateUserType;
    private String appsgateStatus;
    private String appsgateServiceName;
    private Set<Instance> proxies;

    private DocumentBuilderFactory factory;

    @SuppressWarnings("unused")
    private void initialize(Instance instance) {
        Implementation implementation = CST.apamResolver.findImplByName(null, "MediaBrowser");

        deviceId = instance.getProperty(UPnPDevice.ID);
        proxies = implementation.getInsts();

        Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.INFO, "proxies instanciated: {}", proxies);

        appsgatePictureId = null;
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
		descr.put("friendlyName", mediaServer.getFriendlyName());

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
     * @see appsgate.lig.core.object.spec.CoreObjectSpec#getPictureId()
     */
    @Override
    public String getPictureId() {
        return appsgatePictureId;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * appsgate.lig.core.object.spec.CoreObjectSpec#setPictureId(java.lang.String
     * )
     */
    @Override
    public void setPictureId(String pictureId) {
        this.appsgatePictureId = pictureId;
    }

    @Override
    public String browse(String objectID, String browseFlag, String filter,
            long startingIndex, long requestedCount, String sortCriteria) {
        DocumentBuilder builder;
        try {
            //Get the DOM Builder
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return "<empty/>";
        }

        try {
            String ret = "";
            StringHolder result = new StringHolder();
            LongHolder number = new LongHolder();
            LongHolder totalMatches = new LongHolder();
            LongHolder updateId = new LongHolder();

            mediaServer.getContentDirectory().browse(objectID, browseFlag, filter, startingIndex, requestedCount, sortCriteria,
                    result, number, totalMatches, updateId);
            return result.getObject();
//
//            try {
//                InputSource is = new InputSource();
//                is.setCharacterStream(new StringReader(result.getObject()));
//
//                Document document = builder.parse(is);
//
//                ret += parseXml(document, objectID);
//
//            } catch (SAXException ex) {
//                Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
//                return "<XMLError/>";
//            } catch (IOException ex) {
//                Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
//                return "<IOError/>";
//            }

          //  return ret;

        } catch (UPnPException ignored) {
            System.err.print("UPNP Exception");
            ignored.printStackTrace(System.err);
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
        return "toto";
    }

}
