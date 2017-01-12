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
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.workflow.model.contents.DefinitionDetail;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail.ProcessStatus;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessHistory;
import gov.vha.isaac.ochre.workflow.provider.BPMNInfo;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowComponentSummary;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinition;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowStatusCount;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * {@link WorkflowAPIs}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.workflowAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class WorkflowAPIs {

	private static Logger log = LogManager.getLogger(WorkflowAPIs.class);

	/**
	 * Return all workflow definitions available.  At this time there is only one workflow definition.
	 *
	 * @return RestWorkflowDefinition - Collection of all workflow definitions
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.definition)
	public RestWorkflowDefinition[] getDefinition() throws RestException {
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
	 * Collection of all workflow process statuses and their counts.
	 *
	 * @return RestWorkflowDefinition - Collection of all workflow statuses and their counts.
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.workflowCountSummary  + "{" + RequestParameters.definitionId + "}")
	public RestWorkflowStatusCount[] getWorkflowStatisticsSummary(
			@PathParam(RequestParameters.definitionId) String definitionId) throws RestException {

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.definitionId);

		UUID workflowDefintionId = null;
		UUID userId = null;

		try {
			workflowDefintionId = RequestInfoUtils.parseUuidParameter(RequestParameters.definitionId, definitionId);
			userId = Get.identifierService().getUuidPrimordialFromConceptId(RequestInfo.get().getEditToken().getAuthorSequence()).get();

			ArrayList<RestWorkflowStatusCount> restList = new ArrayList<>();
			ArrayList<ProcessStatus> status = new ArrayList<>();
			status.add(ProcessStatus.LAUNCHED);
			status.add(ProcessStatus.DEFINED);

			Map<ProcessStatus, Long> counters =	RequestInfo.get().getWorkflow().getWorkflowAccessor().getProcessInformation(workflowDefintionId, userId, status)
					.stream().collect(Collectors.groupingBy(ProcessDetail::getStatus, Collectors.counting()));

			for (Map.Entry<ProcessStatus, Long> entry : counters.entrySet()) {
				restList.add(
						new RestWorkflowStatusCount(
								new RestWorkflowProcessStatusType(entry.getKey()), entry.getValue()));
			}

			return restList.toArray(new RestWorkflowStatusCount[restList.size()]);
		} catch (Exception e) {
			String msg = "Failed retrieving workflow status counts from server.";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Return workflow process instance information for the specified process id.
	 *
	 * @param processId UUID identifying a given workflow process instance
	 * @return RestWorkflowProcess - Workflow process instance information
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.process + "{" + RequestParameters.processId + "}")
	public RestWorkflowProcess getProcess(
			@PathParam(RequestParameters.processId) String processId
			) throws RestException
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
	 * @return RestWorkflowProcessHistory - Sorted collection of the process instance's advancements
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.history + "{" + RequestParameters.processId + "}")
	public RestWorkflowProcessHistory[] getProcessHistory(
			@PathParam(RequestParameters.processId) String processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId);

		try {
			SortedSet<ProcessHistory> ochreSet = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getProcessHistory(RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId));

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
	 * Return the available actions that the user may perform on the workflow process instance.
	 *
	 * @param processId UUID identifying a given workflow process instance
	 * @param editToken String serialization of EditToken identifying currently logged in user
	 * @return RestWorkflowAvailableAction Collection - Collection of distinct actions a user can perform
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.actions + "{" + RequestParameters.processId + "}")
	public RestWorkflowAvailableAction[] getProcessActions(
			@PathParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId,
				RequestParameters.editToken);

		UUID userId = null;
		try {
			userId = Get.identifierService()
					.getUuidPrimordialFromConceptId(RequestInfo.get().getEditToken().getAuthorSequence()).get();
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
	 * Return active, canceled or concluded workflow process instances as specified in the parameters.
	 *
	 * @param editToken String serialization of EditToken identifying currently logged in user.
	 * @param includeActive String include processes active processes.
	 * @param includeCanceled String include include canceled processes.
	 * @param includeCompleted String include completed processes.
	 * @return RestWorkflowProcess Collection - Workflow process instances
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.list + "{" + RequestParameters.definitionId + "}")
	public RestWorkflowProcess[] getProcesses(
			@PathParam(RequestParameters.definitionId) String definitionId,
			@QueryParam(RequestParameters.editToken) String editToken,
			@QueryParam(RequestParameters.includeActive) @DefaultValue("true") String includeActive,
			@QueryParam(RequestParameters.includeCanceled) @DefaultValue("false") String includeCanceled,
			@QueryParam(RequestParameters.includeCompleted) @DefaultValue("false") String includeCompleted
			) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.definitionId,
				RequestParameters.editToken,
				RequestParameters.includeActive,
				RequestParameters.includeCanceled,
				RequestParameters.includeCompleted);

		UUID workflowDefintionId = null;
		UUID userId = null;

		try {

			workflowDefintionId = RequestInfoUtils.parseUuidParameter(RequestParameters.definitionId, definitionId);
			userId = Get.identifierService().getUuidPrimordialFromConceptId(RequestInfo.get().getEditToken().getAuthorSequence()).get();

			ArrayList<ProcessStatus> status = new ArrayList<>();
			if (Boolean.parseBoolean(includeActive.trim()))
			{
				status.add(ProcessStatus.DEFINED);
				status.add(ProcessStatus.LAUNCHED);
			}
			if (Boolean.parseBoolean(includeCanceled.trim()))
			{
				status.add(ProcessStatus.CANCELED);
			}
			if (Boolean.parseBoolean(includeCompleted.trim()))
			{
				status.add(ProcessStatus.CONCLUDED);
			}

			ArrayList<RestWorkflowProcess> entries = new ArrayList<>();

			RequestInfo.get().getWorkflow().getWorkflowAccessor().getProcessInformation(workflowDefintionId, userId, status).forEach(p -> {
				entries.add(new RestWorkflowProcess(p));
			});

			for(RestWorkflowProcess process : entries) {

				//if user has permissions and process does not have an ownerId it is available to this user.
				if (process.getOwnerId().compareTo(userId) == 0)
				{
					process.setUserAvailability("Active");
				}
				else if (process.getOwnerId().compareTo(BPMNInfo.UNOWNED_PROCESS) == 0)
				{
					process.setUserAvailability("Suspended");
				}
				else if (process.getOwnerId().compareTo(userId) != 0)
				{
					process.setUserAvailability("Locked");
				}

				ProcessHistory history = RequestInfo.get().getWorkflow().getWorkflowAccessor().getLastProcessHistory(process.getId());
				if (history != null) {
					process.setStage(history.getOutcomeState());
				}

			}

			return entries.toArray(new RestWorkflowProcess[entries.size()]);

		} catch (Exception e) {
			String msg = "Failed retrieving the processes";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

	/**
	 * Retrieve a summary of workflow statuses with process counts.
	 *
	 * @param processId UUID identifying a given workflow process instance
	 * @return RestWorkflowComponentSummary Collection -
	 *
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.componentSummary + "{" + RequestParameters.processId + "}")
	public RestWorkflowComponentSummary[] getComponentSummary(
			@PathParam(RequestParameters.processId) String processId) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.processId);

		try {
			ArrayList<String> ochreSet = RequestInfo.get().getWorkflow().getWorkflowAccessor()
					.getComponentModifications(RequestInfoUtils.parseUuidParameter(RequestParameters.processId, processId));

			List<RestWorkflowComponentSummary> restList = new ArrayList<>();

			ochreSet.stream().forEachOrdered(a -> restList.add(new RestWorkflowComponentSummary(a)));

			return restList.toArray(new RestWorkflowComponentSummary[restList.size()]);

		} catch (Exception e) {
			String msg = "Failed retrieving the ordered set of process component summary by process id";
			log.error(msg, e);
			throw new RestException(msg + ". " + e.getLocalizedMessage());
		}
	}

}











































