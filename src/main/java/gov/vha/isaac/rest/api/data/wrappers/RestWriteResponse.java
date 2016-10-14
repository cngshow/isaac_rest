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
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
	 * The RestEditToken value
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestEditToken editToken;

	/**
	 * The UUID value
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID uuid;

	/**
	 * The Integer NID value
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Integer nid;

	/**
	 * The Integer sequence value
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Integer sequence;
	
	RestWriteResponse() {
		// For JAXB
	}
	
	public RestWriteResponse(RestEditToken editToken, UUID uuid, Integer nid, Integer sequence)
	{
		this.uuid = uuid;
		this.nid = nid;
		this.sequence = sequence;
		this.editToken = editToken;
	}
	public RestWriteResponse(RestEditToken editToken) {
		this(editToken, null, null, null);
	}

	public RestWriteResponse(EditToken editToken, UUID uuid, Integer nid, Integer sequence)
	{
		this(new RestEditToken(editToken), uuid, nid, sequence);
	}
	public RestWriteResponse(EditToken editToken) {
		this(editToken, null, null, null);
	}

	/**
	 * @return the optional UUID uuid
	 */
	@XmlTransient
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return the optional Integer nid
	 */
	@XmlTransient
	public Integer getNid() {
		return nid;
	}

	/**
	 * @return the optional Integer sequence
	 */
	@XmlTransient
	public Integer getSequence() {
		return sequence;
	}
	
	/**
	 * @return the RestEditToken
	 */
	@XmlTransient
	public RestEditToken getEditToken() {
		return editToken;
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
