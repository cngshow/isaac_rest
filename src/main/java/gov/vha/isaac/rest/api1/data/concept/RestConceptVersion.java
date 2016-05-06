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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptVersion
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The concept chronology for this concept.  Depending on the expand parameter, may be empty.
	 */
	@XmlElement
	RestConceptChronology conChronology;
	
	/**
	 * The StampedVersion details for this version of this concept.
	 */
	@XmlElement
	RestStampedVersion conVersion;

	/**
	 * The parent concepts(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 */
	@XmlElement
	List<RestConceptVersion> parents;
	
	/**
	 * The child concepts(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 */
	@XmlElement
	List<RestConceptVersion> children;
	
	/**
	 * The number of child concept(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 * This will not be returned if the children field is populated.
	 */
	@XmlElement
	Integer childCount;
	
	/**
	 * The number of parent concept(s) of the concept at this point in time ('is a' relationships).  Depending on the expand parameter, this may not be returned.
	 * This will not be returned if the parents field is populated.
	 */
	@XmlElement
	Integer parentCount;
	
	/**
	 * The concept sequences of the sememe assemblage concepts that this concept is a member of (there exists a sememe instance where the referencedComponent 
	 * is this concept, and the assemblage is the value returned).  Note that this field is typically not populated - and when it is populated, it is only 
	 * in response to a request via the Taxonomy or Concept APIs, when the parameter 'sememeMembership=true' is passed.
	 * See more details on {@link TaxonomyAPIs#getConceptVersionTaxonomy(String, String, int, String, int, String, String, String)}
	 */
	@XmlElement
	Set<Integer> sememeMembership;
	
	protected RestConceptVersion()
	{
		//for Jaxb
	}
	
	@SuppressWarnings({ "rawtypes" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology) {
		this(cv, includeChronology, false, false, false, false, false, false);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology, boolean includeParents, boolean countParents, 
			boolean includeChildren, boolean countChildren, boolean stated, boolean includeSememeMembership)
	{
		conVersion = new RestStampedVersion(cv);
		
		if (includeSememeMembership)
		{
			sememeMembership = new HashSet<>();
			
			Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
			{
				@Override
				public void accept(SememeChronology sc)
				{
					if (!sememeMembership.contains(sc.getAssemblageSequence()) 
						&& sc.getSememeType() != SememeType.LOGIC_GRAPH 
						&& sc.getSememeType() != SememeType.RELATIONSHIP_ADAPTOR
						&& sc.getSememeType() != SememeType.DESCRIPTION 
						&& sc.getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate()).isPresent()) 
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
		
		if (includeChronology || includeParents || includeChildren || countChildren || countParents)
		{
			expandables = new Expandables();
			if (includeChronology)
			{
				conChronology = new RestConceptChronology(cv.getChronology(), false, false);
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
				TaxonomyAPIs.addParents(cv.getChronology().getConceptSequence(), this, tree, countParents, 0, includeSememeMembership, new ConceptSequenceSet());
			}
			else if (countParents)
			{
				TaxonomyAPIs.countParents(cv.getChronology().getConceptSequence(), this, tree);
			}
			
			if (includeChildren)
			{
				TaxonomyAPIs.addChildren(cv.getChronology().getConceptSequence(), this, tree, countChildren, 0, includeSememeMembership, new ConceptSequenceSet());
			}
			else if (countChildren)
			{
				TaxonomyAPIs.countChildren(cv.getChronology().getConceptSequence(), this, tree);
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
			parents = null;
			children = null;
		}
	}
	
	public void addChild(RestConceptVersion child)
	{
		if (this.children == null)
		{
			this.children = new ArrayList<>();
		}
		this.children.add(child);
	}
	
	public void addParent(RestConceptVersion parent)
	{
		if (this.parents == null)
		{
			this.parents = new ArrayList<>();
		}
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
}
