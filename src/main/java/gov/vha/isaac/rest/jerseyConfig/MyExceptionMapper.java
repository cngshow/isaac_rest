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


import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.LoggerFactory;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link MyExceptionMapper}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception>
{
	@Override
	public Response toResponse(Exception ex)
	{
		// Place any Exception with its own response status and handling here
		if (ex instanceof SecurityException)
		{
			LoggerFactory.getLogger("web").info("SecurityException: " + ex.getMessage());
			return Response.status(403).entity(ex.getMessage()).type("text/plain").build();
		}

		boolean sendMessage = false;

		// Place any Exceptions that fall through to 500 here
		if (ex instanceof ClientErrorException)
		{
			LoggerFactory.getLogger("web").info("ClientError:" + ex.toString());
		}
		else if (ex.getMessage() != null && ex.getMessage().startsWith("The system is not yet ready"))
		{
			sendMessage = true;
			LoggerFactory.getLogger("web").warn(ex.getMessage());
		}
		else if (ex.getMessage() != null && ex.getMessage().startsWith("Edit Token is no longer valid for write"))
		{
			sendMessage = true;
			LoggerFactory.getLogger("web").info(ex.getMessage());
		}
		
		else
		{
			LoggerFactory.getLogger("web").error("Unexpected", ex);
		}
		
		String response;
		if (ex  instanceof ClientErrorException)
		{
			return ((ClientErrorException)ex).getResponse();
		}
		else if (ex instanceof RestException || sendMessage)
		{
			response = ex.toString();
		}
		else
		{
			response = "Unexpected Internal Error";
		}
		return Response.status(500).entity(response).type("text/plain").build();
	}
}
