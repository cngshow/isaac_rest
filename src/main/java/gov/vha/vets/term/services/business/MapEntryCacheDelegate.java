package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.business.api.TerminologyDelegate;
import gov.vha.vets.term.services.dao.MapEntryCacheDao;
import gov.vha.vets.term.services.dto.MapEntryCacheDTO;
import gov.vha.vets.term.services.dto.MapEntryCacheListDTO;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MapEntryCacheDelegate
{
    public static MapEntryCacheListDTO getEntries(int callType, long entityId, Long versionId,
    		Long sourceDesignationTypeId, Long targetDesignationTypeId,
    		Collection<String> sourceValues, String sourceValueType,
    		Collection<String> targetValues, String targetValueType,
    		String sourceDescriptionFilter, String targetDescriptionFilter,
    		Integer mapEntryOrder, Boolean mapEntryStatus, Integer pageSize, Integer pageNumber)
    {
        Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
        
        MapEntryCacheListDTO mapEntryCacheListDTO =
        	MapEntryCacheDao.getEntries(entityId, realVersionId, sourceDesignationTypeId, targetDesignationTypeId,
        		mapEntryOrder, mapEntryStatus, sourceValues, sourceValueType, targetValues, targetValueType,
        		sourceDescriptionFilter, targetDescriptionFilter, pageSize, pageNumber);
        
        // filter the source concepts / designations
        HashMap<String, MapEntryCacheDTO> mapEntryCacheMap = new HashMap<String, MapEntryCacheDTO>();
        for (MapEntryCacheDTO mapEntryCacheDTO : mapEntryCacheListDTO.getMapEntryCaches())
        {
        	String mapKey;
        	if (sourceValueType.equals(TerminologyDelegate.CONCEPT_CODE_TYPE) || sourceValueType.equals(TerminologyDelegate.DESIGNATION_NAME_TYPE))
        	{
        		mapKey = mapEntryCacheDTO.getMapEntryEntityId() + "";
        	}
        	else
        	{
        		mapKey = mapEntryCacheDTO.getMapEntryEntityId() + "-" + mapEntryCacheDTO.getSourceDesignationCode();
        	}
        	MapEntryCacheDTO mapEntryCacheInMap = mapEntryCacheMap.get(mapKey);
        	if (mapEntryCacheInMap == null)
        	{
        		mapEntryCacheMap.put(mapKey, mapEntryCacheDTO);
        		continue;
        	}
        	
            if (mapEntryCacheDTO.getSourceDesignationTypeId() == mapEntryCacheDTO.getSourcePrefDesTypeId() && mapEntryCacheDTO.isSourceDesignationActive() == true)
            {
                // we have the preferred designation type and it is active - 1st choice
                updateSourceDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
            }
            else if (mapEntryCacheDTO.getSourceDesignationTypeId() == mapEntryCacheDTO.getSourcePrefDesTypeId() &&
            		mapEntryCacheInMap.getSourceDesignationTypeId() != mapEntryCacheDTO.getSourcePrefDesTypeId())
            {
                // we have the preferred designation type and it is inactive - 2nd choice
                updateSourceDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
            }
            else if (mapEntryCacheDTO.isSourceDesignationActive() == true && mapEntryCacheInMap.getSourceDesignationTypeId() != mapEntryCacheInMap.getSourcePrefDesTypeId())
            {
                // we have an active designation (not preferred type) - 3rd choice
                updateSourceDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
            }
        }
        List<MapEntryCacheDTO> mapEntryCacheList = new ArrayList<MapEntryCacheDTO>(mapEntryCacheMap.values());
        mapEntryCacheListDTO.setMapEntryCaches(mapEntryCacheList);
        
        if (callType == TerminologyDelegate.MAP_ENTRIES_CALL)
        {
            // filter the target concepts / designations
            mapEntryCacheMap = new HashMap<String, MapEntryCacheDTO>();
            for (MapEntryCacheDTO mapEntryCacheDTO : mapEntryCacheListDTO.getMapEntryCaches())
            {
            	String mapKey;
            	if (targetValueType.startsWith("Concept") == true)
            	{
            		mapKey = mapEntryCacheDTO.getMapEntryEntityId() + "";
            	}
            	else
            	{
            		mapKey = mapEntryCacheDTO.getMapEntryEntityId() + mapEntryCacheDTO.getTargetDesignationCode();
            	}
            	MapEntryCacheDTO mapEntryCacheInMap = mapEntryCacheMap.get(mapKey);
            	if (mapEntryCacheInMap == null)
            	{
            		mapEntryCacheMap.put(mapKey, mapEntryCacheDTO);
            		continue;
            	}
            	
                if (mapEntryCacheDTO.getTargetDesignationTypeId() == mapEntryCacheDTO.getTargetPrefDesTypeId() && mapEntryCacheDTO.isTargetDesignationActive() == true)
                {
                    // we have the preferred designation type and it is active - 1st choice
                    updateTargetDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
                }
                else if (mapEntryCacheDTO.getTargetDesignationTypeId() == mapEntryCacheDTO.getTargetPrefDesTypeId() &&
                		mapEntryCacheInMap.getTargetDesignationTypeId() != mapEntryCacheDTO.getTargetPrefDesTypeId())
                {
                    // we have the preferred designation type and it is inactive - 2nd choice
                    updateTargetDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
                }
                else if (mapEntryCacheDTO.isTargetDesignationActive() == true && mapEntryCacheInMap.getTargetDesignationTypeId() != mapEntryCacheInMap.getTargetPrefDesTypeId())
                {
                    // we have an active designation (not preferred type) - 3rd choice
                    updateTargetDesignationValues(mapEntryCacheDTO, mapEntryCacheInMap);
                }
            }
            mapEntryCacheList = new ArrayList<MapEntryCacheDTO>(mapEntryCacheMap.values());
        }

        mapEntryCacheList = mapEntryCacheListDTO.getMapEntryCaches();
        mapEntryCacheListDTO.setMapEntryCaches(new ArrayList<MapEntryCacheDTO>());
        int fromIndex = (pageNumber-1)*pageSize;
        int toIndex = ((pageNumber-1)*pageSize)+pageSize;
        int totalSize = mapEntryCacheList.size();
        toIndex = (toIndex >= totalSize) ? totalSize : toIndex;
        if (fromIndex < totalSize)
        {
            mapEntryCacheListDTO.setMapEntryCaches(mapEntryCacheList.subList(fromIndex, toIndex));
        }
        Long recordCount = (pageNumber == 1) ? (long)totalSize : null;
        mapEntryCacheListDTO.setTotalNumberOfRecords(recordCount);
        
        return mapEntryCacheListDTO;
    }
    
    
    private static void updateSourceDesignationValues(MapEntryCacheDTO fromMapEntryCache, MapEntryCacheDTO toMapEntryCache)
    {
    	toMapEntryCache.setSourceDesignationCode(fromMapEntryCache.getSourceDesignationCode());
    	toMapEntryCache.setSourceDesignationName(fromMapEntryCache.getSourceDesignationName());
    	toMapEntryCache.setSourceDesignationTypeId(fromMapEntryCache.getSourceDesignationTypeId());
    	toMapEntryCache.setSourceDesignationActive(fromMapEntryCache.isSourceDesignationActive());
    }

    
    private static void updateTargetDesignationValues(MapEntryCacheDTO fromMapEntryCache, MapEntryCacheDTO toMapEntryCache)
    {
    	toMapEntryCache.setTargetDesignationCode(fromMapEntryCache.getTargetDesignationCode());
    	toMapEntryCache.setTargetDesignationName(fromMapEntryCache.getTargetDesignationName());
    	toMapEntryCache.setTargetDesignationTypeId(fromMapEntryCache.getTargetDesignationTypeId());
    	toMapEntryCache.setTargetDesignationActive(fromMapEntryCache.isTargetDesignationActive());
    }

    
    public static void updateMapEntryCache(long mapSetEntityId, long versionId)
	{
        MapEntryCacheDao.updateMapEntryCache(mapSetEntityId, versionId);
	}
}
