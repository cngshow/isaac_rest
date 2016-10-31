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

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * 
 * {@link SecurityUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class SecurityUtils {
	private SecurityUtils() {}

	public static void validateRole(SecurityContext securityContext, Object apiClass) {
		boolean userAuthorized = false;
		DenyAll denyAll = apiClass.getClass().getAnnotation(DenyAll.class);
		// If @DenyAll exists then fail
		if (denyAll != null) {
			throw new SecurityException("User not authorized: " + securityContext.getUserPrincipal() + " to access methods of class " + apiClass.getClass().getName() + ". @DenyAll is set.");
		}
		// If @RolesAllowed exists then check each of @RolesAllowed against securityContext.isUserInRole(),
		// failing if no match found
		RolesAllowed ra = apiClass.getClass().getAnnotation(RolesAllowed.class);
		if (ra != null) {
			for (String role : ra.value()) {
				if (securityContext.isUserInRole(role)) {
					userAuthorized = true;
					break;
				}
			}
		} else {
			// If @RolesAllowed is not set and @PermitAll is, then succeed, else fail
			PermitAll permitAll = apiClass.getClass().getAnnotation(PermitAll.class);
			if (permitAll != null) {
				userAuthorized = true;
			}
		}
		if ( !userAuthorized) {
			throw new SecurityException("User not authorized: " + securityContext.getUserPrincipal().getName() + " to access methods of class " + apiClass.getClass().getName() + ". Must have one of following role(s): " + (ra != null ? ra.value() : null));
		}
	}
}
