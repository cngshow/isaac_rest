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
package gov.vha.isaac.rest.api1.concept;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.SememeUtil;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.component.ComponentWriteAPIs;
import gov.vha.isaac.rest.api1.data.concept.RestConceptCreateData;
import gov.vha.isaac.rest.api1.data.concept.RestConceptUpdateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.EditTokens;


/**
 * {@link ConceptWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent)
public class ConceptWriteAPIs
{
	private static Logger log = LogManager.getLogger(ConceptWriteAPIs.class);
	
	/**
	 * @param creationData - object containing data used to create new concept
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sequence identifying the created concept
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public RestWriteResponse createConcept(
			RestConceptCreateData creationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken);

		if (StringUtils.isBlank(creationData.getFsn())) {
			throw new RestException("RestConceptCreateData.fsn", creationData.getFsn(), "FSN required");
		}
		
		if (creationData.getParentConceptIds().size() < 1) {
			throw new RestException("RestConceptCreateData.parentIds", creationData.getParentConceptIds() + "", "At least one parent concept id required");
		}
		
		int index = 0;
		for (int parentId : creationData.getParentConceptIds()) {
			if (! Get.conceptService().hasConcept(parentId)) {
				throw new RestException("RestConceptCreateData.parentIds[" + index + "]", parentId + "", "Integer id does not correspond to an existing concept");
			}
			
			index++;
		}

		if (! Get.conceptService().hasConcept(creationData.getDescriptionLanguageConceptId())) {
			throw new RestException("RestConceptCreateData.descriptionLanguageConceptId", creationData.getDescriptionLanguageConceptId() + "", "Integer id does not correspond to an existing concept");
		}
		
		index = 0;
		for (int id : creationData.getDescriptionPreferredInDialectAssemblagesConceptIds()) {
			if (! Get.conceptService().hasConcept(id)) {
				throw new RestException("RestConceptCreateData.descriptionPreferredInDialectAssemblagesConceptIds[" + index + "]", id + "", "Integer id does not correspond to an existing concept");
			}
			
			index++;
		}

		if (creationData.getDescriptionExtendedTypeConceptId() != null) {
			if (! Get.conceptService().hasConcept(creationData.getDescriptionExtendedTypeConceptId())) {
				throw new RestException("RestConceptCreateData.descriptionExtendedTypeConceptId", creationData.getDescriptionExtendedTypeConceptId() + "", "Integer id does not correspond to an existing concept");
			}
			if (! Get.identifierService().getUuidPrimordialFromConceptSequence(creationData.getDescriptionExtendedTypeConceptId()).isPresent()) {
				throw new RestException("RestConceptCreateData.descriptionExtendedTypeConceptId", creationData.getDescriptionExtendedTypeConceptId() + "", "Integer id does not correspond to an existing concept UUID");
			}
		}

		try {
			return createNewConcept(
					RequestInfo.get().getEditCoordinate(),
					RequestInfo.get().getLogicCoordinate(),
					creationData.getParentConceptIds(),
					creationData.getFsn(),
					creationData.getDescriptionLanguageConceptId(),
					creationData.getDescriptionPreferredInDialectAssemblagesConceptIds(),
					creationData.getDescriptionExtendedTypeConceptId(), 
					creationData.active == null || creationData.active ? true : false);
		} catch (Exception e) {
			throw new RestException("Failed creating concept " + creationData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	private static RestWriteResponse createNewConcept(
			EditCoordinate editCoordinate,
			LogicCoordinate lc,
			Collection<Integer> parentConceptIds,
			String fsn,
			int descriptionLanguageConceptId,
			Collection<Integer> descriptionPreferredInDialectAssemblagesConceptIds,
			Integer descriptionExtendedTypeConceptId,
			boolean active) throws RestException
	{
		try
		{
			ConceptSpecification defaultDescriptionsLanguageConceptSpec = Get.conceptSpecification(descriptionLanguageConceptId);
			ConceptSpecification defaultDescriptionDialectConceptSpec = (descriptionPreferredInDialectAssemblagesConceptIds != null && descriptionPreferredInDialectAssemblagesConceptIds.size() > 0) ? Get.conceptSpecification(descriptionPreferredInDialectAssemblagesConceptIds.iterator().next()) : MetaData.US_ENGLISH_DIALECT;	

			ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
			conceptBuilderService.setDefaultLanguageForDescriptions(defaultDescriptionsLanguageConceptSpec);
			conceptBuilderService.setDefaultDialectAssemblageForDescriptions(defaultDescriptionDialectConceptSpec);
			conceptBuilderService.setDefaultLogicCoordinate(lc);

			LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

			for (int parentConceptNidOrSequence : parentConceptIds) {
				ConceptChronology<?> parentConcept = Get.conceptService().getConcept(parentConceptNidOrSequence);

				NecessarySet(And(ConceptAssertion(parentConcept, defBuilder)));
			}

			LogicalExpression parentDef = defBuilder.build();

			ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(
					fsn,
					null,
					parentDef);
			
			builder.setState(active ? State.ACTIVE : State.INACTIVE);
			
			// Add optional descriptionExtendedTypeConceptId, if exists
			// TODO confirm that requiredDescriptionsExtendedType is being added to new concept required descriptions
			SememeChronology<DynamicSememe<?>> requiredDescriptionsExtendedTypeSememe = null;
			if (descriptionExtendedTypeConceptId != null) {
				int referencedComponentNid = builder.getFullySpecifiedDescriptionBuilder().getNid();
				requiredDescriptionsExtendedTypeSememe = 
						SememeUtil.addAnnotation(
								editCoordinate,
								referencedComponentNid,
								new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptSequence(descriptionExtendedTypeConceptId).get()),
								DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid());
			}
			
			// Add optional descriptionPreferredInDialectAssemblagesConceptIdsList beyond first (already added , if exists
			if (descriptionPreferredInDialectAssemblagesConceptIds != null && descriptionPreferredInDialectAssemblagesConceptIds.size() > 0) {
				for (int i : descriptionPreferredInDialectAssemblagesConceptIds) {
					builder.getFullySpecifiedDescriptionBuilder().setPreferredInDialectAssemblage(Get.conceptSpecification(i));
				}
			}
			
			List<?> createdObjects = new ArrayList<>();
			ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(editCoordinate, ChangeCheckerMode.ACTIVE, createdObjects).getNoThrow();

			Get.commitService().addUncommitted(newCon).get();

			if (requiredDescriptionsExtendedTypeSememe != null) {
				Get.commitService().addUncommitted(requiredDescriptionsExtendedTypeSememe).get();
			}
			
			Optional<CommitRecord> commitRecord = Get.commitService().commit(
					"creating new concept: NID=" + newCon.getNid() + ", FSN=" + fsn).get();
			
			if (RequestInfo.get().getWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getWorkflowProcessId(), commitRecord);
			}
			
			return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), newCon.getPrimordialUuid());
		}
		catch (Exception e)
		{
			throw new RestException("Creation of concept Failed. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	/**
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + "{" + RequestParameters.id + "}")
	public RestWriteResponse updateConcept(
			RestConceptUpdateData conceptUpdateData, 
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		State stateToUse = (conceptUpdateData.active == null || conceptUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		
		return ComponentWriteAPIs.resetState(RequestInfo.get().getEditCoordinate(), StampCoordinates.getDevelopmentLatest(), stateToUse, id);
	}
}
