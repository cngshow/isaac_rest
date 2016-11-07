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

import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.UserRoleService;

/**
 * 
 * {@link PrismeUserService}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface PrismeUserService extends UserRoleService {

	/**
	 * Return a user and roles available for that user
	 *
	 * @param ssoToken
	 *            the user's SSO token string
	 * @return the user and roles available to the user
	 * 
	 * This implementation gets a User from PRISME IFF prisme.properties is in classpath
	 * and contains a value for property "prisme_roles_by_token_url", otherwise it attempts to parse
	 * the token as a test keyword from UserServiceutils (i.e. TEST_JSON1) OR to parse a test
	 * string of the form {name}:{role1}[{,role2}[{,role3}[...]]]
	 */
	Optional<User> getUser(String ssoToken);

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserRoleService#getUserRoles(java.util.UUID)
	 * 
	 * This method should throw exception if the user has not already been cached
	 */
	Set<UserRole> getUserRoles(UUID userId);

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserRoleService#getAllUserRoles()
	 * 
	 * This implementation gets all roles from PRISME IFF prisme.properties is in classpath
	 * and contains a value for property "prisme_all_roles_url", otherwise it returns all of the
	 * UserRole text values except for "automated"
	 */
	Set<UserRole> getAllUserRoles();

	boolean usePrismeForAllRoles();

	boolean usePrismeForRolesByToken();

	boolean usePrismeForSsoTokenByName();

	Optional<String> safeGetToken(String id, String password);

	String getToken(String id, String password) throws Exception;
}