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
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;

/**
 * 
 * {@link RestConceptChronology}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestConceptChronology 
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The identifier data for the object
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	/**
	 * The list of concept versions.  Depending on the expand parameter, may be empty, the latest only, or all versions.
	 */
	@XmlElement
	List<RestConceptVersion> versions;
	
	protected RestConceptChronology()
	{
		//for JaxB
	}
	
	@SuppressWarnings("rawtypes") 
	public RestConceptChronology(ConceptChronology<? extends ConceptVersion> cc, boolean includeAllVersions, boolean includeLatestVersion)
	{
		identifiers = new RestIdentifiedObject(cc.getUuidList());
		if (includeAllVersions || includeLatestVersion)
		{
			expandables = null;
			versions = new ArrayList<RestConceptVersion>();
			if (includeAllVersions)
			{
				for (ConceptVersion cv : cc.getVersionList())
				{
					versions.add(new RestConceptVersion(cv, false, false, false));
				}
			}
			else 
			{
				//TODO implement
				throw new RuntimeException("Latest version not yet implemented");
			}
		}
		else
		{
			versions = null;
			expandables = new Expandables(
					new Expandable(ExpandUtil.versionsAllExpandable,
							RestPaths.conceptVersionsAppPathComponent + cc.getConceptSequence() + "/"), 
					new Expandable(ExpandUtil.versionsLatestOnlyExpandable,
							RestPaths.conceptVersionComponent + cc.getConceptSequence() + "/"));
		}
	}
}
