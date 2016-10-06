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
	UUID id;

	/**
	 * The process id
	 */
	UUID processId;

	/**
	 * The workflow user
	 */
	UUID userId;

	/**
	 * The time advanced
	 */
	long timeAdvanced;

	/**
	 * The state
	 */
	String initialState;

	/**
	 * The action
	 */
	String action;

	/**
	 * The outcome
	 */
	String outcomeState;

	/**
	 * The comment
	 */
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessHistory [id=" + id + ", processId=" + processId + ", userId=" + userId
				+ ", timeAdvanced=" + timeAdvanced + ", initialState=" + initialState + ", action=" + action + ", outcomeState=" + outcomeState
				+ ", comment=" + comment + "]";
	}
}
