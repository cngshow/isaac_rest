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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.mapping.data.MappingSetDAO;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingSetVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingSetVersion extends RestMappingSetVersionBase implements Comparable<RestMappingSetVersion>
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
	
	/**
	 * The (optional) extended fields which carry additional information about this map set definition. 
	 */
	@XmlElement
	public List<RestMappingSetExtensionValue> mapSetExtendedFields;
	
	/**
	 * The fields that are declared for each map item instance that is created using this map set definition.  
	 */
	@XmlElement
	public List<RestDynamicSememeColumnInfo> mapItemFieldsDefinition;

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
			
			//read the extended field definition information
			mapItemFieldsDefinition = new ArrayList<>();
			DynamicSememeUsageDescription dsud = DynamicSememeUsageDescriptionImpl.read(mappingConcept.get().getNid());
			for (DynamicSememeColumnInfo dsci : dsud.getColumnInfo())
			{
				mapItemFieldsDefinition.add(new RestDynamicSememeColumnInfo(dsci));
			}

			//Read the extended fields off of the map set concept.
			//Strings
			mapSetExtendedFields = new ArrayList<>();
			
			Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.get().getNid(), 
					IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getConceptSequence()).forEach(stringExtensionSememe ->
				{
					Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)stringExtensionSememe).getLatestVersion(DynamicSememe.class, stampCoord);
					//TODO handle contradictions
					if (latest.isPresent())
					{
						DynamicSememe<?> ds = latest.get().value();
						int nameNid = ((DynamicSememeNid)ds.getData(0)).getDataNid();
						DynamicSememeData value = null;
						if (ds.getData().length > 1)
						{
							value = ds.getData(1);
						}
						mapSetExtendedFields.add(new RestMappingSetExtensionValue(nameNid, RestDynamicSememeData.translate(1, value)));
					}
				}
			);
			
			//Nids
			Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.get().getNid(), 
					IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_NID_EXTENSION.getConceptSequence()).forEach(nidExtensionSememe ->
				{
					Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)nidExtensionSememe).getLatestVersion(DynamicSememe.class, stampCoord);
					//TODO handle contradictions
					if (latest.isPresent())
					{
						DynamicSememe<?> ds = latest.get().value();
						int nameNid = ((DynamicSememeNid)ds.getData(0)).getDataNid();
						DynamicSememeData value = null;
						if (ds.getData().length > 1)
						{
							value = ds.getData(1);
						}
						mapSetExtendedFields.add(new RestMappingSetExtensionValue(nameNid, RestDynamicSememeData.translate(1, value)));
					}
				}
			);

			//Read the the description values
			Get.sememeService().getSememesForComponent(mappingConcept.get().getNid()).filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(descriptionC ->
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
												return ds.getChronology().isLatestVersionActive(stampCoord);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersion [identifiers=" + identifiers + ", mappingSetStamp=" + mappingSetStamp
				+ ", mapSetExtendedFields=" + mapSetExtendedFields + ", mapItemFieldsDefinition=" + mapItemFieldsDefinition 
				+ ", name=" + name + ", inverseName=" + inverseName + ", description=" + description + ", purpose=" + purpose + "]";
	}
}
