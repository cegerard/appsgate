package appsgate.lig.upnp.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import appsgate.lig.upnp.generator.util.XSLTransformationGenerator;

/**
 * This plugin generates a java interface from a Service Control Protocol Description
 * 
 * @goal 	generate-service-mapping
 * @phase  	generate-sources
 *  
 * @author vega
 *
 */
public class ServiceMappingGeneratorMojo extends UPnPGeneratorMojo {

	/**
	 * Reference to the source generation output directory
	 * 
	 * @parameter alias="output" expression="${project.build.directory}/generated-sources/upnp/"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * The xlst transformation
	 */
	private final XSLTransformationGenerator generator;
	
	public ServiceMappingGeneratorMojo() {
		generator = new XSLTransformationGenerator("scdp2modelitf.xsl");
	}
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if (! outputDirectory.exists())
			outputDirectory.mkdirs();
		
		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

		for (Service service : services) {
			
			/* 
			 * try to load the service control description
			 */
			
			URI serviceLocation = null;
			try {
				
				if (service.getDescription() == null || service.getDescription().isEmpty())
					throw new MojoFailureException("Invalid mapping : service description not specified");
				
				serviceLocation = getServiceControlProtocolDescription(service.getType());
				if (serviceLocation == null)
					throw new MojoFailureException("Invalid mapping : service description not found at "+service.getDescription());
				
			} catch (URISyntaxException e) {
				throw new MojoFailureException("Invalid mapping : invalid service description location "+service.getDescription());
			} catch (IOException e) {
				throw new MojoFailureException("Invalid mapping : unable to load service description at "+service.getDescription());
			}
			
			/*
			 * Try to load output class file
			 */
			
			if (service.getMapping().getPackageName() == null || service.getMapping().getPackageName().isEmpty())
				throw new MojoFailureException("Invalid mapping : package name not specified");

			if (service.getMapping().getClassName() == null || service.getMapping().getClassName().isEmpty())
				throw new MojoFailureException("Invalid mapping : class name not specified");

			File outputPackageDirectory = new File(outputDirectory,service.getMapping().getPackageName().replace('.',File.separatorChar));
			if (! outputPackageDirectory.exists())
				outputPackageDirectory.mkdirs();


			File outputFile = new File(outputPackageDirectory, service.getMapping().getClassName()+".java");
			if (outputFile.exists())
				outputFile.delete();
			
			/*
			 * try to load and execute transformation 
			 */
			try {
			
				InputStream input 				= serviceLocation.toURL().openStream();
				OutputStream output				= new FileOutputStream(outputFile);
				Map<String,Object> parameters	= new HashMap<String,Object>();

				parameters.put("servicetype", service.getType());
				parameters.put("package", service.getMapping().getPackageName());
				parameters.put("classname",service.getMapping().getClassName());
				
				generator.transform(input, output, parameters);
				output.close();
			
			} catch (IOException cause) {
				throw new MojoExecutionException("Internal error executing transformation, please signal a bug",cause);
			} catch (TransformerException cause) {
				throw new MojoExecutionException("Internal error executing transformation, please signal a bug",cause);
			}

		}
	}
}
