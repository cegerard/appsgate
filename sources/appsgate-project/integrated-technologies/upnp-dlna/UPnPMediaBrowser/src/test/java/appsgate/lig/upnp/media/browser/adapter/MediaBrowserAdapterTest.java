/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appsgate.lig.upnp.media.browser.adapter;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author jr
 */
public class MediaBrowserAdapterTest {

    @Test
    public void TestParser() throws Exception {
        MediaBrowserAdapter adapter = new MediaBrowserAdapter();
        DocumentBuilder builder;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        builder = factory.newDocumentBuilder();

        String xmxml = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/' xmlns:dlna='urn:schemas-dlna-org:metadata-1-0/'><container id='musicdb://' parentID='0' restricted='1' searchable='1'><dc:title>Music Library</dc:title><dc:creator>Unknown</dc:creator><upnp:genre>Unknown</upnp:genre><upnp:class>object.container</upnp:class></container><container id='videodb://' parentID='0' restricted='1' searchable='1'><dc:title>Video Library</dc:title><dc:creator>Unknown</dc:creator><upnp:genre>Unknown</upnp:genre><upnp:class>object.container.storageFolder</upnp:class></container></DIDL-Lite>";
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmxml));

        Document document = builder.parse(is);
        String testedString = adapter.parseXml(document, "objId");
        Assert.assertNotNull(testedString);
        System.out.println(testedString);
    }

}
