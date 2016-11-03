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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingItemVersionBase}
 * This stub class is used for callers to edit {@link RestMappingItemVersion} objects.  It only contains the fields that may be edited after creation.
 * 
 * The API never returns this class.

 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemVersionBase.class)
public class RestMappingItemVersionBase
{
	/**
	 * The target concept sequence being mapped by this map item.  This field is optional, and may be blank, if no target mapping
	 * is available.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer targetConcept;

	/**
	 * An (optional) concept sequence used to qualify this mapping entry 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer qualifierConcept;

	/**
	 * The (optional) extended fields which carry additional information about this map item.  For details on these fields, read 
	 * the info returned as part of the {@link RestMappingSetVersion#mapItemFieldsDefinition} field
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestDynamicSememeData> mapItemExtendedFields;
	
	/**
	 * True to indicate the mapping item should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
	
	protected RestMappingItemVersionBase()
	{
		//for Jaxb
	}

	/**
	 * @param targetConcept
	 * @param mapItemExtendedFields
	 * @param qualifierConcept
	 */
	public RestMappingItemVersionBase(
			Integer targetConcept,
			Integer qualifierConcept,
			List<RestDynamicSememeData> mapItemExtendedFields) {
		super();
		this.targetConcept = targetConcept;
		this.qualifierConcept = qualifierConcept;
		this.mapItemExtendedFields = mapItemExtendedFields;
	}
}
