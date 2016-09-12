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
package gov.vha.isaac.rest.api1.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersions;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link ConceptAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.conceptAPIsPathComponent)
public class ConceptAPIs
{
	private static Logger log = LogManager.getLogger();
	
	private Set<Integer> allDescriptionAssemblageTypes = null;
	/**
	 * Returns a single version of a concept.
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param includeParents - Include the direct parent concepts of the requested concept in the response.  Defaults to false.
	 * @param countParents - true to count the number of parents above this node.  May be used with or without the includeParents parameter
	 *  - it works independently.  When used in combination with the parentHeight parameter, only the last level of items returned will return
	 *  parent counts.   This parameter also applies to the expanded children - if childDepth is requested, and countParents is set, this will 
	 *  return a count of parents of each child, which can be used to determine if a child has multiple parents.  Defaults to false if not provided.
	 * @param includeChildren - Include the direct child concepts of the request concept in the response.  Defaults to false. 
	 * @param countChildren - true to count the number of children below this node.  May be used with or without the includeChildren parameter
	 *  - it works independently.  When used in combination with the childDepth parameter, only the last level of items returned will return
	 *  child counts.  Defaults to false.  
	 * @param sememeMembership - when true, the sememeMembership field of the RestConceptVersion object will be populated with the set of unique
	 * concept sequences that describe sememes that this concept is referenced by.  (there exists a sememe instance where the referencedComponent 
	 * is the RestConceptVersion being returned here, then the value of the assemblage is also included in the RestConceptVersion)
	 * This will not include the membership information for any assemblage of type logic graph or descriptions.
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology'
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 *
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id + "}")
	public RestConceptVersion getConceptVersion(
			@PathParam(RequestParameters.id) String id, 
			@QueryParam(RequestParameters.includeParents) @DefaultValue("false") String includeParents,
			@QueryParam(RequestParameters.countParents) @DefaultValue("false") String countParents,
			@QueryParam(RequestParameters.includeChildren) @DefaultValue("false") String includeChildren,
			@QueryParam(RequestParameters.countChildren) @DefaultValue("false") String countChildren,
			@QueryParam(RequestParameters.sememeMembership) @DefaultValue("false") String sememeMembership,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.includeParents,
				RequestParameters.countParents,
				RequestParameters.includeChildren,
				RequestParameters.countChildren,
				RequestParameters.sememeMembership,
				RequestParameters.EXPANDABLES_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);

		@SuppressWarnings("rawtypes")
		ConceptChronology concept = findConceptChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
		if (cv.isPresent())
		{
			return new RestConceptVersion(cv.get().value(), 
					RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					Boolean.parseBoolean(includeParents.trim()),
					Boolean.parseBoolean(countParents.trim()), 
					Boolean.parseBoolean(includeChildren.trim()),
					Boolean.parseBoolean(countChildren.trim()),
					RequestInfo.get().getStated(),
					Boolean.parseBoolean(sememeMembership.trim()));
		}
		throw new RestException(RequestParameters.id, id, "No version on coordinate path for concept with the specified id");
	}
	
	/**
	 * Returns the chronology of a concept.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the concept chronology object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{" + RequestParameters.id + "}")
	public RestConceptChronology getConceptChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.EXPANDABLES_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ConceptChronology<? extends ConceptVersion<?>> concept = findConceptChronology(id);
		RestConceptChronology chronology =
				new RestConceptChronology(
						concept,
						RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));

		return chronology;
	}
	
	public static ConceptChronology<? extends ConceptVersion<?>> findConceptChronology(String id) throws RestException
	{
		ConceptService conceptService = Get.conceptService();
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = conceptService.getOptionalConcept(intId.get());
			if (c.isPresent())
			{
				return c.get();
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "No concept is available with the specified id");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = conceptService.getOptionalConcept(uuidId.get());
				if (c.isPresent())
				{
					return c.get();
				}
				else
				{
					throw new RestException(RequestParameters.id, id, "No concept is available with the specified id");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Is not a concept identifier.  Must be a UUID or an integer");
			}
		}
	}
	

	/**
	 * @param id - A UUID, nid, or concept sequence of a CONCEPT
	 * @param includeAttributes - true to include the (nested) attibutes, which includes the dialect information, false to ommit
	 * Dialects and other types of attributes will be returned in different structures - all attributes that represent dialects will 
	 * be in the RestSememeDescriptionVersion object, in the dialects fields, while any other type of attribute will be in the 
	 * RestSememeVersion in the nestedAttributes field. 
	 * @param expandReferenced - true to also include type information and preferred descriptions for concepts referenced by nested 
	 * sememes and dialects 
	 * @param expand - A comma separated list of fields to expand.  Supports 'referencedDetails'.
	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
	 * if they represent a concept or a description sememe.  
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return The descriptions associated with the concept
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionsComponent + "{" + RequestParameters.id + "}")
	public List<RestSememeDescriptionVersion> getDescriptions(
			@PathParam(RequestParameters.id) String id, 
			@QueryParam(RequestParameters.includeAttributes) @DefaultValue(RequestParameters.includeAttributesDefault) String includeAttributes,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.includeAttributes,
				RequestParameters.EXPANDABLES_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);

		ArrayList<RestSememeDescriptionVersion> result = new ArrayList<>();
		
		List<RestSememeVersion> descriptions = SememeAPIs.get(
				findConceptChronology(id).getNid() + "",
				getAllDescriptionTypes(),
				true, 
				Boolean.parseBoolean(includeAttributes.trim()),
				RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
				true);
		for (RestSememeVersion d : descriptions)
		{
			//This cast is expected to be safe, if not, the data model is messed up
			if (!(d instanceof RestSememeDescriptionVersion))
			{
				log.warn("SememeAPIs.get(...) didn't filter properly (encountered " + d.getClass().getName() + ")!  Is the DB broken again?");
			}
			else
			{
				result.add((RestSememeDescriptionVersion) d);
			}
		}
		return result;
	}
	// For testing only.  Returns RestSememeDescriptionVersions serializable by JAXB
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionsObjectComponent + "{" + RequestParameters.id + "}")
	public RestSememeDescriptionVersions getDescriptionVersions(
			@PathParam(RequestParameters.id) String id, 
			@QueryParam(RequestParameters.includeAttributes) @DefaultValue(RequestParameters.includeAttributesDefault) String includeAttributes,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		return new RestSememeDescriptionVersions(getDescriptions(id, includeAttributes, expand, coordToken));
	}
	
	private Set<Integer> getAllDescriptionTypes()
	{
		if (allDescriptionAssemblageTypes == null)
		{
			allDescriptionAssemblageTypes = Frills.getAllChildrenOfConcept(MetaData.DESCRIPTION_ASSEMBLAGE.getConceptSequence(), false, true);
		}
		return allDescriptionAssemblageTypes;
	}
}
