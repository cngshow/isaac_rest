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
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;

/**
 * 
 * {@link RestMappingSetDisplayField}
 * 
 * This class is used to convey available mapping set display fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayField.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayField extends RestMappingSetDisplayFieldBase
{
	/**
	 * Optional identifiers of an optional concept that describes the purpose of this display field.
	 * The description from this concept, if set, will be used as the name of the field.
	 * Either fieldNameConceptIdentifiers or name must be passed, but not both.
	 */
	@XmlElement
	public RestIdentifiedObject fieldNameConceptIdentifiers;
	
	/**
	 * Indicates whether or not the corresponding mapping set display field is a computed value
	 */
	@XmlElement
	public boolean computed = false;

	/**
	 * Optional value corresponding to field
	 */
	@XmlElement
	public String value;

	RestMappingSetDisplayField()
	{
		//for Jaxb
		super();
	}

	public RestMappingSetDisplayField(String name, String value) throws RestException {
		this(name, (IdentifiedObject)null, (Boolean)null, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(name).isComputed(), value);
	}
	public RestMappingSetDisplayField(String name, String value, Boolean source) throws RestException {
		this(name, (IdentifiedObject)null, source, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(name).isComputed(), value);
	}
	public RestMappingSetDisplayField(IdentifiedObject fieldNameConcept, String value) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, (Boolean)null, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(fieldNameConcept.getPrimordialUuid().toString()).isComputed(), value);
	}
	public RestMappingSetDisplayField(IdentifiedObject fieldNameConcept, String value, boolean source) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, source, LookupService.getService(MapSetDisplayFieldsService.class).getFieldByIdOrNameIfNotId(fieldNameConcept.getPrimordialUuid().toString()).isComputed(), value);
	}
	public RestMappingSetDisplayField(MapSetDisplayFieldsService.Field field, String value, boolean source) throws RestException {
		this(field.getName(), field.getObject(), source, field.isComputed(), value);
	}
	public RestMappingSetDisplayField(MapSetDisplayFieldsService.Field field, String value) throws RestException {
		this(field.getName(), field.getObject(), null, field.isComputed(), value);
	}
	private RestMappingSetDisplayField(String name, IdentifiedObject fieldNameConcept, Boolean source, boolean computed, String value) throws RestException
	{
		//for Jaxb
		super(name, source);
		this.fieldNameConceptIdentifiers = fieldNameConcept != null ? new RestIdentifiedObject(fieldNameConcept.getPrimordialUuid()) : null;
		this.computed = computed;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayField [name=" + name + ", fieldNameConceptIdentifiers=" + fieldNameConceptIdentifiers + ", computed="
				+ computed + ", source=" + source + "]";
	}
}
