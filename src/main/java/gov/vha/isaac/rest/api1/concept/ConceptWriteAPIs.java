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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
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
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.component.ComponentWriteAPIs;
import gov.vha.isaac.rest.api1.data.concept.RestConceptCreateData;
import gov.vha.isaac.rest.api1.data.concept.RestConceptUpdateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.tokens.EditTokens;


/**
 * {@link ConceptWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent)
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
public class ConceptWriteAPIs
{
	private static Logger log = LogManager.getLogger(ConceptWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

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
	public RestWriteResponseConceptCreate createConcept(
			RestConceptCreateData creationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken);
		
		try
		{
			if (StringUtils.isBlank(creationData.fsn)) {
				throw new RestException("RestConceptCreateData.fsn", creationData.fsn, "FSN required");
			}
			
			if (creationData.parentConceptIds == null || creationData.parentConceptIds.size() < 1) {
				throw new RestException("RestConceptCreateData.parentIds", creationData.parentConceptIds + "", "At least one parent concept id required");
			}
			
			ArrayList<ConceptSpecification> preferredDialects = new ArrayList<>();
			
			if (creationData.descriptionPreferredInDialectAssemblagesConceptIds != null)
			{
				for (String id : creationData.descriptionPreferredInDialectAssemblagesConceptIds) {
					preferredDialects.add(Get.conceptSpecification(
							RequestInfoUtils.getConceptSequenceFromParameter("RestConceptCreateData.descriptionPreferredInDialectAssemblagesConceptIds", id)));
				}
			}
			
			if (preferredDialects.size() == 0)
			{
				preferredDialects.add(MetaData.US_ENGLISH_DIALECT);
			}

			ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
			conceptBuilderService.setDefaultLanguageForDescriptions(StringUtils.isNotBlank(creationData.descriptionLanguageConceptId) ? 
					Get.conceptSpecification(RequestInfoUtils.getConceptSequenceFromParameter("RestConceptCreateData.descriptionLanguageConceptId", 
							creationData.descriptionLanguageConceptId))
						: MetaData.ENGLISH_LANGUAGE);
			conceptBuilderService.setDefaultDialectAssemblageForDescriptions(preferredDialects.get(0));
			conceptBuilderService.setDefaultLogicCoordinate(RequestInfo.get().getLogicCoordinate());

			LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();
			
			HashSet<String> parentSemanticTags = new HashSet<>();
			StampCoordinate readBackCoordinate = null;
			boolean makeSemTag = false;
			if (creationData.calculateSemanticTag != null && creationData.calculateSemanticTag.booleanValue()) {
				readBackCoordinate = Frills.getStampCoordinateFromEditCoordinate(RequestInfo.get().getStampCoordinate(), RequestInfo.get().getEditCoordinate());
				makeSemTag = true;
			}

			for (String parentId : creationData.parentConceptIds) {
				int conSequence = RequestInfoUtils.getConceptSequenceFromParameter("RestConceptCreateData.parentConceptIds", parentId);
				NecessarySet(And(ConceptAssertion(conSequence, defBuilder)));
				if (readBackCoordinate != null) {
					Frills.getDescriptionsOfType(Get.identifierService().getConceptNid(conSequence), MetaData.FULLY_SPECIFIED_NAME, readBackCoordinate).forEach(desc -> 
					{
						if (desc.getText().lastIndexOf('(') > 0 && desc.getText().lastIndexOf(')') > 0)
						{
							parentSemanticTags.add(desc.getText().substring(desc.getText().lastIndexOf('(') + 1, desc.getText().lastIndexOf(')')));
						}
					});
				}
			}
			
			if (makeSemTag && parentSemanticTags.size() != 1)
			{
				throw new RestException("Unable to automatically create semantic tag as requested.  Parent concepts have " + parentSemanticTags.size() + " semantic tags, "
						+ " when 1 is expected");
			}

			LogicalExpression parentDef = defBuilder.build();

			ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(creationData.fsn, makeSemTag ? parentSemanticTags.iterator().next() : null, parentDef);
			
			builder.setState((creationData.active == null || creationData.active.booleanValue()) ? State.ACTIVE : State.INACTIVE);
			
			// Add optional descriptionExtendedTypeConceptId, if exists
			if (creationData.extendedDescriptionTypeConcept != null) {
				builder.addSememe(Get.sememeBuilderService().getDynamicSememeBuilder(
						makeSemTag ? builder.getSynonymPreferredDescriptionBuilder() : builder.getFullySpecifiedDescriptionBuilder(), 
						DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence(),
						new DynamicSememeData[] {new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfoUtils.getConceptSequenceFromParameter("RestConceptCreateData.extendedDescriptionTypeConcept", 
										creationData.extendedDescriptionTypeConcept)).get())}));
			}
			
			// Add optional descriptionPreferredInDialectAssemblagesConceptIdsList beyond first (already added , if exists
			for (int i = 1; i < preferredDialects.size(); i++)
			{
				builder.getFullySpecifiedDescriptionBuilder().addPreferredInDialectAssemblage(preferredDialects.get(i));
				if (builder.getSynonymPreferredDescriptionBuilder() != null)
				{
					builder.getSynonymPreferredDescriptionBuilder().addPreferredInDialectAssemblage(preferredDialects.get(i));
				}
			}
			
			List<ObjectChronology<? extends StampedVersion>> createdObjects = new ArrayList<>();
			ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE, createdObjects).getNoThrow();

			Optional<CommitRecord> commitRecord = Get.commitService().commit(
					"creating new concept: NID=" + newCon.getNid() + ", FSN=" + creationData.fsn).get();
			
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
			
			return new RestWriteResponseConceptCreate(EditTokens.renew(RequestInfo.get().getEditToken()), newCon.getPrimordialUuid(), createdObjects);
		}
		catch (RestException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			log.error("Error creating concept", e);
			throw new RestException("Unexpected internal error creating concept");
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
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		State stateToUse = (conceptUpdateData.active == null || conceptUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		
		return ComponentWriteAPIs.resetState(RequestInfo.get().getEditCoordinate(), StampCoordinates.getDevelopmentLatest(), stateToUse, id);
	}
}