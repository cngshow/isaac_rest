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
package gov.vha.isaac.rest.api1.association;

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
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexerConfiguration;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersionCreate;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersionUpdate;
import gov.vha.isaac.rest.api1.data.association.RestAssociationTypeVersionCreate;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.LatestVersionUtils;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.utils.SecurityUtils;


/**
 * {@link AssociationWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.EDITOR})
public class AssociationWriteAPIs
{
	private static Logger log = LogManager.getLogger(AssociationWriteAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * @param associationCreationData - object containing data used to create new association
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the UUID identifying the created concept which defines the association
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationComponent + RestPaths.createPathComponent)
	public RestWriteResponse createNewAssociationType(
		RestAssociationTypeVersionCreate associationCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		
		if (StringUtils.isBlank(associationCreationData.associationName))
		{
			throw new RestException("The parameter 'associationName' is required");
		}
		if (StringUtils.isBlank(associationCreationData.description))
		{
			throw new RestException("The parameter 'description' is required");
		}
		
		DynamicSememeColumnInfo[] columns = new DynamicSememeColumnInfo[1];
		columns[0] = new DynamicSememeColumnInfo(0, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID(), 
				DynamicSememeDataType.UUID, null, false, true);
		
		DynamicSememeUsageDescription rdud = Frills.createNewDynamicSememeUsageDescriptionConcept(
				associationCreationData.associationName, associationCreationData.associationName, associationCreationData.description, columns,
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getConceptSequence(), null, null, RequestInfo.get().getEditCoordinate());
		
		//Then, annotate the concept created above as a member of the MappingSet dynamic sememe, and add the inverse name, if present.
		if (!StringUtils.isBlank(associationCreationData.associationInverseName))
		{
			ObjectChronology<?> builtDesc = LookupService.get().getService(DescriptionBuilderService.class).getDescriptionBuilder(associationCreationData.associationInverseName, 
					rdud.getDynamicSememeUsageDescriptorSequence(), 
					MetaData.SYNONYM, MetaData.ENGLISH_LANGUAGE).addAcceptableInDialectAssemblage(MetaData.US_ENGLISH_DIALECT)
						.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
			
			Get.sememeBuilderService().getDynamicSememeBuilder(builtDesc.getNid(),DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_INVERSE_NAME.getSequence())
				.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
		}
		
		//Add the association annotation
		Get.sememeBuilderService().getDynamicSememeBuilder(Get.identifierService().getConceptNid(rdud.getDynamicSememeUsageDescriptorSequence()),
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSOCIATION_SEMEME.getSequence())
					.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();
		
		try
		{
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing create of association type" + rdud.getDynamicSememeName()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected", e);
		}
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				//TODO 2 Dan (index config) see if I still need to manually do this, I thought I fixed this.
				SememeIndexerConfiguration.configureColumnsToIndex(rdud.getDynamicSememeUsageDescriptorSequence(), new Integer[] {0}, true);
			}
			catch (Exception e)
			{
				log.error("Unexpected error enabling the index on newly created mapping set!", e);
			}
		});
		return new RestWriteResponse(
				RequestInfo.get().getEditToken().renewToken(),
				Get.identifierService().getUuidPrimordialFromConceptId(rdud.getDynamicSememeUsageDescriptorSequence()).get(), 
				null, 
				rdud.getDynamicSememeUsageDescriptorSequence());
	}
	
	/**
	 * @param associationItemCreationData - RestAssociationItemVersionBaseCreate object containing data to create new association item
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sememe UUID identifying the sememe which stores the created association item
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationItemComponent + RestPaths.createPathComponent)
	public RestWriteResponse createNewAssociationItem(
		RestAssociationItemVersionCreate associationItemCreationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);

		Optional<? extends ObjectChronology<? extends StampedVersion>> source = Get.identifiedObjectService()
				.getIdentifiedObjectChronology(
						RequestInfoUtils.getNidFromUuidOrNidParameter("RestAssociationItemVersionCreate.sourceId", associationItemCreationData.sourceId));
		Optional<UUID> target = StringUtils.isBlank(associationItemCreationData.targetId) ? Optional.empty() : 
			Get.identifierService().getUuidPrimordialForNid(
					RequestInfoUtils.getNidFromUuidOrNidParameter("RestAssociationItemVersionCreate.targetId", associationItemCreationData.targetId));
		
		int assemblageType = RequestInfoUtils.getConceptSequenceFromParameter("RestAssociationItemVersionCreate.associationType", associationItemCreationData.associationType);
		Optional<UUID> associationID = Get.identifierService().getUuidPrimordialFromConceptId(assemblageType);
		
		if (!source.isPresent())
		{
			throw new RestException("sourceId", associationItemCreationData.sourceId + "", "Unable to locate the source component");
		}
		if (!associationID.isPresent())
		{
			throw new RestException("associationTypeSequence", associationItemCreationData.associationType + "", "Unable to locate the association type");
		}
		if (StringUtils.isNotBlank(associationItemCreationData.targetId) && !target.isPresent())
		{
			throw new RestException("targetId", associationItemCreationData.targetId + "", "Unable to locate the target component");
		}

		DynamicSememeData[] data = new DynamicSememeData[1];
		data[0] = (target.isPresent() ?  new DynamicSememeUUIDImpl(target.get()) : null);
		
		SememeBuilder<? extends SememeChronology<?>> sb =  Get.sememeBuilderService().getDynamicSememeBuilder(
				source.get().getNid(), assemblageType, data);
		
		UUID associationItemUUID = UuidT5Generator.get(IsaacMappingConstants.get().MAPPING_NAMESPACE.getUUID(), 
				source.get().getPrimordialUuid().toString() + "|" 
				+ associationID.get().toString() + "|"
				+ (!(target.isPresent()) ? "" : target.get().toString()) + "|");
		
		if (Get.identifierService().hasUuid(associationItemUUID))
		{
			throw new RestException("A mapping with the specified source, target and equivalence type already exists in this set.  Please edit that mapping.");
		}
		
		sb.setPrimordialUuid(associationItemUUID);
		
		if (associationItemCreationData.active != null && !associationItemCreationData.active)
		{
			sb.setState(State.INACTIVE);
		}
		
		@SuppressWarnings("rawtypes")
		SememeChronology built = sb.build(RequestInfo.get().getEditCoordinate(),ChangeCheckerMode.ACTIVE).getNoThrow();

		try
		{
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing creation of association item " + built.getPrimordialUuid() 
				+ " for association type " + associationID.get()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RestException("Failed committing new association item sememe");
		}
		return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken(), built.getPrimordialUuid());
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param associationItemUpdateData - object containing data used to update existing association item
	 * @param id - id of association item sememe to update
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the sememe UUID identifying the sememe which was updated
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationItemComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public RestWriteResponse updateAssociationItem(
		RestAssociationItemVersionUpdate associationItemUpdateData,
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES); // TODO switch to UPDATE_COORDINATE_PARAM_NAMES when WEB GUI ready

		State stateToSave = (associationItemUpdateData.active == null || associationItemUpdateData.active) ? State.ACTIVE : State.INACTIVE;
		SememeChronology<?> associationItemSememeChronology = SememeAPIs.findSememeChronology(id);
		
		Optional<UUID> target = StringUtils.isBlank(associationItemUpdateData.targetId) ? Optional.empty() : 
			Get.identifierService().getUuidPrimordialForNid(
					RequestInfoUtils.getNidFromUuidOrNidParameter("RestAssociationItemVersionUpdate.targetId", associationItemUpdateData.targetId));
		
		if (StringUtils.isNotBlank(associationItemUpdateData.targetId) && !target.isPresent())
		{
			throw new RestException("targetId", associationItemUpdateData.targetId + "", "Unable to locate the target component");
		}
		
		try {
			// Retrieve current version for comparison in order to short-circuit save if unchanged
			@SuppressWarnings("unchecked")
			Optional<DynamicSememeImpl> currentVersion = LatestVersionUtils.getLatestVersionForUpdate((SememeChronology<DynamicSememeImpl>)associationItemSememeChronology, DynamicSememeImpl.class);
			if (currentVersion.isPresent()) {
				DynamicSememeData currentTargetSememeData = (currentVersion.get().getData() != null 
						&& currentVersion.get().getData().length > 0) ? currentVersion.get().getData()[0] : null;

				UUID currentTargetUuid = null;
				if (currentTargetSememeData != null) {
					// Validate DynamicSememeData type
					if (currentTargetSememeData.getDynamicSememeDataType() != DynamicSememeDataType.UUID) {
						throw new RestException(RequestParameters.id, id, "Retrieved dynamic sememe contains unexpected data of type " + currentTargetSememeData.getDynamicSememeDataType() + ". Expected " + DynamicSememeDataType.UUID);
					}

					currentTargetUuid = ((DynamicSememeUUIDImpl)currentTargetSememeData).getDataUUID();
				}
				UUID newTargetUuid = target.isPresent() ? target.get() : null;

				// This code short-circuits update if passed data are identical to current relevant version
				if ((newTargetUuid == currentTargetUuid && currentVersion.get().getState() == stateToSave)
						|| (newTargetUuid != null && currentTargetUuid != null && newTargetUuid.equals(currentTargetUuid) && currentVersion.get().getState() == stateToSave)) {
					log.debug("Not updating association sememe {} because data unchanged", associationItemSememeChronology.getPrimordialUuid());
					return new RestWriteResponse(RequestInfo.get().getEditToken(), associationItemSememeChronology.getPrimordialUuid(), RestWriteResponseEnumeratedDetails.UNCHANGED);
				}
			} else {
				log.info("Failed retrieving latest version of association item dynamic sememe " + id + ". Module Change?  Unconditionally performing update.");
			}
		} catch (Exception e) {
			log.error("Failed checking update against latest association item dynamic sememe " + id + " version. Unconditionally performing update", e);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		DynamicSememeImpl mutable = (DynamicSememeImpl) ((SememeChronology)associationItemSememeChronology).createMutableVersion(
				MutableDynamicSememe.class,
				stateToSave,
				RequestInfo.get().getEditCoordinate());
		
		DynamicSememeData[] data = new DynamicSememeData[1];
		data[0] = (target.isPresent() ? new DynamicSememeUUIDImpl(target.get()) : null);

		mutable.setData(data);

		try
		{
			Get.commitService().addUncommitted(associationItemSememeChronology).get();
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Committing update of association item " 
					+ associationItemSememeChronology.getPrimordialUuid()).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected", e);
			throw new RuntimeException("error committing", e);
		}
		return new RestWriteResponse(
				RequestInfo.get().getEditToken().renewToken(),
				associationItemSememeChronology.getPrimordialUuid());
		
	}
}