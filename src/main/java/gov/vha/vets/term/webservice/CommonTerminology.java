package gov.vha.vets.term.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.vha.vets.term.services.business.TerminologyConfigDelegate;
import gov.vha.vets.term.services.business.api.TerminologyDelegate;
import gov.vha.vets.term.services.dto.MapEntryCacheDTO;
import gov.vha.vets.term.services.dto.MapEntryCacheListDTO;
import gov.vha.vets.term.services.dto.api.ConceptViewDTO;
import gov.vha.vets.term.services.dto.api.DesignationViewDTO;
import gov.vha.vets.term.services.dto.api.RelationshipViewDTO;
import gov.vha.vets.term.services.dto.config.MapSetConfig;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.webservice.transfer.ConceptDetailTransfer;
import gov.vha.vets.term.webservice.transfer.DesignationDetailTransfer;
import gov.vha.vets.term.webservice.transfer.MapEntryValueListTransfer;
import gov.vha.vets.term.webservice.transfer.MapEntryValueTransfer;
import gov.vha.vets.term.webservice.transfer.PropertyTransfer;
import gov.vha.vets.term.webservice.transfer.RelationshipTransfer;
import gov.vha.vets.term.webservice.transfer.ValueSetTransfer;

public class CommonTerminology
{
	private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final int MAX_PAGE_SIZE = 5000;
	
    static public ConceptDetailTransfer getConceptDetail(Long codeSystemVuid, String versionName, String code) throws STSException
	{
		prohibitAuthoringVersion(versionName);

		ConceptViewDTO concept = null;
		ConceptDetailTransfer conceptDetailTransfer = null;
		
		prohibitNullValue(codeSystemVuid, "Code System VUID");
		prohibitNullValue(versionName, "Version name");
		prohibitNullValue(code, "Concept code");

		try
		{
			concept = TerminologyDelegate.getConceptDetail(codeSystemVuid, versionName, code);

			if (concept != null)
			{
				String conceptStatus = (concept.getConceptStatus()) ? "Active" : "Inactive";
				conceptDetailTransfer = new ConceptDetailTransfer(concept.getConceptCode(), conceptStatus);
				
				// convert property objects to propertyTransfer objects
				List<PropertyTransfer> propertyTransfers = PropertyTransfer.convertFromPropertyList(concept.getProperties());
				conceptDetailTransfer.setProperties(propertyTransfers);

				// create and populate list of DesignationDetailTransfer objects
				List<DesignationDetailTransfer> designationDetailTransfers = new ArrayList<DesignationDetailTransfer>();
				List<DesignationViewDTO> designations = concept.getDesignations();
				if (designations != null)
				{
					for (DesignationViewDTO designationView : designations)
					{
						String designationStatus = (designationView.getDesignation().getActive()) ? "Active" : "Inactive";
						DesignationDetailTransfer designationDetailTransfer = new DesignationDetailTransfer(designationView.getDesignation().getName(),
								designationView.getDesignation().getCode(), designationView.getDesignation().getType().getName(),
								designationStatus, PropertyTransfer.convertFromPropertyList(designationView.getProperties()), 
								ValueSetTransfer.convertFromSubsetList(designationView.getSubsets()));
						designationDetailTransfers.add(designationDetailTransfer);
					}
					conceptDetailTransfer.setDesignations(designationDetailTransfers);
				}

				// create and populate list of RelationshipTransfer objects 
				List<RelationshipTransfer> relationshipTransfers = new ArrayList<RelationshipTransfer>();
				List<RelationshipViewDTO> relationships = concept.getRelationships();
				if (relationships != null)
				{
					for (RelationshipViewDTO relationshipViewDTO : relationships)
					{
						RelationshipTransfer relationshipTransfer = new RelationshipTransfer();
						relationshipTransfer.setName(relationshipViewDTO.getName());
						relationshipTransfer.setType(relationshipViewDTO.getType());
                    	relationshipTransfer.setCode(relationshipViewDTO.getCode());
                    	relationshipTransfer.setStatus(relationshipViewDTO.getActive()?"Active":"Inactive");

						relationshipTransfers.add(relationshipTransfer);
					}
					conceptDetailTransfer.setRelationships(relationshipTransfers);
				}
			}
		}
		catch (STSException e)
		{
			throw e;
		}

		return conceptDetailTransfer;
	}

