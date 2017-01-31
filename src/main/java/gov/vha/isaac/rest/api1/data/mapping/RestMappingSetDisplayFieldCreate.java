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

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;

/**
 * 
 * {@link RestMappingSetDisplayFieldCreate}
 * 
 * This class is used to convey available mapping fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayFieldCreate.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayFieldCreate extends RestMappingSetDisplayFieldBase
{
	RestMappingSetDisplayFieldCreate()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param name required to be one of the values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 * @param componentType required to be non null
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldCreate(String name, MapSetItemComponent componentType) throws RestException
	{
		super(name, validateAndReturnRestMapSetItemComponentType(name, componentType));
	}
	/**
	 * @param field required to be one of the values returned by MapSetDisplayFieldsService.getAllFields()
	 * @param componentType required to be non null
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldCreate(MapSetDisplayFieldsService.Field field, MapSetItemComponent componentType) throws RestException {
		this(field.getId(), componentType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayFieldCreate [name=" + id + ", componentType=" + componentType + "]";
	}

	private static MapSetItemComponent validateAndReturnRestMapSetItemComponentType(String name, MapSetItemComponent componentType) throws RestException {
		if (componentType != null) {
			return componentType;
		} else {
			throw new RestException("Cannot construct RestMappingSetDisplayFieldCreate " + name + " with null RestMapSetItemComponentType");
		}
	}
}
