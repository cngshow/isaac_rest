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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.ochre.api.LookupService;

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
	*/
	public static Properties getPrismeProperties()
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
//		#prisme_notify_url=http://localhost:3000/log_event?security_token=%5B%22u%5Cf%5Cx92%5CxBC%5Cx17%7D%5CxD1%5CxE4%5CxFB%5CxE5%5Cx99%5CxA3%5C%22%5CxE8%5C%5CK%22%2C+%22%3E%5Cx16%5CxDE%5CxA8v%5Cx14%5CxFF%5CxD2%5CxC6%5CxDD%5CxAD%5Cx9F%5Cx1D%5CxD1cF%22%5D

		if (prismeProperties_ == null) {
			InputStream stream = null;
			try {
				Properties props = new Properties();
				final URL propertiesFile = PrismeServiceUtils.class.getResource("/prisme.properties");
				
				stream = PrismeServiceUtils.class.getResourceAsStream("/prisme.properties");

				if (stream == null)
				{
					log.debug("No prisme.properties file was found on the classpath");
				}
				else
				{
					log.info("Reading PRISME configuration from prisme.properties file " + propertiesFile);
					props.load(stream);
				}
				
				prismeProperties_ = props;
			}
			catch (Exception e)
			{
				String msg = "Unexpected error trying to read properties from the prisme.properties file";
				log.error(msg, e);
				throw new RuntimeException(msg, e);
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
		try {
			StringBuilder target = new StringBuilder();
			target.append(url.getProtocol());
			target.append("://");
			target.append(url.getHost());
			if (url.getPort() > 0) {
				target.append(":" + url.getPort());
			}

			return target.toString();
		} catch (RuntimeException e) {
			log.error("FAILED getting target from URL '" + url + "'", e);
			throw e;
		}
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
		Response response = targetWithPath.request().accept(MediaType.APPLICATION_JSON).post(Entity.json(json));

		if (response.getStatus() != Status.OK.getStatusCode()) {
			throw new RuntimeException("Failed performing POST " + targetWithPath + " of \"" + json + "\" + with CODE=" + response.getStatus() + " and REASON=" + response.getStatusInfo());
		}

		String responseJson = response.readEntity(String.class);
	
		return responseJson;
	}

	static String getResultJsonFromPrisme(WebTarget targetWithPath, Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			targetWithPath = targetWithPath.queryParam(entry.getKey(), entry.getValue());
		}
		Response response = targetWithPath.request().accept(MediaType.APPLICATION_JSON).get();

		if (response.getStatus() != Status.OK.getStatusCode()) {
			throw new RuntimeException("Failed performing GET " + targetWithPath + " with CODE=" + response.getStatus() + " and REASON=" + response.getStatusInfo());
		}

		String responseJson = response.readEntity(String.class);
		log.debug("Request '{}' returned '{}'", targetWithPath.toString(), responseJson);
	
		return responseJson;
	}
	
	static String getResultJsonFromPrisme(String targetStr, String pathStr, Map<String, String> params) {
		ClientService clientService = LookupService.getService(ClientService.class);
		WebTarget target = clientService.getClient().target(targetStr);
		target = target.path(pathStr);
		
		return getResultJsonFromPrisme(target, params);
	}

	static String getResultJsonFromPrisme(String targetStr, String pathStr) {
		return getResultJsonFromPrisme(targetStr, pathStr, new HashMap<>());
	}
}
