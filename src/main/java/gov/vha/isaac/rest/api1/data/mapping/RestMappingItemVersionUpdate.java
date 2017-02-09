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
 * {@link RestMappingItemVersionUpdate}
 * This stub class is used for callers to edit {@link RestMappingItemVersion} objects.  It only contains the fields that may be edited after creation.
 * 
 * The API never returns this class.

 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemVersionUpdate.class)
public class RestMappingItemVersionUpdate extends RestMappingItemVersionBase
{
	/**
	 * The (optional) target concept being mapped by this map item.  This field is optional, and may be blank, if no target mapping
	 * is available.  Accepts a nid, sequence or UUID.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String targetConcept;

	/**
	 * An (optional) concept used to qualify this mapping entry.  Accepts a nid, sequence or UUID.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String qualifierConcept; // TOOD change to equivalenceTypeConcept

	/**
	 * True to indicate the mapping item should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
	
	protected RestMappingItemVersionUpdate()
	{
		//for Jaxb
	}

	/**
	 * @param targetConcept
	 * @param mapItemExtendedFields
	 * @param equivalenceTypeConcept
	 */
	public RestMappingItemVersionUpdate(String targetConcept, String equivalenceTypeConcept, List<RestDynamicSememeData> mapItemExtendedFields,  Boolean active) 
	{
		super(mapItemExtendedFields);
		this.targetConcept = targetConcept;
		this.qualifierConcept = equivalenceTypeConcept;
		this.active = active;
	}
}
