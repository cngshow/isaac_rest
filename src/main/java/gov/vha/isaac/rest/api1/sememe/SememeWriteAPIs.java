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
package gov.vha.isaac.rest.api1.sememe;

import java.util.Optional;

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
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LogicGraphSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LongSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.wrappers.RestBoolean;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreateData;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import javafx.concurrent.Task;

/**
 * {@link SememeWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent)
public class SememeWriteAPIs
{
	private static Logger log = LogManager.getLogger(SememeWriteAPIs.class);

	static WorkflowUpdater updater = null;
	
	static {
		try {
			updater = new WorkflowUpdater();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};

	/**
	 * Create a new description sememe associated with a specified concept
	 * 
	 * @param editToken - the edit coordinates identifying who is making the edit
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.createPathComponent)
	public RestInteger createDescriptionSememe(
			RestSememeDescriptionCreateData creationData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		// TODO test createDescription(), including validation of creationData.getDescriptionTypeConceptSequence()
		try {
			SememeBuilderService<? extends SememeChronology<? extends SememeVersion<?>>> sememeBuilderService
					= Get.sememeBuilderService();
			SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> descriptionSememeBuilder
					= sememeBuilderService.getDescriptionSememeBuilder(
							creationData.getCaseSignificanceConceptSequence(),
							creationData.getLanguageConceptSequence(),
							creationData.getDescriptionTypeConceptSequence(),
							creationData.getText(),
							creationData.getReferencedComponentNid());

			@SuppressWarnings("unchecked")
			SememeChronology<DescriptionSememe<?>> newDescription = (SememeChronology<DescriptionSememe<?>>)
			descriptionSememeBuilder.build(
					RequestInfo.get().getEditCoordinate(),
					ChangeCheckerMode.ACTIVE); // TODO should be ACTIVE?
			Get.commitService().addUncommitted(newDescription);

			creationData.getPreferredInDialectAssemblagesIds().forEach((id) -> {
				Get.commitService().addUncommitted(sememeBuilderService.getComponentSememeBuilder(
						TermAux.PREFERRED.getNid(), newDescription.getNid(),
						id).
						build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE));
			});

			creationData.getAcceptableInDialectAssemblagesIds().forEach((id) -> {
				Get.commitService().addUncommitted(sememeBuilderService.getComponentSememeBuilder(
						TermAux.ACCEPTABLE.getNid(), 
						newDescription.getNid(),
						id).
						build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE));
			});

			// TODO add extended-description-type component sememe when dan creates metadata constant
//			if (creationData.getExtendedDescriptionTypeConceptSequence() != null && creationData.getExtendedDescriptionTypeConceptSequence() > 0) {
//				Get.commitService().addUncommitted(sememeBuilderService.getComponentSememeBuilder(
//						creationData.getExtendedDescriptionTypeConceptSequence(), 
//						newDescription.getNid(),
//						DESCRIPTION_SOURCE_TYPE_REFERENCE_SETS).
//						build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE));
//			}

			Optional<CommitRecord> commitRecord = Get.commitService().commit("creating new description sememe: NID=" + newDescription.getNid() + ", text=" + creationData.getText()).get();

			updater.addCommitRecordToWorkflow(updater.getRestTestProcessId(), commitRecord);

			return new RestInteger(newDescription.getSememeSequence());
		} catch (Exception e) {
			throw new RestException("Failed creating description " + creationData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Update/edit an existing description sememe
	 * 
	 * @param id The id for which to determine RestSememeType
	 * If an int then assumed to be a sememe NID or sequence
	 * If a String then parsed and handled as a sememe UUID
	 * @param editToken - the edit coordinates identifying who is making the edit
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id + "}")
	public void updateDescriptionSememe(
			RestSememeDescriptionUpdateData updateData,
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		// TODO test updateDescription(), including validation of updateData.getDescriptionTypeConceptSequence()
		int sememeSequence = RequestInfoUtils.getSememeSequenceFromParameter(RequestParameters.id, id);

		try {
			SememeChronology<? extends SememeVersion<?>> sememeChronology = Get.sememeService().getOptionalSememe(sememeSequence).get();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			DescriptionSememeImpl mutableVersion =
					(DescriptionSememeImpl)((SememeChronology)sememeChronology).createMutableVersion(
							DescriptionSememeImpl.class, updateData.isActive() ? State.ACTIVE : State.INACTIVE,
							RequestInfo.get().getEditCoordinate());

			mutableVersion.setCaseSignificanceConceptSequence(updateData.getCaseSignificanceConceptSequence());
			mutableVersion.setLanguageConceptSequence(updateData.getLanguageConceptSequence());
			mutableVersion.setText(updateData.getText());
			mutableVersion.setDescriptionTypeConceptSequence(updateData.getDescriptionTypeConceptSequence());

			Util.setStampedVersionFields(RequestInfo.get().getEditCoordinate(), mutableVersion);

			Get.commitService().addUncommitted(sememeChronology);
			Task<Optional<CommitRecord>> commitRecord = Get.commitService().commit("updating description sememe: SEQ=" + sememeSequence + ", NID=" + sememeChronology.getNid() + " with " + updateData);

			updater.addCommitRecordToWorkflow(updater.getRestTestProcessId(), commitRecord.get());
		} catch (Exception e) {
			throw new RestException("Failed updating description " + id + " with " + updateData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	/**
	 * Reset sememe status to ACTIVE if passed value is true, or INACTIVE if passed value is false
	 * If specified state is same as the state of the latest version retrievable (based on stamp coordinates)
	 * then the state remains unchanged
	 * 
	 * @param id The id for which to determine RestSememeType
	 * If an int then assumed to be a sememe NID or sequence
	 * If a String then parsed and handled as a sememe UUID
	 * @param editToken - the edit coordinates identifying who is making the edit
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.updateStateComponent + "{" + RequestParameters.id + "}")
	public void updateSememeState( // TODO test updateSememeState()
			RestBoolean isActive,
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{	
		resetSememeState(RequestInfo.get().getEditCoordinate(), RequestInfo.get().getStampCoordinate(), isActive.isValue() ? State.ACTIVE : State.INACTIVE, id);
	}

	@SuppressWarnings("rawtypes")
	private static class SememeVersionUpdatePair<T extends SememeVersionImpl> {
		T mutable;
		T latest;
		
		public void set(T mutable, T latest) {
			this.mutable = mutable;
			this.latest = latest;
		}
	}

	@SuppressWarnings("rawtypes")
	private static <T extends SememeVersionImpl> SememeVersionUpdatePair<T> resetSememeState(EditCoordinate ec, StampCoordinate sc, State state, SememeChronology<? extends SememeVersion<?>> sememe, Class<T> clazz, SememeVersionUpdatePair<T> versionsHolder) throws RestException {
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<SememeVersionImpl>> rawLatestVersion = ((SememeChronology)sememe).getLatestVersion(clazz, sc);

		@SuppressWarnings("unchecked")
		SememeVersion rawMutableVersion = ((SememeChronology)sememe).createMutableVersion(clazz, state, ec);
		@SuppressWarnings("unchecked")
		T mutableVersion = (T)rawMutableVersion;

		@SuppressWarnings("unchecked")
		T latestVersion = (T)rawLatestVersion.get().value();

		if (! rawLatestVersion.isPresent()) {
			throw new RestException("Failed getting latest version of " + sememe.getSememeType() + " " + sememe.getSememeSequence());
		} else if (rawLatestVersion.get().contradictions().isPresent()) {
			log.warn("Resetting state of " + sememe.getSememeType() + " " + sememe.getSememeSequence() + " with " + rawLatestVersion.get().contradictions().get().size() + " version contradictions from " + latestVersion.getState() + " to " + state);
		}
		
		if (latestVersion.getState() == state) {
			log.warn("Not resetting state of " + sememe.getSememeType() + " " + sememe.getSememeSequence() + " from " + latestVersion.getState() + " to " + state);
			return null;
		}

		Util.setStampedVersionFields(ec, mutableVersion);
		
		versionsHolder.set(mutableVersion, latestVersion);

		return versionsHolder;
	}

	private static void resetSememeState(EditCoordinate ec, StampCoordinate sc, State state, String id) throws RestException {
		int sememeSequence = RequestInfoUtils.getSememeSequenceFromParameter(RequestParameters.id, id);

		try {
			SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeService().getSememe(sememeSequence);

			SememeVersionUpdatePair<?> rawSememeUpdatePair = null;
			switch (sememe.getSememeType()) {
			case DESCRIPTION: {
				SememeVersionUpdatePair<DescriptionSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, DescriptionSememeImpl.class, new SememeVersionUpdatePair<DescriptionSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setCaseSignificanceConceptSequence(sememeUpdatePair.latest.getCaseSignificanceConceptSequence());
					sememeUpdatePair.mutable.setDescriptionTypeConceptSequence(sememeUpdatePair.latest.getDescriptionTypeConceptSequence());
					sememeUpdatePair.mutable.setLanguageConceptSequence(sememeUpdatePair.latest.getLanguageConceptSequence());
					sememeUpdatePair.mutable.setText(sememeUpdatePair.latest.getText());
				} else {
					return;
				}
				
				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case STRING: {
				SememeVersionUpdatePair<StringSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, StringSememeImpl.class, new SememeVersionUpdatePair<StringSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setString(sememeUpdatePair.latest.getString());
				} else {
					return;
				}

				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case DYNAMIC: {
				SememeVersionUpdatePair<DynamicSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, DynamicSememeImpl.class, new SememeVersionUpdatePair<DynamicSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setData(sememeUpdatePair.latest.getData());
				} else {
					return;
				}

				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case COMPONENT_NID: {
				SememeVersionUpdatePair<ComponentNidSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, ComponentNidSememeImpl.class, new SememeVersionUpdatePair<ComponentNidSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setComponentNid(sememeUpdatePair.latest.getComponentNid());
				} else {
					return;
				}

				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case LOGIC_GRAPH: {
				SememeVersionUpdatePair<LogicGraphSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, LogicGraphSememeImpl.class, new SememeVersionUpdatePair<LogicGraphSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setGraphData(sememeUpdatePair.latest.getGraphData());
				} else {
					return;
				}

				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case LONG: {
				SememeVersionUpdatePair<LongSememeImpl> sememeUpdatePair = resetSememeState(ec, sc, state, sememe, LongSememeImpl.class, new SememeVersionUpdatePair<LongSememeImpl>());

				if (sememeUpdatePair != null) {
					sememeUpdatePair.mutable.setLongValue(sememeUpdatePair.latest.getLongValue());
				} else {
					return;
				}

				rawSememeUpdatePair = sememeUpdatePair;
				break;
			}
			case MEMBER:
			case RELATIONSHIP_ADAPTOR:
			case UNKNOWN:
			default:
				throw new RestException("Unsupported sememe " + id + " of type " + sememe.getSememeType());
			}

			Get.commitService().addUncommitted(sememe);
			Task<Optional<CommitRecord>> commitRecord = Get.commitService().commit("updating sememe " + id + " from " + rawSememeUpdatePair.latest.getState() + " to " + rawSememeUpdatePair.mutable.getState() + ": SEQ=" + sememeSequence + ", NID=" + sememe.getNid());

			updater.addCommitRecordToWorkflow(updater.getRestTestProcessId(), commitRecord.get());
		} catch (RestException e) {	
			throw e;
		} catch (Exception e) {	
			throw new RestException("Failed updating sememe " + id + " state to " + state + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}