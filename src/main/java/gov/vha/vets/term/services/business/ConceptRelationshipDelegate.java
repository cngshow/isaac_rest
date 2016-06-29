package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ConceptRelationshipDao;
import gov.vha.vets.term.services.dao.DesignationDao;
import gov.vha.vets.term.services.dto.ConceptRelationshipConceptListDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipListDTO;
import gov.vha.vets.term.services.dto.RelationshipTypeListDTO;
import gov.vha.vets.term.services.dto.change.ConceptChangeDTO;
import gov.vha.vets.term.services.dto.change.RelationshipChangeDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;

public class ConceptRelationshipDelegate
{
    /**
     * Create a relationship for each new ConceptRelationship object in the List
     * 
     * @param conceptRelationships
     *            List of ConceptRelationship objects
     * @throws HibernateException
     */
    public static void create(CodeSystem codeSystem, String sourceCode, String targetCode, String relationshipType, State state) throws STSNotFoundException
    {
        long entityId = 0L;
        // look for an existing relationship in authoring
        ConceptRelationship existingRelationship = ConceptRelationshipDao.get(codeSystem, sourceCode, targetCode,
                relationshipType);
        
        if (existingRelationship != null)
        {
            entityId = existingRelationship.getEntityId();
        }
        RelationshipType type = null;
        type = ConceptRelationshipDao.getType(relationshipType);
        if (type == null)
        {
            // go ahead and create it
            type = ConceptRelationshipDao.createType(relationshipType);
        }
        CodedConcept source = CodedConceptDelegate.get(codeSystem, sourceCode);
        CodedConcept target = CodedConceptDelegate.get(codeSystem, targetCode);
        if (target == null)
        {
            throw new STSNotFoundException("Target concept not found: code system = " + codeSystem.getName() + ", code = " + targetCode);
        }
        add(VersionDelegate.getAuthoring(), entityId, source.getEntityId(), target.getEntityId(), type, true);
        ConceptStateDelegate.createOrUpdate(source.getEntityId(), state);
    }

    /**
     * Add a entry to the authoring version
     * @param entityId
     * @param sourceEntityId
     * @param targetEntityId
     * @param type
     * @param active
     * @return
     */
    protected static ConceptRelationship add(Version version, long entityId, long sourceEntityId, long targetEntityId,
            RelationshipType type, boolean active)
    {
        ConceptRelationship relationship = new ConceptRelationship();
        relationship.setEntityId(entityId);
        relationship.setSourceEntityId(sourceEntityId);
        relationship.setTargetEntityId(targetEntityId);
        relationship.setVersion(version);
        relationship.setRelationshipType(type);
        relationship.setActive(active);
        ConceptRelationshipDao.save(relationship);
        return relationship;
    }

    /**
     * Update the relationship with the information in each new
     * ConceptRelationship object in the List Gets the entityId from the
     * existing relationship before saving the updated relationship with the
     * same entityId. Updates result in a new row entry.
     * 
     * @throws STSException
     */
    public static void update(CodeSystem codeSystem, String sourceCode, String relationshipTypeName,
            String oldTargetCode, String newTargetCode, State state) throws STSNotFoundException
    {
        ConceptRelationship existingRelationship = ConceptRelationshipDao.get(codeSystem, sourceCode, oldTargetCode,
                relationshipTypeName);
        if (existingRelationship == null)
        {
            throw new STSNotFoundException("ConceptRelationship not found: sourceCode = "
                    + sourceCode + ", targetCode = "+ oldTargetCode);
        }
        CodedConcept newTargetConcept = CodedConceptDelegate.get(codeSystem, newTargetCode);
        if (newTargetConcept == null)
        {
            throw new STSNotFoundException("Target concept not found: code system = "
                    + codeSystem.getName() + ", code = " + newTargetCode);
        }
        add(VersionDelegate.getAuthoring(), existingRelationship.getEntityId(), existingRelationship.getSourceEntityId(), newTargetConcept.getEntityId(), existingRelationship.getRelationshipType(), true);
        ConceptStateDelegate.createOrUpdate(existingRelationship.getSourceEntityId(), state);            
    }

