package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.MapEntryDao;
import gov.vha.vets.term.services.dao.MapSetDao;
import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.dto.MapEntryDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.MapSetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapEntryDelegate
{
    
    private static final String MAP_TO = " to ";
    
    public static MapEntry createVHAT(Version version, long mapSetEntityId,String code, long vuid, boolean active, 
            String sourceConceptCode, String targetConceptCode, int sequence, Long grouping, Date effectiveDate) throws STSException
    {
        MapEntry mapEntry = new MapEntry(sourceConceptCode+MAP_TO+targetConceptCode, 
                code, vuid, version.getCodeSystem(), 
                version, true, sourceConceptCode, targetConceptCode, effectiveDate);
        MapEntryDao.save(mapEntry);
        MapSetRelationshipDelegate.create(version, mapSetEntityId, mapEntry.getEntityId(), active, sequence, grouping);
        
        return mapEntry;
    }
	public static MapEntry createVHATWithValidation(Version version, long mapSetEntityId,String code, long vuid, boolean active, 
    		String sourceCode, String targetCode, int sequence, Long grouping) throws STSException
    {
	    MapEntry mapEntry = MapEntryDao.get(sourceCode, targetCode);
	    if (mapEntry == null)
	    {
        	mapEntry = new MapEntry(sourceCode+MAP_TO+targetCode, 
        			code, vuid, version.getCodeSystem(), 
        			version, true, sourceCode, targetCode, null);
            MapEntryDao.save(mapEntry);
            MapSetRelationshipDelegate.create(version, mapSetEntityId, mapEntry.getEntityId(), active, sequence, grouping);
	    }
	    else
	    {
	        // if we already have a mapEntry that ok becuase we can reuse mapentries but not if they already are used within the mapSet
	        MapSetRelationship mapSetRelationship = MapSetRelationshipDelegate.get(mapSetEntityId, mapEntry.getEntityId());
	        if (mapSetRelationship == null)
	        {
	            MapSetRelationshipDelegate.create(version, mapSetEntityId, mapEntry.getEntityId(), active, sequence, grouping);
	        }
	        else
	        {
	            throw new STSException("Map Entry source:"+sourceCode+" target:"+targetCode+" already exists in the map set.");
	        }
	    }
        return mapEntry;
    }

    public static List<MapEntry> get(Collection<String> sourceCodes, Collection<String> targetCodes)
    {
        return MapEntryDao.get(sourceCodes, targetCodes);
    }

    public static List<MapEntry> get(Collection<Long> vuids)
    {
        return MapEntryDao.get(vuids);
    }

	public static List<MapEntryDTO> getChanges(Long mapSetEntityId)
	{
		return MapEntryDao.getChanges(mapSetEntityId);
	}
	
    public static List<MapEntryDTO> getEntries(long mapSetEntityId, Long versionId, boolean includeInactives)
    {
    	return MapEntryDao.getEntries(mapSetEntityId, versionId, includeInactives);
    }
    
    public static List<MapEntryDTO> getEntriesOrderedByVuid(long mapSetEntityId, Long versionId)
    {
    	return MapEntryDao.getEntriesOrderedByVuid(mapSetEntityId, versionId, false);
    }

	public static void setAuthoringToVersion(List<Long> mapSetEntityIds, Version version)
	{
		MapEntryDao.setAuthoringToVersion(mapSetEntityIds, version);
	}

	public static void updateVHAT(long mapSetEntityId, MapEntry mapEntry, String targetConceptCode, boolean active, int sequence, Long grouping, Date effectiveDate)
	{
		boolean effectiveDateChanged = false;
		if ((mapEntry.getEffectiveDate() != null && effectiveDate == null) || (mapEntry.getEffectiveDate() == null && effectiveDate != null) ||
				(mapEntry.getEffectiveDate() != null && effectiveDate != null && mapEntry.getEffectiveDate().getTime() != effectiveDate.getTime()))
		{
			effectiveDateChanged = true;
		}
		if (!mapEntry.getTargetCode().equals(targetConceptCode) || effectiveDateChanged)
		{	
			MapEntry mapEntryUpdate = new MapEntry(mapEntry.getSourceCode()+MAP_TO+targetConceptCode, mapEntry.getCode(), mapEntry.getVuid(), mapEntry.getCodeSystem(),
					VersionDelegate.getAuthoring(), true, mapEntry.getSourceCode(), targetConceptCode, effectiveDate);
			mapEntryUpdate.setEntityId(mapEntry.getEntityId());
			MapEntryDao.save(mapEntryUpdate);
		}
		
		// update the relationship information
		MapSetRelationship mapSetRelationship = MapSetRelationshipDelegate.get(mapSetEntityId, mapEntry.getEntityId());
    	if (mapSetRelationship.getSequence() != sequence || mapSetRelationship.getGrouping() != grouping || mapSetRelationship.getActive() != active)
    	{
    		MapSetRelationshipDelegate.update(mapSetRelationship, active, sequence, grouping);
    	}
	}

	public static void updateVHAT(long mapSetEntityId, MapEntry mapEntry, String targetConceptCode, boolean active, int sequence, Long grouping)
	{
		updateVHAT(mapSetEntityId, mapEntry, targetConceptCode, active, sequence, grouping, mapEntry.getEffectiveDate());
	}
	
	public static List<MapEntryDetailDTO> getEntries(long entityId, String sourceCodeSystemName, String targetCodeSystemName, String sourceVersionName, String targetVersionName, Long versionId)
	{
		Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
        List<MapEntryDTO> mapEntries = MapEntryDao.getEntries(entityId, realVersionId, true);
        
        Version sourceVersion = VersionDelegate.get(sourceCodeSystemName, sourceVersionName);        
        Version targetVersion = VersionDelegate.get(targetCodeSystemName, targetVersionName);
        return mapEntries.size()==0 ? new ArrayList<MapEntryDetailDTO>() : processMapEntries(sourceVersion, targetVersion, mapEntries);
	}

	public static List<MapEntryDetailDTO> getEntries(MapSetDTO mapSetDTO, Long versionId)
	{
		Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
	    List<MapEntryDetailDTO> results = new ArrayList<MapEntryDetailDTO>();
	    List<MapEntryDTO> mapEntries = MapEntryDao.getEntries(mapSetDTO.getEntityId(), realVersionId, true);
	    results.addAll(processMapEntries(mapSetDTO.getSourceVersion(), mapSetDTO.getTargetVersion(), mapEntries));
	    
		return results;
	}
	
	public static List<MapEntryDetailDTO> getEntries(MapSet mapSet, String sourceCode)
	{
        List<MapEntryDTO> mapEntries = MapEntryDao.getEntriesBySourceCode(mapSet.getEntityId(), sourceCode);
        
        Version sourceVersion = VersionDelegate.getByVersionId(mapSet.getSourceVersionId());
        Version targetVersion = VersionDelegate.getByVersionId(mapSet.getTargetVersionId());

        return processMapEntries(sourceVersion, targetVersion, mapEntries);
	}

	public static List<MapEntryDetailDTO> getEntries(MapSet mapSet, long mapEntryEntityId)
	{
        List<MapEntryDTO> mapEntries = MapEntryDao.getEntriesByEntityId(mapSet.getEntityId(), mapEntryEntityId);
        
        Version sourceVersion = VersionDelegate.getByVersionId(mapSet.getSourceVersionId());
        Version targetVersion = VersionDelegate.getByVersionId(mapSet.getTargetVersionId());

        return processMapEntries(sourceVersion, targetVersion, mapEntries);
	}

	private static List<MapEntryDetailDTO> processMapEntries(Version sourceVersion, Version targetVersion, List<MapEntryDTO> mapEntries)
    {
        List<MapEntryDetailDTO> results = new ArrayList<MapEntryDetailDTO>();
        HashSet<String> sourceCodeHashSet = new HashSet<String>();
        HashSet<String> targetCodeHashSet = new HashSet<String>();
        
        for (MapEntryDTO mapEntryDTO : mapEntries)
        {
            sourceCodeHashSet.add(mapEntryDTO.getMapEntry().getSourceCode());
            targetCodeHashSet.add(mapEntryDTO.getMapEntry().getTargetCode());
            
            MapEntryDetailDTO detail = new MapEntryDetailDTO();
            detail.setMapEntryDTO(mapEntryDTO);
            results.add(detail);
        }
        Map<String, Designation> sourceCodeDescriptionMap = null;
        if (sourceCodeHashSet.size() > 0)
        {
            sourceCodeDescriptionMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(sourceVersion.getCodeSystem(), sourceVersion.getId(), sourceCodeHashSet);
        }   
        Map<String, Designation> targetCodeDescriptionMap = null;
        if (targetCodeHashSet.size() > 0)
        {
            targetCodeDescriptionMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(targetVersion.getCodeSystem(), targetVersion.getId(), targetCodeHashSet);
        }
        
        for (MapEntryDetailDTO mapEntryDetailDTO : results)
        {
            Designation sourceDesignation = sourceCodeDescriptionMap.get(mapEntryDetailDTO.getMapEntryDTO().getMapEntry().getSourceCode());
            Designation targetDesignation = targetCodeDescriptionMap.get(mapEntryDetailDTO.getMapEntryDTO().getMapEntry().getTargetCode());
            mapEntryDetailDTO.setSourceDesignation(sourceDesignation);
            mapEntryDetailDTO.setTargetDesignation(targetDesignation);
        }
        return results;
    }

	public static void verifySequence(long mapSetEntityId) throws STSException
    {
        String savedSourceConceptCode = "";
        int sequence = 0;

        MapSet mapSet = MapSetDao.get(mapSetEntityId);
        List<MapEntryDTO> mapEntries = MapEntryDao.getMapEntriesForSequenceVerification(mapSet.getEntityId());
        for (MapEntryDTO mapEntry : mapEntries)
        {
            if (savedSourceConceptCode.equals(mapEntry.getMapEntry().getSourceCode()) == false)
            {
                savedSourceConceptCode = mapEntry.getMapEntry().getSourceCode();
                sequence = 0;
            }

            if (mapEntry.getMapSetRelationship().getSequence() != 0)
            {
	            sequence++;
	            
	            if (mapEntry.getMapSetRelationship().getSequence() != sequence)
	            {
	                throw new STSException("Improper sequence of '" + mapEntry.getMapSetRelationship().getSequence() + "'"
	                        + " for map set entry with a map set name: " + mapSet.getName()
	                        + ", source concept code: " + mapEntry.getMapEntry().getSourceCode()
	                        + ", and a target concept code: " + mapEntry.getMapEntry().getTargetCode());
	            }
            }
        }
    }

	public static int getEntryCount(long mapSetEntityId, long versionId)
	{
		return MapEntryDao.getEntryCount(mapSetEntityId, versionId);
	}

	public static String getDiscoveryEntriesQuery()
	{
		return MapEntryDao.getDiscoveryEntriesQuery();
	}

    public static MapEntryDTO getMapEntry(Long mapEntryEntityId, Long mapSetRelationshipEntityId)
    {
        return MapEntryDao.get(mapEntryEntityId, mapSetRelationshipEntityId);
    }
    public static void inactivate(Long mapSetRelationshipEntityId) throws STSException
    {
        MapSetRelationship mapSetRelationship = MapSetRelationshipDelegate.get(mapSetRelationshipEntityId);
        if (mapSetRelationship != null)
        {
            MapSetRelationshipDelegate.update(mapSetRelationship, false, mapSetRelationship.getSequence(), mapSetRelationship.getGrouping());
        }
        else
        {
            throw new STSException("Cannot find MapSetRelationship for entityId:"+mapSetRelationshipEntityId);
        }
    }
    
    public static boolean isConceptCodeInMapEntry(CodeSystem codeSystem, String conceptCode)
    {
        boolean isFound = MapEntryDao.isConceptCodeInMapEntry(codeSystem, conceptCode, true);
        if (!isFound)
        {
            isFound = MapEntryDao.isConceptCodeInMapEntry(codeSystem, conceptCode, false);
        }
        
        return isFound;
    }
    /**
     * 
     * @param mapSetDTO
     * @param versionId
     * @return
     */
	public static List<MapEntryDetailDTO> getDeploymentMapEntries(MapSetDTO mapSetDTO, Long versionId)
	{
		Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
	    List<MapEntryDetailDTO> results = new ArrayList<MapEntryDetailDTO>();
	    List<MapEntryDTO> mapEntries = MapEntryDao.getDeploymentMapEntries(mapSetDTO.getEntityId(), realVersionId, true);
	    results.addAll(processMapEntries(mapSetDTO.getSourceVersion(), mapSetDTO.getTargetVersion(), mapEntries));
	    
		return results;
	}
}
