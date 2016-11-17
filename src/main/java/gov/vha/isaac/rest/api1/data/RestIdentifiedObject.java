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
package gov.vha.isaac.rest.api1.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;

/**
 * Returns the UUIDs and nid associated with an object in the system
 * {@link RestIdentifiedObject}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestIdentifiedObject
{
	/**
	 * The globally unique, fixed, stable set of identifiers for the object.  Typically populated, but may be null in edge cases - such as 
	 * a sequence stored in a dynamic sememe column - one has to refer to the dynamic sememe definition to determine if the sequence represents
	 * a concept or a sememe - which is expensive, so it isn't pre-populated here - which will leave the UUIDs and nid blank.
	 */
	@XmlElement
	public List<UUID> uuids = new ArrayList<>();
	
	/**
	 * The local-database-only internal nid identifier for this object.  Typically populated, but may be null in edge cases - such as 
	 * a UUID stored in a dynamic sememe column which doesn't represent a known object.
	 */
	@XmlElement
	public Integer nid;
	
	/**
	 * The local-database-only internal sequence identifier for this object.  Typically populated, but may be null in edge cases - such as 
	 * a UUID stored in a dynamic sememe column which doesn't represent a known object.
	 */
	@XmlElement
	public Integer sequence;
	
	/**
	 * The type of this object - concept, sememe, or unknown.
	 */
	@XmlElement
	public RestObjectChronologyType type;
	
	RestIdentifiedObject() {
		// For JAXB only
	}
	
	public RestIdentifiedObject(SememeChronology<?> sememe)
	{
		uuids.addAll(sememe.getUuidList());
		nid = sememe.getNid();
		sequence = sememe.getSememeSequence();
		type = new RestObjectChronologyType(ObjectChronologyType.SEMEME);
	}
	
	public RestIdentifiedObject(ConceptChronology<?> concept)
	{
		uuids.addAll(concept.getUuidList());
		nid = concept.getNid();
		sequence = concept.getConceptSequence();
		type = new RestObjectChronologyType(ObjectChronologyType.CONCEPT);
	}
	
	public RestIdentifiedObject(UUID uuid)
	{
		if (uuid == null) 
		{
			throw new RuntimeException("Attempted to return an empty RestIdentifiedObject!");
		}
		if (Get.identifierService().hasUuid(uuid))
		{
			nid = Get.identifierService().getNidForUuids(uuid);
			uuids.addAll(Get.identifierService().getUuidsForNid(nid));
			readSequence();
		}
		else
		{
			uuids.add(uuid);
			type = new RestObjectChronologyType(ObjectChronologyType.UNKNOWN_NID);
		}
	}
	
	public RestIdentifiedObject(UUID uuid, ObjectChronologyType type)
	{
		if (uuid == null) 
		{
			throw new RuntimeException("Attempted to return an empty RestIdentifiedObject!");
		}
		this.type = new RestObjectChronologyType(type);
		if (type == ObjectChronologyType.UNKNOWN_NID)
		{
			uuids.add(uuid);
		}
		else
		{
			switch (type) 
			{
				case CONCEPT:
					sequence = Get.identifierService().getConceptSequenceForUuids(uuid);
					nid = Get.identifierService().getConceptNid(sequence);
					break;
				case SEMEME:
					sequence = Get.identifierService().getSememeSequenceForUuids(uuid);
					nid = Get.identifierService().getSememeNid(sequence);
					break;
				default :
					throw new RuntimeException("Unexpected case");
			}
			uuids.addAll(Get.identifierService().getUuidsForNid(nid));
		}
	}
	
	public RestIdentifiedObject(List<UUID> uuids)
	{
		if (uuids == null || uuids.size() == 0) 
		{
			throw new RuntimeException("Attempted to return an empty RestIdentifiedObject!");
		}
		if (Get.identifierService().hasUuid(uuids))
		{
			nid = Get.identifierService().getNidForUuids(uuids);
			this.uuids.addAll(Get.identifierService().getUuidsForNid(nid));
			readSequence();
		}
		else
		{
			this.uuids.addAll(uuids);
			type = new RestObjectChronologyType(ObjectChronologyType.UNKNOWN_NID);
		}
	}
	
	public RestIdentifiedObject(ObjectChronology<?> object)
	{
		nid = object.getNid();
		uuids.addAll(object.getUuidList());
		switch (object.getOchreObjectType()) {
			case CONCEPT:
				sequence = Get.identifierService().getConceptSequence(nid);
				type = new RestObjectChronologyType(ObjectChronologyType.CONCEPT);
				break;
			case SEMEME:
				sequence = Get.identifierService().getSememeSequence(nid);
				type = new RestObjectChronologyType(ObjectChronologyType.SEMEME);
				break;
			default :
				throw new RuntimeException("Unexpected case");
		}
	}
	
	public RestIdentifiedObject(int id, ObjectChronologyType type)
	{
		this.type = new RestObjectChronologyType(type);
		switch (type) {
			case CONCEPT:
				nid = Get.identifierService().getConceptNid(id);
				sequence = Get.identifierService().getConceptSequence(id);
				break;
			case SEMEME:
				nid = Get.identifierService().getSememeNid(id);
				sequence = Get.identifierService().getSememeSequence(id);
				break;
			case UNKNOWN_NID:
			default :
				throw new RuntimeException("Unexpected case");
		}
		uuids.addAll(Get.identifierService().getUuidsForNid(nid));
	}
	
	public RestIdentifiedObject(int id)
	{
		if (id < 0)
		{
			this.nid = id;
			uuids.addAll(Get.identifierService().getUuidsForNid(nid));
			readSequence();
		}
		else
		{
			//was passed a sequence, but don't know if it is a concept or a sememe.
			this.sequence = id;
			
			if (Get.conceptService().hasConcept(sequence))
			{
				if (Get.sememeService().hasSememe(sequence))
				{
					this.type = new RestObjectChronologyType(ObjectChronologyType.UNKNOWN_NID);
				}
				else
				{
					this.type = new RestObjectChronologyType(ObjectChronologyType.CONCEPT);
					this.nid = Get.identifierService().getConceptNid(sequence);
					this.uuids.addAll(Get.identifierService().getUuidsForNid(nid));
				}
			}
			else if (Get.sememeService().hasSememe(sequence))
			{
				this.type = new RestObjectChronologyType(ObjectChronologyType.SEMEME);
				this.nid = Get.identifierService().getSememeNid(sequence);
				this.uuids.addAll(Get.identifierService().getUuidsForNid(nid));
			}
		}
	}
	
	private void readSequence()
	{
		ObjectChronologyType internalType = Get.identifierService().getChronologyTypeForNid(nid);
		type = new RestObjectChronologyType(internalType);
		
		switch (internalType) {
			case CONCEPT:
				sequence = Get.identifierService().getConceptSequence(nid);
				break;
			case SEMEME:
				sequence = Get.identifierService().getSememeSequence(nid);
				break;
			case UNKNOWN_NID:
				sequence = 0;
				break;
			default :
				throw new RuntimeException("Unexpected case");
		}
	}
	
	@XmlTransient
	public UUID getFirst() {
		return uuids.get(0);
	}
}
