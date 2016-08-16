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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.metacontent.workflow.contents.ProcessHistory;

/**
 * 
 * {@link RestWorkflowProcessHistory}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessHistory
{
	/**
	 * The identifier data
	 */
	@XmlElement
	public UUID id;

	/**
	 * The process id
	 */
	public UUID processId;

	/**
	 * The workflow user
	 */
	public int userId;

	/**
	 * The time advanced
	 */
	public long timeAdvanced;

	/**
	 * The state
	 */
	public String state;

	/**
	 * The action
	 */
	public String action;

	/**
	 * The outcome
	 */
	public String outcome;

	/**
	 * The comment
	 */
	public String comment;

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
		this.userId = processHistory.getWorkflowUser();
		this.timeAdvanced = processHistory.getTimeAdvanced();
		this.state = processHistory.getState();
		this.action = processHistory.getAction();
		this.outcome = processHistory.getOutcome();
		this.comment = processHistory.getComment();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessHistory [id=" + id + ", processId=" + processId + ", userId=" + userId
				+ ", timeAdvanced=" + timeAdvanced + ", state=" + state + ", action=" + action + ", outcome=" + outcome
				+ ", comment=" + comment + "]";
	}
}
