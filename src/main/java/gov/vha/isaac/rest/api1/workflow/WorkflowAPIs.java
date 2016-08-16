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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.metacontent.workflow.contents.ProcessHistory;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.rest.api.data.wrappers.RestBoolean;
import gov.vha.isaac.rest.api.data.wrappers.RestStrings;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableActions;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinitionDetail;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcesses;
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
	private static Logger log = LogManager.getLogger(WorkflowAPIs.class);

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

		try {
			return new RestStrings(WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getUserRoles(
					RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
					RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of roles for the specified workflow definition " + wfDefinitionId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
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

		try {
		List<RestWorkflowUserPermission> permissions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getAllPermissionsForUser(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEach(a -> permissions.add(new RestWorkflowUserPermission(a)));
		return new RestWorkflowUserPermissions(permissions);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of permissions for the specified workflow definition " + wfDefinitionId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
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

		try {
		List<RestWorkflowAvailableAction> actions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getAvailableActionsForState(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
				wfState).stream().forEach(a -> actions.add(new RestWorkflowAvailableAction(a)));
		return new RestWorkflowAvailableActions(actions);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of actions for the specified workflow definition " + wfDefinitionId + " and state " + wfState;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
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

		try {
		List<RestWorkflowAvailableAction> actions = new ArrayList<>();
		WorkflowProviderManager.getWorkflowActionsPermissionsAccessor().getUserPermissibleActionsForProcess(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEach(a -> actions.add(new RestWorkflowAvailableAction(a)));
		return new RestWorkflowAvailableActions(actions);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of actions for the specified workflow process " + wfProcessId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
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

		try {
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
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the map of process histories by role for the specified workflow definition " + wfDefinitionId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
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

		try {
		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveByProcess();

		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of active process histories by process id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the map of active process histories by definition id
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by definition id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesActiveByDefinitionMapComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesActiveByDefinitionMap() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());

		try {
		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveByDefinition();

		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of active process histories by definition id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the list of active process histories for the specified concept id
	 * 
	 * @param id - sequence, NID or UUID id for concept
	 * @return RestWorkflowProcessHistories list of distinct active RestWorkflowProcessHistory entries
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

		try {
		List<RestWorkflowProcessHistory> set = new ArrayList<>();
		WorkflowProviderManager.getWorkflowHistoryAccessor().getActiveForConcept(
				RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)).forEach(a -> set.add(new RestWorkflowProcessHistory(a)));

		return new RestWorkflowProcessHistories(set);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "the list of active process histories for the specified concept id " + id;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the map of process histories by process id
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by process id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesByProcessMapComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesByProcessMap() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());

		try {
		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getByProcessMap();

		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of process histories by process id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the map of process histories by definition id
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by definition id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesByDefinitionMapComponent)
	public RestWorkflowProcessHistoriesMap getHistoriesByDefinitionMap() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters());

		try {
		Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
		Map<UUID, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowHistoryAccessor().getByDefinitionMap();

		for (Map.Entry<UUID, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			ochreMapEntry.getValue().stream().forEach(a -> restList.add(new RestWorkflowProcessHistory(a)));
			map.put(ochreMapEntry.getKey(), restList);
		}
		return new RestWorkflowProcessHistoriesMap(map);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of process histories by definition id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the list of process histories for the specified concept id
	 * 
	 * @param id - sequence, NID or UUID id for concept
	 * @return RestWorkflowProcessHistories list of distinct RestWorkflowProcessHistory entries
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesForConceptComponent)
	public RestWorkflowProcessHistories getHistoriesForConcept(
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id);

		try {
		List<RestWorkflowProcessHistory> set = new ArrayList<>();
		WorkflowProviderManager.getWorkflowHistoryAccessor().getForConcept(
				RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)).forEach(a -> set.add(new RestWorkflowProcessHistory(a)));

		return new RestWorkflowProcessHistories(set);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the list of process histories for the specified concept id " + id;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the latest process history for the specified process id
	 * 
	 * @param wfProcessId - UUID id for process
	 * @return RestWorkflowProcessHistory latest process history for the specified process id
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historyLatestForProcessComponent)
	public RestWorkflowProcessHistory getHistoryLatestForProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
		return new RestWorkflowProcessHistory(
				WorkflowProviderManager.getWorkflowHistoryAccessor().getLatestForProcess(
						RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the latest process history for the specified process id " + wfProcessId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	// WorkflowStatusAccessor
	/**
	 * Return the list of process details for the specified concept id
	 * 
	 * @param id - sequence, NID or UUID id for concept
	 * @return RestWorkflowProcessDetails list of distinct RestWorkflowProcessDetail entries
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processesForConceptComponent)
	public RestWorkflowProcesses getProcessesForConcept(
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id);

		try {
		List<RestWorkflowProcess> set = new ArrayList<>();
		WorkflowProviderManager.getWorkflowStatusAccessor().getProcessesForConcept(
				RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)).forEach(a -> set.add(new RestWorkflowProcess(a)));

		return new RestWorkflowProcesses(set);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the list of process details for the specified concept id " + id;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the process detail for the specified process id
	 * 
	 * @param wfProcessId - UUID id for process
	 * @return RestWorkflowProcessDetail
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processForProcessComponent)
	public RestWorkflowProcessBaseCreate getProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
		return new RestWorkflowProcess(
				WorkflowProviderManager.getWorkflowStatusAccessor().getProcessDetail(
						RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the process detail for the specified process id " + wfProcessId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the list of active process details for the specified definition
	 * 
	 * @param wfDefinitionId - UUID id for process
	 * @return RestWorkflowProcessDetails list of distinct RestWorkflowProcessDetail entries
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processesActiveForDefinitionComponent)
	public RestWorkflowProcesses getProcessesActiveForDefinition(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId);

		try {
		List<RestWorkflowProcess> set = new ArrayList<>();
		WorkflowProviderManager.getWorkflowStatusAccessor().getActiveProcessesForDefinition(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId)).forEach(a -> set.add(new RestWorkflowProcess(a)));

		return new RestWorkflowProcesses(set);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the list of active process details for the specified definition " + wfDefinitionId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the definition detail for the specified definition id
	 * 
	 * @param wfDefinitionId - UUID id for definition
	 * @return RestWorkflowDefinitionDetail
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.definitionForDefinitionComponent)
	public RestWorkflowDefinitionDetail getDefinitionForDefinition(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId);

		try {
		return new RestWorkflowDefinitionDetail(
				WorkflowProviderManager.getWorkflowStatusAccessor().getDefinition(
						RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the definition detail for the specified definition id " + wfDefinitionId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return RestBoolean indicating whether active process exists for the specified concept id
	 * 
	 * @param id - sequence, NID or UUID id for concept
	 * @return RestBoolean
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.isConceptInActiveWorkflowComponent)
	public RestBoolean isConceptInActiveWorkflow(
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id);

		try {
		return new RestBoolean(
				WorkflowProviderManager.getWorkflowStatusAccessor().isConceptInActiveWorkflow(
						RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving RestBoolean indicating whether active process exists for the specified concept id " + id;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return RestBoolean indicating whether active process exists for the specified concept id XOR sememe sequence
	 * 
	 * @param objType - "CONCEPT" or "SEMEME" (case insensitive)
	 * @param id - sequence, NID or UUID id for CONCEPT XOR sequence for SEMEME
	 * @return RestBoolean
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.isComponentInActiveWorkflowComponent)
	public RestBoolean isComponentInActiveWorkflow(
			@QueryParam(RequestParameters.objType) String objType,
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.objType,
				RequestParameters.id);

		OchreExternalizableObjectType typeToUse = null;

		try {
			RestObjectChronologyType restType = RestObjectChronologyType.valueOf(objType);
			if (restType.equals(new RestObjectChronologyType(ObjectChronologyType.CONCEPT))) {
				typeToUse = OchreExternalizableObjectType.CONCEPT;
			} else if (restType.equals(new RestObjectChronologyType(ObjectChronologyType.SEMEME))) {
				typeToUse = OchreExternalizableObjectType.SEMEME;
			} else {
				throw new RestException(RequestParameters.objType, objType, "unsupported objType=" + objType);
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(RequestParameters.objType, objType, "failed parsing objType=" + objType + ". " + e.getLocalizedMessage());
		}

		Integer sequence = null;
		try {
			switch (typeToUse) {
			case CONCEPT:
				sequence = RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id);
				break;
			case SEMEME:
				sequence = RequestInfoUtils.parseIntegerParameter(RequestParameters.id, id);
				break;
			case STAMP_ALIAS:
			case STAMP_COMMENT:
			default:
				throw new RestException(RequestParameters.id, id, "invalid " + (typeToUse == OchreExternalizableObjectType.CONCEPT ? "concept id" : "sememe sequence") + "=" + id);
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(RequestParameters.id, id, "failed parsing " + (typeToUse == OchreExternalizableObjectType.CONCEPT ? "concept id" : "sememe sequence") + "=" + id + ". " + e.getLocalizedMessage());
		}

		try {
			return new RestBoolean(
					WorkflowProviderManager.getWorkflowStatusAccessor().isComponentInActiveWorkflow(
							typeToUse,
							sequence));
		} catch (Exception e) {
			String msg = "failed determining if " + typeToUse + " with seq=" + sequence + " is in active workflow";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return the active process detail, if any, for the specified concept id
	 * 
	 * @param id - sequence, NID or UUID id for concept
	 * @return RestWorkflowProcessDetail
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processActiveForConceptComponent)
	public RestWorkflowProcessBaseCreate getActiveProcessForConcept(
			@QueryParam(RequestParameters.id) String id) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id);

		try {
		return new RestWorkflowProcess(
				WorkflowProviderManager.getWorkflowStatusAccessor().getActiveProcessForConcept(
						RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the active process detail, if any, for the specified concept id " + id;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
}
