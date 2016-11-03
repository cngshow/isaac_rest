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
package gov.vha.isaac.rest.api1.coordinate;

import javax.annotation.security.DeclareRoles;
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

import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.RestEditToken;
import gov.vha.isaac.rest.api1.data.coordinate.RestCoordinates;
import gov.vha.isaac.rest.api1.data.coordinate.RestLanguageCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestLogicCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestStampCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link CoordinateAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.coordinateAPIsPathComponent)
@DeclareRoles({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class CoordinateAPIs
{
	private static Logger log = LogManager.getLogger(CoordinateAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * 
	 * This method returns a serialized CoordinatesToken string specifying all coordinate parameters
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinates specified by the
	 * explicit serialized CoordinatesToken string, and the resulting modified CoordinatesToken will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @param stated specifies premise/taxonomy type of <code>STATED</code> when true and <code>INFERRED</code> when false.
	 * 
	 * @param descriptionTypePrefs specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
	 * @param dialectPrefs specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
	 * @param language specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
	 * 
	 * @param modules specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
	 * @param path specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
	 * @param precedence specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
	 * @param allowedStates specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".
	 * @param time specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".
	 * 
	 * @param logicStatedAssemblage specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicInferredAssemblage specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param descriptionLogicProfile specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param classifier specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * 
	 * @return RestCoordinatesToken
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.coordinatesTokenComponent)  
	public RestCoordinatesToken getCoordinatesToken(
			@QueryParam(RequestParameters.coordToken) String coordToken,
			
			@QueryParam(RequestParameters.stated) String stated,
			
			@QueryParam(RequestParameters.descriptionTypePrefs) String descriptionTypePrefs,
			@QueryParam(RequestParameters.dialectPrefs) String dialectPrefs,
			@QueryParam(RequestParameters.language) String language,
			
			@QueryParam(RequestParameters.modules) String modules,
			@QueryParam(RequestParameters.path) String path,
			@QueryParam(RequestParameters.precedence) String precedence,
			@QueryParam(RequestParameters.allowedStates) String allowedStates,
			@QueryParam(RequestParameters.time) String time,
			
			@QueryParam(RequestParameters.logicStatedAssemblage) String logicStatedAssemblage,
			@QueryParam(RequestParameters.logicInferredAssemblage) String logicInferredAssemblage,
			@QueryParam(RequestParameters.descriptionLogicProfile) String descriptionLogicProfile,
			@QueryParam(RequestParameters.classifier) String classifier) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		// All parameters, including defaults, are handled by the RestContainerRequestFilter
		
		log.debug("Returning RestCoordinatesToken...");
		return new RestCoordinatesToken(RequestInfo.get().getCoordinatesToken());
	}

	/**
	 * 
	 * This method returns an object comprising all coordinate parameters.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinates corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinates specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinates will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestCoordinates Object containing all coordinates.
	 * Note that <code>RestTaxonomyCoordinate</code> contains <code>RestStampCoordinate</code>, <code>RestLanguageCoordinate</code> and <code>RestLogicCoordinate</code>.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.coordinatesComponent)  
	public RestCoordinates getCoordinates(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);

		RestTaxonomyCoordinate taxonomyCoordinate = getTaxonomyCoordinate(coordToken);
		RestCoordinates coordinates =
				new RestCoordinates(
						taxonomyCoordinate,
						taxonomyCoordinate.stampCoordinate,
						taxonomyCoordinate.languageCoordinate,
						taxonomyCoordinate.logicCoordinate
				);

		log.debug("Returning REST Coordinates...");
		
		return coordinates;
	}

	/**
	 * 
	 * This method returns <code>RestTaxonomyCoordinate</code>.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinate corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinate specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinate will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestTaxonomyCoordinate
	 * Note that <code>RestTaxonomyCoordinate</code> contains <code>RestStampCoordinate</code>, <code>RestLanguageCoordinate</code> and <code>RestLogicCoordinate</code>.
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.taxonomyCoordinatePathComponent)  
	public RestTaxonomyCoordinate getTaxonomyCoordinate(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);

		return new RestTaxonomyCoordinate(RequestInfo.get().getTaxonomyCoordinate());
	}

	/**
	 * 
	 * This method returns <code>RestStampCoordinate</code>.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinate corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinate specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinate will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestStampCoordinate
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.stampCoordinatePathComponent)  
	public RestStampCoordinate getStampCoordinate(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);

		return new RestStampCoordinate(RequestInfo.get().getStampCoordinate());
	}

	/**
	 * 
	 * This method returns <code>RestLanguageCoordinate</code>.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinate corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinate specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinate will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestLanguageCoordinate
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.languageCoordinatePathComponent)  
	public RestLanguageCoordinate getLanguageCoordinate(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);

		return new RestLanguageCoordinate(RequestInfo.get().getLanguageCoordinate());
	}

	/**
	 * 
	 * This method returns <code>RestLogicCoordinate</code>.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinate corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinate specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinate will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestLogicCoordinate
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.logicCoordinatePathComponent)  
	public RestLogicCoordinate getLogicCoordinate(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);

		return new RestLogicCoordinate(RequestInfo.get().getLogicCoordinate());
	}
	
	/**
	 * 
	 * This method returns <code>RestEditToken</code>.
	 * It takes an explicit serialized SSO string parameter <code>ssoToken</code>
	 * specifying authenticated user identification.
	 * Also accepts an optional editToken string parameter specifying all component values
	 * as well as optional individual editModule, editPath and wfProcessId parameters.
	 * If no optional parameters are specified,
	 * then the editToken corresponding to the passed <code>ssoToken</code> token will be returned.
	 * If any additional optional parameters are passed, then their values will be applied to the token specified by the
	 * explicit serialized ssoToken string, and the resulting RestEditToken will be returned.
	 * 
	 * @param ssoToken specifies an explicit serialized SSO token string
	 * 
	 * @return RestEditToken
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.editTokenComponent)  
	public RestEditToken getEditToken(
			@QueryParam(RequestParameters.ssoToken) String coordToken, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editToken) String editToken, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editModule) String editModule, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editPath) String editPath // Applied in RestContainerRequestFilter
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.ssoToken,
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);

		// All work is done in RequestInfo.get().getEditToken(), initially invoked by RestContainerRequestFilter

		return new RestEditToken(RequestInfo.get().getEditToken());
	}
}
