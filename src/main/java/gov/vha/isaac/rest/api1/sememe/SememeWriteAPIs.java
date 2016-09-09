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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreateData;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link SememeWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent)
public class SememeWriteAPIs
{
	//private static Logger log = LogManager.getLogger(ConceptWriteAPIs.class);

	/**
	 * Create a new description on a specified concept
	 * 
	 * @param creationData
	 * @param editToken
	 * @return
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.createPathComponent)
	public RestInteger createDescription(
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
					ChangeCheckerMode.ACTIVE);
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

			Get.commitService().commit("creating new description sememe: NID=" + newDescription.getNid() + ", text=" + creationData.getText()).get();

			return new RestInteger(newDescription.getSememeSequence());
		} catch (Exception e) {
			throw new RestException("Failed creating description " + creationData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
	
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id + "}")
	public void updateDescription(
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
			
			Get.commitService().addUncommitted(sememeChronology);
			Get.commitService().commit("updating description sememe: SEQ=" + sememeSequence + ", NID=" + sememeChronology.getNid() + " with " + updateData);
		} catch (Exception e) {
			throw new RestException("Failed updating description " + id + " with " + updateData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}
