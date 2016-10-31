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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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

	public static void validateRole(SecurityContext securityContext, Object apiClassInstance) {
		// Confirm that no method-level role-related security annotations are declared
		// Only class-level role-related security annotations supported
		for (Method method : apiClassInstance.getClass().getMethods()) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType() == RolesAllowed.class
						|| annotation.annotationType() == PermitAll.class
						|| annotation.annotationType() == DenyAll.class
						|| annotation.annotationType() == DeclareRoles.class) {
					throw new RuntimeException("Cannot properly apply annotation-based role-related security constraints to " + apiClassInstance.getClass().getName() + " when any of its methods have method-level annotation " + annotation);
				}
			}
		}

		validateRole(securityContext, apiClassInstance, null);
	}
	public static void validateRole(SecurityContext securityContext, Object apiClassInstance, Method method) {
		// Confirm that no method-level role-related security annotations are declared
		// Only class-level role-related security annotations supported
		if (method != null) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType() == RolesAllowed.class
						|| annotation.annotationType() == PermitAll.class
						|| annotation.annotationType() == DenyAll.class
						|| annotation.annotationType() == DeclareRoles.class) {
					throw new RuntimeException("Cannot properly apply annotation-based role-related security constraints to " + apiClassInstance.getClass().getName() + " when method " + method.getName() + " has method-level annotation " + annotation);
				}
			}
		}
		boolean userAuthorized = false;
		DenyAll denyAll = apiClassInstance.getClass().getAnnotation(DenyAll.class);
		// If @DenyAll exists then fail
		if (denyAll != null) {
			throw new SecurityException("User not authorized: " + securityContext.getUserPrincipal() + " to access methods of class " + apiClassInstance.getClass().getName() + ". @DenyAll is set.");
		}
		// If @RolesAllowed exists then check each of @RolesAllowed against securityContext.isUserInRole(),
		// failing if no match found
		RolesAllowed ra = apiClassInstance.getClass().getAnnotation(RolesAllowed.class);
		if (ra != null) {
			for (String role : ra.value()) {
				if (securityContext.isUserInRole(role)) {
					userAuthorized = true;
					break;
				}
			}
		} else {
			// If @RolesAllowed is not set and @PermitAll is, then succeed, else fail
			PermitAll permitAll = apiClassInstance.getClass().getAnnotation(PermitAll.class);
			if (permitAll != null) {
				userAuthorized = true;
			}
		}
		if ( !userAuthorized) {
			throw new SecurityException("User not authorized: " + securityContext.getUserPrincipal().getName() + " to access methods of class " + apiClassInstance.getClass().getName() + ". Must have one of following role(s): " + (ra != null ? ra.value() : null));
		}
	}
}
