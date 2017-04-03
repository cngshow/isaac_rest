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
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LongSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.LatestVersionNotFoundException;
import gov.vha.isaac.rest.session.LatestVersionUtils;
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
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
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
				RequestParameters.UPDATE_COORDINATE_PARAM_NAMES);
		
		if (StringUtils.isBlank(active))
		{
			throw new RestException(RequestParameters.active, "The parameter must be set to true or false");
		}
		
		Boolean setActive = Boolean.parseBoolean(active.trim());

		return resetState(setActive ? State.ACTIVE : State.INACTIVE, id);
	}

	private static class VersionUpdatePair<T extends StampedVersion> {
		T mutable;
		T latest;
		
		public void set(T mutable, T latest) {
			this.mutable = mutable;
			this.latest = latest;
		}
	}
	
	/**
	 * Reset the state of the chronology IFF an existing version corresponding to passed edit and/or stamp coordinates
	 * either does not exist or differs in state.
	 * 
	 * The clazz parameter should correspond to the target StampVersion implementation.
	 * 
	 * @param state - state to which to set new version of chronology
	 * @param chronology - chronology for which to create a new version of the specified state
	 * @param clazz - Java Class of StampVersion implementation for which to create a new version of the specified state
	 * @return
	 * @throws RestException
	 */
	private static <T extends ConceptVersion<T>> VersionUpdatePair<T> resetState(State state, ConceptChronology<T> chronology) throws RestException {	
		return resetState(state, chronology, (Class<T>)null);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends StampedVersion> VersionUpdatePair<T> resetState(State state, ObjectChronology<T> chronology, Class<T> clazz) throws RestException {		
		Optional<T> latestVersion = LatestVersionUtils.getLatestVersionForUpdate(chronology, clazz);
		
		StampedVersion rawMutableVersion = null;
		String detail = chronology.getOchreObjectType() + " " + chronology.getClass().getSimpleName() + " (UUID=" + chronology.getPrimordialUuid() + ")";
		if (chronology instanceof SememeChronology) {
			rawMutableVersion = ((SememeChronology)chronology).createMutableVersion(clazz, state, RequestInfo.get().getEditCoordinate());
			detail = chronology.getOchreObjectType() + " " + chronology.getClass().getSimpleName() + " (UUID=" + chronology.getPrimordialUuid() + ", SEMEME SEQ=" + ((SememeChronology<?>)chronology).getSememeSequence() + ", REF COMP NID=" + ((SememeChronology<?>)chronology).getReferencedComponentNid() + ")";
		} else if (chronology instanceof ConceptChronology) {
			rawMutableVersion = ((ConceptChronology)chronology).createMutableVersion(state, RequestInfo.get().getEditCoordinate());
		} else {
			throw new RuntimeException("Unsupported ObjectChronology type " + detail);
		}
		
		if (! latestVersion.isPresent()) {
			throw new LatestVersionNotFoundException("Failed getting latest version of " + detail + ". May require different stamp or edit coordinate parameters.");
		}

		if (latestVersion.get().getState() == state) {
			log.info("Not resetting state of " + detail + " from " + latestVersion.get().getState() + " to " + state);
			return null;
		}
		VersionUpdatePair<T> versionsHolder = new VersionUpdatePair<>();
		versionsHolder.set((T)rawMutableVersion, latestVersion.get());

		return versionsHolder;
	}
	@SuppressWarnings("rawtypes")
	public static ObjectChronology resetStateWithNoCommit(State state, String id) throws RestException {
		final int nid = RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.id, id);
		
		final ObjectChronologyType type = Get.identifierService().getChronologyTypeForNid(nid);

		ObjectChronology objectToCommit = null;

		State priorState = null;
		
		try 
		{
			switch (type)
			{
				case CONCEPT:
				{
					ConceptChronology cc = Get.conceptService().getConcept(nid);

					@SuppressWarnings("unchecked")
					VersionUpdatePair<ConceptVersion> updatePair = resetState(state, cc);
					if (updatePair != null) {
						priorState = updatePair.latest.getState();
						objectToCommit = cc;
					}
					break;
				}
					
				case SEMEME:
				{
					SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeService().getSememe(nid);
	
					switch (sememe.getSememeType()) 
					{
						case DESCRIPTION: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<DescriptionSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<DescriptionSememeImpl>)sememe, DescriptionSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setCaseSignificanceConceptSequence(sememeUpdatePair.latest.getCaseSignificanceConceptSequence());
								sememeUpdatePair.mutable.setDescriptionTypeConceptSequence(sememeUpdatePair.latest.getDescriptionTypeConceptSequence());
								sememeUpdatePair.mutable.setLanguageConceptSequence(sememeUpdatePair.latest.getLanguageConceptSequence());
								sememeUpdatePair.mutable.setText(sememeUpdatePair.latest.getText());
								objectToCommit = sememe;
							}
							break;
						}
						case STRING: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<StringSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<StringSememeImpl>)sememe, StringSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setString(sememeUpdatePair.latest.getString());
								objectToCommit = sememe;
							} 
		
							break;
						}
						case DYNAMIC: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<DynamicSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<DynamicSememeImpl>)sememe, DynamicSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setData(sememeUpdatePair.latest.getData());
								objectToCommit = sememe;
							}
							break;
						}
						case COMPONENT_NID: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<ComponentNidSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<ComponentNidSememeImpl>)sememe, ComponentNidSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setComponentNid(sememeUpdatePair.latest.getComponentNid());
								objectToCommit = sememe;
							} 
							break;
						}
						case LOGIC_GRAPH: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<LogicGraphSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<LogicGraphSememeImpl>)sememe, LogicGraphSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setGraphData(sememeUpdatePair.latest.getGraphData());
								objectToCommit = sememe;
							}
							break;
						}
						case LONG: {
							@SuppressWarnings("unchecked")
							VersionUpdatePair<LongSememeImpl> sememeUpdatePair = resetState(state, (SememeChronology<LongSememeImpl>)sememe, LongSememeImpl.class);
		
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								sememeUpdatePair.mutable.setLongValue(sememeUpdatePair.latest.getLongValue());
								objectToCommit = sememe;
							}
							break;
						}
						case MEMBER:
							// TODO figure out why compiling of generics failing on command line but not in eclipse, requiring this hack
							@SuppressWarnings("unchecked")
							VersionUpdatePair<SememeVersion> sememeUpdatePair = resetState(state, (SememeChronology)sememe, SememeVersion.class);
							
							if (sememeUpdatePair != null) {
								priorState = sememeUpdatePair.latest.getState();
								objectToCommit = sememe;
							}
							break;
						case RELATIONSHIP_ADAPTOR:
						case UNKNOWN:
						default:
							String detail = sememe.getSememeType() + " (UUID=" + sememe.getPrimordialUuid() + ", SEMEME SEQ=" + sememe.getSememeSequence() + ", REF COMP NID=" + sememe.getReferencedComponentNid() + ")";

							throw new RestException(RequestParameters.id, id, "Unsupported sememe of type " + detail);
					}
					break;
				}
	
				case UNKNOWN_NID:
				default :
					throw new RestException(RequestParameters.id, id, "Could not locate component of unexpected type " + type + " to change its state");
			}
			
			if (objectToCommit != null) {
				log.debug("Built updated version of " + type + " " + id + " with state changed (from " + priorState + " to " + state + ")");
			} else {
				log.debug("No need to commit update of " + type + " " + id + " with unchanged state (" + state + ")");
			}

			return objectToCommit;
		} 
		catch (RestException e)
		{
			throw e;
		} 
		catch (Exception e) {	
			log.error("Unexpected", e);
			throw new RestException("Failed updating " + type + " " + id + " state to " + state + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	@SuppressWarnings("rawtypes")
	public static RestWriteResponse resetState(State state, String id) throws RestException {
		ObjectChronology objectToCommit = resetStateWithNoCommit(state, id);

		int nid = RequestInfoUtils.getNidFromUuidOrNidParameter(RequestParameters.id, id);

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
					throw new RestException("Failed updating " + objectToCommit.getOchreObjectType() + " " + id + " state to " + state + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				}
			} 

			return new RestWriteResponse(RequestInfo.get().getEditToken(), Get.identifierService().getUuidPrimordialForNid(nid).get());
		} else {
			log.debug("Not committing update of " + id + " with unchanged state (" + state + ")");
			return new RestWriteResponse(RequestInfo.get().getEditToken(), Get.identifierService().getUuidPrimordialForNid(nid).get(), RestWriteResponseEnumeratedDetails.UNCHANGED);
		}
	}
}