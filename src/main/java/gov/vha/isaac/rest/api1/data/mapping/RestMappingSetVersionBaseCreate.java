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
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfoCreate;

/**
 * 
 * {@link RestMappingSetVersionBaseCreate}
 * This stub class is used for callers to create {@link RestMappingSetVersion} objects.  This class, in combination with {@link RestMappingSetVersionBase} 
 * contains the fields that can be populated for creation.  
 * 
 * The API never returns this class.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetVersionBaseCreate.class)
public class RestMappingSetVersionBaseCreate extends RestMappingSetVersionBase
{
	/**
	 * The (optional) extended fields which carry additional information about this map set definition. 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestMappingSetExtensionValueCreate> mapSetExtendedFields;
	
	/**
	 * The (optional) extended fields that are declared for each map item instance that is created using this map set definition.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestDynamicSememeColumnInfoCreate> mapItemExtendedFieldsDefinition;

	/**
	 * The (optional) ordered list of fields to be displayed for the mapping set
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestMappingSetFieldCreate> mapSetFields;
	
	protected RestMappingSetVersionBaseCreate()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 */
	public RestMappingSetVersionBaseCreate(String name, String inverseName, String description, String purpose, Boolean active) 
	{
		super(name, inverseName, description, purpose, active);
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 * @param active
	 * @param mapSetExtendedFields
	 * @param mapItemExtendedFieldsDefinition
	 */
	public RestMappingSetVersionBaseCreate(
			String name,
			String inverseName,
			String description,
			String purpose,
			Boolean active,
			List<RestMappingSetExtensionValueCreate> mapSetExtendedFields,
			List<RestDynamicSememeColumnInfoCreate> mapItemExtendedFieldsDefinition,
			List<RestMappingSetFieldCreate> mapSetFields) 
	{
		super(name, inverseName, description, purpose, active);

		this.mapSetExtendedFields = mapSetExtendedFields;
		this.mapItemExtendedFieldsDefinition = mapItemExtendedFieldsDefinition;
		this.mapSetFields = mapSetFields;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersionBaseCreate [mapSetExtendedFields=" + mapSetExtendedFields
				+ ", mapItemExtendedFieldsDefinition=" + mapItemExtendedFieldsDefinition + ", mapSetFields="
				+ mapSetFields + ", name=" + name + ", inverseName=" + inverseName + ", description=" + description
				+ ", purpose=" + purpose + ", active=" + active + "]";
	}
}
