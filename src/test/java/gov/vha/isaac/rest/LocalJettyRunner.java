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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.id.IdAPIs;
import gov.vha.isaac.rest.api1.taxonomy.TaxonomyAPIs;
import gov.vha.isaac.rest.jerseyConfig.MyExceptionMapper;
import gov.vha.isaac.rest.jerseyConfig.MyJacksonMapperConfig;
import gov.vha.isaac.rest.jerseyConfig.RestExceptionMapper;

/**
 * 
 * {@link LocalJettyRunner}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LocalJettyRunner
{
	private static final URI BASE_URI = URI.create("http://localhost:8180/rest/");

	public static void main(String[] args) throws Exception
	{
		System.out.println("Launching Jetty Server");

		final ResourceConfig resourceConfig = new ResourceConfig(ApplicationConfig.class, 
				ConceptAPIs.class,
				TaxonomyAPIs.class,
				IdAPIs.class,
				RestExceptionMapper.class,
				MyExceptionMapper.class, 
				JacksonFeature.class, 
				MyJacksonMapperConfig.class, 
				RestApi.class);

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

		System.out.println(String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", 
			BASE_URI.toString().substring(0, BASE_URI.toString().length() - 5), 
				RestPaths.conceptVersionAppPathComponent + MetaData.CONCRETE_DOMAIN_OPERATOR.getNid()));
		Thread.currentThread().join();
	}
}
