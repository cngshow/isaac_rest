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

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link ThreadLocalCleanupContainerResponseFilter}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * Filter that resets RequestInfo state in ThreadLocal after handling a client request
 * Priority is set to 9999 to increase likelihood that cleanup will be performed only after all other filters execute
 * 
 */
@Priority(9999)
@Provider
public class ThreadLocalCleanupContainerResponseFilter implements ContainerResponseFilter {
	private static Logger log = LogManager.getLogger();

	/* (non-Javadoc)
	 * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException 
	{
		log.debug("Removing RequestInfo state in ThreadLocal after server response to client request...");
		try
		{
			RequestInfo.remove();
		}
		catch (Throwable e)
		{
			log.error("Unexpected error trying to clear the thread local", e);
		}
	}
}
