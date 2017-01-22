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

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.rest.api.exceptions.RestException;
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
public class RestMappingSetDisplayFieldBase
{
	/**
	 * Name that identifies this field within set of known fields
	 */
	@XmlElement
	public String name; // TODO should this be private so that set method and ctor can enforce validation?

	/**
	 * Optional specification that field should contain data from either source or target.
	 * This will be null unless associated with a specific mapping set.
	 */
	@XmlElement
	public Boolean source;

	RestMappingSetDisplayFieldBase()
	{
		//for Jaxb
		super();
	}

	public RestMappingSetDisplayFieldBase(String name) throws RestException
	{
		this(name, (Boolean)null);
	}
	public RestMappingSetDisplayFieldBase(String name, Boolean source) throws RestException
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
		this.source = source;
	}
	public RestMappingSetDisplayFieldBase(MapSetDisplayFieldsService.Field field) {
		this.name = field.getName();
		this.source = null;
	}
}
