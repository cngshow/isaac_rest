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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.intake;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.rest.api.data.RestXML;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.tokens.EditTokens;

/**
 * {@link IntakeWriteAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.writePathComponent + RestPaths.intakeAPIsPathComponent)
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
public class IntakeWriteAPIs
{
	private static Logger log = LogManager.getLogger(IntakeWriteAPIs.class);
	
	@Context
	private SecurityContext securityContext;
	
	
	/**
	 * Used to submit an XML VETS VHAT XML file to the system, to be processed as a set of edits directly 
	 * onto this database.  The system will conduct various validations on the incoming content, and return
	 * a RestWriteResponse only if it is fully valid.  Any error during processing will result in a RestException
	 * being thrown.
	 * 
	 * @param editToken - 
	 *            EditToken string returned by previous call to /system/editToken
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return a RestWriteResponse with a new token, if the content was read successfully.  
	 * @throws RestException if any processing error happened.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path(RestPaths.vetsXMLComponent)
	public RestWriteResponse readVHATXML(RestXML inputXML) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());
		log.info("VHAT XML was posted for intake - length " + inputXML.xml.length());
		log.debug("Posted XML: '" + inputXML + "'");
		
		//TODO dan has to actually process this XML...
		
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()));
		
	}
}