    /**
     * Inactivate a relationship
     * @param codeSystem
     * @param sourceCode
     * @param targetCode
     * @param relationshipType
     * @param active
     * @throws STSException
     */
    public static void activate(CodeSystem codeSystem, String sourceCode, String targetCode, String relationshipType, State state, boolean active)
            throws STSException
    {
        ConceptRelationship existingRelationship = ConceptRelationshipDao.get(codeSystem, sourceCode, targetCode, relationshipType);
        if (existingRelationship == null)
        {
            throw new STSNotFoundException("ConceptRelationship not found: sourceCode = "
                    + sourceCode + ", targetCode = "+ targetCode);
        }
        add(VersionDelegate.getAuthoring(), existingRelationship.getEntityId(), existingRelationship.getSourceEntityId(), existingRelationship.getTargetEntityId(), existingRelationship.getRelationshipType(), active);
        ConceptStateDelegate.createOrUpdate(existingRelationship.getSourceEntityId(), state);            
    }
    
    /**
     * inactivate the existing relationship
     * @param existingRelationship
     * @param state
     * @throws STSException
     */
    public static void activate(ConceptRelationship existingRelationship, State state, boolean active) throws STSException
	{
		add(VersionDelegate.getAuthoring(), existingRelationship.getEntityId(), existingRelationship.getSourceEntityId(), existingRelationship
						.getTargetEntityId(), existingRelationship.getRelationshipType(), active);
		ConceptStateDelegate.createOrUpdate(existingRelationship.getSourceEntityId(), state);
	}

    /**
     * Create relationship types
     * @param types
     * @throws Exception
     */
    public static void createRelationshipTypes(List<String> types) throws STSException
    {
        for (Iterator<String> it = types.iterator(); it.hasNext();)
        {
            String relationshipType = (String) it.next();

            // create the relationship type
            RelationshipType relType = ConceptRelationshipDao.getType(relationshipType);
            if (relType == null)
            {
                ConceptRelationshipDao.createType(relationshipType);
            }
        }
    }
    
    public static List<RelationshipChangeDTO> getRelationshipChanges(long conceptEntityId, boolean inverse, long versionId)
    {
        // get all relationships less than or equal to the version specified
        List<ConceptRelationship> relationships = ConceptRelationshipDao.getRelationships(conceptEntityId, inverse, versionId);
        List<ConceptRelationship> previousRelationships = ConceptRelationshipDao.getPreviousVersionRelationships(conceptEntityId, inverse, versionId);
        Map<Long, ConceptChangeDTO> conceptChangeForRelationshipsMap = new HashMap<Long, ConceptChangeDTO>();
        if (relationships.size() > 0)
        {
            conceptChangeForRelationshipsMap = getAssociatedConceptChanges(inverse, relationships, versionId);
        }
        Map<Long, ConceptChangeDTO> conceptChangeForPreviousRelationshipsMap = new HashMap<Long, ConceptChangeDTO>();
        if (previousRelationships.size() > 0)
        {
            conceptChangeForPreviousRelationshipsMap = getAssociatedConceptChanges(inverse, previousRelationships, versionId);
        }
        
        Map<Long, RelationshipChangeDTO> map = new HashMap<Long, RelationshipChangeDTO>();
        // add all the less than or equal to version relationships into the map
        for (ConceptRelationship relationship : relationships)
        {
            RelationshipChangeDTO change = new RelationshipChangeDTO();
            
            change.setVersionId(versionId);
            change.setRecent(relationship);
            ConceptChangeDTO conceptChange = conceptChangeForRelationshipsMap.get(relationship.getId());
            change.setRecentAssociatedConceptChange(conceptChange);
            map.put(relationship.getEntityId(), change);
        }
        // now go over all the previous relationships and see if they constitute a true previous version or just a duplicate
        for (ConceptRelationship relationship : previousRelationships)
        {
            RelationshipChangeDTO change = map.get(relationship.getEntityId());
            if (change != null && relationship.getVersion().getId() != change.getRecent().getVersion().getId())
            {
                change.setPrevious(relationship);
                ConceptChangeDTO conceptChange = conceptChangeForPreviousRelationshipsMap.get(relationship.getId());
                change.setPreviousAssociatedConceptChange(conceptChange);
            }
            // if previous is null and recent is inactive then we know we don't need to display anything
            if (change != null && change.getPrevious() == null && change.getRecent() != null && change.getRecent().getActive() == false)
            {
                map.remove(relationship.getEntityId());
            }
        }
        List<RelationshipChangeDTO> list = new ArrayList<RelationshipChangeDTO>();
        list.addAll(map.values());

        for (Iterator<RelationshipChangeDTO> iterator = list.iterator(); iterator.hasNext();)
        {
			RelationshipChangeDTO relationshipChangeDTO = (RelationshipChangeDTO) iterator.next();
			if (relationshipChangeDTO.getPrevious() == null && relationshipChangeDTO.getRecent() != null && relationshipChangeDTO.getRecent().getActive() == false)
			{
				iterator.remove();
			}
		}

        return list;
    }

