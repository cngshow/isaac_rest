package gov.vha.isaac.rest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.maven.plugin.MojoExecutionException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
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
import gov.vha.isaac.rest.jerseyConfig.MyExceptionMapper;
import gov.vha.isaac.rest.jerseyConfig.MyJacksonMapperConfig;

public class Main
{
	private static final URI BASE_URI = URI.create("http://localhost:8180/base/");

	public static void main(String[] args) throws Exception
	{
		System.out.println("\"Hello World\" Jersey Example App");

		final ResourceConfig resourceConfig = new ResourceConfig(RestApi.class, JacksonFeature.class, MyJacksonMapperConfig.class, MyExceptionMapper.class);

		Map<String, Object> properties = new HashMap<>();
		properties.put(MessageProperties.XML_FORMAT_OUTPUT, true);
		resourceConfig.addProperties(properties);
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				server.shutdownNow();
			}
		}));
		server.start();

		//Make sure the service Locator comes up ok
		LookupService.get();

		File dataStoreLocation = DBLocator.findDBFolder(new File(args[0]));

		if (!dataStoreLocation.exists())
		{
			throw new MojoExecutionException("Couldn't find a data store from the input of '" + dataStoreLocation.getAbsoluteFile().getAbsolutePath() + "'");
		}
		if (!dataStoreLocation.isDirectory())
		{
			throw new IOException("The specified data store: '" + dataStoreLocation.getAbsolutePath() + "' is not a folder");
		}

		LookupService.getService(ConfigurationService.class).setDataStoreFolderPath(dataStoreLocation.toPath());
		System.out.println("  Setup AppContext, data store location = " + dataStoreLocation.getCanonicalPath());

		LookupService.startupIsaac();

		System.out.println("Done setting up ISAAC");

		System.out.println("System up...");

		StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
		LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();

		ConceptService conceptService = Get.conceptService();

		ConceptChronology<? extends ConceptVersion<?>> sctId = conceptService.getConcept(MetaData.SNOMED_INTEGER_ID.getPrimordialUuid());
		System.out.println("Found [1]: " + sctId);

		System.out.println(conceptService.getSnapshot(stampCoordinate, languageCoordinate).getConceptSnapshot(sctId.getNid()));

		System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", BASE_URI, "ts"));
		Thread.currentThread().join();
	}
}
