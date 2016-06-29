package gov.vha.vets.term.services.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class PropertiesReader
{
	private static Logger log = Logger.getLogger(PropertiesReader.class.getPackage().getName());

	/**
	 * The <code>getApplicationProperties</code> method returns the value associated with
	 * the key that is passed to the method.  This method always refers to "application.properties"
	 * which must be in the classpath.
	 * @param parameterName
	 * @return String Value of the parameter
	 * @throws MissingResourceException
	 */
	public static String getApplicationProperties(String parameterName) 
    {
        String parameterValue = "";
        
        String baseName = "application";
        
        try
        {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName);
            String key = parameterName;
            
            parameterValue = resourceBundle.getString(key);
        }
        catch (RuntimeException e)
        {
        	// this really is not a problem, it may mean it failed for a certain locale
        	log.error(e.getMessage());
        }
        
        return parameterValue;
    }
	
	public static String getBuildVersionProperties(String parameterName) 
    {
        String parameterValue = "";
        
        String baseName = "properties.buildVersion";
        
        try
        { 
	        ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName);
	        String key = parameterName;
	        
	        parameterValue = resourceBundle.getString(key);
        }
        catch (RuntimeException e)
        {
        	// this really is not a problem, it may mean it failed for a certain locale
        	log.error(e.getMessage());
        }
        
        return parameterValue;
    }

}
