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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.tokens.CoordinatesToken;
import gov.vha.isaac.rest.tokens.CoordinatesTokens;

/**
 * 
 * {@link CoordinatesUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CoordinatesUtil {
	private CoordinatesUtil() {}

	/**
	 * Used to hash CoordinatesToken object and serialized string by request parameters
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return string representation of parameter name to value-list map
	 */
	public static String encodeCoordinateParameters(Map<String, List<String>> params) {
		Map<String,List<String>> coordinateParams = getCoordinateParameters(params);
		
		StringBuilder sb = new StringBuilder(coordinateParams.size() * 32);
		for (Map.Entry<String, List<String>> entry : coordinateParams.entrySet()) {
			String key = entry.getKey();
			List<String> parameterValueList = entry.getValue();
			Collections.sort(parameterValueList);
			
			sb.append(key + ':');
			for (int i = 0; i < parameterValueList.size(); ++i) {
				sb.append(parameterValueList.get(i));
				if (i < (parameterValueList.size() - 1)) {
					sb.append(',');
				}
			}
			sb.append(';');
		}
		
		return sb.toString();
	}

	/**
	 * 
	 * Returns subset of parameter map relevant to CoordinatesToken,
	 * including the coordToken parameter itself
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return subset of parameter map relevant to CoordinatesToken
	 */
	public static Map<String, List<String>> getCoordinateParameters(Map<String, List<String>> params) {
		Map<String, List<String>> coordinateParams = new TreeMap<>();

		coordinateParams.putAll(getParametersSubset(params, RequestParameters.COORDINATE_PARAM_NAMES));
		
		return coordinateParams;
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

	/**
	 * 
	 * This method returns an Optional containing a CoordinatesToken object if its parameter exists in the parameters map.
	 * If the parameter exists, it automatically attempts to construct and cache the CoordinatesToken object before returning it
	 *
	 * @param allParams parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return an Optional containing a CoordinatesToken string if it exists in the parameters map
	 * @throws Exception 
	 */
	public static Optional<CoordinatesToken> getCoordinatesTokenParameterTokenObjectValue(Map<String, List<String>> allParams) throws RestException {
		Optional<String> tokenStringOptional = getCoordinatesTokenParameterStringValue(allParams);
		
		if (! tokenStringOptional.isPresent()) {
			return Optional.empty();
		} else {
			return Optional.of(CoordinatesTokens.getOrCreate(tokenStringOptional.get()));
		}
	}
	/**
	 * 
	 * This method returns an Optional containing a CoordinatesToken string if it exists in the parameters map.
	 *
	 * @param allParams parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return an Optional containing a CoordinatesToken string if it exists in the parameters map
	 * @throws RestException
	 */
	public static Optional<String> getCoordinatesTokenParameterStringValue(Map<String, List<String>> allParams) throws RestException {
		List<String> coordinateTokenParameterValues = allParams.get(RequestParameters.coordToken);
		
		if (coordinateTokenParameterValues == null || coordinateTokenParameterValues.size() == 0 || StringUtils.isBlank(coordinateTokenParameterValues.get(0))) {
			return Optional.empty();
		} else if (coordinateTokenParameterValues.size() > 1) {
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues + "\"", "too many (" + coordinateTokenParameterValues.size() 
			+ " values - should only be passed with one value");
		}		
		return Optional.of(coordinateTokenParameterValues.get(0));
	}

	/**
	 * @param params list of values for parameter. Blank or empty values specify default,
	 * otherwise valid IFF single boolean-parseable string
	 * @param token underlying token providing default values, if present
	 * @return
	 * @throws RestException if contains multiple values or non boolean-parseable non-empty string
	 */
	public static boolean getStatedFromParameter(List<String> params, Optional<CoordinatesToken> token) throws RestException {
		boolean defaultValue = token.isPresent() ? (token.get().getTaxonomyType() == PremiseType.STATED) : CoordinatesTokens.getDefaultCoordinatesToken().getTaxonomyType() == PremiseType.STATED;
		
		List<String> statedParamStrs = RequestInfoUtils.expandCommaDelimitedElements(params);

		if (statedParamStrs == null || statedParamStrs.size() == 0) {
			return defaultValue;
		} else if (statedParamStrs.size() == 1) {
			String statedParamStr = statedParamStrs.iterator().next();

			if (StringUtils.isBlank(statedParamStr)) {
				return defaultValue;
			}

			return Boolean.parseBoolean(statedParamStrs.get(0).trim());
		}

		throw new RestException(RequestParameters.stated, (params != null ? params.toString() : null), "invalid stated/inferred value");
	}

