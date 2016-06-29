package gov.vha.vets.term.webservice;
import java.util.Collection;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;

import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.webservice.transfer.ConceptDetailTransfer;
import gov.vha.vets.term.webservice.transfer.MapEntryValueListTransfer;
import gov.vha.vets.term.webservice.transfer.SourcesTransfer;

@WebService(name="sts", serviceName="ctService", targetNamespace="urn:gov:va:med:sts:webservice:ct")
public class CommonTerminologyWS
{
    @WebMethod(operationName="ReturnConceptDetails", action="getConceptDetail")
	public ConceptDetailTransfer getConceptDetail(@WebParam(name = "CodeSystemVUID")Long codeSystemVuid,
	        @WebParam(name = "VersionName")String versionName, @WebParam(name = "Code")String code) throws SOAPException
	{
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
    @WebMethod(operationName="ListMapEntriesFromSource", action="ListMapEntriesFromSource")
    public MapEntryValueListTransfer getMapEntriesFromSource(@WebParam(name="MapSetVUID") Long mapSetVuid,
    		@WebParam(name="MapSetVersionName") String mapSetVersionName,
            @WebParam(name="Sources") SourcesTransfer sources,
            @WebParam(name="SourceDesignationTypeName") String sourceDesignationTypeName,
            @WebParam(name="TargetDesignationTypeName") String targetDesignationTypeName,
            @WebParam(name="PageSize")Integer pageSize, @WebParam(name="PageNumber")Integer pageNumber) throws SOAPException
    {
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
