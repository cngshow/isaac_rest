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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.UserRoleService;
import gov.vha.isaac.ochre.api.util.UuidT5Generator;

/**
 * The Class PrismeIntegratedUserService.
 *
 * {@link PrismeIntegratedUserService}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Service(name="rest-prismeUserService")
@Rank(value = 10)
@Singleton
public class PrismeIntegratedUserService implements UserRoleService {
	
	//private static Logger log = LogManager.getLogger(PrismeIntegratedUserService.class);

	private Properties prismeProperties_ = null;

	private PrismeIntegratedUserService() {
		//for HK2
	}
	
	private Optional<User> getUserFromPrisme(String ssoToken) throws JsonParseException, JsonMappingException, IOException {
//		/*
//		 * Example URL for get_roles_by_token
//		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
//		 */
//		/*
//		 * Example SSO Token
//		 * %5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC2%5CxEE%5CxFA%5CxE1%5Cx94%5CxBF3%5CxA9%5Cx16K%22%2C+%22%7EK%5CxC4%5CxEFXk%5Cx80%5CxB1%5CxA3%5CxF3%5Cx8D%5CxB1%5Cx7F%5CxBC%5Cx02K%22%2C+%22k%5Cf%5CxDC%5CxF7%2CP%5CxB2%5Cx97%5Cx99%5Cx99%5CxE0%5CxE1%7C%5CxBF%5Cx1DK%22%2C+%22J%5Cf%5Cx9B%5CxD8w%5Cx15%5CxFE%5CxD3%5CxC7%5CxDC%5CxAC%5Cx9E%5Cx1C%5CxD0bG%22%5D
//		 */
//		//String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";

		URL url = new URL(getPrismeRolesByTokenUrl());
		Map<String, String> params = new HashMap<>();
		params.put("token", ssoToken);
		String jsonResultString = getResultJsonFromPrisme(UserServiceUtils.getTargetFromUrl(url), url.getPath(), params);
		
		return Optional.of(getUserFromJson(jsonResultString));
	}
	private Set<UserRole> getAllRolesFromPrisme() throws JsonParseException, JsonMappingException, IOException {
		URL url = new URL(getPrismeAllRolesUrl());
		String jsonResultString = getResultJsonFromPrisme(UserServiceUtils.getTargetFromUrl(url), url.getPath());
		
		Set<UserRole> roles = new HashSet<>();
		
		ObjectMapper mapper = new ObjectMapper();
		Object returnedObject = mapper.readValue(jsonResultString, List.class);
		
		for (Object roleFromPrisme : (List<?>)returnedObject) {
			roles.add(UserRole.valueOf(roleFromPrisme.toString()));
		}
		
		return Collections.unmodifiableSet(roles);
	}
	
	private static UUID getUuidFromUserName(String userName) {
		return UuidT5Generator.get(MetaData.USER.getPrimordialUuid(), userName);
	}

	static User getUserFromJson(String jsonToUse) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> map = null;
		map = mapper.readValue(jsonToUse, Map.class);
		
		String userName = (String)map.get("user");
		Set<UserRole> roleSet = new HashSet<>();
		Collection<?> roles = (Collection<?>)map.get("roles");
		for (Object roleMapObject : roles) {
			Map<?,?> roleMap = (Map<?,?>)roleMapObject;
			String roleName = (String)roleMap.get("name");
			
			roleSet.add(UserRole.safeValueOf(roleName).get());
		}
		
		final UUID uuidFromUserFsn = getUuidFromUserName(userName);;

		User newUser = new User(userName, uuidFromUserFsn, roleSet);
		
		return newUser;
	}
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
	public Optional<User> getUser(String ssoToken) {
		if (usePrismeForRolesByToken()) {
			try {
				return getUserFromPrisme(ssoToken);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return UserServiceUtils.getUserFromTestToken(ssoToken);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserRoleService#getUserRoles(java.util.UUID)
	 * 
	 * This method should throw exception if the user has not already been cached
	 */
	@Override
	public Set<UserRole> getUserRoles(UUID userId)
	{
		return UserCache.get(userId).get().getRoles();
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserRoleService#getAllUserRoles()
	 * 
	 * This implementation gets all roles from PRISME IFF prisme.properties is in classpath
	 * and contains a value for property "prisme_all_roles_url", otherwise it returns all of the
	 * UserRole text values except for "automated"
	 */
	@Override
	public Set<UserRole> getAllUserRoles()
	{
		if (usePrismeForAllRoles()) {
			try {
				return getAllRolesFromPrisme();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			Set<UserRole> availableRoles = new HashSet<>();

			for (UserRole role : UserRole.values()) {
				if (role != UserRole.AUTOMATED) { // AUTOMATED will not be a PRISME role
					availableRoles.add(role);
				}
			}

			return Collections.unmodifiableSet(availableRoles);
		}
	}

	/**
	 * Return a Properties object which contains PRISME properties. Empty if prisme.properties not found. Never returns null.
	 * 
	 * @return
	 */
	Properties getPrismeProperties()
	{
//		#if prisme.properties is present prisme must be up!
//		#edits here require a restart to your Komet instance
//		#Edit prisme_root to use prisme for roles.
//		#prisme_root=http://localhost:8080/rails_prisme
//		#prisme_all_roles_url=https://localhost:443/roles/get_all_roles.json
//		#prisme_roles_user_url=https://localhost:443/roles/get_user_roles.json
//		#prisme_roles_ssoi_url=https://localhost:443/roles/get_ssoi_roles.json
//		#prisme_roles_by_token_url=https://localhost:443/roles/get_roles_by_token.json
//		#prisme_config_url=https://localhost:443/utilities/prisme_config.json
//		#Edit this to true to default to the prisme instead of the test roles test harness.
//		#war_group_id=gov.vha.isaac.gui.rails
//		#war_artifact_id=rails_komet
//		#war_version=1.11
//		#war_repo=releases
//		#war_classifier=a
//		#war_package=war
//		#isaac_root=https://vadev.mantech.com:4848/isaac-rest/
		if (prismeProperties_ == null) {
			prismeProperties_ = new Properties();

			try (final InputStream stream = this.getClass().getResourceAsStream("/prisme.properties"))
			{
				if (stream == null)
				{
					//log.debug("No prisme.properties file was found on the classpath");
				}
				else
				{
					//log.info("Reading PRISME configuration from prisme.properties file");
					prismeProperties_.load(stream);
				}
				
				return prismeProperties_;
			}
			catch (Exception e)
			{
				//log.error("Unexpected error trying to read properties from the prisme.properties file", e);
				throw new RuntimeException(e);
			}
		}

		return prismeProperties_;
	}

	String getPrismeAllRolesUrl() {
		return getPrismeProperties().getProperty("prisme_all_roles_url");
	}
	boolean usePrismeForAllRoles() {
		return getPrismeAllRolesUrl() != null;
	}
	String getPrismeRolesByTokenUrl() {
		return getPrismeProperties().getProperty("prisme_roles_by_token_url");
	}
	boolean usePrismeForRolesByToken() {
		return getPrismeRolesByTokenUrl() != null;
	}

	private String getResultJsonFromPrisme(String targetStr, String pathStr, Map<String, String> params) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(targetStr);
		target = target.path(pathStr);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			target = target.queryParam(entry.getKey(), entry.getValue());
		}
		Response response = target.request().get();
		
		String responseJson = response.readEntity(String.class);

		return responseJson;
	}
	private String getResultJsonFromPrisme(String targetStr, String pathStr) {
		return getResultJsonFromPrisme(targetStr, pathStr, new HashMap<>());
	}
}
