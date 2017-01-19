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

import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.session.MapSetFieldsService;

/**
 * 
 * {@link RestMappingSetField}
 * 
 * This class is used to convey available mapping fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetField.class)
public class RestMappingSetField extends RestMappingSetFieldBase
{
	/**
	 * Optional identifiers of an optional concept that describes the purpose of this field.
	 * The description from this concept, if set, will be used as the name of the field.
	 * Either fieldNameConceptIdentifiers or name must be passed, but not both.
	 */
	@XmlElement
	public RestIdentifiedObject fieldNameConceptIdentifiers;
	
	@XmlElement
	public boolean computed = false;

	RestMappingSetField()
	{
		//for Jaxb
		super();
	}

	public RestMappingSetField(String name) throws RestException {
		this(name, (IdentifiedObject)null, (Boolean)null, false);
	}
	public RestMappingSetField(String name, boolean computed) throws RestException {
		this(name, (IdentifiedObject)null, (Boolean)null, computed);
	}
	public RestMappingSetField(String name, boolean source, boolean computed) throws RestException {
		this(name, (IdentifiedObject)null, source, computed);
	}
	public RestMappingSetField(IdentifiedObject fieldNameConcept, boolean computed) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, (Boolean)null, computed);
	}
	public RestMappingSetField(IdentifiedObject fieldNameConcept, boolean source, boolean computed) throws RestException {
		this(fieldNameConcept.getPrimordialUuid().toString(), fieldNameConcept, source, computed);
	}
	public RestMappingSetField(MapSetFieldsService.Field field, boolean source) throws RestException {
		this(field.getName(), field.getObject(), source, field.isComputed());
	}
	public RestMappingSetField(MapSetFieldsService.Field field) throws RestException {
		this(field.getName(), field.getObject(), null, field.isComputed());
	}
	private RestMappingSetField(String name, IdentifiedObject fieldNameConcept, Boolean source, boolean computed) throws RestException
	{
		//for Jaxb
		super(name, source);
		this.fieldNameConceptIdentifiers = fieldNameConcept != null ? new RestIdentifiedObject(fieldNameConcept.getPrimordialUuid()) : null;
		this.computed = computed;
	}
}
