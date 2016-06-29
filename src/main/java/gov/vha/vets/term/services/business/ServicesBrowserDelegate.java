package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ServicesBrowserDao;
import gov.vha.vets.term.services.dto.BrowseMappingHeaderDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipDTO;
import gov.vha.vets.term.services.dto.DesignationRelationshipDTO;
import gov.vha.vets.term.services.dto.DomainDTO;
import gov.vha.vets.term.services.dto.GemDTO;
import gov.vha.vets.term.services.dto.MapEntryVersionDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.dto.MapSetHistoryDTO;
import gov.vha.vets.term.services.dto.RegionDTO;
import gov.vha.vets.term.services.dto.RelationshipDTO;
import gov.vha.vets.term.services.dto.SearchResultDTO;
import gov.vha.vets.term.services.dto.SubsetRelationshipDTO;
import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.dto.history.ConceptHistoryDTO;
import gov.vha.vets.term.services.dto.history.DesignationEntityChangeDTO;
import gov.vha.vets.term.services.dto.history.DesignationEntityHistoryDTO;
import gov.vha.vets.term.services.dto.history.EntityChangeDTO;
import gov.vha.vets.term.services.dto.history.EntityHistoryDTO;
import gov.vha.vets.term.services.dto.history.RelationshipEntityChangeDTO;
import gov.vha.vets.term.services.dto.history.SubsetEntityChangeDTO;
import gov.vha.vets.term.services.exception.STSEntityNotInMapsetException;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.BaseVersion;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationRelationship;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.comparator.VersionSortById;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.exception.GenericJDBCException;

public class ServicesBrowserDelegate
{
	public static final String CURRENT_MAPPING = "Current Mapping";
	public static final String FILTER_OPTION_CONTAINS = "Contains";
	public static final String FILTER_OPTION_BEGINS_WITH = "Begins with";
	public static final String FILTER_OPTION_ENDS_WITH = "Ends with";
	public static final String FILTER_OPTION_EXACT_MATCH = "Exact match";

	public static MapSetHistoryDTO getMapSetHistory(MapSet mapSet, Designation mapSetDesignation, long mapEntryEntityId, boolean includeAuthoring)
		throws STSException
    {
        HibernateSessionFactory.currentSession().clear();

        MapSetHistoryDTO mapSetHistory = new MapSetHistoryDTO();
        mapSetHistory.setMapSet(mapSet);
        mapSetHistory.setMapSetDesignation(mapSetDesignation);

    	List<Version> completeVersions = new ArrayList<Version>();
		// Determine the most recent Map Set version (note: This is not the version for the map set entries)
		Version mostCurrentMapSetVersion = MapSetDelegate.getCurrentVersionByVuid(mapSetHistory.getMapSet().getVuid());
		mapSetHistory.setMostCurrentVersion(mostCurrentMapSetVersion);
		
    	List<Version> versions = ServicesBrowserDao.getMapEntryFinalizedVersions(mapSet, mapEntryEntityId, includeAuthoring);
		
       	completeVersions.add(mostCurrentMapSetVersion); 
    	completeVersions.addAll(versions);
    	
    	List<MapEntryVersionDetailDTO> mapEntryVersionDetails = ServicesBrowserDao.getMapEntryVersions(mapSet, mapEntryEntityId, includeAuthoring, false);
		if (mapEntryVersionDetails.size() < 1)
		{
			throw new STSEntityNotInMapsetException(" No map entries found for entityId: "+mapEntryEntityId+" in MapSet: "+mapSetDesignation.getName());
		}
		
/*    	Iterator<MapEntryVersionDetailDTO> iterator = mapEntryVersionDetails.iterator();
    	int index = 0;
		boolean setSelectedMapEntryDetail = false;
    	while (iterator.hasNext()) {
    		MapEntryVersionDetailDTO mapEntryVersionDetailDTO = iterator.next();
    		if (!mapEntryVersionDetailDTO.isActive() && iterator.hasNext())
    		{
    			mapEntryVersionDetails.set(index, mapEntryVersionDetails.get(index+1));
    		}    	
			if (!setSelectedMapEntryDetail && mapEntryVersionDetailDTO.getEntityId() == mapEntryEntityId)
			{
				mapSetHistory.setSelectedMapEntryDetail(mapEntryVersionDetailDTO);
				setSelectedMapEntryDetail = true;
			}
    		
    	}*/

    	// find and save selected mapEntry information
    	for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : mapEntryVersionDetails)
		{
			if (mapEntryVersionDetailDTO.getEntityId() == mapEntryEntityId)
			{
				mapSetHistory.setSelectedMapEntryDetail(mapEntryVersionDetailDTO);
				break;
			}
		}
    	HashMap<String, List<MapEntryVersionDetailDTO>> historyMap = createMapEntryHistoryMap(mapEntryVersionDetails);
    	mapSetHistory.setMapSetVersions(completeVersions);
    	mapSetHistory.setMapEntryVersionMap(historyMap);
    	
