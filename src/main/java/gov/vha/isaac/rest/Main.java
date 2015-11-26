package gov.vha.isaac.rest;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import gov.va.isaac.init.SystemInit;
import gov.vha.isaac.metadata.coordinates.LanguageCoordinates;
import gov.vha.isaac.metadata.coordinates.StampCoordinates;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
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

		SystemInit.doBasicSystemInit(new File(args[0]));

		LookupService.startupIsaac();

		System.out.println("System up...");

		StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
		LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();

		ConceptService conceptService = Get.conceptService();

		ConceptChronology<? extends ConceptVersion<?>> bleedingConcept1 = conceptService.getConcept(UUID.fromString("89ce6b87-545b-3138-82c7-aafa76f8f9a0"));
		System.out.println("Found [1]: " + bleedingConcept1);

		System.out.println(conceptService.getSnapshot(stampCoordinate, languageCoordinate).getConceptSnapshot(bleedingConcept1.getNid()));

		System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", BASE_URI, "ts"));
		Thread.currentThread().join();
	}
}
