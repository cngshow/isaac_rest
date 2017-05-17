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

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.rest.api.data.vuid.RestVuidBlockData;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.session.VuidService;

/**
 * {@link VuidWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.vuidAPIsPathComponent)
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
public class VuidWriteAPIs
{
	private static Logger log = LogManager.getLogger(VuidWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * Allocates and returns a block (range) of contiguous VUID numbers
	 * 
	 * @param blockSize - integer size (0 < size < 1,000,000) of contiguous block of VUID numbers to be returned
	 * @param reason - text (length <= 30) explaining purpose of allocation request
	 * @param ssoToken - ssoToken string used to determine and authenticate user and role
	 *
	 * @return RestVuidBlockData containing start and end integer values representing the VUID values bounding the returned block from nearer to farther from zero
	 * 
	 * @throws RestException
	 * 
	 */
	final private static int MAX_REASON_LENGTH = 30;
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.allocateComponent)
	public RestVuidBlockData allocate(
			@QueryParam(RequestParameters.blockSize) int blockSize,
			@QueryParam(RequestParameters.reason) String reason,
			@QueryParam(RequestParameters.ssoToken) String ssoToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());
		
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.ssoToken,
				RequestParameters.blockSize,
				RequestParameters.reason);

		if (StringUtils.isBlank(reason)) {
			throw new RestException("reason", null, "blank or null request reason");
		}
		
		if (reason.length() > MAX_REASON_LENGTH) {
			throw new RestException("reason", reason, "request reason longer than maximum (" + MAX_REASON_LENGTH + ")");
		}

		if (blockSize < 1) {
			throw new RestException("blockSize", blockSize + "", "requested blocksize is less than 1 (" + blockSize + ")");
		}

		try
		{			
			// TODO invoke VuidService API
			Optional<RestVuidBlockData> block = LookupService.getService(VuidService.class).allocate(blockSize, reason, ssoToken);
	
			return block.get();
		}
		catch (Exception e)
		{
			log.error("Error allocating vuids", e);
			throw new RestException("Unexpected internal error allocating VUIDs at " + LookupService.getService(VuidService.class).getVuidServiceUrl());
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.allocateTestComponent)
	public RestVuidBlockData allocateTestOnly(
			@QueryParam(RequestParameters.blockSize) int blockSize,
			@QueryParam(RequestParameters.reason) String reason,
			@QueryParam(RequestParameters.ssoToken) String ssoToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.blockSize,
				RequestParameters.reason,
				RequestParameters.ssoToken);
		
		return allocate(blockSize, reason, ssoToken);
	}
}