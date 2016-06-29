package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ServicesBrowserDao;
import gov.vha.vets.term.services.dto.GemDTO;
import gov.vha.vets.term.services.dto.GemTargetDTO;
import gov.vha.vets.term.services.dto.GemVersionDTO;
import gov.vha.vets.term.services.dto.MapEntryVersionDetailDTO;
import gov.vha.vets.term.services.exception.STSEntityNotInMapsetException;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.comparator.MapEntryVersionDetailSortbyEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GemDelegate
{

	public static GemDTO getGemMapSetHistory(MapSet mapSet, Designation mapSetDesignation, long mapEntryEntityId, boolean includeAuthoring) throws STSException
	{
	    HibernateSessionFactory.currentSession().clear();
	    //CCR1200, get the finalized version since it was not pulled in queries below
    	List<Version> versionss = ServicesBrowserDao.getMapEntryFinalizedVersions(mapSet, mapEntryEntityId, includeAuthoring);

	    // CodeCR 1258 - raw list with inactive entries needed in subsequent method processing
    	// for collecting historically active mappings
		List<MapEntryVersionDetailDTO> activeMapEntryVersionDetails = new ArrayList<MapEntryVersionDetailDTO>();
		List<MapEntryVersionDetailDTO> activeOrInactiveMapEntryVersionDetails = ServicesBrowserDao.getMapEntryVersions(mapSet, mapEntryEntityId, includeAuthoring, true);
        // COdeCR 1258 - Do not show the inactive mappings
		for (MapEntryVersionDetailDTO activeOrInactiveMapEntry : activeOrInactiveMapEntryVersionDetails) {
			if (activeOrInactiveMapEntry.isActive()) {
				activeMapEntryVersionDetails.add(activeOrInactiveMapEntry);
			}
		}
		if (activeMapEntryVersionDetails.size() < 1)
		{
			throw new STSEntityNotInMapsetException(" No map entries found for entityId: "+mapEntryEntityId+" in MapSet: "+mapSetDesignation.getName());
		}
		// make sure it is in entityId, versionId order
		Collections.sort(activeMapEntryVersionDetails, new MapEntryVersionDetailSortbyEntity());
		// create a map where the key is the entityId and the values are each item in there version
		Map<Long, List<MapEntryVersionDetailDTO>> map = new HashMap<Long, List<MapEntryVersionDetailDTO>>();
		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : activeMapEntryVersionDetails)
		{
			List<MapEntryVersionDetailDTO> entityVersions = map.get(mapEntryVersionDetailDTO.getEntityId());
			if (entityVersions == null)
			{
				entityVersions = new ArrayList<MapEntryVersionDetailDTO>();
				map.put(mapEntryVersionDetailDTO.getEntityId(), entityVersions);
			}
			entityVersions.add(mapEntryVersionDetailDTO);
		}
		
		//CodeCR 1258 - create a map by version for the raw list of map entries
		// make sure it is in entityId, versionId order
		Collections.sort(activeOrInactiveMapEntryVersionDetails, new MapEntryVersionDetailSortbyEntity());
		Map<Long, List<MapEntryVersionDetailDTO>> activeOrInactiveMap = new HashMap<Long, List<MapEntryVersionDetailDTO>>();
		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : activeOrInactiveMapEntryVersionDetails)
		{
			List<MapEntryVersionDetailDTO> entityVersions = activeOrInactiveMap.get(mapEntryVersionDetailDTO.getEntityId());
			if (entityVersions == null)
			{
				entityVersions = new ArrayList<MapEntryVersionDetailDTO>();
				activeOrInactiveMap.put(mapEntryVersionDetailDTO.getEntityId(), entityVersions);
			}
			entityVersions.add(mapEntryVersionDetailDTO);
		}
		
		List<MapSet> mapSetVersions = MapSetDelegate.getAllMapSetVersions(mapSet.getEntityId(), includeAuthoring);
		List<MapSet> mapSetVersionsFinaList = new ArrayList<MapSet>();
		List<Long> mapSetVersionsIds = new ArrayList<Long>();

		//CCR1200, get the Version ID's for all Map Set Versions so we can compare it with finalized verson ids
		for (MapSet mapSet2 : mapSetVersions){
			mapSetVersionsIds.add(mapSet2.getVersion().getId());
		}
		//CCR1200 create a final list of version based on all map set version and finalized map entry versions.
		for (Version versions : versionss)
		{
		    if(!mapSetVersionsIds.contains(new Long(versions.getId()))){
		    	MapSet tempMapSet = new MapSet(mapSetDesignation.getName(), "", 0L, null, versions, true,0,0);
			    mapSetVersionsFinaList.add(tempMapSet);			
			}
		}
		mapSetVersionsFinaList.addAll(mapSetVersions);
		
		
		// build the Main gem object
		GemDTO gem = new GemDTO();
	    gem.setMapSetName(mapSetDesignation.getName());
	    gem.setSourceCode(activeMapEntryVersionDetails.get(0).getSourceCode());
	    gem.setSourceCodeSystemName(VersionDelegate.getByVersionId(mapSet.getSourceVersionId()).getCodeSystem().getName());
	    gem.setTargetCodeSystemName(VersionDelegate.getByVersionId(mapSet.getTargetVersionId()).getCodeSystem().getName());
	    gem.setSourceDescription(activeMapEntryVersionDetails.get(0).getSourceDescription());

	    List<GemVersionDTO> versions = new ArrayList<GemVersionDTO>();
	    // now build each version
		for (MapSet mapSetVersion : mapSetVersionsFinaList)
		{
	    	GemVersionDTO gemVersion = new GemVersionDTO();
	    	gemVersion.setVersion(mapSetVersion.getVersion());
    		gemVersion.setEffectiveDate(mapSetVersion.getEffectiveDate());
	    	gemVersion.setTargets(getGemTargets(mapSetVersion.getVersion().getId(), map, activeOrInactiveMap));
	    	versions.add(gemVersion);
		}
	    

	    gem.setGemVersions(versions);
	    // CodeCR1258 - fill in no longer needed
	    //gem.setGemVersions(fillActiveEmptyMapEntiresforGemMapSet(versions));
	    
	    return gem;
	}
	/*
	private static List<GemVersionDTO> fillActiveEmptyMapEntiresforGemMapSet(List<GemVersionDTO> versions){
		List<Integer> mapEntryEnptyIndexes = new ArrayList<Integer>();
		int i = 0;
		for (GemVersionDTO gemVersion : versions){
			if ( null != gemVersion && null != gemVersion.getTargets() && gemVersion.getTargets().isEmpty())  {
				mapEntryEnptyIndexes.add(i);
			}
			i++;
		}
		Collections.reverse(mapEntryEnptyIndexes);
		for (Integer entryIndex : mapEntryEnptyIndexes ){
			GemVersionDTO version = versions.get(entryIndex);
			if (entryIndex < (versions.size()-1)) {
				if (versions.get(entryIndex+1) != null && versions.get(entryIndex+1).getTargets() != null) {
					version.setTargets(versions.get(entryIndex+1).getTargets());
				}
			} 
			if (version.getEffectiveDate() == null && version.getVersion().getEffectiveDate() != null) {
				version.setEffectiveDate(version.getVersion().getEffectiveDate());
			}
			versions.set(entryIndex, version);
			
		}
		return versions;
	}
	*/
	private static List<List<GemTargetDTO>> getGemTargets(long versionId, Map<Long, List<MapEntryVersionDetailDTO>> map, Map<Long, List<MapEntryVersionDetailDTO>> activeOrInactiveMap) throws STSException
	{
	    Set<Long> entityIds = new HashSet<Long>();
		
	    List<List<GemTargetDTO>> gemCombinations = new ArrayList<List<GemTargetDTO>>();
	    List<GemTargetDTO> targets = new ArrayList<GemTargetDTO>();
		List<MapEntryVersionDetailDTO> entries = getMapEntryByVersion(versionId, map, activeOrInactiveMap);
		if (entries != null && !entries.isEmpty()) {
    		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : entries)
    		{
    			entityIds.add(mapEntryVersionDetailDTO.getEntityId());
    		}
    		
    		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : entries)
    		{
        		Map<Long, String> propertyMap = getGemPropertyMap(entityIds, mapEntryVersionDetailDTO.getVersionId());

    			if (mapEntryVersionDetailDTO.isActive())
    			{
    				String gemFlag = propertyMap.get(mapEntryVersionDetailDTO.getEntityId());
    				if (gemFlag == null)
    				{
    					throw new STSException("Gem flag is null for entityId: "+mapEntryVersionDetailDTO.getEntityId());
    				}
    				GemTargetDTO gemTarget = new GemTargetDTO(mapEntryVersionDetailDTO.getTargetCode(), 
    						mapEntryVersionDetailDTO.getTargetDescription(), gemFlag, mapEntryVersionDetailDTO.getVersionId());
    				targets.add(gemTarget);
    			}
    		}
    		Collections.sort(targets);
    		gemCombinations = getGemCombinations(targets);
		}

		return gemCombinations;
	}
	private static List<List<GemTargetDTO>> getGemCombinations(List<GemTargetDTO> targets)
	{
		List<List<GemTargetDTO>> results = new ArrayList<List<GemTargetDTO>>();
		
		// generate a list for each scenario and put in map
		Map<Integer, List<GemTargetDTO>> scenarioMap = new HashMap<Integer, List<GemTargetDTO>>();
		int scenario = 0;
		
		for (GemTargetDTO target : targets)
		{
			if (target.isCombination())
			{
				scenario = target.getScenario();
				List<GemTargetDTO> values = scenarioMap.get(scenario);
				if (values == null)
				{
					values = new ArrayList<GemTargetDTO>();
					scenarioMap.put(scenario, values);
				}
				values.add(target);
			}
			else
			{
				List<GemTargetDTO> choice = new ArrayList<GemTargetDTO>();
				choice.add(target);
				results.add(choice);
			}
			
		}
		// now process each scenario
		for (int i = 1; i <= scenario; i++)
		{
			List<List<GemTargetDTO>> choiceLists = new ArrayList<List<GemTargetDTO>>();
			List<GemTargetDTO> list = scenarioMap.get(i);
			for (GemTargetDTO gemTarget : list)
			{
				List<GemTargetDTO> choiceList = null; 
				if (gemTarget.getChoiceList() > choiceLists.size())
				{
					choiceList = new ArrayList<GemTargetDTO>();
					choiceLists.add(choiceList);
				}
				else
				{
					choiceList = choiceLists.get(gemTarget.getChoiceList()-1);
				}
				choiceList.add(gemTarget);
			}
			results.addAll(combineAllLists(choiceLists));
		}
		return results;
	}
	private static List<List<GemTargetDTO>> combineAllLists(List<List<GemTargetDTO>> choiceLists)
	{
		List<List<GemTargetDTO>> results = new ArrayList<List<GemTargetDTO>>();
		int n = choiceLists.size();
		int[] indexes = new int[n];
		int totalAlternatives = 1;
	
		for (int i = 0; i < n; i++)
		{
			indexes[i] = 0;
			List<GemTargetDTO> theList = choiceLists.get(i);
			if (theList != null)
			{
				totalAlternatives *= theList.size();
			}
		}
	
		for (int t = 0; t < totalAlternatives; t++)
		{
			List<GemTargetDTO> choice = new ArrayList<GemTargetDTO>();
			for (int i = 0; i< indexes.length; i++)
			{
				List<GemTargetDTO> theList = choiceLists.get(i);
				if (theList != null)
				{
					GemTargetDTO item = theList.get(indexes[i]);
					choice.add(item);
				}
			}
			results.add(choice);
			// increment each index.  when the index of a lower list is to be incremented beyond the list size then 
			// reset the index to zero and try incrementing a higher list
			for (int j = indexes.length-1;j >= 0;j--)
			{
				List<GemTargetDTO> theList = choiceLists.get(j);
				if (theList != null)
				{
					if (++indexes[j] >= theList.size())
					{
						indexes[j] = 0;
					}
					else
					{
						break;
					}
				}
			}
		}
		return results;
	}
		
	/**
	 * Return a list of entries for a given version id
	 * @param versionId
	 * @param map
	 * @return
	 */
	public static List<MapEntryVersionDetailDTO> getMapEntryByVersion(long versionId, Map<Long, List<MapEntryVersionDetailDTO>> map, Map<Long, List<MapEntryVersionDetailDTO>> activeOrInactiveMap)
	{
		List<MapEntryVersionDetailDTO> entries = new ArrayList<MapEntryVersionDetailDTO>();
		Collection<List<MapEntryVersionDetailDTO>> values = map.values();
		//each list represents the map entry at each version level
		for (List<MapEntryVersionDetailDTO> list : values)
		{
			// we want the entry for the given version level
			for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : list)
			{
				if (mapEntryVersionDetailDTO.isActive() && mapEntryVersionDetailDTO.getVersionId() == versionId)
				{
					entries.add(mapEntryVersionDetailDTO);
					break;
				}
			}
		}
		List<MapEntryVersionDetailDTO>priorEntries = getMapEntriesForPriorVersions(versionId, activeOrInactiveMap);
		ploop: for (MapEntryVersionDetailDTO priorEntry : priorEntries) {
			for (MapEntryVersionDetailDTO mappedEntry : entries) {
				if (mappedEntry.getEntityId() == priorEntry.getEntityId()) {
					continue ploop;
				}
			}
			entries.add(priorEntry);
		}
		return entries;
	}
	//Code CR 1258 Get mappings for prior version to see if any are still active
	public static List<MapEntryVersionDetailDTO> getMapEntriesForPriorVersions(long versionId, Map<Long, List<MapEntryVersionDetailDTO>> activeOrInactiveMap)
	{
		List<MapEntryVersionDetailDTO> entries = new ArrayList<MapEntryVersionDetailDTO>();
		Collection<List<MapEntryVersionDetailDTO>> values = activeOrInactiveMap.values();
		HashMap<Long,MapEntryVersionDetailDTO> mostRecentMappingForEntityIdMap = new HashMap<Long,MapEntryVersionDetailDTO>();
		
		//each list represents the map entry at each version level
		for (List<MapEntryVersionDetailDTO> list : values)
		{
			// we want the entry for the given version level
			for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : list)
			{
				if (mapEntryVersionDetailDTO.getVersionId() <= versionId) // must use >= to pull in possible inactive this version
				{
					MapEntryVersionDetailDTO mostRecentMappingForEntityId = 
						mostRecentMappingForEntityIdMap.get(mapEntryVersionDetailDTO.getEntityId());

					if ((mostRecentMappingForEntityId == null) || 
						(mapEntryVersionDetailDTO.getVersionId() >
					     mostRecentMappingForEntityId.getVersionId()))	{
						
						mostRecentMappingForEntityIdMap.put(mapEntryVersionDetailDTO.getEntityId(), mapEntryVersionDetailDTO);
					}
				}
			}
		}

		// if the most recent update made the mapping inactive then remove it from the list
		Iterator<Entry<Long, MapEntryVersionDetailDTO>> mapIterator = mostRecentMappingForEntityIdMap.entrySet().iterator();
		while  (mapIterator.hasNext()) {
			Entry<Long,MapEntryVersionDetailDTO> hashMapEntry = mapIterator.next();
			MapEntryVersionDetailDTO mapSetEntry = hashMapEntry.getValue();
			if (mapSetEntry != null && !mapSetEntry.isActive()) {
				mapIterator.remove();
			}
		}				
		
		entries = new ArrayList<MapEntryVersionDetailDTO>(mostRecentMappingForEntityIdMap.values());
		
		return entries;
	}
	
    /**
     * Get each property value for each given concept entity id
     * @param conceptEntityIds
     * @param versionId
     * @param propertyTypeName
     * @return
     */
	public static Map<Long, String> getGemPropertyMap(Collection<Long> conceptEntityIds, long versionId)
    {
    	Map<Long, String> map = new HashMap<Long,String>();
    	List<String> propertyTypeNames = new ArrayList<String>();
    	propertyTypeNames.add("GEM_Flags");
    	List<Property> propertyList = PropertyDelegate.getProperties(conceptEntityIds, versionId, propertyTypeNames);
    	for (Property property : propertyList)
		{
    		map.put(property.getConceptEntityId(), property.getValue());
		}
    	return map;
    }
}
