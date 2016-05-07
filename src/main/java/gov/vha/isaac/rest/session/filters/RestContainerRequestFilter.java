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

package gov.vha.isaac.rest.session.filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link RestContainerRequestFilter}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * Initializes RequestInfo ThreadLocal if necessary and initializes
 * StampCoordinate and LanguageCoordinate based on intercepted query parameters,
 * or default values if no relevant parameters are present.
 * 
 * Priority is set to Priorities.USER - 500
 * to ensure that this filter is run before other user filters
 * 
 */
@Priority(Priorities.USER - 500)
@Provider
public class RestContainerRequestFilter implements ContainerRequestFilter {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * 
	 */
	public RestContainerRequestFilter() {
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if (!ApplicationConfig.getInstance().isIsaacReady())
		{
			LOG.debug("Rejecting request as ISAAC is not yet ready");
			throw new IOException("The system is not yet ready.  Status:  " + ApplicationConfig.getInstance().getStatusMessage());
		}
		
		LOG.debug("Running CONTAINER REQUEST FILTER " + this.getClass().getName() + " on request " + requestContext.getRequest().getMethod());
		
		LOG.debug("Path parameters: " + requestContext.getUriInfo().getPathParameters().keySet());
		for (Map.Entry<String, List<String>> parameter : requestContext.getUriInfo().getPathParameters().entrySet()) {
			LOG.debug("Path parameter \"" + parameter.getKey() + "\"=\"" + parameter.getValue() + "\"");
		}
		LOG.debug("Query parameters: " + requestContext.getUriInfo().getQueryParameters().keySet());
		for (Map.Entry<String, List<String>> parameter : requestContext.getUriInfo().getQueryParameters().entrySet()) {
			LOG.debug("Query parameter \"" + parameter.getKey() + "\"=\"" + parameter.getValue() + "\"");
		}

		try {
			RequestInfo.get().readAll(requestContext.getUriInfo().getQueryParameters());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
