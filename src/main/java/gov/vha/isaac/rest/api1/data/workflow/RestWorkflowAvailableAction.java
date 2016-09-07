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

/**
 * 
 * {@link RestWorkflowAvailableAction}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowAvailableAction
{
	/**
	 * The identifier data
	 */
	@XmlElement
	public UUID id;
	/**
	 * The definition id
	 */
	public UUID definitionId;
	/**
	 * The initial state
	 */
	public String initialState;
	/**
	 * The action
	 */
	public String action;
	/**
	 * The outcome state
	 */
	public String outcomeState;
	/**
	 * The role
	 * */
	public String role;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowAvailableAction()
	{
		//for Jaxb
		super();
	}

	
	/**
	 * @param action - ISAAC workflow AvailableAction
	 */
	public RestWorkflowAvailableAction(AvailableAction action) {
		this.id = action.getId();
		this.definitionId = action.getDefinitionId();
		this.initialState = action.getInitialState();
		this.action = action.getAction();
		this.outcomeState = action.getOutcomeState();
		this.role = action.getRole();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowAvailableAction [id=" + id + ", definitionId=" + definitionId + ", initialState="
				+ initialState + ", action=" + action + ", outcomeState=" + outcomeState + ", role=" + role + "]";
	} 
}
