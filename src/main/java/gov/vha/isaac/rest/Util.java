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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;

public class Util
{
	public static <E extends Enum<E>> byte[] byteArrayFromEnumSet(EnumSet<E> set) {
		byte[] returnValue = new byte[set.size()];
		int index = 0;
		for (Iterator<E> it = set.iterator(); it.hasNext();) {
			returnValue[index++] = (byte)it.next().ordinal();
		}
		
		return returnValue;
	}
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
}
