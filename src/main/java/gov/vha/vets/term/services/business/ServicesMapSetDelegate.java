package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.dto.MapSetDTO;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.dto.RegionChecksumDTO;
import gov.vha.vets.term.services.dto.publish.PublishMapSetDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.util.ChecksumCalculator;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServicesMapSetDelegate
{
	/**
	 * Get a list of MapEntries that are available for publishing
	 * @param mapSetIds
	 * @return List<PublishMapSetDTO
	 * @throws STSException 
	 */
	public static PublishMapSetDTO getMapEntryForPublish(MapSet mapSet) throws STSException
	{
		List<MapEntryDTO> mapEntries = MapEntryDelegate.getChanges(mapSet.getEntityId());
        
        return new PublishMapSetDTO(mapSet.getEntityId(), mapSet.getVuid(), mapSet.getName(), new Date(), mapEntries);
	}

	public static List<RegionChecksumDTO> getChecksums(List<MapSetDesignationDTO> mapSets) throws STSException
    {
		return getChecksums(mapSets, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
	/**
	 * Get the checksum for a list of mapSets
	 * @param mapSets
	 * @param versionId
	 * @return
	 * @throws STSException
	 */
	public static List<RegionChecksumDTO> getChecksums(List<MapSetDesignationDTO> mapSets, Long versionId) throws STSException
    {
        List<RegionChecksumDTO> regionChecksums = new ArrayList<RegionChecksumDTO>();
        for (MapSetDesignationDTO mapSetDesignationDTO : mapSets)
		{
			regionChecksums.add(getChecksum(mapSetDesignationDTO, versionId));
		}
        return regionChecksums;
    }

	/**
	 * Get the checksum for a mapset and version
	 * @param mapSet
	 * @param versionId
	 * @return
	 * @throws STSException
	 */
    private static RegionChecksumDTO getChecksum(MapSetDesignationDTO mapSetDesignationDTO, Long versionId) throws STSException
    {
        ChecksumCalculator checksum = new ChecksumCalculator(mapSetDesignationDTO.getDesignation().getName());
      
        List<MapEntryDTO> mapEntries = MapEntryDelegate.getEntriesOrderedByVuid(mapSetDesignationDTO.getMapSet().getEntityId(), versionId);
        for (MapEntryDTO entry : mapEntries)
        {
            checksum.write(""+entry.getMapEntry().getVuid()); // Mapping id
            checksum.write(entry.getMapEntry().getSourceCode()); // Source Concept Code
            checksum.write(entry.getMapEntry().getTargetCode()); // Target Concept Code
            checksum.write(""+entry.getMapSetRelationship().getSequence()); // Sequence
        }
        checksum.close();
        RegionChecksumDTO checksumDTO = new RegionChecksumDTO(mapSetDesignationDTO.getMapSet().getName(), checksum.getChecksum());
        return checksumDTO;
    }
    
    public static MapSet getMapSet(long mapSetEntityId)
    {
    	return MapSetDelegate.get(mapSetEntityId);
    }
    
	public static List<MapSetDesignationDTO> getMapSets()
	{
		return MapSetDelegate.getAll();
	}

	public static List<MapSetDesignationDTO> get(List<Long> mapSetEntityIds)
	{
		return MapSetDelegate.get(mapSetEntityIds);
	}

	public static List<MapSetDTO> getMapSetDTOs(List<Long> mapSetEntityIds)
	{
		List<MapSetDTO> results = new ArrayList<MapSetDTO>();
		List<MapSetDesignationDTO> mapSets = MapSetDelegate.get(mapSetEntityIds);
		for (MapSetDesignationDTO mapSet : mapSets)
		{
			int count = MapEntryDelegate.getEntryCount(mapSet.getMapSet().getEntityId(), HibernateSessionFactory.AUTHORING_VERSION_ID);
            MapSetDTO mapSetDTO = new MapSetDTO(mapSet.getMapSet().getEntityId(), mapSet.getDesignation().getName(), 
            		VersionDelegate.getByVersionId(mapSet.getMapSet().getSourceVersionId()), 
            		VersionDelegate.getByVersionId(mapSet.getMapSet().getTargetVersionId()), count);
            mapSetDTO.setVuid(mapSet.getMapSet().getVuid());
            
            results.add(mapSetDTO);
		}
		return results;
	}

}
