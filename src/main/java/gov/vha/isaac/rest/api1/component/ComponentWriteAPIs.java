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
package gov.vha.isaac.rest.api1.component;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import javafx.concurrent.Task;

/**
 * 
 * {@link ComponentWriteAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.apiVersionComponent + RestPaths.componentComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.EDITOR})
public class ComponentWriteAPIs
{
	private static Logger log = LogManager.getLogger(ComponentWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * Reset component status to ACTIVE if passed value is true, or INACTIVE if passed value is false
	 * If specified state is same as the state of the latest version retrievable (based on stamp coordinates)
	 * then the state remains unchanged
	 * 
	 * This method relies on the passed in stamp coordinates to read in the component - the pattern is that it 
	 * reads the version of the object specified by the current stamp coordinate, changes the status, then commits that.
	 * 
	 * @param id The id (UUID or NID) of the component to change the state of.  Sequences are not allowed.
	 * @param active - true for activate, false for inactivate.
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.updateStateComponent + "{" + RequestParameters.id + "}")
	public RestWriteResponse updateState(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.active) String active,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.active,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES); // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
		
		if (StringUtils.isBlank(active))
		{
			throw new RestException(RequestParameters.active, "The parameter must be set to true or false");
		}
		
		Boolean setActive = Boolean.parseBoolean(active.trim());

		return resetState(setActive ? State.ACTIVE : State.INACTIVE, id);
	}
	
	@SuppressWarnings("rawtypes")
	public static ObjectChronology resetStateWithNoCommit(State state, String id) throws RestException {
		final int nid = RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.id, id);

		//Figure out which read coordinate we should pass.
		//First, tries to read the current version using the passed in stamp (but any state), and the module from the edit coordinate.
		//If no version is present using the edit coordinate module, then it tries again using the module(s) from the current read coordinate, with any state.
			
		StampCoordinate[] readCoordinates = new StampCoordinate[2];
		readCoordinates[0] = Frills.makeStampCoordinateAnalogVaryingByModulesOnly(
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getEditCoordinate().getModuleSequence(),
				null).makeAnalog(State.values()).makeAnalog(Long.MAX_VALUE);
		readCoordinates[1] = RequestInfo.get().getStampCoordinate().makeAnalog(State.values()).makeAnalog(Long.MAX_VALUE);

		
		try {
			return Frills.resetStateWithNoCommit(state, nid, RequestInfo.get().getEditCoordinate(), readCoordinates);
		} catch (Exception e) {
			throw new RestException(e.getMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	public static RestWriteResponse resetState(State state, String id) throws RestException {
		ObjectChronology objectToCommit = resetStateWithNoCommit(state, id);
		
		if (objectToCommit != null)
		{
			if (objectToCommit.getOchreObjectType() == OchreExternalizableObjectType.CONCEPT) {
				Get.commitService().addUncommitted((ConceptChronology)objectToCommit);
			} else if (objectToCommit.getOchreObjectType() == OchreExternalizableObjectType.SEMEME) {
				Get.commitService().addUncommitted((SememeChronology)objectToCommit);
			} else {
				throw new RuntimeException("Cannot addUncommitted() for commit object with id=" + id + " of unsupported OchreObjectType " + objectToCommit.getOchreObjectType());
			}

			Task<Optional<CommitRecord>> commitRecord = Get.commitService().commit("updating " + objectToCommit.getOchreObjectType() + " with id " + id + " to " + state);

			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				try {
					LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord.get());
				} catch (RestException re) {
					throw re;
				} catch (Exception e) {
					log.error("Unexpected", e);
					throw new RestException("Failed updating " + objectToCommit.getOchreObjectType() + " " + id + " state to " + state + ". Caught " + e.getClass().getName() 
							+ " " + e.getLocalizedMessage());
				}
			} 

			return new RestWriteResponse(RequestInfo.get().getEditToken(), objectToCommit.getPrimordialUuid());
		} else {
			log.debug("Not committing update of " + id + " with unchanged state (" + state + ")");
			return new RestWriteResponse(RequestInfo.get().getEditToken(), 
					Get.identifierService().getUuidPrimordialForNid(RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.id, id)).get(), 
					RestWriteResponseEnumeratedDetails.UNCHANGED);
		}
	}
}