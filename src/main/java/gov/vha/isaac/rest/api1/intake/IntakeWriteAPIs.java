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

import java.io.File;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.ochre.api.util.StringUtils;
import gov.vha.isaac.ochre.utility.importer.VHATDeltaImport;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.vuid.VuidWriteAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;

/**
 * {@link IntakeWriteAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@Path(RestPaths.writePathComponent + RestPaths.intakeAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.EDITOR})
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
	 * @param vuidGeneration - true, to generate vuids where missing.  False to not generate any vuids.
	 * Defaults to true / vuid generation on - if not provided
	 * @param editToken - 
	 *            EditToken string returned by previous call to 1/coordinate/editToken
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return a RestWriteResponse with a new token, if the content was read successfully.  
	 * @throws RestException if any processing error happened.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.vetsXMLComponent)
	public RestWriteResponse readVHATXML(String inputXML, 
			@QueryParam(RequestParameters.vuidGeneration) String vuidGeneration) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());
		
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.COORDINATE_PARAM_NAMES,
				RequestParameters.editToken, 
				RequestParameters.vuidGeneration);
		
		boolean generateVuids = true;
		if (StringUtils.isNotBlank(vuidGeneration))
		{
			generateVuids = Boolean.valueOf(vuidGeneration);
		}

		
		log.info("VHAT XML was posted for intake - length " + inputXML.length() + " with vuid generation " + generateVuids);
		log.debug("Posted XML: '" + inputXML + "'");
		
		File debugOutput = new File(StringUtils.stringForFortify(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "xmlIntakeDebug"));
		debugOutput.mkdir();
		
		//need to enforce single threading on this process for now, as there are some issues with static code in ConverterUUID, and also 
		//potential issues with the way some of the validation checks are being done.
		synchronized (log) {
			try
			{
				new VHATDeltaImport(inputXML, 
					Get.identifierService().getUuidPrimordialFromConceptId(RequestInfo.get().getEditCoordinate().getAuthorSequence()).get(),
					Get.identifierService().getUuidPrimordialFromConceptId(RequestInfo.get().getEditCoordinate().getModuleSequence()).get(), 
					Get.identifierService().getUuidPrimordialFromConceptId(RequestInfo.get().getEditCoordinate().getPathSequence()).get(),
					generateVuids ? 
					(() -> 
					{
						try
						{
							return new Long(VuidWriteAPIs.allocateVUID(1, "XML Import", RequestInfo.get().getEditToken().getUser().getSSOToken().get()).startInclusive);
						}
						catch (Exception e)
						{
							throw new RuntimeException("Failed to allocate a new VUID", e);
						}
					}) : null,
					debugOutput);
			}
			catch (Exception e)
			{
				log.info("Failed processing input xml", e);
				throw new RestException("Could not process the provided XML: " + e.getMessage());
			}
			
			return new RestWriteResponse(RequestInfo.get().getEditToken().renewToken());
		}
		
	}
}
