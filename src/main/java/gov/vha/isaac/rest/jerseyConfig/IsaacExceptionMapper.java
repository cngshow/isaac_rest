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
package gov.vha.isaac.rest.jerseyConfig;


import java.io.IOException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link IsaacExceptionMapper}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Provider
public class IsaacExceptionMapper implements ExceptionMapper<Exception>
{
	private static Logger log = LogManager.getLogger("web");
	
	private static String serialize(RestExceptionResponse restExceptionResponse) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(restExceptionResponse);
	}

	private static Response buildResponse(RestExceptionResponse response) {
		String json = null;
		try {
			json = serialize(response);
		} catch (JsonProcessingException e) {
			log.error("Failed serializing-to-json RestExceptionResponse " + response);
			json = "{\"conciseMessage\":\"Failed serializing response\"}";
		}
		Status status = response.status != null ? response.status : Status.INTERNAL_SERVER_ERROR;
		
		return Response.status(status).entity(json).type(MediaType.APPLICATION_JSON).build();
	}
	
	@Override
	public Response toResponse(Exception ex)
	{
		Status status = Status.INTERNAL_SERVER_ERROR; // Default is INTERNAL_SERVER_ERROR
		
		// Place any Exception with its own response status and handling here
		if (ex instanceof SecurityException || (ex instanceof IOException && ((IOException)ex).getCause() instanceof SecurityException))
		{
			log.info("SecurityException: " + ex.getMessage());

			RestExceptionResponse response = new RestExceptionResponse(
					"SecurityException",
					ex.getMessage(),
					null,
					null,
					Status.FORBIDDEN);
			return buildResponse(response);
		}

		boolean sendMessage = false;

		// Place any Exceptions that fall through to 500 here
		if (ex instanceof ClientErrorException)
		{
			log.info("ClientError:" + ex.toString());
		}
		else if (ex.getMessage() != null && ex.getMessage().startsWith("The system is not yet ready"))
		{
			status = Status.SERVICE_UNAVAILABLE;
			sendMessage = true;
			log.warn(ex.getMessage());
		}
		else if (ex.getMessage() != null && ex.getMessage().startsWith("Edit Token is no longer valid for write"))
		{
			status = Status.UNAUTHORIZED;
			sendMessage = true;
			log.info(ex.getMessage());
		}
		else if (ex instanceof RestException
				&& (((RestException)ex).getParameterSpecificMessage() != null || ex.getCause() == null)
			)
		{
			// Only accept parameter-specific RestException or one without a cause as a RestException
			log.info("RestException", ex);
		}
		else
		{
			log.error("Unexpected", ex);
		}
		
		if (ex  instanceof ClientErrorException)
		{
			status =  Status.fromStatusCode(((ClientErrorException)ex).getResponse().getStatus());
			RestExceptionResponse exceptionResponse = new RestExceptionResponse(
					ex.getMessage(),
					ex.getMessage(),
					null,
					null,
					status);
			return buildResponse(exceptionResponse);
		}
		else if (sendMessage)
		{
			// Assume that message is explicit
			String response = ex.getMessage();
			RestExceptionResponse exceptionResponse = new RestExceptionResponse(
					response,
					ex.toString(),
					null,
					null,
					status);
			return buildResponse(exceptionResponse);
		}
		else if (
				ex instanceof RestException
				&& (((RestException)ex).getParameterSpecificMessage() != null || ex.getCause() == null))
		{			
			RestException re = (RestException) ex;
			
			// Assume that RestException indicates a BAD_REQUEST
			status = Status.BAD_REQUEST;
			RestExceptionResponse exceptionResponse = new RestExceptionResponse(
					ex.getMessage(),
					ex.toString(),
					re.getParameterName(),
					re.getParameterValue(),
					status);
			return buildResponse(exceptionResponse);
		}
		else
		{
			String response = "Unexpected Internal Error";

			RestExceptionResponse exceptionResponse = new RestExceptionResponse(
					response,
					response,
					null,
					null,
					status);
			return buildResponse(exceptionResponse);
		}
	}
}
