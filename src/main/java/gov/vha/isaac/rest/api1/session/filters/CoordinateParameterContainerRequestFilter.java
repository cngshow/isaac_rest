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

package gov.vha.isaac.rest.api1.session.filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.session.RequestInfo;

/**
 * 
 * {@link CoordinateParameterContainerRequestFilter}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Provider
public class CoordinateParameterContainerRequestFilter implements ContainerRequestFilter {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * 
	 */
	public CoordinateParameterContainerRequestFilter() {
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
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
			RequestInfo.get().readStampCoordinate(requestContext.getUriInfo().getQueryParameters());
			RequestInfo.get().readLanguageCoordinate(requestContext.getUriInfo().getQueryParameters());
		} catch (RestException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}
}
