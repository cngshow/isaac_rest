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
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.SememeUtil;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
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

	/**
	 * Create a new description sememe associated with a specified concept
	 * 
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.createPathComponent)
	public RestWriteResponse createDescriptionSememe(
			RestSememeDescriptionCreateData creationData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken);

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

			if (creationData.active != null && !creationData.active)
			{
				descriptionSememeBuilder.setState(State.INACTIVE);
			}
			
			SememeChronology<? extends DescriptionSememe<?>> newDescription = descriptionSememeBuilder.build(RequestInfo.get().getEditCoordinate(),
					ChangeCheckerMode.ACTIVE).get();

			if (creationData.getPreferredInDialectAssemblagesIds() != null) {
				creationData.getPreferredInDialectAssemblagesIds().forEach((id) -> {
					sememeBuilderService.getComponentSememeBuilder(
							TermAux.PREFERRED.getNid(), newDescription.getNid(),
							id).
							build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
				});
			}

			if (creationData.getAcceptableInDialectAssemblagesIds() != null) {
				creationData.getAcceptableInDialectAssemblagesIds().forEach((id) -> {
					sememeBuilderService.getComponentSememeBuilder(
							TermAux.ACCEPTABLE.getNid(), 
							newDescription.getNid(),
							id).
							build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
				});
			}

			// TODO test addition of extendedDescriptionTypeConceptSequence UUID annotation to new description
			if (creationData.getExtendedDescriptionTypeConceptSequence() != null) {
				SememeUtil.addAnnotation(
						RequestInfo.get().getEditCoordinate(),
						newDescription.getNid(),
						new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptSequence(creationData.getExtendedDescriptionTypeConceptSequence()).get()),
						DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid());
			}

			Optional<CommitRecord> commitRecord = Get.commitService().commit("creating new description sememe: NID=" 
					+ newDescription.getNid() + ", text=" + creationData.getText()).get();

			if (RequestInfo.get().getWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getWorkflowProcessId(), commitRecord);
			}

			return new RestWriteResponse(RequestInfo.get().getEditToken(), newDescription.getPrimordialUuid());
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
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id + "}")
	public RestWriteResponse updateDescriptionSememe(
			RestSememeDescriptionUpdateData updateData,
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken);

		// TODO test updateDescription(), including validation of updateData.getDescriptionTypeConceptSequence()
		int sememeSequence = RequestInfoUtils.getSememeSequenceFromParameter(RequestParameters.id, id);

		try {
			SememeChronology<? extends SememeVersion<?>> sememeChronology = Get.sememeService().getOptionalSememe(sememeSequence).get();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			DescriptionSememeImpl mutableVersion =
					(DescriptionSememeImpl)((SememeChronology)sememeChronology).createMutableVersion(
							DescriptionSememeImpl.class, (updateData == null || updateData.isActive() ? State.ACTIVE : State.INACTIVE),
							RequestInfo.get().getEditCoordinate());

			mutableVersion.setCaseSignificanceConceptSequence(updateData.getCaseSignificanceConceptSequence());
			mutableVersion.setLanguageConceptSequence(updateData.getLanguageConceptSequence());
			mutableVersion.setText(updateData.getText());
			mutableVersion.setDescriptionTypeConceptSequence(updateData.getDescriptionTypeConceptSequence());

			Get.commitService().addUncommitted(sememeChronology);
			Task<Optional<CommitRecord>> commitRecord = Get.commitService().commit("updating description sememe: SEQ=" + sememeSequence 
					+ ", NID=" + sememeChronology.getNid() + " with " + updateData);

			if (RequestInfo.get().getWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getWorkflowProcessId(), commitRecord.get());
			}
			
			return new RestWriteResponse(RequestInfo.get().getEditToken(), mutableVersion.getPrimordialUuid());
			
		} catch (Exception e) {
			throw new RestException("Failed updating description " + id + " with " + updateData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}