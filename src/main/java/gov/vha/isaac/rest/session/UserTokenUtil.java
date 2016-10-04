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

package gov.vha.isaac.rest.session;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.rest.SememeUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.PRISMEServices.User;
import gov.vha.isaac.rest.tokens.UserToken;

/**
 * 
 * {@link UserTokenUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
class UserTokenUtil {
	private UserTokenUtil() {}

	static UserToken getUserToken(
			User user,
			EditCoordinate adminEditCoordinate,
			StampCoordinate stampCoordinate,
			LanguageCoordinate languageCoordinate,
			LogicCoordinate logicCoordinate,
			UUID wfProcessId
			) throws RestException {
		Integer authorSequence = null;
		// Try to find existing author by PRISME user id
		
		//TODO this needs to be redone - this Sememe design was off / didn't make sense.
		//User should be located by a direct hash of "uniqueKeyFromSSO -> UUID".
		//We don't even need to store anything else on a sememe to do this hash - (though we can, for convenience)
		
		SememeSequenceSet prismeUserAnnotationSememeSequences = Get.sememeService().getSememeSequencesFromAssemblage(DynamicSememeConstants.get().DYNAMIC_SEMEME_PRISME_USER_ID.getConceptSequence());
		for (int prismeUserAnnotationSememeSequence : prismeUserAnnotationSememeSequences.asArray()) {
			SememeChronology prismeUserAnnotationSememeChronology = Get.sememeService().getSememe(prismeUserAnnotationSememeSequence);
			Optional<LatestVersion<DynamicSememeImpl>> prismeUserAnnotationSememeVersionOptional = prismeUserAnnotationSememeChronology.getLatestVersion(DynamicSememeImpl.class, stampCoordinate);
			DynamicSememeImpl prismeUserAnnotationSememe = prismeUserAnnotationSememeVersionOptional.get().value();

			Long prismeId = null;
			for (DynamicSememeData data : prismeUserAnnotationSememe.getData()) {
				if (data.getDynamicSememeDataType() == DynamicSememeDataType.LONG) {
					prismeId = ((DynamicSememeLongImpl)data).getDataLong();
					break;
				}
			}
			if (prismeId == null) {
				throw new RuntimeException("DYNAMIC_SEMEME_PRISME_USER_ID annotation does not contain Long data value");
			}
			if (user.getId() == prismeId) {
				authorSequence = Get.identifierService().getConceptSequence(prismeUserAnnotationSememe.getReferencedComponentNid());
				break;
			}
		}

		// If no existing author by PRISME user id, create new
		if (authorSequence == null) {
			final String fsn = user.getUserName();

			try
			{
				ConceptSpecification defaultDescriptionsLanguageConceptSpec = Get.conceptSpecification(languageCoordinate.getLanguageConceptSequence());
				ConceptSpecification defaultDescriptionDialectConceptSpec = Get.conceptSpecification(languageCoordinate.getDialectAssemblagePreferenceList()[0]);

				ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
				conceptBuilderService.setDefaultLanguageForDescriptions(defaultDescriptionsLanguageConceptSpec);
				conceptBuilderService.setDefaultDialectAssemblageForDescriptions(defaultDescriptionDialectConceptSpec);
				conceptBuilderService.setDefaultLogicCoordinate(logicCoordinate);

				LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

				NecessarySet(And(ConceptAssertion(MetaData.USER, defBuilder)));

				LogicalExpression parentDef = defBuilder.build();

				ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(
						fsn,
						null,
						parentDef);

				// Add PRISME user.id in DYNAMIC_SEMEME_PRISME_USER_ID annotation
				// TODO confirm that user.id is being added in DYNAMIC_SEMEME_PRISME_USER_ID annotation
				SememeChronology<DynamicSememe<?>> requiredDescriptionsExtendedTypeSememe = null;
				requiredDescriptionsExtendedTypeSememe = 
						SememeUtil.addAnnotation(
								EditCoordinates.getDefaultUserMetadata(),
								builder.getNid(),
								new DynamicSememeLongImpl(user.getId()),
								DynamicSememeConstants.get().DYNAMIC_SEMEME_PRISME_USER_ID.getPrimordialUuid());

				// Add optional descriptionPreferredInDialectAssemblagesConceptIdsList beyond first (already added , if exists
				if (languageCoordinate.getDialectAssemblagePreferenceList() != null && languageCoordinate.getDialectAssemblagePreferenceList().length > 0) {
					for (int i : languageCoordinate.getDialectAssemblagePreferenceList()) {
						builder.getFullySpecifiedDescriptionBuilder().setPreferredInDialectAssemblage(Get.conceptSpecification(i));
					}
				}

				List<?> createdObjects = new ArrayList<>();
				ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE, createdObjects).getNoThrow();

				Get.commitService().addUncommitted(newCon).get();

				if (requiredDescriptionsExtendedTypeSememe != null) {
					Get.commitService().addUncommitted(requiredDescriptionsExtendedTypeSememe).get();
				}

				Optional<CommitRecord> commitRecord = Get.commitService().commit(
						"creating new concept: NID=" + newCon.getNid() + ", FSN=" + fsn).get();
			}
			catch (Exception e)
			{
				throw new RestException("Creation of user concept failed. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			}
		}

		return new UserToken(
				authorSequence,
				adminEditCoordinate.getModuleSequence(),
				adminEditCoordinate.getPathSequence(),
				RequestInfo.workflowProcessId);
	}

}
