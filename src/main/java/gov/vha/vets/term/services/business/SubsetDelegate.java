package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.SubsetDao;
import gov.vha.vets.term.services.dto.SubsetDetailDTO;
import gov.vha.vets.term.services.dto.SubsetPublishDetailDTO;
import gov.vha.vets.term.services.dto.SubsetRelationshipDTO;
import gov.vha.vets.term.services.dto.api.SubsetListViewDTO;
import gov.vha.vets.term.services.dto.change.SubsetChangeDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubsetDelegate
{

    /**
     * Update the concept in the VTS database
     * 
     * @param oldName
     * @param newName
     * @throws Exception 
     * @throws Exception
     */
    public static void update(String code, String newName, boolean active) throws STSException
    {
        // get the authoring version if available in VTS
        Subset existingSubset = SubsetDao.getByCode(code);
        if (existingSubset == null)
        {
            throw new STSNotFoundException("Subset code: " + code + " not found while trying to update a subset.");
        }
        addEntity(VersionDelegate.getAuthoring(),existingSubset.getEntityId(), code, newName, existingSubset.getVuid(), active);
    }

    /**
     * Get the list of subsets for the given version
     * 
     * @param version
     * @return List List of Subsets
     * @throws STSNotFoundException
     */
    public static List<Subset> getSubsets(Set<Long> vuids) throws STSNotFoundException
    {
        return SubsetDao.getSubsets(vuids);
    }
    /**
     * Get the highest available version of the named subset
     * 
     * @param name
     *            Name of the Subset
     * @return Subset
     * @throws STSNotFoundException
     */
    public static Subset getByName(String name)
    {
        return SubsetDao.getByName(name);
    }
    
    public static Subset getByCode(String code) 
    {
        return SubsetDao.getByCode(code);
    }

    /**
     * Create a new subset. This method assigns the Id to be the Entity Id of
     * the new concept.
     * 
     * @param newName
     *            New Subset name
     * @return Subset
     */
    public static Subset create(String code, String newName, Long vuid, boolean status) throws STSException
    {
        // check for valid vuid
        if (vuid < 1)
        {
            throw new STSException("Subset code: "+code+" subset name: "+newName+" has invalid vuid of: "+vuid);
        }

        return addEntity(VersionDelegate.getAuthoring(), 0L, code, newName, vuid, status);
    }

    /**
     * Convenience method for JUnit testing. Create a new subset using a
     * partially complete Subset object.
     * 
     * @param subset
     *            Subset
     * @throws Exception
     */
    protected static Subset create(Subset subset) throws Exception
    {
        SubsetDao.save(subset);

        return subset;
    }

    /**
     * Physical delete of the subset from the database
     * 
     * @param name
     */
    public static void inactivate(String code) throws STSException
    {
        Subset subset = SubsetDao.getByCode(code);
        if (subset == null)
        {
            throw new STSNotFoundException("Subset code: " + code + " not found while trying to inactivate a subset.");
        }
        addEntity(VersionDelegate.getAuthoring(),subset.getEntityId(), code, subset.getName(), subset.getVuid(), false);
    }

    private static Subset addEntity(Version version, Long entityId, String code, String name, Long vuid, boolean status) 
    {
        // the subset is in the real version pool
        // add new inactive subset to Authoring Version pool
        Subset authorSubset = new Subset();
        authorSubset.setActive(status);
        if (entityId != null)
        {
        authorSubset.setEntityId(entityId);
        }
        authorSubset.setCode(code);
        authorSubset.setName(name);
        authorSubset.setVersion(version);
        authorSubset.setVuid(vuid);
        SubsetDao.save(authorSubset);
        return authorSubset;
    }

    /**
     * Update all subset relationshihp entries that belong to the list of
     * concepts versioned
     * 
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return SubsetDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    /**
     * @param conceptEntityId
     * @return
     */
    public static List<SubsetRelationshipDTO> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        return SubsetDao.getAllVersions(conceptEntityId, includeAuthoring);
    }

    /**
     * @param conceptEntityId
     * @return
     */
    public static List<SubsetDetailDTO> getSubsetDetail(long conceptEntityId)
    {
        return SubsetDao.getSubsetDetail(conceptEntityId);
    }

    public static List<SubsetPublishDetailDTO> getSubsetPublishDetail(Collection<Long> conceptEntityIds, long versionId)
    {
        return SubsetDao.getSubsetPublishDetail(conceptEntityIds, versionId);
    }

    /**
     * Get the subsetDetail information for dependents subsets.  ie. Vital Types which has inverse relationships
     * @param conceptEntityId
     * @param relationshipTypeNames
     * @param versionId
     * @return
     */
    public static List<SubsetPublishDetailDTO> getSubsetPublishDetailForTargets(Collection<Long> conceptEntityIds, List<String> subsetNames, List<String> relationshipTypeNames, long versionId) 
    {
        return SubsetDao.getSubsetPublishDetailForTargets(conceptEntityIds, subsetNames, relationshipTypeNames, versionId);
    }
    
    public static List<Subset> getSubsets(long entityId)
    {
        return SubsetDao.getSubsets(entityId);
    }

    public static Map<String, List<Long>> getSubsets(List<Long> conceptEntityIds, long versionId)
    {
        return SubsetDao.getSubsets(conceptEntityIds, versionId);
    }

    public static Map<Long, List<Subset>> getSubsetsByDesignationEntityIds(List<Long> designationEntityIds, long versionId)
    {
        return SubsetDao.getSubsetsByDesignationEntityIds(designationEntityIds, versionId);
    }

    public static SubsetListViewDTO getSubsetsByNameFilter(String filter, String status, Integer pageSize, Integer pageNumber)
    {
        return SubsetDao.getByFilter(filter, status, pageSize, pageNumber);
    }

    /**
     * get subset changes
     * @param subsetEntityId
     * @return
     */
    public static SubsetChangeDTO getSubsetChange(long subsetEntityId, long versionId)
    {
        SubsetChangeDTO change = new SubsetChangeDTO();
        Subset subset = SubsetDao.get(subsetEntityId);
        if (subset != null)
        {
            change.setRecent(subset);
            if (subset.getVersion().getId() == versionId)
            {
                Subset subsetPrevious = SubsetDao.getPreviousVersion(subset.getCode(), versionId);
                change.setPrevious(subsetPrevious);
            }
        }
        
        return change;
    }
    
    public static Subset getSubset(long entityId)
    {
        Subset subset = SubsetDao.get(entityId);
    	
        return subset;
    }
    
    public static List<SubsetChangeDTO> getSubsetChanges(long conceptEntityId)
    {
        List<Subset> authoringSubsets = SubsetDao.getSubsets(conceptEntityId, VersionDelegate.getAuthoring());
        List<Subset> versionedSubsets = SubsetDao.getVersionedSubsets(conceptEntityId);
        Map<Long, SubsetChangeDTO> map = new HashMap<Long, SubsetChangeDTO>();
        for (Subset subset : authoringSubsets)
        {
            if (subset.getVersion().getName().equals(HibernateSessionFactory.AUTHORING_VERSION_NAME))
            {
                SubsetChangeDTO change = new SubsetChangeDTO();
                change.setRecent(subset);
                map.put(subset.getEntityId(), change);
            }
        }
        for (Subset subset : versionedSubsets)
        {
            SubsetChangeDTO change = map.get(subset.getEntityId());
            if (change == null)
            {
                change = new SubsetChangeDTO();
                change.setRecent(subset);
                map.put(subset.getEntityId(), change);
            }
            else
            {
                change.setPrevious(subset);
            }
        }
        List<SubsetChangeDTO> list = new ArrayList<SubsetChangeDTO>();
        list.addAll(map.values());
        return list;
    }

	public static Subset create(String subsetName, Version version, boolean active)
	{
	    return addEntity(version, null, null, subsetName, null, active);
	}
	
    /**
     * Get a list of Subsets that have been finalized
     * @return List<Subset>
     */
	public static List<Subset> getFinalizedSubsets()
	{
		return SubsetDao.getFinalizedSubsets();
	}

	/**
	 * Get a list of Subsets from a list of subset names
	 * @param subsetNames
	 * @return List<Subset>
	 */
	public static List<Subset> getSubsetsByNames(List<String> subsetNames)
	{
		return SubsetDao.getSubsetsByNames(subsetNames);
	}

    public static Subset getByVuid(long vuid)
    {
        return SubsetDao.getByVuid(vuid);
    }
    
    public static Subset getByVuid(long vuid, String versionName)
    {
        return SubsetDao.getByVuid(vuid, versionName);
    }
    
    public static boolean isSubsetMember(Long subsetEntityId, Long designationEntityId, Version version)
    {
    	return SubsetDao.isSubsetMember(subsetEntityId, designationEntityId, version);
    }

    public static void update(Long vuid, boolean active) throws STSNotFoundException
    {
        // get the authoring version if available in VTS
        Subset existingSubset = SubsetDao.getByVuid(vuid);
        if (existingSubset == null)
        {
            throw new STSNotFoundException("Subset vuid: " + vuid + " not found while trying to update a subset.");
        }
        addEntity(VersionDelegate.getAuthoring(),existingSubset.getEntityId(), existingSubset.getCode(), existingSubset.getName(), existingSubset.getVuid(), active);
    }
    public static List<Subset> getAll() 
    {
        return SubsetDao.getAll();
    }

	public static List<Subset> getDomainSubsets(Long domainVuid, int pageNumber, int pageSize)
	{
		return SubsetDao.getSubsetsByDomain(domainVuid, pageNumber, pageSize);
	}

    public static Map<Long, Collection<String>> getVersions(Collection<Long> subsetEntityIds, boolean includeAuthoring)
    {
        return SubsetDao.getVersions(subsetEntityIds, includeAuthoring);
    }
}
