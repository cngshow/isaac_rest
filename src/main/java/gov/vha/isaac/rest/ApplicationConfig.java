package gov.vha.isaac.rest;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.util.DBLocator;

@ApplicationPath("rest/")
public class ApplicationConfig extends Application implements ContainerLifecycleListener
{
	private static final AtomicInteger startup = new AtomicInteger(1);
	private static Logger log = LogManager.getLogger();
	
	//TODO implement convenience method to describe a object - concept, semeeme, etc
	//TODO implement convenience methods for 'associations'
	
	public static void issacInit()
	{
		log.info("Rest Configuration Requested");
		if (startup.getAndDecrement() == 1)
		{
			log.info("Executing ISAAC Init");
			//do startup in this thread
			LookupService.get();
			
			//TODO background thread this
			if (StringUtils.isBlank(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)))
			{
				//if there isn't an official system property set, check this one.
				String sysProp = System.getProperty("isaacDatabaseLocation");
				if (StringUtils.isBlank(sysProp))
				{
					sysProp = "";
				}
				File dataStoreLocation = DBLocator.findDBFolder(new File(sysProp));
				
				if (!dataStoreLocation.exists())
				{
					throw new RuntimeException("Couldn't find a data store from the input of '" + dataStoreLocation.getAbsoluteFile().getAbsolutePath() + "'");
				}
				if (!dataStoreLocation.isDirectory())
				{
					throw new RuntimeException("The specified data store: '" + dataStoreLocation.getAbsolutePath() + "' is not a folder");
				}
		
				LookupService.getService(ConfigurationService.class).setDataStoreFolderPath(dataStoreLocation.toPath());
				System.out.println("  Setup AppContext, data store location = " + dataStoreLocation.getAbsolutePath());
			}
	
			LookupService.startupIsaac();
	
			System.out.println("Done setting up ISAAC");
		}
	}
	
	@Override
	public void onReload(Container arg0)
	{
		// noop
	}

	@Override
	public void onShutdown(Container arg0)
	{
		// noop
	}

	@Override
	public void onStartup(Container container)
	{
		log.info("onStartup called");
		//Bunch of junk debug code from hacking at glassfish...
		//ServiceLocator jerseyServiceLocator = container.getApplicationHandler().getServiceLocator();
		
		
//		System.err.println("parent? " + jerseyServiceLocator.getParent());
//		
//		System.err.println("---JERSEY DUMP START---");
//		ServiceLocatorUtilities.dumpAllDescriptors(jerseyServiceLocator);
//		System.err.println("---JERSEY DUMP END---");
//		
//		
//		try
//		{
//			Thread.sleep(5000);
//			System.err.println("Found RunLevelController? " + jerseyServiceLocator.getService(RunLevelController.class));
//			Thread.sleep(5000);
//			System.err.println("Found AsyncRunLevelContext? " + jerseyServiceLocator.getService(AsyncRunLevelContext.class));
//		}
//		catch (Exception e)
//		{
//			System.err.println("FAILURE:" + e);
//		}
		
		//Make sure the service Locator comes up ok
		//LookupService.setExistingLocator(null);
		
		issacInit();
	}
}
