package appsgate.lig.upnp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This plugin generates the APAM metadata corresponding to the 
 * generated proxies
 * 
 * @goal 	generate-metadata
 * @phase  	generate-resources
 *  
 * @author vega
 *
 */
public class ServiceMetadataGeneratorMojo extends UPnPGeneratorMojo {


	/**
	 * Reference to the source generation output directory
	 * 
	 * @parameter alias="output-metadata" expression="${project.build.directory}/generated-resources/upnp/"
	 * @required
	 * @readonly
	 */
	protected File resourceOutputDirectory;
	
	/**
	 * Reference to the list of devices to generate
	 * 
	 * @parameter
	 * @required
	 */
	protected List<Device> devices;

	/**
	 * The metadata generator
	 * 
	 */
	private MetadataGenerator generator;
	
	public ServiceMetadataGeneratorMojo() {
	}
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if (! resourceOutputDirectory.exists())
			resourceOutputDirectory.mkdirs();
		
		projectHelper.addResource(project, makeRelative(resourceOutputDirectory).getPath(),Collections.singletonList("**/*"), null);
		
		File metadataFile	= new File(resourceOutputDirectory,"metadata.xml");
		if (metadataFile.exists())
			metadataFile.delete();
		
		
		try {
			Writer metadata = new FileWriter(metadataFile,false);
			generator = new MetadataGenerator(metadata);
			
			List<Service> processedServices = new ArrayList<Service>();
			
			for (Device device : devices) {
				
				if (device.getServices() == null)
					continue;
				
				if (device.getMapping() != null)
					generator.addDeviceProxy(device);
				
				for (Service service : device.getServices()) {
					if (service.getMapping() == null)
						continue;

					if (processedServices.contains(service))
						continue;

					generator.addServiceProxy(service);
					processedServices.add(service);
				}
				
				if (device.getMapping() != null) {
				}
				
			}
			
			generator.close();
		} catch (IOException cause) {
			throw new MojoExecutionException("Internal error writing metadata ",cause);
		}

		

	}
	
}
