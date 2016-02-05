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
package gov.vha.isaac.rest.api1.taxonomy;

import java.util.Optional;
import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;

/**
 * {@link TaxonomyAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.taxonomyPathComponent)
public class TaxonomyAPIs
{
	/**
	 * Returns a single version of a concept, with parents and children expanded to the specified levels.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence to center this taxonomy lookup on.
	 * @param parentHeight - How far to walk up the parent tree (this is applicable whether parents are expanded or not)
	 * @param childDepth - How far to walk up down the tree (this is applicable whether childrent are expanded or not)
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'parents', 'children'
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.conceptVersionComponent)
	public RestConceptVersion getConceptVersionTaxonomy(
			//ISAAC_Root - any variable ref here breaks the compiler and/or enunciate
			@QueryParam("id") @DefaultValue("cc0b2455-f546-48fa-90e8-e214cc8478d6") String id, 
			@QueryParam("parentHeight") @DefaultValue("0") int parentHeight, 
			@QueryParam("childDepth") @DefaultValue("1") int childDepth, 
			@QueryParam("expand") String expand) throws RestException
	{
		ConceptChronologyImpl concept = ConceptAPIs.findConceptChronology(id);
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, StampCoordinates.getDevelopmentLatest());
		if (cv.isPresent())
		{
			Set<String> expandables = ExpandUtil.read(expand);
			RestConceptVersion rcv = new RestConceptVersion(cv.get().value(), 
					expandables.contains(ExpandUtil.chronologyExpandable), 
					expandables.contains(ExpandUtil.parentsExpandable), 
					expandables.contains(ExpandUtil.childrenExpandable));
			
			TaxonomyCoordinate tc = TaxonomyCoordinates.getStatedTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest(), LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
			Tree tree = Get.taxonomyService().getTaxonomyTree(tc);
			if (parentHeight > 0)
			{
				addParents(concept.getConceptSequence(), rcv, expandables.contains(ExpandUtil.parentsExpandable), parentHeight - 1);
			}
			if (childDepth > 0)
			{
				addChildren(concept.getConceptSequence(), rcv, tree, expandables.contains(ExpandUtil.chronologyExpandable), 
						expandables.contains(ExpandUtil.childrenExpandable), childDepth - 1);
			}
			return rcv;
		}
		throw new RestException("id", id, "No concept was found");
	}

	private void addChildren(int conceptSequence, RestConceptVersion rcv, Tree tree, boolean includeChronology, boolean expandParents, int remainingChildDepth)
	{
		for (int childSequence : tree.getChildrenSequences(conceptSequence))
		{
			ConceptChronologyImpl childConcept;
			try
			{
				childConcept = ConceptAPIs.findConceptChronology(childSequence + "");
			}
			catch (RestException e)
			{
				throw new RuntimeException("Internal Error!", e);
			}
			Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, StampCoordinates.getDevelopmentLatest());
			if (cv.isPresent())
			{
				RestConceptVersion childVersion = new RestConceptVersion(cv.get().value(), includeChronology, false, false);
				rcv.addChild(childVersion);
				if (remainingChildDepth > 0)
				{
					addChildren(childConcept.getConceptSequence(), childVersion, tree, includeChronology, expandParents, remainingChildDepth - 1);
				}
			}
		}
	}

	private void addParents(int conceptSequence, RestConceptVersion rcv, boolean expandChildren, int remainingParentDepth)
	{
		//TODO implement lookup parents (psuedocode)
//		for (X parent : conceptSequence.getParents())
//		{
//			rcv.addParent(parent);
//			if (remainingParentDepth > 0)
//			{
//				addChildren(child, remainingParentDepth - 1);
//			}
//		}
		
	}
}
