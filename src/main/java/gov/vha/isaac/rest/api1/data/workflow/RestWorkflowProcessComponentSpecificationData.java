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

package gov.vha.isaac.rest.api1.data.workflow;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Object containing data required to specify a component in a process.
 * 
 * {@link RestWorkflowProcessComponentSpecificationData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessComponentSpecificationData {
	/**
	 * The process id of the process to advance
	 */
	@XmlElement
	@JsonInclude
	UUID processId;

	/**
	 * The specified component int NID
	 */
	@XmlElement
	@JsonInclude
	int componentNid;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowProcessComponentSpecificationData() {
		super();
	}

	/**
	 * @param processId - workflow process UUID
	 * @param componentNid - component int NID
	 */
	public RestWorkflowProcessComponentSpecificationData(UUID processId, int componentNid) {
		super();
		this.processId = processId;
		this.componentNid = componentNid;
	}

	/**
	 * @return the processId
	 */
	@XmlTransient
	public UUID getProcessId() {
		return processId;
	}

	/**
	 * @return the component int NID
	 */
	@XmlTransient
	public int getComponentNid() {
		return componentNid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessComponentSpecificationData [processId=" + processId + ", componentNid=" + componentNid
				+ "]";
	}
}