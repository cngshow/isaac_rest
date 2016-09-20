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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessComponentSpecificationData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * {@link WorkflowWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent)
public class WorkflowWriteAPIs
{
	//private static Logger log = LogManager.getLogger(WorkflowWriteAPIs.class);

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
		
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			return new RestUUID(provider.createWorkflowProcess(
					workflowProcessCreationData.getDefinitionId(),
					workflowProcessCreationData.getCreatorNid(),
					workflowProcessCreationData.getName(),
					workflowProcessCreationData.getDescription()));
		} catch (Exception e) {
			throw new RestException("Failed creating new workflow process from " + (workflowProcessCreationData != null ? workflowProcessCreationData : null));
		}
	}

//	/**
//	 * 
//	 * Launch a workflow process
//	 * 
//	 * @param processId RestUUID process id of workflow process to launch
//	 * @throws RestException
//	 */
//	@PUT
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.launchWorkflowProcessComponent)
//	public void launchWorkflowProcess(
//			RestUUID processId) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters());
//		
//		// TODO test launchWorkflowProcess()
//		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
//		try {
//			provider.launchWorkflowProcess(processId.value);
//		} catch (Exception e) {
//			throw new RestException("Failed launching workflow process " + (processId != null ? processId : null));
//		}
//	}
//
//	/**
//	 * 
//	 * End a workflow process
//	 * 
//	 * @param endData RestWorkflowProcessEndData workflow end data
//	 * @throws RestException
//	 */
//	@PUT
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.endWorkflowProcessComponent)
//	public void endWorkflowProcess(
//			RestWorkflowProcessEndData endData) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters());
//		
//		// TODO test endWorkflowProcess()
//		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
//		try {
//			provider.endWorkflowProcess(
//					endData.processId,
//					new AvailableAction(
//							endData.actionToProcess.definitionId,
//							endData.actionToProcess.initialState,
//							endData.actionToProcess.action,
//							endData.actionToProcess.outcomeState,
//							endData.actionToProcess.role),
//					endData.userId,
//					endData.comment,
//					EndWorkflowType.valueOf(endData.endType.toString()));
//		} catch (Exception e) {
//			throw new RestException("Failed ending workflow process with " + (endData != null ? endData : null));
//		}
//	}
	
	// WorkflowUpdater

	/**
	 * 
	 * Advance existing workflow process
	 * 
	 * @param processAdvancementData RestWorkflowProcessAdvancementData workflow advancement data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.advanceWorkflowProcessComponent)
	public void advanceWorkflowProcess(
			RestWorkflowProcessAdvancementData processAdvancementData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test advanceWorkflowProcess()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			provider.advanceWorkflow(processAdvancementData.getProcessId(), processAdvancementData.getUserId(), processAdvancementData.getActionRequested(), processAdvancementData.getComment());
		} catch (Exception e) {
			throw new RestException("Failed advancing workflow process with " + (processAdvancementData != null ? processAdvancementData : null));
		}
	}

	/**
	 * 
	 * Add role to user
	 * 
	 * @param roleData RestWorkflowRoleChangeData workflow definition user role change data
	 * @throws RestException
	 */
	// TODO: Decide if this feature is necessary
	/*
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.addWorkflowUserRoleComponent)
	public RestUUID addWorkflowUserRole(
			RestWorkflowRoleChangeData roleData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test addWorkflowUserRole()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			return new RestUUID(provider.addNewUserRole(roleData.getDefinitionId(), roleData.getUserId(), roleData.getRole()));
		} catch (Exception e) {
			throw new RestException("Failed adding role to user with " + (roleData != null ? roleData : null));
		}
	}
*/	
	/**
	 * 
	 * Remove component from workflow for process and component NID
	 * 
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.removeComponentFromWorkflowComponent)
	public void removeComponentFromWorkflow(
			RestWorkflowProcessComponentSpecificationData specifiedComponent) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test removeComponentFromWorkflow()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			provider.removeComponentFromWorkflow(
					specifiedComponent.getProcessId(),
					RequestInfoUtils.getNidFromParameter("RestWorkflowComponentSpecificationData.componentNid", specifiedComponent.getComponentNid()));
		} catch (Exception e) {
			throw new RestException("Failed removing component " + specifiedComponent + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}
