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
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;

/**
 * 
 * {@link RestConceptVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestConceptVersion
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The concept chronology for this concept.  Depending on the expand paramter, may be empty.
	 */
	@XmlElement
	RestConceptChronology conChronology;
	
	/**
	 * The StampedVersion details for this version of this concept.
	 */
	@XmlElement
	RestStampedVersion conVersion;
	
	/**
	 * The parent concepts(s) of the concept at this point in time (is a relationships)
	 */
	@XmlElement
	List<RestConceptVersion> parent;
	
	/**
	 * The child concepts(s) of the concept at this point in time (is a relationships)
	 */
	@XmlElement
	List<RestConceptVersion> child;
	
	protected RestConceptVersion()
	{
		//for Jaxb
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" }) 
	public RestConceptVersion(ConceptVersion cv, boolean includeChronology, boolean includeParents, boolean includeChildren)
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
				expandables.add(
						new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.conceptChronologyAppPathComponent + cv.getChronology().getConceptSequence()));
			}
			if (includeParents)
			{
				//TODO populate parents
				parent = new ArrayList<>();
			}
			else
			{
				expandables.add(
						new Expandable(ExpandUtil.parentsExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
								+ "?expand=" + ExpandUtil.parentsExpandable));
			}
			
			if (includeChildren)
			{
				//TODO populate children
				child = new ArrayList<>();
			}
			else
			{
				expandables.add(
						new Expandable(ExpandUtil.childrenExpandable,  RestPaths.conceptVersionAppPathComponent + cv.getChronology().getConceptSequence() 
								+ "?expand=" + ExpandUtil.childrenExpandable));
			}
			if (expandables.size() == 0)
			{
				expandables = null;
			}
		}
		else
		{
			expandables = new Expandables(
					new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.conceptChronologyAppPathComponent + cv.getChronology().getConceptSequence()),
					new Expandable(ExpandUtil.parentsExpandable,  RestPaths.conceptVersionComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.parentsExpandable),
					new Expandable(ExpandUtil.childrenExpandable,  RestPaths.conceptVersionComponent + cv.getChronology().getConceptSequence() 
							+ "?expand=" + ExpandUtil.childrenExpandable));
			conChronology = null;
			parent = null;
			child = null;
		}
	}
	
	public void addChild(RestConceptVersion child)
	{
		if (this.child == null)
		{
			this.child = new ArrayList<>();
		}
		this.child.add(child);
		if (expandables != null)
		{
			expandables.remove(ExpandUtil.childrenExpandable);
		}
	}
	
	public void addParent(RestConceptVersion parent)
	{
		if (this.parent == null)
		{
			this.parent = new ArrayList<>();
		}
		this.parent.add(parent);
		if (expandables != null)
		{
			expandables.remove(ExpandUtil.parentsExpandable);
		}
	}
}
