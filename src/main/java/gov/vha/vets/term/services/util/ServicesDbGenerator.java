package gov.vha.vets.term.services.util;

import java.util.Properties;

public class ServicesDbGenerator
{
	/**
	 * The <code>recreateSchema</code> method drops and creates all the tables defined
	 * in the model objects through the hibernate configuration file.
	 *
	 */
	public static void recreateSchema()
	{
		// Create properties for the configuration
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", "create");
		
		try
		{
			// make sure the configuration object has been created
            HibernateSessionFactory.currentSession();
			// Build a sessionFactory using properties
			HibernateSessionFactory.getConfiguration().addProperties(properties).buildSessionFactory();
	    	
			// close the session and reopen it so that the database is initialized
			HibernateSessionFactory.currentSession().close();
            HibernateSessionFactory.currentSession();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		recreateSchema();
	}
}
