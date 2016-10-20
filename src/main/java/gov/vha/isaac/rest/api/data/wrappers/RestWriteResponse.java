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
package gov.vha.isaac.rest.api.data.wrappers;

import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.rest.api1.data.RestEditToken;
import gov.vha.isaac.rest.tokens.EditToken;

/**
 * This class is a wrapper for a renewed RestEditToken
 * and several optional return values from write API calls,
 * such as a UUID id, an Integer nid and an Integer sequence
 * 
 * {@link RestWriteResponse}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWriteResponse
{
	/**
	 * The RestEditToken value - updated to be valid for a future submit.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestEditToken editToken;

	/**
	 * The UUID value of the item that was created or updated.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public UUID uuid;

	/**
	 * The Integer NID value of the item created or updated (if applicable, may be null)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer nid;

	/**
	 * The Integer sequence value of the item created or updated (if applicable, may be null)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer sequence;
	
	RestWriteResponse() {
		// For JAXB
	}
	
	/**
	 * If nid or uuid is populated, it will autopopulate any missing values of nid, uuid or sequence.
	 * The UUID is checked for system validity, if provided (nid and sequence is not populated if uuid isn't valid)
	 * 
	 */
	public RestWriteResponse(RestEditToken editTokenIn, UUID uuidIn, Integer nidIn, Integer sequenceIn)
	{
		uuid = uuidIn;
		nid = nidIn;
		sequence = sequenceIn;
		editToken = editTokenIn;
		if (nid == null || sequence == null || uuid == null)
		{
			if (nid != null || uuid != null)
			{
				//populate what is missing.
				if (nid != null && uuid == null)
				{
					uuid = Get.identifierService().getUuidPrimordialForNid(nid).get();
				}
				if (uuid != null && nid == null && Get.identifierService().hasUuid(uuid))
				{
					nid = Get.identifierService().getNidForUuids(uuid);
				}
			}
			if (sequence == null && nid != null)
			{
				ObjectChronologyType oct = Get.identifierService().getChronologyTypeForNid(nid); 
				if (oct == ObjectChronologyType.CONCEPT)
				{
					sequence = Get.identifierService().getConceptSequence(nid);
				}
				else if (oct == ObjectChronologyType.SEMEME)
				{
					sequence = Get.identifierService().getSememeSequence(nid);
				}
			}
		}
	}
	
	/**
	 * If nid or uuid is populated, it will autopopulate any missing values of nid, uuid or sequence.
	 */
	public RestWriteResponse(EditToken editToken, UUID uuid, Integer nid, Integer sequence)
	{
		this(new RestEditToken(editToken), uuid, nid, sequence);
	}
	
	/**
	 * Populates nid and sequence from UUID, if the UUID is in the system.
	 */
	public RestWriteResponse(EditToken editToken, UUID uuid)
	{
		this(new RestEditToken(editToken), uuid, null, null);
	}
	
	public RestWriteResponse(EditToken editToken) {
		this(editToken, null, null, null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWriteResponse [uuid=" + uuid + ", nid=" + nid + ", sequence=" + sequence + ", editToken="
				+ editToken + "]";
	}
}