    /**
     * Build the CodedConceptChangeDTO for the previous target
     * @param inverse
     * @param relationship
     * @return
     */
    private static Map<Long, ConceptChangeDTO> getAssociatedConceptChanges(boolean inverse, Collection<ConceptRelationship> relationships, long versionId)
    {
        Set<Long> entityIds = new HashSet<Long>();
        HashMap<Long, ConceptChangeDTO> conceptEntityIdMap = new HashMap<Long, ConceptChangeDTO>();
        HashMap<Long, ConceptChangeDTO> results = new HashMap<Long, ConceptChangeDTO>();
        
        for (ConceptRelationship relationship : relationships)
        {
            long entityId = inverse ? relationship.getSourceEntityId() : relationship.getTargetEntityId();
            entityIds.add(entityId);
        }
        List<ConceptChangeDTO> conceptChangeList = CodedConceptDelegate.getConceptChanges(entityIds, versionId);
        for (ConceptChangeDTO conceptChangeDTO : conceptChangeList)
        {
            // if it is not in the final state then it must have a change
            if (conceptChangeDTO.getConceptState() != null && conceptChangeDTO.getConceptState().getState().getType() != null)
            {
                conceptChangeDTO.setEntityOfConceptChanged(true);
            }
            conceptEntityIdMap.put(conceptChangeDTO.getRecent().getEntityId(), conceptChangeDTO);
        }
        for (ConceptRelationship relationship : relationships)
        {
            long entityId = inverse ? relationship.getSourceEntityId() : relationship.getTargetEntityId();
            ConceptChangeDTO conceptChangeDTO = conceptEntityIdMap.get(entityId);
            results.put(relationship.getId(), conceptChangeDTO);
        }
        return results;
    }

    public static void removeConceptRelationshipsInAuthoring(long conceptEntityId) throws STSException
    {
        List<ConceptRelationship> conceptRelationships = ConceptRelationshipDao.getBySourceOrTarget(conceptEntityId);
        for (ConceptRelationship relationship : conceptRelationships)
        {
            if (relationship.getVersion().getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
            {
                throw new STSException("Cannot delete a versioned relationship for concept entity: "+conceptEntityId);
            }
            // no previous version exists - go ahead and delete
            ConceptRelationshipDao.delete(relationship);
        }
    }
    
    /**
     * Remove Concept Relationships based on passed parameters
     * @param version
     * @param conceptEntityId
     * @param relationshipType
     * @throws STSException
     */
    public static void removeConceptRelationships(Version version, long conceptEntityId, String relationshipType) throws STSException
    {
    	List<ConceptRelationship> conceptRelationships = ConceptRelationshipDao.getBySource(conceptEntityId);
    	for(ConceptRelationship relationship : conceptRelationships)
    	{
    		if(relationship.getVersion().getId() == version.getId() && relationship.getRelationshipType().getName().equals(relationshipType))
    		{
    			ConceptRelationshipDao.delete(relationship);
    		}
    	}
    }
    
    /**
     * Return the relationship types that have changed
     * @param conceptEntityId
     * @param version
     * @param relationshipTypeNames
     * @param inverse
     * @return
     */
    public static List<String> getChangedRelationshipTypes(long conceptEntityId, long versionId, List<String> relationshipTypeNames, boolean inverse)
    {
        return ConceptRelationshipDao.getChangedRelationshipTypes(conceptEntityId, versionId, relationshipTypeNames, inverse);
    }
    /**
     * get the target concepts preferred designation for the given relationship types 
     * @param conceptEntityId
     * @param relationshipTypeNames
     * @return
     */
    public static Map<String, List<Object>> getTargetDesignations(long conceptEntityId, List<String> relationshipTypeNames, long versionId, boolean inverse)
    {
        return ConceptRelationshipDao.getTargetDesignations(conceptEntityId, relationshipTypeNames, versionId, inverse);
    }

    /**
     * get detail relationship information for a concept
     * 
     * @param conceptEntityId
     * @return
     */
    public static List<ConceptRelationshipDTO> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        return ConceptRelationshipDao.getAllVersions(conceptEntityId, includeAuthoring);
    }

