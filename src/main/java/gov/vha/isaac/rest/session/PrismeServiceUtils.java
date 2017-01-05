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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * {@link PrismeServiceUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class PrismeServiceUtils {
	private static Logger log = LogManager.getLogger(PrismeServiceUtils.class);

	private static Properties prismeProperties_ = null;
	
	private PrismeServiceUtils() {}
	/**
	 * Return a Properties object which contains the PRISME properties.
	 * Empty if prisme.properties not found. Never returns null.
	 * 
	 * @return
	 */
	static Properties getPrismeProperties()
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
				final URL propertiesFile = PrismeServiceUtils.class.getResource("/prisme.properties");
				
				stream = PrismeServiceUtils.class.getResourceAsStream("/prisme.properties");

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

	public static String getTargetFromUrl(URL url) {
		StringBuilder target = new StringBuilder();
		target.append(url.getProtocol());
		target.append("://");
		target.append(url.getHost());
		if (url.getPort() > 0) {
			target.append(":" + url.getPort());
		}
		
		return target.toString();
	}

	static String postJsonToPrisme(WebTarget targetWithPath, String json) {
		return postJsonToPrisme(targetWithPath, json, (Map<String, String>)null);
	}
	static String postJsonToPrisme(WebTarget targetWithPath, String json, Map<String, String> params) {
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				targetWithPath = targetWithPath.queryParam(entry.getKey(), entry.getValue());
			}
		}
		Response response = targetWithPath.request().post(Entity.json(json));
		
		String responseJson = response.readEntity(String.class);
	
		return responseJson;
	}

	static String getResultJsonFromPrisme(WebTarget targetWithPath, Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			targetWithPath = targetWithPath.queryParam(entry.getKey(), entry.getValue());
		}
		Response response = targetWithPath.request().get();
		
		String responseJson = response.readEntity(String.class);
	
		return responseJson;
	}
	
	static String getResultJsonFromPrisme(String targetStr, String pathStr, Map<String, String> params) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(targetStr);
		target = target.path(pathStr);
		
		return getResultJsonFromPrisme(target, params);
	}

	static String getResultJsonFromPrisme(String targetStr, String pathStr) {
		return getResultJsonFromPrisme(targetStr, pathStr, new HashMap<>());
	}
}
