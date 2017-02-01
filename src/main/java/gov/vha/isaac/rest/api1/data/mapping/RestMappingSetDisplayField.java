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

import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;
import gov.vha.isaac.rest.session.RequestInfo;

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
	 * The description from this concept, if set, will be used as the id of the field.
	 * Either fieldNameConceptIdentifiers or id must be passed, but not both.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject fieldNameConceptIdentifiers;

	/**
	 * Description of this field
	 * If the field id is the ID of an assemblage concept then it will be the description of that concept.
	 * If the field id is a string literal then the description will be MapSetDisplayFieldsService.Field.NonConceptFieldName.valueOf(id).getDescription()
	 * 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String description;

	RestMappingSetDisplayField()
	{
		//for Jaxb
		super();
	}

	public RestMappingSetDisplayField(String id) throws RestException {
		this(id, (IdentifiedObject)null, (MapSetItemComponent)null, (String)null);
	}
	public RestMappingSetDisplayField(String id, MapSetItemComponent component) throws RestException {
		this(id, (IdentifiedObject)null, component, (String)null);
	}
	public RestMappingSetDisplayField(MapSetDisplayFieldsService.Field field) throws RestException {
		this(field.getId(), field.getObject(), (MapSetItemComponent)null, (String)null);
	}
	public RestMappingSetDisplayField(String id, MapSetItemComponent component, String description) throws RestException {
		this(id, (IdentifiedObject)null, component, description);
	}
	public RestMappingSetDisplayField(String id, IdentifiedObject fieldNameConcept, MapSetItemComponent component, String description) throws RestException
	{
		//for Jaxb
		super(id, component); // MapSetDisplayFieldsService performs validation
		if (component == MapSetItemComponent.ITEM_EXTENDED) {
			// Do not set fieldNameConceptIdentifiers
			this.description = description;
			this.fieldNameConceptIdentifiers = fieldNameConcept != null ? new RestIdentifiedObject(fieldNameConcept.getPrimordialUuid()) : null;
		} else {
			Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> cc = Frills.getConceptForUnknownIdentifier(this.id);
			if (cc.isPresent()) {
				if (fieldNameConcept != null) {
					if (fieldNameConcept.getNid() != cc.get().getNid()) {
						throw new RuntimeException("fieldNameConcept NID " + fieldNameConcept.getNid() + " does not match NID " + cc.get().getNid() + " for concept corresponding to id \"" + this.id + "\"");
					}
					if (! fieldNameConcept.getPrimordialUuid().equals(cc.get().getPrimordialUuid())) {
						throw new RuntimeException("fieldNameConcept UUID " + fieldNameConcept.getPrimordialUuid() + " does not match UUID " + cc.get().getPrimordialUuid() + " for concept corresponding to id \"" + this.id + "\"");
					}
				}
				this.fieldNameConceptIdentifiers = new RestIdentifiedObject(cc.get().getPrimordialUuid());
			} else {
				this.fieldNameConceptIdentifiers = fieldNameConcept != null ? new RestIdentifiedObject(fieldNameConcept.getPrimordialUuid()) : null;
			}
			this.description = description != null ? description : getDescriptionFromId(this.id);
		}
	}

	private static String getDescriptionFromId(String id) {
		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> cc = Frills.getConceptForUnknownIdentifier(id);
		if (cc.isPresent()) {
			Optional<String> desc = Frills.getDescription(cc.get().getNid(), RequestInfo.get().getTaxonomyCoordinate());
			if (desc.isPresent()) {
				return desc.get();
			} else {
				throw new RuntimeException("Frills.getDescription() failed to find description for concept id=" + id + " (UUID=" + cc.get().getPrimordialUuid() + ")");
			}
		} else {
			MapSetDisplayFieldsService.Field.NonConceptFieldName nonConceptFieldName = null;
			try {
				nonConceptFieldName = MapSetDisplayFieldsService.Field.NonConceptFieldName.valueOf(id);
				return nonConceptFieldName.getDescription();
			} catch (Exception e) {
				throw new RuntimeException("getDescriptionFromName() Failed to find map item display field description for id=\"" + id + "\"");
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayField [id=" + id + ", componentType=" + componentType + ", description="
				+ description + ", fieldNameConceptIdentifiers=" + fieldNameConceptIdentifiers + "]";
	}
}