    	mapSetHistory.setMaxSequenceNumber(getMaxSequenceNumber(mapEntryVersionDetails));
    	updateMapSetEntryDetailStatus(mapSetHistory);
    	
    	return mapSetHistory;
    }

	private static HashMap<String, List<MapEntryVersionDetailDTO>> createMapEntryHistoryMap(List<MapEntryVersionDetailDTO> mapEntryVersionDetailDTOList)
	{
		HashMap<String, List<MapEntryVersionDetailDTO>> mapEntryHistoryMap = new HashMap<String, List<MapEntryVersionDetailDTO>>();

		long versionId = -1L;
		String versionName = null;
		Map<Integer, MapEntryVersionDetailDTO> currentMapEntryVersionMap = new HashMap<Integer, MapEntryVersionDetailDTO>();
		Map<Integer, MapEntryVersionDetailDTO> previousMapEntryVersionMap = new HashMap<Integer, MapEntryVersionDetailDTO>();
		int currentSequenceCount = 0;
		int previousSequenceCount = 0;
		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : mapEntryVersionDetailDTOList)
		{
			currentSequenceCount = mapEntryVersionDetailDTO.getSequence();
			
			if (mapEntryVersionDetailDTO.getVersionId() != versionId)
			{
				if (versionId != -1L) // If this is not the first time through the loop such that there is both a current and a previous
				{
					while (previousSequenceCount >= currentSequenceCount)
					{
						MapEntryVersionDetailDTO previousMapEntryVersionDetailDTO = previousMapEntryVersionMap.get(currentSequenceCount);
						currentMapEntryVersionMap.put(currentSequenceCount, previousMapEntryVersionDetailDTO);
					}
					Collection<MapEntryVersionDetailDTO> mapEntryVersionDetailCollection = currentMapEntryVersionMap.values();
					List<MapEntryVersionDetailDTO> mapEntryVersionDetailList = new ArrayList<MapEntryVersionDetailDTO>(mapEntryVersionDetailCollection);
					Collections.sort(mapEntryVersionDetailList); // sort list by sequence (ascending order)
					mapEntryHistoryMap.put(versionName, mapEntryVersionDetailList);
				}

				previousSequenceCount = currentSequenceCount-1;

				previousMapEntryVersionMap = currentMapEntryVersionMap;
				currentMapEntryVersionMap = new HashMap<Integer, MapEntryVersionDetailDTO>();
				versionId = mapEntryVersionDetailDTO.getVersionId();
				versionName = VersionDelegate.getByVersionId(versionId).getName();
			}
			
			//currentSequenceCount++;
			// populate currentMapEntryVersionMap with previousMapEntryVersionMap
			// with sequences that are not in the currentMapEntryVersionMap
			while (mapEntryVersionDetailDTO.getSequence() > currentSequenceCount)
			{
				MapEntryVersionDetailDTO previousMapEntryVersionDetailDTO = previousMapEntryVersionMap.get(currentSequenceCount);
				currentMapEntryVersionMap.put(currentSequenceCount, previousMapEntryVersionDetailDTO);
				currentSequenceCount++;
			}
			
			// if we have the sequence then put the mapEntryVersionDetailDTO in the currentMapEntryVersionMap
			// otherwise populate the currentMapEntryVersionMap with the previousMapEntryVersionMap
			if (mapEntryVersionDetailDTO.getSequence() == currentSequenceCount)
			{
				MapEntryVersionDetailDTO currentMapEntryVersionDTO = currentMapEntryVersionMap.get(currentSequenceCount);
				if (currentMapEntryVersionDTO != null && mapEntryVersionDetailDTO.isActive() == false)
				{
					continue;
				}
				currentMapEntryVersionMap.put(currentSequenceCount, mapEntryVersionDetailDTO);
			}
			else if (previousSequenceCount >= currentSequenceCount)
			{
				MapEntryVersionDetailDTO previousMapEntryVersionDetailDTO = previousMapEntryVersionMap.get(currentSequenceCount);
				currentMapEntryVersionMap.put(currentSequenceCount, previousMapEntryVersionDetailDTO);
			}
		}
		
		if (versionId != -1L)
		{
			// finish populating 'currentMapEntryVersionMap'  
			while (previousSequenceCount >= currentSequenceCount)
			{
				MapEntryVersionDetailDTO previousMapEntryVersionDetailDTO = previousMapEntryVersionMap.get(currentSequenceCount);
				currentMapEntryVersionMap.put(currentSequenceCount, previousMapEntryVersionDetailDTO);
			}
			Collection<MapEntryVersionDetailDTO> mapEntryVersionCollection = currentMapEntryVersionMap.values();
			List<MapEntryVersionDetailDTO> mapEntryVersionDetailList = new ArrayList<MapEntryVersionDetailDTO>(mapEntryVersionCollection);
			Collections.sort(mapEntryVersionDetailList); // sort list by sequence (ascending order)
			mapEntryHistoryMap.put(versionName, mapEntryVersionDetailList);
			
			// create a list from the previous versions list so that
			// we have a CURRENT_MAPPING key in the mapEntryHistoryMap
			List<MapEntryVersionDetailDTO> currentMapEntryVersionDetailList = new ArrayList<MapEntryVersionDetailDTO>();

			for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : mapEntryVersionDetailDTOList)
			{
				/*if (mapEntryVersionDetailDTO.isActive() == true)
				{*/
					currentMapEntryVersionDetailList.add(mapEntryVersionDetailDTO);
				/*} else { // inactive
					// CCR 1203 - if the most recent map entry detail (first in list) is inactive add it to the list but skip any older inactivation
					//            if the list is still empty and this entry is inactive that means that the most recent map entry is set to inactive... show it on details page
					if (currentMapEntryVersionDetailList.isEmpty()) {
						currentMapEntryVersionDetailList.add(mapEntryVersionDetailDTO);						
					}
				}*/
			}
			mapEntryHistoryMap.put(CURRENT_MAPPING, currentMapEntryVersionDetailList);
		}
		
		return mapEntryHistoryMap;
	}
	
	private static int getMaxSequenceNumber(List<MapEntryVersionDetailDTO> mapEntryVersionDetailDTOList)
	{
		int maxSequence = 0;
		
		for (MapEntryVersionDetailDTO mapEntryVersionDetailDTO : mapEntryVersionDetailDTOList)
		{
			if (maxSequence < mapEntryVersionDetailDTO.getSequence())
			{
				maxSequence = mapEntryVersionDetailDTO.getSequence();
			}
		}

		return maxSequence;
	}

	private static void updateMapSetEntryDetailStatus(MapSetHistoryDTO mapSetHistory)
	{
		List<Version> versions = mapSetHistory.getMapEntryVersions();
		HashMap<Integer, MapEntryVersionDetailDTO> sequenceMapEntryDetailMap = new HashMap<Integer, MapEntryVersionDetailDTO>();
		int numberOfVersions = versions.size();
		
		for (int i=numberOfVersions-1; i > 0; i--)
		{
			List<MapEntryVersionDetailDTO> mapEntryVersionDTO = mapSetHistory.getMapEntryVersionMap().get(versions.get(i).getName());
			if (null != mapEntryVersionDTO){
				for (MapEntryVersionDetailDTO currentMapEntryVersionDetailDTO : mapEntryVersionDTO)
				{
					MapEntryVersionDetailDTO previousMapEntryDetailDTO = sequenceMapEntryDetailMap.get(currentMapEntryVersionDetailDTO.getSequence());
	                if (previousMapEntryDetailDTO == null)
	                {
	                	currentMapEntryVersionDetailDTO.setStatus(MapEntryVersionDetailDTO.StatusType.ADDED);
	                }
	                else if (currentMapEntryVersionDetailDTO.isActive() == true && previousMapEntryDetailDTO.isActive() == false)
	                {
	                	currentMapEntryVersionDetailDTO.setStatus(MapEntryVersionDetailDTO.StatusType.ACTIVATED);
	                }
	                else if (currentMapEntryVersionDetailDTO.isActive() == false && previousMapEntryDetailDTO.isActive() == true)
	                {
	                	currentMapEntryVersionDetailDTO.setStatus(MapEntryVersionDetailDTO.StatusType.INACTIVATED);
	                }
	                else if (currentMapEntryVersionDetailDTO != previousMapEntryDetailDTO)
	                {
	                	currentMapEntryVersionDetailDTO.setStatus(MapEntryVersionDetailDTO.StatusType.UPDATED);
	                }
	                
	                sequenceMapEntryDetailMap.put(currentMapEntryVersionDetailDTO.getSequence(), currentMapEntryVersionDetailDTO);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.vets.term.services.business.BrowserInterface#getConceptDetailDto(int)
	 */
	@SuppressWarnings("unchecked")
    public static ConceptHistoryDTO getConceptVersionDetail(long conceptEntityId, boolean includeAuthoring) throws STSException
	{
        HibernateSessionFactory.currentSession().clear();

	    // get all the raw data
        List<CodedConcept> codedConcepts = CodedConceptDelegate.getAllVersions(conceptEntityId,includeAuthoring);
        if (codedConcepts.size() == 0)
        {
        	throw new STSException("Concept for entity id: " + conceptEntityId + " is not found.");
        }
        List<Designation> designations = DesignationDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        List<Property> properties = PropertyDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        List<DesignationRelationshipDTO> designationRelationships = DesignationRelationshipDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        List<ConceptRelationshipDTO> conceptRelationshipDTOs = ConceptRelationshipDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        List<ConceptRelationshipDTO> conceptChildrenRelationshipDTOs = ConceptRelationshipDelegate.getAllChildrenVersions(conceptEntityId, includeAuthoring);
        List<SubsetRelationshipDTO> subsetRelationshipDTOs = SubsetDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        List<Property> designationProperties = DesignationPropertyDelegate.getAllVersions(conceptEntityId, includeAuthoring);
        
        // get a unique list of versions are contained in the changes
        Set<Version> versionSet = new HashSet<Version>();
        versionSet.addAll(getVersions(codedConcepts));
        versionSet.addAll(getVersions(properties));
        versionSet.addAll(getVersions(designations));
        versionSet.addAll(getVersions(subsetRelationshipDTOs));
        versionSet.addAll(getVersions(conceptRelationshipDTOs));
        versionSet.addAll(getVersions(conceptChildrenRelationshipDTOs));
        versionSet.addAll(getVersions(designationProperties));
        versionSet.addAll(getVersions(designationRelationships));

        Version currentVersion = VersionDelegate.getRecent(codedConcepts.get(0).getCodeSystem().getName(), includeAuthoring);

        List<Version> versions = new ArrayList<Version>();
        versions.addAll(versionSet);
        Collections.sort(versions);
        Set<Long> conceptEntityIds = new HashSet<Long>();
        conceptEntityIds.add(conceptEntityId);
        
        Map<Long, List<ConceptRelationshipDTO>> relationshipMap = new HashMap<Long, List<ConceptRelationshipDTO>>();
        for (ConceptRelationshipDTO conceptRelationshipDTO : conceptRelationshipDTOs)
        {
            conceptEntityIds.add(conceptRelationshipDTO.getRelationship().getTargetEntityId());
            List<ConceptRelationshipDTO> conceptRelationships = relationshipMap.get(conceptRelationshipDTO.getRelationship().getTargetEntityId());
            if (conceptRelationships == null)
            {
                conceptRelationships = new ArrayList<ConceptRelationshipDTO>();
                relationshipMap.put(conceptRelationshipDTO.getRelationship().getTargetEntityId(), conceptRelationships);
            }
            conceptRelationships.add(conceptRelationshipDTO);
        }
        for (ConceptRelationshipDTO conceptRelationshipDTO : conceptChildrenRelationshipDTOs)
        {
			//child relationships are inverse to has_parent so use the source instead of the target
            conceptEntityIds.add(conceptRelationshipDTO.getRelationship().getSourceEntityId());
            List<ConceptRelationshipDTO> conceptRelationships = relationshipMap.get(conceptRelationshipDTO.getRelationship().getSourceEntityId());
            if (conceptRelationships == null)
            {
                conceptRelationships = new ArrayList<ConceptRelationshipDTO>();
                relationshipMap.put(conceptRelationshipDTO.getRelationship().getSourceEntityId(), conceptRelationships);
            }
            conceptRelationships.add(conceptRelationshipDTO);
        }
        
        Map<Long, Designation> designationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(currentVersion.getCodeSystem(), currentVersion.getId(), conceptEntityIds);
        for (Iterator<Entry<Long, Designation>> iter = designationMap.entrySet().iterator();iter.hasNext();)
        {
            Map.Entry<Long, Designation> entry = iter.next();
            Long key = (Long) entry.getKey();
            List<ConceptRelationshipDTO> relationships = relationshipMap.get(key);
            if(relationships != null)
            {
	            for (ConceptRelationshipDTO relationship : relationships)
	            {
	                relationship.setName(((Designation)entry.getValue()).getName());
	            }
	        }
        }
        Designation preferred = designationMap.get(conceptEntityId);
        
        // process all the data into the model
        List<EntityHistoryDTO> conceptHistoryDTOs = (List<EntityHistoryDTO>) processEntity(codedConcepts, versions, conceptEntityId);
        List<EntityHistoryDTO> propertyHistoryDTOs = (List<EntityHistoryDTO>) processEntity(properties, versions, conceptEntityId);
        List<DesignationEntityHistoryDTO> designationHistoryDTOs = (List<DesignationEntityHistoryDTO>)processEntity(designations, versions, conceptEntityId);
        List<EntityHistoryDTO> designationRelationshipHistoryDTOs = (List<EntityHistoryDTO>)processEntity(designationRelationships, versions, conceptEntityId);
        List<EntityHistoryDTO> relationshipHistoryDTOs = (List<EntityHistoryDTO>) processEntity(conceptRelationshipDTOs, versions, true, conceptEntityId);
        List<EntityHistoryDTO> childrenRelationshipHistoryDTOs = (List<EntityHistoryDTO>) processEntity(conceptChildrenRelationshipDTOs, versions, false, conceptEntityId);
        List<EntityHistoryDTO> designationPropertyHistoryDTOs = (List<EntityHistoryDTO>) processEntity(designationProperties, versions, conceptEntityId);
        List<EntityHistoryDTO> subsetHistoryDTOs = (List<EntityHistoryDTO>) processEntity(subsetRelationshipDTOs, versions, conceptEntityId);

        // parcel out the designation properties to designation
        //    first build a map of the designations
        Map<Long, DesignationEntityHistoryDTO> lookup = new HashMap<Long, DesignationEntityHistoryDTO>();
        for (EntityHistoryDTO entityHistoryDTO : designationHistoryDTOs)
        {
            lookup.put(entityHistoryDTO.getCurrent().getEntity().getEntityId(), (DesignationEntityHistoryDTO) entityHistoryDTO);
        }
        //   second iterate over the properties and assign them to the designations
        for (EntityHistoryDTO propertyHistory : designationPropertyHistoryDTOs)
        {
            DesignationEntityHistoryDTO designationHistory = lookup.get(((Property)propertyHistory.getCurrent().getEntity()).getConceptEntityId());
            designationHistory.addProperty(propertyHistory);
        }
        //   third iterate over the subsets and assign them to the designations
        for (EntityHistoryDTO subsetHistoryDTO : subsetHistoryDTOs)
        {
            DesignationEntityHistoryDTO designationHistory = lookup.get(((SubsetRelationship)subsetHistoryDTO.getCurrent().getEntity()).getTargetEntityId());
            designationHistory.addSubset(subsetHistoryDTO);
        }
        
        // Fish out the parent relationships from the mix
        List<EntityHistoryDTO> parentRelationshipHistoryDTOs = new ArrayList<EntityHistoryDTO>();
        for (Iterator<EntityHistoryDTO> iterator = relationshipHistoryDTOs.iterator(); iterator.hasNext();)
        {
            EntityHistoryDTO relationship = iterator.next();
            if (RelationshipType.HAS_PARENT.equals( ((ConceptRelationship)relationship.getCurrent().getEntity()).getRelationshipType().getName()) || 
                    RelationshipType.HAS_ROOT.equals( ((ConceptRelationship)relationship.getCurrent().getEntity()).getRelationshipType().getName()))
            {
                parentRelationshipHistoryDTOs.add(relationship);
                iterator.remove();
            }
        }
        
        Collections.sort(propertyHistoryDTOs);
        Collections.sort(designationHistoryDTOs);
        
        // Find any designation that should not be considered for the preferred designation 
        HashSet<Long> excludedDesignations = new HashSet<Long>();
        for (EntityHistoryDTO designationRelationshipDTO : designationRelationshipHistoryDTOs)
        {
        	// if the source of the designation does not match the concept then we know that this designation has been moved
        	if (((DesignationRelationship)designationRelationshipDTO.getCurrent().entity).getSourceEntityId() != conceptEntityId)
        	{
        		excludedDesignations.add(((DesignationRelationship)designationRelationshipDTO.getCurrent().entity).getTargetEntityId());
        	}
        }
        
        for (DesignationEntityHistoryDTO designationEntityHistory : designationHistoryDTOs)
        {
            Collections.sort(designationEntityHistory.getProperties());
            Collections.sort(designationEntityHistory.getSubsets());
        }
        Collections.sort(parentRelationshipHistoryDTOs);
        Collections.sort(childrenRelationshipHistoryDTOs);
        Collections.sort(relationshipHistoryDTOs);
        // build the final object to return
        ConceptHistoryDTO conceptHistory = new ConceptHistoryDTO(currentVersion, versions, (EntityHistoryDTO)conceptHistoryDTOs.get(0), propertyHistoryDTOs, 
                designationHistoryDTOs, designationRelationshipHistoryDTOs, relationshipHistoryDTOs, parentRelationshipHistoryDTOs, childrenRelationshipHistoryDTOs);
        conceptHistory.setPreferredDesignation(preferred);
        return conceptHistory;
	}

	private static List<? extends EntityHistoryDTO> processEntity(List<? extends Object> entityList, List<Version> versions, long conceptEntityId)
	{
	    return processEntity(entityList, versions, null, conceptEntityId);
	}

	/**
     * @param conceptEntityId TODO
	 * @param detail
	 * @param result
	 * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws NoSuchMethodException 
     */
    @SuppressWarnings("unchecked")
    private static List<? extends EntityHistoryDTO> processEntity(List<? extends Object> entityList, List<Version> versions, Boolean useTargetForRel, long conceptEntityId) 
    {

        List<EntityHistoryDTO> results = new ArrayList<EntityHistoryDTO>();
        EntityHistoryDTO entityHistory = null;
        long currentEntityId = -1;
        BaseVersion currentEntity = null;
        EntityChangeDTO currentEntityChangeDTO = null;
        
        // Loop over each item that is passed in
        for (Object entity : entityList)
        {
            Version version = null;
            BaseVersion entityItem = null;
            long entityId = 0L;
            
            // we have either an entity or and entity Wrapper because some entities like relationship need the name of the related concept
            if (entity instanceof BaseVersion)
            {
                version = ((BaseVersion)entity).getVersion();
                entityId = ((BaseVersion)entity).getEntityId();
                entityItem = (BaseVersion)entity;
            }
            else if (entity instanceof RelationshipDTO)
            {
                version = ((RelationshipDTO)entity).getRelationship().getVersion();
                entityId = ((RelationshipDTO)entity).getRelationship().getEntityId();
                entityItem = ((RelationshipDTO)entity).getRelationship();
            }

            // Check to see if we have a new entity
            if (currentEntityId != entityId)
            {
                // if this is true then we have hit a new entity and we need to update the prior one
                if (currentEntityId > 0)
                {
                    entityHistory.setCurrent(currentEntityChangeDTO);
                    currentEntity = null;
                }

                // set the currentEntity
                currentEntityId = entityId;
                if (entity instanceof Designation)
                {
                    entityHistory = new DesignationEntityHistoryDTO();
                }
                else
                {
                    entityHistory = new EntityHistoryDTO();
                }
                results.add(entityHistory);
            }
            EntityChangeDTO entityChangeDTO = null;

            // add a change for each version level
            if (version != null)
            {
                if (entity instanceof BaseVersion)
                {
                    entityChangeDTO = new EntityChangeDTO(entityItem);
                }
                else if (entity instanceof ConceptRelationshipDTO)
                {
                    ConceptRelationship conceptRelationship = (ConceptRelationship)entityItem;
                    Long relatedEntityId =  (useTargetForRel.booleanValue()) ? conceptRelationship.getTargetEntityId() : conceptRelationship.getSourceEntityId();
                    entityChangeDTO = new RelationshipEntityChangeDTO(entityItem, ((ConceptRelationshipDTO)entity).getName(), relatedEntityId);
                }
                else if (entity instanceof SubsetRelationshipDTO)
                {
                    entityChangeDTO = new SubsetEntityChangeDTO(entityItem, ((SubsetRelationshipDTO)entity).getName());
                }
                else if (entity instanceof DesignationRelationshipDTO)
                {
                    entityChangeDTO = new DesignationEntityChangeDTO(entityItem, ((DesignationRelationshipDTO)entity).getName());
                }
                entityHistory.setChange(entityChangeDTO, version.getId());
            }
            
            // set the currentEntity to reflect the highest numerical version found for this entity
            if (currentEntity == null || currentEntity.getVersion().getId() <= version.getId())
            {    
                currentEntity = entityItem;
                currentEntityChangeDTO = entityChangeDTO;
            }
        }
        // this case happens when there is only one version for an entity
        if (entityHistory != null && currentEntityChangeDTO != null)
        {
            entityHistory.setCurrent(currentEntityChangeDTO);
        }
        
        List<Version> versionsSorted = ((List<Version>) ((ArrayList<Version>) versions).clone());

        Collections.sort(versionsSorted, new VersionSortById());
        // figure out what the status is for each change ie ADDED, INACTIVATED, UPDATED
        for (EntityHistoryDTO entityHistoryDTO : results)
        {
            EntityChangeDTO prev = null;
            if (entityHistoryDTO != null)
            {
                for (Version version : versionsSorted)
                {
                    EntityChangeDTO current = entityHistoryDTO.getChange(version.getId());
                    if (current != null)
                    {
                        if (prev == null)
                        {
                            current.setStatus(EntityChangeDTO.StatusType.ADDED);
                        }
                        else if (current.getEntity().getActive() == true && prev.getEntity().getActive() == false)
                        {
                            current.setStatus(EntityChangeDTO.StatusType.ACTIVATED);
                        }
                        else if (current.getEntity().getActive() == false && prev.getEntity().getActive() == true)
                        {
                            current.setStatus(EntityChangeDTO.StatusType.INACTIVATED);
                        }
                        else
                        {
                            if (current.getEntity() instanceof DesignationRelationship)
                            {
                                DesignationRelationship prevDesignationRelationship = (DesignationRelationship)prev.getEntity();
                                DesignationRelationship currentDesignationRelationship = (DesignationRelationship)current.getEntity();
                                if (prevDesignationRelationship.getSourceEntityId() != currentDesignationRelationship.getSourceEntityId())
                                {
                                    if (conceptEntityId == prevDesignationRelationship.getSourceEntityId())
                                    {
                                        current.setStatus(EntityChangeDTO.StatusType.MOVED_TO);
                                    }
                                    else
                                    {
                                        current.setStatus(EntityChangeDTO.StatusType.MOVED_FROM);
                                    }
                                }
                            }
                            else
                            {
                                current.setStatus(EntityChangeDTO.StatusType.UPDATED);
                            }
                        }
                        prev = current;
                    }
                }
            }
        }
        return results;
    }

    private static Set<Version> getVersions(List<?> entityItems)
    {
        Set<Version> results = new HashSet<Version>();
        for (Object entityItem : entityItems)
        {
            Version version=null;
            if (entityItem instanceof BaseVersion)
            {
                version = ((BaseVersion)entityItem).getVersion();
            }
            else if (entityItem instanceof RelationshipDTO)
            {
                version = ((RelationshipDTO)entityItem).getRelationship().getVersion();
            }
            results.add(version);
        }
        return results;
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.vets.term.services.business.BrowserInterface#getVersionList(gov.vha.vets.term.services.model.CodeSystem)
	 */
    public static List<Version> getFinalizedVersions(CodeSystem codeSystem)
	{
        HibernateSessionFactory.currentSession().clear();

        return VersionDelegate.getFinalizedVersions(codeSystem);
	}
	
	public List<Version> getVHATVersions(boolean includeAuthoring)
	{
        HibernateSessionFactory.currentSession().clear();

        return VersionDelegate.getVHATVersions(includeAuthoring);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.vha.vets.term.services.business.BrowserInterface#getCodeSystemsList()
	 */
    public static List<CodeSystem> getCodeSystems() throws STSException
	{
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        return CodeSystemDelegate.getCodeSystems();
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
	}
	
	/**
	 * Get a list of Domains and their Subsets that have Designations
	 * in a finalized version.  The returned list of DomainDTO objects
	 * does not contain a populated List<ConceptEntityDTO>.
	 * @return List<DomainDTO>
	 * @throws STSException 
	 */
	public List<DomainDTO> getDomainsHavingVersions() throws STSException
	{
		List<DomainDTO> domainList = null; 
		
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        //Get the list of Subsets having Designations in a finalized version
			List<Subset> versionedSubsets = SubsetDelegate.getFinalizedSubsets();

			//for each of the subsetNames we just got, get the Domain name
			domainList = new ArrayList<DomainDTO>();
			List<DomainConfig> domainConfigList = TerminologyConfigDelegate.getDomains();
			
			for(DomainConfig domainConfig : domainConfigList)
			{
				List<SubsetConfig> subsetConfigList = domainConfig.getSubsets();
				
				List<RegionDTO> regionList = new ArrayList<RegionDTO>();
				for(SubsetConfig subsetConfig : subsetConfigList)
				{
					for (Subset versionedSubset : versionedSubsets)
					{
						if (versionedSubset.getName().equals(subsetConfig.getName()))
						{
							regionList.add(new RegionDTO(subsetConfig.getName(), null, subsetConfig.isActive()));
							break;
						}
					}
				}
				
				if(regionList.size() > 0)
				{
					domainList.add(new DomainDTO(domainConfig.getName(), regionList));
				}
			}
			// display non-domain as a domain
			List<RegionDTO> regionDTOs = new ArrayList<RegionDTO>();
			regionDTOs.add(new RegionDTO(RegionDTO.NON_SUBSET, null, false));
			domainList.add(new DomainDTO(DomainDTO.NON_DOMAIN, regionDTOs));
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
		catch (STSException e)
		{
			e.printStackTrace();
		}
		
		return domainList;
	}
	
	public static SearchResultDTO searchVHAT(String searchString, String versionName, List<String> subsetNames,
		        boolean searchVUID, boolean searchConceptName, boolean searchDesignationName, boolean searchPropertyName,
		        boolean searchPropertyValue, boolean searchRelationshipName, boolean searchRelationshipValue, int maxSearchResults,
		        String selectedFilterByOption)
	        throws STSException
	{
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        Version selectedVersion = VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, versionName);
			boolean includeNonSubset = false;
			
			if (subsetNames.contains(RegionDTO.NON_SUBSET))
			{
				includeNonSubset = true;
			}

			List<Subset> selectedSubsets = SubsetDelegate.getSubsetsByNames(subsetNames);
			return ServicesBrowserDao.searchVHAT(searchString, selectedVersion,
					selectedSubsets, includeNonSubset, searchVUID, searchConceptName, searchDesignationName,
					searchPropertyName, searchPropertyValue, searchRelationshipName, searchRelationshipValue,
					maxSearchResults, selectedFilterByOption);
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
	}
	
	
	public static SearchResultDTO searchSDOs(String searchString, CodeSystem codeSystem, String versionName,
			boolean searchConceptCode, boolean searchDesignationName, int maxSearchResults, String selectedFilterByOption) throws STSException
	{
        try
        {
            HibernateSessionFactory.currentSession().clear();

            Version version = VersionDelegate.get(codeSystem.getId(), versionName);
            if (version == null)
            {
            	throw new STSException("Version does not exits!");
            }

            return ServicesBrowserDao.searchSDO(searchString, codeSystem, version, 
            			searchConceptCode, searchDesignationName, maxSearchResults, selectedFilterByOption);
        }
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
	}
	
    public static SearchResultDTO searchMappings(String searchString, 
            boolean searchSourceCode, boolean searchSourceCodeDescription,  boolean searchTargetCode,
            boolean searchTargetCodeDescription, boolean searchVuid, BrowseMappingHeaderDTO browseMappingHeaderDTO,
            int maxSearchResults, String selectedFilterByOption) throws STSException
  {
      try
      {
          HibernateSessionFactory.currentSession().clear();

          return ServicesBrowserDao.searchMappings(searchString,
                searchSourceCode, searchSourceCodeDescription, searchTargetCode,
                searchTargetCodeDescription, searchVuid, browseMappingHeaderDTO,
                maxSearchResults, selectedFilterByOption);
      }
      catch (GenericJDBCException e)
      {
          handleGenericJDBCException(e);
          
          throw e;
      }
  }

	public static SearchResultDTO searchGemMappings(String searchString, 
			  boolean searchSourceCode, boolean searchSourceCodeDescription,  boolean searchTargetCode,
			  boolean searchTargetCodeDescription, BrowseMappingHeaderDTO browseMappingHeaderDTO,
			  int maxSearchResults, String selectedFilterByOption) throws STSException
	{
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        SearchResultDTO searchResultsDTO = ServicesBrowserDao.searchGemMappings(searchString,
	                searchSourceCode, searchSourceCodeDescription, searchTargetCode,
	                searchTargetCodeDescription, browseMappingHeaderDTO,
	                maxSearchResults, selectedFilterByOption);
        
	        
	        return searchResultsDTO;
		}
      catch (GenericJDBCException e)
      {
      	handleGenericJDBCException(e);
      	
      	throw e;
      }
	}

	private static void handleGenericJDBCException(GenericJDBCException e) throws STSException
	{
    	if (e.getMessage().indexOf("Cannot open connection") >= 0)
    	{
    		throw new STSException("All database connections are busy. Try search request later.");
    	}
	}
	
	public static List<MapSetDesignationDTO> getMapSets(boolean includeAuthoring) throws STSException
	{
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        return MapSetDelegate.getVersioned(includeAuthoring);
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
	}
	
	public static List<BrowseMappingHeaderDTO> getBrowseMappingHeaderList(MapSet mapSet, Designation mapSetDesignation, boolean includeAuthoring) throws STSException
	{
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        return ServicesBrowserDao.getBrowseMappingHeaderList(mapSet, mapSetDesignation, includeAuthoring);
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
	}
	
	public static Date getLastUpdatedDate() throws STSException
    {
		try
		{
	        HibernateSessionFactory.currentSession().clear();

	        return VersionDelegate.getLastUpdatedDate();
		}
        catch (GenericJDBCException e)
        {
        	handleGenericJDBCException(e);
        	
        	throw e;
        }
    }

	public static GemDTO getGemMapSetHistory(MapSet mapSet, Designation mapSetDesignation, long mapEntryEntityId, boolean includeAuthoring) throws STSException
	{
		return GemDelegate.getGemMapSetHistory(mapSet, mapSetDesignation, mapEntryEntityId, includeAuthoring);
	}
}
