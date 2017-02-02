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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.ConceptAPIs;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayField;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayFieldCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfo;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link MappingAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.mappingAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class MappingAPIs
{
	private static Logger log = LogManager.getLogger(MappingAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * 
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - A comma separated list of fields to expand.  Supports 'comments'.  When comments is passed, the latest comment(s) attached to each 
	 * mapSet are included.
	 * @return the latest version of each unique mapping set definition found in the system on the specified coordinates. 
	 * 
	 * TODO add parameters to this method to allow the return of all versions (current + historical)
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetsComponent)
	public RestMappingSetVersion[] getMappingSets(
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestMappingSetVersion> results = new ArrayList<>();
		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		
		Get.sememeService().getSememesFromAssemblage(IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).forEach(sememeC -> 
		{
			//We don't change the state / care about the state on the sememe.  We update the state on the concept.
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememeC).getLatestVersion(DynamicSememe.class, 
					RequestInfo.get().getStampCoordinate().makeAnalog(State.ACTIVE, State.INACTIVE));
			
			if (latest.isPresent())
			{
				ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService().getConcept(latest.get().value().getReferencedComponentNid());
				
				StampCoordinate conceptCoord = Util.getPreWorkflowStampCoordinate(processIdUUID, cc.getNid());
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Optional<LatestVersion<ConceptVersion<?>>> cv =  ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class, 
						conceptCoord);
				
				if (cv.isPresent())
				{
					//TODO handle contradictions
					results.add(new RestMappingSetVersion(cv.get().value(), latest.get().value(), conceptCoord, 
							RequestInfo.get().shouldExpand(ExpandUtil.comments), processIdUUID));
				}
			}
		});
		return results.toArray(new RestMappingSetVersion[results.size()]);
	}
	
	/**
	 * @param id - A UUID, nid, or concept sequence that identifies the map set.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param expand - A comma separated list of fields to expand.  Supports 'comments'.  When comments is passed, the latest comment(s) attached to each 
	 * mapSet are included.
	 * @return the latest version of the specified mapping set.
	 * 
	 * TODO add parameters to this method to allow the return of all versions (current + historical)
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingSetComponent + "{" + RequestParameters.id +"}")
	public RestMappingSetVersion getMappingSet(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.processId) String processId,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);

		return getMappingSet(id, processId);
	}
	
	static RestMappingSetVersion getMappingSet(
			String id,
			String processId) throws RestException {
		Optional<SememeChronology<? extends SememeVersion<?>>> sememe = Get.sememeService().getSememesForComponentFromAssemblage(ConceptAPIs.findConceptChronology(id).getNid(), 
				IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getSequence()).findAny();

			if (! sememe.isPresent()) 
			{
				throw new RestException("The map set identified by '" + id + "' is not present");
			}
			
			UUID processIdUUID = Util.validateWorkflowProcess(processId);
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememe.get()).getLatestVersion(DynamicSememe.class, 
					Util.getPreWorkflowStampCoordinate(processIdUUID, sememe.get().getNid()));
			if (latest.isPresent())
			{
				ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService().getConcept(latest.get().value().getReferencedComponentNid());
				
				StampCoordinate conceptCoord = Util.getPreWorkflowStampCoordinate(processIdUUID, cc.getNid());
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Optional<LatestVersion<ConceptVersion<?>>> cv =  ((ConceptChronology) cc).getLatestVersion(ConceptVersion.class, 
						conceptCoord);
				
				if (cv.isPresent())
				{
					//TODO handle contradictions
					return new RestMappingSetVersion(cv.get().value(), latest.get().value(), conceptCoord, RequestInfo.get().shouldExpand(ExpandUtil.comments), 
						processIdUUID);
				}
				else 
				{
					throw new RestException("The map set identified by '" + id + "' is not present at the given stamp");
				}
			} 
			else 
			{
				throw new RestException("The map set identified by '" + id + "' is not present at the given stamp");
			}
	}

	/**
	 * @return array of {@link RestMappingSetDisplayField} available for ordering and displaying mapping set item fields
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingFieldsComponent)
	public RestMappingSetDisplayField[] getAvailableMappingSetDisplayFields() throws RestException {
		SecurityUtils.validateRole(securityContext, getClass());
		
		MapSetDisplayFieldsService service = LookupService.getService(MapSetDisplayFieldsService.class);
		Collection<MapSetDisplayFieldsService.Field> fields = service.getAllFields();
		List<RestMappingSetDisplayField> restFields = new ArrayList<>();
		for (MapSetDisplayFieldsService.Field field : fields) {			
			restFields.add(new RestMappingSetDisplayField(field.getId()));
		}
		
		return restFields.toArray(new RestMappingSetDisplayField[restFields.size()]);
	}
	
	/**
	 * @return array of {@link RestMapSetItemComponentType} values available for use in constructing a {@link RestMappingSetDisplayFieldCreate}
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingFieldComponentTypesComponent)
	public RestMapSetItemComponentType[] getAvailableMappingSetDisplayFieldComponentTypes() throws RestException {
		SecurityUtils.validateRole(securityContext, getClass());
		
		return RestMapSetItemComponentType.getAll();
	}

	
	/**
	 * @param id - A UUID, nid, or concept sequence that identifies the map set to list items for.  Should be from {@link RestMappingSetVersion#identifiers}}
	 * @param expand - A comma separated list of fields to expand.  Supports 'referencedDetails,comments'.  When referencedDetails is passed, descriptions
	 * will be included for all referenced concepts which align with your current coordinates.  When comments is passed, all comments attached to each mapItem are 
	 * included.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the mapping items versions object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemsComponent + "{" + RequestParameters.id +"}")
	public RestMappingItemVersion[] getMappingItems(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.expand) String expand,
		@QueryParam(RequestParameters.processId) String processId,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		//TODO 1 Dan this MUST be paged - note, we can use the fact that the sememe iterate iterates in order, to figure out where to start/stop the ranges.
		//will make it fast for early pages... still slow for later pages, unless we enhance the underlying isaac code to handle paging natively
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestMappingItemVersion> results = new ArrayList<>();
		
		int sememeConceptSequence = Util.convertToConceptSequence(id);
		
		Positions positions = Positions.getPositions(sememeConceptSequence);
		
		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		
		List<RestMappingSetDisplayField> displayFields = MappingAPIs.getMappingSetDisplayFieldsFromMappingSet(
				Get.identifierService().getConceptNid(sememeConceptSequence), RequestInfo.get().getStampCoordinate());
		try
		{
			Get.sememeService().getSememesFromAssemblage(sememeConceptSequence).forEach(sememeC -> 
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)sememeC).getLatestVersion(DynamicSememe.class, 
						Util.getPreWorkflowStampCoordinate(processIdUUID, sememeC.getNid()));
				
				if (latest.isPresent())
				{
					//TODO handle contradictions
					results.add(new RestMappingItemVersion(((DynamicSememe<?>)latest.get().value()), 
						positions.targetPos, positions.qualfierPos,
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
						RequestInfo.get().shouldExpand(ExpandUtil.comments),
						processIdUUID, displayFields));
				}
				if (results.size() >= 250)
				{
					throw new RuntimeException("Java 9 will fix this with takeWhile...");
				}
				
			});
		}
		catch (RuntimeException e)
		{
			if (e.getMessage() != null && e.getMessage().startsWith("Java 9 will"))
			{
				log.warn("Cutting results short, as paging isn't yet implemented!");
			}
			else
			{
				throw e;
			}
		}
		return results.toArray(new RestMappingItemVersion[results.size()]);
	}

	/**
	 * @param id - A UUID, nid, or sememe sequence that identifies a map item.
	 * @param expand - A comma separated list of fields to expand.  Supports 'referencedDetails,comments'.  When referencedDetails is passed, descriptions
	 * will be included for all referenced concepts which align with your current coordinates.  When comments is passed, all comments attached to each mapItem are 
	 * included.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the mapping item version object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.mappingItemComponent + "{" + RequestParameters.id +"}")
	public RestMappingItemVersion getMappingItem(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.expand) String expand,
		@QueryParam(RequestParameters.processId) String processId,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		int sequence = RequestInfoUtils.getSememeSequenceFromParameter(RequestParameters.id, id);
		
		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		@SuppressWarnings("rawtypes")
		SememeChronology sememe =  Get.sememeService().getSememe(sequence);
		
		Positions positions = Positions.getPositions(sememe.getAssemblageSequence());
		
		@SuppressWarnings({ "unchecked"})
		Optional<LatestVersion<DynamicSememe<?>>> latest = sememe.getLatestVersion(DynamicSememe.class, 
				Util.getPreWorkflowStampCoordinate(processIdUUID, sememe.getNid()));
		
		List<RestMappingSetDisplayField> displayFields = MappingAPIs.getMappingSetDisplayFieldsFromMappingSet(
				Get.identifierService().getConceptNid(sememe.getAssemblageSequence()), RequestInfo.get().getStampCoordinate());
			
		if (latest.isPresent())
		{
			//TODO handle contradictions
			return new RestMappingItemVersion(((DynamicSememe<?>)latest.get().value()), 
				positions.targetPos, positions.qualfierPos,
				RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
				RequestInfo.get().shouldExpand(ExpandUtil.comments),
				processIdUUID, displayFields);
		}
		else
		{
			throw new RestException("The specified map item is not available on the specified coordinate");
		}
	}
	
	/**
	 * This method retrieves the map set display fields on the respective sememe attached to the map set concept,
	 * if it exists
	 *  
	 * @param mappingConceptNid the NID for the map set concept from which to extract the list of RestMappingSetDisplayField
	 * @param stampCoord the StampCoordinate with which to request the latest version of the display fields sememe, if it exists
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<RestMappingSetDisplayField> getMappingSetDisplayFieldsFromMappingSet(
			int mappingConceptNid,
			StampCoordinate stampCoord) {
		List<RestMappingSetDisplayField> fields = new ArrayList<>();
		Optional<SememeChronology<? extends SememeVersion<?>>> mapSetFieldsSememe = Frills.getAnnotationSememe(mappingConceptNid, IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_DISPLAY_FIELDS.getSequence());
		if (mapSetFieldsSememe.isPresent()) {
			List<RestDynamicSememeColumnInfo> itemFieldDefinitions = getItemFieldDefinitions(mappingConceptNid);

			@SuppressWarnings({ "rawtypes" })
			Optional<LatestVersion<DynamicSememeImpl>> existingVersionOptionalLatest = ((SememeChronology)mapSetFieldsSememe.get()).getLatestVersion(DynamicSememeImpl.class, stampCoord);
			if (! existingVersionOptionalLatest.isPresent()) { // TODO Handle contradictions
				throw new RuntimeException("No latest version of mapSetFieldsSememe " + mapSetFieldsSememe.get().getNid() + " found for specified stamp coordinate " + stampCoord);
			}
			DynamicSememeData[] existingData = existingVersionOptionalLatest.get().value().getData();
			DynamicSememeArrayImpl<DynamicSememeStringImpl> mapSetFieldsSememeDataArray = (existingData != null && existingData.length > 0) ? (DynamicSememeArrayImpl<DynamicSememeStringImpl>)existingData[0] : null;
			if (
					mapSetFieldsSememeDataArray != null
					&& mapSetFieldsSememeDataArray.getDataArray() != null
					&& mapSetFieldsSememeDataArray.getDataArray().length > 0) {
				for (DynamicSememeStringImpl stringSememe : (DynamicSememeStringImpl[])mapSetFieldsSememeDataArray.getDataArray()) {
					String[] fieldComponents = stringSememe.getDataString().split(":");
					String id = fieldComponents[0];
					MapSetItemComponent componentType = MapSetItemComponent.valueOf(fieldComponents[1]);
					try {
						String description = null; // If passed description is null will use id to determine description
						if (componentType == MapSetItemComponent.ITEM_EXTENDED) {
							// If ITEM_EXTENDED then description is from itemFieldDefinitions
							int col = Integer.parseUnsignedInt(id);
							for (RestDynamicSememeColumnInfo def : itemFieldDefinitions) {
								if (def.columnOrder == col) {
									description = def.columnName;
									break;
								}
							}
							if (description == null) {
								String msg = "Failed correlating item display field id " + id + " for item display field of type " + componentType + " with any existing extended field definition in map set";
								log.error(msg);
								throw new RuntimeException(msg);
							}
						}

						fields.add(new RestMappingSetDisplayField(id, componentType, description));
					} catch (RestException e) {
						throw new RuntimeException("Failed constructing RestMappingSetField from stored data", e);
					}
				}
			}

			// Add defaults if empty
			if (fields.size() == 0) {
				fields.addAll(getDefaultDisplayFields(itemFieldDefinitions));
			}
		}

		return fields;
	}

	public static List<RestDynamicSememeColumnInfo> getItemFieldDefinitions(int mappingConceptNid) {
		List<RestDynamicSememeColumnInfo> mapItemFieldsDefinition = new ArrayList<>();
		
		//read the extended field definition information
		DynamicSememeUsageDescription dsud = DynamicSememeUsageDescriptionImpl.read(mappingConceptNid);
		//There are two columns used to store the target concept and qualifier, we shouldn't return them here.
		Positions positions;
		try
		{
			positions = Positions.getPositions(dsud);
		}
		catch (RestException e1)
		{
			throw new RuntimeException (e1);
		}
		
		int offset = 0;
		
		for (int i = 0; i < dsud.getColumnInfo().length; i++)
		{
			if (i == positions.targetPos || i == positions.qualfierPos)
			{
				offset++;
			}
			else
			{
				mapItemFieldsDefinition.add(new RestDynamicSememeColumnInfo(dsud.getColumnInfo()[i]));
				mapItemFieldsDefinition.get(i - offset).columnOrder = mapItemFieldsDefinition.get(i - offset).columnOrder - offset;
			}
		}
		
		return mapItemFieldsDefinition;
	}

	public static List<RestMappingSetDisplayField> getDefaultDisplayFields(List<RestDynamicSememeColumnInfo> mapItemFieldsDefinition) {
		List<RestMappingSetDisplayField> displayFields = new ArrayList<>();
		// If no display fields passed, then add default display fields
		// as SOURCE, TARGET and EQUIVALENCE_TYPE DESCRIPTION followed by item extended fields, if any
		String id = null;
		MapSetItemComponent componentType = null;
		try {
			id = MapSetDisplayFieldsService.Field.NonConceptFieldName.DESCRIPTION.name();
			componentType = MapSetItemComponent.SOURCE;
			displayFields.add(new RestMappingSetDisplayField(id, componentType, (String)null));
			id = MapSetDisplayFieldsService.Field.NonConceptFieldName.DESCRIPTION.name();
			componentType = MapSetItemComponent.TARGET;
			displayFields.add(new RestMappingSetDisplayField(id, componentType, (String)null));
			id = MapSetDisplayFieldsService.Field.NonConceptFieldName.DESCRIPTION.name();
			componentType = MapSetItemComponent.EQUIVALENCE_TYPE;
			displayFields.add(new RestMappingSetDisplayField(id, componentType, (String)null));

			for (RestDynamicSememeColumnInfo itemExtendedFieldCol : mapItemFieldsDefinition) {
				id = itemExtendedFieldCol.columnOrder + "";
				componentType = MapSetItemComponent.ITEM_EXTENDED;
				displayFields.add(new RestMappingSetDisplayField(id, Get.conceptService().getConcept(itemExtendedFieldCol.columnLabelConcept.nid), componentType, (String)null));
			}
		} catch (Exception e) {
			String msg = "Failed constructing default item display field id=\"" + id + "\", componentType=" + componentType;
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		
		return displayFields;
	}
}
