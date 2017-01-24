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

/**
 * 
 * {@link RestMappingSetVersionBaseUpdate}
 * This stub class is used for callers to edit {@link RestMappingSetVersion} objects.  It only contains the fields that may be edited after creation.
 * 
 * The API never returns this class.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetVersionBaseUpdate.class)
public class RestMappingSetVersionBaseUpdate extends RestMappingSetVersionBase
{
	/**
	 * The (optional) extended fields which carry additional information about this map set definition. 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestMappingSetExtensionValueUpdate> mapSetExtendedFields;

	/**
	 * The (optional) ordered list of fields to be displayed for the mapping set.
	 * The passed map set display fields will replace, not update, any existing set.
	 * If no fields are passed, then any existing display fields on the map set concept will be removed.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestMappingSetDisplayFieldCreate> displayFields;

	protected RestMappingSetVersionBaseUpdate()
	{
		//for Jaxb
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 * @param active
	 */
	public RestMappingSetVersionBaseUpdate(
			String name,
			String inverseName,
			String description,
			String purpose,
			Boolean active) {
		super(name, inverseName, description, purpose, active);
		this.mapSetExtendedFields = null;
		this.displayFields = null;
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 * @param active
	 * @param mapSetExtendedFields
	 * @param displayFields specifies display fields that should populate each item and respective order
	 */
	public RestMappingSetVersionBaseUpdate(
			String name,
			String inverseName,
			String description,
			String purpose,
			Boolean active,
			List<RestMappingSetExtensionValueUpdate> mapSetExtendedFields,
			List<RestMappingSetDisplayFieldCreate> displayFields) {
		super(name, inverseName, description, purpose, active);
		this.mapSetExtendedFields = mapSetExtendedFields;
		this.displayFields = displayFields;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersionBaseUpdate [mapSetExtendedFields=" + mapSetExtendedFields + ", displayFields="
				+ displayFields + ", name=" + name + ", inverseName=" + inverseName + ", description=" + description
				+ ", purpose=" + purpose + ", active=" + active + "]";
	}
}
