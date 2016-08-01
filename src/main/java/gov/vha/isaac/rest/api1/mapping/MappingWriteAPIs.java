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

import org.codehaus.plexus.util.StringUtils;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSnapshot;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.data.MappingItem;
import gov.vha.isaac.ochre.mapping.data.MappingItemDAO;
import gov.vha.isaac.ochre.mapping.data.MappingSet;
import gov.vha.isaac.ochre.mapping.data.MappingSetDAO;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBaseCreate;
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
		MappingSet newMappingSet = null;
		try {
			newMappingSet = MappingSetDAO.createMappingSet(
					mappingSetCreationData.name,
					mappingSetCreationData.inverseName,
					mappingSetCreationData.purpose,
					mappingSetCreationData.description,
					/* (UUID editorStatus) */ null,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate());
			
			return new RestInteger(Get.identifierService().getConceptSequenceForUuids(newMappingSet.getPrimordialUUID()));
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
	@Path(RestPaths.mappingSetComponent + RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
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
		
		MappingSetDAO.updateMappingSet(
				mappingConcept,
				StringUtils.isBlank(mappingSetUpdateData.name) ? "" : mappingSetUpdateData.name.trim(),
				StringUtils.isBlank(mappingSetUpdateData.inverseName) ? "" : mappingSetUpdateData.inverseName.trim(),
				StringUtils.isBlank(mappingSetUpdateData.description) ? "" : mappingSetUpdateData.description.trim(),
				StringUtils.isBlank(mappingSetUpdateData.purpose) ? "" : mappingSetUpdateData.purpose.trim(),
				/* ConceptChronology editorConceptChronology */ null, // optional
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getEditCoordinate());
		
		switch (stateToUse) {
		case INACTIVE:
			try {
				MappingSetDAO.retireMappingSet(mappingConcept.getPrimordialUuid(),
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getEditCoordinate());
			} catch (IOException e) {
				throw new RestException(RequestParameters.state, e.getLocalizedMessage());
			}
			break;
		case ACTIVE:
			try {
				MappingSetDAO.unRetireMappingSet(mappingConcept.getPrimordialUuid(),
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getEditCoordinate());
			} catch (IOException e) {
				throw new RestException(RequestParameters.state, e.getLocalizedMessage());
			}
			break;

		case CANCELED:
		case PRIMORDIAL:
		default:
			throw new RestException(RequestParameters.state, state.toString(), "unsupported State");
		}
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

		MappingItem newMappingItem =
				MappingItemDAO.createMappingItem(
						sourceConcept.get(),
						mappingSetID.get(),
						targetConcept.get(),
						qualifierID.get(),
						/* UUID editorStatusID */ null,
						RequestInfo.get().getStampCoordinate(),
						RequestInfo.get().getEditCoordinate());
		
		int newMappingItemSequence = Get.identifierService().getSememeSequenceForUuids(newMappingItem.getPrimordialUUID());

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

		SememeChronology<?> mappingItemSememe = SememeAPIs.findSememeChronology(id);

		// TODO test updateMappingItem()
		// TODO implement update of all fields, including extended fields, in updateMappingItem()
		try {
			MappingItemDAO.updateMappingItem(
					mappingItemSememe,
					/* ConceptChronology<?> mappingItemEditorConcept */ null,
					RequestInfo.get().getStampCoordinate(),
					RequestInfo.get().getEditCoordinate());

			switch (stateToUse) {
			case INACTIVE:
				try {
					MappingItemDAO.retireMappingItem(mappingItemSememe.getPrimordialUuid(),
							RequestInfo.get().getStampCoordinate(),
							RequestInfo.get().getEditCoordinate());
				} catch (IOException e) {
					throw new RestException(RequestParameters.state, e.getLocalizedMessage());
				}
				break;
			case ACTIVE:
				try {
					MappingItemDAO.unRetireMappingItem(mappingItemSememe.getPrimordialUuid(),
							RequestInfo.get().getStampCoordinate(),
							RequestInfo.get().getEditCoordinate());
				} catch (IOException e) {
					throw new RestException(RequestParameters.state, e.getLocalizedMessage());
				}
				break;

			case CANCELED:
			case PRIMORDIAL:
			default:
				throw new RestException(RequestParameters.state, state.toString(), "unsupported State");
			}
		} catch (IOException e) {
			throw new RestException("Failed updating mapping item " + id + " on " + e.getClass().getName() + " exception \"" + e.getLocalizedMessage() + "\"");
		}
	}
}
