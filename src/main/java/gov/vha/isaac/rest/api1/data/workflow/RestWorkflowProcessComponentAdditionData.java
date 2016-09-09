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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestWorkflowProcessComponentAdditionData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessComponentAdditionData {

	/**
	 * The process id of the process to which to add concepts
	 */
	@XmlElement
	UUID processId;

	/**
	 * The nid of component to add
	 */
	@XmlElement
	int componentNid = 0;
	
	/**
	 * The stamp sequence of component to add
	 */
	@XmlElement
	int stampSequence = -1;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowProcessComponentAdditionData() {
		super();
	}

	/**
	 * @param processId - UUID id of process to which to add concepts
	 * @param conceptNids - integer nids of components to add to the specified workflow process
	 */
	public RestWorkflowProcessComponentAdditionData(UUID processId, int componentNid, int stampSeq) {
		super();
		this.processId = processId;
		this.componentNid = componentNid;
		this.stampSequence = stampSeq;
	}

	/**
	 * @return the processId
	 */
	@XmlTransient
	public UUID getProcessId() {
		return processId;
	}

	/**
	 * @return the componentNid
	 */
	@XmlTransient
	public int getComponentNid() {
		return componentNid;
	}

	/**
	 * @return the stampSequence
	 */
	@XmlTransient
	public int getStampSequence() {
		return stampSequence;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessConceptsAdditionData ["
				+ "processId=" + processId
				+ ", componentNid=" + componentNid
				+ ", stampSequence=" + stampSequence
				+ "]";
	}
}