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
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;

/**
 * 
 * {@link RestMappingSetDisplayFieldBase}
 * 
 * This class is used to convey available mapping fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayFieldBase.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayFieldBase
{
	/**
	 * Name that identifies this field within set of known fields
	 * Must be non null and be one of values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 */
	@XmlElement
	public String name; // TODO should this be private so that set method and ctor can enforce validation?

	/**
	 * Optional specification that field should contain data from specified component.
	 * This will be null in the MapSetDisplayFieldsService and non null in RestMappingSetDisplayFieldCreate.
	 * This field is only optional for internal use, being required by the API.
	 */
	@XmlElement
	public RestMapSetItemComponentType componentType;

	RestMappingSetDisplayFieldBase()
	{
		//for Jaxb
		super();
	}

	/**
	 * This constructor should only be called when initializing MapSetDisplayFieldsService
	 *  
	 * @param name required to be one of the values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldBase(String name) throws RestException
	{
		this(name, (RestMapSetItemComponentType)null);
	}

	/**
	 * This is the only constructor that should be called by derived classes
	 * 
	 * @param name required to be one of the values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 * @param componentType required to be non null only for RestMappingSetDisplayFieldCreate
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldBase(String name, RestMapSetItemComponentType componentType) throws RestException
	{
		//for Jaxb
		super();
		MapSetDisplayFieldsService service = LookupService.getService(MapSetDisplayFieldsService.class);
		MapSetDisplayFieldsService.Field field = service.getFieldByIdOrNameIfNotId(name);
		if (field == null) {
			throw new RestException("RestMappingSetFieldBase.name", name, "Unsupported MapSet field. Should be one of " + service.getAllFieldNames());
		} else {
			this.name = field.getName();
		}
		this.componentType = componentType;
	}

	/**
	 * This constructor should only be called when initializing MapSetDisplayFieldsService
	 * 
	 * @param field required to be one of the values returned by MapSetDisplayFieldsService.getAllFields()
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldBase(MapSetDisplayFieldsService.Field field) {
		this.name = field.getName();
		this.componentType = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayFieldBase [name=" + name + ", component=" + componentType + "]";
	}
}
