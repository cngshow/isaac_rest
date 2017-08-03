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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.provider.BPMNInfo;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.EditToken;

/**
 * {@link WorkflowWriteAPIs}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.EDITOR, PrismeRoleConstants.REVIEWER, PrismeRoleConstants.APPROVER, PrismeRoleConstants.DEPLOYMENT_MANAGER})
public class WorkflowWriteAPIs {
	private static Logger log = LogManager.getLogger(WorkflowWriteAPIs.class);

	/**
	 * Creates a new workflow process instance which will be processed via the rules governing
	 * the definition specified in the RestWorkflowProcessBaseCreate field.<br/><br/>
	 *
	 * Returns a renewed RestEditToken containing the new process id as the active process.<br/><br/>
	 *
	 * A user may not create a new process if they have an existing active process.
	 * An exception is thrown if the user already has an active process.<br/><br/>
	 *
	 * @param workflowProcessCreationData Structure containing data required when creating a new
	 * workflow process instance. Includes definitionId, name, and description.
	 * Workflow definition identifiers are returned from workflow/definition/all.
	 * @param editToken EditToken string returned by previous call to getEditToken()
	 * or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return RestWriteResponse containing renewed EditToken and UUID of newly created process instance
	 *
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent + RestPaths.createProcess)
	public RestWriteResponse createProcess(
			@QueryParam(RequestParameters.editToken) String editToken,
			RestWorkflowProcessBaseCreate workflowProcessCreationData
			) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		WorkflowProcessInitializerConcluder provider = RequestInfo.get().getWorkflow()
				.getWorkflowProcessInitializerConcluder();

		try {
			EditToken existingToken = RequestInfo.get().getEditToken();

			if (existingToken.getActiveWorkflowProcessId() == null) {
				UUID newProcessId = provider.createWorkflowProcess(
						workflowProcessCreationData.getDefinitionId(),
						Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfo.get().getEditToken().getAuthorSequence()).get(),
						workflowProcessCreationData.getName(),
						workflowProcessCreationData.getDescription());

				existingToken.updateActiveWorkflowProcessId(newProcessId);

				return new RestWriteResponse(
						existingToken.renewToken(),
						newProcessId,
						null,
						null);
			} else {
				throw new RestException("Cannot create a new process whilst another process is active. Suspend or release the active process and try again.");
			}
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
	 * Returns a renewed RestEditToken without an active process.
	 *
	 * If attempting to advance a process when no process is active, an exception is thrown.
	 *
	 * @param processAdvancementData RestWorkflowProcessAdvancementData process state isn't correctly reflected
	 * in the process detailsAdvancementData data containing workflow advancement information includes
	 * processId, action selected, and user comment
	 * @param editToken EditToken string returned by previous call to getEditToken()
	 * or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return RestWriteResponse containing renewed EditToken
	 *
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.advanceProcess)
	public RestWriteResponse advanceProcess(
			@QueryParam(RequestParameters.editToken) String editToken,
			RestWorkflowProcessAdvancementData processAdvancementData) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();

		try {
			EditToken existingToken = RequestInfo.get().getEditToken();

			if (existingToken.getActiveWorkflowProcessId() != null) {
				boolean correctActionRequested = provider.advanceWorkflow(existingToken.getActiveWorkflowProcessId(), Get.identifierService()
						.getUuidPrimordialFromConceptId(RequestInfo.get().getEditToken().getAuthorSequence()).get(),
						processAdvancementData.getActionRequested(), processAdvancementData.getComment(),
						RequestInfo.get().getEditCoordinate());

				if (! correctActionRequested) {
					throw new RestException("Invalid process advancement for process " + existingToken.getActiveWorkflowProcessId() + ": " + processAdvancementData);
				}

				return new RestWriteResponse(existingToken.renewToken());
			} else {
				throw new RestException("Cannot advance a process unless a process is active. Acquire the process active and try again.");
			}
		} catch (Exception e) {
			String msg = "Failed advancing workflow process with "
					+ (processAdvancementData != null ? processAdvancementData : "NULL");
			log.error(msg, e);
			throw new RestException(msg);
		}
	}

	/**
	 * Sets the owner of an existing workflow process instance. This same method can be used for two purposes:
	 * <br/>&nbsp;&nbsp;&nbsp;&nbsp; - Acquiring a lock: in this case, the ownerId is set to the current userId
	 * <br/>&nbsp;&nbsp;&nbsp;&nbsp; - Releasing a lock: in this case, the ownerId is set to BPMNInfo.UNOWNED_PROCESS
	 * <br/><br/>
	 * Returns a renewed RestEditToken with the requested process designated as the active process.
	 *
	 * An exception is thrown if attempting to acquire a process when the active process is already set.<br/>
	 * An exception is thrown if attempting to release a process and no process is designated as the active process.<br/>
	 *
	 * @param processId The process to lock or unlock
	 * @param editToken EditToken returned by previous write API call in a RestWriteResponse
	 * @param acquireLock String value of true to acquire or false to release a lock.
	 * @return RestWriteResponse containing renewed EditToken
	 *
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.process + RestPaths.lock + "{" + RequestParameters.processId + "}")
	public RestWriteResponse setProcessLock(
			@PathParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.editToken) String editToken,
			@QueryParam(RequestParameters.acquireLock) String acquireLock ) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId,
				RequestParameters.editToken,
				RequestParameters.acquireLock);

		boolean boolAcquireLock = RequestInfoUtils.parseBooleanParameter(RequestParameters.acquireLock, acquireLock);
		UUID uuidProcessId = RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId);

		try {
			// Verify that active process is in the process state to acquire or release
			EditToken existingToken = RequestInfo.get().getEditToken();

			if (!boolAcquireLock && existingToken.getActiveWorkflowProcessId() == null) {
				throw new RestException("No need to release a process if no process is currently active.");
			} else if (boolAcquireLock && existingToken.getActiveWorkflowProcessId() != null) {
				throw new RestException("Cannot acquire a process if another process is already active.  Suspend or release the active process and try again.");
			}

			// verify that process attempted to be acquired is not currently locked
			ProcessDetail processDetails = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(uuidProcessId);

			//check if process is already locked.
			if (boolAcquireLock && !processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Cannot acquire a process that is already locked");
				//check if process is not owned in which case it cannot be released
			} else if (!boolAcquireLock && processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS)) {
				throw new RestException("Current process state isn't correctly reflected in the process details");
			}

			// If acquire set the owner to user.  If release set owner to BPMNInfo.UNOWNED_PROCESS.
			UUID newLockOwner;
			if (boolAcquireLock) {
				newLockOwner = Get.identifierService()
						.getUuidPrimordialFromConceptId(RequestInfo.get().getEditToken().getAuthorSequence())
						.get();
				existingToken.updateActiveWorkflowProcessId(uuidProcessId);
			} else {
				newLockOwner = BPMNInfo.UNOWNED_PROCESS;
				existingToken.updateActiveWorkflowProcessId(null);
			}

			// Perform acquire or release
			WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();

			provider.setProcessOwner(uuidProcessId, newLockOwner);

			return new RestWriteResponse(existingToken.renewToken());
		} catch (Exception e) {
			String actionRequested;
			if (boolAcquireLock) {
				actionRequested = "acquire";
			} else {
				actionRequested = "release";
			}
			throw new RestException("Failed " + actionRequested + " lock on " + RequestInfo.get().getEditToken().getActiveWorkflowProcessId() + ". Caught " + e.getClass().getName()
					+ " " + e.getLocalizedMessage());
		}
	}

	/**
	 * Removes a component already changed within a workflow process instance.

	 * In doing so, reverts the component to its original state prior to the saves associated
	 * with the component. NOTE: The revert is performed by adding new versions to ensure that
	 * the component attributes are identical prior to any modification associated with the process.
	 * Nothing prevents future edits to be performed upon the component associated with
	 * the same process.

	 * The current active process remains active.

	 * If attempting to remove a component from a process when no process is active, an exception is thrown.
	 * @param id UUID or nid String of component to be removed
	 * @param editToken EditToken string returned by previous call to getEditToken() or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return RestWriteResponse containing renewed RestEditToken
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.processComponent + "{" + RequestParameters.id + "}")
	public RestWriteResponse removeComponentFromProcess(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken);
		int nid = RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.id, id);

		try {
			EditToken existingToken = RequestInfo.get().getEditToken();

			if (existingToken.getActiveWorkflowProcessId() != null) {
				WorkflowUpdater provider = RequestInfo.get().getWorkflow().getWorkflowUpdater();
				UUID processId = RequestInfo.get().getActiveWorkflowProcessId();
				EditCoordinate ec = RequestInfo.get().getEditCoordinate();
				provider.removeComponentFromWorkflow(processId,
						nid,
						ec);

				return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), null, nid, null);
			} else {
				throw new RestException("Cannot remove a component from a process unless the process is active. Acquire the process active and try again.");
			}
		} catch (Exception e) {
			log.error("Unexpected error", e);
			throw new RestException("Failed removing component " + nid + ". Caught " + e.getClass().getName()
					+ " " + e.getLocalizedMessage());
		}
	}

}

