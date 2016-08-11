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
package gov.vha.isaac.rest.api1.mapping;

import java.util.ArrayList;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersions;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersions;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link MappingAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.mappingAPIsPathComponent)
public class MappingAPIs
{
	/**
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the mapping set versions object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetsComponent)
	public RestMappingSetVersions getMappingSets(
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestMappingSetVersion> results = new ArrayList<>();
		
		Get.sememeService().getSememesFromAssemblage(IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).forEach(sememeC -> 
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememeC).getLatestVersion(DynamicSememe.class, RequestInfo.get().getStampCoordinate());
			
			if (latest.isPresent())
			{
				//TODO handle contradictions properly
				results.add(new RestMappingSetVersion(latest.get().value(), RequestInfo.get().getStampCoordinate()));
			}
		});
		return new RestMappingSetVersions(results);
	}
	
	/**
	 * @param id - A UUID, nid, or concept sequence that identifies the map set.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the mapping set version object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + "{" + RequestParameters.id +"}")
	public RestMappingSetVersion getMappingSet(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES);

		Optional<SememeChronology<? extends SememeVersion<?>>> sememe = Get.sememeService().getSememesForComponentFromAssemblage(ConceptAPIs.findConceptChronology(id).getNid(), 
			IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).findAny();

		if (! sememe.isPresent()) {
			throw new RestException("The map set identified by '" + id + "' is not present at the given stamp");
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememe.get()).getLatestVersion(DynamicSememe.class, 
				RequestInfo.get().getStampCoordinate());
		if (latest.isPresent())
		{
			return new RestMappingSetVersion(latest.get().value(), RequestInfo.get().getStampCoordinate());
		} else {
			throw new RestException("The map set identified by '" + id + "' is not present at the given stamp");
		}
	}
	
	
	/**
	 * @param id - A UUID, nid, or concept sequence that identifies the map set to list items for.  Should be from {@link RestMappingSetVersion#identifiers}}
	 * @param expand - A comma separated list of fields to expand.  Supports 'referencedDetails'.  When referencedDetails is passed, descriptions
	 * will be included for all referenced concepts which align with your current coordinates.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the mapping items versions object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemsComponent + "{" + RequestParameters.id +"}")
	public RestMappingItemVersions getMappingItems(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.expand) String expand,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		//TODO not sure if we will need to page these
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestMappingItemVersion> results = new ArrayList<>();
		
		Integer extendedFieldsType = getMappingSet(id, coordToken).mapItemExtendedFieldsType;
		
		Get.sememeService().getSememesFromAssemblage(Util.convertToConceptSequence(id)).forEach(sememeC -> 
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememeC).getLatestVersion(DynamicSememe.class, RequestInfo.get().getStampCoordinate());
			
			if (latest.isPresent())
			{
				//TODO figure out how to handle contradictions!
				results.add(new RestMappingItemVersion(((DynamicSememe<?>)latest.get().value()), RequestInfo.get().getStampCoordinate(), 
					extendedFieldsType, RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
			}
		});
		return new RestMappingItemVersions(results);
	}
}
