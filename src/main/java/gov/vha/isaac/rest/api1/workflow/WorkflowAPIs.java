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
import java.util.SortedSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.metacontent.workflow.contents.ProcessDetail;
import gov.vha.isaac.metacontent.workflow.contents.ProcessHistory;
import gov.vha.isaac.rest.api.data.wrappers.RestBoolean;
import gov.vha.isaac.rest.api.data.wrappers.RestStrings;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableActions;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinitionDetail;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistories;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMap;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
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
	/**
	 * Return the definition for the specified definition id
	 * 
	 * @param wfDefinitionId - UUID id for definition
	 * @return RestWorkflowDefinitionDetail
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.definitionComponent)
	public RestWorkflowDefinitionDetail getDefinition(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId);

		try {
		return new RestWorkflowDefinitionDetail(
				WorkflowProviderManager.getWorkflowAccessor().getDefinitionDetails(
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
	 * Return the process for the specified process id
	 * 
	 * @param wfProcessId - UUID id for process
	 * @return RestWorkflowProcessDetail
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.processComponent)
	public RestWorkflowProcess getProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
			return new RestWorkflowProcess(
				WorkflowProviderManager.getWorkflowAccessor().getProcessDetails(
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
	 * Return a sorted set of distinct workflow histories for the specified process id
	 * 
	 * @return RestWorkflowProcessHistories sorted set of distinct workflow histories for a specified process
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.historiesForProcessComponent)
	public RestWorkflowProcessHistories getHistoriesForProcess(
			@QueryParam(RequestParameters.wfProcessId) String wfProcessId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfProcessId);

		try {
			SortedSet<ProcessHistory> ochreSet = WorkflowProviderManager.getWorkflowAccessor().getProcessHistory(
				RequestInfoUtils.parseUuidParameter(wfProcessId, wfProcessId));

			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
			
			ochreSet.stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
			
			return new RestWorkflowProcessHistories(restList);
		} catch (Exception e) {
			String msg = "Failed retrieving the ordered set of process histories by process id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Return RestBoolean indicating whether active process exists for the specified concept or sememe NID or UUID
	 * 
	 * @param nid - NID or UUID id for CONCEPT or SEMEME
	 * @return RestBoolean
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.isComponentInActiveWorkflowComponent)
	public RestBoolean isComponentInActiveWorkflow(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.nid) String nid) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.nid);
		try {
			return new RestBoolean(
					WorkflowProviderManager.getWorkflowAccessor().isComponentInActiveWorkflow(
							RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId),
							RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.nid, nid)));
		} catch (RestException re) {
			throw re;
		} catch (Exception e) {
			String msg = "failed determining if component with id=" + nid + " is in active workflow for definition " + wfDefinitionId;
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
	public RestStrings getPermissionsForDefinitionAndUser(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfUserId);

		try {
			List<String> roles = new ArrayList<>();
			WorkflowProviderManager.getWorkflowAccessor().getUserRoles(
					RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId), 
					RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEachOrdered(a -> roles.add(a));
			return new RestStrings(roles);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of permissions for the specified workflow definition " + wfDefinitionId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Return advancable process information,
	 * which is a map of process histories by process,
	 * for a specified definition and user
	 * 
	 * @return RestWorkflowProcessHistoriesMap map of lists of distinct workflow histories by process
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.advanceableProcessInformationComponent)
	public RestWorkflowProcessHistoriesMap getAdvanceableProcessInformation(
			@QueryParam(RequestParameters.wfDefinitionId) String wfDefinitionId,
			@QueryParam(RequestParameters.wfUserId) String wfUserId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.wfDefinitionId,
				RequestParameters.wfUserId);
		try {
			Map<Object, List<RestWorkflowProcessHistory>> map = new HashMap<>();
			Map<ProcessDetail, SortedSet<ProcessHistory>> ochreMap = WorkflowProviderManager.getWorkflowAccessor().getAdvanceableProcessInformation(
					RequestInfoUtils.parseUuidParameter(RequestParameters.wfDefinitionId, wfDefinitionId),
					RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId));

			for (Map.Entry<ProcessDetail, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
				List<RestWorkflowProcessHistory> restList = new ArrayList<>();
				ochreMapEntry.getValue().stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
				map.put(ochreMapEntry.getKey(), restList);
			}
			return new RestWorkflowProcessHistoriesMap(map);
		} catch (Exception e) {
			String msg = "Failed retrieving the map of process histories by process for definition id " + wfDefinitionId + " and user id " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Return the set of available actions for the specified workflow process and user
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
		WorkflowProviderManager.getWorkflowAccessor().getUserPermissibleActionsForProcess(
				RequestInfoUtils.parseUuidParameter(RequestParameters.wfProcessId, wfProcessId), 
				RequestInfoUtils.parseIntegerParameter(RequestParameters.wfUserId, wfUserId)).stream().forEachOrdered(a -> actions.add(new RestWorkflowAvailableAction(a)));
		return new RestWorkflowAvailableActions(actions);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of actions for the specified workflow process " + wfProcessId + " and user " + wfUserId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
}
