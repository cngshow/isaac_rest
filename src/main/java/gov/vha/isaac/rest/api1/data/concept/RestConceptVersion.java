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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.tree.Tree;
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
	
	protected RestConceptVersion()
	{
		//for Jaxb
	}
	
	@SuppressWarnings({ "rawtypes" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology) {
		this(cv, includeChronology, false, false, false, false);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology, boolean includeParents, boolean includeChildren, boolean countChildren, boolean stated)
	{
		conVersion = new RestStampedVersion(cv);
		if (includeChronology || includeParents || includeChildren)
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
			if (includeParents || includeChildren || countChildren)
			{
				tree = Get.taxonomyService().getTaxonomyTree(RequestInfo.get().getTaxonomyCoordinate(stated));
			}
			if (includeParents)
			{
				TaxonomyAPIs.addParents(cv.getChronology().getConceptSequence(), this, tree, 0);
			}
			else
			{
				if (RequestInfo.get().returnExpandableLinks())
				{
					expandables.add(
						new Expandable(ExpandUtil.parentsExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.parentsExpandable + "&stated=" + stated));
				}
			}
			
			if (includeChildren)
			{
				TaxonomyAPIs.addChildren(cv.getChronology().getConceptSequence(), this, tree, countChildren, 0);
			}
			else
			{
				if (RequestInfo.get().returnExpandableLinks())
				{
					expandables.add(
						new Expandable(ExpandUtil.childrenExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.childrenExpandable + "&stated=" + stated));
				}
				if (countChildren)
				{
					TaxonomyAPIs.countChildren(cv.getChronology().getConceptSequence(), this, tree);
				}
				else
				{
					if (RequestInfo.get().returnExpandableLinks())
					{
						expandables.add(
							new Expandable(ExpandUtil.childCountExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
								+ "?expand=" + ExpandUtil.childCountExpandable + "&stated=" + stated));
					}
				}
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
					new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.conceptChronologyAppPathComponent + cv.getChronology().getConceptSequence()),
					new Expandable(ExpandUtil.parentsExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.parentsExpandable + "&stated=" + stated),
					new Expandable(ExpandUtil.childrenExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.childrenExpandable + "&stated=" + stated));
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
		if (expandables != null)
		{
			expandables.remove(ExpandUtil.childrenExpandable);
		}
	}
	
	public void addParent(RestConceptVersion parent)
	{
		if (this.parents == null)
		{
			this.parents = new ArrayList<>();
		}
		this.parents.add(parent);
		if (expandables != null)
		{
			expandables.remove(ExpandUtil.parentsExpandable);
		}
	}

	public void setChildCount(int count)
	{
		this.childCount = count;
	}
}
