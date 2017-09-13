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
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.RestEditToken;
import gov.vha.isaac.rest.api1.data.coordinate.RestCoordinates;
import gov.vha.isaac.rest.api1.data.coordinate.RestLanguageCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestLogicCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestStampCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.session.PrismeIntegratedUserService;
import gov.vha.isaac.rest.session.PrismeUserService;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.utils.SecurityUtils;


/**
 * {@link CoordinateAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.coordinateAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.AUTOMATED, PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.ADMINISTRATOR, PrismeRoleConstants.READ_ONLY, PrismeRoleConstants.EDITOR, PrismeRoleConstants.REVIEWER, PrismeRoleConstants.APPROVER, PrismeRoleConstants.DEPLOYMENT_MANAGER})
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
	 * @param time specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest" (case ignored).  The default is "latest".
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
	 * This method returns a <code>RestEditToken</code>, which is an encrypted String that is used internally
	 * to authenticate and convey user and session information between KOMET and PRISME.  Information conveyed includes
	 * user, module, and path concepts as well as, optionally, a workflow process.  Each EditToken expires after a set amount of time
	 * and is otherwise usable for a write operation exactly once, after which it becomes expired and unusable for further write operations.
	 * An expired token may be renewed by passing it as an editToken parameter to another getEditToken() call.
	 * 
	 * All write operations return a <code>RestWriteResponse</code> object containing a renewed, and therefore readily usable EditToken.
	 * 
	 * If a previously-retrieved editToken parameter is passed, it will be used. The editToken parameter is incompatible with ssoToken and userId parameters.
	 * If editToken is not passed and PRISME services are configured (prisme.properties exists and prisme_roles_by_token_url is set)
	 * then a valid SSO token string must be passed in the ssoToken parameter
	 * 
	 * ---------- DEVELOPER (Not Production) options below: --------------
	 * 
	 * If editToken is not passed and PRISME services are not configured and userId is passed then the userId parameter is parsed, to load a test user with all 
	 * roles, as an existing concept id (UUID, NID or concept sequence), a case-insensitive keyword "DEFAULT" or the FSN description of an existing user concept
	 * 
	 * If editToken is not passed and PRISME services are not configured and ssoToken is passed AND IT IS RUNNING AS A UNIT TEST UNDER src/test then the ssoToken 
	 * is parsed, to load or create a test user with name and roles specified by a string with the  syntax {name}:{role1}[{,role2}[{,role3}[...]]].  
	 * 
	 * If any additional optional parameters are passed, then their values will be applied to the token specified by the
	 * required parameters, and the resulting RestEditToken will be returned.
	 * 
	 *
	 * @param ssoToken specifies an explicit serialized SSO token string. Not valid with use of userId or editToken. 
	 * @param editToken - optional previously-retrieved editToken string encoding user, module, path concept ids and optional workflow process id. Not valid with use 
	 *     of ssoToken or userId.
	 * @param editModule - optional module concept id
	 * @param editPath - optional path concept id
	 * @param userId - optional test User id of an existing concept id (UUID, NID or concept sequence),
	 * a case-insensitive keyword "DEFAULT" or the exact FSN description of an existing user concept.
	 * The userId parameter is only valid if PRISME is NOT configured.  Not valid with use of ssoToken or editToken.
	 *
	 * @return RestEditToken
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.editTokenComponent)  
	public RestEditToken getEditToken(
			@QueryParam(RequestParameters.ssoToken) String ssoToken, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.userId) String userId, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editToken) String editToken, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editModule) String editModule, // Applied in RestContainerRequestFilter
			@QueryParam(RequestParameters.editPath) String editPath // Applied in RestContainerRequestFilter
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());
		RequestInfoUtils.validateIncompatibleParameters(RequestInfo.get().getParameters(), RequestParameters.editToken, RequestParameters.ssoToken, RequestParameters.userId);

		PrismeUserService userService = LookupService.getService(PrismeIntegratedUserService.class);
		if (! userService.usePrismeForRolesByToken()) {
			RequestParameters.validateParameterNamesAgainstSupportedNames(
					RequestInfo.get().getParameters(),
					RequestParameters.ssoToken,
					RequestParameters.userId,
					RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		} else {
			RequestParameters.validateParameterNamesAgainstSupportedNames(
					RequestInfo.get().getParameters(),
					RequestParameters.ssoToken,
					RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		}

		// All work is done in RequestInfo.get().getEditToken(), initially invoked by RestContainerRequestFilter

		return new RestEditToken(RequestInfo.get().getEditToken().renewToken());
	}
}
