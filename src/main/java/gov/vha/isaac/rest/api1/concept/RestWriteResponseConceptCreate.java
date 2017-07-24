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
package gov.vha.isaac.rest.api1.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.modules.vhat.VHATConstants;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.tokens.EditToken;

/**
 * {@link RestWriteResponseConceptCreate}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWriteResponseConceptCreate extends RestWriteResponse
{
	/**
	 * The identifiers for the created FSN
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject fsnDescriptionSememe;
	
	/**
	 * The identifiers for the created preferred description (may be null)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject preferredDescriptionSememe;
	
	/**
	 * The identifiers for the created extended description type (may be null)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject extendedDescriptionTypeSememe;
	
	/**
	 * The identifiers for the created has_parent association sememe (may be null)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject hasParentAssociationSememe;

	/**
	 * The identifiers for the created logic graph
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject logicGraph;
	
	/**
	 * The identifiers for the nested dialects that were created
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestIdentifiedObject> dialectSememes = new ArrayList<>();
	
	
	RestWriteResponseConceptCreate() {
		super();
		// For JAXB
	}
	/**
	 * @param renew
	 * @param primordialUuid
	 * @param createdObjects
	 */
	public RestWriteResponseConceptCreate(EditToken renew, UUID primordialUuid, List<ObjectChronology<? extends StampedVersion>> createdObjects)
	{
		super(renew, primordialUuid);
		for (ObjectChronology<? extends StampedVersion> o : createdObjects)
		{
			if (o.getPrimordialUuid().equals(this.uuid))
			{
				//ignore
			}
			else if (o.getOchreObjectType() ==  OchreExternalizableObjectType.SEMEME)
			{
				@SuppressWarnings("rawtypes")
				SememeChronology sc = (SememeChronology)o;
				if (sc.getSememeType() == SememeType.DESCRIPTION)
				{
					@SuppressWarnings("rawtypes")
					DescriptionSememe ds = (DescriptionSememe) sc.getUnwrittenVersionList().iterator().next();
					if (ds.getDescriptionTypeConceptSequence() == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence())
					{
						fsnDescriptionSememe = new RestIdentifiedObject(sc);
					}
					else if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
					{
						preferredDescriptionSememe = new RestIdentifiedObject(sc);
					}
					else
					{
						throw new RuntimeException("Unexpected created object type! " + o);
					}
				}
				else if (sc.getSememeType() == SememeType.DYNAMIC) {
					UUID assemblageUuid = null;
					String assemblageDesc = null;
					try {
						assemblageUuid = Get.identifierService().getUuidPrimordialFromConceptId(sc.getAssemblageSequence()).orElse(null);
						assemblageDesc = Get.conceptDescriptionText(sc.getAssemblageSequence());
					} catch (Exception e) {
						// ignore
					}
					if (sc.getAssemblageSequence() == DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence())
					{
						extendedDescriptionTypeSememe = new RestIdentifiedObject(sc);
					}
					else if (assemblageUuid != null && assemblageUuid.equals(VHATConstants.VHAT_HAS_PARENT_ASSOCIATION_TYPE_UUID))
					{
						hasParentAssociationSememe = new RestIdentifiedObject(sc);
					} else {
						throw new RuntimeException("Unexpected created " + sc.getSememeType() + " sememe (assemblage UUID=" + assemblageUuid + ", DESC=" + assemblageDesc + ") type! " + o);
					}
				}
				else if (sc.getSememeType() == SememeType.COMPONENT_NID)
				{
					dialectSememes.add(new RestIdentifiedObject(sc));
				}
				else if (sc.getSememeType() == SememeType.LOGIC_GRAPH)
				{
					logicGraph = new RestIdentifiedObject(sc);
				}
				else
				{
					throw new RuntimeException("Unexpected created " + sc.getSememeType() + " sememe type! " + o);
				}
			}
			else
			{
				throw new RuntimeException("Unexpected created object type! " + o);
			}
		}
	}
}
