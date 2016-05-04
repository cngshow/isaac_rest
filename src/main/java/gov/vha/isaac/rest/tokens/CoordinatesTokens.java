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
import java.util.Map;

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
	private static Map<String, CoordinatesToken> CACHE = null;

	public static void init(final int maxEntries) {
		synchronized(LOCK) {
			CACHE = new LinkedHashMap<String, CoordinatesToken>(maxEntries, 0.75F, true) {
				private static final long serialVersionUID = -1236481390177598762L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, CoordinatesToken> eldest){
					return size() > maxEntries;
				}
			};
			
			CoordinatesToken defaultCoordinatesToken = new CoordinatesToken();
			defaultCoordinatesTokenStr = defaultCoordinatesToken.serialize();
			put(defaultCoordinatesToken);
		}
	}

	public static String getDefaultCoordinatesTokenString() {
		synchronized(LOCK) {
			if (CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			
			return defaultCoordinatesTokenStr;
		}
	}
	public static CoordinatesToken getDefaultCoordinatesTokenObject() {
		synchronized(LOCK) {
			if (CACHE == null) {
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
			if (CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}

			get(value);
		}
	}
	public static void put(CoordinatesToken value) {
		synchronized(LOCK) {
			if (CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			CACHE.put(value.serialize(), value);
		}
	}
	public static CoordinatesToken get(String key) throws Exception {
		synchronized(LOCK) {
			if (CACHE == null) {
				init(DEFAULT_MAX_SIZE);
			}
			if (CACHE.get(key) != null) {
				return CACHE.get(key);
			} else {
				CACHE.put(key, new CoordinatesToken(key));
			}
			return CACHE.get(key);
		}
	}
}
