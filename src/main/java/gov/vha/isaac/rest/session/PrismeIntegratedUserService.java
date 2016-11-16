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
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.vha.isaac.ochre.api.UserRole;

/**
 * The Class PrismeIntegratedUserService
 *
 * {@link PrismeIntegratedUserService}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Service(name="rest-prismeUserService")
@Rank(value = 10)
@Singleton
public class PrismeIntegratedUserService implements PrismeUserService {
	private static Logger log = LogManager.getLogger(PrismeIntegratedUserService.class);
	
	private Properties prismeProperties_ = null;

	protected PrismeIntegratedUserService() {
		//for HK2
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#getUser(java.lang.String)
	 * 
	 * This implementation will fail if PRISME is not configured
	 */
	@Override
	public Optional<User> getUser(String ssoToken) {
		try {
			return getUserFromPrisme(ssoToken);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.ochre.api.UserRoleService#getUserRoles(java.util.UUID)
	 * 
	 * This method should throw exception if the user has not already been cached
	 */
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#getUserRoles(java.util.UUID)
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
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#getAllUserRoles()
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
	protected Properties getPrismeProperties()
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

			InputStream stream = null;
			try {
				final URL propertiesFile = this.getClass().getResource("/prisme.properties");
				
				stream = this.getClass().getResourceAsStream("/prisme.properties");

				if (stream == null)
				{
					log.debug("No prisme.properties file was found on the classpath");
				}
				else
				{
					log.info("Reading PRISME configuration from prisme.properties file " + propertiesFile);
					prismeProperties_.load(stream);
				}
				
				return prismeProperties_;
			}
			catch (Exception e)
			{
				log.error("Unexpected error trying to read properties from the prisme.properties file", e);
				throw new RuntimeException(e);
			}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}

		return prismeProperties_;
	}

	protected String getPrismeAllRolesUrl() {
		return getPrismeProperties().getProperty("prisme_all_roles_url");
	}
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#usePrismeForAllRoles()
	 */
	@Override
	public boolean usePrismeForAllRoles() {
		return getPrismeAllRolesUrl() != null;
	}
	protected String getPrismeRolesByTokenUrl() {
		return getPrismeProperties().getProperty("prisme_roles_by_token_url");
	}
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#usePrismeForRolesByToken()
	 */
	@Override
	public boolean usePrismeForRolesByToken() {
		return getPrismeRolesByTokenUrl() != null;
	}
	protected String getSsoTokenByNameUrl() {
		return getPrismeProperties().getProperty("prisme_roles_user_url");
	}
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#usePrismeForSsoTokenByName()
	 */
	@Override
	public boolean usePrismeForSsoTokenByName() {
		return getSsoTokenByNameUrl() != null;
	}
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#safeGetToken(java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<String> safeGetToken(String id, String password) {
		try {
			return Optional.of(getToken(id, password));
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return Optional.empty();
		} 
	}
	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.PrismeUserService#getToken(java.lang.String, java.lang.String)
	 */
	@Override
	public String getToken(String id, String password) throws Exception {		
		if (usePrismeForSsoTokenByName()) {
			return getUserSsoTokenFromPrisme(id, password);
		} else {
			throw new RuntimeException("Cannot generate SSO token for " + id + " without access to PRISME");
		}
	}

	// Private helpers
	protected Optional<User> getUserFromPrisme(String ssoToken) throws JsonParseException, JsonMappingException, IOException {
//		/*
//		 * Example URL for get_roles_by_token
//		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
//		 */
//		/*
//		 * Example SSO Token
//		 * %5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC2%5CxEE%5CxFA%5CxE1%5Cx94%5CxBF3%5CxA9%5Cx16K%22%2C+%22%7EK%5CxC4%5CxEFXk%5Cx80%5CxB1%5CxA3%5CxF3%5Cx8D%5CxB1%5Cx7F%5CxBC%5Cx02K%22%2C+%22k%5Cf%5CxDC%5CxF7%2CP%5CxB2%5Cx97%5Cx99%5Cx99%5CxE0%5CxE1%7C%5CxBF%5Cx1DK%22%2C+%22J%5Cf%5Cx9B%5CxD8w%5Cx15%5CxFE%5CxD3%5CxC7%5CxDC%5CxAC%5Cx9E%5Cx1C%5CxD0bG%22%5D
//		 */
//		//String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
		String prismeRolesByTokenUrlStr = getPrismeRolesByTokenUrl();
		log.trace("Retrieved from prisme.properties prismeRolesByTokenUrlStr=\"" + prismeRolesByTokenUrlStr + "\"");
		URL url = new URL(prismeRolesByTokenUrlStr);
		Optional<User> user = UserServiceUtils.getUserFromUrl(url, ssoToken);
		log.trace("Retrieved from " + prismeRolesByTokenUrlStr + " user=\"" + user + "\"");

		if (! user.isPresent()) {
			log.error("FAILED retrieving User from " + prismeRolesByTokenUrlStr);
		}
		return user;
	}
	protected Set<UserRole> getAllRolesFromPrisme() throws JsonParseException, JsonMappingException, IOException {
		String prismeAllRolesUrlStr = getPrismeAllRolesUrl();
		log.trace("Retrieved from prisme.properties prismeAllRolesUrlStr=\"" + prismeAllRolesUrlStr + "\"");
		URL url = new URL(prismeAllRolesUrlStr);
		Set<UserRole> allRolesFromFromPrisme = UserServiceUtils.getAllRolesFromUrl(url);
		log.trace("Retrieved from " + prismeAllRolesUrlStr + " allRolesFromFromPrisme=" + allRolesFromFromPrisme);
		return allRolesFromFromPrisme;
	}
	
	protected String getUserSsoTokenFromPrisme(String id, String password) throws Exception {
		String ssoTokenByNameUrlStr = getSsoTokenByNameUrl();
		log.trace("Retrieved from prisme.properties ssoTokenByNameUrlStr=\"" + ssoTokenByNameUrlStr + "\"");
		URL url = new URL(ssoTokenByNameUrlStr);
		String ssoToken = UserServiceUtils.getUserSsoTokenFromUrl(url, id, password);
		log.trace("Retrieved from " + ssoTokenByNameUrlStr + " ssoToken=\"" + ssoToken + "\"");
		return ssoToken;
	}
}
