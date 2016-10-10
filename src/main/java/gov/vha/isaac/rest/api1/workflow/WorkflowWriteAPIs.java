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

import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.provider.BPMNInfo;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowLockAquisitionData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessComponentSpecificationData;
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
	 * Creates a new workflow process. In turn, a new entry is added to
	 * the {@link RestWorkflowProcess} content store. The process status defaults as DEFINED.
	 * 
	 * Used by users when creating a new process
	 * 
	 * @param workflowProcessCreationData structure containing data required to create a new workflow process
	 * @return RestUUID uuid of new workflow process
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent + RestPaths.createWorkflowProcessComponent)
	public RestUUID createWorkflowProcess(
			RestWorkflowProcessBaseCreate workflowProcessCreationData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		
		WorkflowProcessInitializerConcluder provider = RequestInfo.get().getWorkflow().getWorkflowProcessInitializerConcluder();
		try {
			return new RestUUID(provider.createWorkflowProcess(
					workflowProcessCreationData.getDefinitionId(),
					workflowProcessCreationData.getCreatorId(),
					workflowProcessCreationData.getName(),
					workflowProcessCreationData.getDescription()));
		} catch (Exception e) {
			throw new RestException("Failed creating new workflow process from " + (workflowProcessCreationData != null ? workflowProcessCreationData : "NULL"));
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
//	@Path(RestPaths.updatePathComponent + RestPaths.launchWorkflowProcessComponent)
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
//	@Path(RestPaths.updatePathComponent + RestPaths.endWorkflowProcessComponent)
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
	 * Advance an existing process {@link RestWorkflowProcess} with the specified action. In doing so, the
	 * user must add an advancement comment.
	 * 
	 * Used by filling in the information prompted for after selecting a
	 * Transition Workflow action.
	 * 
	 * @param processAdvancementData RestWorkflowProcessAdvancementData workflow advancement data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.advanceWorkflowProcessComponent)
	public void advanceWorkflowProcess(
			RestWorkflowProcessAdvancementData processAdvancementData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		
		// TODO test advanceWorkflowProcess()
		WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();
		try {
			provider.advanceWorkflow(processAdvancementData.getProcessId(), processAdvancementData.getUserId(), processAdvancementData.getActionRequested(), processAdvancementData.getComment(), RequestInfo.get().getEditCoordinate());
		} catch (Exception e) {
			throw new RestException("Failed advancing workflow process with " + (processAdvancementData != null ? processAdvancementData : "NULL"));
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
	@Path(RestPaths.updatePathComponent + RestPaths.addWorkflowUserRoleComponent)
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
	 * Removes a component from a process {@link RestWorkflowProcess} where the component had been
	 * previously saved and associated with. In doing so, reverts the component
	 * to its original state prior to the saves associated with the component.
	 * 
	 * The revert is performed by adding new versions to ensure that the
	 * component attributes are identical prior to any modification associated
	 * with the process. Note that nothing prevents future edits to be performed
	 * upon the component associated with the same process.
	 * 
	 * Used when component is removed from the process' component details panel
	 * 
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.removeComponentFromWorkflowComponent)
	public void removeComponentFromWorkflow(
			RestWorkflowProcessComponentSpecificationData specifiedComponent) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		
		// TODO test removeComponentFromWorkflow()
		WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();
		try {
			provider.removeComponentFromWorkflow(
					specifiedComponent.getProcessId(),
					RequestInfoUtils.getNidFromParameter("RestWorkflowComponentSpecificationData.componentNid", specifiedComponent.getComponentNid()), RequestInfo.get().getEditCoordinate());
		} catch (Exception e) {
			throw new RestException("Failed removing component " + specifiedComponent + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
	
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.releaseWorkflowLockComponent)
	public void releaseWorkflowLock(
			RestUUID processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		
		// TODO test releaseWorkflowLock()
		try {
			ProcessDetail processDetails = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(processId.getValue());

			if (!processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Cannot acquire a process that is already locked");
			}
			
			WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();

			provider.setProcessOwner(processId.getValue(), BPMNInfo.UNOWNED_PROCESS);
		} catch (Exception e) {
			throw new RestException("Failed releasing lock on " + processId.getValue() + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.acquireWorkflowLockComponent)
	public void acquireWorkflowLock(
			RestWorkflowLockAquisitionData lockAquisitionData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.EDIT_TOKEN_PARAM_NAMES);
		
		// TODO test acquireWorkflowLock()
		try {
			ProcessDetail processDetails = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(lockAquisitionData.getProcessId());

			if (!processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Cannot acquire a process that is already locked");
			}
			
			WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();

			provider.setProcessOwner(lockAquisitionData.getProcessId(), lockAquisitionData.getUserId());
		} catch (Exception e) {
			throw new RestException("Failed aquiring lock on " + lockAquisitionData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}
