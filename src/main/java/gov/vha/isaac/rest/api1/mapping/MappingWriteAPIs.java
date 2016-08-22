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
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshot;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
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
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexerConfiguration;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


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
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @return the sequence identifying the created concept which defines the map set
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + RestPaths.createPathComponent)
	public RestInteger createNewMapSet(
		RestMappingSetVersionBaseCreate mappingSetCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		// TODO test createNewMapSet()
		// TODO implement createNewMapSet() handling of extended fields
		RestMappingSetVersion newMappingSet = null;
		try {
			newMappingSet = createMappingSet(
					mappingSetCreationData.name,
					mappingSetCreationData.inverseName,
					mappingSetCreationData.purpose,
					mappingSetCreationData.description,
					/* (UUID editorStatus) */ null,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate());
			
			return new RestInteger(Get.identifierService().getConceptSequenceForUuids(newMappingSet.identifiers.uuids));
		} catch (IOException e) {
			throw new RestException("Failed creating mapping set name=" + mappingSetCreationData.name + ", inverse=" + mappingSetCreationData.inverseName + ", purpose=" + mappingSetCreationData.purpose + ", desc=" + mappingSetCreationData.description);
		}
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param mappingSetUpdateData - object containing data used to update existing mapping set
	 * @param id - id of mapping set concept to update
	 * @param state - The state to put the mapping set into.  Valid values are "INACTIVE"/"Inactive"/"I" or "ACTIVE"/"Active"/"A"
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@PUT
	@Path(RestPaths.mappingSetComponent + RestPaths.updatePathComponent)
	public void updateMapSet(
		RestMappingSetVersionBase mappingSetUpdateData,
		@QueryParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.state) String state,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		// TODO test updateMapSet()
		// TODO implement updateMapSet() handling of extended fields
		State stateToUse = null;
		try {
			if (RestStateType.valueOf(state).equals(new RestStateType(State.ACTIVE))) {
				stateToUse = State.ACTIVE;
			} else if (RestStateType.valueOf(state).equals(new RestStateType(State.INACTIVE))) {
				stateToUse = State.INACTIVE;
			} else {
				throw new RestException(RequestParameters.state, state, "unsupported mapping set State. Should be one of \"active\" or \"inactive\"");
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(RequestParameters.state, state, "invalid mapping set State. Should be one of \"active\" or \"inactive\"");
		}
		
		ConceptChronology<?> mappingConcept = ConceptAPIs.findConceptChronology(id);
		
		updateMappingSet(
				mappingConcept,
				StringUtils.isBlank(mappingSetUpdateData.name) ? "" : mappingSetUpdateData.name.trim(),
				StringUtils.isBlank(mappingSetUpdateData.inverseName) ? "" : mappingSetUpdateData.inverseName.trim(),
				StringUtils.isBlank(mappingSetUpdateData.description) ? "" : mappingSetUpdateData.description.trim(),
				StringUtils.isBlank(mappingSetUpdateData.purpose) ? "" : mappingSetUpdateData.purpose.trim(),
				/* ConceptChronology editorConceptChronology */ null, // optional
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getEditCoordinate(),
				stateToUse);
	}
	
	/**
	 * @param mappingItemCreationData - RestMappingItemVersionBaseCreate object containing data to create new mapping item
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @return the sememe sequence identifying the sememe which stores the created mapping item
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemComponent + RestPaths.createPathComponent)
	public RestInteger createNewMappingItem(
		RestMappingItemVersionBaseCreate mappingItemCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		// TODO test createNewMappingItem()
		// TODO modify createNewMappingItem() to handle optional and extended fields
		
		Optional<ConceptSnapshot> sourceConcept = Frills.getConceptSnapshot(mappingItemCreationData.sourceConcept, RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate());
		Optional<ConceptSnapshot> targetConcept = Frills.getConceptSnapshot(mappingItemCreationData.targetConcept, RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate());
		
		Optional<UUID> mappingSetID = Get.identifierService().getUuidPrimordialFromConceptSequence(mappingItemCreationData.mapSetConcept);
		Optional<UUID> qualifierID = Get.identifierService().getUuidPrimordialFromConceptSequence(mappingItemCreationData.qualifierConcept);

		RestMappingItemVersion newMappingItem =
				createMappingItem(
						sourceConcept.get(),
						mappingSetID.get(),
						targetConcept.get(),
						qualifierID.get(),
						/* UUID editorStatusID */ null,
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getEditCoordinate());
		
		int newMappingItemSequence = Get.identifierService().getSememeSequenceForUuids(newMappingItem.identifiers.uuids);

		return new RestInteger(newMappingItemSequence);
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param mappingItemUpdateData - object containing data used to update existing mapping item
	 * @param id - id of mapping item sememe to update
	 * @param state - The state to mapping item into.  Valid values are "INACTIVE"/"Inactive"/"I" or "ACTIVE"/"Active"/"A"
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@PUT
	@Path(RestPaths.mappingItemComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public void updateMappingItem(
		RestMappingItemVersionBase mappingItemUpdateData,
		@QueryParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.state) String state,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		State stateToUse = null;
		try {
			if (RestStateType.valueOf(state).equals(new RestStateType(State.ACTIVE))) {
				stateToUse = State.ACTIVE;
			} else if (RestStateType.valueOf(state).equals(new RestStateType(State.INACTIVE))) {
				stateToUse = State.INACTIVE;
			} else {
				throw new RestException(RequestParameters.state, state, "unsupported mapping item State. Should be one of \"active\" or \"inactive\"");
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(RequestParameters.state, state, "invalid mapping item State. Should be one of \"active\" or \"inactive\"");
		}

		SememeChronology<?> mappingItemSememeChronology = SememeAPIs.findSememeChronology(id);

		// TODO test updateMappingItem()
		// TODO implement update of all fields, including extended fields, in updateMappingItem()
		try {

//			switch (stateToUse) {
//			case INACTIVE:
//				try {
//					MappingItemDAO.retireMappingItem(mappingItemSememeChronology.getPrimordialUuid(),
//							RequestInfo.get().getStampCoordinate(),
//							RequestInfo.get().getEditCoordinate());
//				} catch (IOException e) {
//					throw new RestException(RequestParameters.state, e.getLocalizedMessage());
//				}
//				break;
//			case ACTIVE:
//				try {
//					MappingItemDAO.unRetireMappingItem(mappingItemSememeChronology.getPrimordialUuid(),
//							RequestInfo.get().getStampCoordinate(),
//							RequestInfo.get().getEditCoordinate());
//				} catch (IOException e) {
//					throw new RestException(RequestParameters.state, e.getLocalizedMessage());
//				}
//				break;
//
//			case CANCELED:
//			case PRIMORDIAL:
//			default:
//				throw new RestException(RequestParameters.state, state.toString(), "unsupported State");
//			}
			updateMappingItem(
					mappingItemSememeChronology,
					mappingItemUpdateData.targetConcept != null ? Get.conceptService().getConcept(mappingItemUpdateData.targetConcept) : null,
					mappingItemUpdateData.qualifierConcept != null ? Get.conceptService().getConcept(mappingItemUpdateData.qualifierConcept) : null,
					/* ConceptChronology<?> mappingItemEditorConcept */ null,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate(),
					stateToUse);

		} catch (IOException e) {
			throw new RestException("Failed updating mapping item " + id + " on " + e.getClass().getName() + " exception \"" + e.getLocalizedMessage() + "\"");
		}
	}
	
	/**
	 * Create and store a new mapping set in the DB.
	 * @param mappingName - The name of the mapping set (used for the FSN and preferred term of the underlying concept)
	 * @param inverseName - (optional) inverse name of the mapping set (if it makes sense for the mapping)
	 * @param purpose - (optional) - user specified purpose of the mapping set
	 * @param description - the intended use of the mapping set
	 * @param editorStatus - (optional) user specified status concept of the mapping set
	 * @return
	 * @throws IOException
	 */
	private static RestMappingSetVersion createMappingSet(
			String mappingName,
			String inverseName,
			String purpose,
			String description,
			UUID editorStatus,
			StampCoordinate stampCoord, 
			EditCoordinate editCoord) throws IOException
	{
		//We need to create a new concept - which itself is defining a dynamic sememe - so set that up here.
		DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(
				mappingName, mappingName, description, 
				new DynamicSememeColumnInfo[] {
					new DynamicSememeColumnInfo(0, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
							DynamicSememeDataType.UUID, null, false, false),
					new DynamicSememeColumnInfo(1, IsaacMappingConstants.get().MAPPING_QUALIFIERS.getUUID(), DynamicSememeDataType.UUID, null, false, 
							DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_QUALIFIERS.getUUID()), false)},
//					new DynamicSememeColumnInfo(2, IsaacMappingConstants.get().MAPPING_STATUS.getUUID(), DynamicSememeDataType.UUID, null, false, 
//							DynamicSememeValidatorType.IS_KIND_OF, new DynamicSememeUUIDImpl(IsaacMappingConstants.get().MAPPING_STATUS.getUUID()), false)}, 
				null, ObjectChronologyType.CONCEPT, null);
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
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
					MetaData.SYNONYM, MetaData.ENGLISH_LANGUAGE).build(
							editCoord, ChangeCheckerMode.ACTIVE);
			
			Get.sememeBuilderService().getDynamicSememeBuilder(builtDesc.getNid(),DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).build(
					editCoord, ChangeCheckerMode.ACTIVE);
		}
		
		@SuppressWarnings("rawtypes")
		SememeChronology mappingAnnotation = Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence(), 
				new DynamicSememeData[] {
//						(editorStatus == null ? null : new DynamicSememeUUIDImpl(editorStatus)),
						(StringUtils.isBlank(purpose) ? null : new DynamicSememeStringImpl(purpose))}).build(
				editCoord, ChangeCheckerMode.ACTIVE);

		
		Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getSequence()).build(
				editCoord, ChangeCheckerMode.ACTIVE);
		
		try
		{
			Get.commitService().commit("Committing update of mapping set " + mappingAnnotation.getPrimordialUuid()).get();
		}
		catch (Exception e)
		{
			throw new RuntimeException();
		}
		
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<DynamicSememe<?>>> sememe = mappingAnnotation.getLatestVersion(DynamicSememe.class, stampCoord);
		
		return new RestMappingSetVersion(sememe.get().value(), stampCoord);
	}
	private static void updateMappingSet(
			ConceptChronology<?> mappingConcept,
			String mapName,
			String mapInverseName,
			String mapDescription,
			String mapPurpose,
			ConceptChronology<?> editorConceptChronology, // optional
			StampCoordinate stampCoord,
			EditCoordinate editCoord,
			State state) throws RuntimeException 
	{		
		Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.getNid(), 
				MetaData.DESCRIPTION_ASSEMBLAGE.getConceptSequence())
			.forEach(descriptionC ->
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<DescriptionSememe<?>>> latest = ((SememeChronology)descriptionC).getLatestVersion(DescriptionSememe.class, 
						stampCoord);
				if (latest.isPresent())
				{
					DescriptionSememe<?> ds = latest.get().value();
					if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
					{
						if (Frills.isDescriptionPreferred(ds.getNid(), null))
						{
							if (!ds.getText().equals(mapName))
							{
								@SuppressWarnings({ "unchecked", "rawtypes" })
//								MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds)
//										.createMutableVersion(MutableDescriptionSememe.class, ds.getStampSequence());
								MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds)
										.createMutableVersion(MutableDescriptionSememe.class, state, editCoord);
								mutable.setText(mapName);
								@SuppressWarnings("unchecked")
								SememeChronology<DescriptionSememe<? extends DescriptionSememe<?>>> sc = (SememeChronology<DescriptionSememe<? extends DescriptionSememe<?>>>)ds;
								Get.commitService().addUncommitted(sc);
							}
						}
						else
						//see if it is the inverse name
						{
							if (Get.sememeService().getSememesForComponentFromAssemblage(ds.getNid(), 
									DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence()).anyMatch(sememeC -> 
									{
										return sememeC.isLatestVersionActive(stampCoord);
									}))
							{
								if (!ds.getText().equals(mapInverseName))
								{
									@SuppressWarnings({ "unchecked", "rawtypes" })
//									MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds)
//											.createMutableVersion(MutableDescriptionSememe.class, ds.getStampSequence());
									MutableDescriptionSememe<? extends MutableDescriptionSememe<?>> mutable = ((SememeChronology<DescriptionSememe>)ds)
											.createMutableVersion(MutableDescriptionSememe.class, state, editCoord);
									mutable.setText(mapInverseName);
									
									@SuppressWarnings("unchecked")
									SememeChronology<DescriptionSememe<? extends DescriptionSememe<?>>> sc = (SememeChronology<DescriptionSememe<? extends DescriptionSememe<?>>>)ds;
									Get.commitService().addUncommitted(sc);
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
//								MutableDescriptionSememe mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
//										.createMutableVersion(MutableDescriptionSememe.class, ds.getStampSequence());
								MutableDescriptionSememe mutable = ((SememeChronology<DescriptionSememe>)ds.getChronology())
										.createMutableVersion(MutableDescriptionSememe.class, state, editCoord);
								mutable.setText(mapDescription);
								Get.commitService().addUncommitted(ds.getChronology());
							}
						}
					}
				}
			});
		

		Optional<SememeChronology<? extends SememeVersion<?>>> mappingSememe =  Get.sememeService().getSememesForComponentFromAssemblage(mappingConcept.getNid(), 
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).findAny();
					
		if (!mappingSememe.isPresent())
		{
			log.error("Couldn't find mapping refex?");
			throw new RuntimeException("internal error");
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latestVersion = ((SememeChronology)mappingSememe.get()).getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));
		
		DynamicSememe<?> latest = latestVersion.get().value();
		
		if (latest.getData()[0] == null && mapPurpose != null || mapPurpose == null && latest.getData()[0] != null
//				|| (latest.getData()[0] != null && editorConceptChronology != null && latest.getData()[0] instanceof DynamicSememeUUID && ((DynamicSememeUUID)latest.getData()[0]).getDataUUID().equals(editorConceptChronology.getPrimordialUuid())) 
				|| latest.getData().length > 0 && latest.getData()[0] == null && mapPurpose != null || mapPurpose == null && latest.getData()[0] != null
				|| (latest.getData().length > 0 && latest.getData()[0] != null && mapPurpose != null && latest.getData()[0] instanceof DynamicSememeString && !((DynamicSememeString)latest.getData()[0]).getDataString().equals(mapPurpose)))
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingSememe.get()).createMutableVersion(
					MutableDynamicSememe.class,
					state,
					editCoord);

			mutable.setData(new DynamicSememeData[] {
//					(editorConceptChronology == null ? null : new DynamicSememeUUIDImpl(editorConceptChronology.getPrimordialUuid())),
					(StringUtils.isBlank(mapPurpose) ? null : new DynamicSememeStringImpl(mapPurpose))});
			Get.commitService().addUncommitted(latest.getChronology());
		}
		
		Get.commitService().commit("Committing update of mapping set " + mappingConcept.getPrimordialUuid());
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
	private static RestMappingItemVersion createMappingItem(
			ConceptSnapshot sourceConcept,
			UUID mappingSetID,
			ConceptSnapshot targetConcept, 
			UUID qualifierID,
			UUID editorStatusID,
			StampCoordinate stampCoord,
			EditCoordinate editCoord) throws RuntimeException, RestException
	{
		SememeBuilder<? extends SememeChronology<?>> sb =  Get.sememeBuilderService().getDynamicSememeBuilder(
				sourceConcept.getNid(),  
				Get.identifierService().getConceptSequenceForUuids(mappingSetID), 
				new DynamicSememeData[] {
						(targetConcept == null ? null : new DynamicSememeUUIDImpl(targetConcept.getPrimordialUuid())),
						(qualifierID == null ? null : new DynamicSememeUUIDImpl(qualifierID)),
						(editorStatusID == null ? null : new DynamicSememeUUIDImpl(editorStatusID))
						});
		
		UUID mappingItemUUID = UuidT5Generator.get(IsaacMappingConstants.get().MAPPING_NAMESPACE.getUUID(), 
				sourceConcept.getPrimordialUuid().toString() + "|" 
				+ mappingSetID.toString() + "|"
				+ ((targetConcept == null)? "" : targetConcept.getPrimordialUuid().toString()) + "|" 
				+ ((qualifierID == null)?   "" : qualifierID.toString()));
		
		if (Get.identifierService().hasUuid(mappingItemUUID))
		{
			throw new RestException("A mapping with the specified source, target and qualifier already exists in this set.  Please edit that mapping.");
		}
		
		sb.setPrimordialUuid(mappingItemUUID);
		@SuppressWarnings("rawtypes")
		SememeChronology built = sb.build(editCoord,ChangeCheckerMode.ACTIVE);

		try
		{
			Get.commitService().commit("Committing creation of mapping item " + built.getPrimordialUuid() + " for mapping set " + mappingSetID).get();
		}
		catch (Exception e)
		{
			throw new RestException("Failed committing new mapping item sememe");
		}
		
		@SuppressWarnings({ "unchecked" })
		Optional<LatestVersion<DynamicSememe<?>>> latest = built.getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));

		return new RestMappingItemVersion(
				latest.get().value(),
				stampCoord,
				/* Integer extendedFieldType */ null, // TODO handle extendedFieldType in RestMappingItemVersion ctor
				RequestInfo.get().shouldExpand(ExpandUtil.descriptionsExpandable));
	}
	private static void updateMappingItem(
			SememeChronology<?> mappingItemSememe,
			ConceptChronology<?> mappingItemTargetConcept,
			ConceptChronology<?> mappingItemQualifierConcept,
			ConceptChronology<?> mappingItemEditorConcept,
			StampCoordinate stampCoord,
			EditCoordinate editCoord,
			State state) throws IOException
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)mappingItemSememe).getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));
		DynamicSememe<?> rdv = latest.get().value();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)mappingItemSememe).createMutableVersion(
				MutableDynamicSememe.class,
				state,
				editCoord);
		
		DynamicSememeData[] data = rdv.getData();
		data[0] = (mappingItemTargetConcept != null ? new DynamicSememeUUIDImpl(mappingItemTargetConcept.getPrimordialUuid()) : null);
		data[1] = (mappingItemQualifierConcept != null ? new DynamicSememeUUIDImpl(mappingItemQualifierConcept.getPrimordialUuid()) : null);
		data[2] = (mappingItemEditorConcept != null ? new DynamicSememeUUIDImpl(mappingItemEditorConcept.getPrimordialUuid()) : null);

		mutable.setData(data);
		Get.commitService().addUncommitted(mappingItemSememe);

		try
		{
			Get.commitService().commit("Committing update of mapping item " + mappingItemSememe.getPrimordialUuid()).get();

			setSememeStatus(rdv.getChronology(), state, stampCoord, editCoord);
		}
		catch (Exception e)
		{
			throw new RuntimeException();
		}
	}

	private static void setSememeStatus(SememeChronology<?> sc, State state, StampCoordinate stampCoord, EditCoordinate editCoord) throws RuntimeException
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sc).getLatestVersion(DynamicSememe.class, 
				stampCoord.makeAnalog(State.ACTIVE, State.INACTIVE));
		DynamicSememe<?> ds = latest.get().value();
		
		final State priorState = ds.getState();
		if (priorState == state)
		{
			log.warn("Tried set the status to the value " + priorState.toString() + " it already has.  Doing nothing");
		}
		else
		{
			@SuppressWarnings("unchecked")
			MutableDynamicSememe<?> mds = ((SememeChronology<DynamicSememe<?>>)ds.getChronology()).createMutableVersion(MutableDynamicSememe.class, state,
					editCoord);
			mds.setData(ds.getData());
			
			Get.commitService().addUncommitted(ds.getChronology());
			Get.commitService().commit("Committing update of sememe state from " + priorState.toString() + " to " + state.toString());
		}
	}
}
