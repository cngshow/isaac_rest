package gov.vha.vets.term.webservice;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.webservice.transfer.ConceptDetailTransfer;
import gov.vha.vets.term.webservice.transfer.MapEntryValueListTransfer;

@Path(RestPaths.ct)
public class CommonTerminologyRS
{
    @GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/ReturnConceptDetails/{CodeSystemVUID}/{VersionName}/{Code}")
	public ConceptDetailTransfer getConceptDetail(@PathParam("CodeSystemVUID")Long codeSystemVuid,
	        @PathParam("VersionName")String versionName, @PathParam("Code")String code)
	{
        ConceptDetailTransfer conceptDetailTransfer;
        
        try
        {
            conceptDetailTransfer = CommonTerminology.getConceptDetail(codeSystemVuid, versionName, code);
        }
        catch (STSException e)
        {
        	Response response = Response.serverError().build();
            throw new WebApplicationException(e, response);
        }
        
		return conceptDetailTransfer;
	}

    @GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Path("/ListMapEntriesFromSource/{MapSetVUID}/{MapSetVersionName}/{Sources}/")
    public MapEntryValueListTransfer getMapEntriesFromSource(@PathParam("MapSetVUID") Long mapSetVuid, @PathParam("MapSetVersionName") String mapSetVersionName,
            @PathParam("Sources") String sources, @QueryParam("SourceDesignationTypeName")String sourceDesignationTypeName, 
            @QueryParam("TargetDesignationTypeName")String targetDesignationTypeName, @QueryParam("PageSize")Integer pageSize,
            @QueryParam("PageNumber")Integer pageNumber)
    {
        MapEntryValueListTransfer mapEntrySourceTransfer = new MapEntryValueListTransfer();
        try
        {
            List<String> sourceList = null;
            if (sources != null)
            {
                String[] sourceStrings = sources.split("\\|");
                sourceList = (sourceStrings.length > 0) ? Arrays.asList(sourceStrings) : null;
            }
            mapEntrySourceTransfer = CommonTerminology.getMapEntriesFromSources(mapSetVuid, mapSetVersionName,
            		sourceList, sourceDesignationTypeName, targetDesignationTypeName, pageSize, pageNumber);
        }
        catch (Exception ex)
        {
        	Response response = Response.serverError().build();
            throw new WebApplicationException(ex, response);
        }
        
        return mapEntrySourceTransfer;
    }
	
	
}
