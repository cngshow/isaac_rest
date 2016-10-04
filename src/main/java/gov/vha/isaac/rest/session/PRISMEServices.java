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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * {@link PRISMEServices}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class PRISMEServices {
	public static class Role {
		private long id;
		private String roleName;

		/**
		 * @param id
		 * @param name
		 */
		public Role(long id, String roleName) {
			super();
			this.id = id;
			this.roleName = roleName.trim();
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @return the role name
		 */
		public String getRoleName() {
			return roleName;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Role other = (Role) obj;
			if (id != other.id)
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Role [id=" + id + ", roleName=" + roleName + "]";
		}
	}
	public static class User {
		/*
		 * {"roles":[{"id":10000,"name":"read_only","resource_id":null,"resource_type":null,"created_at":"2016-09-23T17:26:38.000Z","updated_at":"2016-09-23T17:26:38.000Z"},{"id":10001,"name":"super_user","resource_id":null,"resource_type":null,"created_at":"2016-09-23T17:26:45.000Z","updated_at":"2016-09-23T17:26:45.000Z"},{"id":10020,"name":"approver","resource_id":null,"resource_type":null,"created_at":"2016-09-27T15:41:58.000Z","updated_at":"2016-09-27T15:41:58.000Z"}],"token_parsed?":true,"user":"cboden","type":"ssoi","id":10000}
		 */
		private long id;
		private String userName;
		private Set<Role> roles = new HashSet<>();
		
		/**
		 * @param id
		 * @param userName
		 * @param roles
		 */
		public User(long id, String userName, Set<Role> roles) {
			super();
			this.id = id;
			this.userName = userName;
			if (roles != null) {
				this.roles.addAll(roles);
			}
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @return the user
		 */
		public String getUserName() {
			return userName;
		}

		/**
		 * @return the roles
		 */
		public Set<Role> getRoles() {
			return Collections.unmodifiableSet(roles);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "User [id=" + id + ", userName=" + userName + ", roles=" + roles + "]";
		}
	}
	
	public static User getUser(String token) {
		return null;
	}

	public static boolean hasRole(String token, String roleName) {
		return getUser(token).getRoles().contains(roleName.trim());
	}

	/**
	 * 
	 */
	public PRISMEServices() {
		// TODO Auto-generated constructor stub
	}
}
