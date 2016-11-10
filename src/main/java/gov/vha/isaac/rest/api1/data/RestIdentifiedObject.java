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
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
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
	 * The globally unique, fixed, stable set of identifiers for the object
	 */
	@XmlElement
	public List<UUID> uuids = new ArrayList<>();
	
	/**
	 * The local-database-only internal nid identifier for this object.
	 */
	@XmlElement
	public int nid;
	
	/**
	 * The local-database-only internal sequence identifier for this object.
	 */
	@XmlElement
	public int sequence;
	
	/**
	 * The type of this object - concept, sememe, or unknown.
	 */
	@XmlElement
	RestObjectChronologyType type;
	
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
	
	public RestIdentifiedObject(UUID uuid)
	{
		if (uuid == null) 
		{
			throw new RuntimeException("Attempted to return an empty RestIdentifiedObject!");
		}
		this.uuids.add(uuid);
		nid = Get.identifierService().getNidForUuids(uuids);
		readSequence();
	}
	
	//TODO go through the callers of this method, and see which ones could use a different method, to pass more information up front.
	public RestIdentifiedObject(List<UUID> uuids)
	{
		if (uuids == null || uuids.size() == 0) 
		{
			throw new RuntimeException("Attempted to return an empty RestIdentifiedObject!");
		}
		this.uuids.addAll(uuids);
		nid = Get.identifierService().getNidForUuids(uuids);
		readSequence();
	}
	
	public RestIdentifiedObject(int id, ObjectChronologyType type)
	{
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
		this.type = new RestObjectChronologyType(type);
	}
	
	public RestIdentifiedObject(int nid)
	{
		this.nid = nid;
		uuids.addAll(Get.identifierService().getUuidsForNid(nid));
		readSequence();
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
