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
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;


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
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.conceptVersionComponent + "{id}")
	public RestConceptVersion getConceptVersion(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		ConceptChronologyImpl concept = findConceptChronology(id);
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, StampCoordinates.getDevelopmentLatest());
		if (cv.isPresent())
		{
			Set<String> expandables = ExpandUtil.read(expand);
			return new RestConceptVersion(cv.get().value(), 
					expandables.contains(ExpandUtil.chronologyExpandable), 
					expandables.contains(ExpandUtil.parentsExpandable), 
					expandables.contains(ExpandUtil.childrenExpandable));
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
	@Path(RestPaths.conceptChronologyComponent + "{id}")
	public RestConceptChronology getConceptChronology(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		ConceptChronologyImpl concept = findConceptChronology(id);
		Set<String> expandables = ExpandUtil.read(expand);
		return new RestConceptChronology(concept, expandables.contains(ExpandUtil.versionsAllExpandable), 
				expandables.contains(ExpandUtil.versionsLatestOnlyExpandable));
		
	}
	
	public static ConceptChronologyImpl findConceptChronology(String id) throws RestException
	{
		ConceptService conceptService = Get.conceptService();
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			return (ConceptChronologyImpl) conceptService.getConcept(intId.get());
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				return (ConceptChronologyImpl) conceptService.getConcept(uuidId.get());
			}
			else
			{
				throw new RestException("id", id, "Is not a concept identifier.  Must be a UUID or an integer");
			}
		}
	}
}
