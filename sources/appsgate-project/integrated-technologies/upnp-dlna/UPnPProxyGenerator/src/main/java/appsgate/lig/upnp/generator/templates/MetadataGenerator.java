package appsgate.lig.upnp.generator.templates;

import java.io.IOException;
import java.io.Writer;

import appsgate.lig.upnp.generator.Device;
import appsgate.lig.upnp.generator.Service;
import appsgate.lig.upnp.generator.util.Naming;

/**
 * This class represents a APAM metadata generator
 * 
 * @author vega
 *
 */
public class MetadataGenerator  {
	
	private final Writer metadata;
	
	public MetadataGenerator(Writer metadata) throws IOException {
		this.metadata = metadata;
		
		metadata.write("<apam xmlns=\"fr.imag.adele.apam\" xmlns:ipojo=\"org.apache.felix.ipojo\" \n");
		metadata.write("	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		metadata.write("	xsi:schemaLocation=\"fr.imag.adele.apam http://raw.github.com/AdeleResearchGroup/ApAM/master/runtime/core/src/main/resources/xsd/ApamCore.xsd\">\n");
		metadata.write("\n");
		
		metadata.flush();
		
	}

	public void addDeviceProxy(Device device) throws IOException {

		metadata.write("<implementation \n");
		metadata.write("	name="+quote(device.getMapping().getClassName())+"\n");
		metadata.write("	classname="+quote(device.getMapping().getPackageName(),".",device.getMapping().getClassName())+"\n");
		metadata.write("	specification=\"CoreObjectSpec\"\n");
		metadata.write("	instantiable=\"false\"\n");
		metadata.write("	push=\"stateChanged\" >\n");
		metadata.write("\n");
		metadata.write("	<callback onInit=\"initialize\" />\n");
		metadata.write("\n");
		
		for (Service service : device.getServices()) {
			metadata.write("	<relation resolve=\"exist\" fail=\"wait\" field="+quote(Naming.getField(service.getServiceId()))+">\n");
			metadata.write("		<constraints>\n");
			metadata.write("			<instance filter=\"(UPnP.device.UDN=$.$UPnP\\.device\\.UDN)\"/>\n");
			metadata.write("		</constraints>\n");
			metadata.write("	</relation>\n");
		}

		metadata.write("\n");
		metadata.write("	<definition name=\"UPnP.device.type\" type=\"string\" />\n");
		metadata.write("	<property name=\"UPnP.device.type\" value="+quote(device.getDeviceType())+" />\n");
		metadata.write("	<definition name=\"UPnP.device.UDN\" type=\"string\"/>\n");
		metadata.write("\n");

		metadata.write("\n");
		metadata.write("</implementation>\n");
		metadata.write("\n");
		
		metadata.flush();
	}

	public void addServiceProxy(Service service) throws IOException {
		
		metadata.write("<implementation \n");
		metadata.write("	name="+quote(service.getMapping().getClassName())+"\n");
		metadata.write("	classname="+quote(service.getMapping().getPackageName(),".",service.getMapping().getClassName())+"\n");
		metadata.write("	specification=\"CoreObjectSpec\"\n");
		metadata.write("	instantiable=\"false\"\n");
		metadata.write("	push=\"stateChanged\" >\n");
		metadata.write("\n");
		metadata.write("	<callback onInit=\"initialize\" />\n");
		metadata.write("\n");
		metadata.write("\n");
		metadata.write("	<definition name=\"UPnP.service.type\" type=\"string\" />\n");
		metadata.write("	<property name=\"UPnP.service.type\" value="+quote(service.getType())+" />\n");
		metadata.write("	<definition name=\"UPnP.device.UDN\" type=\"string\"/>\n");
		metadata.write("	<definition name=\"UPnP.service.id\" type=\"string\"/>\n");
		metadata.write("\n");
		metadata.write("	<ipojo:provides specifications=\"org.osgi.service.upnp.UPnPEventListener\">\n");
		metadata.write("		<ipojo:property name=\"upnp.filter\" field=\"upnpEventFilter\" type=\"org.osgi.framework.Filter\" mandatory=\"true\"/>\n");
		metadata.write("	</ipojo:provides>\n");
		metadata.write("\n");
		metadata.write("	<ipojo:requires id=\"UPnP.device.UDN\" field=\"upnpDevice\" optional=\"false\"/>\n");
		metadata.write("</implementation>\n");
		metadata.write("\n");
		
		metadata.flush();
	}

	public void close() throws IOException {
		metadata.write("\n");
		metadata.write("</apam>\n");
		metadata.close();
	}
	
	private static String quote(String ... segments) {
		StringBuilder quoted = new StringBuilder();
		quoted.append('"');
		for (String segment : segments) {
			quoted.append(segment);
		}
		quoted.append('"');
		
		return quoted.toString();
	}
	
}