	public static MapEntryValueListTransfer getMapEntriesFromSources(Long mapSetVuid, String mapSetVersionName,
    		Collection<String> sourceValues, String sourceDesignationTypeName, String targetDesignationTypeName, 
    		Integer pageSize, Integer pageNumber) throws STSException
    {
		prohibitAuthoringVersion(mapSetVersionName);
		prohibitNullValue(mapSetVuid, "MapSet VUID");
		prohibitNullValue(mapSetVersionName, "MapSet version name");
		prohibitNullValue(sourceValues, "Source values");

		pageSize = validatePageSize(pageSize);
        pageNumber = validatePageNumber(pageNumber);
        
        MapEntryValueListTransfer mapEntryValueListTransfer = new MapEntryValueListTransfer();
    	List<Long> mapSetsNotAcccessibleVuidList = TerminologyConfigDelegate.getMapSetsNotAccessibleVuidList();
    	if(mapSetsNotAcccessibleVuidList.contains(mapSetVuid)){
    		return mapEntryValueListTransfer;
    	}

    	MapSetConfig mapSetConfig = TerminologyDelegate.getMapSetConfig(mapSetVuid);
        if (mapSetConfig.isFound() == false)
        {
        	System.out.println("WARNING: MapSet configuration for VUID: " + mapSetVuid + " not found - using defaults.");
        }
        MapEntryCacheListDTO mapEntryCacheList = null; 
        
        mapEntryCacheList = TerminologyDelegate.getMapEntries(
        		TerminologyDelegate.MAP_ENTRIES_FROM_SOURCE_CALL, mapSetVuid, mapSetVersionName,
        		sourceDesignationTypeName, targetDesignationTypeName,
        		sourceValues, mapSetConfig.getSourceType(), null, mapSetConfig.getTargetType(),
        		null, null, null, null, pageSize, pageNumber);
        
        List<MapEntryValueTransfer> mapEntryValueTransferList = new ArrayList<MapEntryValueTransfer>();
        for (MapEntryCacheDTO mapEntryCacheDTO : mapEntryCacheList.getMapEntryCaches())
        {
        	MapEntryValueTransfer mapEntryValueTransfer = new MapEntryValueTransfer();
        	mapEntryValueTransfer.setVuid(mapEntryCacheDTO.getMapEntryVuid());
            if (mapSetConfig.getSourceType().equals(TerminologyDelegate.CONCEPT_CODE_TYPE))
            {
            	mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceConceptCode());
            }
            else if (mapSetConfig.getSourceType().equals(TerminologyDelegate.DESIGNATION_CODE_TYPE))
            {
            	mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceDesignationCode());
            }
            else if  (mapSetConfig.getSourceType().equals(TerminologyDelegate.DESIGNATION_NAME_TYPE))
            {
            	mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceDesignationName());
            }
        	DesignationType sourceDesType = TerminologyDelegate.getCachedDesignationType(mapEntryCacheDTO.getSourceDesignationTypeId());
            mapEntryValueTransfer.setSourceDesignationTypeName(sourceDesType.getName());
            
            if (mapSetConfig.getTargetType().equals(TerminologyDelegate.CONCEPT_CODE_TYPE))
            {
            	mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetConceptCode());
            }
            else if (mapSetConfig.getTargetType().equals(TerminologyDelegate.DESIGNATION_CODE_TYPE))
            {
            	mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetDesignationCode());
            }
            else if  (mapSetConfig.getTargetType().equals(TerminologyDelegate.DESIGNATION_NAME_TYPE))
            {
            	mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetDesignationName());
            }
        	DesignationType targetDesType = TerminologyDelegate.getCachedDesignationType(mapEntryCacheDTO.getTargetDesignationTypeId());
            mapEntryValueTransfer.setTargetDesignationTypeName(targetDesType.getName());
            mapEntryValueTransfer.setTargetDesignationName(mapEntryCacheDTO.getTargetDesignationName());
            Version targetVersion = TerminologyDelegate.getCachedVersion(mapEntryCacheDTO.getTargetVersionId());
            mapEntryValueTransfer.setTargetCodeSystemVuid(targetVersion.getCodeSystem().getVuid());
            mapEntryValueTransfer.setTargetCodeSystemVersionName(targetVersion.getName());
            mapEntryValueTransfer.setOrder(mapEntryCacheDTO.getMapEntrySequence());
            mapEntryValueTransfer.setStatus(mapEntryCacheDTO.isMapEntryActive());
            mapEntryValueTransferList.add(mapEntryValueTransfer);
        }
        
        mapEntryValueListTransfer.setTotalNumberOfRecords(mapEntryCacheList.getTotalNumberOfRecords());
        mapEntryValueListTransfer.setMapEntryValueTransfers(mapEntryValueTransferList);

        return mapEntryValueListTransfer;
    }
	    private static Integer validatePageSize(Integer pageSize) throws STSException
	    {
	        if (pageSize == null)
	        {
	            pageSize = DEFAULT_PAGE_SIZE;
	        }
	        else if (pageSize > MAX_PAGE_SIZE)
	        {
	            throw new STSException("Page size exceeded maximum size of: "+ MAX_PAGE_SIZE);
	        }
	        else if (pageSize < 1)
	        {
	        	throw new STSException("Invalid page size ("+pageSize+").");
	        }
	        
	       return pageSize;
	    }
	    
	    private static Integer validatePageNumber(Integer pageNumber) throws STSException
	    {
	        if (pageNumber == null)
	        {
	            pageNumber = 1;
	        }
	        else if (pageNumber < 1)
	        {
	        	throw new STSException("Invalid page number ("+pageNumber+").");
	        }
	        
	        return pageNumber;
	    }

	private static void prohibitAuthoringVersion(String versionName) throws STSException
	{
		if (HibernateSessionFactory.AUTHORING_VERSION_NAME.equals(versionName))
		{
			throw new STSException(HibernateSessionFactory.AUTHORING_VERSION_NAME + " is not an allowed version name.");
		}
	}
	
	private static void prohibitNullValue(Object value, String valueName) throws STSException
	{
		if (value == null)
		{
			String singularOrPlural = (valueName.endsWith("s") == true) ? "are" : "is";
			throw new STSException(valueName+" " + singularOrPlural + " required.");
		}
	}

}

