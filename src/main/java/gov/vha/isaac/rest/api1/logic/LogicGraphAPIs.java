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

import javax.ws.rs.DefaultValue;
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
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;
import gov.vha.isaac.rest.api1.session.RequestParameters;

/**
 * {@link LogicGraphAPIs}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a> 
 */

@Path(RestPaths.logicGraphPathComponent)
public class LogicGraphAPIs
{	
	private static Logger LOG = LogManager.getLogger();

	/**
	 * Returns a single version of a logic graph.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence identifying the concept at the root of the logic graph
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'logicNodeUuids' and/or 'version'
	 * @param stated - if expansion of parents or children is requested - should the stated or inferred taxonomy be used.  true for stated, false for inferred.
	 * @return the logic graph version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id + "}")
	public RestSememeLogicGraphVersion getLogicGraphVersion(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand, 
			@QueryParam(RequestParameters.stated) @DefaultValue(RequestParameters.statedDefault) String stated) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		@SuppressWarnings("rawtypes")
		SememeChronology logicGraphSememeChronology = findLogicGraphChronology(id, Boolean.parseBoolean(stated.trim()));

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
	 * @return the concept chronology object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{" + RequestParameters.id + "}")
	public RestSememeChronology getLogicGraphChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand, 
			@QueryParam(RequestParameters.stated) @DefaultValue(RequestParameters.statedDefault) String stated) throws RestException
	{
		RequestInfo.get().readExpandables(expand);

		SememeChronology<? extends LogicGraphSememe<?>> logicGraphChronology = findLogicGraphChronology(id, Boolean.parseBoolean(stated.trim()));
		
		return new RestSememeChronology(
				logicGraphChronology,
				RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
				RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
				false // LogicGraphSememe should not support nestedSememesExpandable
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
	private static SememeChronology<? extends LogicGraphSememe<?>> findLogicGraphChronology(String id, boolean stated) throws RestException
	{
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			// id interpreted as the id of the referenced concept
			Optional<SememeChronology<? extends SememeVersion<?>>> defChronologyOptional = stated ? Get.statedDefinitionChronology(intId.get()) : Get.inferredDefinitionChronology(intId.get());
			if (defChronologyOptional.isPresent())
			{
				@SuppressWarnings("unchecked")
				SememeChronology<? extends LogicGraphSememe<?>> sememeChronology = (SememeChronology<? extends LogicGraphSememe<?>>)defChronologyOptional.get();
				LOG.debug("Used CONCEPT id " + intId.get() + " to retrieve LogicGraphSememe SememeChronology " + sememeChronology);

				return sememeChronology;
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
				
				Optional<? extends SememeChronology<? extends SememeVersion<?>>> defChronologyOptional = null;
				
				switch (typeOfPassedId) {
				case CONCEPT: {
					int seqForUuid = Get.identifierService().getConceptSequenceForUuids(uuidId.get());
					defChronologyOptional = stated ? Get.statedDefinitionChronology(seqForUuid) : Get.inferredDefinitionChronology(seqForUuid);

					
					break;
				}
				case SEMEME: {
					int seqForUuid = Get.identifierService().getSememeSequenceForUuids(uuidId.get());
					defChronologyOptional = Get.sememeService().getOptionalSememe(seqForUuid);

					break;
				}
				case UNKNOWN_NID:
				default:
					throw new RestException(RequestParameters.id, id, "LogicGraph chronology cannot be retrieved by id of unsupported ObjectChronologyType " + typeOfPassedId);
				}

				if (defChronologyOptional.isPresent())
				{
					@SuppressWarnings("unchecked")
					SememeChronology<? extends LogicGraphSememe<?>> sememeChronology = (SememeChronology<? extends LogicGraphSememe<?>>)defChronologyOptional.get();
					
					LOG.debug("Used " + typeOfPassedId + " UUID " + uuidId.get() + " to retrieve LogicGraphSememe SememeChronology " + sememeChronology);
					return sememeChronology;
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