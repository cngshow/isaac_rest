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
package gov.vha.isaac.rest.api1.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexerConfiguration;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValueBaseCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfoCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.EditTokens;


/**
 * {@link MappingWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.mappingAPIsPathComponent)
public class MappingWriteAPIs
{
	private static Logger log = LogManager.getLogger(MappingWriteAPIs.class);

	/**
	 * @param mappingSetCreationData - object containing data used to create new mapping set
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the UUID identifying the created concept which defines the map set
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + RestPaths.createPathComponent)
	public RestWriteResponse createNewMapSet(
		RestMappingSetVersionBaseCreate mappingSetCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);

		
		try 
		{
			return createMappingSet(
					mappingSetCreationData.name,
					mappingSetCreationData.inverseName,
					mappingSetCreationData.purpose,
					mappingSetCreationData.description,
					mappingSetCreationData.mapItemExtendedFieldsDefinition,
					mappingSetCreationData.mapSetExtendedFields,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate());
		} 
		catch (IOException e) 
		{
			throw new RestException("Failed creating mapping set name=" + mappingSetCreationData.name + ", inverse=" 
					+ mappingSetCreationData.inverseName + ", purpose=" + mappingSetCreationData.purpose + ", desc=" + mappingSetCreationData.description);
		}
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param mappingSetUpdateData - object containing data used to update existing mapping set
	 * @param id - id of mapping set concept to update
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id + "}")
	public RestWriteResponse updateMapSet(
		RestMappingSetVersionBase mappingSetUpdateData,
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);

		if (StringUtils.isBlank(mappingSetUpdateData.name))
		{
			throw new RestException("The parameter 'name' is required");
		}
		if (StringUtils.isBlank(mappingSetUpdateData.description))
		{
			throw new RestException("The parameter 'description' is required");
		}

		// TODO This update method doesn't currently allow updating of extended field values.  Need to figure out how to put that into the API
		State stateToUse = (mappingSetUpdateData.active == null || mappingSetUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		
		ConceptChronology<?> mappingConcept = ConceptAPIs.findConceptChronology(id);
		
		return updateMappingSet(
				mappingConcept,
				StringUtils.isBlank(mappingSetUpdateData.name) ? "" : mappingSetUpdateData.name.trim(),
				StringUtils.isBlank(mappingSetUpdateData.inverseName) ? "" : mappingSetUpdateData.inverseName.trim(),
				StringUtils.isBlank(mappingSetUpdateData.description) ? "" : mappingSetUpdateData.description.trim(),
				StringUtils.isBlank(mappingSetUpdateData.purpose) ? "" : mappingSetUpdateData.purpose.trim(),
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getEditCoordinate(),
				stateToUse);
	}
	
	/**
	 * @param mappingItemCreationData - RestMappingItemVersionBaseCreate object containing data to create new mapping item
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sememe UUID identifying the sememe which stores the created mapping item
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemComponent + RestPaths.createPathComponent)
	public RestWriteResponse createNewMappingItem(
		RestMappingItemVersionCreate mappingItemCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);

		Optional<UUID> sourceConcept = readConcept(mappingItemCreationData.sourceConcept, "mappingItemCreationData.sourceConcept");
		Optional<UUID> targetConcept = readConcept(mappingItemCreationData.targetConcept, "mappingItemCreationData.targetConcept");
		Optional<UUID> mappingSetID = readConcept(mappingItemCreationData.mapSetConcept, "mappingItemCreationData.mapSetConcept");
		Optional<UUID> qualifierID = readConcept(mappingItemCreationData.qualifierConcept, "mappingItemCreationData.qualifierConcept");
		
		if (!sourceConcept.isPresent())
		{
			throw new RestException("sourceConcept", mappingItemCreationData.sourceConcept + "", "Unable to locate the source concept");
		}
		if (!mappingSetID.isPresent())
		{
			throw new RestException("mapSetConcept", mappingItemCreationData.mapSetConcept + "", "Unable to locate the map set");
		}
		if (mappingItemCreationData.targetConcept != null && !targetConcept.isPresent())
		{
			throw new RestException("targetConcept", mappingItemCreationData.targetConcept + "", "Unable to locate the target concept");
		}
		if (mappingItemCreationData.qualifierConcept != null && !qualifierID.isPresent())
		{
			throw new RestException("qualifierConcept", mappingItemCreationData.qualifierConcept + "", "Unable to locate the qualifier concept");
		}
		
		return createMappingItem(
						sourceConcept.get(),
						mappingSetID.get(),
						targetConcept.orElse(null),
						qualifierID.orElse(null),
						mappingItemCreationData.mapItemExtendedFields,
						(mappingItemCreationData.active == null || mappingItemCreationData.active ? true : false),
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getEditCoordinate());
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param mappingItemUpdateData - object containing data used to update existing mapping item
	 * @param id - id of mapping item sememe to update
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public RestWriteResponse updateMappingItem(
		RestMappingItemVersionUpdate mappingItemUpdateData,
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);

		State stateToUse = (mappingItemUpdateData.active == null || mappingItemUpdateData.active) ? State.ACTIVE : State.INACTIVE;

		SememeChronology<?> mappingItemSememeChronology = SememeAPIs.findSememeChronology(id);
		
		Optional<UUID> targetConcept = readConcept(mappingItemUpdateData.targetConcept, "mappingItemUpdateData.targetConcept");
		
		if (mappingItemUpdateData.targetConcept != null && !targetConcept.isPresent())
		{
			throw new RestException("targetConcept", mappingItemUpdateData.targetConcept + "", "Unable to locate the target concept");
		}

		Optional<UUID> qualifierConcept = readConcept(mappingItemUpdateData.qualifierConcept, "mappingItemUpdateData.qualifierConcept");
		
		try {
			return updateMappingItem(
					mappingItemSememeChronology,
					targetConcept.orElse(null),
					qualifierConcept.isPresent() ? Get.conceptService().getConcept(qualifierConcept.get()) : null,
					mappingItemUpdateData.mapItemExtendedFields,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate(),
					stateToUse);

		} catch (IOException e) {
			throw new RestException("Failed updating mapping item " + id + " on " + e.getClass().getName() + " exception \"" + e.getLocalizedMessage() + "\"");
		}
	}
	
	private Optional<UUID> readConcept(String conceptIdentifier, String paramName) throws RestException
	{
		if (StringUtils.isBlank(conceptIdentifier))
		{
			return Optional.empty();
		}
		return Get.identifierService().getUuidPrimordialFromConceptId(RequestInfoUtils.getConceptSequenceFromParameter(paramName, conceptIdentifier));
		
	}
	
	/**
	 * Create and store a new mapping set in the DB.
	 * @param mappingName - The name of the mapping set (used for the FSN and preferred term of the underlying concept)
	 * @param inverseName - (optional) inverse name of the mapping set (if it makes sense for the mapping)
	 * @param purpose - (optional) - user specified purpose of the mapping set
	 * @param description - the intended use of the mapping set
	 * @return the UUID of the created map set
	 * @throws IOException
	 */
	private static RestWriteResponse createMappingSet(
			String mappingName,
			String inverseName,
			String purpose,
			String description,
			List<RestDynamicSememeColumnInfoCreate> extendedFields,
			List<RestMappingSetExtensionValueBaseCreate> mapSetExtendedFields,
			StampCoordinate stampCoord, 
			EditCoordinate editCoord) throws IOException
	{
		//We need to create a new concept - which itself is defining a dynamic sememe - so set that up here.
		
		if (StringUtils.isBlank(mappingName))
		{
			throw new RestException("The parameter 'name' is required");
		}
		if (StringUtils.isBlank(description))
		{
			throw new RestException("The parameter 'description' is required");
		}
		
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[2 + (extendedFields == null ? 0 : extendedFields.size())];
		columns[0] = new DynamicSememeColumnInfo(0, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
				DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.COMPONENT_TYPE, 
				new DynamicSememeArrayImpl<>(new DynamicSememeString[] {new DynamicSememeStringImpl(ObjectChronologyType.CONCEPT.name())}), 
				true);
		columns[1] = new DynamicSememeColumnInfo(1, IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_QUALIFIER.getUUID(), DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_QUALIFIERS.getUUID()), true);
		if (extendedFields != null)
		{
			int i = 2;
			for (RestDynamicSememeColumnInfoCreate colInfo : extendedFields)
			{
				columns[i] = new DynamicSememeColumnInfo(i++, 
						Get.identifierService().getUuidPrimordialFromConceptId(colInfo.columnLabelConcept).get(), 
						DynamicSememeDataType.parse(colInfo.columnDataType, true), RestDynamicSememeData.translate(colInfo.columnDefaultData), colInfo.columnRequired, 
						DynamicSememeValidatorType.parse(colInfo.columnValidatorTypes, true), RestDynamicSememeData.translate(colInfo.columnValidatorData), true);
			}
		}
		
		DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(
				mappingName, mappingName, description, columns,
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getConceptSequence(), ObjectChronologyType.CONCEPT, null, editCoord);
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				//TODO 2 Dan (index config)  see if I still need to manually do this, I thought I fixed this.
				SememeIndexerConfiguration.configureColumnsToIndex(rdud.getDynamicSememeUsageDescriptorSequence(), new Integer[] {0, 1, 2}, true);
			}
			catch (Exception e)
			{
				log.error("Unexpected error enabling the index on newly created mapping set!", e);
			}
		});
		
		//Then, annotate the concept created above as a member of the MappingSet dynamic sememe, and add the inverse name, if present.
		if (!StringUtils.isBlank(inverseName))
		{
			ObjectChronology<?> builtDesc = LookupService.get().getService(DescriptionBuilderService.class).getDescriptionBuilder(inverseName, rdud.getDynamicSememeUsageDescriptorSequence(), 
					MetaData.SYNONYM, MetaData.ENGLISH_LANGUAGE).addAcceptableInDialectAssemblage(MetaData.US_ENGLISH_DIALECT).build(editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
			
			Get.sememeBuilderService().getDynamicSememeBuilder(builtDesc.getNid(),DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).build(
					editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		
		@SuppressWarnings("rawtypes")
		SememeChronology mappingAnnotation = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence(), 
				new DynamicSememeData[] {
						(StringUtils.isBlank(purpose) ? null : new DynamicSememeStringImpl(purpose))}).build(
				editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();

		if (mapSetExtendedFields != null)
		{
			for (RestMappingSetExtensionValueBaseCreate field : mapSetExtendedFields)
			{
				if (field.extensionValue instanceof RestDynamicSememeString)
				{
					@SuppressWarnings({ "rawtypes", "unused" })
					SememeChronology extension = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
							IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getSequence(), 
							new DynamicSememeData[] {
									new DynamicSememeNidImpl(Get.identifierService().getConceptNid(field.extensionNameConcept)),
									new DynamicSememeStringImpl(((RestDynamicSememeString)field.extensionValue).getString())}).build(
							editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
				}
				else if (field.extensionValue instanceof RestDynamicSememeNid || field.extensionValue instanceof RestDynamicSememeUUID)
				{
					@SuppressWarnings({ "rawtypes", "unused" })
					SememeChronology extension = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
							IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_NID_EXTENSION.getSequence(), 
							new DynamicSememeData[] {
									new DynamicSememeNidImpl(Get.identifierService().getConceptNid(field.extensionNameConcept)),
									new DynamicSememeNidImpl(
											(field.extensionValue instanceof RestDynamicSememeNid ? 
												((RestDynamicSememeNid)field.extensionValue).getNid() :
												Get.identifierService().getNidForUuids(((RestDynamicSememeUUID)field.extensionValue).getUUID())))
													}).build(editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
				}
				else
				{
					throw new RuntimeException("Unsupported map set extension field type");
				}
			}
		}
		
		try
		{
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing create of mapping set " + rdud.getDynamicSememeName()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed during commit", e);
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), 
				Get.identifierService().getUuidPrimordialFromConceptId(rdud.getDynamicSememeUsageDescriptorSequence()).get());
	}
	private static RestWriteResponse updateMappingSet(
			ConceptChronology<?> mappingConcept,
			String mapName,
			String mapInverseName,
			String mapDescription,
			String mapPurpose,
			StampCoordinate stampCoord,
			EditCoordinate editCoord,
			State state) throws RuntimeException, RestException 
	{		
		Get.sememeService().getSememesForComponent(mappingConcept.getNid()).filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(descriptionC ->
			{
				try
				{
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<LatestVersion<DescriptionSememe<?>>> latest = ((SememeChronology)descriptionC).getLatestVersion(DescriptionSememe.class, 
							stampCoord);
					if (latest.isPresent())
					{
						//TODO handle contradictions
						DescriptionSememe<?> ds = latest.get().value();
						if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
						{
							if (Frills.isDescriptionPreferred(ds.getNid(), null))
							{
								if (!ds.getText().equals(mapName))
								{
									@SuppressWarnings({ "unchecked", "rawtypes" })
									MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
											.createMutableVersion(MutableDescriptionSememe.class, State.ACTIVE, editCoord);
									mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
									mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
									mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
									mutable.setText(mapName);
									Get.commitService().addUncommitted(ds.getChronology()).get();
								}
							}
							else
							//see if it is the inverse name
							{
								if (Get.sememeService().getSememesForComponentFromAssemblage(ds.getNid(), 
										DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).anyMatch(sememeC -> 
										{
											return true;  //return if active or inactive
										}))
								{
									if (!ds.getText().equals(mapInverseName))
									{
										@SuppressWarnings({ "unchecked", "rawtypes" })
										MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
												.createMutableVersion(MutableDescriptionSememe.class, (StringUtils.isBlank(mapInverseName) ? State.INACTIVE : State.ACTIVE), editCoord);
										mutable.setText(StringUtils.isBlank(mapInverseName) ? ds.getText() : mapInverseName);
										mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
										mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
										mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
										
										Get.commitService().addUncommitted(ds.getChronology()).get();
									}
								}
							}
						}
						else if (ds.getDescriptionTypeConceptSequence() == MetaData.DEFINITION_DESCRIPTION_TYPE.getConceptSequence())
						{
							if (Frills.isDescriptionPreferred(ds.getNid(), null))
							{
								if (!mapDescription.equals(ds.getText()))
								{
									@SuppressWarnings({ "unchecked", "rawtypes" })
									MutableDescriptionSememe mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
											.createMutableVersion(MutableDescriptionSememe.class, State.ACTIVE, editCoord);
									mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
									mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
									mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
									mutable.setText(mapDescription);
									Get.commitService().addUncommitted(ds.getChronology()).get();
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					log.error("unexpected error updating mapping set", e);
					throw new RuntimeException("Unexpected error during update");
				}
			});
		

		Optional<SememeChronology<? extends SememeVersion<?>>> mappingSememe =  Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.getNid(), 
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).findAny();
					
		if (!mappingSememe.isPresent())
		{
			log.error("Couldn't find mapping refex?");
			throw new RestException("internal error");
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latestVersion = ((SememeChronology)mappingSememe.get()).getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));
		//TODO handle contradictions
		DynamicSememe<?> latest = latestVersion.get().value();
		
		String currentMapPurposeValue = (latest.getData().length == 0 || latest.getData()[0] == null ? "" : latest.getData()[0].dataToString());

		try
		{
			if (!currentMapPurposeValue.equals(mapPurpose))
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingSememe.get()).createMutableVersion(
						MutableDynamicSememe.class,
						State.ACTIVE,
						editCoord);
	
				mutable.setData(new DynamicSememeData[] {
						(StringUtils.isBlank(mapPurpose) ? null : new DynamicSememeStringImpl(mapPurpose))});
				Get.commitService().addUncommitted(latest.getChronology()).get();
			}
			
			//Look up the current state of the mapset concept - if it differs, update the concept.
			Optional<LatestVersion<ConceptVersionImpl>> concept = ((ConceptChronology)mappingConcept).getLatestVersion(ConceptVersionImpl.class, stampCoord);
			
			if (!concept.isPresent())
			{
				log.error("Couldn't find mapping refex?");
				throw new RuntimeException("internal error");
			}
			else if (concept.get().value().getState() != state) 
			{
				mappingConcept.createMutableVersion(state, editCoord);
				Get.commitService().addUncommitted(mappingConcept).get();
			}

			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of mapping set " + mappingConcept.getPrimordialUuid()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (RestException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			log.error("Error during commit", e);
			throw new RestException("Unexpected internal error");
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), mappingConcept.getPrimordialUuid());
	}

	/**
	 * Construct (and save to the DB) a new MappingItem.  
	 * @param sourceConcept - the primary ID of the source concept
	 * @param mappingSetID - the primary ID of the mapping type
	 * @param targetConcept - the primary ID of the target concept
	 * @param qualifierID - (optional) the primary ID of the qualifier concept
	 * @param editorStatusID - (optional) the primary ID of the status concept
	 * @throws RestException 
	 * @throws IOException
	 */
	private static RestWriteResponse createMappingItem(
			UUID sourceConcept,
			UUID mappingSetID,
			UUID targetConcept, 
			UUID qualifierID,
			List<RestDynamicSememeData> extendedDataFields,
			boolean active,
			StampCoordinate stampCoord,
			EditCoordinate editCoord) throws RuntimeException, RestException
	{
		int sememeConceptSequence = Get.identifierService().getConceptSequenceForUuids(mappingSetID);
		
		DynamicSememeData[] data = translateAndFixOffset(sememeConceptSequence, targetConcept, qualifierID, extendedDataFields == null ? new RestDynamicSememeData[] {} :
			extendedDataFields.toArray(new RestDynamicSememeData[extendedDataFields.size()]));
		
		SememeBuilder<? extends SememeChronology<?>> sb;
		sb = Get.sememeBuilderService().getDynamicSememeBuilder(
				Get.identifierService().getNidForUuids(sourceConcept),
				sememeConceptSequence, 
				data);
		
		UUID mappingItemUUID = UuidT5Generator.get(IsaacMappingConstants.get().MAPPING_NAMESPACE.getUUID(), 
				sourceConcept.toString() + "|" 
				+ mappingSetID.toString() + "|"
				+ ((targetConcept == null)? "" : targetConcept.toString()) + "|" 
				+ ((qualifierID == null)?   "" : qualifierID.toString()));
		
		if (Get.identifierService().hasUuid(mappingItemUUID))
		{
			throw new RestException("A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
		}
		
		sb.setPrimordialUuid(mappingItemUUID);
		
		if (!active)
		{
			sb.setState(State.INACTIVE);
		}

		@SuppressWarnings("rawtypes")
		SememeChronology built;
		try
		{
			built = sb.build(editCoord,ChangeCheckerMode.ACTIVE).getNoThrow();
		
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing creation of mapping item " + built.getPrimordialUuid() 
				+ " for mapping set " + mappingSetID).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (IllegalArgumentException e1)
		{
			//This happens when validators fail
			throw new RestException(e1.getMessage());
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new RestException("Failed committing new mapping item sememe", e);
		}
		
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), built.getPrimordialUuid());
	}
	
	private static RestWriteResponse updateMappingItem(
			SememeChronology<?> mappingItemSememe,
			UUID mappingItemTargetConcept,
			ConceptChronology<?> mappingItemQualifierConcept,
			List<RestDynamicSememeData> extendedDataFields,
			StampCoordinate stampCoord,
			EditCoordinate editCoord,
			State state) throws IOException
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)mappingItemSememe).getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));
		/* DynamicSememe<?> rdv = */ latest.get().value();
		
		if (latest.get().contradictions().isPresent() && latest.get().contradictions().get().size() > 0) {
			//TODO handle contradictions
			log.warn("Updating mapping item " + mappingItemSememe.getSememeSequence() + " with " + latest.get().contradictions().get().size() + " contradictions");
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingItemSememe).createMutableVersion(
				MutableDynamicSememe.class,
				state,
				editCoord);
		
		int sememeConceptSequence = mappingItemSememe.getAssemblageSequence();
		
		DynamicSememeData[] data = translateAndFixOffset(sememeConceptSequence, mappingItemTargetConcept, 
				mappingItemQualifierConcept != null ? mappingItemQualifierConcept.getPrimordialUuid() : null, 
				extendedDataFields == null ? new RestDynamicSememeData[] {} :extendedDataFields.toArray(new RestDynamicSememeData[extendedDataFields.size()]));

		mutable.setData(data);

		try
		{
			Get.commitService().addUncommitted(mappingItemSememe).get();
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of mapping item " + mappingItemSememe.getPrimordialUuid()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed during commit", e);
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), mappingItemSememe.getPrimordialUuid());
	}
	
	private static DynamicSememeData[] translateAndFixOffset(int sememeSequence, UUID targetConcept, UUID qualifierConcept, RestDynamicSememeData[] incomingExtendedDataFields) 
			throws RestException
	{
		Positions positions = Positions.getPositions(sememeSequence);
		
		//Make sure what they sent us is sorted.
		RestDynamicSememeData.sort(incomingExtendedDataFields);
		
		//Need to make a pass through, and insert the two fields where they belong (and update any other column numbers as appropriate
		List<RestDynamicSememeData> temp = new ArrayList<>(incomingExtendedDataFields.length + 2);
		int offset = 0;
		for (RestDynamicSememeData rdsd : incomingExtendedDataFields)
		{
			if (temp.size() == positions.qualfierPos)
			{
				temp.add(new RestDynamicSememeUUID(positions.qualfierPos, qualifierConcept));
				offset++;
			}
			if (temp.size() == positions.targetPos)
			{
				temp.add(new RestDynamicSememeUUID(positions.targetPos, targetConcept));
				offset++;
				//recheck
				if (temp.size() == positions.qualfierPos)
				{
					temp.add(new RestDynamicSememeUUID(positions.qualfierPos, qualifierConcept));
					offset++;
				}
			}
			
			temp.add(rdsd);
			if (rdsd != null && rdsd.columnNumber != null)
			{
				rdsd.columnNumber = rdsd.columnNumber + offset;
			}
		}
		//Need to check one more time, incase there were no incoming, or then ended up at the end of the list...
		if (temp.size() == positions.qualfierPos)
		{
			temp.add(new RestDynamicSememeUUID(positions.qualfierPos, qualifierConcept));
			offset++;
		}
		if (temp.size() == positions.targetPos)
		{
			temp.add(new RestDynamicSememeUUID(positions.targetPos, targetConcept));
			offset++;
			//recheck
			if (temp.size() == positions.qualfierPos)
			{
				temp.add(new RestDynamicSememeUUID(positions.qualfierPos, qualifierConcept));
				offset++;
			}
		}
		//Then we can translate, which sorts and dupe column number checks again, to make sure we didn't get it wrong...
		return RestDynamicSememeData.translate(temp.toArray(new RestDynamicSememeData[temp.size()]));
	}
}
