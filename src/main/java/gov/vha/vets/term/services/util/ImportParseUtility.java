/**
 * 
 */
package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.config.XSDLocator;
import gov.vha.vets.term.services.exception.STSException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.msv.verifier.jarv.TheFactoryImpl;

/**
 * @author vhaislchevaj
 *
 */
public abstract class ImportParseUtility
{
	/**
	 * retrieve the nodes in the DOM tree
	 * @param configFileName
	 * @param schemaName
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @throws STSException
	 * @throws VerifierConfigurationException
	 */
	public static List<Element> getNodes(String configFileName, String schemaName) throws JDOMException, IOException, STSException, VerifierConfigurationException
	{
		// Load the XML File and Walk Down the Tree.
		SAXBuilder builder = new SAXBuilder();
		validateXMLAgainstSchema(configFileName, schemaName);
		File inputFile = new File(configFileName);
		if (inputFile == null) 
		{ 
			throw new FileNotFoundException("Unable to locate file: " + configFileName); 
		}
		Document document = builder.build(inputFile);
		Element rootNode = document.getRootElement();

		return rootNode.getChildren();
	}

	/**
	 * validate the XML file against the schema in the classpath
	 * @param configFileName
	 * @param schemaName
	 * @throws STSException
	 * @throws JDOMException
	 * @throws IOException
	 * @throws VerifierConfigurationException
	 */
	public static void validateXMLAgainstSchema(String configFileName, String schemaName) throws STSException, JDOMException, IOException, VerifierConfigurationException
	{
		// Load the XML File and Walk Down the Tree.
		XSDLocator builder = new XSDLocator();
		File inputFile = new File(configFileName);
		if (inputFile == null) 
		{ 
			throw new FileNotFoundException("Unable to locate file: " + configFileName); 
		}
		
		InputStream schemaIS = builder.getClass().getResourceAsStream(schemaName);
		if (schemaIS == null) 
		{ 
			throw new FileNotFoundException("The resource schema " + schemaName + " could not be found."); 
		}
		
		try
		{
			VerifierFactory factory = new TheFactoryImpl();
			Schema schema = factory.compileSchema(schemaIS);
			Verifier verifier = schema.newVerifier();
			verifier.verify(inputFile);
		}
		catch (SAXParseException e)
		{
			throw new STSException("The import file did not validate against the Schema file: "
					+ schemaName + " at line " +  e.getLineNumber() + ", column " + e.getColumnNumber() + ". The error is: " + e.getMessage(), e);
		}
		catch (SAXException e)
		{
			throw new STSException("The import file did not validate against the Schema file: "
					+ schemaName + ". The error is: " + e.getMessage(), e);
			
		}
	}
}