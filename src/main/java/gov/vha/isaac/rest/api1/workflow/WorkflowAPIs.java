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
import java.util.HashMap;
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

import gov.vha.isaac.metacontent.workflow.contents.ProcessHistory;
import gov.vha.isaac.rest.api.data.wrappers.RestStrings;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableActions;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistories;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMap;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowUserPermission;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowUserPermissions;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link WorkflowAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.workflowAPIsPathComponent)
public class WorkflowAPIs
{
	// WorkflowActionsPermissionsAccessor
	/**
	 * Return the list of roles for the specified workflow definition and user
	 * 
	 * @param wfDefinitionId - UUID id for workflow definition
	 * @param wfUserId - Integer id for workflow user
	 * @return RestStrings list of distinct workflow roles
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.rolesForDefinitionAndUserComponent)
	public RestStrings getRolesForDefinitionAndUser(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfUserId);

		return new RestStrings(WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getUserRoles(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)));
	}

	/**
	 * Return the list of user permissions for the specified workflow definition and user
	 * 
	 * @param wfDefinitionId - UUID id for workflow definition
	 * @param wfUserId - Integer id for workflow user
	 * @return RestWorkflowUserPermissions list of distinct workflow user permissions
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.permissionsForDefinitionAndUserComponent)
	public RestWorkflowUserPermissions getPermissionsForDefinitionAndUser(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfUserId);

		List<RestWorkflowUserPermission> permissions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getAllPermissionsForUser(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEach(a -> permissions.add(new RestWorkflowUserPermission(a)));
		return new RestWorkflowUserPermissions(permissions);
	}

	/**
	 * Return the list of available actions for the specified workflow definition and state
	 * 
	 * @param wfDefinitionId - UUID id for workflow definition
	 * @param wfState - String representation of a workflow state
	 * @return RestWorkflowAvailableActions list of distinct actions
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.actionsForDefinitionAndStateComponent)
	public RestWorkflowAvailableActions getActionsForDefinitionAndState(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfState) String wfState) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfState);

		List<RestWorkflowAvailableAction> actions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getAvailableActionsForState(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				wfState).stream().forEach(a -> actions.add(new RestWorkflowAvailableAction(a)));
		return new RestWorkflowAvailableActions(actions);
	}

	/**
	 * Return the list of available actions for the specified workflow process and user
	 * 
	 * @param wfProcessId - UUID id for workflow process
	 * @param wfUserId - Integer id for a workflow user
	 * @return RestWorkflowAvailableActions list of distinct actions
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.actionsForProcessAndUserComponent)
	public RestWorkflowAvailableActions getActionsForProcessAndUser(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId,
				RequestParameters.wfUserId);

		List<RestWorkflowAvailableAction> actions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getUserPermissibleActionsForProcess(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEach(a -> actions.add(new RestWorkflowAvailableAction(a)));
		return new RestWorkflowAvailableActions(actions);
	}

	/**
	 * Return the map of process histories by role for the specified workflow definition and user
	 * 
	 * @param wfDefinitionId - UUID id for workflow definition
	 * @param wfUserId - Integer id for workflow user
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by role
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesLatestActiveByRoleMapForDefinitionAndUserComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesLatestActiveByRoleMapForDefinitionAndUser(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfUserId);

		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<String, Set<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getLatestActivePermissibleByRole(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId));
		
		for (Map.Entry<String, Set<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
	}
	
	// WorkflowHistoryAccessor
	/**
	 * Return the map of active process histories by process id
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by process id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesActiveByProcessMapComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesActiveByProcessMap() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());

		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveByProcess();
		
		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
	}

	/**
	 * Return the map of active process histories by process id
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by process id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesActiveByDefinitionMapComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesActiveByDefinitionMap() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());

		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveByDefinition();
		
		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
	}
	
	/**
	 * Return the list of roles for the specified workflow definition and user
	 * 
	 * @param wfDefinitionId - UUID id for workflow definition
	 * @param wfUserId - Integer id for workflow user
	 * @return RestStrings list of distinct workflow roles
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesActiveForConceptComponent)
	public RestWorkflowProcessHistories getHistoriesActiveForConcept(
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id);

		List<RestWorkflowProcessHistory> set = new ArrayList<>();
		WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveForConcept(
				RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)).forEach(a -> set.add(new RestWorkflowProcessHistory(a)));

		return new RestWorkflowProcessHistories(set);
	}
}
