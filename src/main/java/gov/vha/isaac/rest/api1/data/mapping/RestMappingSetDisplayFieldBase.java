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
	 * ID that identifies this field within set of known fields
	 * Must be non null and be one of values returned by MapSetDisplayFieldsService.getAllFieldNames()
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String id; // TODO should this be private so that set method and ctor can enforce validation?

	/**
	 * Optional specification that field should contain data from specified component.
	 * This will be null in the MapSetDisplayFieldsService and non null in RestMappingSetDisplayFieldCreate.
	 * This field is only optional for internal use, being required by the API.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestMapSetItemComponentType componentType;

	RestMappingSetDisplayFieldBase()
	{
		//for Jaxb
		super();
	}

//	/**
//	 * This constructor should only be called when initializing MapSetDisplayFieldsService
//	 *  
//	 * @param id required to be one of the values returned by MapSetDisplayFieldsService.getAllFieldNames()
//	 * @throws RestException
//	 */
//	public RestMappingSetDisplayFieldBase(String id) throws RestException
//	{
//		this(id, (RestMapSetItemComponentType)null);
//	}

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
	/**
	 * This constructor should only be called when initializing MapSetDisplayFieldsService
	 * 
	 * @param field required to be one of the values returned by MapSetDisplayFieldsService.getAllFields()
	 * @throws RestException
	 */
	public RestMappingSetDisplayFieldBase(MapSetDisplayFieldsService.Field field) {
		this.id = field.getId();
		this.componentType = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayFieldBase [name=" + id + ", component=" + componentType + "]";
	}
}
