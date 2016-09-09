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
package gov.vha.isaac.rest;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.RequestInfo;

public class Util
{
	public static int convertToConceptSequence(String conceptId) throws RestException
	{
		Optional<UUID> uuidId = UUIDUtil.getUUID(conceptId);
		Optional<Integer> sequence = Optional.empty();
		if (uuidId.isPresent())
		{
			if (Get.identifierService().hasUuid(uuidId.get()))
			{
				Optional<? extends ConceptChronology<?>> con = Get.conceptService().getOptionalConcept(uuidId.get());
				if (!con.isPresent())
				{
					throw new RestException("The UUID '" + conceptId + "' is known by the system, but it is not a concept (perhaps a sememe)");
				}
				sequence = Optional.of(con.get().getConceptSequence());
			}
			else
			{
				throw new RestException("The UUID '" + conceptId + "' Is not known by the system");
			}
		}
		else
		{
			sequence = NumericUtils.getInt(conceptId);
			if (sequence.isPresent() && sequence.get() < 0)
			{
				sequence = Optional.of(Get.identifierService().getConceptSequence(sequence.get()));
			}
		}
		
		if (!sequence.isPresent())
		{
			throw new RestException("The value '" + conceptId + "' does not appear to be a UUID or a nid");
		}
		
		return sequence.get();
	}
	
	public static UUID convertToConceptUUID(String conceptId) throws RestException
	{
		Optional<UUID> uuid = UUIDUtil.getUUID(conceptId);
		if (uuid.isPresent())
		{
			if (Get.identifierService().hasUuid(uuid.get()) && Get.conceptService().getOptionalConcept(uuid.get()).isPresent())
			{
				return uuid.get();
			}
			else
			{
				throw new RestException("The UUID '" + conceptId + "' Is not known by the system");
			}
		}
		else
		{
			Optional<Integer> numId = NumericUtils.getInt(conceptId);
			if (numId.isPresent() && numId.get() < 0)
			{
				if (numId.get() < 0)
				{
					uuid = Get.identifierService().getUuidPrimordialForNid(numId.get());
					if (uuid.isPresent())
					{
						return uuid.get();
					}
					else
					{
						throw new RestException("The nid '" + conceptId + "' is not known by the system");
					}
				}
				else
				{
					Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = Get.conceptService().getOptionalConcept(numId.get());
					if (c.isPresent())
					{
						return c.get().getPrimordialUuid();
					}
					else
					{
						throw new RestException("The concept sequence '" + conceptId + "' is not known by the system");
					}
				}
			}
			else
			{
				throw new RestException("The id '" + conceptId + "' does not appear to be a valid UUID, NID or Concept Sequence");
			}
		}
	}
	
	public static int convertToNid(String id) throws RestException
	{
		Optional<UUID> uuidId = UUIDUtil.getUUID(id);
		Optional<Integer> nid = Optional.empty();
		if (uuidId.isPresent())
		{
			if (Get.identifierService().hasUuid(uuidId.get()))
			{
				nid = Optional.of(Get.identifierService().getNidForUuids(uuidId.get()));
			}
			else
			{
				throw new RestException("The UUID '" + id + "' Is not known by the system");
			}
		}
		else
		{
			nid = NumericUtils.getInt(id);
			if (nid.isPresent() && nid.get() > 0)
			{
				throw new RestException("The sequence id '" + id + "' cannot be turned into a nid");
			}
		}
		
		if (!nid.isPresent())
		{
			throw new RestException("The value '" + nid + "' does not appear to be a UUID or a nid");
		}
		
		return nid.get();
	}
	
	/**
	 * Utility method to find the 'best' description for the concept at hand.
	 * @param conceptId (nid or sequence)
	 * @return
	 */
	public static String readBestDescription(int conceptId)
	{
		Optional<LatestVersion<DescriptionSememe<?>>> descriptionOptional = Optional.empty();
		
		int conceptNid = Get.identifierService().getConceptNid(conceptId);
		
		if (RequestInfo.get().useFsn())
		{
			descriptionOptional = RequestInfo.get().getLanguageCoordinate().getFullySpecifiedDescription(
				Get.sememeService().getDescriptionsForComponent(conceptNid).collect(Collectors.toList()), RequestInfo.get().getStampCoordinate());
		}
		
		if (!descriptionOptional.isPresent())
		{
			descriptionOptional = RequestInfo.get().getLanguageCoordinate().getPreferredDescription(
				Get.sememeService().getDescriptionsForComponent(conceptNid).collect(Collectors.toList()), RequestInfo.get().getStampCoordinate());
		}
		
		if (descriptionOptional.isPresent())
		{
			if (descriptionOptional.get().contradictions().isPresent())
			{
				//Prefer active descriptions over inactive, if there was a contradiction (which means they tied the sort - have the same time)
				//common for a replacement description to have the same time as the retired one.
				if (descriptionOptional.get().value().getState() == State.ACTIVE)
				{
					return descriptionOptional.get().value().getText();
				}
				else
				{
					for (DescriptionSememe<?> ds : descriptionOptional.get().contradictions().get())
					{
						if (ds.getState() == State.ACTIVE)
						{
							return ds.getText();
						}
					}
				}
			}
			return descriptionOptional.get().value().getText();
		}
		else
		{
			return null;
		}
	}
}
