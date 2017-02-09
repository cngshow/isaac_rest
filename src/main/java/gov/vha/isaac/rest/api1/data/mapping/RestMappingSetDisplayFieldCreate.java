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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;

/**
 * 
 * {@link RestMappingSetDisplayFieldCreate}
 * 
 * This class is used to specify a field for ordering on a map set during update or create.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayFieldCreate.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayFieldCreate
{
	/**
	 * ID that identifies this display field.  Depending on the value of fieldComponenetType, this may be one of three distinct types:
	 * 
	 *  1) when fieldComponentType below is set to ITEM_EXTENDED, this should be the integer column number that represents the columnPosition 
	 *     of the extended field (extensionValue.columnNumber)
	 *  2)  when fieldComponentType below is set to a value such as SOURCE or TARGET - This required value must be an ID pulled from 
	 *        1/mapping/fields[.id], which will be a UUID
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String id;

	/**
	 * This should be a enumName or enumId from the available values at /1/mapping/fieldComponentTypes - however in the case where the id is set to an id 
	 * from 1/mapping/fields, the value can more easily be populated from 1/mapping/fields[.componentType]
	 * 
	 * Example values of for this field are SOURCE, ITEM_EXTENDED
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String fieldComponentType;
	
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
	public RestMappingSetDisplayFieldCreate(String id, MapSetItemComponent componentType) throws RestException
	{
		this.id = id;
		this.fieldComponentType = componentType.name();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayFieldCreate [name=" + id + ", fieldComponentType=" + fieldComponentType + "]";
	}
}
