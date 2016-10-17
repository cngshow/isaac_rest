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
import gov.vha.isaac.ochre.workflow.model.contents.ProcessHistory;

/**
 * A single advancement (history) of a given workflow {@link RestWorkflowProcess}. A new entry is
 * added for every workflow {@link RestWorkflowAvailableAction} a user takes.
 *
 * {@link RestWorkflowProcessHistory}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessHistory
{
	/**
	 * The identifier data
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID id;

	/**
	 * The process id
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID processId;

	/**
	 * The workflow user
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID userId;

	/**
	 * The time advanced
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	long timeAdvanced;

	/**
	 * The state
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String initialState;

	/**
	 * The action
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String action;

	/**
	 * The outcome
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String outcomeState;

	/**
	 * The comment
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String comment;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcessHistory()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param processHistory - ISAAC workflow ProcessHistory
	 */
	public RestWorkflowProcessHistory(ProcessHistory processHistory) {
		this.id = processHistory.getId();
		this.processId = processHistory.getProcessId();
		this.userId = processHistory.getUserId();
		this.timeAdvanced = processHistory.getTimeAdvanced();
		this.initialState = processHistory.getInitialState();
		this.action = processHistory.getAction();
		this.outcomeState = processHistory.getOutcomeState();
		this.comment = processHistory.getComment();
	}

	/**
	 * @return the id
	 */
	@XmlTransient
	public UUID getId() {
		return id;
	}

	/**
	 * @return the processId
	 */
	@XmlTransient
	public UUID getProcessId() {
		return processId;
	}

	/**
	 * @return the userId
	 */
	@XmlTransient
	public UUID getUserId() {
		return userId;
	}

	/**
	 * @return the timeAdvanced
	 */
	@XmlTransient
	public long getTimeAdvanced() {
		return timeAdvanced;
	}

	/**
	 * @return the initialState
	 */
	@XmlTransient
	public String getInitialState() {
		return initialState;
	}

	/**
	 * @return the action
	 */
	@XmlTransient
	public String getAction() {
		return action;
	}

	/**
	 * @return the outcomeState
	 */
	@XmlTransient
	public String getOutcomeState() {
		return outcomeState;
	}

	/**
	 * @return the comment
	 */
	@XmlTransient
	public String getComment() {
		return comment;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
		result = prime * result + ((outcomeState == null) ? 0 : outcomeState.hashCode());
		result = prime * result + ((processId == null) ? 0 : processId.hashCode());
		result = prime * result + (int) (timeAdvanced ^ (timeAdvanced >>> 32));
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		RestWorkflowProcessHistory other = (RestWorkflowProcessHistory) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (initialState == null) {
			if (other.initialState != null)
				return false;
		} else if (!initialState.equals(other.initialState))
			return false;
		if (outcomeState == null) {
			if (other.outcomeState != null)
				return false;
		} else if (!outcomeState.equals(other.outcomeState))
			return false;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		if (timeAdvanced != other.timeAdvanced)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessHistory [id=" + id + ", processId=" + processId + ", userId=" + userId
				+ ", timeAdvanced=" + timeAdvanced + ", initialState=" + initialState + ", action=" + action + ", outcomeState=" + outcomeState
				+ ", comment=" + comment + "]";
	}
}
