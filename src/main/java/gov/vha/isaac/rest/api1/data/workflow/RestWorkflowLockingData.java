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

import gov.vha.isaac.rest.api1.workflow.WorkflowWriteAPIs;

/**
 * Object containing data required to aquire or release a lock on a {@link RestWorkflowProcess}.  Passed to {@link WorkflowWriteAPIs#setProcessLock}.
 * 
 * {@link RestWorkflowLockingData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowLockingData {

	/**
	 * The process id of the process to advance
	 */
	@XmlElement
	@JsonInclude
	UUID processId;

	/**
	 * The boolean indicating whether to acquire lock (false is to release lock)
	 */
	@XmlElement
	@JsonInclude
	boolean acquireLock;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowLockingData() {
		super();
	}

	/**
	 * @param processId - workflow process UUID
	 * @param userId - workflow user id
	 */
	public RestWorkflowLockingData(UUID processId, boolean acquireLock) {
		super();
		this.processId = processId;
		this.acquireLock = acquireLock;
	}

	/**
	 * @return the processId
	 */
	@XmlTransient
	public UUID getProcessId() {
		return processId;
	}

	/**
	 * @return whether the lock should be acquired
	 */
	@XmlTransient
	public boolean isAquireLock() {
		return acquireLock;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessAdvancementData ["
				+ "processId=" + processId
				+ "acquireLock=" + acquireLock
				+ "]";
	}
}