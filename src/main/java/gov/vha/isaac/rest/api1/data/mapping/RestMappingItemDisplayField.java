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
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;

/**
 * 
 * {@link RestMappingItemDisplayField}
 * 
 * This class is used to convey available mapping set display fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemDisplayField.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingItemDisplayField extends RestMappingSetDisplayField
{
	/**
	 * Value corresponding to field
	 */
	@XmlElement
	public String value;

	RestMappingItemDisplayField()
	{
		//for Jaxb
		super();
	}

	public RestMappingItemDisplayField(String name, String value) throws RestException {
		this(name, (IdentifiedObject)null, (RestMapSetItemComponentType)null, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(name).isComputed(), value);
	}
	public RestMappingItemDisplayField(String name, String value, RestMapSetItemComponentType component) throws RestException {
		this(name, (IdentifiedObject)null, component, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(name).isComputed(), value);
	}
	public RestMappingItemDisplayField(IdentifiedObject fieldNameConcept, String value) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, (RestMapSetItemComponentType)null, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(fieldNameConcept.getPrimordialUuid().toString()).isComputed(), value);
	}
	public RestMappingItemDisplayField(IdentifiedObject fieldNameConcept, String value, RestMapSetItemComponentType component) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, component, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(fieldNameConcept.getPrimordialUuid().toString()).isComputed(), value);
	}
	public RestMappingItemDisplayField(MapSetDisplayFieldsService.Field field, String value, RestMapSetItemComponentType component) throws RestException {
		this(field.getName(), field.getObject(), component, field.isComputed(), value);
	}
	public RestMappingItemDisplayField(MapSetDisplayFieldsService.Field field, String value) throws RestException {
		this(field.getName(), field.getObject(), null, field.isComputed(), value);
	}
	private RestMappingItemDisplayField(String name, IdentifiedObject fieldNameConcept, RestMapSetItemComponentType component, boolean computed, String value) throws RestException
	{
		//for Jaxb
		super(name, component); 
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayField [name=" + name + ", componentType=" + componentType + ", description="
				+ description + ", fieldNameConceptIdentifiers=" + fieldNameConceptIdentifiers + ", value=" + value + "]";
	}
}
