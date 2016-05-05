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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.coordinate.LanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.tokens.CoordinatesToken;

/**
 * 
 * {@link CoordinatesUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CoordinatesUtil {
	private static Logger log = LogManager.getLogger();

	private CoordinatesUtil() {}

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
	public static  Map<String, List<String>> getCoordinateParameters(Map<String, List<String>> params) {
		Map<String, List<String>> coordinateParams = new TreeMap<>();
		
		if (params.containsKey(RequestParameters.coordToken) && params.get(RequestParameters.coordToken) != null && params.get(RequestParameters.coordToken).size() > 0) {
			coordinateParams.put(RequestParameters.coordToken, params.get(RequestParameters.coordToken));
		}
		coordinateParams.putAll(getTaxonomyCoordinateParameters(params));
		coordinateParams.putAll(getStampCoordinateParameters(params));
		coordinateParams.putAll(getLanguageCoordinateParameters(params));
		coordinateParams.putAll(getLogicCoordinateParameters(params));
		
		return coordinateParams;
	}
	public static Map<String,List<String>> getTaxonomyCoordinateParameters(Map<String, List<String>> params) {
		Map<String,List<String>> coordinateParams = new HashMap<>();

		String coordinateParamNames[] = new String[] {
				RequestParameters.stated
		};
		for (String paramName : coordinateParamNames) {
			if (params.containsKey(paramName) && params.get(paramName) != null && params.get(paramName).size() > 0) {
				coordinateParams.put(paramName, params.get(paramName));
			}
		}

		return coordinateParams;
	}
	public static boolean getStatedFromParameter(List<String> params, Optional<CoordinatesToken> token) throws RestException {
		boolean defaultValue = token.isPresent() ? (token.get().getTaxonomyType() == PremiseType.STATED) : Boolean.parseBoolean(RequestParameters.statedDefault);
		
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
	public static boolean getStatedFromParameters(Map<String, List<String>> params) throws RestException {
		Optional<CoordinatesToken> token = CoordinatesUtil.getCoordinatesTokenFromParameter(params.get(RequestParameters.coordToken));

		return getStatedFromParameter(params.get(RequestParameters.stated), token);
	}
	
	public static Map<String,List<String>> getLanguageCoordinateParameters(Map<String, List<String>> params) {
		Map<String,List<String>> languageCoordinateParams = new HashMap<>();

		String langCoordinateParamNames[] = new String[] {
				RequestParameters.langCoordLang,
				RequestParameters.langCoordDialectsPref,
				RequestParameters.langCoordDescTypesPref
		};
		for (String paramName : langCoordinateParamNames) {
			if (params.containsKey(paramName) && params.get(paramName) != null && params.get(paramName).size() > 0) {
				languageCoordinateParams.put(paramName, params.get(paramName));
			}
		}

		return languageCoordinateParams;
	}
	public static LanguageCoordinate getLanguageCoordinateFromParameters(Map<String, List<String>> params) throws RestException {
		Optional<CoordinatesToken> token = getCoordinatesTokenFromParameter(params.get(RequestParameters.coordToken));

		LanguageCoordinateImpl languageCoordinate = new LanguageCoordinateImpl(
				getLanguageCoordinateLanguageSequenceFromParameter(params.get(RequestParameters.langCoordLang), token), 
				getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(params.get(RequestParameters.langCoordDialectsPref), token),
				getLanguageCoordinateDescriptionTypePreferenceSequencesFromParameter(params.get(RequestParameters.langCoordDescTypesPref), token));

		log.debug("Created LanguageCoordinate from params: " + getLanguageCoordinateParameters(params) + ": " + languageCoordinate);

		return languageCoordinate;
	}

	public static int getLanguageCoordinateLanguageSequenceFromParameter(List<String> unexpandedLanguageParamStrs, Optional<CoordinatesToken> token) throws RestException {
		int defaultValue = token.isPresent() ? token.get().getLangCoord() : TermAux.ENGLISH_LANGUAGE.getConceptSequence();
		
		List<String> languageParamStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedLanguageParamStrs);

		if (languageParamStrs == null || languageParamStrs.size() == 0) {
			return defaultValue;
		} else if (languageParamStrs.size() == 1) {
			String languageParamStr = languageParamStrs.iterator().next();

			if (StringUtils.isBlank(languageParamStr)) {
				return defaultValue;
			}

			if (languageParamStr.trim().toLowerCase().startsWith("english")) {
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

			Optional<Integer> languageIntIdOptional = NumericUtils.getInt(languageParamStr.trim());
			if (languageIntIdOptional.isPresent()) {
				int nid = Get.identifierService().getConceptNid(languageIntIdOptional.get());
				if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT)
					return Get.identifierService().getConceptSequence(nid);
			}

			Optional<UUID> languageUuidOptional = UUIDUtil.getUUID(languageParamStr.trim());
			if (languageUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(languageUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(languageUuidOptional.get());
			}
		}

		throw new RestException(RequestParameters.langCoordLang, languageParamStrs.toString(), "Invalid language coordinate language value");
	}

	public static int[] getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(List<String> unexpandedDialectsStrs, Optional<CoordinatesToken> token) throws RestException {
		int[] defaultValues = token.isPresent() ? token.get().getLangDialects().asArray() : LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate().getDialectAssemblagePreferenceList();
		List<Integer> seqList = new ArrayList<>();

		List<String> dialectsStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedDialectsStrs);
		if (dialectsStrs != null && dialectsStrs.size() > 0) {
			for (String dialectId : dialectsStrs)
			{
				if (StringUtils.isNotBlank(dialectId))
				{
					Optional<Integer> dialectIdIntIdOptional = NumericUtils.getInt(dialectId.trim());
					if (dialectIdIntIdOptional.isPresent()) {
						int nid = Get.identifierService().getConceptNid(dialectIdIntIdOptional.get());
						if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
							seqList.add(Get.identifierService().getConceptSequence(dialectIdIntIdOptional.get()));
							continue;
						}
					}

					Optional<UUID> dialectUuidOptional = UUIDUtil.getUUID(dialectId.trim());
					if (dialectUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(dialectUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
						seqList.add(Get.identifierService().getConceptSequenceForUuids(dialectUuidOptional.get()));
						continue;
					}
					if (dialectId.trim().toLowerCase().startsWith("us")) {
						seqList.add(TermAux.US_DIALECT_ASSEMBLAGE.getConceptSequence());
						continue;
					} else if (dialectId.trim().toLowerCase().startsWith("gb")) {
						seqList.add(TermAux.GB_DIALECT_ASSEMBLAGE.getConceptSequence());
						continue;
					}

					throw new RestException(RequestParameters.langCoordDialectsPref, dialectId, "Invalid language coordinate dialect value");
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
		int[] defaultValues = token.isPresent() ? token.get().getLangTypePrefs().asArray() : LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate().getDescriptionTypePreferenceList();

		List<Integer> seqList = new ArrayList<>();

		List<String> descTypesStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedDescTypesStrs);

		if (descTypesStrs != null && descTypesStrs.size() > 0) {
			for (String descTypeId : descTypesStrs)
			{
				if (StringUtils.isNotBlank(descTypeId))
				{
					Optional<Integer> descTypeIdIntIdOptional = NumericUtils.getInt(descTypeId.trim());
					if (descTypeIdIntIdOptional.isPresent()) {
						int nid = Get.identifierService().getConceptNid(descTypeIdIntIdOptional.get());
						if (Get.identifierService().getChronologyTypeForNid(nid) == ObjectChronologyType.CONCEPT) {
							seqList.add(Get.identifierService().getConceptSequence(descTypeIdIntIdOptional.get()));
							continue;
						}
					}

					Optional<UUID> descTypeUuidOptional = UUIDUtil.getUUID(descTypeId.trim());
					if (descTypeUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(descTypeUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
						seqList.add(Get.identifierService().getConceptSequenceForUuids(descTypeUuidOptional.get()));
						continue;
					}
					if (descTypeId.trim().toLowerCase().startsWith("fsn")) {
						seqList.add(TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					} else if (descTypeId.trim().toLowerCase().startsWith("synonym")) {
						seqList.add(TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					} else if (descTypeId.trim().toLowerCase().startsWith("definition")) {
						seqList.add(TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence());
						continue;
					}

					throw new RestException(RequestParameters.langCoordDescTypesPref, descTypeId, "Invalid language description type value");
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

	public static Map<String,List<String>> getStampCoordinateParameters(Map<String, List<String>> params) {
		Map<String,List<String>> stampCoordinateParams = new HashMap<>();

		String stampCoordinateParamNames[] = new String[] {
				RequestParameters.stampCoordTime,
				RequestParameters.stampCoordPath,
				RequestParameters.stampCoordPrecedence,
				RequestParameters.stampCoordModules,
				RequestParameters.stampCoordStates
		};
		for (String paramName : stampCoordinateParamNames) {
			if (params.containsKey(paramName) && params.get(paramName) != null && params.get(paramName).size() > 0) {
				stampCoordinateParams.put(paramName, params.get(paramName));
			}
		}

		return stampCoordinateParams;
	}

	public static Optional<String> getCoordinatesTokenStringFromParameters(Map<String, List<String>> parameters) throws RestException {
		List<String> coordinateTokenParameterValues = parameters.get(RequestParameters.coordToken);
		
		if (coordinateTokenParameterValues == null || coordinateTokenParameterValues.size() == 0) {
			return Optional.empty();
		} else if (coordinateTokenParameterValues.size() > 1) {
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues + "\"", "too many (" + coordinateTokenParameterValues.size() + " values");
		} else if (coordinateTokenParameterValues.get(0) == null) {
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues.get(0) + "\"", "invalid (null) value");
		}
		
		try {
			return Optional.of(coordinateTokenParameterValues.get(0));
		} catch (Exception e) {
			log.warn("Failed constructing CoordinatesToken from parameters. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues.get(0) + "\"", "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
	public static Optional<CoordinatesToken> getCoordinatesTokenFromParameter(List<String> coordinateTokenParameterValues) throws RestException {
		if (coordinateTokenParameterValues == null || coordinateTokenParameterValues.size() == 0) {
			return Optional.empty();
		} else if (coordinateTokenParameterValues.size() > 1) {
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues + "\"", "too many (" + coordinateTokenParameterValues.size() + " values");
		} else if (coordinateTokenParameterValues.get(0) == null) {
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues.get(0) + "\"", "invalid (null) value");
		}
		
		try {
			return Optional.of(CoordinatesToken.get(coordinateTokenParameterValues.get(0)));
		} catch (Exception e) {
			log.warn("Failed constructing CoordinatesToken from parameters. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new RestException(RequestParameters.coordToken, "\"" + coordinateTokenParameterValues.get(0) + "\"", "Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
	public static StampCoordinate getStampCoordinateFromParameters(Map<String, List<String>> params) throws RestException {
		Optional<CoordinatesToken> token = getCoordinatesTokenFromParameter(params.get(RequestParameters.coordToken));
		
		StampPosition stampPosition = new StampPositionImpl(
				getStampCoordinateTimeFromParameter(params.get(RequestParameters.stampCoordTime), token), 
				getStampCoordinatePathSequenceFromParameter(params.get(RequestParameters.stampCoordPath), token));
		StampCoordinate stampCoordinate = new StampCoordinateImpl(
				getStampCoordinatePrecedenceFromParameter(params.get(RequestParameters.stampCoordPrecedence), token),
				stampPosition, 
				getStampCoordinateModuleSequencesFromParameter(params.get(RequestParameters.stampCoordModules), token),
				getStampCoordinateAllowedStatesFromParameter(params.get(RequestParameters.stampCoordStates), token));

		log.debug("Created StampCoordinate from params: " + getStampCoordinateParameters(params) + ": " + stampCoordinate);

		return stampCoordinate;
	}
	public static StampPrecedence getStampCoordinatePrecedenceFromParameter(List<String> unexpandedPrecedenceStrs, Optional<CoordinatesToken> token) throws RestException {
		StampPrecedence defaultValue = token.isPresent() ? token.get().getStampPrecedence() : StampPrecedence.PATH;

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

		throw new RestException("stampCoordPrecedence", "\"" + precedenceStrs + "\"", "Invalid stamp coordinate precedence value");
	}

	public static EnumSet<State> getStampCoordinateAllowedStatesFromParameter(List<String> unexpandedStatesStrs, Optional<CoordinatesToken> token) throws RestException {
		EnumSet<State> defaultValues = token.isPresent() ? token.get().getStampStates() : EnumSet.of(State.ACTIVE);
		
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
							throw new RestException("stampCoordStates", stateStr, "Invalid stamp coordinate state value");
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
		ConceptSequenceSet defaultValue = token.isPresent() ? token.get().getStampModules() : new ConceptSequenceSet();

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
							valuesFromParameters.add(Get.identifierService().getConceptSequence(moduleIdIntIdOptional.get()));
							continue;
						}
					}

					Optional<UUID> moduleUuidOptional = UUIDUtil.getUUID(moduleId.trim());
					if (moduleUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(moduleUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
						valuesFromParameters.add(Get.identifierService().getConceptSequenceForUuids(moduleUuidOptional.get()));
						continue;
					}

					throw new RestException("stampCoordModules", "\"" + moduleId + "\"", "Invalid stamp coordinate module value");
				}
			}
		}

		return valuesFromParameters;
	}

	public static int getStampCoordinatePathSequenceFromParameter(List<String> unexpandedPathStrs, Optional<CoordinatesToken> token) throws RestException {
		int defaultValue = token.isPresent() ? token.get().getStampPath() : TermAux.DEVELOPMENT_PATH.getConceptSequence();

		List<String> pathStrs = RequestInfoUtils.expandCommaDelimitedElements(unexpandedPathStrs);

		if (pathStrs == null || pathStrs.size() == 0) {
			return defaultValue;
		} else if (pathStrs.size() == 1) {
			String pathStr = pathStrs.iterator().next();

			if (StringUtils.isBlank(pathStr)) {
				return defaultValue;
			}

			Optional<Integer> pathIntIdOptional = NumericUtils.getInt(pathStr.trim());
			if (pathIntIdOptional.isPresent()) {
				return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(pathStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
			}

			if (pathStr.trim().equalsIgnoreCase("development")) {
				return TermAux.DEVELOPMENT_PATH.getConceptSequence();
			} else if (pathStr.trim().equalsIgnoreCase("master")) {
				return TermAux.MASTER_PATH.getConceptSequence();
			}
		}

		throw new RestException("stampCoordPath", "\"" + pathStrs + "\"", "Invalid stamp coordinate path value");
	}

	public static long getStampCoordinateTimeFromParameter(List<String> unexpandedTimeStrs, Optional<CoordinatesToken> token) throws RestException {
		long defaultValue = token.isPresent() ? token.get().getStampTime() : Long.MAX_VALUE;

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

		throw new RestException("stampCoordTime", "\"" + timeStrs + "\"", "invalid stamp coordinate time value");
	}

	public static Map<String,List<String>> getLogicCoordinateParameters(Map<String, List<String>> params) {
		Map<String,List<String>> logicCoordinateParams = new HashMap<>();

		String logicCoordinateParamNames[] = new String[] {
				RequestParameters.logicCoordStated,
				RequestParameters.logicCoordInferred,
				RequestParameters.logicCoordDesc,
				RequestParameters.logicCoordClassifier
		};
		for (String paramName : logicCoordinateParamNames) {
			if (params.containsKey(paramName)) {
				logicCoordinateParams.put(paramName, params.get(paramName));
			}
		}

		return logicCoordinateParams;
	}
	public static LogicCoordinate getLogicCoordinateFromParameters(Map<String, List<String>> params) throws RestException {
		Optional<CoordinatesToken> token = getCoordinatesTokenFromParameter(params.get(RequestParameters.coordToken));

		LogicCoordinate logicCoordinate = new LogicCoordinateImpl(
				getLogicCoordinateStatedAssemblageFromParameter(params.get(RequestParameters.logicCoordStated), token), 
				getLogicCoordinateInferredAssemblageFromParameter(params.get(RequestParameters.logicCoordInferred), token),
				getLogicCoordinateDescProfileAssemblageFromParameter(params.get(RequestParameters.logicCoordDesc), token),
				getLogicCoordinateClassifierAssemblageFromParameter(params.get(RequestParameters.logicCoordClassifier), token));

		log.debug("Created LogicCoordinate from params: " + getLogicCoordinateParameters(params) + ": " + logicCoordinate);

		return logicCoordinate;
	}
	public static int getLogicCoordinateStatedAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicStatedAssemblage() : Get.identifierService().getConceptSequenceForUuids(UUID.fromString(RequestParameters.logicCoordStatedDefault));

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
				return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
			}
		}

		throw new RestException(RequestParameters.logicCoordStated, "\"" + assemblageStrs + "\"", "Invalid logic coordinate stated assemblage value");
	}
	public static int getLogicCoordinateInferredAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicInferredAssemblage() : Get.identifierService().getConceptSequenceForUuids(UUID.fromString(RequestParameters.logicCoordInferredDefault));

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
				return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
			}
		}

		throw new RestException(RequestParameters.logicCoordInferred, "\"" + assemblageStrs + "\"", "Invalid logic coordinate inferred assemblage value");
	}
	public static int getLogicCoordinateDescProfileAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicDescLogicProfile() : Get.identifierService().getConceptSequenceForUuids(UUID.fromString(RequestParameters.logicCoordDescDefault));

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
				return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
			}
		}

		throw new RestException(RequestParameters.logicCoordDesc, "\"" + assemblageStrs + "\"", "Invalid logic coordinate description profile assemblage value");
	}
	public static int getLogicCoordinateClassifierAssemblageFromParameter(List<String> unexpandedAssemblageStrs, Optional<CoordinatesToken> token) throws RestException {
		final int defaultSeq = token.isPresent() ? token.get().getLogicClassifier() : Get.identifierService().getConceptSequenceForUuids(UUID.fromString(RequestParameters.logicCoordClassifierDefault));

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
				return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
			}

			Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(assemblageStr.trim());
			if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
				return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
			}
		}

		throw new RestException(RequestParameters.logicCoordClassifier, "\"" + assemblageStrs + "\"", "Invalid logic coordinate classifier assemblage value");
	}
}
