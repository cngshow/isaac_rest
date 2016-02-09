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

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.session.RequestInfo;


/**
 * {@link IdAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.idPathComponent)
public class IdAPIs
{
	public static enum IDType {uuid, nid, conceptSequence, sememeSequence, sctid, vuid};
	private static Logger log = LogManager.getLogger();
	
	/**
	 * Translate an ID from one type to another.  
	 * TODO still need to define how to pass in a version parameter
	 * @param id The id to translate
	 * @param inputType - should be one of the types from the supportedTypes call.  Currently includes [uuid, nid, sequence, sctid, vuid]
	 * If not specified, selects the type as follows.  
	 * UUIDs - if it is a correctly formatted UUID.  
	 * If negative - a nid.  All other values are ambiguous, and the type must be input.  An error will be thrown.
	 * @param outputType -  should be one of the types from the supportedTypes call.  Currently includes [uuid, nid, sequence, sctid, vuid].
	 * Defaults to uuid.
	 * @return the converted ID, if possible.  Otherwise, a RestException, if no translation is possible.  Note that for some id types, 
	 * the translation may depend on the STAMP!
	 * @throws RestException
	 */
	@SuppressWarnings("rawtypes")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idTranslateComponent + "{id}")  
	public String translateId(@PathParam("id") String id, @QueryParam("inputType") String inputType, 
		@QueryParam("outputType") @DefaultValue("uuid") String outputType) throws RestException
	{
		IDType inputTypeFormat = read(inputType).orElse(IDType.uuid);
		Optional<? extends ObjectChronology> object = Optional.empty();
		switch (inputTypeFormat)
		{
			case vuid:
				//TODO implement vuid translation
				throw new RestException("vuid input type not yet supported");
			case sctid:
				long l = NumericUtils.getLong(id).orElse(0l);
				Optional<Integer> nid = Frills.getNidForSCTID(l);
				if (nid.isPresent())
				{
					object = Get.identifiedObjectService().getIdentifiedObjectChronology(nid.get());
				}
				break;
			case conceptSequence:
				object = Get.conceptService().getOptionalConcept(NumericUtils.getInt(id).orElse(0));
				break;
			case sememeSequence:
				object = Get.sememeService().getOptionalSememe(NumericUtils.getInt(id).orElse(0));
				break;
			case uuid: case nid:
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
			IDType outputTypeFormat = read(outputType).orElse(IDType.uuid);
			switch (outputTypeFormat)
			{
				case nid:
					return object.get().getNid() + "";
				case sctid:
					return "" + Frills.getSctId(object.get().getNid(), RequestInfo.get().getStampCoordinate()).
						orElseThrow(() -> new RestException("No SCTID was found on the specified component"));
				case conceptSequence:
					if (object.get().getOchreObjectType() == OchreExternalizableObjectType.CONCEPT)
					{
						return ((ConceptChronology)object.get()).getConceptSequence() + "";
					}
					else
					{
						throw new RestException("The found object was of type " + object.get().getOchreObjectType() + " which cannot have a concept sequence");
					}
				case sememeSequence:
					if (object.get().getOchreObjectType() == OchreExternalizableObjectType.SEMEME)
					{
						return ((SememeChronology)object.get()).getSememeSequence() + "";
					}
					else
					{
						throw new RestException("The found object was of type " + object.get().getOchreObjectType() + " which cannot have a sememe sequence");
					}
				case uuid:
					return object.get().getPrimordialUuid().toString();
				case vuid:
					//TODO implement vuid translation
					throw new RestException("Not yet implemented");

				default :
					log.error("Design error - case not handled: " + inputTypeFormat);
					throw new RestException("Internal server error");
			}
		}
		else
		{
			throw new RestException("id", id, "Unable to locate an object with the given id.");
		}
	}
	

	/**
	 * @return The translation types supported by this server, suitable for use in the translate call, as either an inputType or an outputType
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idTypesComponent)  
	public String[] getSupportedTypes() throws RestException
	{
		String[] temp = new String[IDType.values().length];
		for (IDType idt : IDType.values())
		{
			temp[idt.ordinal()] = idt.name();
		}
		return temp;
	}
	
	private Optional<IDType> read(String input)
	{
		if (StringUtils.isNotBlank(input))
		{
			for (IDType idt : IDType.values())
			{
				if (input.trim().equalsIgnoreCase(idt.name()))
				{
					return Optional.of(idt);
				}
			}
		}
		return Optional.empty();
	}
}
