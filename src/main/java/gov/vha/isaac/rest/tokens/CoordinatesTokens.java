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

package gov.vha.isaac.rest.tokens;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.CoordinatesUtil;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link CoordinatesTokens}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CoordinatesTokens {
	private static Logger log = LogManager.getLogger(CoordinatesTokens.class);

	private final static Object OBJECT_BY_TOKEN_CACHE_LOCK = new Object();
	private final static Object TOKEN_BY_PARAMS_CACHE_LOCK = new Object();
	
	private static final int DEFAULT_MAX_SIZE = 1024;
	private static CoordinatesToken defaultCoordinatesToken = null;
	private static TaxonomyCoordinate defaultCoordinates = null;
	private static Map<String, CoordinatesToken> OBJECT_BY_TOKEN_CACHE = null;
	private static Map<String, CoordinatesToken> TOKEN_BY_PARAMS_CACHE = null;

	private static void init(final int maxEntries) {
		synchronized(OBJECT_BY_TOKEN_CACHE_LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				OBJECT_BY_TOKEN_CACHE = new LinkedHashMap<String, CoordinatesToken>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -1236481390177598762L;
					@Override
					protected boolean removeEldestEntry(Map.Entry<String, CoordinatesToken> eldest){
						return size() > maxEntries;
					}
				};

				defaultCoordinatesToken = CoordinatesTokens.getOrCreate(
						getDefaultTaxonomyCoordinate().getStampCoordinate(),
						getDefaultTaxonomyCoordinate().getLanguageCoordinate(),
						getDefaultTaxonomyCoordinate().getLogicCoordinate(),
						getDefaultTaxonomyCoordinate().getTaxonomyType()
						);
			}
		}
		synchronized(TOKEN_BY_PARAMS_CACHE_LOCK) {
			if (TOKEN_BY_PARAMS_CACHE == null) {
				TOKEN_BY_PARAMS_CACHE = new LinkedHashMap<String, CoordinatesToken>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -2638577900934193146L;

					@Override
					protected boolean removeEldestEntry(Map.Entry<String, CoordinatesToken> eldest){
						return size() > maxEntries;
					}
				};
			}
		}
	}

	/**
	 * 
	 * This method returns the coordinate used for all defaulting
	 * 
	 * @return
	 */
	private static TaxonomyCoordinate getDefaultTaxonomyCoordinate() {
		if (defaultCoordinates == null) {
			defaultCoordinates = TaxonomyCoordinates.getStatedTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest(),
					LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
		}

		return defaultCoordinates;
	}

	/**
	 * @return CoordinatesToken object containing components for default coordinates
	 */
	public static CoordinatesToken getDefaultCoordinatesToken() {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		return defaultCoordinatesToken;
	}

	/**
	 * 
	 * This method caches a CoordinatesToken object,
	 * automatically serializing itself to generate its key
	 * 
	 * @param value CoordinatesToken object
	 * @throws Exception
	 */
	private static void _put(CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			OBJECT_BY_TOKEN_CACHE.put(value.getSerialized(), value);
		}
	}

	/**
	 * 
	 * This method caches a CoordinatesToken object by the provided key
	 * and also caching the key by a hash of the parameters presumably
	 * used to generate the object
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @param serializedToken CoordinatesToken serialization string used as key
	 * @param value CoordinatesToken object
	 * @throws Exception
	 */
	private static void _put(Map<String, List<String>> params, CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), value);
		}
	}

	/**
	 * 
	 * This method attempts to retrieve the CoordinatesToken object
	 * corresponding to the passed serialized CoordinatesToken string key.
	 * 
	 * @param key serialized CoordinatesToken string
	 * @return CoordinatesToken object
	 * @throws Exception
	 */
	private static CoordinatesToken _get(String key) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			return OBJECT_BY_TOKEN_CACHE.get(key);
		}
	}
	
	/**
	 * This method attempts to retrieve the CoordinatesToken object
	 * corresponding to the passed serialized CoordinatesToken string key.
	 * If the CoordinatesToken is not cached, it creates and caches it before returning it.
	 * 
	 * @param key serialized CoordinatesToken string
	 * @return CoordinatesToken object
	 * @throws RestException
	 */
	public static CoordinatesToken getOrCreate(String key) throws RestException {
		CoordinatesToken token = _get(key);
		
		if (token == null) {
			token = new CoordinatesToken(key);
			_put(token);
		}
		
		return _get(key);
	}

	/**
	 * This method attempts to retrieve the CoordinatesToken object
	 * corresponding to the serialized CoordinatesToken string key
	 * corresponding to the CoordinatesToken object
	 * constructed from the passed coordinate parameters.
	 * If the CoordinatesToken is not cached, it creates and caches it before returning it.
	 * 
	 * @param StampCoordinate
	 * @param LanguageCoordinate
	 * @param LogicCoordinate
	 * @param PremiseType
	 * @return
	 */
	public static CoordinatesToken getOrCreate(
			StampCoordinate stamp,
			LanguageCoordinate lang,
			LogicCoordinate logic,
			PremiseType taxType) {
		CoordinatesToken constructedToken =
				new CoordinatesToken(
						stamp,
						lang,
						logic,
						taxType);
		
		CoordinatesToken cachedToken = _get(constructedToken.getSerialized());
		
		if (cachedToken == null) {
			cachedToken = constructedToken;
			_put(cachedToken);
		}
		
		return _get(cachedToken.getSerialized());
	}

	/**
	 * This method attempts to retrieve the CoordinatesToken object
	 * corresponding to the serialized CoordinatesToken string key
	 * corresponding to the CoordinatesToken object
	 * constructed from the passed individual coordinate parameters.
	 * If the CoordinatesToken is not cached, it creates and caches it before returning it.
	 * 
	 * @param stampTime long 
	 * @param stampPath int  concept sequence or nid
	 * @param stampPrecedence StampPrecedence
	 * @param stampModules ConceptSequenceSet
	 * @param stampStates EnumSet<State>
	 * @param langCoord int
	 * @param langDialects int[]
	 * @param langTypePrefs int[]
	 * @param statedTaxonomyType boolean
	 * @param logicStatedAssemblage int
	 * @param logicInferredAssemblage int
	 * @param logicDescLogicProfile int
	 * @param logicClassifier int
	 * @return
	 */
	public static CoordinatesToken getOrCreate(
			long stampTime,
			int stampPath,
			StampPrecedence stampPrecedence,
			ConceptSequenceSet stampModules,
			EnumSet<State> stampStates,
			int langCoord,
			int[] langDialects,
			int[] langTypePrefs,
			boolean statedTaxonomyType,
			int logicStatedAssemblage,
			int logicInferredAssemblage,
			int logicDescLogicProfile,
			int logicClassifier) {
		
		CoordinatesToken constructedToken =
				new CoordinatesToken(
						stampTime,
						stampPath,
						(byte)stampPrecedence.ordinal(),
						stampModules.asArray(),
						Util.byteArrayFromEnumSet(stampStates),
						langCoord,
						langDialects,
						langTypePrefs,
						(byte)(statedTaxonomyType ? PremiseType.STATED : PremiseType.INFERRED).ordinal(),
						logicStatedAssemblage,
						logicInferredAssemblage,
						logicDescLogicProfile,
						logicClassifier);
		
		CoordinatesToken cachedToken = _get(constructedToken.getSerialized());
		
		if (cachedToken == null) {
			cachedToken = constructedToken;
			_put(cachedToken);
		}
		
		return _get(cachedToken.getSerialized());
	}

	/**
	 * Attempt to retrieve CoordinatesToken serialization key string
	 * by a hash of the parameters presumably used to generate the object.
	 * The hash includes all of the parameters returned by CoordinatesUtil.getCoordinateParameters()
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return
	 */
	private static CoordinatesToken _get(Map<String, List<String>> params) {
		if (TOKEN_BY_PARAMS_CACHE == null) {
			return null;
		} else {
			synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
				return TOKEN_BY_PARAMS_CACHE.get(CoordinatesUtil.encodeCoordinateParameters(params));
			}
		}
	}

	/**
	 * Attempt to retrieve CoordinatesToken serialization key string
	 * by a hash of the parameters presumably used to generate the object.
	 * The hash includes all of the parameters returned by CoordinatesUtil.getCoordinateParameters().
	 * If the CoordinatesToken corresponding to the passed parameters does not exist,
	 * then attempt to create and cache it before returning it
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return
	 */
	public static CoordinatesToken getOrCreate(Map<String, List<String>> parameters) throws Exception {
		log.debug("Constructing CoordinatesToken from parameters");

		CoordinatesToken cachedToken = _get(parameters);
		if (cachedToken != null) {
			return cachedToken;
		}

		// Set RequestInfo coordinatesToken string to parameter value if set, otherwise set to default
		Optional<CoordinatesToken> token = CoordinatesUtil.getCoordinatesTokenFromCoordinatesTokenParameter(parameters);
		if (token.isPresent()) {
			log.debug("Applying CoordinatesToken " + RequestParameters.coordToken + " parameter \"" + token.get().getSerialized() + "\"");
		} else {
			log.debug("Applying default coordinates");

			token = Optional.of(CoordinatesTokens.getDefaultCoordinatesToken());
		}

		// Determine if any relevant coordinate parameters set
		Map<String,List<String>> coordinateParameters = new HashMap<>();
		coordinateParameters.putAll(CoordinatesUtil.getParametersSubset(parameters,
				RequestParameters.stated,
				RequestParameters.STAMP_COORDINATE_PARAM_NAMES,
				RequestParameters.LANGUAGE_COORDINATE_PARAM_NAMES,
				RequestParameters.LOGIC_COORDINATE_PARAM_NAMES));

		// If ANY relevant coordinate parameter values set, then calculate new CoordinatesToken string
		if (coordinateParameters.size() == 0) {
			log.debug("No individual coordinate parameters to apply to token \"" + token.get().getSerialized() + "\"");

			return token.get();
		} else { // if (coordinateParameters.size() > 0)
			log.debug("Applying {} individual parameters to coordinates token \"{}\": {}", token.get().getSerialized(), coordinateParameters.size(), coordinateParameters.toString());

			// TaxonomyCoordinate components
			boolean stated = CoordinatesUtil.getStatedFromParameter(coordinateParameters.get(RequestParameters.stated), token);

			// LanguageCoordinate components
			int langCoordLangSeq = CoordinatesUtil.getLanguageCoordinateLanguageSequenceFromParameter(coordinateParameters.get(RequestParameters.language), token); 
			int[] langCoordDialectPrefs = CoordinatesUtil.getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.dialectPrefs), token);
			int[] langCoordDescTypePrefs = CoordinatesUtil.getLanguageCoordinateDescriptionTypePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.descriptionTypePrefs), token);

			// StampCoordinate components
			long stampTime = CoordinatesUtil.getStampCoordinateTimeFromParameter(coordinateParameters.get(RequestParameters.time), token); 
			int stampPathSeq = CoordinatesUtil.getStampCoordinatePathSequenceFromParameter(coordinateParameters.get(RequestParameters.path), token);
			StampPrecedence stampPrecedence = CoordinatesUtil.getStampCoordinatePrecedenceFromParameter(coordinateParameters.get(RequestParameters.precedence), token);
			ConceptSequenceSet stampModules = CoordinatesUtil.getStampCoordinateModuleSequencesFromParameter(coordinateParameters.get(RequestParameters.modules), token);
			EnumSet<State> stampAllowedStates = CoordinatesUtil.getStampCoordinateAllowedStatesFromParameter(coordinateParameters.get(RequestParameters.allowedStates), token);

			// LogicCoordinate components
			int logicStatedSeq = CoordinatesUtil.getLogicCoordinateStatedAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicStatedAssemblage), token);
			int logicInferredSeq = CoordinatesUtil.getLogicCoordinateInferredAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicInferredAssemblage), token);
			int logicDescProfileSeq = CoordinatesUtil.getLogicCoordinateDescProfileAssemblageFromParameter(coordinateParameters.get(RequestParameters.descriptionLogicProfile), token);
			int logicClassifierSeq = CoordinatesUtil.getLogicCoordinateClassifierAssemblageFromParameter(coordinateParameters.get(RequestParameters.classifier), token);

			CoordinatesToken tokenObjFromParameters = CoordinatesTokens.getOrCreate(
					stampTime,
					stampPathSeq,
					stampPrecedence,
					stampModules,
					stampAllowedStates,
					langCoordLangSeq,
					langCoordDialectPrefs,
					langCoordDescTypePrefs,
					stated,
					logicStatedSeq,
					logicInferredSeq,
					logicDescProfileSeq,
					logicClassifierSeq);

			_put(CoordinatesUtil.getCoordinateParameters(parameters), tokenObjFromParameters);
			
			log.debug("Created CoordinatesToken \"" + tokenObjFromParameters.getSerialized() + "\"");
			
			return tokenObjFromParameters;
		}
	}
}
