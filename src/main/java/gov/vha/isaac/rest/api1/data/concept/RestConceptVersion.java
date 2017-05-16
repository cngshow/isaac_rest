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
package gov.vha.isaac.rest.api1.data.concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.taxonomy.TaxonomyAPIs;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link RestConceptVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptVersion implements Comparable<RestConceptVersion>
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Expandables expandables;
	
	/**
	 * The concept chronology for this concept.  Depending on the expand parameter, may be empty.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestConceptChronology conChronology;
	
	/**
	 * The StampedVersion details for this version of this concept.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestStampedVersion conVersion;
	
	/**
	 * A boolean indicating whether the concept is fully-defined or primitive.  true for fully-defined, false for primitive
	 * This value is not populated / returned if the concept does not contain a logic graph from which to derive the information.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Boolean isConceptDefined;

	/**
	 * The parent concepts(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	List<RestConceptVersion> parents = new ArrayList<>();
	
	/**
	 * The child concepts(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	List<RestConceptVersion> children = new ArrayList<>();
	
	/**
	 * The number of child concept(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 * This will not be returned if the children field is populated.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Integer childCount;
	
	/**
	 * The number of parent concept(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 * This will not be returned if the parents field is populated.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Integer parentCount;
	
	/**
	 * The concept sequences of the sememe assemblage concepts that this concept is a member of (there exists a sememe instance where the referencedComponent 
	 * is this concept, and the assemblage is the value returned).  Note that this field is typically not populated - and when it is populated, it is only 
	 * in response to a request via the Taxonomy or Concept APIs, when the parameter 'sememeMembership=true' is passed.
	 * See more details on {@link TaxonomyAPIs#getConceptVersionTaxonomy(String, String, int, String, int, String, String, String)}
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Set<Integer> sememeMembership = new HashSet<>();
	

	/**
	 * The concept sequences of the terminologies that this concept is part of.  This is determined by whether or not there is version of this concept present 
	 * with a module that extends from one of the children of the {@link MetaData#MODULE} concepts.  Note that this field is typically not populated - and when it 
	 * is populated, it is only in response to a request via the Taxonomy or Concept APIs, when the parameter 'terminologyTypes=true' is passed.
	 * 
	 * See 1/system/TODO for more details on the potential terminology concepts that will be returned.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Set<Integer> terminologyTypes = new HashSet<>();
	
	protected RestConceptVersion()
	{
		//for Jaxb
	}
	
	@SuppressWarnings({ "rawtypes" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology, UUID processId) {
		this(cv, includeChronology, false, false, false, false, false, false, false, processId);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public RestConceptVersion(
			ConceptVersion cv, 
			boolean includeChronology,
			boolean includeParents,
			boolean countParents, 
			boolean includeChildren,
			boolean countChildren,
			boolean stated,
			boolean includeSememeMembership,
			boolean includeTerminologyType,
			final UUID processId)
	{
		conVersion = new RestStampedVersion(cv);
		
		Optional<SememeChronology<? extends SememeVersion<?>>> sememe = Get.sememeService().getSememesForComponentFromAssemblage(cv.getNid(), 
				(RequestInfo.get().getStated() ? 
						RequestInfo.get().getLogicCoordinate().getStatedAssemblageSequence() :
							RequestInfo.get().getLogicCoordinate().getInferredAssemblageSequence())).findAny();

		if (sememe.isPresent())
		{
			Optional<LatestVersion<LogicGraphSememe>> sv = ((SememeChronology)sememe.get()).getLatestVersion(LogicGraphSememe.class, 
					Util.getPreWorkflowStampCoordinate(processId, sememe.get().getNid()));
			if (sv.isPresent())
			{
				//TODO handle contradictions
				isConceptDefined = Frills.isConceptFullyDefined(sv.get().value());
			}
		}
		
		if (includeSememeMembership)
		{			
			Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
			{
				@Override
				public void accept(SememeChronology sc)
				{
					if (!sememeMembership.contains(sc.getAssemblageSequence()) 
						&& sc.getSememeType() != SememeType.LOGIC_GRAPH 
						&& sc.getSememeType() != SememeType.RELATIONSHIP_ADAPTOR
						&& sc.getSememeType() != SememeType.DESCRIPTION 
						&& sc.getLatestVersion(SememeVersionImpl.class, Util.getPreWorkflowStampCoordinate(processId, sc.getNid())).isPresent()) 
					{
						sememeMembership.add(sc.getAssemblageSequence());
					}
				}
			};
			
			Stream<SememeChronology<? extends SememeVersion<?>>> sememes = Get.sememeService().getSememesForComponent(cv.getNid());
			sememes.forEach(consumer);
		}
		else
		{
			sememeMembership = null;
		}
		
		if (includeTerminologyType)
		{
			HashSet<Integer> modules = new HashSet<>();
			cv.getChronology().getVersionStampSequences().forEach(stampSequence -> 
			{
				modules.add(Get.stampService().getModuleSequenceForStamp(stampSequence));
			});
			
			for (int moduleSequence : modules)
			{
				if (Get.taxonomyService().wasEverKindOf(moduleSequence, MetaData.MODULE.getConceptSequence()))
				{
					AtomicInteger previous = new AtomicInteger(0);
					OfInt parents = Get.taxonomyService().getTaxonomyParentSequences(moduleSequence).iterator();
					while (parents.hasNext())
					{
						int current = parents.nextInt();
						if (previous.get() != 0)
						{
							if (current == MetaData.MODULE.getConceptSequence())
							{
								terminologyTypes.add(previous.get());
								break;
							}
						}
						previous.set(current);
					};
				}
			}
		}
		else
		{
			terminologyTypes = null;
		}
		
		if (includeChronology || includeParents || includeChildren || countChildren || countParents)
		{
			expandables = new Expandables();
			if (includeChronology)
			{
				conChronology = new RestConceptChronology(cv.getChronology(), false, false, processId);
			}
			else
			{
				conChronology = null;
				if (RequestInfo.get().returnExpandableLinks())
				{
					expandables
						.add(new Expandable(ExpandUtil.chronologyExpandable, RestPaths.conceptChronologyAppPathComponent + cv.getChronology().getConceptSequence()));
				}
			}
			Tree tree = null;
			if (includeParents || includeChildren || countChildren || countParents)
			{
				tree = Get.taxonomyService().getTaxonomyTree(RequestInfo.get().getTaxonomyCoordinate(stated));
			}

			if (includeParents)
			{
				TaxonomyAPIs.addParents(cv.getChronology().getConceptSequence(), this, tree, countParents, 0, includeSememeMembership, includeTerminologyType, 
						new ConceptSequenceSet(), processId);
			}
			else if (countParents)
			{
				TaxonomyAPIs.countParents(cv.getChronology().getConceptSequence(), this, tree, processId);
			}

			if (includeChildren)
			{
				TaxonomyAPIs.addChildren(cv.getChronology().getConceptSequence(), this, tree, countChildren, countParents, 0, includeSememeMembership, 
						includeTerminologyType, new ConceptSequenceSet(), processId);
			}
			else if (countChildren)
			{
				TaxonomyAPIs.countChildren(cv.getChronology().getConceptSequence(), this, tree, processId);
			}
			
			if (includeParents || includeChildren)
			{
				sortParentsAndChildren();
			}
			
			if (expandables.size() == 0)
			{
				expandables = null;
			}
		}
		else
		{
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables = new Expandables(
					new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.conceptChronologyAppPathComponent + cv.getChronology().getConceptSequence()));
			}
			else
			{
				expandables = null;
			}
			conChronology = null;
			parents.clear();
			children.clear();
		}
	}
	
	public void addChild(RestConceptVersion child)
	{
		this.children.add(child);
	}
	
	public void addParent(RestConceptVersion parent)
	{
		this.parents.add(parent);
	}

	public void setChildCount(int count)
	{
		this.childCount = count;
	}
	
	public void setParentCount(int count)
	{
		this.parentCount = count;
	}
	
	public void sortParentsAndChildren()
	{
		if (parents.size() > 0)
		{
			Collections.sort(parents);
			for (RestConceptVersion rcv : parents)
			{
				rcv.sortParentsAndChildren();
			}
		}
		if (children.size() > 0)
		{
			Collections.sort(children);
			for (RestConceptVersion rcv : children)
			{
				rcv.sortParentsAndChildren();
			}
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RestConceptVersion o)
	{
		if (this.conChronology != null && o.conChronology != null)
		{
			return this.conChronology.compareTo(o.conChronology);
		}
		return 0;  //not really anything worth sorting on, if no chronology.
	}
	
	/**
	 * @return conVersion
	 */
	@XmlTransient
	public RestStampedVersion getConVersion() {
		return conVersion;
	}

	/**
	 * @return parents
	 */
	@XmlTransient
	public List<RestConceptVersion> getParents() {
		return Collections.unmodifiableList(parents);
	}

	/**
	 * @return conChronology
	 */
	@XmlTransient
	public RestConceptChronology getConChronology() {
		return conChronology;
	}

	/**
	 * This is an internal method, not part of the over the wire information.
	 * @return number of actual children, if present, otherwise, the value of the child count variable
	 */
	@XmlTransient
	public int getChildCount()
	{
		return (children == null  || children.size() == 0 ? (childCount == null ? 0 : childCount) : children.size());
	}

	/**
	 * @return the children
	 */
	@XmlTransient
	public List<RestConceptVersion> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * @return the sememeMembership
	 */
	@XmlTransient
	public Set<Integer> getSememeMembership() {
		return Collections.unmodifiableSet(sememeMembership);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestConceptVersion [conChronology=" + conChronology + ", conVersion=" + conVersion
				+ ", isConceptDefined=" + isConceptDefined + ", parents=" + parents + ", children=" + children
				+ ", childCount=" + childCount + ", parentCount=" + parentCount + ", sememeMembership="
				+ sememeMembership + "]";
	}
}
