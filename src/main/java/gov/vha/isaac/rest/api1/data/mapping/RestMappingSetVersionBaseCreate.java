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
	public List<RestMappingSetExtensionValueBaseCreate> mapSetExtendedFields;
	
	/**
	 * The (optional) extended fields that are declared for each map item instance that is created using this map set definition.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestDynamicSememeColumnInfoCreate> mapItemExtendedFieldsDefinition;
		
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
	public RestMappingSetVersionBaseCreate(
			String name,
			String inverseName,
			String description,
			String purpose) 
	{
		super(name, inverseName, description, purpose);
	}

	/**
	 * @param mapSetExtendedFieldsType
	 * @param mapSetExtendedFields
	 * @param mapItemExtendedFieldsType
	 */
	public RestMappingSetVersionBaseCreate(
			String name,
			String inverseName,
			String description,
			String purpose,
			List<RestMappingSetExtensionValueBaseCreate> mapSetExtendedFields,
			List<RestDynamicSememeColumnInfoCreate> mapItemExtendedFieldsDefinition) 
	{
		super(name, inverseName, description, purpose);

		this.mapSetExtendedFields = mapSetExtendedFields;
		this.mapItemExtendedFieldsDefinition = mapItemExtendedFieldsDefinition;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersionBaseCreate [mapSetExtendedFields=" + mapSetExtendedFields
				+ ", mapItemExtendedFieldsDefinition="+ mapItemExtendedFieldsDefinition 
				+ ", name=" + name + ", inverseName=" + inverseName + ", description=" + description + ", purpose=" + purpose + "]";
	}
}
