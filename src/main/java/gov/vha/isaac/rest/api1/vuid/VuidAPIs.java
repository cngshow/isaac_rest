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
package gov.vha.isaac.rest.api1.vuid;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.rest.api.data.RestBoolean;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.session.VuidService;

/**
 * {@link VuidAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.vuidAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER, UserRoleConstants.VUID_REQUESTOR})
public class VuidAPIs
{
	private static Logger log = LogManager.getLogger(VuidAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * Validate a passed VUID (long) as being within the universe of allocated VUIDs
	 * 
	 * @param vuid long value to be validated
	 * @return RestBoolean indicating true or false
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.validateComponent)
	public RestBoolean isValidVuid(
			@QueryParam(RequestParameters.vuid) long vuid) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());
		
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.vuid);

		log.info("Checking validity of VUID " + vuid + "...");

		boolean isValid = false;
		isValid = LookupService.getService(VuidService.class).isVuidValid(vuid);
		
		log.info("VUID " + vuid + " is " + (isValid ? "VALID" : "NOT VALID"));
		
		return new RestBoolean(isValid);
	}
}