    /**
     * get all children concepts
     * @param conceptEntityId
     * @return
     */
    public static List<ConceptRelationshipDTO> getAllChildrenVersions(long conceptEntityId, boolean includeAuthoring)
    {
        return ConceptRelationshipDao.getAllChildrenVersions(conceptEntityId, includeAuthoring);
    }
    /**
     * Update all the relationship that belong to the list of concept to be
     * versioned
     * 
     * @param conceptEntityIdList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return ConceptRelationshipDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    /**
     * Get all concept for properties that have changed for particular concept relationship
     * @param version
     * @return
     */
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return ConceptRelationshipDao.getConceptEntityIdsByVersionId(versionId);
    }


    /**
     * Get a list of relationships for a given list of relationships
     * @param conceptEntityIds that are the source of the relationship
     * @return
     */
    public static List<ConceptRelationship> getRelationships(Collection<Long> conceptEntityIds)
    {
        return getRelationships(conceptEntityIds, null, null, false);
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Collection<ConceptRelationshipDTO>> getTargetConcepts(Collection<Long> conceptEntityIds) 
    {
        return ConceptRelationshipDao.getTargetConcepts(conceptEntityIds);
    }

    /**
     * Get a list of relationships for a given list of source entities that relate to the target entities
     * @param conceptEntityIds source of the relationship
     * @param possibleTargetIds target of the relationship
     * @return
     */
    public static List<ConceptRelationship> getRelationships(Collection<Long> conceptEntityIds, Collection<Long> possibleTargetIds)
    {
        return getRelationships(conceptEntityIds, null, possibleTargetIds, false);
    }
    
    /**
     * Get a the relationships for a concept
     * @param conceptEntityIds
     * @param relationshipTypeName - filter based on type name
     */
    public static List<ConceptRelationship> getRelationships(long conceptEntityId, String relationshipTypeName)
    {
    	List<Long> conceptEntityIds = new ArrayList<Long>();
    	conceptEntityIds.add(conceptEntityId);
    	
    	List<String> relationshipTypeNames = new ArrayList<String>();
    	relationshipTypeNames.add(relationshipTypeName);
    	
        return getRelationships(conceptEntityIds, relationshipTypeNames, null, false);
    }

    /**
     * Get a the relationships for a concept
     * @param conceptEntityIds
     * @param relationshipTypeNames - filter based on type name
     * @param inverse - false for forward relationship true for inverse
     * @return a list of conceptRelationships
     */
    public static List<ConceptRelationship> getRelationships(Collection<Long> conceptEntityIds, List<String> relationshipTypeNames, Collection<Long> possibleConceptIds, boolean inverse)
    {
        return ConceptRelationshipDao.getRelationships(conceptEntityIds, relationshipTypeNames, possibleConceptIds, inverse, false);
    }

    public static List<ConceptRelationship> getRelationships(Collection<Long> sourceConceptEntityIds, List<String> relationshipTypeNames, Collection<Long> targetConceptEntityIds, boolean inverse, boolean includeInactives)
    {
        return ConceptRelationshipDao.getRelationships(sourceConceptEntityIds, relationshipTypeNames, targetConceptEntityIds, inverse, includeInactives);
    }
    
    public static ConceptRelationship create(Version version, long sourceEntityId, long targetEntityId, String relationshipTypeName)
    {
        RelationshipType type = ConceptRelationshipDao.getType(relationshipTypeName);
        if (type == null)
        {
            // go ahead and create it
            type = ConceptRelationshipDao.createType(relationshipTypeName);
        }
        return add(version, 0L, sourceEntityId, targetEntityId, type, true);
    }
    
    public static void delete(ConceptRelationship conceptRelationship)
    {
    	ConceptRelationshipDao.delete(conceptRelationship);
    }
    
    public static RelationshipType getType(String relationshipTypeName)
    {
        return ConceptRelationshipDao.getType(relationshipTypeName);
    }
    
    public static RelationshipType getType(long id)
    {
        return ConceptRelationshipDao.getType(id);
    }
    
    public static RelationshipTypeListDTO getAllTypes(String relationshipTypeName, CodeSystem codeSystem, Version version, Integer pageSize, Integer pageNumber)
    {
        return ConceptRelationshipDao.getAllTypes(relationshipTypeName, codeSystem, version, pageSize,pageNumber);
    }
    
    public static List<String> getTypesByCodeSystem(String codeSystemName)
    {
        return ConceptRelationshipDao.getTypesByCodeSystem(codeSystemName);
    }

    public static RelationshipType createType(String relationshipTypeName)
    {
        return ConceptRelationshipDao.createType(relationshipTypeName);
    }

    public static ConceptRelationship createSDO(Version version, Long sourceConceptEntityId, RelationshipType relationshipType, long targetConceptEntityId)
    {
        ConceptRelationship conceptRelationship = new ConceptRelationship(sourceConceptEntityId, targetConceptEntityId, version, true);
        conceptRelationship.setRelationshipType(relationshipType);
        ConceptRelationshipDao.save(conceptRelationship);
        return conceptRelationship;
    }

    public static ConceptRelationship updateSDO(Version version, ConceptRelationship relationship, long newTargetConceptEntityId, boolean active)
    {
        ConceptRelationship conceptRelationship = new ConceptRelationship(relationship.getSourceEntityId(), newTargetConceptEntityId, version, active);
        conceptRelationship.setEntityId(relationship.getEntityId());
        conceptRelationship.setRelationshipType(relationship.getRelationshipType());
        ConceptRelationshipDao.save(conceptRelationship);
        return conceptRelationship;
    }
    public static ConceptRelationship getPreviousRelationship(long entityId, long versionId)
    {
        return ConceptRelationshipDao.getPreviousRelationship(entityId, versionId);
    }
    
    public static List<ConceptRelationship> getRelationships(long conceptEntityId)
    {
        return ConceptRelationshipDao.getRelationships(conceptEntityId, false, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
    public static List<ConceptRelationship> getRelationships(long conceptEntityId, long versionId)
    {
        return ConceptRelationshipDao.getRelationships(conceptEntityId, false, versionId);
    }

    public static boolean isConceptSubsumedRelationship(Version version, long sourceConceptEntityId, long targetConceptEntityId, String relationshipTypeName)
    {
        return ConceptRelationshipDao.isConceptSubsumedRelationship(version, sourceConceptEntityId, targetConceptEntityId, relationshipTypeName);
    }
    
    public static Collection<String> getPath(long codeSystemId, long versionId, String sourceCode, String targetCode, long relationshipTypeId)
    {
        return ConceptRelationshipDao.getPath(codeSystemId, versionId, sourceCode, targetCode, relationshipTypeId);
    }
    
    // this is for the RelationshipDetail web service 
    public static ConceptRelationship get(Long entityId) 
    {
    	return ConceptRelationshipDao.get(entityId);
    }
    
    public static ConceptRelationshipListDTO getRelationships(CodeSystem codeSystem, Version version, String sourceConceptCode, 
								String targetConceptCode, String relationshipTypeName, Integer pageSize, Integer pageNumber) 
    {
    	return ConceptRelationshipDao.getRelationships(codeSystem, version, sourceConceptCode, targetConceptCode, relationshipTypeName, pageSize, pageNumber );
    }
    
    
    public static ConceptRelationshipConceptListDTO getChildren(String parentName, String childName,
            Integer pageSize, Integer pageNumber)
    {
        return ConceptRelationshipDao.getChildren(parentName, childName, pageSize, pageNumber);
    }

	public static Map<Long, Map<String, Set<String>>> getRelationshipsForCodeSystem(long versionId)
	{
		return ConceptRelationshipDao.getRelationshipsForCodeSystem(versionId);
		
	}
}
