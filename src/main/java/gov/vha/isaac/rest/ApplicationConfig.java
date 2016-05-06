package gov.vha.isaac.rest;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.ApplicationPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.util.ArtifactUtilities;
import gov.vha.isaac.ochre.api.util.DBLocator;
import gov.vha.isaac.ochre.api.util.DownloadUnzipTask;
import gov.vha.isaac.ochre.api.util.WorkExecutors;
import gov.vha.isaac.rest.api1.RestPaths;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

@ApplicationPath("rest/")
public class ApplicationConfig extends ResourceConfig implements ContainerLifecycleListener
{
	private static final AtomicInteger startup = new AtomicInteger(1);
	private static Logger log = LogManager.getLogger();
	
	private static ApplicationConfig instance_;
	
	private StringProperty status_ = new SimpleStringProperty("Not Started");
	
	//TODO implement convenience methods for 'associations'
	
	public ApplicationConfig()
	{
		//If we leave everything to annotations, is picks up the eclipse moxy gson writer, which doesn't handle abstract classes properly.
		//The goal here is to force it to use Jackson, but it seems that registering jackson disables scanning, so also have to re-enable 
		//scanning.  It also seems ot forget to scan this class... so register itself..
		super(new ResourceConfig().packages("gov.vha.isaac.rest").register(JacksonFeature.class).register(ApplicationConfig.class));
	}
	
	public static ApplicationConfig getInstance()
	{
		return instance_;
	}
	
	@Override
	public void onReload(Container arg0)
	{
		// noop
	}

	@Override
	public void onShutdown(Container arg0)
	{
		log.info("Stopping ISAAC");
		LookupService.shutdownIsaac();
		log.info("ISAAC stopped");
	}

	@Override
	public void onStartup(Container container)
	{
		log.info("onStartup called");
		if (instance_ != null)
		{
			throw new RuntimeException("Unexpected!");
		}
		instance_ = this;
		issacInit();
	}
	
	public boolean isIsaacReady()
	{
		return LookupService.isIsaacStarted();
	}
	
	public String getStatusMessage()
	{
		return status_.get();
	}
	
	private void issacInit()
	{
		log.info("Isaac Init called");
		if (startup.getAndDecrement() == 1)
		{
			log.info("Executing initial ISAAC Init in background thread");
			//do startup in this thread
			LookupService.get();
			LookupService.startupWorkExecutors();
			
			Runnable r = new Runnable()
			{
				
				@Override
				public void run()
				{
					try
					{
						log.info("ISAAC Init thread begins");
						//First, see if there is a properties file embedded in the war (PRISME places this during deployment)
						Properties props = new Properties();
						try (final InputStream stream = this.getClass().getResourceAsStream("/prisme.properties"))
						{
							props.load(stream);
						}
						catch (Exception e1)
						{
							log.info("Could not read a prism.properties file from the classpath");
						}
						
						System.out.println("Read property:" + props.getProperty("g"));
						
						
						if (StringUtils.isBlank(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY)))
						{
							//if there isn't an official system property set, check this one.
							String sysProp = System.getProperty("isaacDatabaseLocation");
							File temp;
							if (StringUtils.isBlank(sysProp))
							{
								log.info("Downloading a database for use");
								status_.set("Downloading DB");
								try
								{
									temp = downloadDB();
								}
								catch (Exception e)
								{
									status_.unbind();
									status_.set("Download Failed: " + e);
									throw new RuntimeException(e);
								}
							}
							else
							{
								temp = new File(sysProp);
							}
							
							File dataStoreLocation = DBLocator.findDBFolder(temp);
							
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

						status_.set("Starting ISAAC");
						LookupService.startupIsaac();
						status_.set("Ready");
						System.out.println("Done setting up ISAAC");

						System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", 
							"http://localhost:8180/", RestPaths.conceptVersionAppPathComponent + MetaData.CONCRETE_DOMAIN_OPERATOR.getNid()));
					}
					catch (Exception e)
					{
						log.error("Failure starting ISAAC", e);
						status_.unbind();
						status_.set("FAILED!");
					}
				}
			};
			
			LookupService.get().getService(WorkExecutors.class).getExecutor().execute(r);
		}
	}
	
	private File downloadDB() throws Exception
	{
		log.info("Checking for existing DB");
		File targetDBLocation = new File(System.getProperty("java.io.tmpdir"), "ISAAC.db");
		if (targetDBLocation.isDirectory())
		{
			log.info("Using existing db folder: " + targetDBLocation.getAbsolutePath());
			status_.set("Using existing directory");
			return targetDBLocation;
		}
		
		File dbFolder = File.createTempFile("ISAAC-DATA", "");
		dbFolder.delete();
		dbFolder.mkdirs();
		log.info("Downloading DB to " + dbFolder.getAbsolutePath());
		URL snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/groups/everything/" 
				+ ArtifactUtilities.makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/groups/everything/", "system", "system", 
						"gov.vha.isaac.db", "vets", "1.0", "all", "cradle.zip"));
		Task<File> task = new DownloadUnzipTask("system", "system", snapshot, true, true, dbFolder);
		status_.bind(task.messageProperty());
		Get.workExecutors().getExecutor().submit(task);
		task.get();
		status_.unbind();
		
		snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/groups/everything/" 
				+ ArtifactUtilities.makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/groups/everything/", "system", "system", 
						"gov.vha.isaac.db", "vets", "1.0", "all", "lucene.zip"));
		task = new DownloadUnzipTask("system", "system", snapshot, true, true, dbFolder);
		status_.bind(task.messageProperty());
		Get.workExecutors().getExecutor().submit(task);
		task.get();
		status_.unbind();
		status_.set("Download complete");

		log.debug("Renaming " + dbFolder.getCanonicalPath() + " to " + targetDBLocation.getCanonicalPath());
		if (dbFolder.renameTo(targetDBLocation))
		{
			return targetDBLocation;
		}
		else
		{
			log.warn("Failed to rename the database");
			//rename failed...  perhaps we tried to cross filesystems?
			return dbFolder;
		}
	}
}
