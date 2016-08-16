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
package gov.vha.isaac.rest.api1.workflow;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.metacontent.workflow.contents.ProcessDetail.SubjectMatter;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessDetailSubjectMatterType;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link WorkflowWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent)
public class WorkflowWriteAPIs
{
	private static Logger log = LogManager.getLogger(WorkflowWriteAPIs.class);

	/**
	 * 
	 * Start a new workflow process
	 * 
	 * @param workflowProcessCreationData structure containing data required to create a new workflow process
	 * @return RestUUID uuid of new workflow process
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.createPathComponent + RestPaths.createWorkflowProcessComponent)
	public RestUUID createWorkflowProcess(
			RestWorkflowProcessBaseCreate workflowProcessCreationData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test createWorkflowProcess()
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			SubjectMatter subjectMatter = null;
			for (SubjectMatter current : SubjectMatter.values()) {
				if (new RestWorkflowProcessDetailSubjectMatterType(current).equals(workflowProcessCreationData.subjectMatter)) {
					subjectMatter = current;
					break;
				}
			}
			return new RestUUID(provider.createWorkflowProcess(
					workflowProcessCreationData.definitionId,
					workflowProcessCreationData.conceptSequences,
					workflowProcessCreationData.stampSequences,
					workflowProcessCreationData.creator,
					subjectMatter));
		} catch (Exception e) {
			throw new RestException("Failed creating new workflow process from " + (workflowProcessCreationData != null ? workflowProcessCreationData : null));
		}
	}

	/**
	 * 
	 * Launch a workflow process
	 * 
	 * @param processId RestUUID process id of workflow process to launch
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.createPathComponent + RestPaths.launchWorkflowProcessComponent)
	public void launchWorkflowProcess(
			RestUUID processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test createWorkflowProcess()
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			provider.launchWorkflowProcess(processId.value);
		} catch (Exception e) {
			throw new RestException("Failed launching workflow process " + (processId != null ? processId : null));
		}
	}
}
