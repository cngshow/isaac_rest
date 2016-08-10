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

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.metacontent.workflow.contents.DefinitionDetail;

/**
 * 
 * {@link RestWorkflowDefinitionDetail}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowDefinitionDetail
{
	/**
	 * The identifier data
	 */
	@XmlElement
	public UUID id;

	/** The bpmn2 id. */
	public String bpmn2Id;

	/** The name. */
	public String name;

	/** The namespace. */
	public String namespace;

	/** The version. */
	public String version;

	/** The roles. */
	public Set<String> roles;

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
}
