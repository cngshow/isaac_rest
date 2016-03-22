package gov.vha.isaac.rest;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;
import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.util.ArtifactUtilities;
import gov.vha.isaac.ochre.api.util.DBLocator;
import gov.vha.isaac.ochre.api.util.DownloadUnzipTask;
import gov.vha.isaac.ochre.api.util.WorkExecutors;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.rest.api1.RestPaths;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

@ApplicationPath("rest/")
public class ApplicationConfig extends Application implements ContainerLifecycleListener
{
	private static final AtomicInteger startup = new AtomicInteger(1);
	private static Logger log = LogManager.getLogger();
	
	private static ApplicationConfig instance_;
	
	private StringProperty status_ = new SimpleStringProperty("Not Started");
	
	//TODO implement convenience method to describe a object - concept, semeeme, etc
	//TODO implement convenience methods for 'associations'
	
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
		// noop
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
					log.info("ISAAC Init thread begins");
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
					
					
					StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
					LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();

					ConceptService conceptService = Get.conceptService();

					ConceptChronology<? extends ConceptVersion<?>> sctId = conceptService.getConcept(MetaData.SNOMED_INTEGER_ID.getPrimordialUuid());
					System.out.println("Found [1]: " + sctId);

					System.out.println(conceptService.getSnapshot(stampCoordinate, languageCoordinate).getConceptSnapshot(sctId.getNid()));

					System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", 
						"http://localhost:8180/", RestPaths.conceptVersionAppPathComponent + MetaData.CONCRETE_DOMAIN_OPERATOR.getNid()));
					
				}
			};
			
			LookupService.get().getService(WorkExecutors.class).getExecutor().execute(r);
		}
	}
	
	private File downloadDB() throws Exception
	{
		
		File dbFolder = File.createTempFile("ISAAC-DATA", "");
		dbFolder.delete();
		dbFolder.mkdirs();
		log.info("Downloading DB to " + dbFolder.getAbsolutePath());
		URL snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/" 
				+ ArtifactUtilities.makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/", "system", "system", 
						"gov.vha.isaac.db", "vets", "1.0-SNAPSHOT", "all", "cradle.zip"));
		Task<File> task = new DownloadUnzipTask("system", "system", snapshot, true, true, dbFolder);
		status_.bind(task.messageProperty());
		Get.workExecutors().getExecutor().submit(task);
		task.get();
		status_.unbind();
		
		snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/" 
				+ ArtifactUtilities.makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/", "system", "system", 
						"gov.vha.isaac.db", "vets", "1.0-SNAPSHOT", "all", "lucene.zip"));
		task = new DownloadUnzipTask("system", "system", snapshot, true, true, dbFolder);
		status_.bind(task.messageProperty());
		Get.workExecutors().getExecutor().submit(task);
		task.get();
		status_.unbind();
		status_.set("Download complete");
		return dbFolder;
	}
}
