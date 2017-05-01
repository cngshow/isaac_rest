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
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;

/**
 * {@link TaxonomyAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.taxonomyAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class TaxonomyAPIs
{
	private static Logger log = LogManager.getLogger(TaxonomyAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * Returns a single version of a concept, with parents and children expanded to the specified levels.
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence to center this taxonomy lookup on.  If not provided, the default value 
	 * is the UUID for the ISAAC_ROOT concept.
	 * @param parentHeight - How far to walk up (expand) the parent tree
	 * @param countParents - true to count the number of parents above this node.  May be used with or without the parentHeight parameter
	 *  - it works independently.  When used in combination with the parentHeight parameter, only the last level of items returned will return
	 *  parent counts.  This parameter also applies to the expanded children - if childDepth is requested, and countParents is set, this will 
	 *  return a count of parents of each child, which can be used to determine if a child has multiple parents.
	 * @param childDepth - How far to walk down (expand) the tree 
	 * @param countChildren - true to count the number of children below this node.  May be used with or without the childDepth parameter
	 *  - it works independently.  When used in combination with the childDepth parameter, only the last level of items returned will return
	 *  child counts.  
	 * @param sememeMembership - when true, the sememeMembership field of the RestConceptVersion object will be populated with the set of unique
	 * concept sequences that describe sememes that this concept is referenced by.  (there exists a sememe instance where the referencedComponent 
	 * is the RestConceptVersion being returned here, then the value of the assemblage is also included in the RestConceptVersion).
	 * This will not include the membership information for any assemblage of type logic graph or descriptions.
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology'.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the concept version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent)
	public RestConceptVersion getConceptVersionTaxonomy(
			//ISAAC_Root - any variable ref here breaks the compiler and/or enunciate
			@QueryParam(RequestParameters.id) @DefaultValue(RequestParameters.ISAAC_ROOT_UUID) String id,
			@QueryParam(RequestParameters.parentHeight) @DefaultValue("0") int parentHeight,
			@QueryParam(RequestParameters.countParents) @DefaultValue("false") String countParents,
			@QueryParam(RequestParameters.childDepth) @DefaultValue("1") int childDepth,
			@QueryParam(RequestParameters.countChildren) @DefaultValue("false") String countChildren,
			@QueryParam(RequestParameters.sememeMembership) @DefaultValue("false") String sememeMembership,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.parentHeight,
				RequestParameters.countParents,
				RequestParameters.childDepth,
				RequestParameters.countChildren,
				RequestParameters.sememeMembership,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);

		boolean countChildrenBoolean = Boolean.parseBoolean(countChildren.trim());
		boolean countParentsBoolean = Boolean.parseBoolean(countParents.trim());
		boolean includeSememeMembership = Boolean.parseBoolean(sememeMembership.trim());
		
		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		
		@SuppressWarnings("rawtypes")
		ConceptChronology concept = ConceptAPIs.findConceptChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<ConceptVersionImpl>> cv = concept.getLatestVersion(ConceptVersionImpl.class, 
				Util.getPreWorkflowStampCoordinate(processId, concept.getNid()));
		if (cv.isPresent())
		{
			//parent / child expansion is handled here by providing a depth, not with expandables.
			//TODO handle contradictions
			RestConceptVersion rcv = new RestConceptVersion(cv.get().value(), 
					RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					false, false, false, false, RequestInfo.get().getStated(), includeSememeMembership,
					processIdUUID);  
			
			Tree tree = Get.taxonomyService().getTaxonomyTree(RequestInfo.get().getTaxonomyCoordinate(RequestInfo.get().getStated()));
			
			if (parentHeight > 0)
			{
				addParents(concept.getConceptSequence(), rcv, tree, countParentsBoolean, parentHeight - 1, includeSememeMembership, new ConceptSequenceSet(), processIdUUID);
			}
			else if (countParentsBoolean)
			{
				countParents(concept.getConceptSequence(), rcv, tree, processIdUUID);
			}
			
			if (childDepth > 0)
			{
				addChildren(concept.getConceptSequence(), rcv, tree, countChildrenBoolean, countParentsBoolean, childDepth - 1, includeSememeMembership, 
						new ConceptSequenceSet(), processIdUUID);
			}
			else if (countChildrenBoolean)
			{
				countChildren(concept.getConceptSequence(), rcv, tree, processIdUUID);
			}
			rcv.sortParentsAndChildren();
			return rcv;
		}
		throw new RestException(RequestParameters.id, id, "No concept was found");
	}

	public static void addChildren(
			int conceptSequence,
			RestConceptVersion rcv,
			Tree tree,
			boolean countLeafChildren,
			boolean countParents,
			int remainingChildDepth,
			boolean includeSememeMembership,
			ConceptSequenceSet alreadyAddedChildren,
			UUID processId)
	{
		if (alreadyAddedChildren.contains(conceptSequence)) {
			// Avoiding infinite loop
			log.warn("addChildren(" + conceptSequence + ") aborted potential infinite recursion");
			return;
		} else {
			alreadyAddedChildren.add(conceptSequence);
		}
		//TODO 3 we need to guard against very large result returns - we must cap this, and, ideally, introduce paging, 
		//or something along those lines to handle very large result sets.
		for (int childSequence : tree.getChildrenSequences(conceptSequence))
		{
			if (rcv.getChildCount() > 5000)
			{
				log.warn("Limiting the number of taxonomy children under concept " + Frills.getIdInfo(childSequence));
				break;
			}
			@SuppressWarnings("rawtypes")
			ConceptChronology childConcept = null;
			try
			{
				childConcept = ConceptAPIs.findConceptChronology(childSequence + "");
			}
			catch (RestException e)
			{
				log.error("Failed finding concept for child concept SEQ=" + childSequence + " of parent concept " + Frills.getIdInfo(conceptSequence) + ". Not including child.", e);
				//throw new RuntimeException("Internal Error!", e);
			}
			if (childConcept != null) {
				@SuppressWarnings("unchecked")
				Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, 
						Util.getPreWorkflowStampCoordinate(processId, childConcept.getNid()));
				if (cv.isPresent())
				{
					//expand chronology of child even if unrequested, otherwise, you can't identify what the child is
					//TODO handle contradictions
					RestConceptVersion childVersion = new RestConceptVersion(cv.get().value(), true, false, countParents, false, false, RequestInfo.get().getStated(), 
							includeSememeMembership, processId);
					rcv.addChild(childVersion);
					if (remainingChildDepth > 0)
					{
						addChildren(childConcept.getConceptSequence(), childVersion, tree, countLeafChildren, countParents, remainingChildDepth - 1, includeSememeMembership, 
								alreadyAddedChildren, processId);
					}
					else if (countLeafChildren)
					{
						countChildren(childConcept.getConceptSequence(), childVersion, tree, processId);
					}
				}
			}
		}
	}
	
	public static void countParents(int conceptSequence, RestConceptVersion rcv, Tree tree, UUID processId)
	{
		int count = 0;
		for (int parentSequence : tree.getParentSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology parentConcept = null;
			try
			{
				parentConcept = ConceptAPIs.findConceptChronology(parentSequence + "");
			}
			catch (RestException e)
			{
				log.error("Unexpected error reading parent concept " + parentSequence + " of child concept " + Frills.getIdInfo(conceptSequence) + ". Will not be included in count!", e);
			}
			
			if (parentConcept != null) {
				try {
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<ConceptVersionImpl>> cv = parentConcept.getLatestVersion(ConceptVersionImpl.class, 
							Util.getPreWorkflowStampCoordinate(processId, parentConcept.getNid()));
					if (cv.isPresent())
					{
						count++;
					}
				} catch (Exception e) {
					log.error("Unexpected error reading latest version of parent concept " + Frills.getIdInfo(parentSequence) + " of child concept " + Frills.getIdInfo(conceptSequence) + ". Will not be included in count!", e);
				}
			}
		}
		rcv.setParentCount(count);
	}
	
	
	public static void countChildren(int conceptSequence, RestConceptVersion rcv, Tree tree, UUID processId)
	{
		int count = 0;
		for (int childSequence : tree.getChildrenSequences(conceptSequence))
		{
			@SuppressWarnings("rawtypes")
			ConceptChronology childConcept = null;
			try
			{
				childConcept = ConceptAPIs.findConceptChronology(childSequence + "");
			}
			catch (Exception e)
			{
				log.error("Failed finding concept for child concept SEQ=" + childSequence + " of parent concept " + Frills.getIdInfo(conceptSequence) + ". Not including child in count.", e);
				//throw new RuntimeException("Internal Error!", e);
			}
			
			if (childConcept != null) {
				try {
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, Util.getPreWorkflowStampCoordinate(processId, childConcept.getNid()));
					if (cv.isPresent())
					{
						count++;
					}
				} catch (Exception e) {
					log.error("Failed finding latest version of child concept " + Frills.getIdInfo(childSequence) + " of parent concept " + Frills.getIdInfo(conceptSequence) + ". Not including child in count.", e);
				}
			}
		}
		rcv.setChildCount(count);
	}

	public static void addParents(
			int conceptSequence,
			RestConceptVersion rcv,
			Tree tree,
			boolean countLeafParents,
			int remainingParentDepth, 
			boolean includeSememeMembership,
			ConceptSequenceSet handledConcepts,
			UUID processId)
	{
		if (handledConcepts.contains(conceptSequence)) {
			// Avoiding infinite loop
			log.warn("addParents(" + conceptSequence + ") aborted potential infinite recursion");
			return;
		} else if (tree.getParentSequences(conceptSequence).length == 0) {
			// If no parents, just add self
			handledConcepts.add(conceptSequence);
		} else {
			ConceptSequenceSet passedHandledConcepts = new ConceptSequenceSet();
			passedHandledConcepts.addAll(handledConcepts.stream());
			
			for (int parentSequence : tree.getParentSequences(conceptSequence))
			{
				// create a new perParentHandledConcepts for each parent
				ConceptSequenceSet perParentHandledConcepts = new ConceptSequenceSet();
				perParentHandledConcepts.add(conceptSequence);
				perParentHandledConcepts.addAll(passedHandledConcepts.stream());

				@SuppressWarnings("rawtypes")
				ConceptChronology parentConceptChronlogy = null;
				try
				{
					parentConceptChronlogy = ConceptAPIs.findConceptChronology(parentSequence + "");
				}
				catch (Exception e) {
					log.error("Unexpected error reading parent concept " + parentSequence + " of child concept " + Frills.getIdInfo(conceptSequence) + ". Will not be included in result!", e);
				}
				
				//if error is caught above parentConceptChronlogy will be null and not usable in the block below
				if (parentConceptChronlogy != null) {
					try {
						@SuppressWarnings("unchecked")
						Optional<LatestVersion<ConceptVersionImpl>> cv = parentConceptChronlogy.getLatestVersion(ConceptVersionImpl.class, 
								Util.getPreWorkflowStampCoordinate(processId, parentConceptChronlogy.getNid()));
						
						if (cv.isPresent())
						{
							//expand chronology of the parent even if unrequested, otherwise, you can't identify what the child is
							//TODO handle contradictions
							RestConceptVersion parentVersion = new RestConceptVersion(cv.get().value(), true, false, false, false, false, RequestInfo.get().getStated(), 
									includeSememeMembership, processId);
							rcv.addParent(parentVersion);
							if (remainingParentDepth > 0)
							{
								addParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree, countLeafParents, remainingParentDepth - 1, includeSememeMembership, 
										perParentHandledConcepts, processId);
							}
							else if (countLeafParents)
							{
								countParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree, processId);
							}
						}
					}
					catch (Exception e)
					{
						log.error("Unexpected error processing parent concept " + Frills.getIdInfo(parentSequence) + " of child concept " + Frills.getIdInfo(conceptSequence) + ". May not be included in result!", e);
					}
				}
				// Add perParentHandledConcepts concepts back to handledConcepts
				handledConcepts.addAll(perParentHandledConcepts.stream());
			}
		}
	}
}
