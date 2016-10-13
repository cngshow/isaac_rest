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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.workflow.model.contents.DefinitionDetail;

/**
 * The metadata defining a given process (or workflow instance). This doesn't
 * include its history, which is available via {@link RestWorkflowProcessHistory}}
 * 
 * {@link RestWorkflowDefinitionDetail}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowDefinitionDetail
{
	/**
	 * The definition identifier
	 */
	@XmlElement
	UUID id;

	/** The bpmn2 id that contains the definition if it exists. */
	@XmlElement
	String bpmn2Id;

	/** The definition name. */
	@XmlElement
	String name;

	/** The definition namespace. */
	@XmlElement
	String namespace;

	/** The version of the definition. */
	@XmlElement
	String version;

	/** The workflow roles available defined via the definition . */
	@XmlElement
	Set<UserRole> roles = new HashSet<>();

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowDefinitionDetail()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param processDetail- ISAAC workflow DefinitionDetail
	 */
	public RestWorkflowDefinitionDetail(DefinitionDetail processDetail) {
		this.id = processDetail.getId();
		this.bpmn2Id = processDetail.getBpmn2Id();
		this.name = processDetail.getName();
		this.namespace = processDetail.getNamespace();
		this.version = processDetail.getVersion();
		this.roles = processDetail.getRoles();
	}

	/**
	 * @return the id
	 */
	@XmlTransient
	public UUID getId() {
		return id;
	}

	/**
	 * @return the bpmn2Id
	 */
	@XmlTransient
	public String getBpmn2Id() {
		return bpmn2Id;
	}

	/**
	 * @return the name
	 */
	@XmlTransient
	public String getName() {
		return name;
	}

	/**
	 * @return the namespace
	 */
	@XmlTransient
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @return the version
	 */
	@XmlTransient
	public String getVersion() {
		return version;
	}

	/**
	 * @return the roles
	 */
	@XmlTransient
	public Set<UserRole> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowDefinitionDetail [id=" + id + ", bpmn2Id=" + bpmn2Id + ", name=" + name + ", namespace="
				+ namespace + ", version=" + version + ", roles=" + roles + "]";
	}
}
