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
package gov.vha.isaac.soap;

import gov.va.med.term.services.exception.STSException;
import gov.vha.isaac.soap.transfer.ConceptDetailTransfer;

import gov.vha.isaac.soap.transfer.MapEntryValueListTransfer;
import gov.vha.isaac.soap.transfer.SourcesTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsListTransfer;

import java.util.Collection;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * 
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 *
 */

@WebService(name="sts", serviceName="ctService", targetNamespace="urn:gov:va:med:sts:webservice:ct")
public class CommonTerminologyWS
{
	private static Logger log = LogManager.getLogger(CommonTerminologyWS.class);
	
	
	/**
	 * ReturnConceptDetails	returns the details for a given concept.
	 *
	 * @param codeSystemVuid 
	 * 		CodeSystemVUID (Required) The VUID of the Concept’s Code System
	 * @param versionName 
	 * 		VersionName (Required) The Code System Version Name that contains the Concept to return. To return the most recent Code System Version the Code System Version Name ‘current’ can be used.
	 * @param code 
	 * 		Code (Required) The Code of the Concept to return
	 * 
	 * @return
	 * Concept Code<br/>
	 * Concept Status<br/>
	 * Properties (0+) [Property Type, Property Value, Status]<br/>
	 * Associations (0+) [Association Type, Association Value, Status, Target Code]<br/>
	 * Designations (1+) [Code, Type, Name, Status, Properties (0+) [Type,Value,Status], Value Set Membership]<br/>
	 * 
	 * @throws SOAPException
	 * Code System VUID not found<br/>
	 * Code System Version Name not found<br/>
	 * Concept Code not found<br/>
	 * Invalid Filter Criteria<br/>
	 */
    @WebMethod(operationName="ReturnConceptDetails", action="getConceptDetail")
	public ConceptDetailTransfer getConceptDetail(
			@WebParam(name = "CodeSystemVUID")Long codeSystemVuid,
	        @WebParam(name = "VersionName")String versionName, 
	        @WebParam(name = "Code")String code) throws SOAPException
	{
    	log.info("getConceptDetail [CodeSystemVUID:{}, VersionName:{}, Code:{}]", codeSystemVuid, versionName, code);
    	
        ConceptDetailTransfer conceptDetailTransfer;
        
        try
        {
            conceptDetailTransfer = CommonTerminology.getConceptDetail(codeSystemVuid, versionName, code);
        }
        catch (STSException ex)
        {        	
        	throw new SOAPException(ex.getMessage(), ex);
        	//throw new SoapFault(ex.getMessage(), ex, SoapFault.FAULT_CODE_SERVER);
        }
        
        return conceptDetailTransfer;
	}
   
    /**
     * List the Designations in the Value Set
     * 
     * @param vuid
     * 		VUID (Required) The VUID of the Value Set that contains the Designations to return.
     * @param versionName
     * 		VersionName  (Required)  The Code System Version Name that contains the Designations to return. To return the most recent Code System Version the Code System Version Name ‘current’ can be used.
     * @param designationName
     * 		DesignationName (Optional)  Designations who’s name contains the text to return.
     * @param membershipStatus
     * 		MembershipStatus (Optional) Designations who’s membership status to return.
     * @param pageSize
     * 		PageSize (Optional) Number of records to return per page. If this is omitted, it defaults to 1,000. The maximum allowed value is 5,000.
     * @param pageNumber
     * 		PageNumber (Optional) The page of results to return. If this is omitted, it defaults to 1.
     * 
     * @return
     * Designation Code<br/>
     * Designation Name<br/>
     * Designation Status<br/>
     * Designation Type<br/>
     * Value Set Membership Status<br/>
     * Total Number of Records<br/>
     * 
     * @throws SOAPException
	 * Code System VUID not found<br/>
	 * Code System Version Name not found<br/>
	 * Invalid Filter Criteria<br/>
     */
    @WebMethod(operationName="ListValueSetContents", action="getValueSetContents")
    public ValueSetContentsListTransfer getValueSetContents(
    		@WebParam(name="VUID")Long vuid, 
    		@WebParam(name="VersionName")String versionName,
            @WebParam(name="DesignationName")String designationName, 
            @WebParam(name="MembershipStatus")String membershipStatus,
            @WebParam(name="PageSize") Integer pageSize, 
            @WebParam(name="PageNumber") Integer pageNumber) throws SOAPException
    {
    	log.info("getValueSetContents [VUID:{}, VersionName:{}, DesignationName:{}, MembershipStatus:{}, PageSize:{}, PageNumber:{}]",
    			vuid, versionName, designationName, membershipStatus, pageSize, pageNumber);
    	
    	ValueSetContentsListTransfer results = null; 
        try
        {
            results = CommonTerminology.getValueSetContents(vuid, versionName, designationName, membershipStatus, pageSize, pageNumber);
        }
        catch (STSException ex)
        {
        	throw new SOAPException(ex);
        }
        return results;
        
    }

