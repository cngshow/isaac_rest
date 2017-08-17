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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebService(name="sts", serviceName="ctService", targetNamespace="urn:gov:va:med:sts:webservice:ct")
public class CommonTerminologyWS
{
	private static Logger log = LogManager.getLogger(CommonTerminologyWS.class);
	
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
