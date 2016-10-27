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
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import gov.vha.isaac.ochre.workflow.model.contents.DefinitionDetail;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessHistory;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinition;
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
	 * Return all workflow definitions available on server
	 * 
	 * @return RestWorkflowDefinition Collection - Collection of all workflow definitions 
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.allDefinitions)
	public RestWorkflowDefinition[] getAllDefinitions() throws RestException {
		try {
			ArrayList<RestWorkflowDefinition> restList = new ArrayList<>();
			
			Collection<DefinitionDetail> ochreSet = RequestInfo.get().getWorkflow().getDefinitionDetailStore().values();
	
			ochreSet.stream().forEach(a -> restList.add(new RestWorkflowDefinition(a)));
			
			return restList.toArray(new RestWorkflowDefinition[restList.size()]);
		} catch (Exception e) {
			String msg = "Failed retrieving the definitions stored in the server";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return workflow process instance information not including the process history
	 * 
	 * @param processId UUID identifying a given workflow process instance
	 * @return RestWorkflowProcess - Workflow process instance information
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.process)
	public RestWorkflowProcess getProcess(
			@QueryParam(RequestParameters.processId) String processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId);

		try {
			return new RestWorkflowProcess(RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessDetails(RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId)));
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving the specified process id " + processId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return workflow process instance history. The history is sorted by advancement sequence, 
	 * with last being most recent advance operation
	 * 
	 * @param processId UUID identifying a given workflow process instance
	 * @return RestWorkflowProcessHistory Collection - Sorted collection of the process 
	 * instance's advancements
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.history)
	public RestWorkflowProcessHistory[] getProcessHistory(
			@QueryParam(RequestParameters.processId) String processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId);

		try {
			SortedSet<ProcessHistory> ochreSet = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessHistory(RequestInfoUtils.parseUuidParameter(processId, processId));

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
	 * Return the actions that the user may perform on the workflow process
	 * instance instance
	 * 
	 * @param editToken String serialization of EditToken identifying currently logged in user
	 * @param processId UUID identifying a given workflow process instance
	 * @return RestWorkflowAvailableAction Collection - Collection of distinct actions a user can perform
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.actions)
	public RestWorkflowAvailableAction[] getProcessActions(
			@QueryParam(RequestParameters.editToken) String editToken,
			@QueryParam(RequestParameters.processId) String processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken, 
				RequestParameters.processId);

		UUID userId = null;
		try {
			userId = Get.identifierService()
					.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
			List<RestWorkflowAvailableAction> actions = new ArrayList<>();
			RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getUserPermissibleActionsForProcess(
							RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId), userId)
					.stream().forEachOrdered(a -> actions.add(new RestWorkflowAvailableAction(a)));
			return actions.toArray(new RestWorkflowAvailableAction[actions.size()]);
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed retrieving list of actions for the specified workflow process " + processId
					+ " and user " + userId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return all active workflow process instances for which the user has proper permissions 
	 * to act upon. To minimize REST calls, the sorted history of each process is mapped in 
	 * the return object.
	 * 
	 * @param editToken String serialization of EditToken identifying currently logged in user
	 * @param definitionId UUID identifying a specific workflow definition
	 * @return RestWorkflowProcessHistoriesMapEntry Collection - Workflow process instances 
	 * mapped to their sorted history
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.available)
	public RestWorkflowProcessHistoriesMapEntry[] getAvailableProcesses(
			@QueryParam(RequestParameters.definitionId) String definitionId,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.definitionId, 
				RequestParameters.editToken);
		
		UUID userId = null;
		try {
			userId = Get.identifierService()
					.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
			ArrayList<RestWorkflowProcessHistoriesMapEntry> entries = new ArrayList<>();
			Map<ProcessDetail, SortedSet<ProcessHistory>> ochreMap = RequestInfo.get().getWorkflow()
					.getWorkflowAccessor().getAdvanceableProcessInformation(
							RequestInfoUtils.parseUuidParameter(RequestParameters.definitionId, definitionId), userId);

			for (Map.Entry<ProcessDetail, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
				List<RestWorkflowProcessHistory> restList = new ArrayList<>();
				ochreMapEntry.getValue().stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
				entries.add(new RestWorkflowProcessHistoriesMapEntry(new RestWorkflowProcess(ochreMapEntry.getKey()),
						restList.toArray(new RestWorkflowProcessHistory[restList.size()])));
			}
			return entries.toArray(new RestWorkflowProcessHistoriesMapEntry[entries.size()]);
		} catch (Exception e) {
			String msg = "Failed retrieving the process and their history that exist within a definition id "
					+ definitionId + " and may be acted upon by user id " + userId;
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}
}
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

