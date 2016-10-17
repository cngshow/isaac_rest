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
package gov.vha.isaac.rest.api1.system;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestSystemInfo;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.enumerations.RestConcreteDomainOperatorsType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;
import gov.vha.isaac.rest.api1.data.enumerations.RestNodeSemanticType;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSupportedIdType;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.systeminfo.RestIdentifiedObjectsResult;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link SystemAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.systemAPIsPathComponent)
public class SystemAPIs
{
	/**
	 * @param id The id for which to retrieve objects. May be a UUID, NID or sequence
	 * @param expand comma separated list of fields to expand.  Support depends on type of object identified by the passed id
	 * RestConceptChronology supports 'versionsAll', 'versionsLatestOnly'
	 * RestSememeChronology supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
	 * if they represent a concept or a description sememe.  
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken 
	 * may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.identifiedObjectsComponent + "{" + RequestParameters.id + "}")  
	public RestIdentifiedObjectsResult getIdentifiedObjects(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		Optional<UUID> processIdOptional = RequestInfoUtils.safeParseUuidParameter(processId);

		RestConceptChronology concept = null;
		RestSememeChronology sememe = null;
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (intId.get() < 0)
			{
				// id is NID
				ObjectChronologyType objectChronologyType = Get.identifierService().getChronologyTypeForNid(intId.get());
				switch(objectChronologyType) {
				case CONCEPT: {
					concept =
							new RestConceptChronology(
									Get.conceptService().getConcept(intId.get()),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									processIdOptional.isPresent() ? processIdOptional.get() : null);
					break;
				}
				case SEMEME:
					sememe =
							new RestSememeChronology(
									Get.sememeService().getSememe(intId.get()),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
					break;
				case UNKNOWN_NID:
				default:
					throw new RestException(RequestParameters.id, id, "Specified NID is for unsupported ObjectChronologyType " + objectChronologyType);
				}
				
				if (concept == null && sememe == null) {
					throw new RestException(RequestParameters.id, id, "Specified NID does not correspond to an existing concept or sememe");
				}

				return new RestIdentifiedObjectsResult(concept, sememe);
			}
			else
			{
				// id is either sememe or concept sequence

				int conceptNid = Get.identifierService().getConceptNid(intId.get());
				if (conceptNid != 0) {
					concept =
							new RestConceptChronology(
									Get.conceptService().getConcept(conceptNid),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									processIdOptional.isPresent() ? processIdOptional.get() : null);
				}

				int sememeNid = Get.identifierService().getSememeNid(intId.get());
				if (sememeNid != 0) {
					sememe =
							new RestSememeChronology(
									Get.sememeService().getSememe(sememeNid),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
				}
			}

			if (concept == null && sememe == null) {
				throw new RestException(RequestParameters.id, id, "Specified sequence does not correspond to an existing concept or sememe");
			}

			return new RestIdentifiedObjectsResult(concept, sememe);
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer nid = null;

				if (Get.identifierService().hasUuid(uuidId.get()) && (nid = Get.identifierService().getNidForUuids(uuidId.get())) != 0) {
					ObjectChronologyType objectChronologyType = Get.identifierService().getChronologyTypeForNid(nid);
				
					switch(objectChronologyType) {
					case CONCEPT:
						concept =
								new RestConceptChronology(
										Get.conceptService().getConcept(nid),
										RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
										RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
										processIdOptional.isPresent() ? processIdOptional.get() : null);
						break;
					case SEMEME:
						sememe =
								new RestSememeChronology(
										Get.sememeService().getSememe(nid),
										RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
										RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
										RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
										RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
						break;
					case UNKNOWN_NID:
					default:
						throw new RestException(RequestParameters.id, id, "Specified UUID is for NID " + nid + " for unsupported ObjectChronologyType " + objectChronologyType);
					}
					
					if (concept == null && sememe == null) {
						throw new RestException(RequestParameters.id, id, "Specified UUID is for NID " + nid + " that does not correspond to an existing concept or sememe");
					}

					return new RestIdentifiedObjectsResult(concept, sememe);
				} else {
					throw new RestException(RequestParameters.id, id, "No concept or sememe exists corresponding to the passed UUID id.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified string id is not a valid identifier.  Must be a UUID, or integer NID or sequence.");
			}
		}
	}
	
	/**
	 * Return the RestObjectChronologyType of the component corresponding to the passed id
	 * @param id The id for which to determine RestObjectChronologyType
	 * If an int < 0 then assumed to be a NID, else ambiguous and treated as a sememe or concept sequence, each of which may or may not correspond to existing components
	 * If a String then parsed and handled as a UUID of either a concept or sequence
	 * @return Map of RestObjectChronologyType to RestId.  Will contain exactly one entry if passed a UUID or NID, or one or two entries if passed a sequence. if no 
	 * corresponding ids found a RestException is thrown.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a 
	 * separate (prior) call to getCoordinatesToken().
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.objectChronologyTypeComponent + "{" + RequestParameters.id + "}")  
	public RestObjectChronologyType getObjectChronologyType(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES);

		RestObjectChronologyType returnedType = null;
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (intId.get() < 0)
			{
				// id is NID
				returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(intId.get()));
			}
			else
			{
				// id is either sememe or concept sequence

				int conceptNid = Get.identifierService().getConceptNid(intId.get());
				if (conceptNid != 0) {
					returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(conceptNid));
				}

				int sememeNid = Get.identifierService().getSememeNid(intId.get());
				if (sememeNid != 0) {
					if (returnedType != null) {
						throw new RestException(RequestParameters.id, id, "Specified int id is ambiguous, as it may be either a sememe or concept sequence. "
								+" Must be a UUID, or integer NID or sequence that uniquely identifies either a sememe or concept, but not both.");
					}
					returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(sememeNid));
				}
			}

			if (returnedType != null) {
				return returnedType;
			} else {
				throw new RestException(RequestParameters.id, id, "Specified int id is not a valid NID or sequence. Must be a UUID, or integer NID or sequence "
						+"that uniquely identifies either a sememe or concept, but not both.");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer nid = null;

				if (Get.identifierService().hasUuid(uuidId.get()) && (nid = Get.identifierService().getNidForUuids(uuidId.get())) != 0) {
					return returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(nid));
				} else {
					throw new RestException(RequestParameters.id, id, "No concept or sememe exists corresponding to the passed UUID id.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified string id is not a valid identifier.  Must be a UUID, or integer NID or sequence.");
			}
		}
	}

	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeDataTypeComponent)  
	public RestDynamicSememeDataType[] getRestDynamicSememeDataTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestDynamicSememeDataType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeValidatorTypeComponent)  
	public RestDynamicSememeValidatorType[] getRestDynamicSememeValidatorTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestDynamicSememeValidatorType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestObjectChronologyTypeComponent)
	public RestObjectChronologyType[] getRestObjectChronologyTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestObjectChronologyType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSememeTypeComponent)
	public RestSememeType[] getRestObjectSememeTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestSememeType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestConcreteDomainOperatorTypes)
	public RestConcreteDomainOperatorsType[] getRestConcreteDomainOperatorTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestConcreteDomainOperatorsType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestNodeSemanticTypes)
	public RestNodeSemanticType[] getRestNodeSemanticTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestNodeSemanticType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSupportedIdTypes)
	public RestSupportedIdType[] getRestSupportedIdTypes() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return RestSupportedIdType.getAll();
	}

	/**
	 * ISAAC, REST API and related DB metadata.  These values are cached.
	 * TODO move functionality in getSystemInfo() into OCHRE
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.systemInfoComponent)
	public RestSystemInfo getSystemInfo() throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);

		return ApplicationConfig.getInstance().getSystemInfo();
	}
}
