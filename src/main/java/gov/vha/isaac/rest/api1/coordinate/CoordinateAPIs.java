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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.coordinate.RestLanguageCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestLogicCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestStampCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.tokens.CoordinatesToken;


/**
 * {@link CoordinateAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.coordinatePathComponent)
public class CoordinateAPIs
{
	/**
	 * @param stated specifies premise/taxonomy type of <code>STATED</code> when true and <code>INFERRED</code> when false.
	 * 
	 * @param langCoordDescTypesPref specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
	 * @param langCoordDialectsPref specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
	 * @param langCoordLang specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
	 * 
	 * @param stampCoordModules specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
	 * @param stampCoordPath specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
	 * @param stampCoordPrecedence specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
	 * @param stampCoordStates specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".
	 * @param stampCoordTime specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".
	 * 
	 * @param logicCoordStated specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordInferred specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordDesc specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordClassifier specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * 
	 * @return RestCoordinatesToken
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.coordinatesTokenComponent)  
	public RestCoordinatesToken getCoordinatesToken() throws RestException
	{
		return new RestCoordinatesToken(new CoordinatesToken(RequestInfo.get().getTaxonomyCoordinate()));
	}

	/**
	 * @param stated specifies premise/taxonomy type of <code>STATED</code> when true and <code>INFERRED</code> when false.
	 * 
	 * @param langCoordDescTypesPref specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
	 * @param langCoordDialectsPref specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
	 * @param langCoordLang specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
	 * 
	 * @param stampCoordModules specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
	 * @param stampCoordPath specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
	 * @param stampCoordPrecedence specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
	 * @param stampCoordStates specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".
	 * @param stampCoordTime specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".
	 * 
	 * @param logicCoordStated specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordInferred specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordDesc specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordClassifier specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * 
	 * @return RestTaxonomyCoordinate
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.taxonomyCoordinatePathComponent)  
	public RestTaxonomyCoordinate getTaxonomyCoordinate() throws RestException
	{
		return new RestTaxonomyCoordinate(RequestInfo.get().getTaxonomyCoordinate());
	}

	/**
	 * @param stampCoordModules specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
	 * @param stampCoordPath specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
	 * @param stampCoordPrecedence specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
	 * @param stampCoordStates specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".
	 * @param stampCoordTime specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".
	 * 
	 * @return RestStampCoordinate
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.stampCoordinatePathComponent)  
	public RestStampCoordinate getStampCoordinate() throws RestException
	{
		return new RestStampCoordinate(RequestInfo.get().getStampCoordinate());
	}

	/**
	 * @param langCoordDescTypesPref specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
	 * @param langCoordDialectsPref specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
	 * @param langCoordLang specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
	 * 
	 * @return RestLanguageCoordinate
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.languageCoordinatePathComponent)  
	public RestLanguageCoordinate getLanguageCoordinate() throws RestException
	{
		return new RestLanguageCoordinate(RequestInfo.get().getLanguageCoordinate());
	}

	/**
	 * @param logicCoordStated specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordInferred specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordDesc specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * @param logicCoordClassifier specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
	 * 
	 * @return RestLogicCoordinate
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.logicCoordinatePathComponent)  
	public RestLogicCoordinate getLogicCoordinate() throws RestException
	{
		return new RestLogicCoordinate(RequestInfo.get().getLogicCoordinate());
	}
}
