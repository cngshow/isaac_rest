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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.coordinate.RestLanguageCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestLogicCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestStampCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link CoordinateAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.coordinatePathComponent)
public class CoordinateAPIs
{
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
		// All parameters, including defaults, are handled by the RestContainerRequestFilter
		return new RestCoordinatesToken(RequestInfo.get().getCoordinatesToken());
	}

	/**
	 * 
	 * This method returns a list of coordinates comprising all coordinate parameters.
	 * It takes an explicit serialized CoordinatesToken string parameter <code>coordToken</code>
	 * specifying all coordinate parameters in addition to all of the other coordinate-specific parameters.
	 * If no additional individual coordinate-specific parameters are specified,
	 * then the coordinates corresponding to the passed <code>coordToken</code> CoordinatesToken will be returned.
	 * If any additional individual parameters are passed, then their values will be applied to the coordinates specified by the
	 * explicit serialized CoordinatesToken string, and the resulting coordinates will be returned.
	 * 
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return List<Object> list of all coordinates.
	 * Note that <code>RestTaxonomyCoordinate</code> contains <code>RestStampCoordinate</code>, <code>RestLanguageCoordinate</code> and <code>RestLogicCoordinate</code>.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.coordinatesComponent)  
	public List<Object> getCoordinates(
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		List<Object> coordinates = new ArrayList<>();
		
		RestTaxonomyCoordinate taxonomyCoordinate = getTaxonomyCoordinate(coordToken);
		coordinates.add(taxonomyCoordinate);
		coordinates.add(taxonomyCoordinate.stampCoordinate);
		coordinates.add(taxonomyCoordinate.languageCoordinate);
		coordinates.add(taxonomyCoordinate.logicCoordinate);
		
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
		return new RestLogicCoordinate(RequestInfo.get().getLogicCoordinate());
	}
}
