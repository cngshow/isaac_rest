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

import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.provider.BPMNInfo;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowLockingData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessComponentSpecificationData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.EditTokens;

/**
 * {@link WorkflowWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent)
public class WorkflowWriteAPIs {
	private static Logger log = LogManager.getLogger(WorkflowWriteAPIs.class);

	/**
	 * Creates a new workflow process instance which will be processed via the
	 * rules governing the definition specified in the
	 * RestWorkflowProcessBaseCreate field.
	 * 
	 * @param workflowProcessCreationData
	 *            Structure containing data required when creating a new
	 *            workflow process instance. Includes definitionId, name, and
	 *            description
	 * @param editToken
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * 
	 * @return RestUUID - Id of newly created process instance
	 * 
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent + RestPaths.createProcess)
	public RestWriteResponse createProcess(
			RestWorkflowProcessBaseCreate workflowProcessCreationData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		WorkflowProcessInitializerConcluder provider = RequestInfo.get().getWorkflow()
				.getWorkflowProcessInitializerConcluder();
		try {
			return new RestWriteResponse(
					EditTokens.renew(RequestInfo.get().getEditToken()),
					provider.createWorkflowProcess(workflowProcessCreationData.getDefinitionId(),
							Get.identifierService()
									.getUuidPrimordialFromConceptSequence(
											RequestInfo.get().getEditToken().getAuthorSequence())
									.get(),
							workflowProcessCreationData.getName(), workflowProcessCreationData.getDescription()),
					null,
					null);
		} catch (Exception e) {
			String msg = "Failed creating new workflow process from "
					+ (workflowProcessCreationData != null ? workflowProcessCreationData : "NULL");
			log.error(msg, e);
			throw new RestException(msg);
		}
	}

	/**
	 * Advances an existing workflow process instance with the specified action.
	 * 
	 * Returns a renewed RestEditToken.
	 * 
	 * @param processAdvancementData
	 *            RestWorkflowProcessAdvancementData Data containing workflow
	 *            advancement information includes processId, action selected,
	 *            and user comment
	 * @param editToken
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * 
	 * @return renewed EditToken
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.advanceProcess)
	public RestWriteResponse advanceProcess(
			RestWorkflowProcessAdvancementData processAdvancementData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		// TODO test advanceWorkflowProcess()
		WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();
		try {
			provider.advanceWorkflow(processAdvancementData.getProcessId(), Get.identifierService()
					.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get(),
					processAdvancementData.getActionRequested(), processAdvancementData.getComment(),
					RequestInfo.get().getEditCoordinate());

			return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()));
		} catch (Exception e) {
			String msg = "Failed advancing workflow process with "
					+ (processAdvancementData != null ? processAdvancementData : "NULL");
			log.error(msg, e);
			throw new RestException(msg);
		}
	}

	/**
	 * Sets the owner of an existing workflow process instance. This same method
	 * can be used for two purposes: - Acquiring a lock: in this case, the
	 * ownerId is the current userId - Releasing a lock: In this case, the
	 * ownerId set is BPMNInfo.UNOWNED_PROCESS
	 * 
	 * Returns a renewed RestEditToken.
	 * 
	 * @param lockingData
	 *            RestWorkflowLockingData Data containing process being updated
	 *            and request type (release or acquire).
	 * @param editToken
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * 
	 * @return renewed EditToken
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.lock)
	public RestWriteResponse setProcessLock(
			RestWorkflowLockingData lockingData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		// TODO test acquireWorkflowLock()
		try {
			ProcessDetail processDetails = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(lockingData.getProcessId());

			// verify that lock is in proper state per request
			if (lockingData.isAquireLock() && !processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Cannot acquire a process that is already locked");
			} else if (!lockingData.isAquireLock() && processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Cannot release a process that is not currently locked");
			}

			// Set owner based on acquire or release request
			UUID newLockOwner;
			if (lockingData.isAquireLock()) {
				newLockOwner = Get.identifierService()
						.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence())
						.get();
			} else {
				newLockOwner = BPMNInfo.UNOWNED_PROCESS;
			}

			// Perform acquire or release
			WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();

			provider.setProcessOwner(lockingData.getProcessId(), newLockOwner);
			
			return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()));
		} catch (Exception e) {
			throw new RestException("Failed aquiring lock on " + lockingData + ". Caught " + e.getClass().getName()
					+ " " + e.getLocalizedMessage());
		}
	}

	/**
	 * Removes a component already changed within a workflow process instance.
	 * 
	 * In doing so, reverts the component to its original state prior to the
	 * saves associated with the component. NOTE: The revert is performed by
	 * adding new versions to ensure that the component attributes are identical
	 * prior to any modification associated with the process. Note that nothing
	 * prevents future edits to be performed upon the component associated with
	 * the same process.
	 * 
	 * @param component
	 *            RestWorkflowProcessComponentSpecificationData Data containing
	 *            processId and componentId
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.removeComponent)
	public void removeComponentFromProcess(RestWorkflowProcessComponentSpecificationData component)
			throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		// TODO test removeComponentFromWorkflow()
		WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();
		try {
			provider.removeComponentFromWorkflow(component.getProcessId(),
					RequestInfoUtils.getNidFromParameter("RestWorkflowComponentSpecificationData.componentNid",
							component.getComponentNid()),
					RequestInfo.get().getEditCoordinate());
		} catch (Exception e) {
			throw new RestException("Failed removing component " + component + ". Caught " + e.getClass().getName()
					+ " " + e.getLocalizedMessage());
		}
	}
}
