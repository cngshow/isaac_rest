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
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.webcohesion.enunciate.metadata.Facet;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.SystemInfo;
import gov.vha.isaac.rest.api1.data.enumerations.RestConcreteDomainOperatorsType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;
import gov.vha.isaac.rest.api1.data.enumerations.RestNodeSemanticType;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSupportedIdType;
import gov.vha.isaac.rest.api1.data.logic.RestConceptNode;
import gov.vha.isaac.rest.api1.data.logic.RestFeatureNode;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeBoolean;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeFloat;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeInstant;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeInteger;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeString;
import gov.vha.isaac.rest.api1.data.logic.RestRoleNode;
import gov.vha.isaac.rest.api1.data.logic.RestTypedConnectorNode;
import gov.vha.isaac.rest.api1.data.logic.RestUntypedConnectorNode;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeTypedData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeBoolean;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeByteArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeDouble;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeFloat;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link SystemAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.systemPathComponent)
public class SystemAPIs
{
	/**
	 * Return the RestObjectChronologyType of the component corresponding to the passed id
	 * @param id The id for which to determine RestObjectChronologyType
	 * If an int < 0 then assumed to be a NID, else ambiguous and treated as a sememe or concept sequence, each of which may or may not correspond to existing components
	 * If a String then parsed and handled as a UUID of either a concept or sequence
	 * @return Map of RestObjectChronologyType to RestId.  Will contain exactly one entry if passed a UUID or NID, or one or two entries if passed a sequence. if no corresponding ids found a RestException is thrown.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.objectChronologyTypeComponent + "{id}")  
	public RestObjectChronologyType getObjectChronologyType(@PathParam("id") String id) throws RestException
	{
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
						throw new RestException(RequestParameters.id, id, "Specified int id is ambiguous, as it may be either a sememe or concept sequence. Must be a UUID, or integer NID or sequence that uniquely identifies either a sememe or concept, but not both.");
					}
					returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(sememeNid));
				}
			}

			if (returnedType != null) {
				return returnedType;
			} else {
				throw new RestException(RequestParameters.id, id, "Specified int id is not a valid NID or sequence. Must be a UUID, or integer NID or sequence that uniquely identifies either a sememe or concept, but not both.");
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
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeDataTypeComponent)  
	public RestDynamicSememeDataType[] getRestDynamicSememeDataTypes()
	{
		return RestDynamicSememeDataType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeValidatorTypeComponent)  
	public RestDynamicSememeValidatorType[] getRestDynamicSememeValidatorTypes()
	{
		return RestDynamicSememeValidatorType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestObjectChronologyTypeComponent)
	public RestObjectChronologyType[] getRestObjectChronologyTypes()
	{
		return RestObjectChronologyType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSememeTypeComponent)
	public RestSememeType[] getRestObjectSememeTypes()
	{
		return RestSememeType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestConcreteDomainOperatorTypes)
	public RestConcreteDomainOperatorsType[] getRestConcreteDomainOperatorTypes()
	{
		return RestConcreteDomainOperatorsType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestNodeSemanticTypes)
	public RestNodeSemanticType[] getRestNodeSemanticTypes()
	{
		return RestNodeSemanticType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSupportedIdTypes)
	public RestSupportedIdType[] getRestSupportedIdTypes()
	{
		return RestSupportedIdType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.systemInfoComponent)
	public SystemInfo getSystemInfo()
	{
		return new SystemInfo();
	}
	
	//TODO the code below this point (noop, class Z) is a hack workaround for the bug 
	//https://github.com/stoicflame/enunciate/issues/336
	/**
	 * This is not a valid operation.  Do not call.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/noop")
	@Facet ("ignore")  //prevent enunciate from documenting this method
	public Z noop() throws RestException
	{
		throw new RestException("These are not the droids you are looking for");
	}
	
	/**
	 * The {@link Z} object will never be returned by an API call.
	 */
	//Any class that is only referenced by an @XmlSeeAlso should be referenced here, otherwise
	//the generated ruby library code will miss the class.
	@XmlRootElement
	@Facet ("ignore")
	private class Z
	{
		//Put a reference here to any class that is only referenced by an @XmlSeeAlso
		@XmlElement RestConceptNode a1 = null;
		@XmlElement RestUntypedConnectorNode a2 = null;
		@XmlElement RestTypedConnectorNode a3 = null;
		@XmlElement RestLiteralNodeBoolean a4 = null;
		@XmlElement RestLiteralNodeInteger a5 = null;
		@XmlElement RestLiteralNodeFloat a6 = null;
		@XmlElement RestLiteralNodeString a7 = null;
		@XmlElement RestLiteralNodeInstant a8 = null;
		@XmlElement RestRoleNode a9 = null;
		@XmlElement RestDynamicSememeValidatorType a10 = null;
		@XmlElement RestDynamicSememeDataType a11 = null;
		@XmlElement RestObjectChronologyType a12 = null;
		@XmlElement RestSememeType a13 = null;
		@XmlElement RestFeatureNode a14 = null;
		@XmlElement RestSememeDescriptionVersion a15 = null;
		@XmlElement RestDynamicSememeVersion a16 = null;
		@XmlElement RestSememeLogicGraphVersion a17 = null;
		@XmlElement RestDynamicSememeVersion a19 = null;
		@XmlElement RestDynamicSememeArray a20 = null;
		@XmlElement RestDynamicSememeBoolean a21 = null;
		@XmlElement RestDynamicSememeByteArray a22 = null;
		@XmlElement RestDynamicSememeDouble a23 = null;
		@XmlElement RestDynamicSememeFloat a24 = null;
		@XmlElement RestDynamicSememeInteger a25 = null;
		@XmlElement RestDynamicSememeLong a26 = null;
		@XmlElement RestDynamicSememeNid a27 = null;
		@XmlElement RestDynamicSememeSequence a28 = null;
		@XmlElement RestDynamicSememeString a29 = null;
		@XmlElement RestDynamicSememeUUID a30 = null;
		@XmlElement RestDynamicSememeTypedData a31 = null;
	}
}
