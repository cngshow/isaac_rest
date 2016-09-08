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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.metacontent.workflow.contents.ProcessHistory;

/**
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
	int userId;

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
		this.userId = processHistory.getUserNid();
		this.timeAdvanced = processHistory.getTimeAdvanced();
		this.initialState = processHistory.getInitialState();
		this.action = processHistory.getAction();
		this.outcomeState = processHistory.getOutcomeState();
		this.comment = processHistory.getComment();
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
