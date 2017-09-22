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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
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
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUtilityImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexerConfiguration;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.component.ComponentWriteAPIs;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayField;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayFieldCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValue;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValueCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValueUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionClone;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfoCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.api1.sememe.SememeWriteAPIs;
import gov.vha.isaac.rest.session.LatestVersionUtils;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link MappingWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.mappingAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.EDITOR})
public class MappingWriteAPIs
{
	private static Logger log = LogManager.getLogger(MappingWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

	public final static int ITEM_EXTENDED_FIELDS_OFFSET = 2;

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
		SecurityUtils.validateRole(securityContext, getClass());

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
					mappingSetCreationData.displayFields,
					mappingSetCreationData.active,
					RequestInfo.get().getEditCoordinate());
		}
		catch (RestException e)
		{
			throw e;
		}
		catch (IOException e) 
		{
			log.error("Unexpected error: ", e);
			throw new RestException("Failed creating mapping set name=" + mappingSetCreationData.name + ", inverse=" 
					+ mappingSetCreationData.inverseName + ", purpose=" + mappingSetCreationData.purpose + ", desc=" + mappingSetCreationData.description);
		}
	}
	
	/**
	 * This method creates a clone copy of an existing mapping set along with clones of its mapping set items
	 * along with modifications to the clone as specified in the passed RestMappingSetVersionClone data object
	 * 
	 * @param mappingSetCloneData - object containing identifier used to identify mapping set to clone and data to differentiate the clone
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the UUID identifying the created concept which defines the map set clone
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + RestPaths.clonePathComponent)
	public RestWriteResponse cloneMapSet(
		RestMappingSetVersionClone mappingSetCloneData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES, // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
				RequestParameters.editToken);

		RestMappingSetVersion cloneTargetMappingSetVersion = MappingAPIs.getMappingSet(mappingSetCloneData.cloneTargetConcept, null);

		List<RestDynamicSememeColumnInfoCreate> mapItemFieldsDefinitionCreateDTOs = new ArrayList<>();
		for (RestDynamicSememeColumnInfo mapItemFieldsDefinitionDTO : cloneTargetMappingSetVersion.mapItemFieldsDefinition) {
			RestDynamicSememeColumnInfoCreate createDTO = new RestDynamicSememeColumnInfoCreate(
					mapItemFieldsDefinitionDTO.columnLabelConcept.nid + "",
					mapItemFieldsDefinitionDTO.columnDataType,
					mapItemFieldsDefinitionDTO.columnDefaultData,
					mapItemFieldsDefinitionDTO.columnRequired,
					mapItemFieldsDefinitionDTO.columnValidatorTypes,
					mapItemFieldsDefinitionDTO.columnValidatorData);
			mapItemFieldsDefinitionCreateDTOs.add(createDTO);
		}
		
		List<RestMappingSetExtensionValueCreate> extensionCreateDTOs = new ArrayList<>();
		for (RestMappingSetExtensionValue extensionDTO : cloneTargetMappingSetVersion.mapSetExtendedFields) {
			RestMappingSetExtensionValueCreate extensionCreateDTO = new RestMappingSetExtensionValueCreate(extensionDTO.extensionNameConceptIdentifiers.sequence + "",
					extensionDTO.extensionValue, extensionDTO.active == null ? true : extensionDTO.active.booleanValue());
			extensionCreateDTOs.add(extensionCreateDTO);
		}
		
		int cloneTargetConceptId = RequestInfoUtils.getConceptSequenceFromParameter("mappingSetCloneData.cloneTargetConcept", mappingSetCloneData.cloneTargetConcept);
		List<RestMappingSetDisplayField> existingMapSetFields = MappingAPIs.getMappingSetDisplayFieldsFromMappingSet(Get.identifierService().getConceptNid(cloneTargetConceptId), RequestInfo.get().getStampCoordinate());
		List<RestMappingSetDisplayFieldCreate> mapSetFieldCreateDTOs = new ArrayList<>();
		for (RestMappingSetDisplayField existingField : existingMapSetFields) {
			mapSetFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(existingField.id, MapSetItemComponent.valueOf(existingField.componentType.enumName)));
		}
		ConceptChronology<? extends ConceptVersion<?>> mappingSetAssemblageConcept = null;
		try 
		{
			mappingSetAssemblageConcept = createMappingSetObjects(
					mappingSetCloneData.name,
					StringUtils.isBlank(mappingSetCloneData.inverseName) ? cloneTargetMappingSetVersion.inverseName : mappingSetCloneData.inverseName,
					StringUtils.isBlank(mappingSetCloneData.purpose) ? cloneTargetMappingSetVersion.purpose : mappingSetCloneData.purpose,
					StringUtils.isBlank(mappingSetCloneData.description) ? cloneTargetMappingSetVersion.description : mappingSetCloneData.description,
					mapItemFieldsDefinitionCreateDTOs,
					extensionCreateDTOs,
					mapSetFieldCreateDTOs,
					RequestInfo.get().getEditCoordinate());
		} catch (IOException e) {
			throw new RuntimeException("Failed building mapping set clone name=" + mappingSetCloneData.name + ", inverse=" 
						+ mappingSetCloneData.inverseName + ", purpose=" + mappingSetCloneData.purpose + ", desc=" + mappingSetCloneData.description, e);
		}

		/* List<SememeChronology<?>> builtItemClones = */ buildMappingItemClones(mappingSetCloneData.cloneTargetConcept, mappingSetAssemblageConcept.getConceptSequence());
		
		try {
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing mapping set clone name=" + mappingSetCloneData.name + ", inverse=" 
					+ mappingSetCloneData.inverseName + ", purpose=" + mappingSetCloneData.purpose + ", desc=" + mappingSetCloneData.description).get();

			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed committing mapping set clone name=" + mappingSetCloneData.name + ", inverse=" 
					+ mappingSetCloneData.inverseName + ", purpose=" + mappingSetCloneData.purpose + ", desc=" + mappingSetCloneData.description, e);
		}
		
		return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), 
				Get.identifierService().getUuidPrimordialFromConceptId(mappingSetAssemblageConcept.getConceptSequence()).get());
	}
	
	private static List<SememeChronology<?>> buildMappingItemClones(String mappingSetToCloneSememeId, int cloneMapSetConceptSequence) throws RestException {		
		int mappingSetToCloneSememeConceptSequence = Util.convertToConceptSequence(mappingSetToCloneSememeId);

		List<SememeChronology<?>> itemsToCommit = new ArrayList<>();

		Get.sememeService().getSememesFromAssemblage(mappingSetToCloneSememeConceptSequence).forEach(mappingItemToCloneSememeChronology -> 
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)mappingItemToCloneSememeChronology).getLatestVersion(DynamicSememe.class, 
					RequestInfo.get().getStampCoordinate());

			if (latest.isPresent())
			{
				//TODO handle contradictions

				DynamicSememe<?> mappingSetItemToCloneSememeVersion = (DynamicSememe<?>)latest.get().value();

				Optional<UUID> mappingSetUuid = Get.identifierService().getUuidPrimordialFromConceptId(cloneMapSetConceptSequence);

				SememeBuilder<? extends SememeChronology<?>> sb;
				sb = Get.sememeBuilderService().getDynamicSememeBuilder(
						mappingSetItemToCloneSememeVersion.getReferencedComponentNid(), // source
						cloneMapSetConceptSequence, // assemblage
						mappingSetItemToCloneSememeVersion.getData());
				sb.setState(mappingSetItemToCloneSememeVersion.getState());

				UUID mappingItemUUID = UuidT5Generator.get(
						IsaacMappingConstants.get().MAPPING_NAMESPACE.getUUID(), 
						Get.identifierService().getUuidPrimordialFromConceptId(mappingSetItemToCloneSememeVersion.getReferencedComponentNid()) + "|"
								+ mappingSetUuid.get() + "|"
								+ DynamicSememeUtilityImpl.toString(mappingSetItemToCloneSememeVersion.getData()));
				sb.setPrimordialUuid(mappingItemUUID);

				@SuppressWarnings("rawtypes")
				SememeChronology built;
				try
				{
					built = sb.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();

					itemsToCommit.add((SememeChronology<?>)built);
				}
				catch (IllegalArgumentException e1)
				{
					//This happens when validators fail
					throw new RuntimeException("Failed validating mapping item clone sememe: " + e1.getMessage());
				}
				catch (Exception e)
				{
					log.error("Unexpected", e);
					throw new RuntimeException("Failed committing new mapping item clone sememe", e);
				}
			}
		});
		
		log.debug("Built " + itemsToCommit.size() + " mapping set items for mapping set " + Get.identifierService().getUuidPrimordialFromConceptId(cloneMapSetConceptSequence) + " clone of " + Get.identifierService().getUuidPrimordialFromConceptId(mappingSetToCloneSememeConceptSequence));
	
		return itemsToCommit;
	}

	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param mappingSetUpdateData - object containing data used to update existing mapping set.
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
		RestMappingSetVersionBaseUpdate mappingSetUpdateData,
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES, // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
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
				RequestInfo.get().getEditCoordinate(),
				stateToUse,
				mappingSetUpdateData.mapSetExtendedFields,
				mappingSetUpdateData.displayFields);
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
		SecurityUtils.validateRole(securityContext, getClass());

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
		if (StringUtils.isNotBlank(mappingItemCreationData.targetConcept) && !targetConcept.isPresent())
		{
			throw new RestException("targetConcept", mappingItemCreationData.targetConcept + "", "Unable to locate the target concept");
		}
		if (StringUtils.isNotBlank(mappingItemCreationData.qualifierConcept) && !qualifierID.isPresent())
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
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES, // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
				RequestParameters.editToken);

		State stateToUse = (mappingItemUpdateData.active == null || mappingItemUpdateData.active) ? State.ACTIVE : State.INACTIVE;

		SememeChronology<?> mappingItemSememeChronology = SememeAPIs.findSememeChronology(id);
		
		Optional<UUID> targetConcept = readConcept(mappingItemUpdateData.targetConcept, "mappingItemUpdateData.targetConcept");
		
		if (StringUtils.isNotBlank(mappingItemUpdateData.targetConcept) && !targetConcept.isPresent())
		{
			throw new RestException("targetConcept", mappingItemUpdateData.targetConcept + "", "Unable to locate the target concept");
		}

		Optional<UUID> qualifierConcept = readConcept(mappingItemUpdateData.qualifierConcept, "mappingItemUpdateData.qualifierConcept");
		if (StringUtils.isNotBlank(mappingItemUpdateData.qualifierConcept) && !qualifierConcept.isPresent())
		{
			throw new RestException("qualifierConcept", mappingItemUpdateData.qualifierConcept + "", "Unable to locate the qualifier concept");
		}
		
		try {
			return updateMappingItem(
					mappingItemSememeChronology,
					targetConcept.orElse(null),
					qualifierConcept.isPresent() ? Get.conceptService().getConcept(qualifierConcept.get()) : null,
					mappingItemUpdateData.mapItemExtendedFields,
					RequestInfo.get().getEditCoordinate(),
					stateToUse);

		} catch (IOException e) {
			throw new RuntimeException("Failed updating mapping item " + id, e);
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
	
	@SuppressWarnings({ "rawtypes" })
	private static SememeChronology buildMappingSetExtensionValue(
			int mapSetConceptNid,
			String conceptSequenceParamName,
			String extensionNameConcept,
			RestDynamicSememeData extensionValue,
			EditCoordinate editCoord) throws RestException {
		SememeChronology extension = null;
		if (extensionValue instanceof RestDynamicSememeString)
		{
			//TODO this isn't honoring active / inactive of the extensions.
			extension = Get.sememeBuilderService().getDynamicSememeBuilder(mapSetConceptNid,
					IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getSequence(), 
					new DynamicSememeData[] {
							new DynamicSememeNidImpl(
									Get.identifierService().getConceptNid(
											RequestInfoUtils.getConceptSequenceFromParameter(conceptSequenceParamName, 
													extensionNameConcept))),
							new DynamicSememeStringImpl(((RestDynamicSememeString)extensionValue).getString())}).build(
					editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		else if (extensionValue instanceof RestDynamicSememeNid || extensionValue instanceof RestDynamicSememeUUID)
		{
			extension = Get.sememeBuilderService().getDynamicSememeBuilder(mapSetConceptNid,
					IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_NID_EXTENSION.getSequence(), 
					new DynamicSememeData[] {
							new DynamicSememeNidImpl(Get.identifierService().getConceptNid(
									RequestInfoUtils.getConceptSequenceFromParameter(conceptSequenceParamName, 
											extensionNameConcept))),
							new DynamicSememeNidImpl(
									(extensionValue instanceof RestDynamicSememeNid ? 
										((RestDynamicSememeNid)extensionValue).getNid() :
										Get.identifierService().getNidForUuids(((RestDynamicSememeUUID)extensionValue).getUUID())))
											}).build(editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		else
		{
			throw new RuntimeException("Unsupported map set extension field type");
		}
		
		return extension;
	}

	private static DynamicSememeStringImpl getDynamicSememeStringFromMapSetField(RestMappingSetDisplayFieldCreate passedField) throws RestException {
		String dataString = null;
		
		MapSetItemComponent msit = MapSetItemComponent.parse(passedField.fieldComponentType).orElseThrow(() ->
			new RestException("RestMappingSetFieldCreate.fieldComponentType", passedField.fieldComponentType, "unsupported map set display field component type \"" + passedField.fieldComponentType + "\". Must be one of " 
				+ Arrays.toString(MapSetItemComponent.values())));
		
		if (msit == MapSetItemComponent.ITEM_EXTENDED) {
			// item extended fields
			// validates for type and sign but not correspondence to existing item extended field
			try {
				Integer colNum = Integer.parseUnsignedInt(passedField.id.trim());
				dataString = colNum + ":" + msit.name();
			} catch (NumberFormatException e) {
				throw new RestException("RestMappingSetFieldCreate.id", passedField.id, "Invalid or unsupported map set field id " + passedField.id 
						+ " for component type " + msit.name() + ". Must be a non-negative integer and should correspond to an existing item extended field");
			}
		} else {
			MapSetDisplayFieldsService service = LookupService.getService(MapSetDisplayFieldsService.class);
			IdentifiedObject existingField = service.getFieldConceptIdentifierByFieldConceptId(passedField.id);
			if (existingField == null) {
				throw new RestException("RestMappingSetFieldCreate.id", passedField.id, "Invalid or unsupported map set field id " + passedField.id 
						+ " for component type " + msit.name() + ". Must be one of " + service.getAllGlobalFieldIds());
			}
			dataString = existingField.getPrimordialUuid().toString() + ":" + msit.name();
		}

		return new DynamicSememeStringImpl(dataString);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static DynamicSememeArrayImpl getDynamicSememeArrayImplFromMapSetFields(List<RestMappingSetDisplayFieldCreate> passedFields) throws RestException {
		List<DynamicSememeStringImpl> fieldSpecificationStrings = new ArrayList<>();
		
		if (passedFields != null) {
			for (RestMappingSetDisplayFieldCreate passedMapSetField : passedFields) {
				DynamicSememeStringImpl dynamicSememeString = getDynamicSememeStringFromMapSetField(passedMapSetField);
				fieldSpecificationStrings.add(dynamicSememeString);
			}
		}
		
		DynamicSememeArrayImpl dynamicSememeArray = null;
		if (fieldSpecificationStrings.size() > 0) {
			dynamicSememeArray = new DynamicSememeArrayImpl(fieldSpecificationStrings.toArray(new DynamicSememeStringImpl[fieldSpecificationStrings.size()]));
		} else {
			dynamicSememeArray = new DynamicSememeArrayImpl(new DynamicSememeStringImpl[0]); // TODO Joel determine if this is ever ok
		}
		
		return dynamicSememeArray;
	}
	@SuppressWarnings({ "rawtypes" })
	private static SememeChronology buildNewMapSetFieldsSememe(
			int mapSetConceptNid,
			List<RestMappingSetDisplayFieldCreate> mapSetFields,
			EditCoordinate editCoord) throws RestException {
		SememeChronology newMapSetFieldsSememe = null;
		if (mapSetFields != null && mapSetFields.size() > 0) {
			DynamicSememeArrayImpl fields = getDynamicSememeArrayImplFromMapSetFields(mapSetFields);
			newMapSetFieldsSememe = Get.sememeBuilderService().getDynamicSememeBuilder(
					mapSetConceptNid,
					IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_DISPLAY_FIELDS.getSequence(), 
					new DynamicSememeData[] {
							fields
					}).build(
							editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		return newMapSetFieldsSememe;
	}

	@SuppressWarnings({ "rawtypes" })
	private static Optional<SememeChronology> updateMapSetFieldsSememe(
			int mapSetConceptNid,
			List<RestMappingSetDisplayFieldCreate> mapSetFields,
			EditCoordinate editCoord) throws RestException {
		Optional<SememeChronology<? extends SememeVersion<?>>> mapSetFieldsSememe = Frills.getAnnotationSememe(mapSetConceptNid, IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_DISPLAY_FIELDS.getSequence());
		if (! mapSetFieldsSememe.isPresent()) {
			return Optional.of(buildNewMapSetFieldsSememe(mapSetConceptNid, mapSetFields, editCoord));
		} else {
			if (mapSetFields == null || mapSetFields.size() == 0) {
				// If no field passed in update then retire the sememe
				Optional<ObjectChronology> sc = (Optional<ObjectChronology>)ComponentWriteAPIs
						.resetStateWithNoCommit(State.INACTIVE, mapSetFieldsSememe.get().getPrimordialUuid() + "");
				return sc.isPresent() ? Optional.of((SememeChronology)sc.get()) : Optional.empty();  //cast nonesense
			} else {
				DynamicSememeData[] updatedData = new DynamicSememeData[1];
				updatedData[0] = getDynamicSememeArrayImplFromMapSetFields(mapSetFields);

				@SuppressWarnings("unchecked")
				Optional<DynamicSememeImpl> existingVersionOptionalLatest = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)mapSetFieldsSememe.get(), DynamicSememeImpl.class);
				if (! existingVersionOptionalLatest.isPresent()) {
					throw new RuntimeException("No latest version of mapSetFieldsSememe " + mapSetFieldsSememe.get().getNid() + " found for specified edit coordinate " + RequestInfo.get().getEditCoordinate() + " or stamp coordinate " + RequestInfo.get().getStampCoordinate());
				}
				DynamicSememeData[] existingData = existingVersionOptionalLatest.get().getData();

				DynamicSememeArrayImpl updatedArray = (DynamicSememeArrayImpl)updatedData[0];
				DynamicSememeArrayImpl existingArray = (DynamicSememeArrayImpl)existingData[0];
				if (existingArray != null && Arrays.equals(updatedArray.getData(), existingArray.getData())) {
					return null; // No need to update
				}

				@SuppressWarnings("unchecked")
				DynamicSememeImpl mutableVersion = (DynamicSememeImpl)((SememeChronology)mapSetFieldsSememe.get()).createMutableVersion(DynamicSememeImpl.class, State.ACTIVE, editCoord);
				mutableVersion.setData(updatedData);

				return Optional.of(mutableVersion.getChronology());
			}
		}
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
			List<RestDynamicSememeColumnInfoCreate> itemExtendedFieldDefinitions,
			List<RestMappingSetExtensionValueCreate> mapSetExtendedFields,
			List<RestMappingSetDisplayFieldCreate> itemDisplayFields,
			Boolean active,
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
		
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[ITEM_EXTENDED_FIELDS_OFFSET + (itemExtendedFieldDefinitions == null ? 0 : itemExtendedFieldDefinitions.size())];
		columns[0] = new DynamicSememeColumnInfo(0, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
				DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.COMPONENT_TYPE, 
				new DynamicSememeArrayImpl<>(new DynamicSememeString[] {new DynamicSememeStringImpl(ObjectChronologyType.CONCEPT.name())}), 
				true);
		columns[1] = new DynamicSememeColumnInfo(1, IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE.getUUID(), DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getUUID()), true);
		Map<RestDynamicSememeColumnInfoCreate, Integer> calculatedColumnByPassedItemExtendedFieldDefinition = new HashMap<>();

		if (itemExtendedFieldDefinitions != null)
		{
			int i = ITEM_EXTENDED_FIELDS_OFFSET;
			for (RestDynamicSememeColumnInfoCreate colInfo : itemExtendedFieldDefinitions)
			{
				calculatedColumnByPassedItemExtendedFieldDefinition.put(colInfo, i - ITEM_EXTENDED_FIELDS_OFFSET);
				columns[i] = new DynamicSememeColumnInfo(i++, 
						Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfoUtils.getConceptSequenceFromParameter("RestMappingSetVersionBaseCreate.mapItemExtendedFieldsDefinition.columnLabelConcept", 
										colInfo.columnLabelConcept)).get(), 
						DynamicSememeDataType.parse(colInfo.columnDataType, true), RestDynamicSememeData.translate(colInfo.columnDefaultData), colInfo.columnRequired, 
						DynamicSememeValidatorType.parse(colInfo.columnValidatorTypes, true), RestDynamicSememeData.translate(colInfo.columnValidatorData), true);
			}
		}
		
		DynamicSememeUsageDescription tempDsud = null;
		try {
			tempDsud = Frills.createNewDynamicSememeUsageDescriptionConcept(
				mappingName, mappingName, description, columns,
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getConceptSequence(), ObjectChronologyType.CONCEPT, null, editCoord);
		} catch (Exception e) {
			String msg = "Failed creating new DynamicSememeUsageDescription for map set " + mappingName;
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}

		final DynamicSememeUsageDescription rdud = tempDsud;
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
		
		@SuppressWarnings({ "rawtypes", "unused" })
		SememeChronology mappingAnnotation = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence(), 
				new DynamicSememeData[] {
						(StringUtils.isBlank(purpose) ? null : new DynamicSememeStringImpl(purpose))}).build(
				editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();

		if (mapSetExtendedFields != null)
		{
			for (RestMappingSetExtensionValueCreate field : mapSetExtendedFields)
			{
				if (field != null && field.extensionValue != null && field.extensionValue.data != null && !StringUtils.isBlank(field.extensionValue.data.toString()))
				{
					buildMappingSetExtensionValue(
						Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
						"RestMappingSetVersionBaseCreate.mapSetExtendedFields.extensionNameConcept",
						field.extensionNameConcept,
						field.extensionValue,
						editCoord);
				}
			}
		}
		
		// If no display fields passed, then add default display fields
		// as SOURCE, TARGET and EQUIVALENCE_TYPE DESCRIPTION followed by item extended fields, if any
		if (itemDisplayFields == null || itemDisplayFields.size() == 0) {
						
			itemDisplayFields = new ArrayList<>();
			itemDisplayFields.add(new RestMappingSetDisplayFieldCreate(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString(), 
					MapSetItemComponent.SOURCE));
			itemDisplayFields.add(new RestMappingSetDisplayFieldCreate(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString(), 
					MapSetItemComponent.TARGET));
			itemDisplayFields.add(new RestMappingSetDisplayFieldCreate(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString(), 
					MapSetItemComponent.EQUIVALENCE_TYPE));
	
			for (RestDynamicSememeColumnInfoCreate itemExtendedFieldCol : itemExtendedFieldDefinitions) {
				itemDisplayFields.add(new RestMappingSetDisplayFieldCreate(calculatedColumnByPassedItemExtendedFieldDefinition.get(itemExtendedFieldCol) + "", 
						MapSetItemComponent.ITEM_EXTENDED));
			}
		}
			
		buildNewMapSetFieldsSememe(
					Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
					itemDisplayFields,
					editCoord);

		try
		{
			// TODO do we need to perform addUncommitted on the objects first?
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing create of mapping set " + rdud.getDynamicSememeName()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
			if (active != null && !active.booleanValue())
			{
				//Wanted it created inactive for some reason...
				ComponentWriteAPIs.resetState(State.INACTIVE, Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()) + "");
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed during commit", e);
		}
		return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), 
				Get.identifierService().getUuidPrimordialFromConceptId(rdud.getDynamicSememeUsageDescriptorSequence()).get());
	}

	private static RestDynamicSememeData getData(DynamicSememe<?> ds) {
		DynamicSememeData value = null;
		if (ds.getData().length > 1)
		{
			value = ds.getData(1);
		}
		return RestDynamicSememeData.translate(1, value);
	}
	@SuppressWarnings("unchecked")
	private static RestWriteResponse updateMappingSet(
			ConceptChronology<?> mappingConcept,
			String mapName,
			String mapInverseName,
			String mapDescription,
			String mapPurpose,
			EditCoordinate editCoord,
			State state,
			List<RestMappingSetExtensionValueUpdate> mapSetExtendedFields,
			List<RestMappingSetDisplayFieldCreate> mapSetFields) throws RuntimeException, RestException 
	{		
		final List<SememeChronology<? extends SememeVersion<?>>> objectsToAdd = new ArrayList<>();
		Get.sememeService().getSememesForComponent(mappingConcept.getNid()).filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(descriptionC ->
			{
				try
				{
					Optional<DescriptionSememeImpl> latest = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DescriptionSememeImpl>)descriptionC, DescriptionSememeImpl.class);
				
					if (latest.isPresent())
					{
						DescriptionSememe<?> ds = latest.get();
						if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
						{
							if (Frills.isDescriptionPreferred(ds.getNid(), null))
							{
								if (!ds.getText().equals(mapName))
								{
									@SuppressWarnings({ "rawtypes" })
									MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
											.createMutableVersion(MutableDescriptionSememe.class, State.ACTIVE, editCoord);
									mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
									mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
									mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
									mutable.setText(mapName);
									objectsToAdd.add(ds.getChronology());
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
										@SuppressWarnings({ "rawtypes" })
										MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
												.createMutableVersion(MutableDescriptionSememe.class, (StringUtils.isBlank(mapInverseName) ? State.INACTIVE : State.ACTIVE), editCoord);
										mutable.setText(StringUtils.isBlank(mapInverseName) ? ds.getText() : mapInverseName);
										mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
										mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
										mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
										
										objectsToAdd.add(ds.getChronology());
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
									@SuppressWarnings({ "rawtypes" })
									MutableDescriptionSememe mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
											.createMutableVersion(MutableDescriptionSememe.class, State.ACTIVE, editCoord);
									mutable.setCaseSignificanceConceptSequence(ds.getCaseSignificanceConceptSequence());
									mutable.setDescriptionTypeConceptSequence(ds.getDescriptionTypeConceptSequence());
									mutable.setLanguageConceptSequence(ds.getLanguageConceptSequence());
									mutable.setText(mapDescription);
									objectsToAdd.add(ds.getChronology());
								}
							}
						}
					} else {
						log.error("Cannot update Description Sememe {} because no latest version found", descriptionC.getPrimordialUuid());
						throw new RuntimeException("Unable to retrieve current version for update of description sememe " + descriptionC.getPrimordialUuid());
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
			throw new RuntimeException("internal error (failed to find mapping refex)");
		}
		Optional<DynamicSememeImpl> latestVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)mappingSememe.get(), DynamicSememeImpl.class);
		try
		{			
			String currentMapPurposeValue = ((! latestVersion.isPresent() || latestVersion.get().getData().length == 0 || latestVersion.get().getData()[0] == null) ? "" : latestVersion.get().getData()[0].dataToString());

			if (! latestVersion.isPresent() || ! currentMapPurposeValue.equals(mapPurpose))
			{
				if (! latestVersion.isPresent()) {
					log.warn("Latest version not found of mapping Sememe {}. Updating unconditionally.", mappingSememe.get().getPrimordialUuid());
				}
				@SuppressWarnings({ "rawtypes" })
				DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingSememe.get()).createMutableVersion(
						MutableDynamicSememe.class,
						State.ACTIVE,
						editCoord);
	
				mutable.setData(new DynamicSememeData[] {
						(StringUtils.isBlank(mapPurpose) ? null : new DynamicSememeStringImpl(mapPurpose))});
				objectsToAdd.add(mappingSememe.get());
			}

			if (mapSetExtendedFields != null && mapSetExtendedFields.size() > 0) {
				// get existing extended fields
				Map<Integer, DynamicSememe<?>> existingExtensionValues = new HashMap<>();
				for (SememeChronology<?> stringExtensionSememe : Get.sememeService().getSememesForComponentFromAssemblage(
						mappingConcept.getNid(), 
						IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION.getConceptSequence()).collect(Collectors.toList()))
				{
					@SuppressWarnings("rawtypes")
					SememeChronology rawSememeChronology = (SememeChronology)stringExtensionSememe;
					// Get only active values
					Optional<DynamicSememeImpl> latest = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)rawSememeChronology, DynamicSememeImpl.class);
					if (latest.isPresent())
					{
						DynamicSememe<?> ds = latest.get();
						int nameNid = ((DynamicSememeNid)ds.getData(0)).getDataNid();
						existingExtensionValues.put(nameNid, ds);
					}
				}

				for (RestMappingSetExtensionValueUpdate update : mapSetExtendedFields) {
					int passedNid = RequestInfoUtils.getNidFromUuidOrNidParameter("RestMappingSetExtensionValueUpdate.extensionNameConcept", update.extensionNameConcept);
					DynamicSememe<?> existingDynamicSememe = existingExtensionValues.get(passedNid);
					
					@SuppressWarnings("rawtypes")
					SememeChronology sememeToCommit = null;
					if (existingDynamicSememe == null) {
						if (update.active != null && ! update.active) {
							// FAIL on a completely new extension value that is specified as INACTIVE (active == false)
							throw new RestException("RestMappingSetExtensionValueUpdate.active", update.active + "", "cannot create new extension value with inactive state (active == false)");
						}
						// This is an entirely new extension value
						
						if (update != null && update.extensionValue != null && update.extensionValue.data != null && !StringUtils.isBlank(update.extensionValue.data.toString()))
						{
							sememeToCommit = buildMappingSetExtensionValue(
								mappingConcept.getNid(),
								"RestMappingSetVersionBaseUpdate.mapSetExtendedFields.extensionNameConcept",
								update.extensionNameConcept,
								update.extensionValue,
								editCoord);
						}
					} else {
						// This corresponds to an existing extension value
						if (update.active != null && ! update.active) {
							// Deactivate/retire this extension value
							Optional<ObjectChronology> oc = ComponentWriteAPIs.resetStateWithNoCommit(State.INACTIVE, existingDynamicSememe.getNid() + ""); 
							sememeToCommit = oc.isPresent() ? (SememeChronology)oc.get() : null;
						} else {
							RestDynamicSememeData existingData = getData(existingDynamicSememe);
							RestDynamicSememeData updatedData = update.extensionValue;

							if (! (updatedData.getClass().isInstance(existingData))) {
								// Validate RestDynamicSememeData type as consistent with existing value
								throw new RestException("RestMappingSetExtensionValueUpdate.extensionValue", updatedData.data + "", "attempting to update extension " + existingData.getClass().getSimpleName() + " value dynamic sememe with data of differing type " + updatedData.getClass().getSimpleName());
							}
							
							DynamicSememeData[] newDataArray = new DynamicSememeData[2];
							newDataArray[0] = new DynamicSememeNidImpl(passedNid);
							if (updatedData instanceof RestDynamicSememeString) {
								newDataArray[1] = new DynamicSememeStringImpl(((RestDynamicSememeString)updatedData).getString());
							} else if (updatedData instanceof RestDynamicSememeNid) {
								newDataArray[1] = new DynamicSememeNidImpl(((RestDynamicSememeNid)updatedData).getNid());
							}
							else if (updatedData instanceof RestDynamicSememeUUID) {
								newDataArray[1] = new DynamicSememeUUIDImpl(((RestDynamicSememeUUID)updatedData).getUUID());
							}else {
								// Only support RestDynamicSememeString or RestDynamicSememeNid
								throw new RuntimeException(updatedData.getClass().getName() + " NOT SUPPORTED");
							}
							
							@SuppressWarnings({ "rawtypes" })
							DynamicSememeImpl sememeToUpdate = (DynamicSememeImpl)((SememeChronology)existingDynamicSememe.getChronology()).createMutableVersion(DynamicSememeImpl.class, State.ACTIVE, editCoord);
							sememeToUpdate.setData(newDataArray);
							sememeToCommit = sememeToUpdate.getChronology();
						}
						
						if (sememeToCommit != null) {
							SememeChronology<? extends SememeVersion<?>> sc = sememeToCommit;
							objectsToAdd.add(sc);
						}
					}
				}
			}

			//Look up the current state of the mapset concept - if it differs, update the concept.
			Optional<ConceptVersionImpl> concept = LatestVersionUtils.getLatestVersionForUpdate((ConceptChronology<ConceptVersionImpl>)mappingConcept, ConceptVersionImpl.class);
			
			boolean updatedConcept = false;
			if (!concept.isPresent() || concept.get().getState() != state) 
			{
				mappingConcept.createMutableVersion(state, editCoord);
				Get.commitService().addUncommitted(mappingConcept).get();
				updatedConcept = true;
			}

			@SuppressWarnings("rawtypes")
			Optional<SememeChronology> updatedMapSetFieldSememe = updateMapSetFieldsSememe(mappingConcept.getNid(), mapSetFields, editCoord);
			if (updatedMapSetFieldSememe.isPresent()) {
				objectsToAdd.add(updatedMapSetFieldSememe.get());
			}
			
			if (updatedConcept || objectsToAdd.size() > 0) {
				//Delay all of the addUncommited, so if we fail out somewhere else, we don't end up partially added / committed.
				for (SememeChronology<? extends SememeVersion<?>> x : objectsToAdd)
				{
					Get.commitService().addUncommitted(x).get();
				}
				Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of mapping set " + mappingConcept.getPrimordialUuid()).get();
				if (RequestInfo.get().getActiveWorkflowProcessId() != null)
				{
					LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
				}
				
				return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), mappingConcept.getPrimordialUuid());
			} else {
				return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), mappingConcept.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
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
	}

	private static ConceptChronology<? extends ConceptVersion<?>> createMappingSetObjects(
			String mappingName,
			String inverseName,
			String purpose,
			String description,
			List<RestDynamicSememeColumnInfoCreate> extendedFields,
			List<RestMappingSetExtensionValueCreate> mapSetExtendedFields,
			List<RestMappingSetDisplayFieldCreate> mapSetFields,
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
		
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[ITEM_EXTENDED_FIELDS_OFFSET + (extendedFields == null ? 0 : extendedFields.size())];
		columns[0] = new DynamicSememeColumnInfo(0, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
				DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.COMPONENT_TYPE, 
				new DynamicSememeArrayImpl<>(new DynamicSememeString[] {new DynamicSememeStringImpl(ObjectChronologyType.CONCEPT.name())}), 
				true);
		columns[1] = new DynamicSememeColumnInfo(1, IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE.getUUID(), DynamicSememeDataType.UUID, null, false, 
				DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_EQUIVALENCE_TYPES.getUUID()), true);
		if (extendedFields != null)
		{
			int i = ITEM_EXTENDED_FIELDS_OFFSET;
			for (RestDynamicSememeColumnInfoCreate colInfo : extendedFields)
			{
				columns[i] = new DynamicSememeColumnInfo(i++, 
						Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfoUtils.getConceptSequenceFromParameter("RestMappingSetVersionBaseCreate.mapSetExtendedFields.columnLabelConcept", 
										colInfo.columnLabelConcept)).get(), 
						DynamicSememeDataType.parse(colInfo.columnDataType, true), RestDynamicSememeData.translate(colInfo.columnDefaultData), colInfo.columnRequired, 
						DynamicSememeValidatorType.parse(colInfo.columnValidatorTypes, true), RestDynamicSememeData.translate(colInfo.columnValidatorData), true);
			}
		}
		
		ConceptChronology<? extends ConceptVersion<?>> rdudAssemblageConcept = Frills.buildUncommittedNewDynamicSememeUsageDescription(
				mappingName, mappingName, description, columns,
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getConceptSequence(), ObjectChronologyType.CONCEPT, null, editCoord);
		
		DynamicSememeUsageDescription rdud = new DynamicSememeUsageDescriptionImpl(rdudAssemblageConcept.getNid());
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				// TODO 2 Dan (index config)  see if I still need to manually do this, I thought I fixed this.
				// TODO this builds but does not commit
				/* SememeChronology<? extends DynamicSememe<?>> config = */ SememeIndexerConfiguration.buildAndConfigureColumnsToIndex(rdudAssemblageConcept.getNid(), new Integer[] {0, 1, 2}, true);
			}
			catch (Exception e)
			{
				log.error("Unexpected error enabling the index on newly created mapping set!", e);
			}
		});
		
		//Then, annotate the concept created above as a member of the MappingSet dynamic sememe, and add the inverse name, if present.
		if (!StringUtils.isBlank(inverseName))
		{
			ObjectChronology<?> builtDesc = LookupService.get().getService(DescriptionBuilderService.class).getDescriptionBuilder(inverseName, rdudAssemblageConcept.getConceptSequence(), 
					MetaData.SYNONYM, MetaData.ENGLISH_LANGUAGE).addAcceptableInDialectAssemblage(MetaData.US_ENGLISH_DIALECT).build(editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
			
			Get.sememeBuilderService().getDynamicSememeBuilder(builtDesc.getNid(),DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).build(
					editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		
		@SuppressWarnings({ "rawtypes", "unused" })
		SememeChronology mappingAnnotation = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdudAssemblageConcept.getConceptSequence()),
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence(), 
				new DynamicSememeData[] {
						(StringUtils.isBlank(purpose) ? null : new DynamicSememeStringImpl(purpose))}).build(
				editCoord, ChangeCheckerMode.ACTIVE).getNoThrow();

		if (mapSetExtendedFields != null)
		{
			//TODO this isn't honoring the active / inactive of the create info
			for (RestMappingSetExtensionValueCreate field : mapSetExtendedFields)
			{
				
				if (field != null && field.extensionValue != null && field.extensionValue.data != null && !StringUtils.isBlank(field.extensionValue.data.toString()))
				{
					buildMappingSetExtensionValue(
						rdudAssemblageConcept.getNid(),
						"RestMappingSetVersionBaseCreate.mapSetExtendedFields.extensionNameConcept",
						field.extensionNameConcept,
						field.extensionValue,
						editCoord);
				}
			}
		}

		if (mapSetFields.size() > 0) {
			buildNewMapSetFieldsSememe(
					Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
					mapSetFields,
					editCoord);
		}

		return rdudAssemblageConcept;
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
			EditCoordinate editCoord) throws RestException
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
			throw new RuntimeException("Failed committing new mapping item sememe", e);
		}
		
		return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), built.getPrimordialUuid());
	}
	
	private static RestWriteResponse updateMappingItem(
			SememeChronology<?> mappingItemSememe,
			UUID mappingItemTargetConcept,
			ConceptChronology<?> mappingItemQualifierConcept,
			List<RestDynamicSememeData> extendedDataFields,
			EditCoordinate editCoord,
			State state) throws IOException
	{
		int sememeConceptSequence = mappingItemSememe.getAssemblageSequence();
		DynamicSememeData[] newData = translateAndFixOffset(sememeConceptSequence, mappingItemTargetConcept, 
				mappingItemQualifierConcept != null ? mappingItemQualifierConcept.getPrimordialUuid() : null, 
				extendedDataFields == null ? new RestDynamicSememeData[] {} :extendedDataFields.toArray(new RestDynamicSememeData[extendedDataFields.size()]));

		try {
			@SuppressWarnings({ "unchecked" })
			Optional<DynamicSememeImpl> latest = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)mappingItemSememe, DynamicSememeImpl.class);

			if (latest.isPresent()) {
				DynamicSememe<?> currentSememeVersion = latest.get();

				DynamicSememeData[] currentData = currentSememeVersion.getData();

				if (currentSememeVersion.getState() == state
						&& SememeWriteAPIs.equals(currentData, newData)) {
					return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), mappingItemSememe.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
				}
			} else {
				log.warn("Latest version not found of mapping item dynamic sememe {}. Updating unconditionally.", mappingItemSememe.getPrimordialUuid());
			}
		} catch (Exception e) {
			log.warn("Failed checking update against current object " + mappingItemSememe.getPrimordialUuid() + " state. Unconditionally performing update", e);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingItemSememe).createMutableVersion(
				MutableDynamicSememe.class,
				state,
				editCoord);
		mutable.setData(newData);

		try
		{
			Get.commitService().addUncommitted(mappingItemSememe).get();
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of mapping item " + mappingItemSememe.getPrimordialUuid()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
			return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), mappingItemSememe.getPrimordialUuid());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed during commit", e);
		}
	}
	
	private static DynamicSememeData[] translateAndFixOffset(int sememeSequence, UUID targetConcept, UUID qualifierConcept, RestDynamicSememeData[] incomingExtendedDataFields) 
			throws RestException
	{
		Positions positions = Positions.getPositions(sememeSequence);
		
		//Make sure what they sent us is sorted.
		RestDynamicSememeData.sort(incomingExtendedDataFields);
		
		//Need to make a pass through, and insert the two fields where they belong (and update any other column numbers as appropriate
		List<RestDynamicSememeData> temp = new ArrayList<>(incomingExtendedDataFields.length + ITEM_EXTENDED_FIELDS_OFFSET);
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
		return RestDynamicSememeData.translate(temp.toArray(new RestDynamicSememeData[temp.size()]), true);
	}
}
