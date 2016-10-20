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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Object containing fields used in creation of a new {@link RestWorkflowProcess}.
 * Passed as argument to {@link WorkflowWriteAPIs#createWorkflowProcess()}.
 * 
 * {@link RestWorkflowProcessBaseCreate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestWorkflowProcessBaseCreate.class)
public class RestWorkflowProcessBaseCreate {
	// TODO Add comment
	
	/**
	 * The workflow definition id
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID definitionId;

	/**
	 * The process name
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String name;

	/**
	 * The process description
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String description;
	
	/**
	 * Constructor for JAXB
	 */
	protected RestWorkflowProcessBaseCreate() {
		super();
	}

	/**
	 * @param definitionId
	 * @param creatorId
	 * @param name
	 * @param description
	 */
	public RestWorkflowProcessBaseCreate(
			UUID definitionId,
			String name,
			String description) {
		super();
		this.definitionId = definitionId;
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the definitionId
	 */
	@XmlTransient
	public UUID getDefinitionId() {
		return definitionId;
	}

	/**
	 * @return the name
	 */
	@XmlTransient
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	@XmlTransient
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessCreate ["
				+ "definitionId=" + definitionId
				+ ", name=" + name
				+ ", description=" + description
				+ "]";
	}
}