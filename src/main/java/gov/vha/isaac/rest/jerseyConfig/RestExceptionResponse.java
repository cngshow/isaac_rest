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
package gov.vha.isaac.rest.jerseyConfig;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This JSON-serializable class contains failure information usable by the client
 * and is returned by the toResponse() method of MyExceptionMapper
 * 
 * {@link RestExceptionResponse}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestExceptionResponse
{
	/**
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String conciseMessage;

	/**
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String verboseMessage;

	/**
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String relevantQueryParameterName;

	/**
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String relevantQueryParameterValue;
	
	/**
	 * Optional HTTP response Status
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Status status;
	
	RestExceptionResponse() {
		// For JAXB
	}

	/**
	 * @param conciseMessage
	 * @param verboseMessage
	 * @param relevantQueryParameterName
	 * @param relevantQueryParameterValue
	 * @param status
	 */
	public RestExceptionResponse(
			String conciseMessage,
			String verboseMessage,
			String relevantQueryParameterName,
			String relevantQueryParameterValue,
			Status status) {
		super();
		this.conciseMessage = conciseMessage;
		this.verboseMessage = verboseMessage;
		this.relevantQueryParameterName = relevantQueryParameterName;
		this.relevantQueryParameterValue = relevantQueryParameterValue;
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestExceptionResponse [conciseMessage=" + conciseMessage + ", verboseMessage=" + verboseMessage
				+ ", relevantQueryParameterName=" + relevantQueryParameterName + ", relevantQueryParameterValue="
				+ relevantQueryParameterValue + ", status=" + status + "]";
	}
}
