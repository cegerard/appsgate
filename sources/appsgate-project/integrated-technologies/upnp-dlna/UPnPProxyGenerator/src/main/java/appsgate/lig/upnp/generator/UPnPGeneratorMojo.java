package appsgate.lig.upnp.generator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * The base class for all UPnP plugin generators
 * 
 * @author vega
 *
 */
public abstract class UPnPGeneratorMojo extends AbstractMojo {

	/**
	 * Reference to the project to build
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	protected MavenProject project;

	/**
	 *  Helper class to assist in attaching artifacts to the project instance.
	 *  
	 *  @component
	 *  @required
	 *  @readonly 
	 */ 
	protected MavenProjectHelper projectHelper;

	/**
	 * The list of resource definitions to be included in the project jar.
	 * 
	 * List of Resource objects for the current build, containing directory,
	 * includes, and excludes.
	 * 
	 * @parameter default-value="${project.resources}" 
	 * @required
	 * @readonly 
	 * 
	 */
	private List<Resource> resources;

	/**
	 * The list of service descriptions
	 * 
	 * @parameter
	 * @required
	 */
	protected List<Service> services;

	
	/**
	 * Get the relative path of file with respect to the base directory.
	 * The specified file must have an absolute path inside the project.
	 */
	protected File makeRelative(File file) {
		return new File(project.getBasedir().toURI().relativize(file.toURI()).getPath());
	}
	
	private final static Map<String,URI> scpdLocations = new HashMap<String,URI>();
	
	/**
	 * Get the SCDP file description associated with a given service type
	 * 
	 */
	public URI getServiceControlProtocolDescription(String serviceType) throws URISyntaxException, IOException {
		
		/*
		 * Check for cached value
		 */
		synchronized (scpdLocations) {
			URI location = scpdLocations.get(serviceType);
			if (location != null)
				return location;
			
		}

		/*
		 * Look for configured services
		 */
		
		URI location = null;
		for (Service service : services) {
			if (service.getType().equals(serviceType)) {
				location = new URI(service.getDescription());
				break;
			}
		}
		
		if (location == null)
			return null;
		
		/*
		 * Try to resolve relative locations with respect to the project 
		 * resource root directories
		 * 
		 * NOTE notice that this plugin is usually invoked in early phases
		 * of the build lifecycle, where resources have not yet been copied 
		 * into the target directory. So we search directly through all 
		 * configured resource directories
		 */
		if (! location.isAbsolute()) {
			for (Resource root : resources) {
				URI baseURI		= new File(root.getDirectory()).toURI();
				URI resolved	= baseURI.resolve(location);
				
				/*
				 * verify if the file exists, before ending the search
				 */
				File description = new File(resolved);
				if (description.exists()) {
					location = resolved;
					break;
				}
			}
			
		}
			
		synchronized (scpdLocations) {
			scpdLocations.put(serviceType,location);
			return location;
		}

	}
	
	/**
	 * Get the java mapping associated with a given service type
	 * 
	 */
	public Mapping getJavaMapping(String serviceType) {

		for (Service service : services) {
			if (service.getType().equals(serviceType)) {
				return service.getMapping();
			}
		}
		
		return null;
	}

}
