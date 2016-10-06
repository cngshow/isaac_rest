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
	public static User getUser(String token) throws JsonParseException, JsonMappingException, IOException {
		//URL url = new URL("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
		
		ObjectMapper mapper = new ObjectMapper();

		String json = "{\"roles\":[{\"id\":10000,\"name\":\"read_only\",\"resource_id\":null,\"resource_type\":null,\"created_at\":\"2016-09-13T14:48:18.000Z\",\"updated_at\":\"2016-09-13T14:48:18.000Z\"}],\"token_parsed?\":true,\"user\":\"VHAISHArmbrD\",\"type\":\"ssoi\",\"id\":10005}";
		
		//Map map = mapper.readValue(url, Map.class);
		Map map = mapper.readValue(json, Map.class);

		Boolean token_parsed = (Boolean)map.get("token_parsed?");
		String userName = (String)map.get("user");
		String userType = (String)map.get("ssoi");
		Integer userId = (Integer)map.get("id");
		Set<Role> roleSet = new HashSet<>();
		Collection<?> roles = (Collection<?>)map.get("roles");
		for (Object roleMapObject : roles) {
			Map roleMap = (Map)roleMapObject;
			Integer roleId = (Integer)roleMap.get("id");
			String roleName = (String)roleMap.get("name");
			
			roleSet.add(new Role(roleId, roleName));
		}
		
		return new User(token_parsed, userName, userType, userId, roleSet);
//		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		con.connect();
//
//		java.io.BufferedReader in = new java.io.BufferedReader
//				(new java.io.InputStreamReader(con.getInputStream()));
//		
//		StringBuilder sb = new StringBuilder();
//		String line = null;
//		for (; (line = in.readLine()) != null; ) {
//			if (! line.trim().startsWith("<link ")
//					&& ! line.trim().equals("&nbsp;")) {
//				// Web pages sometimes have <link> tags without terminators
//				// and &nbsp;, which the parser can't handle
//				sb.append(line + "\n");
//			}
//		}
//		
//		//HttpClient client = new DefaultHttpClient();
//		HttpGet request = new HttpGet("https://vaauscttweb81.aac.va.gov/rails_prisme/roles/get_roles_by_token.json?token=" + token);
//		HttpResponse response = client.execute(request);
//		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
//		String line = "";
//		while ((line = rd.readLine()) != null) {
//			System.out.println(line);
//		}
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
