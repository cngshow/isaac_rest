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
import gov.vha.isaac.ochre.api.metacontent.workflow.StorableWorkflowContents.SubjectMatter;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessDetailSubjectMatterType;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessCancellationData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessConceptsAdditionData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessConclusionData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessStampAdditionData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowRoleChangeData;
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

	// WorkflowProcessInitializerConcluder

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
					workflowProcessCreationData.creatorId,
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
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.launchWorkflowProcessComponent)
	public void launchWorkflowProcess(
			RestUUID processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test launchWorkflowProcess()
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			provider.launchWorkflowProcess(processId.value);
		} catch (Exception e) {
			throw new RestException("Failed launching workflow process " + (processId != null ? processId : null));
		}
	}

	/**
	 * 
	 * Cancel a workflow process
	 * 
	 * @param cancellationData RestWorkflowProcessCancellationData wokflow cancellation data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.cancelWorkflowProcessComponent)
	public void cancelWorkflowProcess(
			RestWorkflowProcessCancellationData cancellationData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test cancelWorkflowProcess()
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			provider.cancelWorkflowProcess(cancellationData.processId, cancellationData.userId, cancellationData.comment);
		} catch (Exception e) {
			throw new RestException("Failed cancelling workflow process with " + (cancellationData != null ? cancellationData : null));
		}
	}

	/**
	 * 
	 * Conclude a workflow process
	 * 
	 * @param conclusionData RestWorkflowProcessConclusionData workflow conclusion data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.concludeWorkflowProcessComponent)
	public void concludeWorkflowProcess(
			RestWorkflowProcessConclusionData conclusionData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test concludeWorkflowProcess()
		WorkflowProcessInitializerConcluder provider = WorkflowProviderManager.getWorkflowProcessInitializerConcluder();
		try {
			provider.concludeWorkflowProcess(conclusionData.processId, conclusionData.userId);
		} catch (Exception e) {
			throw new RestException("Failed concluding workflow process with " + (conclusionData != null ? conclusionData : null));
		}
	}
	
	// WorkflowUpdater

	/**
	 * 
	 * Add a stamp to an existing workflow process
	 * 
	 * @param stampAdditionData RestWorkflowProcessStampAdditionData workflow stamp addition data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.addStampToExistingWorkflowProcessComponent)
	public void addStampToExistingWorkflowProcess(
			RestWorkflowProcessStampAdditionData stampAdditionData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test addStampToExistingWorkflowProcess()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			provider.addStampToExistingProcess(stampAdditionData.processId, stampAdditionData.stampSequence);
		} catch (Exception e) {
			throw new RestException("Failed adding stamp to workflow process with " + (stampAdditionData != null ? stampAdditionData : null));
		}
	}

	/**
	 * 
	 * Add concepts to an existing workflow process
	 * 
	 * @param conceptsAdditionData RestWorkflowProcessConceptsAdditionData workflow concepts addition data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.addConceptsToExistingWorkflowProcessComponent)
	public void addConceptsToExistingWorkflowProcess(
			RestWorkflowProcessConceptsAdditionData conceptsAdditionData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test addConceptsToExistingWorkflowProcess()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			provider.addConceptsToExistingProcess(conceptsAdditionData.processId, conceptsAdditionData.conceptSequences);
		} catch (Exception e) {
			throw new RestException("Failed adding concepts to workflow process with " + (conceptsAdditionData != null ? conceptsAdditionData : null));
		}
	}

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
	public RestUUID advanceWorkflowProcess(
			RestWorkflowProcessAdvancementData processAdvancementData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test advanceWorkflowProcess()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			return new RestUUID(provider.advanceWorkflow(processAdvancementData.processId, processAdvancementData.userId, processAdvancementData.actionRequested, processAdvancementData.comment));
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
			return new RestUUID(provider.addNewUserRole(roleData.definitionId, roleData.userId, roleData.role));
		} catch (Exception e) {
			throw new RestException("Failed adding role to user with " + (roleData != null ? roleData : null));
		}
	}

	/**
	 * 
	 * Remove role from user
	 * 
	 * @param roleData RestWorkflowRoleChangeData workflow definition user role change data
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.removeWorkflowUserRoleComponent)
	public void removeWorkflowUserRole(
			RestWorkflowRoleChangeData roleData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());
		
		// TODO test removeWorkflowUserRole()
		WorkflowUpdater provider = WorkflowProviderManager.getWorkflowUpdater();
		try {
			provider.removeUserRole(roleData.definitionId, roleData.userId, roleData.role);
		} catch (Exception e) {
			throw new RestException("Failed removing role from user with " + (roleData != null ? roleData : null));
		}
	}
}
