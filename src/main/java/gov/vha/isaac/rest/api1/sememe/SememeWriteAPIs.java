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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
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
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
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
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.impl.utility.Frills;
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
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexerConfiguration;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.component.ComponentWriteAPIs;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeBase;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeBaseCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeTypeCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdate;
import gov.vha.isaac.rest.session.LatestVersionUtils;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.tokens.EditTokens;

/**
 * {@link SememeWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent)
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
public class SememeWriteAPIs
{
	private static Logger log = LogManager.getLogger(SememeWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

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
			RestSememeDescriptionCreate creationData,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);
		
		if (StringUtils.isBlank(creationData.text))
		{
			throw new RestException("creationData.text", "The text value of the description is required");
		}

		// TODO test createDescription(), including validation of creationData.getDescriptionTypeConceptSequence()
		try 
		{
			
			ArrayList<Integer> preferredDialects = new ArrayList<>();
			ArrayList<Integer> acceptableDialects = new ArrayList<>();
			
			if (creationData.preferredInDialectAssemblagesIds != null)
			{
				for (String id : creationData.preferredInDialectAssemblagesIds) {
					preferredDialects.add(RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.preferredInDialectAssemblagesIds", id));
				}
			}
			
			//Previously, we would create a US English preferred dialect here if no preferred were specified, but that made it impossible to just 
			//add an 'acceptable' description.
			
			if (creationData.acceptableInDialectAssemblagesIds != null)
			{
				for (String id : creationData.acceptableInDialectAssemblagesIds) {
					acceptableDialects.add(RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.acceptableInDialectAssemblagesIds", id));
				}
			}
			
			SememeBuilderService<? extends SememeChronology<? extends SememeVersion<?>>> sememeBuilderService
					= Get.sememeBuilderService();
			SememeBuilder<? extends SememeChronology<? extends DescriptionSememe<?>>> descriptionSememeBuilder
					= sememeBuilderService.getDescriptionSememeBuilder(
							//TODO this first one should be validating that it is a proper concept for this task... which probably should be lower in isaac, even.
							RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.caseSignificanceConcept", creationData.caseSignificanceConcept),
							StringUtils.isBlank(creationData.languageConcept) ? MetaData.ENGLISH_LANGUAGE.getConceptSequence() : 
									RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.languageConcept", creationData.languageConcept),
							//TODO validate this is a proper type... which should be down in isaac		
							RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.descriptionTypeConcept", creationData.descriptionTypeConcept),
							creationData.text,
							RequestInfoUtils.getNidFromUuidOrNidParameter("RestSememeDescriptionCreateData.referencedComponentId", creationData.referencedComponentId));

			if (creationData.active != null && !creationData.active)
			{
				descriptionSememeBuilder.setState(State.INACTIVE);
			}
			
			// TODO test addition of extendedDescriptionTypeConceptSequence UUID annotation to new description
			if (StringUtils.isNotBlank(creationData.extendedDescriptionTypeConcept)) {
				descriptionSememeBuilder.addSememe(Get.sememeBuilderService().getDynamicSememeBuilder(
						descriptionSememeBuilder, 
						DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence(),
						new DynamicSememeData[] {new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.extendedDescriptionTypeConcept", 
										creationData.extendedDescriptionTypeConcept)).get())}));
			}
			
			SememeChronology<? extends DescriptionSememe<?>> newDescription = descriptionSememeBuilder.build(RequestInfo.get().getEditCoordinate(),
					ChangeCheckerMode.ACTIVE).get();
			
			preferredDialects.forEach((id) -> 
			{
				try
				{
					sememeBuilderService.getComponentSememeBuilder(TermAux.PREFERRED.getNid(), newDescription.getNid(), id).build(
							RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
				}
				catch (RestException e)
				{
					throw new RuntimeException(e);
				}
			});
			
			acceptableDialects.forEach((id) -> 
			{
				try
				{
					sememeBuilderService.getComponentSememeBuilder(TermAux.ACCEPTABLE.getNid(), newDescription.getNid(), id).build(
							RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
				}
				catch (RestException e)
				{
					throw new RuntimeException(e);
				}
			});
			
			Optional<CommitRecord> commitRecord = Get.commitService().commit("creating new description sememe: NID=" 
					+ newDescription.getNid() + ", text=" + creationData.text).get();

			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}

			return new RestWriteResponse(RequestInfo.get().getEditToken(), newDescription.getPrimordialUuid());
		}
		catch (RuntimeException e)
		{
			if (e.getMessage() != null && e.getCause() instanceof RestException)
			{
				throw (RestException)e.getCause();
			}
			else
			{
				log.error("Unexpected error", e);
				throw new RestException("Failed creating description");
			}
		}
		catch (RestException e)
		{
			throw e;
		}
		catch (Exception e) 
		{
			log.error("Unexpected error", e);
			throw new RestException("Failed creating description");
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
			RestSememeDescriptionUpdate descriptionSememeUpdateData,
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES, // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
				RequestParameters.editToken);

		// TODO test updateDescription(), including validation of updateData.getDescriptionTypeConceptSequence()
		int sememeSequence = RequestInfoUtils.getSememeSequenceFromParameter(RequestParameters.id, id);
		SememeChronology<? extends SememeVersion<?>> sememeChronology = Get.sememeService().getOptionalSememe(sememeSequence).get();

		boolean updateDescriptionRequired = true;
		boolean updateExtendedTypeRequired = true;
		int passedExtendedType = -1;
		int currentExtendedType = -1;
		
		try {
			// This code short-circuits update if passed data are identical to current relevant version
			@SuppressWarnings({ "unchecked" })
			Optional<DescriptionSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DescriptionSememeImpl>)sememeChronology, 
					DescriptionSememeImpl.class);
			
			if (currentVersion.isPresent()) {
				int passedCaseSignificanceConcept = RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.caseSignificanceConcept", descriptionSememeUpdateData.caseSignificanceConcept);
				int passedLanguageConcept = StringUtils.isBlank(descriptionSememeUpdateData.languageConcept) ? MetaData.ENGLISH_LANGUAGE.getConceptSequence() :
					RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.languageConcept", descriptionSememeUpdateData.languageConcept);
				int passedDescriptionTypeConcept = RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.descriptionTypeConcept", descriptionSememeUpdateData.descriptionTypeConcept);
				State passedState = (descriptionSememeUpdateData.active == null || descriptionSememeUpdateData.active) ? State.ACTIVE : State.INACTIVE;
				
				if (passedCaseSignificanceConcept == currentVersion.get().getCaseSignificanceConceptSequence()
						&& passedLanguageConcept == currentVersion.get().getLanguageConceptSequence()
						&& passedDescriptionTypeConcept == currentVersion.get().getDescriptionTypeConceptSequence()
						&& descriptionSememeUpdateData.text.equals(currentVersion.get().getText())
						&& passedState == currentVersion.get().getState()) 
				{
					updateDescriptionRequired = false;
					log.debug("Not updating description sememe {} because data unchanged", currentVersion.get().getPrimordialUuid());
				}
				
				
				Optional<UUID> descriptionExtendedTypeOptional = Frills.getDescriptionExtendedTypeConcept(RequestInfo.get().getStampCoordinate(), 
						currentVersion.get().getNid());
				if (descriptionExtendedTypeOptional.isPresent()) 
				{
					currentExtendedType = Get.identifierService().getConceptSequenceForUuids(descriptionExtendedTypeOptional.get());
				}
				
				if (StringUtils.isNotBlank(descriptionSememeUpdateData.extendedDescriptionTypeConcept)) 
				{
					passedExtendedType = RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionCreateData.extendedDescriptionTypeConcept", 
							descriptionSememeUpdateData.extendedDescriptionTypeConcept);
				}
				
				if (passedExtendedType == currentExtendedType)
				{
					updateExtendedTypeRequired = false;
					log.debug("Not updating extended description type because data unchanged");
				}
				
				if (!updateDescriptionRequired && !updateExtendedTypeRequired)
				{
					log.debug("Not updating description sememe {} or extended type because data unchanged", currentVersion.get().getPrimordialUuid());
					return new RestWriteResponse(RequestInfo.get().getEditToken(), currentVersion.get().getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
				}
				
			} else {
				log.warn("Failed retrieving latest version of object " + id + ". Unconditionally performing update");
			}
		} catch (Exception e) {
			log.warn("Failed checking update against current object " + id + " version. Unconditionally performing update", e);
		}
		
		try {

			if (updateDescriptionRequired)
			{
				@SuppressWarnings({ "rawtypes", "unchecked" })
				DescriptionSememeImpl mutableVersion =
				(DescriptionSememeImpl)((SememeChronology)sememeChronology).createMutableVersion(
						DescriptionSememeImpl.class, (descriptionSememeUpdateData.active == null || descriptionSememeUpdateData.active ? State.ACTIVE : State.INACTIVE),
						RequestInfo.get().getEditCoordinate());
		
				mutableVersion.setCaseSignificanceConceptSequence(RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.caseSignificanceConcept", 
						descriptionSememeUpdateData.caseSignificanceConcept));
				mutableVersion.setLanguageConceptSequence(RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.languageConcept", 
						descriptionSememeUpdateData.languageConcept));
				mutableVersion.setText(descriptionSememeUpdateData.text);
				//TODO this needs a validator in isaac, to ensure it is a proper type
				mutableVersion.setDescriptionTypeConceptSequence(RequestInfoUtils.getConceptSequenceFromParameter("RestSememeDescriptionUpdate.descriptionTypeConcept", 
						descriptionSememeUpdateData.descriptionTypeConcept));
				Get.commitService().addUncommitted(sememeChronology).get();
			}
			
			SememeChronology<? extends SememeVersion<?>> extendedDescriptionTypeSememeChronology = null;
			
			if (updateExtendedTypeRequired)
			{
				// TODO test all edge cases of extendedDescriptionTypeConceptSequence UUID annotation on update....
				if (passedExtendedType == -1)
				{
					//Need to inactivate the existing one, 
					extendedDescriptionTypeSememeChronology =
							Frills.getAnnotationSememe(Get.identifierService().getSememeNid(sememeChronology.getNid()), 
									DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence()).get();
					ComponentWriteAPIs.resetStateWithNoCommit(State.INACTIVE, extendedDescriptionTypeSememeChronology.getNid() + "");
					Get.commitService().addUncommitted(extendedDescriptionTypeSememeChronology).get();
				}
				else if (currentExtendedType == -1)
				{
					//Just need to create
					extendedDescriptionTypeSememeChronology = Get.sememeBuilderService().getDynamicSememeBuilder(
							sememeChronology.getNid(), 
							DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence(),
							new DynamicSememeData[] {new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptId(passedExtendedType).get())})
								.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).get();
				}
				else
				{
					//Modify existing
					extendedDescriptionTypeSememeChronology =
							Frills.getAnnotationSememe(Get.identifierService().getSememeNid(sememeChronology.getNid()), 
									DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getConceptSequence()).get();
					
					DynamicSememeImpl mutableVersion =(DynamicSememeImpl)((SememeChronology)(extendedDescriptionTypeSememeChronology))
							.createMutableVersion(DynamicSememeImpl.class , State.ACTIVE, RequestInfo.get().getEditCoordinate());
					mutableVersion.setData(new DynamicSememeData[] {new DynamicSememeUUIDImpl(Get.identifierService().getUuidPrimordialFromConceptId(passedExtendedType).get())});
					Get.commitService().addUncommitted(extendedDescriptionTypeSememeChronology).get();
				}
			}

			Optional<CommitRecord> commitRecord = Get.commitService().commit("updating description sememe: SEQ=" + sememeSequence 
					+ ", NID=" + sememeChronology.getNid() + " with " + descriptionSememeUpdateData).get();

			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed updating description " + id + " with " + descriptionSememeUpdateData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}

		return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid());
	}
	
	/**
	 * @param mappingSetCreationData - object containing data used to create new mapping set
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the UUID identifying the created concept which defines the map set
	 * @throws RestException
	 */
	/**
	 * @param mappingSetCreationData
	 * @return
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeTypeComponent + RestPaths.createPathComponent)
	public RestWriteResponse createSememeType(
		RestDynamicSememeTypeCreate sememeTypeCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken);

		if (StringUtils.isBlank(sememeTypeCreationData.name))
		{
			throw new RestException("The parameter 'sememeTypeCreationData.name' is required");
		}
		if (StringUtils.isBlank(sememeTypeCreationData.description))
		{
			throw new RestException("The parameter 'sememeTypeCreationData.description' is required");
		}
		
		ObjectChronologyType referencedComponentRestriction = ObjectChronologyType.parse(sememeTypeCreationData.referencedComponentRestriction, true);
		SememeType referencedComponentSubRestriction = SememeType.parse(sememeTypeCreationData.referencedComponentSubRestriction, true);
		
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[sememeTypeCreationData.dataColumnsDefinition == null ? 0 
				: sememeTypeCreationData.dataColumnsDefinition.length];
		ArrayList<Integer> indexConfig = new ArrayList<>();
		if (sememeTypeCreationData.dataColumnsDefinition != null)
		{
			for (int i = 0; i < sememeTypeCreationData.dataColumnsDefinition.length; i++)
			{
				//TODO 2 Dan make index config smarter / easier.  Shouldn't be trying to index unindexable types
				indexConfig.add(i);
				columns[i] = new DynamicSememeColumnInfo(i, 
						Get.identifierService().getUuidPrimordialFromConceptId(
								RequestInfoUtils.getConceptSequenceFromParameter("RestDynamicSememeTypeCreate.dataColumnsDefinition.columnLabelConcept", 
										sememeTypeCreationData.dataColumnsDefinition[i].columnLabelConcept)).get(), 
						DynamicSememeDataType.parse(sememeTypeCreationData.dataColumnsDefinition[i].columnDataType, true), 
						RestDynamicSememeData.translate(sememeTypeCreationData.dataColumnsDefinition[i].columnDefaultData), 
						sememeTypeCreationData.dataColumnsDefinition[i].columnRequired, 
						DynamicSememeValidatorType.parse(sememeTypeCreationData.dataColumnsDefinition[i].columnValidatorTypes, true), 
						RestDynamicSememeData.translate(sememeTypeCreationData.dataColumnsDefinition[i].columnValidatorData), true);
			}
		}
		
		DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(
				sememeTypeCreationData.name, sememeTypeCreationData.name, sememeTypeCreationData.description, columns,
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getConceptSequence(), referencedComponentRestriction, referencedComponentSubRestriction, 
				RequestInfo.get().getEditCoordinate());
		
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
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				//TODO 2 Dan (index config)  see if I still need to manually do this, I thought I fixed this.
				SememeIndexerConfiguration.configureColumnsToIndex(rdud.getDynamicSememeUsageDescriptorSequence(), 
						indexConfig.toArray(new Integer[indexConfig.size()]), true);
			}
			catch (Exception e)
			{
				log.error("Unexpected error enabling the index on newly created sememe set!", e);
			}
		});
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), 
				Get.identifierService().getUuidPrimordialFromConceptId(rdud.getDynamicSememeUsageDescriptorSequence()).get());
	}
	
	
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
	public RestWriteResponse createSememe(
		RestDynamicSememeBaseCreate sememeCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		SememeBuilder<? extends SememeChronology<?>> sb = null;
		
		int referencedComponentNid =  RequestInfoUtils.getNidFromUuidOrNidParameter("RestSememeBaseCreate.referencedComponent", sememeCreationData.referencedComponent);
		int assemblageConceptSequence = RequestInfoUtils.getConceptSequenceFromParameter("RestSememeBaseCreate.assemblageConcept", sememeCreationData.assemblageConcept);
		
		DynamicSememeData[] data = RestDynamicSememeData.translate(sememeCreationData.columnData, true);
		
		SememeType type = readSememeType(assemblageConceptSequence, data);
		checkTypeMap(type, data);
		
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
				if (data[0] instanceof DynamicSememeNid)
				{
					sb = Get.sememeBuilderService().getComponentSememeBuilder(((DynamicSememeNid)data[0]).getDataNid(), referencedComponentNid, assemblageConceptSequence);
				}
				else if (data[0] instanceof DynamicSememeUUID)
				{
					sb = Get.sememeBuilderService().getComponentSememeBuilder(Get.identifierService().getNidForUuids(((DynamicSememeUUID)data[0]).getDataUUID()),
							referencedComponentNid, assemblageConceptSequence);
				}
				else
				{
					throw new RuntimeException("Should have only got to Nid from UUID or nid");
				}
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
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RestException("Failed committing new association item sememe");
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), built.getPrimordialUuid());
	}
	
	public static boolean equals(DynamicSememeData[] d1, DynamicSememeData[] d2) {
		if (d1 == d2) {
			return true;
		}
		
		int d1Length = d1 == null ? 0 : d1.length;
		int d2Length = d2 == null ? 0 : d2.length;
		
		if (d1Length != d2Length) {
			return false;
		}
		
		if (d1Length == 0 && d2Length == 0) {
			return true;
		}
		
		for (int i = 0; i < d1.length; ++i) {
			if (! Arrays.equals(d1[i].getData(), d2[i].getData())) {
				return false;
			}
		}
		
		return true;
	}
 
	private static void validateDynamicSememeDataTypeForUpdate(RestDynamicSememeBase updateObject, String id, SememeType type, DynamicSememeData data, DynamicSememeDataType expectedType) throws RestException {
		if (data.getDynamicSememeDataType() != expectedType) {
			String msg = "passed mismatched sememe type (" + data.getDynamicSememeDataType() + ") of columnData for updating sememe " + id + " of type " + type + " (should be " + expectedType + ")";
			log.info(msg + ": " + updateObject);
			throw new RestException("sememeUpdateData.columnData", null, msg);
		}
	}
	
	private void checkTypeMap(SememeType type, DynamicSememeData[] data) throws RestException
	{
		// Validate DynamicSememeData column array
		// DYNAMIC must have null or 0..n
		// MEMBER must have null or 0
		// LONG, STRING and COMPONENT_NID must have 1
		if (type != SememeType.DYNAMIC) {
			int numColumns = (data == null) ? 0 : data.length;
			int expectedNumColumns = (type == SememeType.MEMBER) ? 0 : 1;
			if (numColumns != expectedNumColumns) {
				String msg = "unsupported number (" + numColumns + ") of columnData for updating sememe (should be exactly " + expectedNumColumns + ")";
				log.info(msg);
				throw new RestException("sememeUpdateData.columnData", null, msg);
			}
		}
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
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES); // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready
		
		State stateToUse = (sememeUpdateData.active == null || sememeUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		SememeChronology<?> sememeChronology = SememeAPIs.findSememeChronology(id);
		
		DynamicSememeData[] passedData = RestDynamicSememeData.translate(sememeUpdateData.columnData, true);
		
		SememeType type = sememeChronology.getSememeType();

		checkTypeMap(type, passedData);
		
		switch (type)
		{
			case DYNAMIC:
			{
				try {
					@SuppressWarnings("unchecked")
					Optional<DynamicSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)sememeChronology, DynamicSememeImpl.class);

					if (currentVersion.isPresent()) {
						// This code short-circuits update if passed data are identical to current relevant version
						if (equals(currentVersion.get().getData(), passedData) && currentVersion.get().getState() == stateToUse) {
							log.debug("Not updating dynamic sememe {} because data unchanged", sememeChronology.getPrimordialUuid());
							return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
						}
					} else {
						log.info("Failed retrieving latest version of " + type + " sememe " + id + ". Module change?  Unconditionally performing update.");
					}
				} catch (Exception e) {
					log.error("Failed checking update against current " + type + " sememe " + id + " version. Unconditionally performing update", e);
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableDynamicSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setData(passedData);
				break;
			}
			case LONG:
			{
				// Validate data type
				validateDynamicSememeDataTypeForUpdate(sememeUpdateData, id, type, passedData[0], DynamicSememeDataType.LONG);

				try {
					@SuppressWarnings("unchecked")
					Optional<LongSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<LongSememeImpl>)sememeChronology, LongSememeImpl.class);

					if (currentVersion.isPresent()) {
						// This code short-circuits update if passed data are identical to current relevant version
						if (currentVersion.get().getLongValue() == ((DynamicSememeLong)passedData[0]).getDataLong()
								&& currentVersion.get().getState() == stateToUse) {
							log.debug("Not updating dynamic sememe {} because data unchanged", sememeChronology.getPrimordialUuid());
							return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
						}
					} else {
						log.info("Failed retrieving latest version of " + type + " sememe " + id + ". Module change?  Unconditionally performing update.");
					}
				} catch (Exception e) {
					log.error("Failed checking update against current " + type + " sememe " + id + " state. Unconditionally performing update", e);
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				LongSememeImpl mutable = (LongSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableLongSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setLongValue(((DynamicSememeLongImpl)passedData[0]).getDataLong());

				break;
			}
			case MEMBER:
			{
				try {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<SememeVersionImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<SememeVersionImpl>)sememeChronology, SememeVersionImpl.class);

					if (currentVersion.isPresent()) {
						// This code short-circuits update if passed data are identical to current relevant version
						if (currentVersion.get().getState() == stateToUse) {
							log.debug("Not updating member sememe {} because state unchanged", sememeChronology.getPrimordialUuid());
							return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
						}
					} else {
						log.info("Failed retrieving latest version of " + type + " sememe " + id + ". Module change?  Unconditionally performing update.");
					}
				} catch (Exception e) {
					log.error("Failed checking update against current " + type + " sememe " + id + " version. Unconditionally performing update", e);
				}

				@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
				SememeVersionImpl mutable = (SememeVersionImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableSememeVersion.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				break;
			}
			case STRING:
			{
				// Validate data type
				validateDynamicSememeDataTypeForUpdate(sememeUpdateData, id, type, passedData[0], DynamicSememeDataType.STRING);

				try {
					@SuppressWarnings("unchecked")
					Optional<StringSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<StringSememeImpl>)sememeChronology, StringSememeImpl.class);

					if (currentVersion.isPresent()) {
						// This code short-circuits update if passed data are identical to current relevant version
						if (currentVersion.get().getString().equals(((DynamicSememeString)passedData[0]).getDataString())
								&& currentVersion.get().getState() == stateToUse) {
							log.debug("Not updating dynamic sememe {} because data unchanged", sememeChronology.getPrimordialUuid());
							return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
						}
					} else {
						log.info("Failed retrieving latest version of " + type + " sememe " + id + ". Module change?  Unconditionally performing update");
					}
				} catch (Exception e) {
					log.error("Failed checking update against current " + type + " sememe " + id + " version. Unconditionally performing update", e);
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				StringSememeImpl mutable = (StringSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableStringSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setString(((DynamicSememeStringImpl)passedData[0]).getDataString());

				break;
			}
			case COMPONENT_NID:
			{
				// Validate data type
				validateDynamicSememeDataTypeForUpdate(sememeUpdateData, id, type, passedData[0], DynamicSememeDataType.NID);

				try {
					@SuppressWarnings("unchecked")
					Optional<ComponentNidSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<ComponentNidSememeImpl>)sememeChronology, ComponentNidSememeImpl.class);

					if (currentVersion.isPresent()) {
						// This code short-circuits update if passed data are identical to current relevant version
						if (currentVersion.get().getComponentNid() == ((DynamicSememeNidImpl)passedData[0]).getDataNid()
								&& currentVersion.get().getState() == stateToUse) {
							log.debug("Not updating dynamic sememe {} because data unchanged", sememeChronology.getPrimordialUuid());
							return new RestWriteResponse(RequestInfo.get().getEditToken(), sememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
						}
					} else {
						log.info("Failed retrieving latest version of " + type + " sememe " + id + ". Module change?  Unconditionally performing update");
					}
				} catch (Exception e) {
					log.error("Failed checking update against current " + type + " sememe " + id + " version. Unconditionally performing update", e);
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				ComponentNidSememeImpl mutable = (ComponentNidSememeImpl) ((SememeChronology)sememeChronology).createMutableVersion(MutableComponentNidSememe.class,
						stateToUse, RequestInfo.get().getEditCoordinate());
				mutable.setComponentNid(((DynamicSememeNidImpl)passedData[0]).getDataNid());

				break;
			}
			case LOGIC_GRAPH:  //Unsupported here and below
			case RELATIONSHIP_ADAPTOR:
			case DESCRIPTION:
			case UNKNOWN:
			default :
				throw new RestException("Unexpected sememe type " + type.toString() + " passed.  Try a more specific API call");
		}

		try
		{
			Get.commitService().addUncommitted(sememeChronology).get();
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of sememe item " + sememeChronology.getPrimordialUuid()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			
			//TODO need to test and see if cancel works... we mostly likely got here because of a validator failure.
			//TODO still need to run all of the validators before we attempt to save, throw a better error on validation failure.
			Get.commitService().cancel(sememeChronology, RequestInfo.get().getEditCoordinate());
			
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
			else if (data[0] instanceof DynamicSememeNid 
					|| (data[0] instanceof DynamicSememeUUID && Get.identifierService().hasUuid(((DynamicSememeUUID)data[0]).getDataUUID())))
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