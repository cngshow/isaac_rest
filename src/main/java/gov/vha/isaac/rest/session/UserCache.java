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

import java.util.Optional;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

/**
 * 
 * {@link UserCache}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface UserCache {
	/**
	 * 
	 * This method caches a User object by its user concept UUID
	 * 
	 * @param value User object
	 * @throws Exception
	 */
	public void put(User value);

	/**
	 * 
	 * This method attempts to retrieve the User object
	 * corresponding to the passed user conceptUUID key.
	 * 
	 * @param user concept UUID key
	 * @return User object
	 * @throws Exception
	 */
	public Optional<User> get(UUID key);
}
