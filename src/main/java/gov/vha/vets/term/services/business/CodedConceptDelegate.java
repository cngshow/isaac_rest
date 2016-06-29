/**
 * 
 */
package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.CodedConceptDao;
import gov.vha.vets.term.services.dao.ConceptDao;
import gov.vha.vets.term.services.dto.CodedConceptDesignationDTO;
import gov.vha.vets.term.services.dto.ConceptHierarchyDTO;
import gov.vha.vets.term.services.dto.api.CodedConceptListViewDTO;
import gov.vha.vets.term.services.dto.api.TotalEntityListView;
import gov.vha.vets.term.services.dto.change.ConceptChangeDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author vhaislchevaj
 *
 */
public class CodedConceptDelegate
{
	public static void update(CodeSystem codeSystem, String code,
			String oldName, String newName, boolean oldActive, boolean newActive, Long oldVuid, Long newVuid, State state) throws STSException
	{
      	//get the authoring version if available in VTS
    	Concept existingConcept = ConceptDao.get(codeSystem, code);
    	if (existingConcept == null)
    	{
            throw new STSNotFoundException("Concept with code="+code+" CodeSystem:"+codeSystem.getName()+" could not be found");
    	}
        Concept versionedConcept = existingConcept.getVersion().getId() != HibernateSessionFactory.AUTHORING_VERSION_ID ? existingConcept : ConceptDao.getVersioned(codeSystem, code);
        if (versionedConcept != null && newVuid.longValue() != versionedConcept.getVuid().longValue())
        {
            throw new STSException("Concept with code: "+code+" cannot change vuid: "+oldVuid+" to new vuid: "+newVuid+" because it is already versioned.");
        }
            
                        //and the name of the concept in authoring is different from TDE
    	addEntity(VersionDelegate.getAuthoring(), existingConcept.getEntityId(), newName, code, newVuid, newActive);
        ConceptStateDelegate.createOrUpdate(existingConcept.getEntityId(), state);
	}
    /**
     * Change the status of the concept 
     * @param concept
     * @throws STSException 
     */
    public static Concept updateSDO(Version version, Concept concept, String name, String code, boolean active) throws STSException
    {
        if (version.getCodeSystem() == null || version.getCodeSystem().getName() == null)
        {
            throw new STSUpdateException("CodeSystem name must be specified for code:"+code);
        }
        if (concept != null)
        {
            addEntity(version, concept.getEntityId(), name, concept.getCode(), concept.getVuid(), active);
        }
        else
        {
            throw new STSUpdateException("Cannot update concept Code: "+code+ " for codeSystem: "+version.getCodeSystem().getName()+" because concept is not found");
        }
        return concept;
    }
    
	private static void addEntity(Version version, Long entityId, String name, String code,
            Long vuid, boolean status) throws STSException 
    {
        CodedConcept concept = new CodedConcept();
        concept.setActive(status);
        concept.setCode(code);
        concept.setCodeSystem(version.getCodeSystem());
        if (entityId != null)
        {
            concept.setEntityId(entityId);
        }
        concept.setName(name);
        concept.setVersion(version); 
        if (vuid != null)
        {
            concept.setVuid(vuid);
        }
        ConceptDao.save(concept);
    }
	
    /**
     * Physical delete of the concept from the database
     * @param concept
     * @throws STSException 
     * @throws Exception 
     */
    public static void inactivate(CodeSystem codeSystem, String code, State state) throws STSException
    {
        remove(codeSystem, code);
    }
    
    public static CodedConcept remove(CodeSystem codeSystem, String code) throws STSException
    {
        if (codeSystem == null || codeSystem.getName() == null)
        {
            throw new STSUpdateException("CodeSystem name must be specified for code: "+code);
        }
        CodedConcept concept = (CodedConcept) ConceptDao.getVersioned(codeSystem, code);
        // nothing has been versioned so we can delete it
        if (concept == null)
        {
            if (MapEntryDelegate.isConceptCodeInMapEntry(codeSystem, code))
            {
                throw new STSUpdateException("Concept code: "+code+" in the code system: "+codeSystem.getName()+" is participating in one more map entries");
            }
            concept = (CodedConcept) ConceptDao.get(codeSystem, code);
            removeOrphanEntities(concept.getEntityId());
            ConceptDao.delete(concept);
        }
        else
        {
            throw new STSUpdateException(concept.getName() + " cannot be deleted because it is in version " +
                    concept.getVersion().getName() );
        }
        return concept;
    }
    
