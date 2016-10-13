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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 
 * {@link UserCache}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class UserCache {
	private final static Object OBJECT_BY_ID_CACHE_LOCK = new Object();
	
	private static final int DEFAULT_MAX_SIZE = 1024;
	private static Map<UUID, User> OBJECT_BY_ID_CACHE = null;

	private static void init(final int maxEntries) {
		synchronized(OBJECT_BY_ID_CACHE_LOCK) {
			if (OBJECT_BY_ID_CACHE == null) {
				OBJECT_BY_ID_CACHE = new LinkedHashMap<UUID, User>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -1236481390177598762L;
					@Override
					protected boolean removeEldestEntry(Map.Entry<UUID, User> eldest){
						return size() > maxEntries;
					}
				};
			}
		}
	}

	/**
	 * 
	 * This method caches a User object by its user concept UUID
	 * 
	 * @param value User object
	 * @throws Exception
	 */
	public static void put(User value) {
		if (OBJECT_BY_ID_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		synchronized (OBJECT_BY_ID_CACHE_LOCK) {
			OBJECT_BY_ID_CACHE.put(value.getId(), value);
		}
	}

	/**
	 * 
	 * This method attempts to retrieve the User object
	 * corresponding to the passed user conceptUUID key.
	 * 
	 * @param user concept UUID key
	 * @return User object
	 * @throws Exception
	 */
	public static Optional<User> get(UUID key) {
		if (OBJECT_BY_ID_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_ID_CACHE_LOCK) {
			User obj = OBJECT_BY_ID_CACHE.get(key);
			return obj != null ? Optional.of(obj) : Optional.empty();
		}
	}
}
