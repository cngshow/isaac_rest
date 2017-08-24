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

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;

public class Util
{
	public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());
	private static Logger log = LogManager.getLogger();
	
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
	
	/**
	 * Handles UUIDs or nids (not sequences)
	 * @param id
	 * @return
	 * @throws RestException
	 */
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
		return readBestDescription(conceptId, RequestInfo.get().getStampCoordinate());
	}

	public static String readBestDescription(int conceptId, StampCoordinate sc)
	{
		return readBestDescription(conceptId, sc, RequestInfo.get().getLanguageCoordinate());
	}

	public static String readBestDescription(int conceptId, StampCoordinate sc, LanguageCoordinate lc)
	{
		Optional<LatestVersion<DescriptionSememe<?>>> descriptionOptional = Optional.empty();
		
		int conceptNid = Get.identifierService().getConceptNid(conceptId);
		
		descriptionOptional = lc.getDescription(
				Get.sememeService().getDescriptionsForComponent(conceptNid).collect(Collectors.toList()),
				sc);
		
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
	 * @param workflowProcessId - The optional workflowProcessId.  If non-blank, this must be a valid process id.
	 * @return - the validated UUID, or null, if no workflowProcessId is submitted.
	 * @throws RestException - if the provided non-blank value isn't valid.
	 */
	public static UUID validateWorkflowProcess(String workflowProcessId) throws RestException
	{
		Optional<UUID> processIdOptional = RequestInfoUtils.parseUuidParameterIfNonBlank(RequestParameters.processId, workflowProcessId);
		if (processIdOptional.isPresent())
		{
			UUID temp = processIdOptional.get();
			if (RequestInfo.get().getWorkflow().getWorkflowAccessor().getProcessDetails(temp) == null)
			{
				throw new RestException(RequestParameters.processId, workflowProcessId, "Not a valid workflow process");
			}
			return temp;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Calls {@link #getPreWorkflowStampCoordinate(UUID, int)} after calling {@link #validateWorkflowProcess(String)}
	 * @param workflowProcessId
	 * @param componentNid
	 * @return
	 * @throws RestException 
	 */
	public static StampCoordinate getPreWorkflowStampCoordinate(String workflowProcessId, int componentNid) throws RestException
	{
		return getPreWorkflowStampCoordinate(workflowProcessId, componentNid, RequestInfo.get().getStampCoordinate());
	}
	public static StampCoordinate getPreWorkflowStampCoordinate(String workflowProcessId, int componentNid, StampCoordinate sc) throws RestException
	{
		return getPreWorkflowStampCoordinate(validateWorkflowProcess(workflowProcessId), componentNid, sc);
	}
	
	
	/**
	 * If a workflowProcessId is passed, and the componentNid is present in the workflow, return the stamp
	 * that occurred prior to the first change in workflow.  Otherwise, returns the user specified stamp coordinate, from {@link RequestInfo#getStampCoordinate()}
	 * 
	 * @param workflowProcessId - the id of the workflow process.  If not provided, this method returns the result of {@link RequestInfo#getStampCoordinate()}
	 * @param componentNid - the component to check for in the workflow.  The componentNid must be a valid component identifier.
	 * If the component is not found in the workflow, the result of this method is simply {@link RequestInfo#getStampCoordinate()}
	 * @throws RestException if the provided processId is invalid
	 */
	public static StampCoordinate getPreWorkflowStampCoordinate(UUID workflowProcessId, int componentNid)
	{
		return getPreWorkflowStampCoordinate(workflowProcessId, componentNid, RequestInfo.get().getStampCoordinate());
	}

	public static StampCoordinate getPreWorkflowStampCoordinate(UUID workflowProcessId, int componentNid, StampCoordinate sc)
	{
		if (componentNid >= 0) {
			throw new RuntimeException("Internal error - sequence passed where nid required: " + componentNid);
		}
		if (workflowProcessId == null)
		{
			return RequestInfo.get().getStampCoordinate();
		}
		else
		{
			if (RequestInfo.get().getWorkflow().getWorkflowAccessor().isComponentInProcess(workflowProcessId, componentNid))
			{
				StampedVersion version = RequestInfo.get().getWorkflow().getWorkflowAccessor().getVersionPriorToWorkflow(workflowProcessId, componentNid);
				return Frills.getStampCoordinateFromVersion(version);
			}
			else
			{
				return sc;
			}
		}
	}

	/**
	 * @param dateString - if null or blank, returns 0.  
	 * if long, parsed as a java time.  
	 * If "latest" - set to Long.MAX_VALUE.  
	 * Otherwise, parsed as {@link DateTimeFormatter#ISO_DATE_TIME}
	 */
	public static long parseDate(String dateString) throws DateTimeParseException
	{
		Optional<Long> l = NumericUtils.getLong(dateString);
		if (l.isPresent())
		{
			return l.get();
		}
		else
		{
			if (StringUtils.isBlank(dateString))
			{
				return 0;
			}
			if (dateString.trim().toLowerCase(Locale.ENGLISH).equals("latest"))
			{
				return Long.MAX_VALUE;
			}
			else
			{
				return Date.from(Instant.from(ISO_DATE_TIME_FORMATTER.parse(dateString))).getTime();
			}
		}
	}
	
	public static InputStream getTerminologyConfigData()
	{
		//Prisme injects this into the war file, at deployment time.
		log.debug("Looking for TerminologyConfig.xml from prisme");
		InputStream is = Util.class.getClassLoader().getResourceAsStream("/prisme_files/TerminologyConfig.xml");
		if (is == null)
		{
			log.warn("Failed to find TerminologyConfig.xml from prisme!  Using embedded default config!");
			//this file comes from the vhat-constants module
			is = Util.class.getClassLoader().getResourceAsStream("/TerminologyConfigDefault.xml");
		}
		if (is == null)
		{
			throw new RuntimeException("Unable to find Terminology Config!");
		}
		return is;
	}
	
	public static InputStream getTerminologyConfigSchema()
	{
		//Prisme injects this into the war file, at deployment time.
		log.debug("Looking for TerminologyConfig.xsd from prisme");
		InputStream is = Util.class.getClassLoader().getResourceAsStream("/prisme_files/TerminologyConfig.xsd");
		if (is == null)
		{
			log.warn("Failed to find TerminologyConfig.xsd from prisme!  Using embedded default config!");
			//this file comes from the vhat-constants module
			is = Util.class.getClassLoader().getResourceAsStream("/TerminologyConfig.xsd.hidden");
		}
		if (is == null)
		{
			throw new RuntimeException("Unable to find Terminology Config Schema!!");
		}
		return is;
	}
}
