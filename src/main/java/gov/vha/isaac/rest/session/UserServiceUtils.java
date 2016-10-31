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
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.UserRole;

/**
 * 
 * {@link UserServiceUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class UserServiceUtils {
	public static final String TEST_JSON1 = "{\"roles\":["
	+ "{\"id\":19990,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19991,\"name\":\"editor\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19992,\"name\":\"reviewer\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19993,\"name\":\"administrator\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19994,\"name\":\"manager\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19995,\"name\":\"approver\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ "],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
	public static final String TEST_JSON2 = "{\"roles\":["
	+ "{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ "],\"token_parsed?\":true,\"user\":\"VHAISBKniazJ\",\"type\":\"ssoi\",\"id\":10005}";
	public static final String TEST_JSON3 = "{\"roles\":["
	+ "{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ ","
	+ "{\"id\":19991,\"name\":\"editor\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}"
	+ "],\"token_parsed?\":true,\"user\":\"VHAISHEfronJ\",\"type\":\"ssoi\",\"id\":10005}";

	private UserServiceUtils() {}

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

	/**
	 * 
	 * Attempt to construct a user from a string of the following format:
	 * 
	 * {name}:{role1}[{,role2}[{,role3}[...]]]
	 * 
	 * @param arg
	 * @return
	 */
	static Optional<String> constructTestUser(String arg) {
		try {
			String[] components = arg.split(":");
	
			String name = null;
			Set<UserRole> roles = new HashSet<>();
			if (components.length == 2) {
				if (components[0].matches("[A-Za-z][A-Za-z0-9_]*")) {
					name = components[0].trim();
	
					String[] roleStrings = components[1].split(",");
	
					for (int i = 0; i < roleStrings.length; ++i) {
						roles.add(UserRole.safeValueOf(roleStrings[i].trim()).get());
					}
				}
			}
			
			if (name != null && name.length() > 0 && roles.size() > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("{\"roles\":[");
				boolean addedRole = false;
				for (UserRole role : roles) {
					if (addedRole) {
						builder.append(",");
					}
					builder.append("{\"id\":19990,\"name\":\"" + role.toString() + "\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}");
	
					addedRole = true;
				}
				
				builder.append("],\"token_parsed?\":true,\"user\":\"" + name + "\",\"type\":\"ssoi\",\"id\":10005}");
				
				return Optional.of(builder.toString());
			}
		} catch (Exception e) {
			// ignore
		}
	
		return Optional.empty();
	}

	public static Optional<User> getUserFromTestToken(String ssoToken) throws JsonParseException, JsonMappingException, IOException {
		String jsonToUse = null;
	
		Optional<String> createdJson = constructTestUser(ssoToken);
		if (createdJson.isPresent()) {
			jsonToUse = createdJson.get();
		} else {
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
		}
	
		/*
		 * Example URL for get_roles_by_token
		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
		 */
		User newUser = null;
		try {
			newUser = PrismeIntegratedUserService.getUserFromJson(jsonToUse);
		} catch (Exception e) {
			try {
				newUser = PrismeIntegratedUserService.getUserFromJson(TEST_JSON1);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
		
		UserCache.put(newUser);
	
		return Optional.of(newUser);
	}


	public static void main(String...argv) {
//		 * Example URL for get_roles_by_token
//		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
		final String ssoToken = "%5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC0%5CxF2%5CxE8%5CxA5%5CxD8%5CxE3t%5CxFFUK%22%2C+%22%2CJ%5Cx83%5CxA3%5Cx13k%5Cx96%5CxFC%5CxE6%5CxF3%5CxCF%5CxF2%7C%5CxB8MK%22%2C+%224%5Cf%5Cx85%5CxA7%5Cx13k%5CxB3%5CxBD%5CxB8%5CxB8%5CxD2%5CxBDr%5CxB2%5Cx02K%22%2C+%22%2CS%5CxE8%5CxDBt%5Cx16%5CxFD%5CxD0%5CxC4%5CxDF%5CxAF%5Cx9D%5Cx1F%5CxD3aD%22%5D";
		//final String urlStr = "https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json?id=cris@cris.com&password=cris@cris.com&token=" + ssoToken;
		//final String url = "https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json?token=" + ssoToken;
		//final String username_ = "cris@cris.com";
		//final String password_ = "cris@cris.com";
		PrismeIntegratedUserService service = LookupService.getService(PrismeIntegratedUserService.class);

		service.getPrismeProperties().setProperty(
				"prisme_roles_by_token_url",
				"https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json");
		service.getPrismeProperties().setProperty(
				"prisme_roles_by_token_url",
				"https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json");
		try {
//			URL testUrl = new URL(service.getPrismeProperties().getProperty("prisme_roles_by_token_url"));
//			System.out.println("URL: " + testUrl);
//			System.out.println("Protocol: " + testUrl.getProtocol());
//			System.out.println("Host: " + testUrl.getHost());
//			System.out.println("Port: " + testUrl.getPort());
//			System.out.println("Path: " + testUrl.getPath());
//
//			testUrl = new URL("https://localhost:443/roles/get_user_roles.json");
//			System.out.println("URL: " + testUrl);
//			System.out.println("Protocol: " + testUrl.getProtocol());
//			System.out.println("Host: " + testUrl.getHost());
//			System.out.println("Port: " + testUrl.getPort());
//			System.out.println("Path: " + testUrl.getPath());
			
			URL url = new URL(service.getPrismeRolesByTokenUrl());
			Client client = ClientBuilder.newClient();
			Response response = client.target(getTargetFromUrl(url))
					.path(url.getPath())
					.queryParam("id", "cris@cris.com")
					.queryParam("password", "cris@cris.com")
					.queryParam("token", ssoToken)
					.request().get();
//			Response response = client.target("https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json")
//					.queryParam("id", "cris@cris.com")
//					.queryParam("password", "cris@cris.com")
//					.queryParam("token", ssoToken)
//					.request().get();
			String responseJson = response.readEntity(String.class);
			
			ObjectMapper mapper = new ObjectMapper();
			Map<?, ?> map = null;
			try {
				map = mapper.readValue(responseJson, Map.class);
			} catch (Exception e) {
				throw new Exception("Failed parsing json \"" + responseJson);
			}

			System.out.println("Output: \"" + map + "\"");

			
			
			
			
//			final URL url = new URL(urlStr);
//
//			HttpURLConnection httpCon = null;
//
//			//log.debug("Beginning download from " + url);
//			httpCon = getConnection(url, null, null);
//			httpCon.setDoInput(true);
//			httpCon.setRequestMethod("GET");
//			httpCon.setConnectTimeout(30 * 1000);
//			httpCon.setReadTimeout(60 * 60 * 1000);
//			InputStream in = httpCon.getInputStream();
//
//			byte[] buf = new byte[1048576];
//			StringBuilder os = new StringBuilder();
//
//			int read = 0;
//			while ((read = in.read(buf, 0, buf.length)) > 0)
//			{
//				os.append(read);
//			}
			
//			ClientConfig clientConfig = new DefaultClientConfig();
//
//			clientConfig.getFeatures().put(
//					JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//
//			Client client = Client.create(clientConfig);
//
//			final String ssoToken = "%5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC0%5CxF2%5CxE8%5CxA5%5CxD8%5CxE3t%5CxFFUK%22%2C+%22%2CJ%5Cx83%5CxA3%5Cx13k%5Cx96%5CxFC%5CxE6%5CxF3%5CxCF%5CxF2%7C%5CxB8MK%22%2C+%224%5Cf%5Cx85%5CxA7%5Cx13k%5CxB3%5CxBD%5CxB8%5CxB8%5CxD2%5CxBDr%5CxB2%5Cx02K%22%2C+%22%2CS%5CxE8%5CxDBt%5Cx16%5CxFD%5CxD0%5CxC4%5CxDF%5CxAF%5Cx9D%5Cx1F%5CxD3aD%22%5D";
//			WebResource webResource = client
//					//.resource("http://localhost:9090/JerseyJSONExample/rest/jsonServices/send");
//					.resource("https://vadev.mantech.com:4848/rails_prisme/roles/get_roles_by_token.json?id=cris@cris.com&password=cris@cris.com&token=" + ssoToken);
//
//			// https://vadev.mantech.com:4848/rails_prisme/roles/get_user_roles.json?id=cris@cris.com&password=cris@cris.com
//			ClientResponse response = webResource.accept("application/json")
//					.type("application/json").get(ClientResponse.class);
//
//			if (response.getStatus() != 200) {
//				throw new RuntimeException("Failed : HTTP error code : "
//						+ response.getStatus());
//			}
//
//			String output = response.getEntity(String.class);
//
//			System.out.println("Server response .... \n");
//			System.out.println(output);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