    /**
     * Remove any child entities that are in authoring
     * @param conceptEntityId
     * @throws STSException
     */
    public static void removeOrphanEntities(long conceptEntityId) throws STSException
    {
        PropertyDelegate.removeProperties(conceptEntityId);
        ConceptRelationshipDelegate.removeConceptRelationshipsInAuthoring(conceptEntityId);
        DesignationDelegate.removeDesignations(conceptEntityId);
    }
	
    /**
	 * Get the list of concepts for a version but we are really only interested in the version name
	 * @param version
	 * @return List List of CodedConcepts
	 */
	public static List<CodedConcept> getCodedConcepts(CodeSystem codeSystem, Version version)
	{
		return CodedConceptDao.getCodedConcepts(codeSystem, version);
	}

    /**
     * Get the list of concepts for a version but we are really only interested in the version name
     * @param version
     * @return List List of CodedConcepts
     */
    public static CodedConceptListViewDTO getCodedConcepts(CodeSystem codeSystem, Version version, String designationNameFilter,
            String conceptCodeFilter, Boolean conceptStatusFilter, Integer pageSize, Integer pageNumber)
    {
        return CodedConceptDao.getCodedConcepts(codeSystem, version, designationNameFilter, conceptCodeFilter,
                conceptStatusFilter, pageSize, pageNumber);
    }

	/**
	 * Get a specific concept for a specific version.
	 * @param codeSystem
	 * @param code
	 * @param version
	 * @return CodedConcept
	 * @throws STSNotFoundException 
	 */
	public static CodedConcept get(CodeSystem codeSystem, String code) 
	{
		return (CodedConcept) ConceptDao.get(codeSystem, code);
	}


	public static CodedConcept get(Version version, String code)
    {

	    return CodedConceptDao.get(version, code);
    }
    /**
     * Return the codedconcept by entityId
     * @param conceptEntityId
     * @return
     */
    public static List<CodedConcept> get(Collection<Long> conceptEntityIds)
    {
        return CodedConceptDao.get(conceptEntityIds);
    }

 
    /**
     * This method is used when creating a SDO version
     * @param codeSystem - must use a complete object
     * @param newCode
     * @param newName
     * @param vuid
     * @param active
     * @param version - Must use a complete object
     * @return
     * @throws STSException 
     * @throws STSException 
     */
    public static CodedConcept createSDO(Version version, String code, String name, Long vuid, boolean active) throws STSException 
    {
        CodedConcept codedConcept = new CodedConcept();
        codedConcept.setActive(active);
        codedConcept.setCode(code);
        codedConcept.setCodeSystem(version.getCodeSystem());
        codedConcept.setName(name);
        codedConcept.setVuid(vuid);
        codedConcept.setVersion(version);
        
        ConceptDao.save(codedConcept);
        
        return codedConcept;
    }
	
    /**
     * Create a new concept.  This method assigns the Id to be the Entity Id of the new concept.
     * @param codeSystem
     * @param concept
     * @param version
     * @return
     * @throws STSException 
     */
    public static CodedConcept create(String newCode, String newName, long vuid, boolean active, State state) throws STSException 
    {
        // check to make sure we don't create a concept with an invalid VUID
        if (vuid < 1)
        {
            throw new STSException("Concept code: "+newCode+" name: "+newName+" has invalid vuid of: "+vuid);
        }
        //Case 2
    	CodedConcept codedConcept = new CodedConcept();
    	codedConcept.setActive(active);
        codedConcept.setVuid(vuid);
    	codedConcept.setCode(newCode);
        Version version = VersionDelegate.getAuthoring();
        if (version == null)
        {
            throw new STSNotFoundException("Cannot find authoring version");
        }
    	codedConcept.setCodeSystem(CodeSystemDelegate.get(version.getCodeSystem().getName()));
    	codedConcept.setName(newName);
    	codedConcept.setVersion(version);
    	
        ConceptDao.save(codedConcept);
        ConceptStateDelegate.createOrUpdate(codedConcept.getEntityId(), state);
        
        return codedConcept;
    }
    

	public static CodedConcept create(Version version, String newName, long vuid, String code, boolean active) 
		throws STSException
	{
    	CodedConcept codedConcept = new CodedConcept();
    	codedConcept.setActive(active);
        codedConcept.setVuid(vuid);
        codedConcept.setCode(code);
    	codedConcept.setCodeSystem(CodeSystemDelegate.get(version.getCodeSystem().getName()));
    	codedConcept.setName(newName);
    	codedConcept.setVersion(version);
    	
        ConceptDao.save(codedConcept);
        
        return codedConcept;
		
	}
    /**
     * Create a new concept using a partially complete CodedConcept object
     * @param concept CodedConcept
     * @throws Exception
     */
    protected static CodedConcept create(CodedConcept concept) throws STSException
    {
    	CodedConcept codedConcept = concept;
    	ConceptDao.save(codedConcept);
    	
    	return codedConcept;
    }
	
