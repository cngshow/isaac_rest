package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.SubsetRelationshipDao;
import gov.vha.vets.term.services.dto.SubsetCountDTO;
import gov.vha.vets.term.services.dto.change.SubsetChangeDTO;
import gov.vha.vets.term.services.dto.change.SubsetRelationshipChangeDTO;
import gov.vha.vets.term.services.exception.STSDuplicateException;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSInvalidValueException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubsetRelationshipDelegate
{
    /**
     * Create a new entry in the subset for the designation
     * @param codeSystem
     * @param subsetName
     * @param conceptCode
     * @param designationName
     * @param designationTypeName
     * @throws STSDuplicateException
     * @throws STSNotFoundException
     */
    public static void create(CodeSystem codeSystem, String subsetName, String conceptCode, String designationCode, State state)
            throws STSException
    {
        // look for an existing relationship in authoring and throws STSNotFoundException if it is not there
        Designation designation = DesignationDelegate.get(codeSystem, designationCode);
        SubsetRelationship existingRelationship = SubsetRelationshipDao.get(subsetName, designation.getEntityId());
        Long entityId = null;
        Long sourceEntityId = null;
        Long targetEntityId = null;
        if (existingRelationship != null )
        {
            entityId = existingRelationship.getEntityId();
            sourceEntityId = existingRelationship.getSourceEntityId();
            targetEntityId = existingRelationship.getTargetEntityId();
        }
        else
        {
            // if the existing relationship is null then add a record 
            Subset subset = SubsetDelegate.getByName(subsetName);
            Designation target = DesignationDelegate.get(codeSystem, designationCode);
            sourceEntityId = subset.getEntityId();
            targetEntityId = target.getEntityId();
        }
        addEntity(VersionDelegate.getAuthoring(), entityId, sourceEntityId, targetEntityId, true);
        ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);
    }

    /**
     * Add a entry to the authoring version
     * @param entityId
     * @param sourceEntityId
     * @param targetEntityId
     * @param type
     * @param active
     * @return
     * @throws STSNotFoundException
     */
    protected static SubsetRelationship addEntity(Version version, Long entityId, long sourceEntityId, long targetEntityId, boolean active) throws STSNotFoundException
    {
        SubsetRelationship relationship = new SubsetRelationship();
        if (entityId != null)
        {
            relationship.setEntityId(entityId);
        }
        relationship.setSourceEntityId(sourceEntityId);
        relationship.setTargetEntityId(targetEntityId);
        relationship.setVersion(version);
        relationship.setActive(active);
        SubsetRelationshipDao.save(relationship);
        return relationship;
    }


    /**
     * Inactivate a relationship
     * @param codeSystem
     * @param sourceCode
     * @param targetCode
     * @param relationshipType
     * @throws STSNotFoundException 
     * @throws Exception
     */
    public static void inactivate(CodeSystem codeSystem, String subsetName, String conceptCode, String designationCode, State state) 
        throws STSNotFoundException, STSDuplicateException, STSInvalidValueException
    {
        Designation designation = DesignationDelegate.get( codeSystem, designationCode);
        SubsetRelationship existingRelationship = SubsetRelationshipDao.get(subsetName, designation.getEntityId());
        if (existingRelationship == null)
        {
            throw new STSNotFoundException("Cannot find existing subset "+subsetName+" designationEntity "+designation.getEntityId());
        }
        addEntity(VersionDelegate.getAuthoring(), existingRelationship.getEntityId(), existingRelationship.getSourceEntityId(),
                existingRelationship.getTargetEntityId(), false);
        ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);
    }

    /**
     * Update all subset relationshihp entries that belong to the list of concepts
     * versioned
     * 
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return SubsetRelationshipDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    public static SubsetRelationship get(String subsetName, CodeSystem codeSystem, String designationCode) throws STSNotFoundException, STSDuplicateException
    {
        Designation designation = DesignationDelegate.get(codeSystem, designationCode);
        return SubsetRelationshipDao.get(subsetName, designation.getEntityId());
    }
    public static List<SubsetRelationship> get(String subsetName) throws STSNotFoundException
    {
        return SubsetRelationshipDao.get(subsetName);
    }
    
    public static List<SubsetRelationship> get(Set<Long> subsetEntityIds, Set<Long> designationEntityIds) throws STSNotFoundException
    {
        return SubsetRelationshipDao.get(subsetEntityIds, designationEntityIds);
    }
    public static Map<Long,SubsetRelationship> getVersioned(List<Long> subsetRelationshipEntityIds)
    {
        return SubsetRelationshipDao.getVersioned(subsetRelationshipEntityIds);
    }

    /**
     * Get all concept for properties that have changed for particular subset relationship
     * @param version
     * @return
     */
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return SubsetRelationshipDao.getConceptEntityIdsByVersionId(versionId);
    }
    
    /**
     * Return all subset relationship changes for a given designationEntityId and which are in the authoring version
     * @param designationEntityId
     * @return
     */
    public static List<SubsetRelationshipChangeDTO> getSubsetRelationshipChanges(long designationEntityId)
    {
        return getSubsetRelationshipChanges(designationEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }

    /**
     * Return all subset relationship changes for a given designationEntityId and versionId
     * @param designationEntityId
     * @return
     */
    public static List<SubsetRelationshipChangeDTO> getSubsetRelationshipChanges(long designationEntityId, long versionId)
    {
        List<SubsetRelationship> subsetRelationships = SubsetRelationshipDao.get(designationEntityId, versionId);
        List<SubsetRelationship> previousSubsetRelationships = SubsetRelationshipDao.getPreviousVersionSubsetRelationships(designationEntityId, versionId);
        Map<Long, SubsetRelationshipChangeDTO> map = new HashMap<Long, SubsetRelationshipChangeDTO>();
        for (SubsetRelationship relationship : subsetRelationships)
        {
            if (relationship.getVersion().getId() == versionId)
            {
                SubsetRelationshipChangeDTO change = new SubsetRelationshipChangeDTO();
                change.setRecent(relationship);
                change.setVersionId(versionId);
                SubsetChangeDTO subsetChange = getAssociatedSubsetChange(relationship, versionId);
                change.setRecentAssociatedSubsetChange(subsetChange);
                map.put(relationship.getEntityId(), change);
            }
        }
        for (SubsetRelationship relationship : previousSubsetRelationships)
        {
            SubsetRelationshipChangeDTO change = map.get(relationship.getEntityId());
            if (change == null)
            {
                change = new SubsetRelationshipChangeDTO();
                change.setRecent(relationship);
                change.setVersionId(versionId);
                SubsetChangeDTO subsetChange = getAssociatedSubsetChange(relationship, versionId);
                change.setRecentAssociatedSubsetChange(subsetChange);
                map.put(relationship.getEntityId(), change);
            }
            else
            {
                change.setPrevious(relationship);
                SubsetChangeDTO subsetChange = getAssociatedSubsetChange(relationship, versionId);
                change.setPreviousAssociatedSubsetChange(subsetChange);
            }
        }
        
        List<SubsetRelationshipChangeDTO> list = new ArrayList<SubsetRelationshipChangeDTO>();
        list.addAll(map.values());

        return list;
    }

    /**
     * Build the SubsetChangeDTO for the previous target
     * @param subsetRelationship
     * @return
     */
    private static SubsetChangeDTO getAssociatedSubsetChange(SubsetRelationship relationship, long versionId)
    {
        long entityId = relationship.getSourceEntityId();
        SubsetChangeDTO subsetChange = SubsetDelegate.getSubsetChange(entityId, versionId);
        // if it is not in the final state then it must have a change
//        if (subsetChange.getSubsetState() != null && subsetChange.getConceptState().getState().getType() != null)
//        {
//            subsetChange.setEntityOfConceptChanged(true);
//        }
        return subsetChange;
    }

	public static SubsetRelationship create(Version version, String subsetName, Designation designation) throws STSException
	{
        // look for an existing relationship in authoring and throws STSNotFoundException if it is not there
        SubsetRelationship relationship = SubsetRelationshipDao.get(subsetName, designation.getEntityId());
        if (relationship == null )
        {
            // if the existing relationship is null then add a record 
            Subset subset = SubsetDelegate.getByName(subsetName);
            if (subset == null)
            {
                throw new STSException("Subset: "+subsetName+" does not exist!");
            }
            relationship = addEntity(version, 0L, subset.getEntityId(), designation.getEntityId(), true);
        }
        else
        {
        	throw new STSException("Error creating a Subset Relation that already exists. Subset name: " + subsetName + ", Designation name: " + designation.getName());
        }
        
        return relationship;
	}

	public static SubsetRelationship get(String subsetName, long designationEntityId) throws STSNotFoundException
	{
		return SubsetRelationshipDao.get(subsetName, designationEntityId);
	}

    public static void createSDO(Version version, long subsetEntityId, long designationEntityId, boolean active) throws STSNotFoundException
    {
        addEntity(version, 0L, subsetEntityId, designationEntityId, active);
    }
    /**
     * Check to see if we need to delete a subsetrelationship becuase of a designation or concept deletion
     * @param entityId
     * @throws STSException 
     */
    public static void removeSubsetRelationships(long conceptEntityId) throws STSException
    {
        List<SubsetRelationship> subsetRelationships = SubsetRelationshipDao.get(conceptEntityId);
        for (SubsetRelationship relationship : subsetRelationships)
        {
            SubsetRelationshipDao.delete(relationship.getEntityId());
        }
    }

    public static void updateSDO(Version currentVersion, long subsetRelationshipEntityId, long sourceEntityId, long targetEntityId, boolean active) throws STSNotFoundException
    {
        addEntity(currentVersion, subsetRelationshipEntityId, sourceEntityId, targetEntityId, active);
    }

    public static void remove(SubsetRelationship relationship) throws STSException
    {
        SubsetRelationship versionedSubsetRelationship = SubsetRelationshipDao.getVersioned(relationship.getEntityId());
        if (versionedSubsetRelationship != null)
        {
            throw new STSException("Cannot remove subset relationship that is already versioned");
        }
        SubsetRelationshipDao.delete(relationship.getEntityId());
    }

    public static SubsetCountDTO getCount(long subsetEntityId, String versionName) 
    {
        return SubsetRelationshipDao.getCount(subsetEntityId, versionName);
    }
    
    public static Map<Long, Collection<SubsetRelationship>> get(Collection<Long> subsetEntityIds, Collection<Long> designationEntityIds) throws STSNotFoundException
    {
        Map<Long, Collection<SubsetRelationship>> results = new HashMap<Long, Collection<SubsetRelationship>>();
        if (!subsetEntityIds.isEmpty() && !designationEntityIds.isEmpty())
        {
            Collection<SubsetRelationship> subsetRelationships = SubsetRelationshipDao.get(subsetEntityIds, designationEntityIds);
            
            for (SubsetRelationship subsetRelationship : subsetRelationships)
            {
                // look for the designationEntityId
                Collection<SubsetRelationship> subsets = results.get(subsetRelationship.getTargetEntityId());
                if (subsets == null)
                {
                    subsets = new ArrayList<SubsetRelationship>();
                    results.put(subsetRelationship.getTargetEntityId(), subsets);
                }
                subsets.add(subsetRelationship);
            }
        }
        return results;
    }
}
