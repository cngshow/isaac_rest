/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.rest.api1.RestPaths;

/**
 * 
 * {@link LocalJettyRunner}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LocalJettyRunner
{
	private static final URI BASE_URI = URI.create("http://localhost:8180/rest/");
	
	public static ResourceConfig configureJerseyServer() throws IOException, ClassNotFoundException
	{
		//Find all classes with the specified annotations:
		AnnotatedClasses ac = new AnnotatedClasses();

		@SuppressWarnings("unchecked") AnnotationDetector cf = new AnnotationDetector(new AnnotationReporter(ac, 
				new Class[] {Path.class, ApplicationPath.class, Provider.class}));
		cf.detect(new String[] {"gov.vha.isaac.rest"});
		
		Set<Class<?>> temp = new HashSet<Class<?>>(Arrays.asList(ac.getAnnotatedClasses()));
		temp.add(JacksonFeature.class);  //No annotations in this class
		
		ResourceConfig rc = new ResourceConfig(temp);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(MessageProperties.XML_FORMAT_OUTPUT, true);
		rc.addProperties(properties);
		
		return rc;
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println("Launching Jetty Server");
		
		final ResourceConfig resourceConfig = configureJerseyServer();

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
		
		
		StampCoordinate stampCoordinate = StampCoordinates.getDevelopmentLatest();
		LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();

		ConceptService conceptService = Get.conceptService();

		ConceptChronology<? extends ConceptVersion<?>> sctId = conceptService.getConcept(MetaData.SNOMED_INTEGER_ID.getPrimordialUuid());
		System.out.println("Found [1]: " + sctId);

		System.out.println(conceptService.getSnapshot(stampCoordinate, languageCoordinate).getConceptSnapshot(sctId.getNid()));

		System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", 
			BASE_URI.toString().substring(0, BASE_URI.toString().length() - 5), 
				RestPaths.conceptVersionAppPathComponent + MetaData.CONCRETE_DOMAIN_OPERATOR.getNid()));
		Thread.currentThread().join();
	}
}
