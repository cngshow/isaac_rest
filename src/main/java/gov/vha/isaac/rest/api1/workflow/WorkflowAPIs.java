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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.UserRoleService;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessHistory;
import gov.vha.isaac.ochre.workflow.provider.BPMNInfo;
import gov.vha.isaac.rest.api.data.wrappers.RestBoolean;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestUserRoleType;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinitionDetail;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMapEntry;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * {@link WorkflowAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.workflowAPIsPathComponent)
public class WorkflowAPIs {
	private static Logger log = LogManager.getLogger(WorkflowAPIs.class);

	/**
	 * Gets the {@link RestWorkflowProcess} for the specified process key
	 * 
	 * Used to access all information associated with a given workflow process
	 * (i.e. an instance of a {@link RestWorkflowDefinitionDetail}).
	 * 
	 * @param wfProcessId
	 *            - UUID workflow-specific id for process
	 * @return RestWorkflowProcess
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processComponent)
	public RestWorkflowProcess getProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId)
			throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
			return new RestWorkflowProcess(RequestInfo.get().getWorkflow().getWorkflowAccessor().getProcessDetails(
					RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the process for the specified process id " + wfProcessId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Returns all {@link RestWorkflowProcessHistory} entries associated with the {@link RestWorkflowProcess}
	 * (an instance of a {@link RestWorkflowDefinitionDetail}) specified by the workflow-specific process id.
	 * This contains all the advancements made during the given process.
	 * The result is sorted by advancement time, with last being most recent.
	 * 
	 * @return {@link RestWorkflowProcessHistory} list which is a sorted set of distinct workflow
	 *         histories for a specified process
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesForProcessComponent)
	public RestWorkflowProcessHistory[] getHistoriesForProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
			SortedSet<ProcessHistory> ochreSet = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessHistory(RequestInfoUtils.parseUuidParameter(wfProcessId, wfProcessId));

			List<RestWorkflowProcessHistory> restList = new ArrayList<>();

			ochreSet.stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));

			return restList.toArray(new RestWorkflowProcessHistory[restList.size()]);
		} catch (Exception e) {
			String msg = "Failed retrieving the ordered set of process histories by process id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Map the list of process history {@link RestWorkflowProcessHistory} by each process {@link RestWorkflowProcess}
	 * for which the user's permissions/entitlements enable them to advance workflow based on the process' current state.
	 * Only active processes can be advanced thus only those processes with such
	 * a status are returned.
	 * 
	 * Used to determine which processes to list when the user selects the
	 * "Author Workflows" link
	 * 
	 * @return a list of RestWorkflowProcessHistoriesMapEntry items that gives the distinct workflow
	 *         histories ({@link RestWorkflowProcessHistory}) by process ({@link RestWorkflowProcess})
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.advanceableProcessInformationComponent)
	public RestWorkflowProcessHistoriesMapEntry[] getAdvanceableProcessInformation(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId, RequestParameters.editToken);
		UUID userId = null;
		try {
			userId = Get.identifierService().getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
			ArrayList<RestWorkflowProcessHistoriesMapEntry> entries = new ArrayList<>();
			Map<ProcessDetail, SortedSet<ProcessHistory>> ochreMap = RequestInfo.get().getWorkflow().getWorkflowAccessor().getAdvanceableProcessInformation(
					RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId),
					userId);

			for (Map.Entry<ProcessDetail, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
				List<RestWorkflowProcessHistory> restList = new ArrayList<>();
				ochreMapEntry.getValue().stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
				entries.add(new RestWorkflowProcessHistoriesMapEntry(new RestWorkflowProcess(ochreMapEntry.getKey()), restList.toArray(new RestWorkflowProcessHistory[restList.size()])));
			}
			return entries.toArray(new RestWorkflowProcessHistoriesMapEntry[entries.size()]);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of process histories by process for definition id " + wfDefinitionId + " and user id " + userId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the set of available actions for the specified workflow process
	 * and user
	 * 
	 * Used to determine which actions populate the Transition Workflow picklist
	 * 
	 * @param wfProcessId
	 *            - UUID workflow-specific id for workflow process {@link RestWorkflowProcess}
	 * @param editToken
	 *            - String serialization of EditToken identifying a workflow user
	 * @return RestWorkflowAvailableActions list of distinct actions
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.actionsForProcessAndUserComponent)
	public RestWorkflowAvailableAction[] getActionsForProcessAndUser(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId, RequestParameters.editToken);

		UUID userId = null;
		try {
			userId = Get.identifierService().getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
			List<RestWorkflowAvailableAction> actions = new ArrayList<>();
			RequestInfo.get().getWorkflow().getWorkflowAccessor().getUserPermissibleActionsForProcess(
							RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId),
							userId)
					.stream().forEachOrdered(a -> actions.add(new RestWorkflowAvailableAction(a)));
			return actions.toArray(new RestWorkflowAvailableAction[actions.size()]);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of actions for the specified workflow process " + wfProcessId
					+ " and user " + userId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Return the default workflow definition UUID
	 * 
	 * @return default definition UUID
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.defaultDefinitionComponent)
	public RestUUID getDefaultDefinition() {
		return new RestUUID(RequestInfo.get().getWorkflow().getDefinitionDetailStore().keySet().iterator().next());
	}

	/**
	 * Return the available workflow definition UUIDs
	 * 
	 * @return available definition UUIDs
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.availableDefinitionsComponent)
	public RestUUID[] getAvailableDefinitions() {
		ArrayList<RestUUID> temp = new ArrayList<>();
		for (UUID uuid : RequestInfo.get().getWorkflow().getDefinitionDetailStore().keySet())
		{
			temp.add(new RestUUID(uuid));
		}
		return temp.toArray(new RestUUID[temp.size()]);
	}

	/**
	 * Return the the locked state of the specified process
	 * 
	 * @return RestBoolean containing true iff locked, else containing false
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowLockStateComponent)
	public RestBoolean isWorkflowLocked(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId)
			throws RestException {
		RequestParameters.validateParameterNamesAgainstSupportedNames(RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
			ProcessDetail processDetails = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId));
			return new RestBoolean(processDetails.getOwnerId().equals(BPMNInfo.UNOWNED_PROCESS));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the process for the specified process id " + wfProcessId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	} 
	

	/**
	 * Return all potential user roles
	 * 
	 * @return array of role names
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.allRolesComponent)
	public RestUserRoleType[] getAllRoles() {
		UserRoleService userService = LookupService.getService(UserRoleService.class);

		Set<RestUserRoleType> restRoles = new HashSet<>();
		for (UserRole role : userService.getAllUserRoles()) {
			restRoles.add(new RestUserRoleType(role));
		}
		return restRoles.toArray(new RestUserRoleType[restRoles.size()]);
	}
}
