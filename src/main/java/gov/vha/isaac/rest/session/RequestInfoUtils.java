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

package gov.vha.isaac.rest.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RequestInfoUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class RequestInfoUtils {
	private RequestInfoUtils() {}

	public static void validateSingleParameterValue(Map<String, List<String>> params, String parameterName) throws RestException {
		if (params == null || params.get(parameterName) == null || params.get(parameterName).size() != 1) {
			throw new RestException(parameterName, params.get(parameterName) + "", "incorrect number (" + (params.get(parameterName) != null ? params.get(parameterName).size() : 0) + " of values. Expected exactly 1 value.");
		}
	}

	public static UUID parseUuidParameter(String parameterName, String str) throws RestException {
		try {
			return UUID.fromString(str);
		} catch (Exception e) {
			throw new RestException(parameterName, str, "invalid UUID " + parameterName + " parameter value: " + str);
		}
	}
	
	public static int parseIntegerParameter(String parameterName, String str) throws RestException {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new RestException(parameterName, str, "invalid integer " + parameterName + " parameter value: " + str);
		}
	}
	
	public static int getNidFromParameter(String parameterName, int nid) throws RestException {
		if (nid >= 0) {
			throw new RestException(parameterName, nid + "", "invalid " + parameterName + " NID parameter value: " + nid);
		}
		if (! Get.conceptService().hasConcept(nid) && ! Get.sememeService().hasSememe(nid)) {
			throw new RestException(parameterName, nid + "", "no concept or sememe exists corresponding to NID " + parameterName + " parameter value: " + nid);
		} else {
			return nid;
		}
	}
	public static int getNidFromUuidOrNidParameter(String parameterName, String str) throws RestException {
		try {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(str);

				if (Get.identifierService().hasUuid(uuid)) {
					Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept = Get.conceptService().getOptionalConcept(uuid);
					if (concept.isPresent()) {
						return concept.get().getNid();
					} else {
						int sememeSeq = Get.identifierService().getSememeSequenceForUuids(uuid);
						if (Get.sememeService().hasSememe(sememeSeq)) {
							return Get.identifierService().getSememeNid(sememeSeq);
						}
					}
				}
				else {
					throw new RestException(parameterName, str, "no concept or sememe exists corresponding to " + parameterName + " parameter value: " + str);
				}
			} catch (RestException ex) {
				throw ex;
			}
			catch (Exception e) {
				// ignore
			}
			int id = Integer.parseInt(str);
			if (id >= 0) {
				throw new RestException(parameterName, str, "invalid " + parameterName + " parameter value: " + str + ".  Must be a nid or UUID, not a sequence");
			}
			if (! Get.conceptService().hasConcept(id) && ! Get.sememeService().hasSememe(id)) {
				throw new RestException(parameterName, str, "no concept or sememe exists corresponding to " + parameterName + " parameter value: " + str);
			} else {
				return id;
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(parameterName, str, "invalid " + parameterName + " parameter value: " + str);
		}
	}
	
	public static int getConceptSequenceFromParameter(Map<String, List<String>> params, String parameterName) throws RestException {
		validateSingleParameterValue(params, parameterName);
		
		return getConceptSequenceFromParameter(parameterName, params.get(parameterName).iterator().next());
	}

	public static int getConceptSequenceFromParameter(String parameterName, String str) throws RestException {
		try {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(str);
				
				Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept = Get.conceptService().getOptionalConcept(uuid);
				
				if (concept.isPresent()) {
					return concept.get().getConceptSequence();
				}
			} catch (Exception e) {
				// ignore
			}
			int id = Integer.parseInt(str);
			if (! Get.conceptService().hasConcept(id)) {
				throw new RestException(parameterName, str, "no concept exists corresponding to integer concept sequence " + parameterName + " parameter value: " + str);
			} else {
				return Get.identifierService().getConceptSequence(id);
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(parameterName, str, "invalid integer concept sequence " + parameterName + " parameter value: " + str);
		}
	}

	public static int getSememeSequenceFromParameter(String parameterName, String str) throws RestException {
		try {
			UUID uuid = null;
			try {
				uuid = UUID.fromString(str);
				
				int sememeSequence = Get.identifierService().getSememeSequenceForUuids(uuid);
				if (Get.sememeService().hasSememe(sememeSequence)) {
					return sememeSequence;
				}
			} catch (Exception e) {
				// ignore
			}
			int id = Integer.parseInt(str);
			if (! Get.sememeService().hasSememe(id)) {
				throw new RestException(parameterName, str, "no sememe exists corresponding to id " + parameterName + " parameter value: " + str);
			} else {
				return Get.identifierService().getSememeSequence(id);
			}
		} catch (RestException e) {
			throw e;
		} catch (Exception e) {
			throw new RestException(parameterName, str, "invalid sememe id " + parameterName + " parameter value: " + str);
		}
	}

	public static boolean parseBooleanParameter(String parameterName, String str) throws RestException {
		if (str == null || (! str.equalsIgnoreCase("false") && ! str.equalsIgnoreCase("true"))) {
			throw new RestException(parameterName, str, "invalid boolean parameter value");
		} else {
			return Boolean.parseBoolean(str);
		}
	}

	public static boolean getBooleanFromParameters(String parameterName, Map<String, List<String>> parameters) throws RestException {
		if (parameters.get(parameterName).size() != 1) {
			throw new RestException(parameterName, null, "invalid boolean parameter value");
		}
		return parseBooleanParameter(parameterName, parameters.get(parameterName).get(0));
	}

	public static List<String> expandCommaDelimitedElements(String list) {
		List<String> expandedList = new ArrayList<>();
		
		if (list == null) {
			return null;
		}
		for (String s : list.trim().split(","))
		{
			if (StringUtils.isNotBlank(s))
			{
				expandedList.add(s.trim());
			}
		}
		
		return expandedList;
	}

	public static List<String> expandCommaDelimitedElements(List<String> list) {
		List<String> expandedList = new ArrayList<>();
	
		if (list == null) {
			return null;
		}
		for (String element : list) {
			if (element != null && element.contains(",")) {
				for (String s : element.trim().split(","))
				{
					if (StringUtils.isNotBlank(s))
					{
						expandedList.add(s.trim());
					}
				}
			} else {
				expandedList.add(element.trim());
			}
		}
		
		return expandedList;
	}

	/**
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @param names array of parameter collections, names or objects for which toString() is used
	 * @return
	 */
	public static Map<String, List<String>> getParametersSubset(Map<String, List<String>> params, Object...names) {
		Map<String,List<String>> paramSubset = new HashMap<>();
	
		for (Object param : names) {
			if (param instanceof Iterable) {
				// Passed a collection
				for (Object paramName : (Iterable<?>)param) {
					if (params.containsKey(paramName.toString()) && params.get(paramName.toString()) != null && params.get(paramName.toString()).size() > 0) {
						paramSubset.put(paramName.toString(), params.get(paramName.toString()));
					}
				}
			} else if (params.containsKey(param.toString()) && params.get(param.toString()) != null && params.get(param.toString()).size() > 0) {
				paramSubset.put(param.toString(), params.get(param.toString()));
			}
		}
	
		return paramSubset;
	}
}
