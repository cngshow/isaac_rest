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

import gov.vha.isaac.metacontent.workflow.contents.AvailableAction;
import gov.vha.isaac.ochre.workflow.provider.AbstractWorkflowUtilities.EndWorkflowType;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowEndType;

/**
 * 
 * {@link RestWorkflowProcessEndData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessEndData {

	/**
	 * The process id of the process to cancel
	 */
	@XmlElement
	public UUID processId;

	/**
	 * The available action to process
	 */
	@XmlElement
	public RestWorkflowAvailableAction actionToProcess;
	
	/**
	 * The user performing the cancellation
	 */
	@XmlElement
	public int userId;

	/**
	 * The comment associated with the cancellation
	 */
	@XmlElement
	public String comment;

	/**
	 * The EndWorkflowType
	 */
	@XmlElement
	public RestWorkflowEndType endType;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowProcessEndData() {
		super();
	}

	/**
	 * @param processId
	 * @param actionToProcess
	 * @param userId
	 * @param comment
	 * @param endType
	 */
	public RestWorkflowProcessEndData(
			UUID processId,
			AvailableAction actionToProcess,
			int userId,
			String comment,
			EndWorkflowType endType) {
		super();
		this.processId = processId;
		this.userId = userId;
		this.comment = comment;
		this.actionToProcess = new RestWorkflowAvailableAction(actionToProcess);
		this.endType = new RestWorkflowEndType(endType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessCancellationData ["
				+ "processId=" + processId
				+ ", userId=" + userId
				+ ", comment=" + comment
				+ ", endType=" + endType
				+ "]";
	}
}