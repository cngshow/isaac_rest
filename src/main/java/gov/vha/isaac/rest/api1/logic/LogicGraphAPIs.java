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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.logic;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * {@link LogicGraphAPIs}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a> 
 */

@Path(RestPaths.logicGraphAPIsPathComponent)
public class LogicGraphAPIs
{	
	private static Logger LOG = LogManager.getLogger();

	
	/**
	 * Returns a single version of a logic graph.
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence identifying the concept at the root of the logic graph
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'logicNodeUuids' and/or 'version'
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the logic graph version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id + "}")
	public RestSememeLogicGraphVersion getLogicGraphVersion(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		@SuppressWarnings("rawtypes")
		//TODO bug - the methods below findLogicGraphChronology are relying on some default logic graph coordiantes...  Also seems to be a lot 
		//of optional to not optional to optional stuff going on below this call... look at cleaning up.
		//See impl in RestConceptVersion constructor
		SememeChronology logicGraphSememeChronology = findLogicGraphChronology(
				id,
				RequestInfo.get().getStated(),
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getLanguageCoordinate(),
				RequestInfo.get().getLogicCoordinate());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<LogicGraphSememe>> lgs = logicGraphSememeChronology.getLatestVersion(LogicGraphSememe.class, RequestInfo.get().getStampCoordinate());
		if (lgs.isPresent())
		{
			return new RestSememeLogicGraphVersion(lgs.get().value(), RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable));
		}
		throw new RestException(RequestParameters.id, id, "No concept was found");
	}
	
	/**
	 * Returns the chronology of a logic graph.
	 * @param id - A UUID, nid, or concept sequence identifying the concept at the root of the logic graph
	 * @param expand - comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly', 'logicNodeUuids' and/or 'version'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the concept chronology object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{" + RequestParameters.id + "}")
	public RestSememeChronology getLogicGraphChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SememeChronology<? extends LogicGraphSememe<?>> logicGraphSememeChronology =
				findLogicGraphChronology(
						id,
						RequestInfo.get().getStated(),
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getLanguageCoordinate(),
						RequestInfo.get().getLogicCoordinate());
		
		return new RestSememeChronology(
				logicGraphSememeChronology,
				RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
				RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
				false, // LogicGraphSememe should not support nestedSememesExpandable
				false
				);
	}

	/**
	 * @param id - A UUID, nid, or concept sequence identifying the concept at the root of the logic graph
	 * @param stated - A boolean specifying whether to use the stated definition of the logic graph 
	 * @return - A LogicGraphSememe SememeChronology corresponding to the concept identified by the passed id
	 * @throws RestException
	 * 
	 * Returns the either the stated or inferred logic graph sememe chronology corresponding to the passed id
	 * 
	 * If the passed String id is an integer, it will be interpreted as the id of the referenced concept
	 * 
	 * If the passed String id is a UUID, it will be interpreted as the id of either the LogicGraphSememe or the referenced concept
	 */
	private static SememeChronology<? extends LogicGraphSememe<?>> findLogicGraphChronology(String id, boolean stated, StampCoordinate stampCoordinate, LanguageCoordinate languageCoordinate, LogicCoordinate logicCoordinate) throws RestException
	{
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			// id interpreted as the id of the referenced concept
			Optional<SememeChronology<? extends LogicGraphSememe<?>>> defChronologyOptional = Frills.getLogicGraphChronology(intId.get(), stated, stampCoordinate, languageCoordinate, logicCoordinate);
			if (defChronologyOptional.isPresent())
			{
				return defChronologyOptional.get();
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "No LogicGraph chronology is available for the concept with the specified id");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id interpreted as the id of either the LogicGraphSememe or the referenced concept
				int nidForUuid = Get.identifierService().getNidForUuids(uuidId.get());
				ObjectChronologyType typeOfPassedId = Get.identifierService().getChronologyTypeForNid(nidForUuid);
				
				int seqForUuid = 0;
				switch (typeOfPassedId) {
				case CONCEPT: {
					seqForUuid = Get.identifierService().getConceptSequenceForUuids(uuidId.get());
					break;
				}
				case SEMEME: {
					seqForUuid = Get.identifierService().getSememeSequenceForUuids(uuidId.get());
					break;
				}
				case UNKNOWN_NID:
				default:
					throw new RestException(RequestParameters.id, id, "LogicGraph chronology cannot be retrieved by id of unsupported ObjectChronologyType " + typeOfPassedId);
				}

				final Optional<? extends SememeChronology<? extends LogicGraphSememe<?>>> defChronologyOptional = Frills.getLogicGraphChronology(seqForUuid, stated, stampCoordinate, languageCoordinate, logicCoordinate);
				if (defChronologyOptional.isPresent())
				{
					LOG.debug("Used " + typeOfPassedId + " UUID " + uuidId.get() + " to retrieve LogicGraphSememe SememeChronology {}", () -> defChronologyOptional.get());
					return defChronologyOptional.get();
				}
				else
				{
					throw new RestException(RequestParameters.id, id, "No LogicGraph chronology is available for the specified " + typeOfPassedId + " UUID");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Is not a valid concept or sememe identifier.  Must be a UUID identifying a CONCEPT or SEMEME or an integer NID or SEQUENCE identifying a CONCEPT");
			}
		}
	}
}