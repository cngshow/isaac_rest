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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.workflow.WorkflowUtils;
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
		
		descriptionOptional = RequestInfo.get().getLanguageCoordinate().getDescription(
				Get.sememeService().getDescriptionsForComponent(conceptNid).collect(Collectors.toList()),
				RequestInfo.get().getStampCoordinate());
		
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
	
	/**
	 * Return the latest version of each unique comment attached to an object, sorted from oldest to newest.
	 * @param id the nid of UUID of the object to check for comments on
	 * @return The comment(s)
	 * @throws RestException
	 */
	public static ArrayList<RestCommentVersion> readComments(String id, UUID processId) throws RestException
	{
		ArrayList<RestCommentVersion> results = new ArrayList<>();
		
		Get.sememeService().getSememesForComponentFromAssemblage(Util.convertToNid(id), DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getConceptSequence())
			.forEach(sememeChronology -> 
			{
				@SuppressWarnings("rawtypes")
				Optional<DynamicSememe> version = Optional.empty();
				try {
					version = WorkflowUtils.getStampedVersion(DynamicSememe.class, processId, sememeChronology.getNid());
				} catch (Exception e) {
					// TODO Joel ignore exception thrown by getStampedVersion()?
				}
				if (version.isPresent())
				{
					//TODO handle contradictions
					results.add(new RestCommentVersion(version.get()));
				}
			});
		
		results.sort(new Comparator<RestCommentVersion>()
		{
			@Override
			public int compare(RestCommentVersion o1, RestCommentVersion o2)
			{
				return Long.compare(o1.getCommentStamp().time, o2.getCommentStamp().time);
			}
		});
		return results;
	}
}
