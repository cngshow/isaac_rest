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
package gov.vha.isaac.rest;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.assertions.ConceptAssertion;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;

/**
 * 
 * {@link DescriptionUtil}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
public class DescriptionUtil
{
	public static enum DescriptionType 
	{
		FSN, SYNONYM, DEFINITION;

		private ConceptSpecification getConceptSpec()
		{
			if (DescriptionType.FSN == this)
			{
				return MetaData.FULLY_SPECIFIED_NAME;
			}
			else if (DescriptionType.SYNONYM == this)
			{
				return MetaData.SYNONYM;
			}
			else if (DescriptionType.DEFINITION == this)
			{
				return MetaData.DEFINITION_DESCRIPTION_TYPE;
			}
			else
			{
				throw new RuntimeException("Unsupported descriptiontype '" + this + "'");
			}
		}

		public static DescriptionType parse(UUID typeId)
		{
			if (MetaData.FULLY_SPECIFIED_NAME.getPrimordialUuid().equals(typeId))
			{
				return FSN;
			}
			else if (MetaData.SYNONYM.getPrimordialUuid().equals(typeId))
			{
				return SYNONYM;
			}
			if (MetaData.DEFINITION_DESCRIPTION_TYPE.getPrimordialUuid().equals(typeId))
			{
				return DEFINITION;
			}
			throw new RuntimeException("Unsupported description type UUID " + typeId);
		}
	};

	private static HashMap<UUID, DynamicSememeColumnInfo[]> refexAllowedColumnTypes_ = new HashMap<>();
	static {
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_INDEX_CONFIGURATION.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getDynamicSememeColumns());
		refexAllowedColumnTypes_.put(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE.getUUID(), 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE.getDynamicSememeColumns());
		//TODO figure out how to get rid of this copy/paste mess too
		refexAllowedColumnTypes_.put(MetaData.LOINC_NUM.getPrimordialUuid(), new DynamicSememeColumnInfo[] { new DynamicSememeColumnInfo(0,
				DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_VALUE.getPrimordialUuid(), DynamicSememeDataType.STRING, null, true, true) });
	}

	/**
	 * Add a description to the concept.
	 * 
	 * @param conceptId - the concept to add this description to
	 * @param descriptionValue - the text value
	 * @param descriptionType - the type of the description FSN, SYNONYM, DEFINITION;
	 * @param preferredDialectConceptIds - set of ids of preferred acceptability concepts see {@link #addDescriptionAcceptibility()}
	 * @param acceptableDialectConceptIds - set of ids of acceptable acceptability concepts see {@link #addDescriptionAcceptibility()}
	 * @param caseSignificanceConceptId - concept id of case significance concept {@link MetaData.DESCRIPTION_CASE_SENSITIVE} or {@link MetaData.MetaData.DESCRIPTION_NOT_CASE_SENSITIVE}
	 * @param languageConceptId - concept id of description language. Is-a {@link MetaData#LANGUAGE_CONCEPT_SEQUENCE_FOR_DESCRIPTION}
	 * @param sourceDescriptionTypeUUID - this optional value is attached as the extended description type
	 */
	@SuppressWarnings("unchecked")
	public static SememeChronology<DescriptionSememe<?>> addDescription(
			EditCoordinate ec,
			int conceptId,
			String descriptionValue, 
			DescriptionType descriptionType,
			Set<Integer> preferredDialectConceptIds,
			Set<Integer> acceptableDialectConceptIds,
			int caseSignificanceConceptId,
			int languageConceptId,
			UUID sourceDescriptionTypeUUID)
	{
		
		if (descriptionValue == null)
		{
			throw new RuntimeException("Description value is required");
		}

		@SuppressWarnings({ "rawtypes" }) 
		SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder
				= Get.sememeBuilderService().getDescriptionSememeBuilder(
						caseSignificanceConceptId,
						languageConceptId,
						descriptionType.getConceptSpec().getConceptSequence(), 
						descriptionValue, 
						conceptId);
		SememeChronology<DescriptionSememe<?>> newDescription = (SememeChronology<DescriptionSememe<?>>)
				descBuilder.build(
						ec, ChangeCheckerMode.ACTIVE);

		for (int id : preferredDialectConceptIds) {
			SememeBuilder<?> acceptabilityTypeBuilder = Get.sememeBuilderService().getComponentSememeBuilder(
					TermAux.PREFERRED.getNid(), newDescription.getNid(),
					id);
			acceptabilityTypeBuilder.build(ec, ChangeCheckerMode.ACTIVE);
		}
		for (int id : acceptableDialectConceptIds) {
			SememeBuilder<?> acceptabilityTypeBuilder = Get.sememeBuilderService().getComponentSememeBuilder(
					TermAux.ACCEPTABLE.getNid(), newDescription.getNid(),
					id);
			acceptabilityTypeBuilder.build(ec, ChangeCheckerMode.ACTIVE);
		}

		if (sourceDescriptionTypeUUID != null)
		{
			addAnnotation(
					ec,
					conceptId,
					(sourceDescriptionTypeUUID == null ? null : new DynamicSememeUUIDImpl(sourceDescriptionTypeUUID)),
					DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getPrimordialUuid());
		}

		return newDescription;
	}

	/**
	 * Add a description to the concept.
	 * 
	 * @param acceptabilityPrimordialUUID - if not supplied, created from the description UUID, dialectRefsetg and preferred flag
	 * @param dialectConceptId - A UUID for a refset like MetaData.US_ENGLISH_DIALECT
	 * @param preferred - true for preferred, false for acceptable
	 * @param state - 
	 * @param time - if null, uses the description time
	 * @param module - optional
	 */
	public SememeChronology<ComponentNidSememe<?>> addDescriptionAcceptibility(
			EditCoordinate ec,
			int descriptionNid,
			int dialectConceptSequence,
			boolean preferred)
	{
		@SuppressWarnings("rawtypes")
		SememeBuilder sb = Get.sememeBuilderService().getComponentSememeBuilder(
				preferred ? TermAux.PREFERRED.getNid() : TermAux.ACCEPTABLE.getNid(),
						descriptionNid,
						dialectConceptSequence);

		@SuppressWarnings("unchecked")
		SememeChronology<ComponentNidSememe<?>> sc = (SememeChronology<ComponentNidSememe<?>>)sb.build(
				ec, ChangeCheckerMode.ACTIVE);

		return sc;
	}

	/**
	 * uses the concept time, UUID is created from the component UUID, the annotation value and type.
	 */
	public SememeChronology<DynamicSememe<?>> addStringAnnotation(
			EditCoordinate ec,
			int referencedComponentNid,
			String annotationValue,
			UUID refsetUuid)
	{
		return addAnnotation(
				ec,
				referencedComponentNid,
				new DynamicSememeData[] {new DynamicSememeStringImpl(annotationValue)},
				refsetUuid);
	}

	public SememeChronology<DynamicSememe<?>> addRefsetMembership(
			EditCoordinate ec,
			int referencedComponentNid,
			UUID refexDynamicTypeConceptUuid)
	{
		return addAnnotation(ec, referencedComponentNid, (DynamicSememeData[])null, refexDynamicTypeConceptUuid);
	}

	/**
	 * @param referencedComponentNid The component to attach this annotation to
	 * @param value - the value to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
	 * of the sememe represented by refexDynamicTypeUuid
	 * @param refexDynamicTypeConceptUuid - the uuid of the dynamic sememe type
	 * @return
	 */
	public static SememeChronology<DynamicSememe<?>> addAnnotation(
			EditCoordinate ec,
			int referencedComponentNid,
			DynamicSememeData value, 
			UUID refexDynamicTypeConceptUuid)
	{
		return addAnnotation(ec,
				referencedComponentNid, 
				(value == null ? new DynamicSememeData[] {} : new DynamicSememeData[] {value}),
				refexDynamicTypeConceptUuid);
	}

	/**
	 * @param referencedComponentNid The component to attach this annotation to
	 * @param values - the values to attach (may be null if the annotation only serves to mark 'membership') - columns must align with values specified in the definition
	 * of the sememe represented by refexDynamicTypeUuid
	 * @param refexDynamicTypeConceptId - the uuid of the dynamic sememe type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static SememeChronology<DynamicSememe<?>> addAnnotation(
			EditCoordinate ec,
			int referencedComponentNid,
			DynamicSememeData[] values, 
			UUID refexDynamicTypeConceptId)
	{
		validateDataTypes(refexDynamicTypeConceptId, values);
		@SuppressWarnings("rawtypes")
		SememeBuilder sb = Get.sememeBuilderService().getDynamicSememeBuilder(referencedComponentNid, 
				Get.identifierService().getConceptSequenceForUuids(refexDynamicTypeConceptId), values);

		SememeChronology<DynamicSememe<?>> sc = (SememeChronology<DynamicSememe<?>>)sb.build(ec, ChangeCheckerMode.ACTIVE);

		return sc;
	}

//	private static boolean isConfiguredAsDynamicSememe(UUID refexDynamicTypeUuid)
//	{
//		return refexAllowedColumnTypes_.containsKey(refexDynamicTypeUuid);
//	}

	/**
	 * @param refexDynamicTypeUuid
	 * @param values
	 */
	private static void validateDataTypes(UUID refexDynamicTypeUuid, DynamicSememeData[] values)
	{
		//TODO this should be a much better validator - checking all of the various things in RefexDynamicCAB.validateData - or in 
		//generateMetadataEConcepts - need to enforce the restrictions defined in the columns in the validators

		if (!refexAllowedColumnTypes_.containsKey(refexDynamicTypeUuid))
		{
			throw new RuntimeException("Attempted to store data on a concept not configured as a dynamic sememe");
		}

		DynamicSememeColumnInfo[] colInfo = refexAllowedColumnTypes_.get(refexDynamicTypeUuid);

		if (values != null && values.length > 0)
		{
			if (colInfo != null)
			{
				for (int i = 0; i < values.length; i++)
				{
					DynamicSememeColumnInfo column = null;
					for (DynamicSememeColumnInfo x : colInfo)
					{
						if(x.getColumnOrder() == i)
						{
							column = x;
							break;
						}
					}
					if (column == null)
					{
						throw new RuntimeException("Column count mismatch");
					}
					else
					{
						if (values[i] == null && column.isColumnRequired())
						{
							throw new RuntimeException("Missing column data for column " + column.getColumnName());
						}
						else if (values[i] != null && column.getColumnDataType() != values[i].getDynamicSememeDataType())
						{
							throw new RuntimeException("Datatype mismatch - " + column.getColumnDataType() + " - " + values[i].getDynamicSememeDataType());
						}
					}
				}
			}
			else if (values.length > 0)
			{
				throw new RuntimeException("Column count mismatch - this dynamic sememe doesn't allow columns!");
			}
		}
		else if (colInfo != null)
		{
			for (DynamicSememeColumnInfo ci : colInfo)
			{
				if (ci.isColumnRequired())
				{
					throw new RuntimeException("Missing column data for column " + ci.getColumnName());
				}
			}
		}
	}

	public static SememeChronology<StringSememe<?>> addStaticStringAnnotation(
			EditCoordinate ec,
			int referencedComponentNid,
			String annotationValue,
			int refsetConceptSequence)
	{
		@SuppressWarnings("rawtypes")
		SememeBuilder sb = Get.sememeBuilderService().getStringSememeBuilder(
				annotationValue,
				referencedComponentNid, 
				refsetConceptSequence);

		@SuppressWarnings("unchecked")
		SememeChronology<StringSememe<?>> sc = (SememeChronology<StringSememe<?>>)sb.build(ec, ChangeCheckerMode.ACTIVE);

		return sc;
	}

	public static SememeChronology<DynamicSememe<?>> addUUIDAnnotation(
			EditCoordinate ec,
			int referencedComponentNid,
			UUID annotationValue,
			UUID annotationRefsetConceptUuid)
	{
		return addAnnotation(
				ec,
				referencedComponentNid,
				new DynamicSememeData[] {new DynamicSememeUUIDImpl(annotationValue)},
				annotationRefsetConceptUuid);
	}


	/**
	 * Add an IS_A_REL relationship
	 * Can only be called once per concept.
	 */
	public static SememeChronology<LogicGraphSememe<?>> addParent(
			EditCoordinate ec,
			LogicCoordinate lc,
			int concept,
			UUID parentConceptUuid)
	{
		return addParent(ec, lc, concept, new UUID[] {parentConceptUuid}, null);
	}

	/**
	 * Add a parent (is a ) relationship. The source of the relationship is assumed to be the specified concept.
	 * Can only be called once per concept
	 */
	public static SememeChronology<LogicGraphSememe<?>> addParent(
			EditCoordinate ec,
			LogicCoordinate lc,
			int concept,
			UUID[] targetConceptIds,
			UUID sourceRelTypeConceptUUID)
	{
		LogicalExpressionBuilder leb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();

		//We are only building isA here, choose necessary set over sufficient.

		ConceptAssertion[] cas = new ConceptAssertion[targetConceptIds.length];
		for (int i = 0; i < targetConceptIds.length; i++)
		{
			cas[i] = ConceptAssertion(Get.identifierService().getConceptSequenceForUuids(targetConceptIds[i]), leb);
		}

		NecessarySet(And(cas));

		LogicalExpression logicalExpression = leb.build();

		@SuppressWarnings("rawtypes")
		SememeBuilder sb = Get.sememeBuilderService().getLogicalExpressionSememeBuilder(logicalExpression, concept,
				lc.getStatedAssemblageSequence());

		@SuppressWarnings("unchecked")
		SememeChronology<LogicGraphSememe<?>> sci = (SememeChronology<LogicGraphSememe<?>>) sb.build(ec, ChangeCheckerMode.ACTIVE);

		if (sourceRelTypeConceptUUID != null) {
			addUUIDAnnotation(ec, concept, sourceRelTypeConceptUUID,
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_RELATIONSHIP_TYPE.getPrimordialUuid());
		}

		return sci;
	}

	public static SememeChronology<LogicGraphSememe<?>> addRelationshipGraph(
			EditCoordinate ec,
			LogicCoordinate lc,
			int concept,
			LogicalExpression logicalExpression,
			boolean stated)
	{		
		@SuppressWarnings("rawtypes") 
		SememeBuilder sb = Get.sememeBuilderService().getLogicalExpressionSememeBuilder(
				logicalExpression,
				concept,
				stated ? lc.getStatedAssemblageSequence() : lc.getInferredAssemblageSequence());

		@SuppressWarnings("unchecked")
		SememeChronology<LogicGraphSememe<?>> sci = (SememeChronology<LogicGraphSememe<?>>) sb.build(
				ec, ChangeCheckerMode.ACTIVE);

		return sci;
	}
}