    /**
     * List the Map Entries in the current version of a given Map Set
	 * By default a Map Entry is looked up using the Concept Code of the Source Concept 
	 * and returns the Concept Code of the Target Concept. However, a Map Entry can be configured 
	 * to accept a Designation Name as a lookup for the Source Concept or a Designation Code for 
	 * the Source Concept. Similarly, the MapSet can be configured to return a Designation Name or 
	 * Designation Code for the Target Concept. This configuration is done on the server side depending 
	 * on the business need.

     * @param mapSetVuid
     * 		MapSetVUID  (Required)  The Map Set who’s Map Entries to return.<br/>
     * @param mapSetVersionName
     * 		MapSetVersionName  (Required)  The Map Set Version Name that contains the Concepts to determine. To check the most recent Map Set Version the Map Set Version Name ‘current’ can be used.<br/>
     * @param sources
     * 		Sources  (Required)  Either the Source Concept Codes, Designation Codes, or exact Designation Names of the Source Concept of the Map Entries to return.<br/>
     * @param sourceDesignationTypeName
     * 		SourceDesignationTypeName (Optional) The Designation Type name of the Source Concept to return. (Only referenced if the Source is a designation name or designation code. If not supplied and the Source is designation name, the Source will be matched against designations of the preferred designation type for the source codesystem)<br/>
     * @param targetDesignationTypeName
     * 		TargetDesignationTypeName (Optional) The Designation Type name of the Target Concept to return.<br/>
     * @param pageSize
     * 		PageSize (Optional) Number of records to return per page. If this is omitted, it defaults to 1,000. The maximum allowed value is 5,000.<br/>
     * @param pageNumber
     * 		PageNumber (Optional) The page of results to return. If this is omitted, it defaults to 1.<br/>
     * 
     * @return
     * Map Entry VUID<br/>
     * Source Concept Value (Concept Code, Preferred Designation Code or Preferred Designation Name)<br/>
     * Source Designation Type Name<br/>
     * Target Concept Value(Concept Code, Preferred Designation Code or Preferred Designation Name)<br/>
     * Target Designation Type Name<br/>
     * Target Designation Name<br/>
     * Target Code System VUID<br/>
     * Target Code System Version Name<br/>
     * Map Entry Order<br/>
     * Map Entry Status<br/>
     * Total Number of Records<br/>
     * 
     * @throws SOAPException
	 * Map Set VUID not found<br/>
	 * Target Designation Type Name not found<br/>
	 * Invalid Filter Criteria<br/>
     */
    @WebMethod(operationName="ListMapEntriesFromSource", action="ListMapEntriesFromSource")
    public MapEntryValueListTransfer getMapEntriesFromSource(
    		@WebParam(name="MapSetVUID") Long mapSetVuid,
    		@WebParam(name="MapSetVersionName") String mapSetVersionName,
            @WebParam(name="Sources") SourcesTransfer sources,
            @WebParam(name="SourceDesignationTypeName") String sourceDesignationTypeName,
            @WebParam(name="TargetDesignationTypeName") String targetDesignationTypeName,
            @WebParam(name="PageSize")Integer pageSize, 
            @WebParam(name="PageNumber")Integer pageNumber) throws SOAPException
    {
    	log.info("getValueSetContents [MapSetVUID:{}, MapSetVersionName:{}, Sources:{}, SourceDesignationTypeName:{}, TargetDesignationTypeName:{}, PageSize:{}, PageNumber:{}]",
    			mapSetVuid, mapSetVersionName, sources, sourceDesignationTypeName, targetDesignationTypeName, pageSize, pageNumber);
    	
        MapEntryValueListTransfer mapEntrySourceTransfer = null;
        try
        {
        	Collection<String> sourceList = (sources != null && sources.getSources() != null && sources.getSources().size() > 0) ? sources.getSources() : null;
            mapEntrySourceTransfer = CommonTerminology.getMapEntriesFromSources(mapSetVuid, mapSetVersionName,
            		sourceList, sourceDesignationTypeName, targetDesignationTypeName,
                    pageSize, pageNumber);
        }
        catch (STSException ex)
        {
        	throw new SOAPException(ex);
        }
        
        return mapEntrySourceTransfer;
    }
}