// Dan shelved Workflow on 10/26/16
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.SortedSet;
//import java.util.UUID;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import gov.vha.isaac.ochre.api.Get;
//import gov.vha.isaac.ochre.workflow.model.contents.DefinitionDetail;
//import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
//import gov.vha.isaac.ochre.workflow.model.contents.ProcessHistory;
//import gov.vha.isaac.rest.api.exceptions.RestException;
//import gov.vha.isaac.rest.api1.RestPaths;
//import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
//import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinition;
//import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
//import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMapEntry;
//import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
//import gov.vha.isaac.rest.session.RequestInfo;
//import gov.vha.isaac.rest.session.RequestInfoUtils;
//import gov.vha.isaac.rest.session.RequestParameters;
//
///**
// * {@link WorkflowAPIs}
// * 
// * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
// */
//@Path(RestPaths.workflowAPIsPathComponent)
//public class WorkflowAPIs {
//
//	private static Logger log = LogManager.getLogger(WorkflowAPIs.class);
//
//	/**
//	 * Return all workflow definitions available on server
//	 * 
//	 * @return RestWorkflowDefinition Collection - Collection of all workflow definitions 
//	 * 
//	 * @throws RestException 
//	 */
//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.allDefinitions)
//	public RestWorkflowDefinition[] getAllDefinitions() throws RestException {
//		try {
//			ArrayList<RestWorkflowDefinition> restList = new ArrayList<>();
//			
//			Collection<DefinitionDetail> ochreSet = RequestInfo.get().getWorkflow().getDefinitionDetailStore().values();
//	
//			ochreSet.stream().forEach(a -> restList.add(new RestWorkflowDefinition(a)));
//			
//			return restList.toArray(new RestWorkflowDefinition[restList.size()]);
//		} catch (Exception e) {
//			String msg = "Failed retrieving the definitions stored in the server";
//			log.error(msg, e);
//			throw new RestException(msg + ". " + e.getLocalizedMessage());
//		}
//	}
//
//	/**
//	 * Return workflow process instance information not including the process history
//	 * 
//	 * @param processId UUID identifying a given workflow process instance
//	 * @return RestWorkflowProcess - Workflow process instance information
//	 * 
//	 * @throws RestException
//	 */
//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.process)
//	public RestWorkflowProcess getProcess(
//			@QueryParam(RequestParameters.processId) String processId) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters(),
//				RequestParameters.processId);
//
//		try {
//			return new RestWorkflowProcess(RequestInfo.get().getWorkflow().getWorkflowAccessor()
//					.getProcessDetails(RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId)));
//		} catch (RestException e) {
//			throw e;
//		} catch (Exception e) {
//			String msg = "Failed retrieving the specified process id " + processId;
//			log.error(msg, e);
//			throw new RestException(msg + ". " + e.getLocalizedMessage());
//		}
//	}
//
//	/**
//	 * Return workflow process instance history. The history is sorted by advancement sequence, 
//	 * with last being most recent advance operation
//	 * 
//	 * @param processId UUID identifying a given workflow process instance
//	 * @return RestWorkflowProcessHistory Collection - Sorted collection of the process 
//	 * instance's advancements
//	 * 
//	 * @throws RestException
//	 */
//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.history)
//	public RestWorkflowProcessHistory[] getProcessHistory(
//			@QueryParam(RequestParameters.processId) String processId) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters(),
//				RequestParameters.processId);
//
//		try {
//			SortedSet<ProcessHistory> ochreSet = RequestInfo.get().getWorkflow().getWorkflowAccessor()
//					.getProcessHistory(RequestInfoUtils.parseUuidParameter(processId, processId));
//
//			List<RestWorkflowProcessHistory> restList = new ArrayList<>();
//
//			ochreSet.stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
//
//			return restList.toArray(new RestWorkflowProcessHistory[restList.size()]);
//		} catch (Exception e) {
//			String msg = "Failed retrieving the ordered set of process histories by process id";
//			log.error(msg, e);
//			throw new RestException(msg + ". " + e.getLocalizedMessage());
//		}
//	}
//
//	/**
//	 * Return the actions that the user may perform on the workflow process
//	 * instance instance
//	 * 
//	 * @param editToken String serialization of EditToken identifying currently logged in user
//	 * @param processId UUID identifying a given workflow process instance
//	 * @return RestWorkflowAvailableAction Collection - Collection of distinct actions a user can perform
//	 * 
//	 * @throws RestException
//	 */
//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.actions)
//	public RestWorkflowAvailableAction[] getProcessActions(
//			@QueryParam(RequestParameters.editToken) String editToken,
//			@QueryParam(RequestParameters.processId) String processId) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters(),
//				RequestParameters.editToken, 
//				RequestParameters.processId);
//
//		UUID userId = null;
//		try {
//			userId = Get.identifierService()
//					.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
//			List<RestWorkflowAvailableAction> actions = new ArrayList<>();
//			RequestInfo.get().getWorkflow().getWorkflowAccessor()
//					.getUserPermissibleActionsForProcess(
//							RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId), userId)
//					.stream().forEachOrdered(a -> actions.add(new RestWorkflowAvailableAction(a)));
//			return actions.toArray(new RestWorkflowAvailableAction[actions.size()]);
//		} catch (RestException e) {
//			throw e;
//		} catch (Exception e) {
//			String msg = "Failed retrieving list of actions for the specified workflow process " + processId
//					+ " and user " + userId;
//			log.error(msg, e);
//			throw new RestException(msg + ". " + e.getLocalizedMessage());
//		}
//	}
//
//	/**
//	 * Return all active workflow process instances for which the user has proper permissions 
//	 * to act upon. To minimize REST calls, the sorted history of each process is mapped in 
//	 * the return object.
//	 * 
//	 * @param editToken String serialization of EditToken identifying currently logged in user
//	 * @param definitionId UUID identifying a specific workflow definition
//	 * @return RestWorkflowProcessHistoriesMapEntry Collection - Workflow process instances 
//	 * mapped to their sorted history
//	 * 
//	 * @throws RestException
//	 */
//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.available)
//	public RestWorkflowProcessHistoriesMapEntry[] getAvailableProcesses(
//			@QueryParam(RequestParameters.definitionId) String definitionId,
//			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters(),
//				RequestParameters.definitionId, 
//				RequestParameters.editToken);
//		
//		UUID userId = null;
//		try {
//			userId = Get.identifierService()
//					.getUuidPrimordialFromConceptSequence(RequestInfo.get().getEditToken().getAuthorSequence()).get();
//			ArrayList<RestWorkflowProcessHistoriesMapEntry> entries = new ArrayList<>();
//			Map<ProcessDetail, SortedSet<ProcessHistory>> ochreMap = RequestInfo.get().getWorkflow()
//					.getWorkflowAccessor().getAdvanceableProcessInformation(
//							RequestInfoUtils.parseUuidParameter(RequestParameters.definitionId, definitionId), userId);
//
//			for (Map.Entry<ProcessDetail, SortedSet<ProcessHistory>> ochreMapEntry : ochreMap.entrySet()) {
//				List<RestWorkflowProcessHistory> restList = new ArrayList<>();
//				ochreMapEntry.getValue().stream().forEachOrdered(a -> restList.add(new RestWorkflowProcessHistory(a)));
//				entries.add(new RestWorkflowProcessHistoriesMapEntry(new RestWorkflowProcess(ochreMapEntry.getKey()),
//						restList.toArray(new RestWorkflowProcessHistory[restList.size()])));
//			}
//			return entries.toArray(new RestWorkflowProcessHistoriesMapEntry[entries.size()]);
//		} catch (Exception e) {
//			String msg = "Failed retrieving the process and their history that exist within a definition id "
//					+ definitionId + " and may be acted upon by user id " + userId;
//			log.error(msg, e);
//			throw new RestException(msg + ". " + e.getLocalizedMessage());
//		}
//	}
//}











































