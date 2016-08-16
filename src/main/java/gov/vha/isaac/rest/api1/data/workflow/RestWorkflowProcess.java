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

import gov.vha.isaac.metacontent.workflow.contents.ProcessDetail;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessDetailSubjectMatterType;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;

/**
 * 
 * {@link RestWorkflowProcess}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcess extends RestWorkflowProcessBaseCreate
{
	/**
	 * The identifier data
	 */
	@XmlElement
	public UUID id;

	/** The time created. */
	public long timeCreated;

	/** The time created. */
	public long timeConcluded = -1L;

	/** The defining status. */
	public RestWorkflowProcessStatusType processStatus;

	
	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcess()
	{
		//for Jaxb
		super();
	}

	/**
	 * @param process- ISAAC workflow Process
	 */
	public RestWorkflowProcess(ProcessDetail process) {
		super(process.getDefinitionId(),
				process.getStampSequences(),
				process.getConceptSequences(),
				process.getCreator(),
				new RestWorkflowProcessDetailSubjectMatterType(process.getSubjectMatter()));
		this.id = process.getId();
		this.timeCreated = process.getTimeCreated();
		this.timeConcluded = process.getTimeConcluded();
		this.processStatus = new RestWorkflowProcessStatusType(process.getProcessStatus());
	}
}
