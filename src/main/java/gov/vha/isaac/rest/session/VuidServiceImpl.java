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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.rest.api.data.vuid.RestVuidBlockData;
import gov.vha.isaac.rest.api.exceptions.RestExceptionResponse;

/**
 * The Class VuidServiceImpl
 *
 * {@link VuidServiceImpl}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Service(name="rest-vuidService")
@Rank(value = 10)
@Singleton
public class VuidServiceImpl implements VuidService {
	private static Logger log = LogManager.getLogger(VuidServiceImpl.class);
	
	protected VuidServiceImpl() {
		//for HK2
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.VuidService#allocate(int, java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<RestVuidBlockData> allocate(int blockSize, String reason, String ssoToken) {
		String vuidServiceUrl = getVuidServiceUrl();

		if (StringUtils.isBlank(vuidServiceUrl)) {
			return Optional.empty();
		}
		URL url = null;
		try {
			url = new URL(vuidServiceUrl);
		} catch (MalformedURLException e) {
			log.error("Malformed VUID Service URL \"" + vuidServiceUrl + "\"", e);
			throw new RuntimeException("Malformed VUID Service URL \"" + vuidServiceUrl + "\"", e); 
		}

		Map<String, String> params = new HashMap<>();
		params.put("blockSize", blockSize + "");
		params.put("reason", reason);
		params.put("ssoToken", ssoToken);
		String resultJson = VuidServiceUtils.getResultJsonFromVuidService(PrismeServiceUtils.getTargetFromUrl(url), url.getPath(), params);
		log.trace("Retrieved from " + vuidServiceUrl + " resultJson=\"" + resultJson + "\"");
		
		if (resultJson.contains(RestExceptionResponse.class.getName()))
		{
			boolean ok = false;
			try
			{
				RestExceptionResponse message = new ObjectMapper().readValue(resultJson, RestExceptionResponse.class);
				log.error("VUID server sent an error: " + message.conciseMessage + " " + message.verboseMessage);
				ok = true;
				throw new RuntimeException(message.conciseMessage + " " + message.verboseMessage);
			}
			catch (Exception e)
			{
				if (!ok || !(e instanceof RuntimeException))
				{
					log.error("Error trying to deserialize upstream error message from '" + resultJson + "'", e);
					throw new RuntimeException("The VUID server returned an unknown error");
				}
				else
				{
					throw (RuntimeException)e;
				}
			}
		}
		else if (resultJson.contains(RestVuidBlockData.class.getName()))
		{
			RestVuidBlockData vuids = null;
			try {
				vuids = new ObjectMapper().readValue(resultJson, RestVuidBlockData.class);
				return Optional.of(vuids);
			} catch (Exception e) {
				log.error("Failed unmarshalling RestVuidBlockData json from \"" + vuidServiceUrl + "\"" + " with json: '" + resultJson + "'", e);
				throw new RuntimeException("Failed unmarshalling RestVuidBlockData json from \"" + vuidServiceUrl + "\"", e);
			}
		}
		else
		{
			log.error("Unexpected response from vuid server '" + resultJson + "'");
			throw new RuntimeException("The VUID server returned an unknown error");
		}
	}

	public String getVuidServiceUrl() {
		String prismeRootUrl =  PrismeServiceUtils.getPrismeProperties().getProperty("prisme_root");
		if (StringUtils.isBlank(prismeRootUrl)) {
			return null;
		}

		//http://localhost:8181/vuid-rest/write/1/vuids/allocate
		String prismeWebServer = prismeRootUrl.replace("rails_prisme", "vuid-rest/write/1/vuids/allocate");
		return prismeWebServer;
	}
}
