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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;

/**
 * The metadata associated with a given workflow process (or workflow instance). This doesn't
 * include its history which is available via {@link RestWorkflowProcessHistory}
 *
 * {@link RestWorkflowProcess}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcess extends RestWorkflowProcessBaseCreate
{
	/**
	 * The workflow process identifier
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID id;

	/**
	 * The creator concept id
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID creatorId;

	/**
	 * The time workflow process created
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	long timeCreated;

	/**
	 * The time workflow process launched
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	long timeLaunched = -1L;

	/**
	 * The time workflow process cancelled or concluded
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	long timeCancelledOrConcluded = -1L;

	/**
	 * The defining workflow process status
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestWorkflowProcessStatusType processStatus;

	/**
	 * The workflow process owner
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID ownerId;

	/**
	 * Indicate if process is available to the user
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String userAvailability;

	/**
	 * Indicate the state of the process such Ready for Approval,
	 * Ready for Review, or Ready for Edit.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String state;


	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcess()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param process- ISAAC workflow Process
	 */
	public RestWorkflowProcess(ProcessDetail process) {
		super(process.getDefinitionId(),
				process.getName(),
				process.getDescription());
		this.id = process.getId();
		this.creatorId = process.getCreatorId();
		this.timeCreated = process.getTimeCreated();
		this.timeLaunched = process.getTimeLaunched();
		this.timeCancelledOrConcluded = process.getTimeCanceledOrConcluded();
		this.processStatus = new RestWorkflowProcessStatusType(process.getStatus());
		this.ownerId = process.getOwnerId();
	}

	/**
	 * @return the id
	 */
	@XmlTransient
	public UUID getId() {
		return id;
	}

	/**
	 * @return the creator id
	 */
	@XmlTransient
	public UUID getCreatorId() {
		return creatorId;
	}

	/**
	 * @return the timeCreated
	 */
	@XmlTransient
	public long getTimeCreated() {
		return timeCreated;
	}

	/**
	 * @return the timeLaunched
	 */
	@XmlTransient
	public long getTimeLaunched() {
		return timeLaunched;
	}

	/**
	 * @return the timeCancelledOrConcluded
	 */
	@XmlTransient
	public long getTimeCancelledOrConcluded() {
		return timeCancelledOrConcluded;
	}

	/**
	 * @return the processStatus
	 */
	@XmlTransient
	public RestWorkflowProcessStatusType getProcessStatus() {
		return processStatus;
	}

	/**
	 * @return the ownerId
	 */
	@XmlTransient
	public UUID getOwnerId() {
		return ownerId;
	}

	/**
	 * @return the userAvailability
	 */
	@XmlTransient
	public String getUserAvailability() {
		return userAvailability;
	}

	/**
	 * @return the state
	 */
	@XmlTransient
	public String getStage() {
		return state;
	}


	/**
	 * @param the userAvailability
	 */
	//@XmlTransient
	public void setUserAvailability(String userAvailability) {
		this.userAvailability = userAvailability;
	}

	/**
	 * @param the process state
	 */
	//@XmlTransient
	public void setStage(String state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RestWorkflowProcess other = (RestWorkflowProcess) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcess ["
				+ super.toString()
				+ "id=" + id + ", creatorId=" + creatorId + ", timeCreated=" + timeCreated + ", timeLaunched=" + timeLaunched
				+ ", timeCancelledOrConcluded=" + timeCancelledOrConcluded + ", processStatus=" + processStatus
				+ ", ownerId=" + ownerId + ", userAvailability=" + userAvailability + ", state=" + state + "]";
	}
}
