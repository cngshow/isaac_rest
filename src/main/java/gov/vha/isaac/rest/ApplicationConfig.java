package gov.vha.isaac.rest;

import java.io.File;
import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.internal.AsyncRunLevelContext;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
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
import gov.vha.isaac.ochre.api.util.DBLocator;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;

@ApplicationPath("rest/")
public class ApplicationConfig extends Application implements ContainerLifecycleListener
{
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
		
		LookupService.get();

		//TODO background thread this
		File dataStoreLocation = DBLocator.findDBFolder(new File("E:/EclipseWorkspaces/ISAAC2/ISAAC-fx-gui-pa/fx-gui-assembly/vhat-2016.01.07-1.0-SNAPSHOT-all.data"));

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

		LookupService.startupIsaac();

		System.out.println("Done setting up ISAAC");

		System.out.println("System up...");

		StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
		LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();

		ConceptService conceptService = Get.conceptService();

		ConceptChronology<? extends ConceptVersion<?>> sctId = conceptService.getConcept(MetaData.SNOMED_INTEGER_ID.getPrimordialUuid());
		System.out.println("Found [1]: " + sctId);

		System.out.println(conceptService.getSnapshot(stampCoordinate, languageCoordinate).getConceptSnapshot(sctId.getNid()));
	}
}
