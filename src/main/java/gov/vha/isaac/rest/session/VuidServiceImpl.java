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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;
import javax.ws.rs.client.Entity;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.rest.api.data.RestBoolean;
import gov.vha.isaac.rest.api.data.vuid.RestVuidBlockData;
import gov.vha.isaac.rest.api.exceptions.RestException;
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

	/**
	 * Validates a VUID. If a VUID is less than the next vuid
	 * stored in the database, then it is considered 'valid'
	 * (as a number, no check if it's been used).
	 * 
	 * So we don't forget why this is:
	 * 
	 	15:47 Neill Robins
		Are the VUIDs going to come in negative values in test cases?
		15:49 Dan Armbrust
		bit of an open question how much we should validate when in test mode with negative vuids.
		it may be, that we don't do this step when in test mode.
		because the test would be pointless, if we aren't the authority server.
		but when we are, we would do the test.
		15:51 Neill Robins
		I'm curious how testers will test this part
		Or will they?
		15:51 Dan Armbrust
		they can put it in 'real'mode
		though they know it isn't in real mode
	 *
	 * @param vuidToValidate The VUID that should be validated
	 * @return true, if the VUID is valid, false if it's certain conditions aren't met, an excpetion is thrown for a bad VUID
	 */
	public boolean isVuidValid(long vuidToValidate) throws RestException {
		String vuidServiceUrl = getVuidValidateServiceUrl();

		if (StringUtils.isBlank(vuidServiceUrl)) {
			throw new RuntimeException("Failed determining VUID validation service URL");
		}
		URL url = null;
		try {
			url = new URL(vuidServiceUrl);
		} catch (MalformedURLException e) {
			log.error("Malformed VUID Service URL \"" + vuidServiceUrl + "\"", e);
			throw new RuntimeException("Malformed VUID Service URL \"" + vuidServiceUrl + "\"", e); 
		}

		Map<String, String> params = new HashMap<>();
		params.put(RequestParameters.vuid, vuidToValidate + "");
		String target = PrismeServiceUtils.getTargetFromUrl(url);
		String resultJson = null;
		try {
			resultJson = VuidServiceUtils.getResultJsonFromVuidService(target, url.getPath(), params, (Entity<?>)null);
		} catch (Exception e) {
			String msg = "FAILED vuid validation request " + target + url.getPath() + "?vuid=" + vuidToValidate;
			log.error(msg, e);
			
			throw new RestException(msg);
		}
		
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
		else if (resultJson.contains(RestBoolean.class.getName()))
		{
			RestBoolean valid = null;
			try {
				valid = new ObjectMapper().readValue(resultJson, RestBoolean.class);
				return valid.value;
			} catch (Exception e) {
				log.error("Failed unmarshalling RestBoolean json from \"" + vuidServiceUrl + "\"" + " with json: '" + resultJson + "'", e);
				throw new RuntimeException("Failed unmarshalling RestBoolean json from \"" + vuidServiceUrl + "\"", e);
			}
		}
		else
		{
			log.error("Unexpected response from vuid server '" + resultJson + "'");
			throw new RuntimeException("The VUID server returned an unknown error");
		}
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.VuidService#allocate(int, java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<RestVuidBlockData> allocate(int blockSize, String reason, String ssoToken) {

		// Log value of ssoToken parameter
		log.info(RequestParameters.ssoToken + "==\"" + ssoToken + "\"");

		String vuidServiceUrl = getVuidAllocateServiceUrl();

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
		String target = PrismeServiceUtils.getTargetFromUrl(url);
		String resultJson = VuidServiceUtils.getResultJsonFromVuidService(target, url.getPath(), params, Entity.xml(""));
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

	public String getVuidValidateServiceUrl() {
		String prismeRootUrl =  PrismeServiceUtils.getPrismeProperties().getProperty("prisme_root");
		if (StringUtils.isBlank(prismeRootUrl)) {
			return null;
		}

		//http://localhost:8181/vuid-rest/1/vuids/validate
		String prismeWebServer = prismeRootUrl.replace("rails_prisme", "vuid-rest/1/vuids/validate");
		return prismeWebServer;
	}
	public String getVuidAllocateServiceUrl() {
		String prismeRootUrl =  PrismeServiceUtils.getPrismeProperties().getProperty("prisme_root");
		if (StringUtils.isBlank(prismeRootUrl)) {
			return null;
		}

		//http://localhost:8181/vuid-rest/write/1/vuids/allocate
		String prismeWebServer = prismeRootUrl.replace("rails_prisme", "vuid-rest/write/1/vuids/allocate");
		return prismeWebServer;
	}
}
