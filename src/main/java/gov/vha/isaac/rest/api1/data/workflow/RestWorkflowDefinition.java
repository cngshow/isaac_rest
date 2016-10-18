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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.workflow.model.contents.DefinitionDetail;
import gov.vha.isaac.rest.api1.data.enumerations.RestUserRoleType;

/**
 * The metadata defining a given workflow definition. These are defined based on
 * BPMN2 Imports.
 * 
 * {@link RestWorkflowDefinition}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowDefinition {
	/**
	 * The definition identifier
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID id;

	/** The bpmn2 id that contains the definition if it exists. */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String bpmn2Id;

	/** The definition name. */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String name;

	/** The definition namespace. */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String namespace;

	/** The version of the definition. */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String version;

	/** The workflow roles available defined via the definition . */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Set<RestUserRoleType> roles = new HashSet<>();

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowDefinition() {
		// for Jaxb
		super();
	}

	/**
	 * @param processDetail-
	 *            ISAAC workflow DefinitionDetail
	 */
	public RestWorkflowDefinition(DefinitionDetail processDetail) {
		this.id = processDetail.getId();
		this.bpmn2Id = processDetail.getBpmn2Id();
		this.name = processDetail.getName();
		this.namespace = processDetail.getNamespace();
		this.version = processDetail.getVersion();
		for (UserRole role : processDetail.getRoles()) {
			this.roles.add(new RestUserRoleType(role));
		}
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
	public Set<RestUserRoleType> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime * +((id == null) ? 0 : id.hashCode()) + bpmn2Id.hashCode() + name.hashCode()
				+ namespace.hashCode() + version.hashCode() + roles.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestWorkflowDefinition other = (RestWorkflowDefinition) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (bpmn2Id == null) {
			if (other.bpmn2Id != null)
				return false;
		} else if (!bpmn2Id.equals(other.bpmn2Id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowDefinitionDetail [id=" + id + ", bpmn2Id=" + bpmn2Id + ", name=" + name + ", namespace="
				+ namespace + ", version=" + version + ", roles=" + roles + "]";
	}
}
