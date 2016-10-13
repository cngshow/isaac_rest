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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Singleton;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.User;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.UserService;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * The Class MockUserService.
 *
 * {@link MockUserService}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Service(name="rest-prismeUserService")
@Rank(value = 10)
@Singleton
public class MockUserService implements UserService {
//	/*
//	 * Example URL for get_roles_by_token
//	 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
//	 */
//	/*
//	 * Example SSO Token
//	 * %5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC2%5CxEE%5CxFA%5CxE1%5Cx94%5CxBF3%5CxA9%5Cx16K%22%2C+%22%7EK%5CxC4%5CxEFXk%5Cx80%5CxB1%5CxA3%5CxF3%5Cx8D%5CxB1%5Cx7F%5CxBC%5Cx02K%22%2C+%22k%5Cf%5CxDC%5CxF7%2CP%5CxB2%5Cx97%5Cx99%5Cx99%5CxE0%5CxE1%7C%5CxBF%5Cx1DK%22%2C+%22J%5Cf%5Cx9B%5CxD8w%5Cx15%5CxFE%5CxD3%5CxC7%5CxDC%5CxAC%5Cx9E%5Cx1C%5CxD0bG%22%5D
//	 */
//	//String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";

//	// TODO Joel implement prisme_all_roles_url=https://vaauscttdbs80.aac.va.gov:8080/rails_prisme/roles/get_all_roles.json
//	/*
//	 * TODO implement https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_all_roles
//	 * returning ["super_user","administrator","read_only","editor","reviewer","approver","manager"]
//	 */

	public static final String TEST_JSON1 = "{\"roles\":["
			+ "{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ ","
			+ "{\"id\":19991,\"name\":\"editor\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ ","
			+ "{\"id\":19992,\"name\":\"reviewer\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ ","
			+ "{\"id\":19993,\"name\":\"approver\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ "],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
	public static final String TEST_JSON2 = "{\"roles\":["
			+ "{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ "],\"token_parsed?\":true,\"user\":\"VHAISHKniazJ\",\"type\":\"ssoi\",\"id\":10005}";
	public static final String TEST_JSON3 = "{\"roles\":["
			+ "{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ ","
			+ "{\"id\":19991,\"name\":\"editor\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
			+ "],\"token_parsed?\":true,\"user\":\"VHAISHEfronJ\",\"type\":\"ssoi\",\"id\":10005}";

	private static final Set<UserRole> ALL_ROLES = new HashSet<>();
	static {
		for (UserRole role : UserRole.values()) {
			ALL_ROLES.add(role);
		}
	}

	public MockUserService() {
		
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserService#getUser(java.lang.String)
	 */
	@Override
	public Optional<User> getUser(String ssoToken) {
		String jsonToUse = null;
		if (ssoToken.equals(TEST_JSON1)) {
			jsonToUse = TEST_JSON1;
		} else if (ssoToken.equals(TEST_JSON2)) {
			jsonToUse = TEST_JSON2;
		} else if (ssoToken.equals(TEST_JSON3)) {
			jsonToUse = TEST_JSON3;
		} else if (ssoToken.equals("TEST_JSON1")) {
			jsonToUse = TEST_JSON1;
		} else if (ssoToken.equals("TEST_JSON2")) {
			jsonToUse = TEST_JSON2;
		} else if (ssoToken.equals("TEST_JSON3")) {
			jsonToUse = TEST_JSON3;
		} else {
			// Either a real SSO token or custom JSON
			jsonToUse = ssoToken;
		}

		/*
		 * Example URL for get_roles_by_token
		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
		 */
		
		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> map = null;
		try {
			map = mapper.readValue(jsonToUse, Map.class);
		} catch (Exception e) {
			// Passed text may be random or a real SSO token, which we can't handle, so use a default
			jsonToUse = TEST_JSON1;
		}

		// TODO Joel implement access to PRISME API

		//Map map = mapper.readValue(url, Map.class);
		
		if (map == null) {
			try {
				map = mapper.readValue(jsonToUse, Map.class);
			} catch (Exception e) {
				throw new RuntimeException("Failed reading test JSON \"" + jsonToUse + "\".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			}
		}

		String userName = (String)map.get("user");
		Set<UserRole> roleSet = new HashSet<>();
		Collection<?> roles = (Collection<?>)map.get("roles");
		for (Object roleMapObject : roles) {
			Map<?,?> roleMap = (Map<?,?>)roleMapObject;
			String roleName = (String)roleMap.get("name");
			
			roleSet.add(UserRole.safeValueOf(roleName).get());
		}
		
		final UUID uuidFromUserFsn = UuidT5Generator.get(MetaData.USER.getPrimordialUuid(), userName);

		return Optional.of(new User(userName, uuidFromUserFsn, roleSet));
	}
}
