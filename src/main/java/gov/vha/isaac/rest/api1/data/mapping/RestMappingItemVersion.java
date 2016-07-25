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
package gov.vha.isaac.rest.api1.data.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.mapping.data.MappingItem;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link RestMappingItemVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingItemVersion implements Comparable<RestMappingItemVersion>
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
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this mapping entry
	 */
	@XmlElement
	RestStampedVersion mappingItemStamp;
	
	
	/**
	 * The concept sequence that identifies the map set that this entry belongs to
	 */
	@XmlElement
	public int mapSetConcept;
	
	/**
	 * The source concept sequence being mapped by this map item
	 */
	@XmlElement
	public int sourceConcept;
	
	/**
	 * The target concept sequence being mapped by this map item.  This field is optional, and may be blank, if no target mapping
	 * is available.
	 */
	@XmlElement
	public Integer targetConcept;
	

	/**
	 * An (optional) concept sequence used to qualify this mapping entry 
	 */
	@XmlElement
	public Integer qualifierConcept;
	
	/**
	 * An (optional) description of the {@link #mapSetConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String mapSetDescription;
	
	/**
	 * An (optional) description of the {@link #sourceConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String sourceDescription;
	
	/**
	 * An (optional) description of the {@link #targetConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String targetDescription;
	
	/**
	 * An (optional) description of the {@link #qualifierConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String qualifierDescription;
	
		
	protected RestMappingItemVersion()
	{
		//for Jaxb
	}

//	
//	- A comma separated list of fields to expand.  Supports 'referencedDetails'.
//	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
//	 * if they represent a concept or a description sememe.  
	 
	public RestMappingItemVersion(MappingItem mappingItem, boolean expandDescriptions)
	{
		identifiers = new RestIdentifiedObject(mappingItem.getUUIDs());
		mappingItemStamp = new RestStampedVersion(mappingItem.getComponentVersion());
		mapSetConcept = mappingItem.getMapSetSequence();
		qualifierConcept = mappingItem.getQualifierConcept() == null ? null : Get.identifierService().getConceptSequenceForUuids(mappingItem.getQualifierConcept());
		if (Get.identifierService().getChronologyTypeForNid(mappingItem.getSourceConceptNid()) != ObjectChronologyType.CONCEPT)
		{
			throw new RuntimeException("Source of the map is not a concept");
		}
		else
		{
			sourceConcept = Get.identifierService().getConceptSequence(mappingItem.getSourceConceptNid());
		}
		targetConcept = mappingItem.getQualifierConcept() == null ? null : Get.identifierService().getConceptSequenceForUuids(mappingItem.getTargetConcept());
		
		if (expandDescriptions)
		{
			mapSetDescription = Util.readBestDescription(mapSetConcept);
			if (qualifierConcept != null)
			{
				qualifierDescription = Util.readBestDescription(qualifierConcept);
			}
			sourceDescription = Util.readBestDescription(sourceConcept);
			if (targetConcept != null)
			{
				targetDescription = Util.readBestDescription(targetConcept);
			}
		}
		else
		{
			expandables = new Expandables();
			if (RequestInfo.get().returnExpandableLinks())
			{
				//TODO fix this expandable link
				expandables.add(new Expandable(ExpandUtil.referencedDetails, ""));
			}
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RestMappingItemVersion o)
	{
		// TODO implement
		return 0;
	}
}
