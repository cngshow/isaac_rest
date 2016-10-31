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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.RestApplicationSecurityContext;
import gov.vha.isaac.rest.session.User;

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
@Priority(Priorities.USER - 500) // TODO required with @PreMatching?
@Provider
@PreMatching
public class RestContainerRequestFilter implements ContainerRequestFilter {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * 
	 */
	public RestContainerRequestFilter() {
	}

	private void authenticate(ContainerRequestContext requestContext) {
		// GET, POST, PUT, ...
		String method = requestContext.getMethod();

		String path = requestContext.getUriInfo().getPath(true);

		// Allow wadl to be retrieved
		if(method.equals(HttpMethod.GET) && (path.equals("application.wadl") || path.equals("application.wadl/xsd0.xsd"))) {
			return;
		}

		// Get user
		User user = RequestInfo.get().getUser().get();

		// Configure Security Context here
		String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
		requestContext.setSecurityContext(new RestApplicationSecurityContext(user, scheme));
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

		try  {
			RequestInfo.get().readAll(requestContext.getUriInfo().getQueryParameters());

			// TODO should User be populated with dummy value when neither SSO token nor EditToken passed?

			if (requestContext.getUriInfo().getPath(true).contains("/write/")
					|| ! requestContext.getMethod().equals(HttpMethod.GET)
					|| requestContext.getUriInfo().getPath().contains(RestPaths.coordinateAPIsPathComponent + RestPaths.editTokenComponent)
					|| requestContext.getUriInfo().getQueryParameters().containsKey(RequestParameters.editToken)
					) {
				try {
					// TODO Joel find out why first invocation seems to fail and fix
					RequestInfo.get().getEditCoordinate();
				} catch (Exception e) {
					// ignore
				}
				RequestInfo.get().getEditCoordinate();
			} else {
				// Set a default read_only user for clients that do not pass SSO token or EditToken
				RequestInfo.get().setDefaultReadOnlyUser(); // TODO confirm that this will do what we want and not blow up
			}

			authenticate(requestContext); // Apply after readAll() in order to populate User, if possible
		} 
		catch (IOException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
