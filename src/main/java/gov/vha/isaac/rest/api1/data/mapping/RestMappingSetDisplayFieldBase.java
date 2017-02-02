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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;

/**
 * 
 * {@link RestMappingSetDisplayFieldBase}
 * 
 * The base class used to return attributes of a mapping set display field.  This class
 * is never returned by itself - you will always be given a concrete subclass.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayFieldBase.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayFieldBase
{
	/**
	 * The unique ID that identifies this display field.  Depending on the value of componenetType, this may be one of three distinct types:
	 * 
	 *  1) when componentType below is set to ITEM_EXTENDED, this should be the integer column number that represents the columnPosition 
	 *     of the extended field (extensionValue.columnNumber)
	 *  2)  when fieldComponentType below is set to a value such as SOURCE or TARGET - This required value must be an ID pulled from 
	 *        1/mapping/fields[.id].
	 *      2a) This id may be a UUID, or
	 *      2b) This id may be a string constant such as DESCRIPTION
	 *      
	 * the ID returned should be utilized to link the column position of this display field in the RestMappingSetVersion.displayFields list 
	 * with the ID found in RestMappingItemComputedDisplayField, when placing items on screen.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String id;

	/**
	 * A value that describes the type of this display field - which will come from the available values at /1/mapping/fieldComponentTypes 
	 * 
	 * Example values of for this field are SOURCE, ITEM_EXTENDED, etc
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestMapSetItemComponentType componentType;

	RestMappingSetDisplayFieldBase()
	{
		//for Jaxb
		super();
	}

	/**
	 * This is the only constructor that should be called by derived classes
	 * 
	 * @param id required to be one of the values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 * @param componentType required to be non null only for RestMappingSetDisplayFieldCreate
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldBase(String id, MapSetItemComponent componentType) throws RestException
	{
		//for Jaxb
		super();

		if (componentType != null) {
			// This should only be null when returned as a container for id and description
			// TODO: Joel fix this by changing hierarchy
			this.componentType = new RestMapSetItemComponentType(componentType);
		}
		
		if (componentType == MapSetItemComponent.ITEM_EXTENDED) {
			// This only validates the id as a valid non-negative integer,
			// not that it corresponds to an actual extended field DynamicSememeData element
			Integer idAsIntegerExtendedFieldsColumnNumber = null;
			try {
				idAsIntegerExtendedFieldsColumnNumber = Integer.valueOf(id);
				if (idAsIntegerExtendedFieldsColumnNumber < 0) {
					throw new RuntimeException("Invalid (negative) map item extended field column " + id + " for RestMapSetItemComponentType " + componentType);
				} else {
					this.id = id;
				}
			} catch (NumberFormatException e) {
				throw new RuntimeException("Invalid map item extended field column " + id + " for RestMapSetItemComponentType " + componentType, e);
			}
		} else {
			// These are globals
			MapSetDisplayFieldsService service = LookupService.getService(MapSetDisplayFieldsService.class);
			MapSetDisplayFieldsService.Field field = service.getFieldByConceptIdOrStringIdIfNotConceptId(id);
			if (field == null) {
				Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> cc = Frills.getConceptForUnknownIdentifier(id);
				throw new RestException("RestMappingSetFieldBase.name", id, "Unsupported MapSet field \"" + id + "\"" + (cc.isPresent() ? " (" + Get.conceptDescriptionText(cc.get().getNid()) + ") " : "") + "\". Should be one of " + getFieldNamesWithDescriptions(service.getAllFields()));
			} else {
				this.id = field.getId();
			}
		}
	}

	private static Map<Object, String> getFieldNamesWithDescriptions(Collection<MapSetDisplayFieldsService.Field> fields) {
		Map<Object, String> descriptionsByName = new HashMap<>();
		
		for (MapSetDisplayFieldsService.Field field : fields) {
			descriptionsByName.put(field.getId(), field.getObject() != null ? Get.conceptDescriptionText(field.getObject().getNid()) : MapSetDisplayFieldsService.Field.NonConceptFieldName.valueOf(field.getId()).getDescription());
		}
		
		return descriptionsByName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayFieldBase [name=" + id + ", component=" + componentType + "]";
	}
}
