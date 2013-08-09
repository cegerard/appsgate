package appsgate.lig.upnp.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import appsgate.lig.upnp.generator.templates.DeviceProxyGenerator;
import appsgate.lig.upnp.generator.util.XSLTransformationGenerator;

/**
 * This plugin generates a java proxy from a Service Control Protocol Description.
 * 
 * The proxy is based on the OSGi UPnP base driver specification 
 * 
 * @goal 	generate-proxies
 * @phase  	generate-sources
 *  
 * @author vega
 *
 */
public class ServiceProxyGeneratorMojo extends UPnPGeneratorMojo {

	/**
	 * Reference to the source generation output directory
	 * 
	 * @parameter alias="output" expression="${project.build.directory}/generated-sources/upnp/"
	 * @required
	 */
	protected File sourceOutputDirectory;

	
	/**
	 * Reference to the list of devices to generate
	 * 
	 * @parameter
	 * @required
	 */
	protected List<Device> devices;

	/**
	 * The xlst transformation for service proxy generation
	 */
	private final XSLTransformationGenerator serviceProxyGenerator;
	
	public ServiceProxyGeneratorMojo() {
		serviceProxyGenerator = new XSLTransformationGenerator("scdp2proxyimpl.xsl");
	}

	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if (! sourceOutputDirectory.exists())
			sourceOutputDirectory.mkdirs();
		
		project.addCompileSourceRoot(sourceOutputDirectory.getAbsolutePath());

		List<File> generatedServiceProxies = new ArrayList<File>();
		
		for (Device device : devices) {
			
			if (device.getServices() == null)
				continue;

			/*
			 * Generate device proxy if requested
			 */
			if (device.getMapping() != null) {
				
				if (device.getMapping().getPackageName() == null || device.getMapping().getPackageName().isEmpty())
					throw new MojoFailureException("Invalid device proxy configuration : package name not specified");
				
				if (device.getMapping().getClassName() == null || device.getMapping().getClassName().isEmpty())
					throw new MojoFailureException("Invalid device proxy configuration : class name not specified");

				File outputPackageDirectory = new File(sourceOutputDirectory,device.getMapping().getPackageName().replace('.',File.separatorChar));
				if (! outputPackageDirectory.exists())
					outputPackageDirectory.mkdirs();


				File deviceProxy = new File(outputPackageDirectory, device.getMapping().getClassName()+".java");
				
				try {
				
					OutputStream output 			= new FileOutputStream(deviceProxy,false);
					DeviceProxyGenerator generator	= new DeviceProxyGenerator(this,device); 

					generator.generate(output);
					output.close();
					
				} catch (IOException cause) {
					throw new MojoExecutionException("Internal error writing device proxy ",cause);
				} catch (TransformerException cause) {
					throw new MojoExecutionException("Internal error writing device proxy ",cause);
				}
				
			}
			
			for (Service service : device.getServices()) {

				/*
				 * Generate service proxy if a mapping is specified
				 */
				if (service.getMapping() == null)
					continue;

				/* 
				 * try to load the service control description
				 */
				
				URI serviceLocation = null;
				try {
					
					if (service.getType() == null || service.getType().isEmpty())
						throw new MojoFailureException("Invalid proxy configuration : service type not specified");
					
					serviceLocation = getServiceControlProtocolDescription(service.getType());
					if (serviceLocation == null)
						throw new MojoFailureException("Invalid proxy configuration : service description not found for "+service.getType());
					
				} catch (URISyntaxException e) {
					throw new MojoFailureException("Invalid proxy configuration : invalid service description location for "+service.getType());
				} catch (IOException cause) {
					throw new MojoFailureException("Invalid proxy configuration : error loading description for "+service.getType());
				}
				
				
				/*
				 * Try to load service interface mapping
				 */
				Mapping interfaceMapping = getJavaMapping(service.getType());
				if (interfaceMapping == null )
					throw new MojoFailureException("Invalid proxy configuration : could not find java mapping for "+service.getType());

				if (interfaceMapping.getPackageName() == null || interfaceMapping.getPackageName().isEmpty())
					throw new MojoFailureException("Invalid proxy configuration : invalid java mapping, package not specified for "+service.getType());

				if (interfaceMapping.getClassName() == null || interfaceMapping.getClassName().isEmpty())
					throw new MojoFailureException("Invalid proxy configuration : invalid java mapping, class not specified for "+service.getType());
				
				
				/*
				 * Try to load output class file
				 */
				
				if (service.getMapping().getPackageName() == null || service.getMapping().getPackageName().isEmpty())
					throw new MojoFailureException("Invalid proxy configuration : package name not specified");
				
				if (service.getMapping().getClassName() == null || service.getMapping().getClassName().isEmpty())
					throw new MojoFailureException("Invalid proxy configuration : class name not specified");

				File outputPackageDirectory = new File(sourceOutputDirectory,service.getMapping().getPackageName().replace('.',File.separatorChar));
				if (! outputPackageDirectory.exists())
					outputPackageDirectory.mkdirs();


				File outputFile = new File(outputPackageDirectory, service.getMapping().getClassName()+".java");
				
				/*
				 * Avoid regenerating proxies
				 */
				if (generatedServiceProxies.contains(outputFile))
					continue;
				
				/*
				 * try to load and execute transformation 
				 */
				try {
				
					InputStream input				= serviceLocation.toURL().openStream();
					OutputStream output				= new FileOutputStream(outputFile,false);
					Map<String,Object> parameters 	= new HashMap<String,Object>();
					
					parameters.put("servicetype", service.getType());
					parameters.put("package", service.getMapping().getPackageName());
					parameters.put("classname",service.getMapping().getClassName());
					parameters.put("interfacePackage",interfaceMapping.getPackageName());
					parameters.put("interfaceName",interfaceMapping.getClassName());

					serviceProxyGenerator.transform(input, output, parameters);
					generatedServiceProxies.add(outputFile);
					
				
				} catch (TransformerConfigurationException cause) {
					throw new MojoExecutionException("Internal error loading transformation, please signal a bug",cause);
				} catch (TransformerException cause) {
					throw new MojoExecutionException("Internal error loading transformation, please signal a bug",cause);
				} catch (IOException cause) {
					throw new MojoExecutionException("Internal executing transformation, please signal a bug",cause);
				}

			}
			
		}
		

	}
	
}