     /**
     * get list of concept changes
     * @param conceptEntityIds
     * @return
     */
    public static List<ConceptChangeDTO> getConceptChanges(Collection<Long> conceptEntityIds, long versionId)
    {
        List<ConceptChangeDTO> changeList = new ArrayList<ConceptChangeDTO>();
        List<CodedConcept> codedConcepts = CodedConceptDao.getAll(conceptEntityIds, versionId);
        HashMap<Long, CodedConcept> previousCodedConceptsMap = CodedConceptDao.getPreviousConceptsForVersionMap(conceptEntityIds, versionId);
        HashMap<Long, ConceptState> conceptStatesMap = ConceptStateDelegate.getConceptStatesMap(conceptEntityIds);
        for (CodedConcept codedConcept : codedConcepts)
        {
            ConceptChangeDTO change = new ConceptChangeDTO();
            change.setVersionId(versionId);
            change.setRecent(codedConcept);
            if (codedConcept.getVersion().getId() == versionId)
            {
                CodedConcept codedConceptPrevious = previousCodedConceptsMap.get(codedConcept.getEntityId());
                change.setPrevious(codedConceptPrevious);
            }
            change.setConceptState(conceptStatesMap.get(codedConcept.getEntityId()));
            changeList.add(change);
		}
        
        return changeList;
    }
    
    
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return CodedConceptDao.getConceptEntityIdsByVersionId(versionId);
    }
    
	/** get all versions of the concept
	 * @param conceptEntityId
	 * @return
	 */
	public static List<CodedConcept> getAllVersions(long conceptEntityId, boolean includeAuthoring)
	{
		return CodedConceptDao.getAllVersions(conceptEntityId, includeAuthoring);
	}
	
	/**
	 * Check for duplicate VUID in Concept
	 * @throws STSUpdateException
	 */
    public static void checkForDuplicateVUIDs() throws STSException
    {
    	CodedConceptDao.checkForDuplicateVUIDs();
    }

	public static CodedConcept getConceptFromDesignationCode(String designationCode)
	{
		return CodedConceptDao.getConceptFromDesignationCode(designationCode);
	}

	public static CodedConcept getConceptFromDesignationVuid(long designationVuid)
    {
        return CodedConceptDao.getConceptFromDesignationVuid(designationVuid);
    }
	
	/**
	 * Get all CodedConcepts with relationships to passed designation VUIDs
	 * @param designationVuids
	 * @return List<CodedConcept>
	 */
	public static List<CodedConceptDesignationDTO> getConceptsFromDesignationVuids(List<Long> designationVuids)
	{
		return CodedConceptDao.getConceptsFromDesignationVuids(designationVuids);
	}
	
	public static List<CodedConceptDesignationDTO> getConceptWithMatchingDesignationsAndRoot(String designationName, String rootName)
	{
	    return CodedConceptDao.getConceptWithMatchingDesignationsAndRoot(designationName, rootName);
	}
    
    public static CodedConcept get(String name, Version version)
    {
    	return CodedConceptDao.get(name, version);
    }
    
	public static boolean isCodeSystemMember(Long codeSystemId, String conceptCode)
	{
		return CodedConceptDao.isCodeSystemMember(codeSystemId, conceptCode);
	}
	
	public static TotalEntityListView getVhatDomains(String domainName, Integer pageNumber, Integer pageSize) throws STSException
	{
		return CodedConceptDao.getVhatDomains(domainName, pageNumber, pageSize);
	}

	public static long getDomainConceptCount(Long domainVuid)
	{
		return CodedConceptDao.getDomainConceptCount(domainVuid);
	}
	
	public static TotalEntityListView getDomainConcepts(Long domainVuid, Integer pageNumber, Integer pageSize)
	{
		return CodedConceptDao.getDomainConcepts(domainVuid, pageNumber, pageSize);
	}

	public static CodedConcept getDomainByVuid(long domainVuid) throws STSException
	{
		return CodedConceptDao.getDomainByVuid(domainVuid);
	}

	public static CodedConcept getCodedConceptFromDesignationName(long codeSystemId, String designationName)
	{
		return CodedConceptDao.getCodedConceptFromDesignationName(codeSystemId, designationName);
	}

	public static List<ConceptHierarchyDTO> getConceptHierarchyList(long codeSystemId, long conceptEntityId)
	{
		return CodedConceptDao.getConceptHierarchyList(codeSystemId, conceptEntityId);
	}

    public static List<CodedConcept> getCodedConceptsByVersionId(long versionId)
    {
    	return CodedConceptDao.getCodedConceptsByVersionId(versionId);
    }
}
