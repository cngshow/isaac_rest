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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.CoordinatesUtil;

/**
 * 
 * {@link CoordinatesTokens}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CoordinatesTokens {
	private final static Object OBJECT_BY_TOKEN_CACHE_LOCK = new Object();
	private final static Object TOKEN_BY_PARAMS_CACHE_LOCK = new Object();
	
	private static final int DEFAULT_MAX_SIZE = 1024;
	private static CoordinatesToken defaultCoordinatesToken = null;
	private static Map<String, CoordinatesToken> OBJECT_BY_TOKEN_CACHE = null;
	private static Map<String, String> TOKEN_BY_PARAMS_CACHE = null;

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
				TOKEN_BY_PARAMS_CACHE = new LinkedHashMap<String, String>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -2638577900934193146L;

					@Override
					protected boolean removeEldestEntry(Map.Entry<String, String> eldest){
						return size() > maxEntries;
					}
				};
			}
		}
	}

	private static TaxonomyCoordinate getDefaultTaxonomyCoordinate() {
		return TaxonomyCoordinates.getStatedTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest(),
				LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
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
	protected static void put(CoordinatesToken value) {
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
	public static void put(Map<String, List<String>> params, CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), value.getSerialized());
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
	public static CoordinatesToken get(String key) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			return OBJECT_BY_TOKEN_CACHE.get(key);
		}
	}
	
	public static CoordinatesToken getOrCreate(String key) throws RestException {
		CoordinatesToken token = get(key);
		
		if (token == null) {
			token = new CoordinatesToken(key);
			put(token);
		}
		
		return get(key);
	}

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
		
		CoordinatesToken cachedToken = get(constructedToken.getSerialized());
		
		if (cachedToken == null) {
			cachedToken = constructedToken;
			put(cachedToken);
		}
		
		return get(cachedToken.getSerialized());
	}

	public static CoordinatesToken getOrCreate(
			long stampTime,
			int stampPath,
			byte stampPrecedence,
			int[] stampModules,
			byte[] stampStates,
			int langCoord,
			int[] langDialects,
			int[] langTypePrefs,
			byte taxonomyType,
			int logicStatedAssemblage,
			int logicInferredAssemblage,
			int logicDescLogicProfile,
			int logicClassifier) {
		
		CoordinatesToken constructedToken =
				new CoordinatesToken(
						stampTime,
						stampPath,
						stampPrecedence,
						stampModules,
						stampStates,
						langCoord,
						langDialects,
						langTypePrefs,
						taxonomyType,
						logicStatedAssemblage,
						logicInferredAssemblage,
						logicDescLogicProfile,
						logicClassifier);
		
		CoordinatesToken cachedToken = get(constructedToken.getSerialized());
		
		if (cachedToken == null) {
			cachedToken = constructedToken;
			put(cachedToken);
		}
		
		return get(cachedToken.getSerialized());
	}

	/**
	 * Attempt to retrieve CoordinatesToken serialization key string
	 * by a hash of the parameters presumably used to generate the object
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return
	 */
	public static String get(Map<String, List<String>> params) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			return null;
		} else {
			synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
				return TOKEN_BY_PARAMS_CACHE.get(CoordinatesUtil.encodeCoordinateParameters(params));
			}
		}
	}
}
