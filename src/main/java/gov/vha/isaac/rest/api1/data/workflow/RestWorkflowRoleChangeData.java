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

/**
 * 
 * {@link RestWorkflowRoleChangeData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowRoleChangeData {

	/**
	 * The UUID id of the workflow definition to/from which to add/remove a role for a specified user
	 */
	@XmlElement
	UUID definitionId;

	/**
	 * The id of the user to/from which to add/remove a role
	 */
	@XmlElement
	int userId;

	/**
	 * The role to add/remove to/from the specified user
	 */
	@XmlElement
	String role;

	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowRoleChangeData() {
		super();
	}

	/**
	 * @param definitionId - UUID id of the workflow definition to/from which to add/remove a role for a specified user
	 * @param userId - user to/from which to add/remove a role
	 * @param role - role to add/remove to/from the specified user
	 */
	public RestWorkflowRoleChangeData(UUID definitionId, int userId, String role) {
		super();
		this.definitionId = definitionId;
		this.userId = userId;
		this.role = role;
	}

	/**
	 * @return the definitionId
	 */
	@XmlTransient
	public UUID getDefinitionId() {
		return definitionId;
	}

	/**
	 * @return the userId
	 */
	@XmlTransient
	public int getUserId() {
		return userId;
	}

	/**
	 * @return the role
	 */
	@XmlTransient
	public String getRole() {
		return role;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowRoleData [definitionId=" + definitionId + ", userId=" + userId + ", role=" + role
				+ "]";
	}
}