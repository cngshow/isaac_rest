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

	public static void init(final int maxEntries) {
		synchronized(OBJECT_BY_TOKEN_CACHE_LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				OBJECT_BY_TOKEN_CACHE = new LinkedHashMap<String, CoordinatesToken>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -1236481390177598762L;
					@Override
					protected boolean removeEldestEntry(Map.Entry<String, CoordinatesToken> eldest){
						return size() > maxEntries;
					}
				};

				defaultCoordinatesToken = new CoordinatesToken();
				put(defaultCoordinatesToken);
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
	 * This method attempts to cache a CoordinatesToken serialization,
	 * automatically constructing the respective object
	 * 
	 * @param value CoordinatesToken string
	 * @throws Exception
	 */
	public static void put(String value) throws Exception {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		if (get(value) == null) {
			put(new CoordinatesToken(value));
		}

	}
	/**
	 * 
	 * This method caches a CoordinatesToken object,
	 * automatically serializing itself to generate its key
	 * 
	 * @param value CoordinatesToken object
	 * @throws Exception
	 */
	public static void put(CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			OBJECT_BY_TOKEN_CACHE.put(value.getSerialized(), value);
		}
	}
	/**
	 * 
	 * This method caches a CoordinatesToken object,
	 * automatically serializing itself to generate its key
	 * and also caching the key by a hash of the parameters presumably
	 * used to generate the object
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @param value CoordinatesToken object
	 * @throws Exception
	 */
	public static void put(Map<String, List<String>> params, CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		String serializedToken = value.getSerialized();
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			OBJECT_BY_TOKEN_CACHE.put(serializedToken, value);
		}
		synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
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
	public static void put(Map<String, List<String>> params, String serializedToken, CoordinatesToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			OBJECT_BY_TOKEN_CACHE.put(serializedToken, value);
		}
		synchronized (TOKEN_BY_PARAMS_CACHE_LOCK) {
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
		}
	}
	/**
	 * 
	 * This method attempts to cache a CoordinatesToken serialization,
	 * automatically constructing the respective object
	 * and also caching the key by a hash of the parameters presumably
	 * used to generate the object
	 * 
	 * @param params parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @param serializedToken CoordinatesToken string
	 * @throws Exception
	 */
	public static void put(Map<String, List<String>> params, String serializedToken) throws Exception {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized(OBJECT_BY_TOKEN_CACHE_LOCK) {
			if (OBJECT_BY_TOKEN_CACHE.get(serializedToken) == null) {
				OBJECT_BY_TOKEN_CACHE.put(serializedToken, new CoordinatesToken(serializedToken));	
			}
		}
		synchronized(TOKEN_BY_PARAMS_CACHE_LOCK) {
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
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
	public static CoordinatesToken get(String key) throws Exception {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			return OBJECT_BY_TOKEN_CACHE.get(key);
		}
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
