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

/**
 * 
 * {@link RestWorkflowProcessAdvancementData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessAdvancementData {

	/**
	 * The process id of the process to advance
	 */
	@XmlElement
	public UUID processId;

	/**
	 * The user performing the advancement
	 */
	@XmlElement
	public int userId;
	
	/**
	 * The advancement action requested
	 */
	@XmlElement
	public String actionRequested;
	

	/**
	 * The comment associated with the advancement
	 */
	@XmlElement
	public String comment;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowProcessAdvancementData() {
		super();
	}

	/**
	 * @param processId - workflow process UUID
	 * @param userId - workflow user id
	 * @param actionRequested - action requested
	 * @param comment - comment associated with the workflow advancement
	 */
	public RestWorkflowProcessAdvancementData(UUID processId, int userId, String actionRequested, String comment) {
		super();
		this.processId = processId;
		this.userId = userId;
		this.actionRequested = actionRequested;
		this.comment = comment;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessAdvancementData [processId=" + processId + ", userId=" + userId + ", action=" + actionRequested + ", comment=" + comment
				+ "]";
	}
}