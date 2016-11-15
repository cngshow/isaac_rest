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
package gov.vha.isaac.rest.api1.request;

import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.server.ContainerRequest;

import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link ContentRequestAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.contentRequestAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class ContentRequestAPIs
{
	@Context
	private SecurityContext securityContext;

	/**
	 * An initial sample of accepting a POST request - this particular one is for testing with the NDS team.
	 * @param data - this API simply accepts a string value - either plain text, JSON, or XML encoded.
	 * @return 406 error if unparsable, 202 if acceptable - and returns an identifier for the request.
	 * @throws RestException
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML , MediaType.TEXT_PLAIN})
	@Produces({MediaType.TEXT_PLAIN})
	@Path(RestPaths.termRequestComponent)  
	public Response putNewTermRequest(String data) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		//System.out.println("received media type: " + request.getMediaType());
		System.out.println("got data '" + data + "'");
		if (data.contains("BAD"))
		{
			return Response.status(Status.NOT_ACCEPTABLE).entity("Invalid Request").build();
		}
		else
		{
			return Response.status(Status.ACCEPTED).entity("Assigned ID of " + UUID.randomUUID() + " to your request").build();
		}
	}
}
