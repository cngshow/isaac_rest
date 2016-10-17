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

import gov.vha.isaac.ochre.workflow.model.contents.AvailableAction;
import gov.vha.isaac.rest.api1.data.enumerations.RestUserRoleType;

/**
 * The available workflow actions as defined via the workflow definition. Each
 * entry contains a single initialState/action/outcomeState triplet that is an
 * available action for a given role.
 * 
 * The workflow must be in the initial state and a user must have the workflow
 * role to be able to perform the action.
 * 
 * {@link RestWorkflowAvailableAction}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowAvailableAction
{
	/**
	 * The identifier
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID id;
	/**
	 * The definition id
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID definitionId;
	/**
	 * The initial state
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
	 * The outcome state
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String outcomeState;
	/**
	 * The role
	 *
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestUserRoleType role;

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
		this.role = new RestUserRoleType(action.getRole());
	}

	/**
	 * @return the id
	 */
	@XmlTransient
	public UUID getId() {
		return id;
	}


	/**
	 * @return the definitionId
	 */
	@XmlTransient
	public UUID getDefinitionId() {
		return definitionId;
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
	 * @return the role
	 */
	@XmlTransient
	public RestUserRoleType getRole() {
		return role;
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
