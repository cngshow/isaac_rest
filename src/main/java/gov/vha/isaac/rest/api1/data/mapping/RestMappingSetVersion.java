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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.mapping.data.MappingSetDAO;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;

/**
 * 
 * {@link RestMappingSetVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingSetVersion extends RestMappingSetVersionBaseCreate implements Comparable<RestMappingSetVersion>
{
	/**
	 * The identifier data of the concept that represents this mapping set
	 */
	@XmlElement
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this map set definition
	 */
	@XmlElement
	public RestStampedVersion mappingSetStamp;

	protected RestMappingSetVersion()
	{
		//for Jaxb
		super();
	}
	 
	/**
	 * This code expects to read a sememe of type {@link IsaacMappingConstants#DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE}
	 * @param sememe
	 * @param stampCoord
	 */
	public RestMappingSetVersion(DynamicSememe<?> sememe, StampCoordinate stampCoord)
	{
		Optional<ConceptVersion<?>> mappingConcept = MappingSetDAO.getMappingConcept(sememe, stampCoord); 
		
		if (mappingConcept.isPresent())
		{
			
			identifiers = new RestIdentifiedObject(mappingConcept.get().getUuidList());
			//TODO whenever we make an edit to any component of the map set, we will also need to commit the concept, so that this stamp
			//always updates with any other stamp that is updated
			mappingSetStamp = new RestStampedVersion(mappingConcept.get());

			if (sememe.getData().length > 0 && sememe.getData()[0] != null)
			{
				purpose = ((DynamicSememeString) sememe.getData()[0]).getDataString();
			}
			
			if (sememe.getData().length > 1 && sememe.getData()[1] != null)
			{
				mapSetExtendedFieldsType = Get.identifierService().getConceptSequenceForUuids(((DynamicSememeUUID) sememe.getData()[1]).getDataUUID());
				
				//if there is an extended fields type, see if there is a sememe of this type attached
				Optional<SememeChronology<? extends SememeVersion<?>>> extended = Get.sememeService()
					.getSememesForComponentFromAssemblage(mappingConcept.get().getNid(), mapSetExtendedFieldsType).findAny();
				if (extended.isPresent())
				{
					@SuppressWarnings("rawtypes")
					SememeChronology extendedSC = extended.get();
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<DynamicSememe<?>>> latest = extendedSC.getLatestVersion(DynamicSememe.class, stampCoord);
					if (latest.isPresent())
					{
						//TODO handle contradictions
						mapSetExtendedFields = RestDynamicSememeVersion.translateData(latest.get().value().getData());
					}
				}
			}
			
			if (sememe.getData().length > 2 && sememe.getData()[2] != null)
			{
				mapItemExtendedFieldsType = Get.identifierService().getConceptSequenceForUuids(((DynamicSememeUUID) sememe.getData()[2]).getDataUUID());
			}

			Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.get().getNid(), 
					MetaData.DESCRIPTION_ASSEMBLAGE.getConceptSequence()).forEach(descriptionC ->
				{
					if (name != null && description != null && inverseName != null)
					{
						//noop... sigh... can't short-circuit in a forEach....
					}
					else
					{
						@SuppressWarnings({ "rawtypes", "unchecked" })
						Optional<LatestVersion<DescriptionSememe<?>>> latest = ((SememeChronology)descriptionC).getLatestVersion(DescriptionSememe.class, stampCoord);
						//TODO handle contradictions
						if (latest.isPresent())
						{
							DescriptionSememe<?> ds = latest.get().value();
							if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
							{
								if (Frills.isDescriptionPreferred(ds.getNid(), null))
								{
									name = ds.getText();
								}
								else
								//see if it is the inverse name
								{
									if (Get.sememeService().getSememesForComponentFromAssemblage(ds.getNid(), 
											DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).anyMatch(sememeC -> 
											{
												return sememeC.isLatestVersionActive(stampCoord);
											}))
									{
										inverseName = ds.getText(); 
									}
								}
							}
							else if (ds.getDescriptionTypeConceptSequence() == MetaData.DEFINITION_DESCRIPTION_TYPE.getConceptSequence())
							{
								if (Frills.isDescriptionPreferred(ds.getNid(), null))
								{
									description = ds.getText();
								}
							}
						}
					}
				});
		}
		else
		{
			throw new RuntimeException("Mapping Set is not present on the given coordinates");
		}
	}

	/**
	 * Sorts by name
	 */
	@Override
	public int compareTo(RestMappingSetVersion o)
	{
		return name.compareTo(o.name);
	}
}
