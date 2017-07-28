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

import java.util.ArrayList;
import java.util.List;
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
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersionPage;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;

/**
 * {@link TaxonomyAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.taxonomyAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.AUTOMATED, PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.ADMINISTRATOR, PrismeRoleConstants.READ_ONLY, PrismeRoleConstants.EDITOR, 
	PrismeRoleConstants.REVIEWER, PrismeRoleConstants.APPROVER, PrismeRoleConstants.DEPLOYMENT_MANAGER})
public class TaxonomyAPIs
{
	private static Logger log = LogManager.getLogger(TaxonomyAPIs.class);

	public final static int MAX_PAGE_SIZE_DEFAULT = 5000;
	public final static int PAGE_NUM_DEFAULT = 1;

	@Context
	private SecurityContext securityContext;

	/**
	 * Returns a single version of a concept, with parents and children expanded to the specified extent and levels.
	 * If no version parameter is specified, returns the latest version.
	 * Pagination parameters may restrict which children are returned, but only effect the direct children of the specified concept,
	 * and are ignored (defaulted) during populating children of children and descendants. 
	 * 
	 * When Parents and Children are returned, the order of the parents and children is alphabetical, HOWEVER if there are a large number
	 * of children - such that you only get back one page of children - only the returned page of children will be sorted.  The sort will 
	 * NOT be correct across multiple pages.  Each page will be sorted independently.  If the end user needs to display all children, sorted, 
	 * they will have to fetch all pages, and then sort.
	 * 
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
	 * @param terminologyType - when true, the concept sequences of the terminologies that this concept is part of on any stamp is returned.  This 
	 * is determined by whether or not there is version of this concept present with a module that extends from one of the children of the 
	 * {@link MetaData#MODULE} concepts.  This is returned as a set, as a concept may exist in multiple terminologies at the same time.
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology'.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be 
	 * obtained by a separate (prior) call to getCoordinatesToken().
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0, defaults to (MAX_PAGE_SIZE_DEFAULT==5000)
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
			@QueryParam(RequestParameters.terminologyType) @DefaultValue("false") String terminologyType,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(PAGE_NUM_DEFAULT + "") int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(MAX_PAGE_SIZE_DEFAULT + "") int maxPageSize) throws RestException
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
				RequestParameters.terminologyType,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.PAGINATION_PARAM_NAMES);

		boolean countChildrenBoolean = Boolean.parseBoolean(countChildren.trim());
		boolean countParentsBoolean = Boolean.parseBoolean(countParents.trim());
		boolean includeSememeMembership = Boolean.parseBoolean(sememeMembership.trim());
		boolean includeTerminologyType = Boolean.parseBoolean(terminologyType.trim());
		
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
					false, false, false, false, RequestInfo.get().getStated(), includeSememeMembership, includeTerminologyType,
					processIdUUID);
			
			Tree tree = Get.taxonomyService().getTaxonomyTree(RequestInfo.get().getTaxonomyCoordinate(RequestInfo.get().getStated()));
			
			if (parentHeight > 0)
			{
				addParents(concept.getConceptSequence(), rcv, tree, countParentsBoolean, parentHeight - 1, includeSememeMembership, 
						includeTerminologyType, new ConceptSequenceSet(), processIdUUID);
			}
			else if (countParentsBoolean)
			{
				countParents(concept.getConceptSequence(), rcv, tree, processIdUUID);
			}
			
			if (childDepth > 0)
			{
				addChildren(concept.getConceptSequence(), rcv, tree, countChildrenBoolean, countParentsBoolean, childDepth - 1, includeSememeMembership, 
						includeTerminologyType, new ConceptSequenceSet(), processIdUUID, pageNum, maxPageSize);
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

	/**
	 * @param conceptSequence
	 * @param children
	 * @param tree
	 * @param countLeafChildren
	 * @param countParents
	 * @param remainingChildDepth
	 * @param includeSememeMembership
	 * @param includeTerminologyType
	 * @param alreadyAddedChildren
	 * @param processId
	 * @param pageNum > 0
	 * @param maxPageSize > 0
	 */
	public static void addChildren(
			int conceptSequence,
			RestConceptVersion rcv,
			Tree tree,
			boolean countLeafChildren,
			boolean countParents,
			int remainingChildDepth,
			boolean includeSememeMembership,
			boolean includeTerminologyType,
			ConceptSequenceSet alreadyAddedChildren,
			UUID processId,
			int pageNum, // PAGE_NUM_DEFAULT == 1
			int maxPageSize) // MAX_PAGE_SIZE_DEFAULT == 5000
		throws RestException
	{
		if (pageNum < 1) {
			// Bad pageNum parameter value
			throw new RestException(RequestParameters.pageNum, pageNum + "", "pageNum (" + pageNum + ") should be >= 1");
		}
		if (maxPageSize < 1) {
			// Bad maxPageSize parameter value
			throw new RestException(RequestParameters.maxPageSize, maxPageSize + "", "maxPageSize (" + maxPageSize + ") should be >= 1");
		}

		if (alreadyAddedChildren.contains(conceptSequence)) {
			// Avoiding infinite loop
			log.warn("addChildren(" + conceptSequence + ") aborted potential infinite recursion");
			return;
		} else {
			alreadyAddedChildren.add(conceptSequence);
		}

		int childCount = 0;
		final int first = pageNum == 1 ? 0 : ((pageNum - 1) * maxPageSize + 1);
		final int last = pageNum * maxPageSize;
		final int[] totalChildrenSequences = tree.getChildrenSequences(conceptSequence);
		List<RestConceptVersion> children = new ArrayList<>();
		for (int childSequence : totalChildrenSequences)
		{
			childCount++;
			if (childCount < first) {
				// Ignore unrequested pages prior to requested page
				continue;
			} else if (childCount > last) {
				// Ignore unrequested pages subsequent to requested page
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
				log.error("Failed finding concept for child concept SEQ=" + childSequence + " of parent concept " + new RestIdentifiedObject(conceptSequence) 
					+ ". Not including child.", e);
				rcv.exceptionMessages.add("Error adding child concept SEQ=" + childSequence + " of parent concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
			}
			if (childConcept != null) {
				try {
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, 
							Util.getPreWorkflowStampCoordinate(processId, childConcept.getNid()));
					if (cv.isPresent())
					{
						//expand chronology of child even if unrequested, otherwise, you can't identify what the child is
						//TODO handle contradictions
						RestConceptVersion childVersion = new RestConceptVersion(cv.get().value(), true, false, countParents, false, false, RequestInfo.get().getStated(), 
								includeSememeMembership, includeTerminologyType, processId);
						children.add(childVersion);
						if (remainingChildDepth > 0)
						{
							addChildren(childConcept.getConceptSequence(), childVersion, tree, countLeafChildren, countParents, remainingChildDepth - 1, includeSememeMembership, 
									includeTerminologyType, alreadyAddedChildren, processId, 1, MAX_PAGE_SIZE_DEFAULT);
						}
						else if (countLeafChildren)
						{
							countChildren(childConcept.getConceptSequence(), childVersion, tree, processId);
						}
					}
				} catch (RestException | RuntimeException e) {
					rcv.exceptionMessages.add("Error adding child concept " + childConcept.getPrimordialUuid() + " of parent concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
					throw e;
				}
			}
		}
		
		final String baseUrl = RestPaths.taxonomyAPIsPathComponent + RestPaths.versionComponent + "?" + RequestParameters.id + "=" + conceptSequence;

		rcv.children = new RestConceptVersionPage(
				pageNum, maxPageSize,
				totalChildrenSequences.length,
				true,
				(last - first) < totalChildrenSequences.length,
				baseUrl,
				children.toArray(new RestConceptVersion[children.size()]));
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
				log.error("Unexpected error reading parent concept " + parentSequence + " of child concept " + new RestIdentifiedObject(conceptSequence) 
					+ ". Will not be included in count!", e);
				rcv.exceptionMessages.add("Error counting parent concept SEQ=" + parentSequence + " of child concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
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
					log.error("Unexpected error reading latest version of parent concept " + new RestIdentifiedObject(parentSequence) + " of child concept " 
				+ new RestIdentifiedObject(conceptSequence) + ". Will not be included in count!", e);
					rcv.exceptionMessages.add("Error counting latest version of parent concept SEQ=" + parentSequence + " of child concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
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
				log.error("Failed finding concept for child concept SEQ=" + childSequence + " of parent concept " + new RestIdentifiedObject(conceptSequence) 
					+ ". Not including child in count.", e);
				rcv.exceptionMessages.add("Error counting child concept SEQ=" + childSequence + " of parent concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
			}
			
			if (childConcept != null) {
				try {
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<ConceptVersionImpl>> cv = childConcept.getLatestVersion(ConceptVersionImpl.class, 
						Util.getPreWorkflowStampCoordinate(processId, childConcept.getNid()));
					if (cv.isPresent())
					{
						count++;
					}
				} catch (Exception e) {
					log.error("Failed finding latest version of child concept " + new RestIdentifiedObject(childSequence) + " of parent concept " 
				+ new RestIdentifiedObject(conceptSequence) + ". Not including child in count.", e);
					rcv.exceptionMessages.add("Error counting latest version of child concept SEQ=" + childSequence + " of parent concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
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
			boolean includeTerminologyType,
			ConceptSequenceSet handledConcepts,
			UUID processId)
	{
		if (handledConcepts.contains(conceptSequence)) {
			// Avoiding infinite loop
			String msg = "addParents(" + conceptSequence + ") aborted potential infinite recursion";
			log.warn(msg);
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
					log.error("Unexpected error reading parent concept " + parentSequence + " of child concept " + new RestIdentifiedObject(conceptSequence) 
							+ ". Will not be included in result!", e);
					rcv.exceptionMessages.add("Error reading parent concept SEQ=" + parentSequence + " of child concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
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
									includeSememeMembership, includeTerminologyType, processId);
							rcv.addParent(parentVersion);
							if (remainingParentDepth > 0)
							{
								addParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree, countLeafParents, remainingParentDepth - 1, includeSememeMembership, 
										includeTerminologyType, perParentHandledConcepts, processId);
							}
							else if (countLeafParents)
							{
								countParents(parentConceptChronlogy.getConceptSequence(), parentVersion, tree, processId);
							}
						}
					}
					catch (Exception e)
					{
						log.error("Unexpected error processing parent concept " + new RestIdentifiedObject(parentSequence) + " of child concept " 
								+ new RestIdentifiedObject(conceptSequence) + ". May not be included in result!", e);
						rcv.exceptionMessages.add("Error reading parent concept SEQ=" + parentSequence + " of child concept SEQ=" + conceptSequence + ": " + e.getLocalizedMessage());
					}
				}
				// Add perParentHandledConcepts concepts back to handledConcepts
				handledConcepts.addAll(perParentHandledConcepts.stream());
			}
		}
	}
}
