package appsgate.lig.upnp.media.browser.adapter;

import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.upnp.media.browser.MediaBrowser;
import appsgate.lig.upnp.media.proxy.MediaServerProxyImpl;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.apache.felix.upnp.devicegen.holder.LongHolder;
import org.apache.felix.upnp.devicegen.holder.StringHolder;

import org.json.JSONException;
import org.json.JSONObject;

import fr.imag.adele.apam.Instance;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    private DocumentBuilder builder;

    @SuppressWarnings("unused")
    private void initialize(Instance instance) {
        Implementation implementation = CST.apamResolver.findImplByName(null, "MediaBrowser");

        deviceId = instance.getProperty(UPnPDevice.ID);
        proxies = implementation.getInsts();

        appsgatePictureId = null;
        appsgateServiceName = "Appsgate UPnP Media browser";
        appsgateUserType = "36";
        appsgateStatus = "2";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            //Get the DOM Builder
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        try {
            StringHolder result = new StringHolder();
            LongHolder number = new LongHolder();
            LongHolder totalMatches = new LongHolder();
            LongHolder updateId = new LongHolder();

            mediaServer.getContentDirectory().browse(objectID, browseFlag, filter, startingIndex, requestedCount, sortCriteria,
                    result, number, totalMatches, updateId);

            return result.getObject();

        } catch (UPnPException ignored) {
            ignored.printStackTrace(System.err);
            return "";
        }
    }

    @Override
    public String list() {

        String ret = "<medias>";
        for (Instance inst : this.proxies) {
            MediaServerProxyImpl proxy = (MediaServerProxyImpl) inst;
            ret += "<server name='" + proxy.getUserObjectName() + "'>\n";
            ret += this.recList(proxy, "0");
            ret += "</server>\n";
        }
        return ret + "</medias>";

    }

    private String recList(MediaServerProxyImpl proxy, String objectID) {
        StringHolder result = new StringHolder();
        LongHolder number = new LongHolder();
        LongHolder totalMatches = new LongHolder();
        LongHolder updateId = new LongHolder();
        String ret = "";
        try {
            proxy.getContentDirectory().browse(objectID, MediaBrowser.BROWSE_CHILDREN, "*", 0, 0, "",
                    result, number, totalMatches, updateId);
            Document document = builder.parse(result.getObject());
            NodeList childNodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    if (node.getLocalName().equalsIgnoreCase("container")) {
                        String id = node.getAttributes().getNamedItem("id").getNodeValue();
                        ret +=  "<container id='" + id + "'>" + recList(proxy, id) + "</container>\n";
                    } else {
                        ret+= "<media id='" + node.getLocalName() + "/>\n";
                    }
                }
            }
        } catch (UPnPException ex) {
            Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return "<upnpError/>";
        } catch (SAXException ex) {
            Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return "<XMLError/>";
        } catch (IOException ex) {
            Logger.getLogger(MediaBrowserAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return "<IOError/>";
        }

        return ret;
    }

}