//	/**
//	 * This method gets the appropriate stated TaxonomyCoordinate value from the parameter map.
//	 * It attempts to retrieve an explicit CoordinatesToken value from the parameter map.
//	 * If an explicit CoordinatesToken exists in the parameter map then its values are used as defaults
//	 * which will be overridden by any and all individual explicit parameter values.
//	 * If an explicit CoordinatesToken exists in the parameter map then hard-coded defaults are used
//	 * for any parameters missing from the relevant parameter subset
//	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
//	 * @return
//	 * @throws RestException if contains either inappropriate number or invalid non-empty values for relevant parameters
//	 */
//	public static boolean getStatedFromParameters(Map<String, List<String>> params) throws RestException {
//		Optional<CoordinatesToken> token = CoordinatesUtil.getCoordinatesTokenFromParameters(params);
//
//		return getStatedFromParameter(params.get(RequestParameters.stated), token);
//	}

	public static int getLanguageCoordinateLanguageSequenceFromParameter(List<String> unexpandedLanguageParamStrs, Optional<CoordinatesToken> token) throws RestException {
		int defaultValue = token.isPresent() ? token.get().getLangCoord() : CoordinatesTokens.getDefaultCoordinatesToken().getLangCoord();
		
		List<String> languageParamStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedLanguageParamStrs);

		if (languageParamStrs == null || languageParamStrs.size() == 0) {
			return defaultValue;
		} else if (languageParamStrs.size() == 1) {
			String languageParamStr = languageParamStrs.iterator().next();

			if (StringUtils.isBlank(languageParamStr)) {
				return defaultValue;
			}

			Optional<UUID> languageUuidOptional = Optional.empty();
			Optional<Integer> languageIntIdOptional = NumericUtils.getInt(languageParamStr.trim());
			if (languageIntIdOptional.isPresent()) {
				int nid = Get.identifierService().getConceptNid(languageIntIdOptional.get());
				if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequence(nid);
					if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LANGUAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			} else if ((languageUuidOptional = UUIDUtil.getUUID(languageParamStr.trim())).isPresent()) {

				if (languageUuidOptional.isPresent() && Get.identifierService().hasUuid(languageUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(languageUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequenceForUuids(languageUuidOptional.get());
					if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LANGUAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			} else if (languageParamStr.trim().toLowerCase().startsWith("english")) {
				return TermAux.ENGLISH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("spanish")) {
				return TermAux.SPANISH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("french")) {
				return TermAux.FRENCH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("danish")) {
				return TermAux.DANISH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("polish")) {
				return TermAux.POLISH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("dutch")) {
				return TermAux.DUTCH_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("lithuanian")) {
				return TermAux.LITHUANIAN_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("chinese")) {
				return TermAux.CHINESE_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("japanese")) {
				return TermAux.JAPANESE_LANGUAGE.getConceptSequence();
			} else if (languageParamStr.trim().toLowerCase().startsWith("swedish")) {
				return TermAux.SWEDISH_LANGUAGE.getConceptSequence();
			}
		}

		throw new RestException(RequestParameters.language, languageParamStrs.toString(), "Invalid language coordinate language value");
	}

	public static int[] getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(List<String> unexpandedDialectsStrs, Optional<CoordinatesToken> token) throws RestException {
		int[] defaultValues = token.isPresent() ? token.get().getLangDialects() : CoordinatesTokens.getDefaultCoordinatesToken().getLangDialects();
		List<Integer> seqList = new ArrayList<>();

		List<String> dialectsStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedDialectsStrs);
		if (dialectsStrs != null && dialectsStrs.size() > 0) {
			for (String dialectId : dialectsStrs)
			{
				if (StringUtils.isNotBlank(dialectId))
				{
					Optional<UUID> dialectUuidOptional = Optional.empty();
					Optional<Integer> dialectIdIntIdOptional = NumericUtils.getInt(dialectId.trim());
					if (dialectIdIntIdOptional.isPresent()) {
						int nid = Get.identifierService().getConceptNid(dialectIdIntIdOptional.get());
						if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequence(dialectIdIntIdOptional.get());
							if (seq == TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence()
									|| seq == TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence()) {
								seqList.add(seq);
								continue;
							}
						}
					} else if ((dialectUuidOptional = UUIDUtil.getUUID(dialectId.trim())).isPresent()) {
						if (dialectUuidOptional.isPresent() && Get.identifierService().hasUuid(dialectUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(dialectUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequenceForUuids(dialectUuidOptional.get());
							if (seq == TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence()
									|| seq == TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence()) {
								seqList.add(seq);
								continue;
							}
						}
					} else if (dialectId.trim().toLowerCase().startsWith("us")) {
						seqList.add(TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence());
						continue;
					} else if (dialectId.trim().toLowerCase().startsWith("gb")) {
						seqList.add(TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence());
						continue;
					}

					throw new RestException(RequestParameters.dialectPrefs, dialectId, "Invalid language coordinate dialect value");
				}
			}
		}

		if (seqList.size() == 0) {
			return defaultValues;
		} else {
			int[] seqArray = new int[seqList.size()];
			for (int i = 0; i < seqList.size(); ++i) {
				seqArray[i] = seqList.get(i);
			}
			return seqArray;
		}
	}
	public static int[] getLanguageCoordinateDescriptionTypePreferenceSequencesFromParameter(List<String> unexpandedDescTypesStrs, Optional<CoordinatesToken> token) throws RestException {
		int[] defaultValues = token.isPresent() ? token.get().getLangDescTypePrefs() : CoordinatesTokens.getDefaultCoordinatesToken().getLangDescTypePrefs();

		List<Integer> seqList = new ArrayList<>();

		List<String> descTypesStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedDescTypesStrs);

		if (descTypesStrs != null && descTypesStrs.size() > 0) {
			for (String descTypeId : descTypesStrs)
			{
				if (StringUtils.isNotBlank(descTypeId))
				{
					Optional<UUID> descTypeUuidOptional = Optional.empty();
					Optional<Integer> descTypeIdIntIdOptional = NumericUtils.getInt(descTypeId.trim());
					
					if (descTypeIdIntIdOptional.isPresent()) {
						int nid = Get.identifierService().getConceptNid(descTypeIdIntIdOptional.get());
						if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequence(descTypeIdIntIdOptional.get());
							if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_TYPE.getConceptSequence()).anyMatch((i) -> i == seq)) {
								seqList.add(seq);
								continue;
							}
						}
					} else if ((descTypeUuidOptional = UUIDUtil.getUUID(descTypeId.trim())).isPresent()) {
						if (descTypeUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(descTypeUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequenceForUuids(descTypeUuidOptional.get());
							if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_TYPE.getConceptSequence()).anyMatch((i) -> i == seq)) {
								seqList.add(seq);
								continue;
							}
						}
					} else if (descTypeId.trim().toLowerCase().startsWith("fsn")) {
						seqList.add(TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					} else if (descTypeId.trim().toLowerCase().startsWith("synonym")) {
						seqList.add(TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					} else if (descTypeId.trim().toLowerCase().startsWith("definition")) {
						seqList.add(TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					}

					throw new RestException(RequestParameters.descriptionTypePrefs, descTypeId, "Invalid language description type value");
				}
			}
		}

		if (seqList.size() == 0) {
			return defaultValues;
		} else {
			int[] seqArray = new int[seqList.size()];
			int i = 0;
			for (int seq : seqList) {
				seqArray[i++] = seq;
			}

			return seqArray;
		}
	}

	public static StampPrecedence getStampCoordinatePrecedenceFromParameter(List<String> unexpandedPrecedenceStrs, Optional<CoordinatesToken> token) throws RestException {
		StampPrecedence defaultValue = token.isPresent() ? token.get().getStampPrecedence() : CoordinatesTokens.getDefaultCoordinatesToken().getStampPrecedence();

		List<String> precedenceStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedPrecedenceStrs);

		if (precedenceStrs == null || precedenceStrs.size() == 0) {
			return defaultValue;
		} else if (precedenceStrs.size() == 1) {
			String precedenceStr = precedenceStrs.iterator().next();

			if (StringUtils.isBlank(precedenceStr)) {
				return defaultValue;
			}

			for (StampPrecedence value : StampPrecedence.values()) {
				if (value.name().equalsIgnoreCase(precedenceStr.trim())
						|| value.toString().equalsIgnoreCase(precedenceStr.trim())) {
					return value;
				}
			}

			Optional<Integer> stampPrecedenceOrdinalOptional = NumericUtils.getInt(precedenceStr.trim());
			if (stampPrecedenceOrdinalOptional.isPresent()) {
				for (StampPrecedence value : StampPrecedence.values()) {
					if (value.ordinal() == stampPrecedenceOrdinalOptional.get()) {
						return value;
					}
				}
			}
		}

		throw new RestException("precedence", "\"" + precedenceStrs + "\"", "Invalid stamp coordinate precedence value");
	}

	public static EnumSet<State> getStampCoordinateAllowedStatesFromParameter(List<String> unexpandedStatesStrs, Optional<CoordinatesToken> token) throws RestException {
		EnumSet<State> defaultValues = token.isPresent() ? token.get().getStampStates() : CoordinatesTokens.getDefaultCoordinatesToken().getStampStates();
		
		EnumSet<State> allowedStates = EnumSet.allOf(State.class);
		allowedStates.clear();

		List<String> statesStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedStatesStrs);

		if (statesStrs == null || statesStrs.size() == 0) {
			return defaultValues; // default
		} else {
			for (String stateStr : statesStrs)
			{
				if (StringUtils.isNotBlank(stateStr))
				{
					boolean foundMatch = false;
					for (State value : State.values()) {
						if (value.name().equalsIgnoreCase(stateStr.trim())
								|| value.getAbbreviation().equalsIgnoreCase(stateStr.trim())
								|| value.toString().equalsIgnoreCase(stateStr.trim())) {
							allowedStates.add(value);
							foundMatch = true;
							break;
						}
					}

					if (! foundMatch) {
						Optional<Integer> stateOrdinalOptional = NumericUtils.getInt(stateStr.trim());
						if (stateOrdinalOptional.isPresent()) {
							for (State value : State.values()) {
								if (value.ordinal() == stateOrdinalOptional.get()) {
									allowedStates.add(value);
									foundMatch = true;
									break;
								}
							}
						}

						if (! foundMatch) {
							throw new RestException("allowedStates", stateStr, "Invalid stamp coordinate state value");
						}
					}
				}
			}

			if (allowedStates.isEmpty()) {
				return defaultValues;
			} else {
				return allowedStates;
			}
		}
	}

	public static ConceptSequenceSet getStampCoordinateModuleSequencesFromParameter(List<String> unexpandedModulesStrs, Optional<CoordinatesToken> token) throws RestException {
		ConceptSequenceSet defaultValue = token.isPresent() ? token.get().getStampModules() : CoordinatesTokens.getDefaultCoordinatesToken().getStampModules();

		ConceptSequenceSet valuesFromParameters = new ConceptSequenceSet();
		List<String> modulesStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedModulesStrs);

		if (modulesStrs == null || modulesStrs.size() == 0) {
			return defaultValue; // default
		} else {
			for (String moduleId : modulesStrs)
			{
				if (StringUtils.isNotBlank(moduleId))
				{
					Optional<Integer> moduleIdIntIdOptional = NumericUtils.getInt(moduleId.trim());
					if (moduleIdIntIdOptional.isPresent()) {
						int nid = Get.identifierService().getConceptNid(moduleIdIntIdOptional.get());
						if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequence(nid);
							if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.MODULE.getConceptSequence()).anyMatch((i) -> i == seq)) {
								valuesFromParameters.add(seq);
								continue;
							}
						}
					} else {
						Optional<UUID> moduleUuidOptional = UUIDUtil.getUUID(moduleId.trim());
						if (moduleUuidOptional.isPresent() && Get.identifierService().hasUuid(moduleUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(moduleUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
							int seq = Get.identifierService().getConceptSequenceForUuids(moduleUuidOptional.get());
							if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.MODULE.getConceptSequence()).anyMatch((i) -> i == seq)) {
								valuesFromParameters.add(seq);
								continue;
							}
						}
					}

					throw new RestException("modules", "\"" + moduleId + "\"", "Invalid stamp coordinate module value");
				}
			}
		}

		return valuesFromParameters;
	}

	public static int getStampCoordinatePathSequenceFromParameter(List<String> unexpandedPathStrs, Optional<CoordinatesToken> token) throws RestException {
		int defaultValue = token.isPresent() ? token.get().getStampPath() : CoordinatesTokens.getDefaultCoordinatesToken().getStampPath();

		List<String> pathStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedPathStrs);

		if (pathStrs == null || pathStrs.size() == 0) {
			return defaultValue;
		} else if (pathStrs.size() == 1) {
			String pathStr = pathStrs.iterator().next();

			if (StringUtils.isBlank(pathStr)) {
				return defaultValue;
			}

			Optional<UUID> pathUuidOptional = Optional.empty();
			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(pathStr.trim());
			if (pathIntIdOptional.isPresent()) {
				int seq = Get.identifierService().getConceptSequence(pathIntIdOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(TermAux.PATH.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			} else if ((pathUuidOptional = UUIDUtil.getUUID(pathStr.trim())).isPresent()) {
				if (pathUuidOptional.isPresent() && Get.identifierService().hasUuid(pathUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
					if (Get.taxonomyService().getTaxonomyChildSequences(TermAux.PATH.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			} else if (pathStr.trim().equalsIgnoreCase("development")) {
				return TermAux.DEVELOPMENT_PATH.getConceptSequence();
			} else if (pathStr.trim().equalsIgnoreCase("master")) {
				return TermAux.MASTER_PATH.getConceptSequence();
			}
		}

		throw new RestException("path", "\"" + pathStrs + "\"", "Invalid stamp coordinate path value");
	}

	public static long getStampCoordinateTimeFromParameter(List<String> unexpandedTimeStrs, Optional<CoordinatesToken> token) throws RestException {
		long defaultValue = token.isPresent() ? token.get().getStampTime() : CoordinatesTokens.getDefaultCoordinatesToken().getStampTime();

		List<String> timeStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedTimeStrs);

		if (timeStrs == null || timeStrs.size() == 0) {
			return defaultValue; // default
		} else if (timeStrs.size() == 1) {
			String timeStr = timeStrs.iterator().next();

			if (StringUtils.isBlank(timeStr)) {
				return defaultValue;
			}

			Optional<Long> longTimeOptional = NumericUtils.getLong(timeStr.trim());
			if (longTimeOptional.isPresent()) {
				return longTimeOptional.get();
			}

			if (timeStr.trim().equalsIgnoreCase("latest")) {
				return Long.MAX_VALUE;
			}
		}

		throw new RestException("time", "\"" + timeStrs + "\"", "invalid stamp coordinate time value");
	}
	public static int getLogicCoordinateStatedAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicStatedAssemblage() : CoordinatesTokens.getDefaultCoordinatesToken().getLogicStatedAssemblage();

		List<String> assemblageStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedAssemblageStrs);

		if (assemblageStrs == null || assemblageStrs.size() == 0) {
			return defaultSeq; // default
		} else if (assemblageStrs.size() == 1) {
			String assemblageStr = assemblageStrs.iterator().next();

			if (StringUtils.isBlank(assemblageStr)) {
				return defaultSeq;
			}

			Optional<UUID> pathUuidOptional = Optional.empty();
			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(assemblageStr.trim());
			if (pathIntIdOptional.isPresent()) {
				int seq = Get.identifierService().getConceptSequence(pathIntIdOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LOGIC_ASSEMBLAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			} else if ((pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim())).isPresent()) {
				if (pathUuidOptional.isPresent() && Get.identifierService().hasUuid(pathUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
					if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LOGIC_ASSEMBLAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			}
		}

		throw new RestException(RequestParameters.logicStatedAssemblage, "\"" + assemblageStrs + "\"", "Invalid logic coordinate stated assemblage value");
	}
	public static int getLogicCoordinateInferredAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicInferredAssemblage() : CoordinatesTokens.getDefaultCoordinatesToken().getLogicInferredAssemblage();

		List<String> assemblageStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedAssemblageStrs);

		if (assemblageStrs == null || assemblageStrs.size() == 0) {
			return defaultSeq; // default
		} else if (assemblageStrs.size() == 1) {
			String assemblageStr = assemblageStrs.iterator().next();

			if (StringUtils.isBlank(assemblageStr)) {
				return defaultSeq;
			}

			Optional<UUID> pathUuidOptional = Optional.empty();
			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(assemblageStr.trim());
			if (pathIntIdOptional.isPresent()) {
				int seq = Get.identifierService().getConceptSequence(pathIntIdOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LOGIC_ASSEMBLAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			} else if ((pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim())).isPresent()) {
				if (pathUuidOptional.isPresent() && Get.identifierService().hasUuid(pathUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
					if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.LOGIC_ASSEMBLAGE.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			}
		}

		throw new RestException(RequestParameters.logicInferredAssemblage, "\"" + assemblageStrs + "\"", "Invalid logic coordinate inferred assemblage value");
	}
	public static int getLogicCoordinateDescProfileAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicDescLogicProfile() : CoordinatesTokens.getDefaultCoordinatesToken().getLogicDescLogicProfile();

		List<String> assemblageStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedAssemblageStrs);

		if (assemblageStrs == null || assemblageStrs.size() == 0) {
			return defaultSeq; // default
		} else if (assemblageStrs.size() == 1) {
			String assemblageStr = assemblageStrs.iterator().next();

			if (StringUtils.isBlank(assemblageStr)) {
				return defaultSeq;
			}

			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(assemblageStr.trim());
			if (pathIntIdOptional.isPresent()) {
				int seq = Get.identifierService().getConceptSequence(pathIntIdOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_LOGIC_PROFILE.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				int seq = Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_LOGIC_PROFILE.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			}
		}

		throw new RestException(RequestParameters.descriptionLogicProfile, "\"" + assemblageStrs + "\"", "Invalid logic coordinate description profile assemblage value");
	}
	public static int getLogicCoordinateClassifierAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicClassifier() : CoordinatesTokens.getDefaultCoordinatesToken().getLogicClassifier();

		List<String> assemblageStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedAssemblageStrs);

		if (assemblageStrs == null || assemblageStrs.size() == 0) {
			return defaultSeq; // default
		} else if (assemblageStrs.size() == 1) {
			String assemblageStr = assemblageStrs.iterator().next();

			if (StringUtils.isBlank(assemblageStr)) {
				return defaultSeq;
			}

			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(assemblageStr.trim());
			if (pathIntIdOptional.isPresent()) {
				int seq = Get.identifierService().getConceptSequence(pathIntIdOptional.get());
				if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_LOGIC_CLASSIFIER.getConceptSequence()).anyMatch((i) -> i == seq)) {
					return seq;
				}
			} else {
				Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
				if (pathUuidOptional.isPresent() && Get.identifierService().hasUuid(pathUuidOptional.get()) && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
					int seq = Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
					if (Get.taxonomyService().getTaxonomyChildSequences(MetaData.DESCRIPTION_LOGIC_CLASSIFIER.getConceptSequence()).anyMatch((i) -> i == seq)) {
						return seq;
					}
				}
			}
		}

		throw new RestException(RequestParameters.classifier, "\"" + assemblageStrs + "\"", "Invalid logic coordinate classifier assemblage value");
	}
}
