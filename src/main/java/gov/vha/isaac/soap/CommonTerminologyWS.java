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

import java.util.Collection;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.va.med.term.services.exception.STSException;
import gov.vha.isaac.soap.transfer.CodeSystemConceptsTransfer;
import gov.vha.isaac.soap.transfer.CodeSystemVersionTransfer;
import gov.vha.isaac.soap.transfer.CodeSystemsListTransfer;
import gov.vha.isaac.soap.transfer.ConceptCodesTransfer;
import gov.vha.isaac.soap.transfer.ConceptDetailTransfer;
import gov.vha.isaac.soap.transfer.DomainDetailTransfer;
import gov.vha.isaac.soap.transfer.DomainListTransfer;
import gov.vha.isaac.soap.transfer.MapEntryDetailListTransfer;
import gov.vha.isaac.soap.transfer.MapEntryValueListTransfer;
import gov.vha.isaac.soap.transfer.MapSetDetailListTransfer;
import gov.vha.isaac.soap.transfer.PathsTransfer;
import gov.vha.isaac.soap.transfer.RelationshipDetailListTransfer;
import gov.vha.isaac.soap.transfer.RelationshipDetailTransfer;
import gov.vha.isaac.soap.transfer.RelationshipTypeListTransfer;
import gov.vha.isaac.soap.transfer.RelationshipTypeTransfer;
import gov.vha.isaac.soap.transfer.SourcesTransfer;
import gov.vha.isaac.soap.transfer.UsageContextDetailTransfer;
import gov.vha.isaac.soap.transfer.UsageContextListTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsListTransfer;
import gov.vha.isaac.soap.transfer.ValueSetDetailTransfer;
import gov.vha.isaac.soap.transfer.ValueSetListTransfer;



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
        	throw new SOAPException(ex);
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
    
	/*
	 * Everything below this was added so that clients don't have an issue with validating unused SOAP
	 * operations.
	 */

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVuid
	 * @param versionName
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnCodeSystemDetails", action = "getCodeSystemDetail")
	public CodeSystemVersionTransfer getCodeSystemDetails(@WebParam(name = "CodeSystemVUID") Long codeSystemVuid,
			@WebParam(name = "VersionName") String versionName) throws SOAPException {
		
		CodeSystemVersionTransfer codeSystemTransfer = null;
		return codeSystemTransfer;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListCodeSystems", action = "ListCodeSystems")
	public CodeSystemsListTransfer ListCodeSystems(@WebParam(name = "CodeSystemName") String codeSystemName,
			@WebParam(name = "PageSize") Integer pageSize, @WebParam(name = "PageNumber") Integer pageNumber)
			throws SOAPException {
		
		CodeSystemsListTransfer codeSystemsListTransfer = null;
		return codeSystemsListTransfer;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param domainName
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListConceptDomains", action = "getDomains")
	public DomainListTransfer getDomains(@WebParam(name = "DomainName") String domainName,
			@WebParam(name = "PageNumber") Integer pageNumber, @WebParam(name = "PageSize") Integer pageSize)
			throws SOAPException {
		
		DomainListTransfer domains = null;
		return domains;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param domainVuid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListConceptDomainBindings", action = "ListConceptDomainBindings")
	public DomainListTransfer getDomainConcepts(@WebParam(name = "DomainVUID") Long domainVuid,
			@WebParam(name = "PageNumber") Integer pageNumber, @WebParam(name = "PageSize") Integer pageSize)
			throws SOAPException {
		
		DomainListTransfer domainConcepts = null;
		return domainConcepts;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param domainVuid
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnConceptDomainDetails", action = "getConceptDomainDetails")
	public DomainDetailTransfer getDomainDetails(@WebParam(name = "DomainVUID") Long domainVuid) throws SOAPException {
		
		DomainDetailTransfer domainDetails = null;
		return domainDetails;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param nameFilter
	 * @param status
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListValueSets", action = "getValueSets")
	public ValueSetListTransfer getValueSets(@WebParam(name = "ValueSetName") String nameFilter,
			@WebParam(name = "ValueSetStatus") String status, @WebParam(name = "PageSize") Integer pageSize,
			@WebParam(name = "PageNumber") Integer pageNumber) throws SOAPException {
		
		ValueSetListTransfer results = null;
		return results;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param vuid
	 * @param versionName
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnValueSetDetails", action = "getValueSetDetail")
	public ValueSetDetailTransfer getValueSetDetail(@WebParam(name = "VUID") Long vuid,
			@WebParam(name = "VersionName") String versionName) throws SOAPException {
		
		ValueSetDetailTransfer valueSetDetail = null;
		return valueSetDetail;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVuid
	 * @param versionName
	 * @param designationNameFilter
	 * @param conceptCodeFilter
	 * @param conceptStatusFilter
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListCodeSystemConcepts", action = "getCodeSystemConcepts")
	public CodeSystemConceptsTransfer getCodeSystemConcepts(@WebParam(name = "CodeSystemVUID") Long codeSystemVuid,
			@WebParam(name = "VersionName") String versionName,
			@WebParam(name = "DesignationName") String designationNameFilter,
			@WebParam(name = "ConceptCode") String conceptCodeFilter,
			@WebParam(name = "ConceptStatus") Boolean conceptStatusFilter,
			@WebParam(name = "PageSize") Integer pageSize, @WebParam(name = "PageNumber") Integer pageNumber)
			throws SOAPException {
		
		CodeSystemConceptsTransfer codeSystemConceptsTransfer = null;
		return codeSystemConceptsTransfer;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param relationshipTypeName
	 * @param codeSystemVuid
	 * @param versionName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListAssociationTypes", action = "getRelationshipTypes")
	public RelationshipTypeListTransfer getRelationshipTypes(
			@WebParam(name = "AssociationTypeName") String relationshipTypeName,
			@WebParam(name = "VUID") Long codeSystemVuid, @WebParam(name = "VersionName") String versionName,
			@WebParam(name = "PageSize") Integer pageSize, @WebParam(name = "PageNumber") Integer pageNumber)
			throws SOAPException {
		
		RelationshipTypeListTransfer relationshipTypeListTransfer = null;
		return relationshipTypeListTransfer;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param relationshipTypeName
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnAssociationTypeDetails", action = "getRelationshipTypeDetails")
	public RelationshipTypeTransfer getRelationshipTypeDetails(
			@WebParam(name = "AssociationTypeName") String relationshipTypeName) throws SOAPException {
		
		RelationshipTypeTransfer relationshipTypeTransfer = null;
		return relationshipTypeTransfer;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVersionName
	 * @param designationVuid
	 * @param valueSetVuid
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "CheckDesignationValueSetMembership", action = "checkDesignationValueSetMembership")
	public String checkDesignationValueSetMembership(
			@WebParam(name = "CodeSystemVersionName") String codeSystemVersionName,
			@WebParam(name = "DesignationVUID") Long designationVuid,
			@WebParam(name = "ValueSetVUID") Long valueSetVuid) throws SOAPException {
		
		String result = null;
		return result;
	}
	
	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVersionName
	 * @param conceptCode
	 * @param domainVuid
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "CheckConceptToConceptDomainAssociation", action = "checkConceptToConceptDomainAssociation")
	public boolean checkConceptToConceptDomainAssociation(
			@WebParam(name = "CodeSystemVersionName") String codeSystemVersionName,
			@WebParam(name = "ConceptCode") String conceptCode, @WebParam(name = "DomainVUID") Long domainVuid)
			throws SOAPException {

		return false;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVuid
	 * @param versionName
	 * @param sourceConceptCode
	 * @param targetConceptCode
	 * @param relationshipTypeName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListAssociations", action = "getRelationships")
	public RelationshipDetailListTransfer getRelationships(@WebParam(name = "VUID") Long codeSystemVuid,
			@WebParam(name = "VersionName") String versionName,
			@WebParam(name = "SourceConceptCode") String sourceConceptCode,
			@WebParam(name = "TargetConceptCode") String targetConceptCode,
			@WebParam(name = "AssociationTypeName") String relationshipTypeName,
			@WebParam(name = "PageSize") Integer pageSize, @WebParam(name = "PageNumber") Integer pageNumber)
			throws SOAPException {
		
		RelationshipDetailListTransfer relationshipDetailListTransfer = null;
		return relationshipDetailListTransfer;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param relationshipId
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnAssociationDetails", action = "getRelationshipDetails")
	public RelationshipDetailTransfer getRelationshipDetails(@WebParam(name = "AssociationId") Long relationshipId)
			throws SOAPException {
		
		RelationshipDetailTransfer relationshipDetailTransfer = null;
		return relationshipDetailTransfer;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVuid
	 * @param versionName
	 * @param parentConceptCode
	 * @param childConceptCode
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ComputeSubsumptionConceptAssociation", action = "computeSubsumptionRelationship")
	public String computeSubsumptionRelationship(@WebParam(name = "CodeSystemVUID") Long codeSystemVuid,
			@WebParam(name = "VersionName") String versionName,
			@WebParam(name = "ParentConceptCode") String parentConceptCode,
			@WebParam(name = "ChildConceptCode") String childConceptCode) throws SOAPException {
		
		String result = null;
		return result;
	}

	/**
	 * 
	 * @param firstValueSetVuid
	 * @param secondValueSetVuid
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "CheckValueSetSubsumption", action = "checkValueSetSubsumption")
	public boolean checkValueSetSubsumption(@WebParam(name = "FirstValueSetVUID") String firstValueSetVuid,
			@WebParam(name = "SecondValueSetVUID") String secondValueSetVuid) throws SOAPException {

		return false;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param codeSystemVuid
	 * @param versionName
	 * @param sourceConceptCode
	 * @param targetConceptCode
	 * @param relationshipTypeName
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "DetermineTransitiveConceptAssociation", action = "getTransitiveConceptRelationship")
	public PathsTransfer getTransitiveConceptRelationship(@WebParam(name = "CodeSystemVUID") Long codeSystemVuid,
			@WebParam(name = "VersionName") String versionName,
			@WebParam(name = "SourceConceptCode") String sourceConceptCode,
			@WebParam(name = "TargetConceptCode") String targetConceptCode,
			@WebParam(name = "AssociationTypeName") String relationshipTypeName) throws SOAPException {
		
		PathsTransfer results = null;
		return results;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param usageContextName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListUsageContexts", action = "getUsageContexts")
	public UsageContextListTransfer getUsageContexts(@WebParam(name = "UsageContextName") String usageContextName,
			@WebParam(name = "PageSize") Integer pageSize, @WebParam(name = "PageNumber") Integer pageNumber)
			throws SOAPException {
		
		UsageContextListTransfer usageContextListTransfer = null;
		return usageContextListTransfer;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param usageContextVuid
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ReturnUsageContextDetails", action = "getUsageContextDetails")
	public UsageContextDetailTransfer getUsageContextDetails(@WebParam(name = "UsageContextVUID") Long usageContextVuid)
			throws SOAPException

	{
		UsageContextDetailTransfer results = null;
		return results;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param mapSetName
	 * @param sourceCodeSystemVuid
	 * @param sourceCodeSystemVersionName
	 * @param targetCodeSystemVuid
	 * @param targetCodeSystemVersionName
	 * @param mapSetStatus
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListMapSets", action = "getMapSets")
	public MapSetDetailListTransfer getMapSets(@WebParam(name = "MapSetName") String mapSetName,
			@WebParam(name = "SourceCodeSystemVUID") Long sourceCodeSystemVuid,
			@WebParam(name = "SourceCodeSystemVersionName") String sourceCodeSystemVersionName,
			@WebParam(name = "TargetCodeSystemVUID") Long targetCodeSystemVuid,
			@WebParam(name = "TargetCodeSystemVersionName") String targetCodeSystemVersionName,
			@WebParam(name = "MapSetStatus") Boolean mapSetStatus, @WebParam(name = "PageSize") Integer pageSize,
			@WebParam(name = "PageNumber") Integer pageNumber) throws SOAPException {
		
		MapSetDetailListTransfer mapSetDetailTransfer = null;
		return mapSetDetailTransfer;
	}

	/**
	 * This operation is stubbed for backwards compatibility.  DO NOT USE.
	 * @param mapSetVuid
	 * @param mapSetVersionName
	 * @param sourceConceptCodes
	 * @param targetConceptCodes
	 * @param sourceConceptPreferredDesignationName
	 * @param targetConceptPreferredDesignationName
	 * @param mapEntryOrder
	 * @param mapEntryStatus
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws SOAPException
	 */
	@WebMethod(operationName = "ListMapEntries", action = "ListMapEntries")
	public MapEntryDetailListTransfer getMapEntries(@WebParam(name = "MapSetVUID") Long mapSetVuid,
			@WebParam(name = "MapSetVersionName") String mapSetVersionName,
			@WebParam(name = "SourceConceptCodes") ConceptCodesTransfer sourceConceptCodes,
			@WebParam(name = "TargetConceptCodes") ConceptCodesTransfer targetConceptCodes,
			@WebParam(name = "SourceConceptPreferredDesignationName") String sourceConceptPreferredDesignationName,
			@WebParam(name = "TargetConceptPreferredDesignationName") String targetConceptPreferredDesignationName,
			@WebParam(name = "MapEntryOrder") Integer mapEntryOrder,
			@WebParam(name = "MapEntryStatus") Boolean mapEntryStatus, @WebParam(name = "PageSize") Integer pageSize,
			@WebParam(name = "PageNumber") Integer pageNumber) throws SOAPException {
		
		MapEntryDetailListTransfer mapEntryDetailListTransfer = null;
		return mapEntryDetailListTransfer;
	}
}
