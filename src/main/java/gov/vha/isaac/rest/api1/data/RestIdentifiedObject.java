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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.rest.ApplicationConfig;
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
	 * A textual description of this identified object.  This field is NOT always populated, and should not be relied on.  
	 * 
	 * It currently always returns null in a production mode - it is only calculated when the service is in debug mode.
	 * 
	 * It is primarily a debugging aid for developers when looking at returned object in a browser.  When concepts are returned, this will return an 
	 * arbitrary description for the concept (sometimes - not always.)
	 * 
	 * When sememes are returned, this is currently not populated at all.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String description;
	
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
		if (ApplicationConfig.getInstance().isDebugDeploy())
		{
			description = concept.getConceptDescriptionText();
		}
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
					if (ApplicationConfig.getInstance().isDebugDeploy())
					{
						description = Get.conceptDescriptionText(sequence);
					}
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
				if (ApplicationConfig.getInstance().isDebugDeploy())
				{
					description = Get.conceptDescriptionText(nid);
				}
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
				if (ApplicationConfig.getInstance().isDebugDeploy())
				{
					description = Get.conceptDescriptionText(id);
				}
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
					if (ApplicationConfig.getInstance().isDebugDeploy())
					{
						description = Get.conceptDescriptionText(sequence);
					}
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
				if (ApplicationConfig.getInstance().isDebugDeploy())
				{
					description = Get.conceptDescriptionText(sequence);
				}
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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nid == null) ? 0 : nid.hashCode());
		result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		
		// Generate based only on initial, presumably primordial, uuid
		result = prime * result + ((uuids == null) ? 0 : (uuids.size() == 0 ? 0 : uuids.get(0).hashCode()));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestIdentifiedObject other = (RestIdentifiedObject) obj;
		if (nid == null) {
			if (other.nid != null)
				return false;
		} else if (!nid.equals(other.nid))
			return false;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		
		// Compare based only on initial, presumably primordial, uuid
		if (uuids == null) {
			if (other.uuids != null)
				return false;
		} else if (uuids.size() == 0 && other.uuids.size() != 0) {
			return false;
		} else if (uuids.size() != 0 && other.uuids.size() == 0) {
			return false;
		} else if (uuids.size() > 0) {
			if (! uuids.get(0).equals(other.uuids.get(0))) {
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestIdentifiedObject [type=" + type + ", nid=" + nid + ", sequence=" + sequence + ", uuids=" + uuids
				+ "]";
	}
}
