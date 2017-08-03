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
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.metadata.source.IsaacMetadataAuxiliary;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link RestMappingSetDisplayField}
 * 
 * This, combined with {@link RestMappingSetDisplayFieldBase} returns all of the attributes about display fields.
 * 
 * In the context of an individual mapset, this communicates the desired order of fields to be displayed to the user, as a combination 
 * of map item components (source, target, equivalenceType), extended fields (if any) and computed fields (if any)
 * 
 *  In the context of the capabilities of displaying computed fields, this communicates all possible computed field types. 
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetDisplayField.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingSetDisplayField extends RestMappingSetDisplayFieldBase
{
	private static Logger log = LogManager.getLogger(RestMappingSetDisplayField.class);

	/**
	 * when componentType is set to a value such as SOURCE or TARGET (basically, any type other than ITEM_EXTENDED) the id is set to a UUID 
	 * that represents a sememe to compute the value from or the UUID constant for the description option, then this field will be populated with the 
	 * concept represented by the UUID value of the id field.
	 * 
	 * In any other case, this will not be populated.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject fieldNameConceptIdentifiers;

	/**
	 * Description of this field
	 * If the field id is the ID of an assemblage concept (of a sememe type - where the desired behavior is to return the sememe string value) 
	 *   then it will be the description of that sememe assemblage concept.  A typical value here would be "VUID, though it will be prefixed with source, 
	 *   target, or another prefix, as makes sense in the context.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String description;

	//for Jaxb
	RestMappingSetDisplayField()
	{
		super();
	}
	
	public RestMappingSetDisplayField(UUID extendedColumnDescriptiveConcept, int extendedColId)
	{
		super(extendedColId, MapSetItemComponent.ITEM_EXTENDED);
		populate(Get.conceptService().getConcept(extendedColumnDescriptiveConcept), MapSetItemComponent.ITEM_EXTENDED);
	}
	

	/**
	 * 
	 * @param fieldNameConcept
	 * @param componentType - should only be null when returning field capabilities - should always be populated in the context of a mapset or mapitem
	 * @throws RestException
	 */
	public RestMappingSetDisplayField(IdentifiedObject fieldNameConcept, MapSetItemComponent componentType)
	{
		super(fieldNameConcept, componentType); // MapSetDisplayFieldsService performs validation
		populate(fieldNameConcept, componentType);
	}

	/**
	 * @param concept
	 * @param itemExtended
	 */
	private void populate(IdentifiedObject concept, MapSetItemComponent componentType)
	{
		String prefix = "";
		fieldNameConceptIdentifiers = new RestIdentifiedObject(concept.getPrimordialUuid());

		if (componentType != null)
		{
			switch (componentType)
			{
				case EQUIVALENCE_TYPE:
					prefix = Frills.getDescription(IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE.getConceptSequence(), 
							RequestInfo.get().getTaxonomyCoordinate()).get() + " ";
					break;
				case SOURCE:
					prefix = "source ";
					break;
				case TARGET:
					prefix = Frills.getDescription(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getConceptSequence(), 
							RequestInfo.get().getTaxonomyCoordinate()).get() + " ";
					break;
				case ITEM_EXTENDED:
					//no prefix
					break;
				default :
					throw new RuntimeException("Unhandled case!");
			}
		}
		
		Optional<String> descriptionOptional = Frills.getDescription(concept.getPrimordialUuid(), RequestInfo.get().getTaxonomyCoordinate());
		if (! descriptionOptional.isPresent()) {
			String msg = "Failed populating " +  componentType + " RestMappingSetDisplayField. No description found for concept " + concept;
			log.error(msg);
			throw new RuntimeException(msg);
		}
		description = prefix + descriptionOptional.get();
		//If the view coordinate is on FSN, we may have one (or more) semantic tags in these labels.  Strip them.
		description = description.replaceAll("\\s\\(" + IsaacMetadataAuxiliary.METADATA_SEMANTIC_TAG + "\\)", "");
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
