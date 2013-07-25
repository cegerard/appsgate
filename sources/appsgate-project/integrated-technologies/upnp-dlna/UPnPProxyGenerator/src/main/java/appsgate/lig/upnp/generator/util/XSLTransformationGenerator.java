package appsgate.lig.upnp.generator.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.URIResolver;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class represents a source generation implemented as a XSLT
 * transformation
 * 
 * @author vega
 *
 */
public class XSLTransformationGenerator implements URIResolver {
	
	/**
	 * The XSLT transformation used to generate the code
	 */
	private final String transformation;
	
	/**
	 * The transformer factory associated with this generator
	 */
	private final TransformerFactory transformerFactory;

	
	public XSLTransformationGenerator(String transformation) {
		
		this.transformation 	= transformation;
		this.transformerFactory	= TransformerFactory.newInstance();
		
		this.transformerFactory.setURIResolver(this);
	}

	/**
	 * Try to resolve relative references specified in xsl:import xsl:include
	 * directly from the specified loader
	 */
	@Override
	public Source resolve(String href, String base) {
		try {
			URI resourceURI = new URI(href);
			if (resourceURI.isAbsolute())
				return null;
			
			InputStream resourceStream 	= this.getClass().getResourceAsStream("/"+href);
			if (resourceStream == null)
				return null;

			return new StreamSource(resourceStream);
		} catch (URISyntaxException e) {
		}

		return null;
	}
	
	public void transform(InputStream input, OutputStream output, Map<String,Object> parameters) throws  TransformerException {
		
		Transformer transformer = transformerFactory.newTransformer(resolve(transformation, null));		
		for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
			transformer.setParameter(parameter.getKey(),parameter.getValue());
		}

		transformer.transform(new StreamSource(input), new StreamResult(output));
	}

}
