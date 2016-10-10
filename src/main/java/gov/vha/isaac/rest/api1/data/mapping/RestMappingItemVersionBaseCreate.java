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

import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingItemVersionBaseCreate}
 * This stub class is used for callers to create {@link RestMappingItemVersion} objects.  This class, in combination with {@link RestMappingItemVersionBase} 
 * contains the fields that can be populated for creation.  
 * 
 * The API never returns this class.

 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingItemVersionBaseCreate extends RestMappingItemVersionBase
{
	/**
	 * The concept sequence that identifies the map set that this entry belongs to
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public int mapSetConcept;
	
	/**
	 * The source concept sequence being mapped by this map item
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public int sourceConcept;
	
	public RestMappingItemVersionBaseCreate()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param targetConcept
	 * @param qualifierConcept
	 * @param mapItemExtendedFields
	 */
	public RestMappingItemVersionBaseCreate(
			Integer targetConcept,
			Integer qualifierConcept,
			int mapSetConcept,
			int sourceConcept,
			List<RestDynamicSememeData> mapItemExtendedFields) {
		super(targetConcept, qualifierConcept, mapItemExtendedFields);
		this.mapSetConcept = mapSetConcept;
		this.sourceConcept = sourceConcept;
	}
	
	public RestMappingItemVersionBaseCreate(
			Integer targetConcept,
			Integer qualifierConcept,
			UUID mapSetConcept,
			int sourceConcept,
			List<RestDynamicSememeData> mapItemExtendedFields) {
		super(targetConcept, qualifierConcept, mapItemExtendedFields);
		this.mapSetConcept = Get.identifierService().getConceptSequenceForUuids(mapSetConcept);
		this.sourceConcept = sourceConcept;
	}
}
