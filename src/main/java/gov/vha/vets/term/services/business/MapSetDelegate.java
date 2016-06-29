package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ConceptDao;
import gov.vha.vets.term.services.dao.MapSetDao;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.dto.MapSetDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDetailListDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.ChangeGroup;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.ChangeGroupManager;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MapSetDelegate
{
	public static MapSet createVHAT(String name, String code, long vuid, boolean active, String sourceCodeSystemName,
			String sourceVersionName, String targetCodeSystemName, String targetVersionName, Date effectiveDate) throws STSException
	{
		Version authoringVersion = VersionDelegate.getAuthoring();
		Version targetVersion = VersionDelegate.get(targetCodeSystemName, targetVersionName);
		if (targetVersion == null)
		{
		    throw new STSException("Error while loading map set: "+name+" because target code system: "+targetCodeSystemName+" and version: "+targetVersionName+" does not exist!");
		}
		Version sourceVersion = VersionDelegate.get(sourceCodeSystemName, sourceVersionName);
        if (sourceVersion == null)
        {
            throw new STSException("Error while loading map set: "+name+" because source code system: "+sourceCodeSystemName+" and version: "+sourceVersionName+" does not exist!");
        }
		long targetVersionId = targetVersion.getId();
		long sourceVersionId = sourceVersion.getId();
		
		MapSet mapSet = new MapSet(name, code, vuid, authoringVersion.getCodeSystem(),
				authoringVersion, active, sourceVersionId, targetVersionId);
		mapSet.setEffectiveDate(effectiveDate);
		MapSetDao.save(mapSet);
		
		
		return mapSet;
	}
	
	public static MapSet updateVHAT(MapSet mapSet, boolean isActive)
	{
		Version authoringVersion = VersionDelegate.getAuthoring();
		MapSet mapSet2 = new MapSet(mapSet.getName(), mapSet.getCode(), mapSet.getVuid(),  mapSet.getCodeSystem(),
				authoringVersion, isActive, mapSet.getSourceVersionId(), mapSet.getTargetVersionId());
		mapSet2.setEntityId(mapSet.getEntityId());
		MapSetDao.save(mapSet2);
		
		return mapSet2;
	}

	public static MapSet updateEffectiveDate(MapSet mapSet)
	{
		MapSet mapSetResult = mapSet;
		if (mapSet.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID && mapSet.getEffectiveDate() == null)
		{
			mapSet.setEffectiveDate(new Date());
			MapSetDao.save(mapSet);
			mapSetResult = mapSet;
		}
		else if (mapSet.getVersion().getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
		{
			Version authoringVersion = VersionDelegate.getAuthoring();
			mapSetResult = new MapSet(mapSet.getName(), mapSet.getCode(), mapSet.getVuid(),  mapSet.getCodeSystem(),
					authoringVersion, mapSet.getActive(), mapSet.getSourceVersionId(), mapSet.getTargetVersionId());
			mapSetResult.setEffectiveDate(new Date());
			mapSetResult.setEntityId(mapSet.getEntityId());
            ChangeGroupManager.getInstance().setChangeGroup(new ChangeGroup(ChangeGroup.SourceName.TDS.toString()));
			MapSetDao.save(mapSetResult);
		}
		return mapSetResult;
	}
	
    public static MapSet updateVHAT(MapSet mapSet, String sourceCodeSystemName, String sourceVersionName, String targetCodeSystemName, String targetVersionName, Date effectiveDate) throws STSException
    {
        MapSet result = null;
        
        Version targetVersion = VersionDelegate.get(targetCodeSystemName, targetVersionName);
        if (targetVersion == null)
        {
            throw new STSException("Target CodeSystem Name: "+targetCodeSystemName+" Version Name: "+targetVersionName+" Not found.");
        }
        Version sourceVersion = VersionDelegate.get(sourceCodeSystemName, sourceVersionName);
        if (sourceVersion == null)
        {
            throw new STSException("Source CodeSystem Name: "+sourceCodeSystemName+" Version Name: "+sourceVersionName+" Not found.");
        }
        long mapSetEffectiveDate = (mapSet.getEffectiveDate() != null) ? mapSet.getEffectiveDate().getTime(): 0;
        // CCR 1263 - ensure that new map set is created when two versions in a row have no effective date
        if (mapSetEffectiveDate == 0) {
        	mapSetEffectiveDate = (mapSet.getVersion().getEffectiveDate() != null) ? mapSet.getVersion().getEffectiveDate().getTime(): 0;
        }
        long newEffectiveDate = (effectiveDate != null) ? effectiveDate.getTime() : 0;
        if (mapSet.getSourceVersionId() != sourceVersion.getId() || mapSet.getTargetVersionId() != targetVersion.getId() ||
        		(mapSetEffectiveDate != newEffectiveDate)
        		)
        {
            Version existingSourceVersion = VersionDelegate.getByVersionId(mapSet.getSourceVersionId());
            Version existingTargetVersion = VersionDelegate.getByVersionId(mapSet.getTargetVersionId());
            if (existingSourceVersion.getCodeSystem().getId() != sourceVersion.getCodeSystem().getId())
            {
                throw new STSException("Cannot change the source codesystem from: "+existingSourceVersion.getCodeSystem().getName()+" to: "+sourceVersion.getCodeSystem().getName());
            }
            if (existingTargetVersion.getCodeSystem().getId() != targetVersion.getCodeSystem().getId())
            {
                throw new STSException("Cannot change the target codesystem from: "+existingTargetVersion.getCodeSystem().getName()+" to: "+targetVersion.getCodeSystem().getName());
            }
            Version version = (mapSet.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID) ? mapSet.getVersion() : VersionDelegate.getAuthoring(); 
            result = new MapSet(mapSet.getName(), mapSet.getCode(), mapSet.getVuid(),  mapSet.getCodeSystem(),
                    version, mapSet.getActive(), sourceVersion.getId(), targetVersion.getId());
            result.setEffectiveDate(effectiveDate);
            result.setEntityId(mapSet.getEntityId());
            MapSetDao.save(result);
        }
        return result;
    }
	/**
	 * Return a list of mapsets
	 * @param vuids
	 * @return
	 */
    public static List<MapSetDesignationDTO> getByVuids(Collection<Long> vuids) 
    {
        return MapSetDao.getByVuids(vuids);
    }
    
    public static List<MapSetDesignationDTO> get(Collection<Long> mapSetEntityIds)
    {
    	return MapSetDao.get(mapSetEntityIds);
    }
	
    /**
     * Get A single mapSet
     * @param mapSetEntityId
     * @return
     */
    public static MapSet get(Long mapSetEntityId) 
    {
        return (MapSet)ConceptDao.get(mapSetEntityId);
    }
    
    public static MapSet getByVuid(long vuid, long versionId) 
    {
        return MapSetDao.getByVuid(vuid, versionId);
    }
	
    public static Version getCurrentVersionByVuid(long vuid) 
    {
        return MapSetDao.getCurrentVersionByVuid(vuid);
    }

    public static List<MapSetDesignationDTO> getAll()
	{
		return MapSetDao.getAll();
	}
	
	/**
	 * get all mapSets that have versions
	 * @return list of MapSetDesignationDTO objects
	 */
	public static List<MapSetDesignationDTO> getVersioned(boolean includeAuthoring)
	{
		return MapSetDao.getVersioned(includeAuthoring);
	}

    public static MapSetDetailListDTO getFilteredVersions(String mapSetName, Long sourceCodeSystemVuid, String sourceCodeSystemVersionName,
            Long targetCodeSystemVuid, String targetCodeSystemVersionName, Boolean mapSetStatus, List<Long> mapSetsNotAccessibleVuidList,
            Integer pageSize, Integer pageNumber)
    {
        // false = DO NOT include authoring versions (webservices excludes authoring versions)
        return MapSetDao.getFilteredVersions(mapSetName, sourceCodeSystemVuid, sourceCodeSystemVersionName, targetCodeSystemVuid,
                targetCodeSystemVersionName, mapSetStatus, mapSetsNotAccessibleVuidList, pageSize, pageNumber, false);
    }

    public static List<MapSet> getAllMapSetVersions(long mapSetEntityId, boolean includeAuthoring)
    {
    	return MapSetDao.getAllMapSetVersions(mapSetEntityId, includeAuthoring);
    }
    
	public static List<MapSetDetailDTO> getAllVersions()
	{
	    // true = include authoring versions
		return MapSetDao.getFilteredVersions(null, null, null, null, null, null, null, null, null, true).getMapSetDetails();
	}

	public static void save(MapSet mapSet)
	{
		MapSetDao.save(mapSet);
	}

	public static void deleteAuthoringVersion(long entityId) throws STSException
	{
		MapSetDao.deleteAuthoringVersion(entityId);
	}
	
    /**
     * Return a list of MapSet that references the source or target version ids
     * @param List<Long> versionIds
     * @return List<MapSet>
     */
    public static List<MapSet> getByReferencedVersionIds(List<Long> versionIds)
    {
    	// get any MapSet that references the source or target versionId
    	return MapSetDao.getByReferencedVersionIds(versionIds);
    }

	public static boolean isPreferredDesignationUnique(String mapSetPreferredName)
	{
		return MapSetDao.isPreferredDesignationUnique(mapSetPreferredName);
	}
}
