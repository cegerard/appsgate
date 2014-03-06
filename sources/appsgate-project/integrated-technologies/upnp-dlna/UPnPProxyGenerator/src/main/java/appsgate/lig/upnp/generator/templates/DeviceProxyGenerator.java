package appsgate.lig.upnp.generator.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import appsgate.lig.upnp.generator.Device;
import appsgate.lig.upnp.generator.Mapping;
import appsgate.lig.upnp.generator.Service;
import appsgate.lig.upnp.generator.UPnPGeneratorMojo;
import appsgate.lig.upnp.generator.util.XSLTransformationGenerator;

/**
 * This class represents a generator for a device driver that aggregates
 * its embedded services
 * 
 * @author vega
 *
 */
public class DeviceProxyGenerator {
	
	private final UPnPGeneratorMojo mojo;
	private final Device device;
	
	private final XSLTransformationGenerator delegateTransformation;
	
	public DeviceProxyGenerator(UPnPGeneratorMojo mojo, Device device) {
		this.mojo	= mojo;
		this.device = device;
		
		this.delegateTransformation = new XSLTransformationGenerator("scdp2deletage.xsl");
	}

	public void generate(OutputStream outputStream) throws MojoFailureException, MojoExecutionException, IOException, TransformerException {
		
		Writer output = new OutputStreamWriter(outputStream);
		
		output.write("package "+device.getMapping().getPackageName()+";\n");
		output.write("\n");
		output.write("import org.apache.felix.upnp.devicegen.holder.*;\n");
		output.write("\n");
		output.write("import org.osgi.service.upnp.UPnPDevice;\n");
		output.write("import org.osgi.service.upnp.UPnPException;\n");
		output.write("\n");
		output.write("import org.json.JSONException;\n");
		output.write("import org.json.JSONObject;\n");
		output.write("\n");
		output.write("import fr.imag.adele.apam.Instance;\n");
		output.write("\n");
		output.write("import appsgate.lig.core.object.spec.CoreObjectSpec;\n");
		output.write("import appsgate.lig.core.object.spec.CoreObjectBehavior;\n");
		output.write("import appsgate.lig.core.object.messages.NotificationMsg;\n");
		output.write("\n");
		output.flush();
		
		for (Service service : device.getServices()) {
			/*
			 * Try to load service interface mapping
			 */
			Mapping interfaceMapping = mojo.getJavaMapping(service.getType());
			if (interfaceMapping == null )
				throw new MojoFailureException("Invalid service configuration : could not find java mapping for "+service.getType());

			if (interfaceMapping.getPackageName() == null || interfaceMapping.getPackageName().isEmpty())
				throw new MojoFailureException("Invalid service configuration : package not specified for "+service.getType());

			if (interfaceMapping.getClassName() == null || interfaceMapping.getClassName().isEmpty())
				throw new MojoFailureException("Invalid service configuration : class not specified for "+service.getType());
			
			output.write("import "+interfaceMapping.getPackageName()+"."+interfaceMapping.getClassName()+";\n");
		}
		output.flush();
		
		output.write("\n");
		output.write("public class "+device.getMapping().getClassName()+" extends CoreObjectBehavior implements CoreObjectSpec {\n");
		output.write("\n");
		output.write("	private String 		deviceType;\n");
		output.write("	private String 		deviceId;\n");
		output.write("	private String 		deviceName;\n");
		output.write("\n");
		output.write("	private String 		userObjectName;\n");
		output.write("	private int 		locationId;\n");
		output.write("	private String 		pictureId;\n");
		output.write("\n");
		output.write("	@SuppressWarnings(\"unused\")\n");
		output.write("	private void initialize(Instance instance) {\n");
		output.write("		deviceType	= instance.getProperty(UPnPDevice.TYPE);\n");
		output.write("		deviceId 	= instance.getProperty(UPnPDevice.ID);\n");
		output.write("		deviceName 	= instance.getProperty(UPnPDevice.FRIENDLY_NAME);\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	private class Notification implements NotificationMsg {\n");
		output.write("		private final String variable;\n");
		output.write("		private final String value;\n");
		output.write("\n");
		output.write("		public Notification( String variable, String value) {\n");
		output.write("			this.variable 	= variable;\n");
		output.write("			this.value	 	= value;\n");
		output.write("		}\n");
		output.write("\n");
		output.write("		public CoreObjectSpec getSource() {\n");
		output.write("			return "+device.getMapping().getClassName()+".this;\n");
		output.write("		}\n");
		output.write("\n");
		output.write("		public String getNewValue() {\n");
		output.write("			return value;\n");
		output.write("		}\n");
		output.write("\n");
		output.write("		public JSONObject JSONize() throws JSONException {\n");
		output.write("			JSONObject notification = new JSONObject();\n");
		output.write("\n");
		output.write("			notification.put(\"objectId\", getAbstractObjectId());\n");
		output.write("			notification.put(\"varName\", variable);\n");
		output.write("			notification.put(\"value\", value);\n");
		output.write("			return notification;\n");
		output.write("		}\n");
		output.write("	}");
		output.write("\n");
		output.write("	@SuppressWarnings(\"unused\")\n");
		output.write("	private NotificationMsg stateChanged(String variable, Object value) {\n");
		output.write("		return new Notification(variable, value.toString());\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public String getAbstractObjectId() {\n");
		output.write("		return deviceId;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public String getUserType() {\n");
		output.write("		return Integer.toString(deviceType.hashCode());\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public int getObjectStatus() {\n");
		output.write("		return 2;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public String getUserObjectName() {\n");
		output.write("		return userObjectName;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public String getFriendlyName() {\n");
		output.write("		return deviceName;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public void setUserObjectName(String userName) {\n");
		output.write("		this.userObjectName = userName;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public int getLocationId() {\n");
		output.write("		return locationId;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	public void setLocationId(int locationId) {\n");
		output.write("		this.locationId = locationId;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public String getPictureId() {\n");
		output.write("		return pictureId;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public void setPictureId(String pictureId) {\n");
		output.write("		this.pictureId = pictureId;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public JSONObject getDescription() throws JSONException {\n");
		output.write("		JSONObject description = new JSONObject();\n");
		output.write("\n");
		output.write("		description.put(\"id\", getAbstractObjectId());\n");
		output.write("		description.put(\"physical-device\", deviceId);\n");
		output.write("		description.put(\"name\", getUserObjectName());\n");
		output.write("		description.put(\"friendlyname\", getFriendlyName());\n");
		output.write("		description.put(\"type\", getUserType());\n");
		output.write("		description.put(\"locationId\", getLocationId());\n");
		output.write("		description.put(\"status\", getObjectStatus());\n");
		output.write("\n");
		output.write("		return description;\n");
		output.write("	}\n");
		output.write("\n");
		output.write("	@Override\n");
		output.write("	public CORE_TYPE getCoreType() {\n");
		output.write("		return CORE_TYPE.SERVICE;\n");
		output.write("	}\n");
		output.write("\n");
		output.flush();
		
		for (Service service : device.getServices()) {
			/* 
			 * try to load the service control description
			 */
			
			URI serviceLocation = null;
			try {
				
				if (service.getType() == null || service.getType().isEmpty())
					throw new MojoFailureException("Invalid proxy configuration : service type not specified");
				
				serviceLocation = mojo.getServiceControlProtocolDescription(service.getType());
				if (serviceLocation == null)
					throw new MojoFailureException("Invalid proxy configuration : service description not found for "+service.getType());
				
			} catch (URISyntaxException e) {
				throw new MojoFailureException("Invalid proxy configuration : invalid service description location for "+service.getType());
			} catch (IOException cause) {
				throw new MojoFailureException("Invalid proxy configuration : error loading description for "+service.getType());
			}

			Mapping interfaceMapping = mojo.getJavaMapping(service.getType());
			
			InputStream input = serviceLocation.toURL().openStream();
			Map<String,Object> parameters = new HashMap<String,Object>();
			
			parameters.put("servicetype", service.getType());
			parameters.put("serviceId", service.getServiceId());
			parameters.put("interfacePackage",interfaceMapping.getPackageName());
			parameters.put("interfaceName",interfaceMapping.getClassName());
			
			delegateTransformation.transform(input,outputStream,parameters);

		}
		output.flush();

		output.write("}\n");
		output.flush();
	}


}
