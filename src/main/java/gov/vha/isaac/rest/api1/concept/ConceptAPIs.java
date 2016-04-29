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
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link ConceptAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.conceptPathComponent)
public class ConceptAPIs
{
	private static Logger log = LogManager.getLogger();
	
	private Set<Integer> allDescriptionAssemblageTypes = null;
	/**
	 * Returns a single version of a concept.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * 
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'parents', 'children', 'countChildren', 'countParents'
	 * @param stated - if expansion of parents or children is requested - should the stated or inferred taxonomy be used.  true for stated, false for inferred.
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id + "}")
	public RestConceptVersion getConceptVersion(
			@PathParam(RequestParameters.id) String id, 
			@QueryParam(RequestParameters.stated) @DefaultValue(RequestParameters.statedDefault) String stated,
			@QueryParam(RequestParameters.expand) String expand ) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		RequestInfo.get().readStated(stated);

		@SuppressWarnings("rawtypes")
		ConceptChronology concept = findConceptChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
		if (cv.isPresent())
		{
			return new RestConceptVersion(cv.get().value(), 
					RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.parentsExpandable),
					RequestInfo.get().shouldExpand(ExpandUtil.parentCountExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.childrenExpandable),
					RequestInfo.get().shouldExpand(ExpandUtil.childCountExpandable),
					Boolean.parseBoolean(stated.trim()));
		}
		throw new RestException(RequestParameters.id, id, "No version on coordinate path for concept with the specified id");
	}
	
	/**
	 * Returns the chronology of a concept.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * @return the concept chronology object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{id}")
	public RestConceptChronology getConceptChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand
			) throws RestException
	{
		RequestInfo.get().readExpandables(expand);

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
	 * if they represent a concept.
	 * @return The descriptions associated with the concept
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionsComponent + "{" + RequestParameters.id + "}")
	public List<RestSememeDescriptionVersion> getDescriptions(@PathParam(RequestParameters.id) String id, 
		@QueryParam("includeAttributes") @DefaultValue("true") String includeAttributes,
		@QueryParam("expand") String expand) throws RestException
	{
		ArrayList<RestSememeDescriptionVersion> result = new ArrayList<>();
		RequestInfo.get().readExpandables(expand);
		
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
				log.warn("SememeAPIs.get(...) didn't filter properly!  Is the DB broken again?");
			}
			else
			{
				result.add((RestSememeDescriptionVersion) d);
			}
		}
		return result;
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
