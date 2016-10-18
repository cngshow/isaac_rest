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

import java.io.IOException;
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
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableLongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableSememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableStringSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.ComponentNidSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.LongSememeImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.SememeUtil;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeBase;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeBaseCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreateData;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.EditTokens;
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
	
//	/**
//	 * @param mappingSetCreationData - object containing data used to create new mapping set
//	 * @param editToken - 
//	 *            EditToken string returned by previous call to getEditToken()
//	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
//	 * @return the UUID identifying the created concept which defines the map set
//	 * @throws RestException
//	 */
//	/**
//	 * @param mappingSetCreationData
//	 * @return
//	 * @throws RestException
//	 */
//	@POST
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@Path(RestPaths.sememeTypeComponent + RestPaths.createPathComponent)
//	public RestWriteResponse createSememeType(
//		RestMappingSetVersionBaseCreate mappingSetCreationData,
//		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
//	{
//		RequestParameters.validateParameterNamesAgainstSupportedNames(
//				RequestInfo.get().getParameters(),
//				RequestParameters.COORDINATE_PARAM_NAMES,
//				RequestParameters.editToken);
//
//		
//		try 
//		{
//			return createMappingSet(
//					mappingSetCreationData.name,
//					mappingSetCreationData.inverseName,
//					mappingSetCreationData.purpose,
//					mappingSetCreationData.description,
//					mappingSetCreationData.mapItemExtendedFieldsDefinition,
//					mappingSetCreationData.mapSetExtendedFields,
//					RequestInfo.get().getStampCoordinate(),
//					RequestInfo.get().getEditCoordinate());
//		} 
//		catch (IOException e) 
//		{
//			throw new RestException("Failed creating mapping set name=" + mappingSetCreationData.name + ", inverse=" 
//					+ mappingSetCreationData.inverseName + ", purpose=" + mappingSetCreationData.purpose + ", desc=" + mappingSetCreationData.description);
//		}
//	}
//	
	
	/**
	 * @param sememeCreationData - RestAssociationItemVersionBaseCreate object containing data to create new sememe item
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sememe UUID identifying the sememe which was created
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public RestWriteResponse createNewSememe(
		RestDynamicSememeBaseCreate sememeCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		SememeBuilder<? extends SememeChronology<?>> sb = null;
		
		int referencedComponentNid =  RequestInfoUtils.getNidFromUuidOrNidParameter("RestSememeBaseCreate.referencedComponent", sememeCreationData.referencedComponent);
		int assemblageConceptSequence = RequestInfoUtils.getConceptSequenceFromParameter("RestSememeBaseCreate.assemblageConcept", sememeCreationData.assemblageConcept);
		
		DynamicSememeData[] data = RestDynamicSememeData.translate(sememeCreationData.columnData);
		
		SememeType type = readSememeType(assemblageConceptSequence, data);
		//TODO add nice error messages on data mapping API assumptions (array size 1, data types, on dynamic to old maps)
		
		switch (type)
		{
			case DYNAMIC:
				sb = Get.sememeBuilderService().getDynamicSememeBuilder(referencedComponentNid, assemblageConceptSequence, data);
				break;
			case LONG:
				sb = Get.sememeBuilderService().getLongSememeBuilder(((DynamicSememeLongImpl)data[0]).getDataLong(), referencedComponentNid, assemblageConceptSequence);
				break;
			case MEMBER:
				sb = Get.sememeBuilderService().getMembershipSememeBuilder(referencedComponentNid, assemblageConceptSequence);
				break;
			case STRING:
				sb = Get.sememeBuilderService().getStringSememeBuilder(((DynamicSememeStringImpl)data[0]).getDataString(), referencedComponentNid, assemblageConceptSequence);
				break;
			case COMPONENT_NID:
				sb = Get.sememeBuilderService().getComponentSememeBuilder(((DynamicSememeNidImpl)data[0]).getDataNid(), referencedComponentNid, assemblageConceptSequence);
				break;
			case LOGIC_GRAPH:  //Unsupported below here
			case RELATIONSHIP_ADAPTOR:
			case DESCRIPTION:
			case UNKNOWN:
			default :
				throw new RestException("Unexpected sememe type " + type.toString() + " passed.  Try a more specific API call");
			
		}
		
		sb.setState(sememeCreationData.active == null || sememeCreationData.active ? State.ACTIVE : State.INACTIVE);
		
		SememeChronology<?> built = sb.build(RequestInfo.get().getEditCoordinate(),ChangeCheckerMode.ACTIVE).getNoThrow();
		
		try
		{
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing creation of sememe item " + built.getPrimordialUuid()).get();
			if (RequestInfo.get().getWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RestException("Failed committing new association item sememe");
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), built.getPrimordialUuid());
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param sememeCreationData - object containing data used to update existing sememe item
	 * @param id - id of the sememe to update
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sememe UUID identifying the sememe which was updated
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public RestWriteResponse updateSememe(
		RestDynamicSememeBase sememeUpdateData,
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		State stateToUse = (sememeUpdateData.active == null || sememeUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		SememeChronology<?> sememeChronology = SememeAPIs.findSememeChronology(id);
		
		DynamicSememeData[] data = RestDynamicSememeData.translate(sememeUpdateData.columnData);
		
		SememeType type = sememeChronology.getSememeType();
		//TODO add nice error messages on data mapping API assumptions (array size 1, data types, on dynamic to old maps)
		
		switch (type)
		{
			case DYNAMIC:
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableDynamicSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setData(data);
				break;
			}
			case LONG:
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				LongSememeImpl mutable = (LongSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableLongSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setLongValue(((DynamicSememeLongImpl)data[0]).getDataLong());
				break;
			}
			case MEMBER:
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				SememeVersionImpl mutable = (SememeVersionImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableSememeVersion.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				break;
			}
			case STRING:
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				StringSememeImpl mutable = (StringSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableStringSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setString(((DynamicSememeStringImpl)data[0]).getDataString());
				break;
			}
			case COMPONENT_NID:
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				ComponentNidSememeImpl mutable = (ComponentNidSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableComponentNidSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setComponentNid(((DynamicSememeNidImpl)data[0]).getDataNid());
				break;
			}
			case LOGIC_GRAPH:  //Unsupported below here
			case RELATIONSHIP_ADAPTOR:
			case DESCRIPTION:
			case UNKNOWN:
			default :
				throw new RestException("Unexpected sememe type " + type.toString() + " passed.  Try a more specific API call");
			
		}

		Get.commitService().addUncommitted(sememeChronology);

		try
		{
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of sememe item " + sememeChronology.getPrimordialUuid()).get();
			if (RequestInfo.get().getWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new RuntimeException("error committing", e);
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), sememeChronology.getPrimordialUuid());
	}
	
	private SememeType readSememeType(int assemblageConceptSequence, DynamicSememeData[] data) throws RestException
	{
		if (DynamicSememeUsageDescriptionImpl.isDynamicSememe(assemblageConceptSequence))
		{
			return SememeType.DYNAMIC;
		}
		else
		{
			if (data == null || data.length == 0)
			{
				return SememeType.MEMBER;
			}
			else if (data[0] instanceof DynamicSememeString)
			{
				return SememeType.STRING;
			}
			else if (data[0] instanceof DynamicSememeNid)
			{
				return SememeType.COMPONENT_NID;
			}
			else if (data[0] instanceof DynamicSememeLong)
			{
				return SememeType.LONG;
			}
		}
		throw new RestException("Assemblage concept isn't defined as a dynamic sememe, and can't map to a legacy sememe type");
		
	}
}