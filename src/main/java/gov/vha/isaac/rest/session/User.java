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

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
 * {
 * 	"roles":
 * 		[
 * 			{
 * 				"id":10000,
 * 				"name":"read_only",
 * 				"resource_id":null,
 * 				"resource_type":null,
 * 				"created_at":"2016-09-13T14:48:18.000Z",
 * 				"updated_at":"2016-09-13T14:48:18.000Z"
 * 			}
 * 		],
 * 	"token_parsed?":true,
 * 	"user":"VHAISHArmbrD",
 * 	"type":"ssoi",
 * 	"id":10005}
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
//JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")  
public class User {
	boolean token_parsed;
	String userName;
	String type;
	int id;
	Set<Role> roles = new HashSet<>();
	
	User() {}
	
	/**
	 * @param token_parsed
	 * @param userName
	 * @param type
	 * @param id
	 * @param roles
	 */
	public User(boolean token_parsed, String userName, String type, int id, Set<Role> roles) {
		super();
		this.token_parsed = token_parsed;
		this.userName = userName;
		this.type = type;
		this.id = id;
		if (roles != null) {
			this.roles.addAll(roles);
		}
	}

	/**
	 * @return the token_parsed
	 */
	public boolean isToken_parsed() {
		return token_parsed;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the user name
	 */
	public String getUserame() {
		return userName;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
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
		return "User [token_parsed=" + token_parsed + ", userName=" + userName + ", type=" + type + ", id=" + id
				+ ", roles=" + roles + "]";
	}
}