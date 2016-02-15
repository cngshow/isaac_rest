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

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;


/**
 * {@link ConceptAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.conceptPathComponent)
public class ConceptAPIs
{
	
	/**
	 * Returns a single version of a concept.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'parents', 'children'
	 * @param stated - if expansion of parents or children is requested - should the stated or inferred taxonomy be used.  true for stated, false for inferred.
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{id}")
	public RestConceptVersion getConceptVersion(@PathParam("id") String id, @QueryParam("expand") String expand, 
		@QueryParam("stated") @DefaultValue("true") String stated) throws RestException
	{
		@SuppressWarnings("rawtypes")
		ConceptChronology concept = findConceptChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, StampCoordinates.getDevelopmentLatest());
		if (cv.isPresent())
		{
			RequestInfo ri = RequestInfo.init(expand);
			return new RestConceptVersion(cv.get().value(), 
					ri.shouldExpand(ExpandUtil.chronologyExpandable), 
					ri.shouldExpand(ExpandUtil.parentsExpandable), 
					ri.shouldExpand(ExpandUtil.childrenExpandable),
					Boolean.parseBoolean(stated.trim()));
		}
		throw new RestException("id", id, "No concept was found");
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
	public RestConceptChronology getConceptChronology(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		ConceptChronology<? extends ConceptVersion<?>> concept = findConceptChronology(id);
		RequestInfo ri = RequestInfo.init(expand);
		return new RestConceptChronology(concept, ri.shouldExpand(ExpandUtil.versionsAllExpandable), 
				ri.shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));
		
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
				throw new RestException("id", id, "No concept is available with the specified id");
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
					throw new RestException("id", id, "No concept is available with the specified id");
				}
			}
			else
			{
				throw new RestException("id", id, "Is not a concept identifier.  Must be a UUID or an integer");
			}
		}
	}
}
