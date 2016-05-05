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
	private static final Object LOCK = new Object();
	private static final int DEFAULT_MAX_SIZE = 1024;
	private static String defaultCoordinatesTokenStr = null;
	private static Map<String, CoordinatesToken> OBJECT_BY_TOKEN_CACHE = null;
	private static Map<String, String> TOKEN_BY_PARAMS_CACHE = null;

	public static void init(final int maxEntries) {
		synchronized(LOCK) {
			OBJECT_BY_TOKEN_CACHE = new LinkedHashMap<String, CoordinatesToken>(maxEntries, 0.75F, true) {
				private static final long serialVersionUID = -1236481390177598762L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, CoordinatesToken> eldest){
					return size() > maxEntries;
				}
			};
			
			CoordinatesToken defaultCoordinatesToken = new CoordinatesToken();
			defaultCoordinatesTokenStr = defaultCoordinatesToken.serialize();
			put(defaultCoordinatesToken);
			
			TOKEN_BY_PARAMS_CACHE = new LinkedHashMap<String, String>(maxEntries, 0.75F, true) {
				private static final long serialVersionUID = -2638577900934193146L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<String, String> eldest){
					return size() > maxEntries;
				}
			};
		}
	}

	public static String getDefaultCoordinatesTokenString() {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			
			return defaultCoordinatesTokenStr;
		}
	}
	public static CoordinatesToken getDefaultCoordinatesTokenObject() {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			
			try {
				return get(defaultCoordinatesTokenStr);
			} catch (Exception e) {
				// Should never fail because defaultCoordinatesTokenStr created from real coordinates
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void put(String value) throws Exception {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}

			get(value);
		}
	}
	public static void put(CoordinatesToken value) {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			OBJECT_BY_TOKEN_CACHE.put(value.serialize(), value);
		}
	}
	public static void put(Map<String, List<String>> params, CoordinatesToken value) {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			String serializedToken = value.serialize();
			OBJECT_BY_TOKEN_CACHE.put(serializedToken, value);
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
		}
	}
	public static void put(Map<String, List<String>> params, String serializedToken, CoordinatesToken value) {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			OBJECT_BY_TOKEN_CACHE.put(serializedToken, value);
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
		}
	}
	public static void put(Map<String, List<String>> params, String serializedToken) throws Exception {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			if (OBJECT_BY_TOKEN_CACHE.get(serializedToken) == null) {
				OBJECT_BY_TOKEN_CACHE.put(serializedToken, new CoordinatesToken(serializedToken));
			}
			TOKEN_BY_PARAMS_CACHE.put(CoordinatesUtil.encodeCoordinateParameters(params), serializedToken);
		}
	}
	public static CoordinatesToken get(String key) throws Exception {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			if (OBJECT_BY_TOKEN_CACHE.get(key) != null) {
				return OBJECT_BY_TOKEN_CACHE.get(key);
			} else {
				OBJECT_BY_TOKEN_CACHE.put(key, new CoordinatesToken(key));
			}
			return OBJECT_BY_TOKEN_CACHE.get(key);
		}
	}
	public static String get(Map<String, List<String>> params) {
		synchronized(LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				return null;
			} else {
				return TOKEN_BY_PARAMS_CACHE.get(CoordinatesUtil.encodeCoordinateParameters(params));
			}
		}
	}
}
