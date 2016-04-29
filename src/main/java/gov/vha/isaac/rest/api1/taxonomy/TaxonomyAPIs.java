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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * {@link TaxonomyAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.taxonomyPathComponent)
public class TaxonomyAPIs
{
	private static Logger log = LogManager.getLogger();

	/**
	 * Returns a single version of a concept, with parents and children expanded to the specified levels.
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence to center this taxonomy lookup on.  If not provided, the default value 
	 * is the UUID for the ISAAC_ROOT concept.
	 * @param stated - true for stated, false for inferred
	 * @param parentHeight - How far to walk up (expand) the parent tree
	 * @param countParents - true to count the number of parents above this node.  May be used with or without the parentHeight parameter
	 *  - it works independently.  When used in combination with the parentHeight parameter, only the last level of items returned will return
	 *  parent counts.
	 * @param childDepth - How far to walk down (expand) the tree 
	 * @param countChildren - true to count the number of children below this node.  May be used with or without the childDepth parameter
	 *  - it works independently.  When used in combination with the childDepth parameter, only the last level of items returned will return
	 *  child counts.  
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology'.  
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent)
	public RestConceptVersion getConceptVersionTaxonomy(
			//ISAAC_Root - any variable ref here breaks the compiler and/or enunciate
			@QueryParam(RequestParameters.id) @DefaultValue("7c21b6c5-cf11-5af9-893b-743f004c97f5") String id,
			@QueryParam(RequestParameters.stated) @DefaultValue(RequestParameters.statedDefault) String stated, 
			@QueryParam("parentHeight") @DefaultValue("0") int parentHeight,
			@QueryParam("countParents") @DefaultValue("false") String countParents,
			@QueryParam("childDepth") @DefaultValue("1") int childDepth,
			@QueryParam("countChildren") @DefaultValue("false") String countChildren,
			@QueryParam(RequestParameters.expand) String expand
			) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		RequestInfo.get().readStated(stated);
		
		boolean countChildrenBoolean = Boolean.parseBoolean(countChildren.trim());
		boolean countParentsBoolean = Boolean.parseBoolean(countParents.trim());
		
		@SuppressWarnings("rawtypes")
		ConceptChronology concept = ConceptAPIs.findConceptChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
		if (cv.isPresent())
		{
			RestConceptVersion rcv = new RestConceptVersion(cv.get().value(), 
					RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					false, false, false, false, Boolean.parseBoolean(stated.trim()));  //parent / child expansion is handled here by providing a depth, not with expandables.
			
			Tree tree = Get.taxonomyService().getTaxonomyTree(RequestInfo.get().getTaxonomyCoordinate(RequestInfo.get().getStated()));
			
			if (parentHeight > 0)
			{
				addParents(concept.getConceptSequence(), rcv, tree, countParentsBoolean, parentHeight - 1, new ConceptSequenceSet());
			}
			else if (countParentsBoolean)
			{
				countParents(concept.getConceptSequence(), rcv, tree);
			}
			
			if (childDepth > 0)
			{
				addChildren(concept.getConceptSequence(), rcv, tree, countChildrenBoolean, childDepth - 1, new ConceptSequenceSet());
			}
			else if (countChildrenBoolean)
			{
				countChildren(concept.getConceptSequence(), rcv, tree);
			}
			return rcv;
		}
		throw new RestException("id", id, "No concept was found");
	}

	public static void addChildren(
			int conceptSequence,
			RestConceptVersion rcv,
			Tree tree,
			boolean countLeafChildren,
			int remainingChildDepth,
			ConceptSequenceSet alreadyAddedChildren)
	{
		if (alreadyAddedChildren.contains(conceptSequence)) {
			// Avoiding infinite loop
			log.warn("addChildren(" + conceptSequence + ") aborted potential infinite recursion");
			return;
		} else {
			alreadyAddedChildren.add(conceptSequence);
		}
		for (int childSequence : tree.getChildrenSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology childConcept;
			try
			{
				childConcept = ConceptAPIs.findConceptChronology(childSequence + "");
			}
			catch (RestException e)
			{
				throw new RuntimeException("Internal Error!", e);
			}
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			if (cv.isPresent())
			{
				//expand chronology of child even if unrequested, otherwise, you can't identify what the child is
				RestConceptVersion childVersion = new RestConceptVersion(cv.get().value(), true, false, false, false, false, RequestInfo.get().getStated());
				rcv.addChild(childVersion);
				if (remainingChildDepth > 0)
				{
					addChildren(childConcept.getConceptSequence(), childVersion, tree, countLeafChildren, remainingChildDepth - 1, alreadyAddedChildren);
				}
				else if (countLeafChildren)
				{
					countChildren(childConcept.getConceptSequence(), childVersion, tree);
				}
			}
		}
	}
	
	public static void countParents(int conceptSequence, RestConceptVersion rcv, Tree tree)
	{
		int count = 0;
		for (int parentSequence : tree.getParentSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology parentConcept;
			try
			{
				parentConcept = ConceptAPIs.findConceptChronology(parentSequence + "");
			}
			catch (RestException e)
			{
				throw new RuntimeException("Internal Error!", e);
			}
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> cv = parentConcept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			if (cv.isPresent())
			{
				count++;
			}
		}
		rcv.setParentCount(count);
	}
	
	
	public static void countChildren(int conceptSequence, RestConceptVersion rcv, Tree tree)
	{
		int count = 0;
		for (int childSequence : tree.getChildrenSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology childConcept;
			try
			{
				childConcept = ConceptAPIs.findConceptChronology(childSequence + "");
			}
			catch (RestException e)
			{
				throw new RuntimeException("Internal Error!", e);
			}
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			if (cv.isPresent())
			{
				count++;
			}
		}
		rcv.setChildCount(count);
	}

	public static void addParents(int conceptSequence, RestConceptVersion rcv, Tree tree, boolean countLeafParents, int remainingParentDepth, ConceptSequenceSet handledConcepts)
	{
		if (handledConcepts.contains(conceptSequence)) {
			// Avoiding infinite loop
			log.warn("addParents(" + conceptSequence + ") aborted potential infinite recursion");
			return;
		} else {
			handledConcepts.add(conceptSequence);
		}
		for (int parentSequence : tree.getParentSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology parentConceptChronlogy;
			try
			{
				parentConceptChronlogy = ConceptAPIs.findConceptChronology(parentSequence + "");
			}
			catch (RestException e)
			{
				throw new RuntimeException("Internal Error!", e);
			}
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> cv = parentConceptChronlogy.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			if (cv.isPresent())
			{
				//expand chronology of the parent even if unrequested, otherwise, you can't identify what the child is
				RestConceptVersion parentVersion = new RestConceptVersion(cv.get().value(),true, false, false, false, false, RequestInfo.get().getStated());
				rcv.addParent(parentVersion);
				if (remainingParentDepth > 0)
				{
					addParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree, countLeafParents, remainingParentDepth - 1, handledConcepts);
				}
				else if (countLeafParents)
				{
					countParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree);
				}
			}
		}
	}
}
