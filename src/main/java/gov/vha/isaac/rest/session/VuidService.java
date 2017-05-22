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
import java.util.Set;
import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

import gov.vha.isaac.ochre.api.User;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.UserRoleService;
import gov.vha.isaac.rest.api.data.vuid.RestVuidBlockData;

/**
 * 
 * {@link VuidService}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface VuidService {

	/**
	 * Allocate and return a block of VUIDs
	 *
	 * @param blockSize
	 *            the size of the block of VUIDs as a positive, non zero integer
	 * @param reason
	 *            the reason for the allocation request as text
	 * @param ssoToken
	 *            the user's SSO token string
	 * @return the RestVuidBlockData
	 * 
	 * This implementation gets a RestVuidBlockData object from VUID-rest IFF vuid.properties is in classpath
	 * and contains a value for property "vuid_allocate_url"
	 */
	Optional<RestVuidBlockData> allocate(int blockSize, String reason, String ssoToken);
	
	String getVuidServiceUrl();
}