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
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.tokens.EditToken;
import gov.vha.isaac.rest.tokens.EditTokens;

/**
 * 
 * {@link EditTokenUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
class EditTokenUtil {
	private EditTokenUtil() {}

	static UUID getUuidFromUserFsn(String fsn) {
		return UuidT5Generator.get(MetaData.USER.getPrimordialUuid(), fsn);
	}
	static EditToken getUserToken(
			User user,
			int moduleSequence,
			int pathSequence,
			UUID processId) throws RestException {
		EditCoordinate adminEditCoordinate = EditCoordinates.getDefaultUserMetadata();
		LanguageCoordinate languageCoordinate = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
		LogicCoordinate logicCoordinate = LogicCoordinates.getStandardElProfile();
		
		Integer authorSequence = null;
		
		// If the SSO UUID already persisted
		if (Get.identifierService().hasUuid(user.getId())) {
			// Set authorSequence to value corresponding to SSO UUID
			authorSequence = Get.identifierService().getConceptSequenceForUuids(user.getId());
		}

		// If no existing author by SSO UUID, create new author concept with that SSO UUID
		if (authorSequence == null) {
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
						user.getName(),
						null,
						parentDef);

				// Set new author concept UUID to SSO UUID
				builder.setPrimordialUuid(user.getId());

				// Add PRISME user.id in DYNAMIC_SEMEME_PRISME_USER_ID annotation
				// TODO confirm that user.id is being added in DYNAMIC_SEMEME_PRISME_USER_ID annotation
//				SememeChronology<DynamicSememe<?>> prismeUserIdSememe = null;
//				prismeUserIdSememe = 
//						SememeUtil.addAnnotation(
//								adminEditCoordinate,
//								builder.getNid(),
//								new DynamicSememeLongImpl(user.getId()),
//								DynamicSememeConstants.get().DYNAMIC_SEMEME_PRISME_USER_ID.getPrimordialUuid());

				if (languageCoordinate.getDialectAssemblagePreferenceList() != null && languageCoordinate.getDialectAssemblagePreferenceList().length > 0) {
					for (int i : languageCoordinate.getDialectAssemblagePreferenceList()) {
						builder.getFullySpecifiedDescriptionBuilder().setPreferredInDialectAssemblage(Get.conceptSpecification(i));
					}
				}

				List<?> createdObjects = new ArrayList<>();
				ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(adminEditCoordinate, ChangeCheckerMode.ACTIVE, createdObjects).getNoThrow();

				Get.commitService().addUncommitted(newCon).get();

//				if (prismeUserIdSememe != null) {
//					Get.commitService().addUncommitted(prismeUserIdSememe).get();
//				}

				@SuppressWarnings("deprecation")
				Optional<CommitRecord> commitRecord = Get.commitService().commit(
						"creating new concept: NID=" + newCon.getNid() + ", FSN=" + user.getName()).get();
				authorSequence = newCon.getConceptSequence();
				
			}
			catch (Exception e)
			{
				throw new RestException("Creation of user concept failed. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			}
		}

		EditToken editToken = EditTokens.getOrCreate(
				authorSequence,
				moduleSequence,
				pathSequence,
				processId,
				user.getRoles());
		
		return editToken;
	}

}
