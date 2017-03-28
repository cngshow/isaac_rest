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
package gov.vha.isaac.rest.api1.id;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestId;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.enumerations.IdType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSupportedIdType;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link IdAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.idAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class IdAPIs
{
	private static Logger log = LogManager.getLogger();

	@Context
	private SecurityContext securityContext;


	/**
	 * Translate an ID from one type to another.  
	 * @param id The id to translate
	 * @param inputType - should be one of the types from the supportedTypes call.  You can pass the name or enumId of the 
	 * returned RestIdType object.  This will be something like [uuid, nid, conceptSequence, sememeSequence, sctid, vuid]
	 * If not specified, selects the type as follows.  
	 * UUIDs - if it is a correctly formatted UUID.  
	 * If negative - a nid.  All other values are ambiguous, and the type must be input.  An error will be thrown.
	 * @param outputType -  should be one of the types from the supportedTypes call.   You can pass the name or enumId of the 
	 * returned RestIdType object.  Currently includes [uuid, nid, conceptSequence, sememeSequence, sctid, vuid].
	 * Defaults to uuid.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the converted ID, if possible.  Otherwise, a RestException, if no translation is possible.  Note that for some id types, 
	 * the translation may depend on the STAMP!
	 * @throws RestException
	 */
	@SuppressWarnings("rawtypes")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idTranslateComponent + "{" + RequestParameters.id + "}")  
	public RestId translateId(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.inputType) String inputType, 
			@QueryParam(RequestParameters.outputType) @DefaultValue("uuid") String outputType,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.inputType,
				RequestParameters.outputType,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		IdType inputTypeFormat = IdType.parse(inputType).orElse(IdType.UUID);
		Optional<? extends ObjectChronology> object = Optional.empty();
		switch (inputTypeFormat)
		{
			case VUID: {
				long l = NumericUtils.getLong(id).orElse(0l);
				Optional<Integer> nid = Frills.getNidForVUID(l);
				if (nid.isPresent())
				{
					object = Get.identifiedObjectService().getIdentifiedObjectChronology(nid.get());
				}
				break;
			}
			case SCTID: {
				long l = NumericUtils.getLong(id).orElse(0l);
				Optional<Integer> nid = Frills.getNidForSCTID(l);
				if (nid.isPresent())
				{
					object = Get.identifiedObjectService().getIdentifiedObjectChronology(nid.get());
				}
				break;
			}
			case CONCEPT_SEQUENCE:
				object = Get.conceptService().getOptionalConcept(NumericUtils.getInt(id).orElse(0));
				break;
			case SEMEME_SEQUENCE:
				object = Get.sememeService().getOptionalSememe(NumericUtils.getInt(id).orElse(0));
				break;
			case UUID: case NID:
				//If not specified, we default it to uuid, even if it is a nid, so check if it is a nid
				Optional<UUID> uuid = UUIDUtil.getUUID(id);
				if (uuid.isPresent())
				{
					if (Get.identifierService().hasUuid(uuid.get()))
					{
						object = Get.identifiedObjectService().getIdentifiedObjectChronology(Get.identifierService().getNidForUuids(uuid.get()));
					}
				}
				else if (NumericUtils.isNID(id))
				{
					object = Get.identifiedObjectService().getIdentifiedObjectChronology(NumericUtils.getNID(id).get());
				}
				if (!object.isPresent() && StringUtils.isBlank(id))
				{
					throw new RestException("inputType", "must be provided in cases where the 'id' value is ambiguous");
				}
				break;

			default :
				log.error("Design error - case not handled: " + inputTypeFormat);
				throw new RestException("Internal server error");
		}
		
		if (object.isPresent())
		{
			IdType outputTypeFormat = IdType.parse(outputType).orElse(IdType.UUID);
			switch (outputTypeFormat)
			{
				case NID:
					return new RestId(outputTypeFormat, object.get().getNid() + "");
				case SCTID:
					/*
					return new RestId(outputTypeFormat, "" + Frills.getSctId(object.get().getNid(), RequestInfo.get().getStampCoordinate()).
							orElseThrow(() -> new RestException("No SCTID was found on the specified component")));
					*/
					Optional<Long> sctId = Frills.getSctId(object.get().getNid(), RequestInfo.get().getStampCoordinate());
					if (!sctId.isPresent())
					{
						throw new RestException("No SCTID was found on the specified component");
					}
					return new RestId(outputTypeFormat, "" + sctId);
				case CONCEPT_SEQUENCE:
					if (object.get().getOchreObjectType() == OchreExternalizableObjectType.CONCEPT)
					{
						return new RestId(outputTypeFormat, ((ConceptChronology)object.get()).getConceptSequence() + "");
					}
					else
					{
						throw new RestException("The found object was of type " + object.get().getOchreObjectType() + " which cannot have a concept sequence");
					}
				case SEMEME_SEQUENCE:
					if (object.get().getOchreObjectType() == OchreExternalizableObjectType.SEMEME)
					{
						return new RestId(outputTypeFormat, ((SememeChronology)object.get()).getSememeSequence() + "");
					}
					else
					{
						throw new RestException("The found object was of type " + object.get().getOchreObjectType() + " which cannot have a sememe sequence");
					}
				case UUID:
					return new RestId(outputTypeFormat, object.get().getPrimordialUuid().toString());
				case VUID:
					/*
					return new RestId(outputTypeFormat, "" + Frills.getVuId(object.get().getNid(), RequestInfo.get().getStampCoordinate()).
							orElseThrow(() -> new RestException("No VUID was found on the specified component")));
					*/
					Optional<Long> vuId = Frills.getVuId(object.get().getNid(), RequestInfo.get().getStampCoordinate());
					if (!vuId.isPresent())
					{
						throw new RestException("No VUID was found on the specified component");
					}
					return new RestId(outputTypeFormat, "" + vuId);
				default :
					log.error("Design error - case not handled: " + inputTypeFormat);
					throw new RestException("Internal server error");
			}
		}
		else
		{
			throw new RestException(RequestParameters.id, id, "Unable to locate an object with the given id");
		}
	}

	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idTypesComponent)  
	public RestSupportedIdType[] getSupportedTypes() throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.coordToken);
	
		return RestSupportedIdType.getAll();
	}

	/**
	 */
	/**
	 * Enumerate the supported ID concepts for the system.  In addition to {@code expand}, accepts coordinate token and/or parameters
	 * 
	 * @param expand - concept-specific expandable parameters
	 * 
	 * @return RestConceptChronology[] - Array of {@link RestConceptChronology} representing identifier concepts
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idsComponent)  
	public RestConceptChronology[] getSupportedIdConcepts() throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		Set<ConceptChronology<?>> identifierAnnotatedConcepts = new HashSet<>();
		
		Stream<SememeChronology<? extends SememeVersion<?>>> identifierAnnotationSememeChronologyStream = Get.sememeService().getSememesFromAssemblage(MetaData.IDENTIFIER_SOURCE.getConceptSequence());
		identifierAnnotationSememeChronologyStream.sequential().forEach(identifierAnnotationSememeChronology -> {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<SememeVersionImpl>> identifierAnnotationSememeLatestOptional = ((SememeChronology)identifierAnnotationSememeChronology).getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
			if (identifierAnnotationSememeLatestOptional.isPresent()) {
				// TODO handle contradictions
				@SuppressWarnings("rawtypes")
				SememeVersionImpl identifierAnnotationSememe = identifierAnnotationSememeLatestOptional.get().value();
				identifierAnnotatedConcepts.add(Get.conceptService().getConcept(identifierAnnotationSememe.getReferencedComponentNid()));
			}
		});
		
		RestConceptChronology[] arrayToReturn = new RestConceptChronology[identifierAnnotatedConcepts.size()];
		
		int i = 0;
		for (ConceptChronology<? extends ConceptVersion<?>> idConcept : identifierAnnotatedConcepts) {
			arrayToReturn[i++] = new RestConceptChronology(idConcept, false, true, (UUID)null);
		}

		return arrayToReturn;
	}
}
