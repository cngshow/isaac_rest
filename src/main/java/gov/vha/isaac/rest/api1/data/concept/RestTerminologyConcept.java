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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.data.concept;

import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * {@link RestTerminologyConcept}
 * 
 * A convenience class that is used to return a high-level summary of the Terminologies available in the system.
 * For more details on specific terminologies available, see 1/system/systemInfo
 * 
 * When this is returned, the 'description' field will be populated with the best short-name of the Terminology, 
 * while the 'definition' field will be populated with the the longer extended name of a terminology.
 * 
 *  There are no supported expandables, and the versions field will not be populated.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestTerminologyConcept extends RestConceptChronology
{
	/**
	 * The (optional) longer name of a terminology (if available)
	 */
	@XmlElement
	String definition;
	
	public RestTerminologyConcept(ConceptChronology<? extends ConceptVersion> cc)
	{
		super();
		identifiers = new RestIdentifiedObject(cc);
		expandables = null;
		
		Get.sememeService().getDescriptionsForComponent(cc.getNid()).forEach(desc -> 
		{
			Optional<LatestVersion<DescriptionSememe>> lv = ((SememeChronology)desc).getLatestVersion(DescriptionSememeImpl.class, 
					RequestInfo.get().getStampCoordinate());
			if (lv.isPresent())
			{
				if (lv.get().value().getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
				{
					//I want the non-preferred synonym in this case - as the way the metadata is set up, the non-preferred synonym is the one without the word "Modules"
					//in it.
					
					Optional<SememeChronology<? extends SememeVersion<?>>> acceptabilitySememe = Get.sememeService()
							.getSememesForComponent(lv.get().value().getNid()).findAny();
					if (acceptabilitySememe.isPresent() && acceptabilitySememe.get().getSememeType() == SememeType.COMPONENT_NID)
					{
						Optional<LatestVersion<ComponentNidSememe<?>>> value = ((SememeChronology)acceptabilitySememe.get())
								.getLatestVersion(ComponentNidSememeImpl.class, RequestInfo.get().getStampCoordinate());
						if (value.isPresent())
						{
							if (value.get().value().getComponentNid() == MetaData.ACCEPTABLE.getNid())
							{
								description = lv.get().value().getText();
							}
						}
					}
					
				}
				else if (lv.get().value().getDescriptionTypeConceptSequence() == MetaData.DEFINITION_DESCRIPTION_TYPE.getConceptSequence())
				{
					definition = lv.get().value().getText();
				}
			}
		});
		if (StringUtils.isBlank(description))
		{
			LogManager.getLogger().warn("Unable to find expected description type for Terminology Concept " + cc.getPrimordialUuid());
			description = Util.readBestDescription(cc.getNid(), RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate());
		}
	}
}
