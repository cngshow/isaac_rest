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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
import java.util.Map;
import java.util.Set;

/**
 * 
 * {@link PRISMEServices}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class PRISMEServices {
	// TODO Joel implement prisme_all_roles_url=https://vaauscttdbs80.aac.va.gov:8080/rails_prisme/roles/get_all_roles.json
	/*
	 * TODO implement https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_all_roles
	 * returning ["super_user","administrator","read_only","editor","reviewer","approver","manager"]
	 */
	public static String[] getAllRoles() {
		return new String[] { "super_user","administrator","read_only","editor","reviewer","approver","manager" };
	}

	public static User getUser(String token) throws JsonParseException, JsonMappingException, IOException {
		/*
		 * Example URL for get_roles_by_token
		 * URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
		 */
		
		ObjectMapper mapper = new ObjectMapper();

		String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
		
		// TODO Joel implement access to PRISME API

		//Map map = mapper.readValue(url, Map.class);
		Map<?,?> map = mapper.readValue(json, Map.class);

		Boolean token_parsed = (Boolean)map.get("token_parsed?");
		String userName = (String)map.get("user");
		String userType = (String)map.get("ssoi");
		Integer userId = (Integer)map.get("id");
		Set<Role> roleSet = new HashSet<>();
		Collection<?> roles = (Collection<?>)map.get("roles");
		for (Object roleMapObject : roles) {
			Map<?,?> roleMap = (Map<?,?>)roleMapObject;
			Integer roleId = (Integer)roleMap.get("id");
			String roleName = (String)roleMap.get("name");
			
			roleSet.add(new Role(roleId, roleName));
		}
		
		return new User(token_parsed, userName, userType, userId, roleSet);
	}

	public static boolean hasRole(String token, String roleName) throws JsonParseException, JsonMappingException, IOException {
		return getUser(token).getRoles().contains(roleName.trim());
	}

	/**
	 * 
	 */
	public PRISMEServices() {
		// TODO Auto-generated constructor stub
	}
	

//	private static final String JSON_ROLES = "http://localhost:3000/roles/get_roles.json";
//	
//	public String fetchJSON(String user, String password) throws Exception {
//		String userEncoded = URLEncoder.encode(user, "UTF-8");
//		String passwordEncoded = URLEncoder.encode(password, "UTF-8");
//		String urlString = JSON_ROLES + "?id=" + userEncoded + "&password=" + passwordEncoded;
//		System.out.println(urlString);
//		URL prismeRolesUrl = new URL(urlString);
//		HttpURLConnection urlConnection = null;
//		StringBuilder result = new StringBuilder();
//		try {
//			urlConnection = (HttpURLConnection) prismeRolesUrl.openConnection();
//			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//			String line = null;
//			while ((line = r.readLine()) != null) {
//				result.append(line);
//			}
//		} finally {
//			urlConnection.disconnect();
//		}
//		return result.toString();
//	}
//	
//	public void parseRoleJSON(String jsonString) throws JSONException {
//        JSONArray rolesArray = new JSONArray(jsonString);
//        int length = rolesArray.length();
//        for (int i = 0; i < length; i++) {
//        	JSONObject obj = rolesArray.getJSONObject(i);
//        	String role = obj.getString("name");
//        	System.out.println("Role " + i + " is " + role);
//        }
//	}
//	
//	public static void main(String[] args) throws Exception{
//		PRISMEServices r = new PRISMEServices();
//		r.parseRoleJSON(r.fetchJSON("cshupp@gmail.com", "cshupp@gmail.com"));
//	}

	public static void main(String...argv) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper reader = new ObjectMapper();
		
		/*
		 * Example SSO Token
		 * %5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC2%5CxEE%5CxFA%5CxE1%5Cx94%5CxBF3%5CxA9%5Cx16K%22%2C+%22%7EK%5CxC4%5CxEFXk%5Cx80%5CxB1%5CxA3%5CxF3%5Cx8D%5CxB1%5Cx7F%5CxBC%5Cx02K%22%2C+%22k%5Cf%5CxDC%5CxF7%2CP%5CxB2%5Cx97%5Cx99%5Cx99%5CxE0%5CxE1%7C%5CxBF%5Cx1DK%22%2C+%22J%5Cf%5Cx9B%5CxD8w%5Cx15%5CxFE%5CxD3%5CxC7%5CxDC%5CxAC%5Cx9E%5Cx1C%5CxD0bG%22%5D
		 */
		String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
		//String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\",\"type\":\"Role\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
	
	
		Map user = reader.readValue(json, Map.class);
		
		System.out.println(user);
		
		for (Object key : user.keySet()) {
			System.out.println(key + ": " + user.get(key).getClass().getName() + ": " + user.get(key));
			if (user.get(key) instanceof Iterable) {
				for (Object role : (Iterable)user.get(key)) {
					System.out.println("\t" + role.getClass().getName() + ": " + role);
					if (role instanceof Map) {
						for (Object roleField : ((Map)role).keySet()) {
							System.out.println("\t\t" + roleField + ": " + (((Map)role).get(roleField) != null ? ((Map)role).get(roleField).getClass().getName() : null) + ": " + ((Map)role).get(roleField));
						}
					}
				}
			}
		}
	}
}
