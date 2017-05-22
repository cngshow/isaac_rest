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

import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import gov.vha.isaac.ochre.api.LookupService;

/**
 * 
 * {@link VuidServiceUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class VuidServiceUtils {
	//private static Logger log = LogManager.getLogger(VuidServiceUtils.class);
	
	private VuidServiceUtils() {}

	private static String getResultJsonFromVuidService(WebTarget targetWithPath, Map<String, String> params) {
		for (Map.Entry<String, String> entry : params.entrySet()) {
			targetWithPath = targetWithPath.queryParam(entry.getKey(), entry.getValue());
		}
		Response response = targetWithPath.request().post(Entity.xml(""));
		
		String responseJson = response.readEntity(String.class);
	
		return responseJson;
	}
	
	static String getResultJsonFromVuidService(String targetStr, String pathStr, Map<String, String> params) {
		ClientService clientService = LookupService.getService(ClientService.class);
		WebTarget target = clientService.getClient().target(targetStr);
		target = target.path(pathStr);
		
		return getResultJsonFromVuidService(target, params);
	}
}
