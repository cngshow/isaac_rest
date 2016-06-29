package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.DesignationDao;
import gov.vha.vets.term.services.dto.ConceptDesignationDTO;
import gov.vha.vets.term.services.dto.api.SubsetContentsListView;
import gov.vha.vets.term.services.dto.change.DesignationChangeDTO;
import gov.vha.vets.term.services.exception.STSDuplicateException;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VHAISLMURDOH
 * 
 */
public class DesignationDelegate
{

    public static Designation create(Version version, String name, long vuid, String code, String typeName, boolean isActive) throws STSException
    {
        Designation designation = new Designation();
        designation.setVersion(version);
        designation.setName(name);
        designation.setVuid(vuid);
        designation.setCode(code);
        DesignationType type = DesignationDelegate.getType(typeName);
        if (type == null)
        {
            type = createType(typeName);
        }
        designation.setType(type);
        designation.setCodeSystem(version.getCodeSystem());
        designation.setActive(isActive);
        DesignationDao.save(designation);
        return designation;
    }

    /**
     * Create a designation unless it is already there
     * 
     * @param codeSystem
     * @param conceptCode
     * @param designationName
     * @param vuid
     * @param designationTypeName
     * @param active
     * @throws STSNotFoundException
     * @throws STSDuplicateException
     */
    public static Designation create(CodeSystem codeSystem, String code, String conceptCode, String designationName, long vuid, String designationTypeName, boolean active, State state)
            throws STSException
    {
        CodedConcept concept = CodedConceptDelegate.get(codeSystem, conceptCode);
        if (concept == null)
        {
            throw new STSException("Cannot find CodedConcept with code: "+conceptCode);
        }

        Designation designation = new Designation();
        designation.setCodeSystem(codeSystem);
        designation.setActive(active);
        designation.setCode(code);
        designation.setName(designationName);
        designation.setVersion(VersionDelegate.getAuthoring());
        designation.setVuid(vuid);
        DesignationType designationType = new DesignationType(designationTypeName);
        designation.setType(designationType);

        return create(concept.getEntityId(), designation, state);
    }
    /**
     * Create a new entry unless it is already there
     * 
     * @param concept
     * @param designation
     * @throws STSException 
     */
    public static Designation create(long conceptEntityId, Designation designation, State state) throws STSException
    {
        // check for valid vuid
        if (designation.getVuid() < 1)
        {
            throw new STSException("Designation code: "+designation.getCode()+" Designation name: "+designation.getName()+" has invalid vuid of: "+designation.getVuid());
        }

        // get the version model object
        if (designation.getVersion().getId() < 1)
        {
            designation.setVersion(VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, designation.getVersion().getName()));
        }
        DesignationType type = DesignationDao.getType(designation.getType().getName());
        if (type == null)
        {
            type = createType(designation.getType().getName());
        }
        designation.setType(type);

        DesignationDao.save(designation);

        DesignationRelationshipDelegate.create(conceptEntityId, designation);
        ConceptStateDelegate.createOrUpdate(conceptEntityId, state);
        
        return designation;
    }

    /**
     * Update a designation
     * @param codeSystem
     * @param conceptCode
     * @param designationCode
     * @param type
     * @param oldDesignationName
     * @param newDesignationName
     * @param vuid
     * @param active
     * @param state
     * @throws STSNotFoundException
     * @throws STSException
     */
    public static void update(CodeSystem codeSystem, String conceptCode, String designationCode,
            String type, String oldDesignationName, String newDesignationName, long vuid,
            boolean active, State state) throws STSNotFoundException, STSException
    {
        Designation designation = DesignationDao.get(codeSystem, designationCode);

        if (designation == null)
        {
            throw new STSNotFoundException("Cannot find designation name: " + oldDesignationName + "to update for designation code: " + designationCode
                    + " and concept code:" + conceptCode);
        }
        Designation versionDesignation = designation.getVersion().getId() != HibernateSessionFactory.AUTHORING_VERSION_ID ? designation : DesignationDao.getVersioned(designation);
        if (versionDesignation != null && (!newDesignationName.equals(designation.getName()) || vuid != designation.getVuid()))
        {
            throw new STSUpdateException("Cannot change designation: " + designation.getName()+" VUID: "+designation.getVuid()+" to: " + newDesignationName + " VUID: " + vuid);
        }
        if (vuid < 1)
        {
            throw new STSException("Designation code: "+designationCode+" Designation name: "+newDesignationName+" has invalid vuid of: "+vuid);
        }

        DesignationType designationType = (DesignationType) ((type.equals(designation.getType().getName())) ? designation.getType() : getTypeFromName(type));
        addEntity(designation, newDesignationName, VersionDelegate.getAuthoring(), designationType, vuid, active);
        ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);
    }
    
    public static List<Designation> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        return DesignationDao.getAllVersions(conceptEntityId, includeAuthoring);
    }

    public static List<Designation> getAll(long conceptEntityId)
    {
        return DesignationDao.getAll(conceptEntityId);
    }

    public static Designation get(CodeSystem codeSystem, String designationCode) 
    {
        return DesignationDao.get(codeSystem, designationCode);
    }

    public static List<Designation> get(CodeSystem codeSystem, Collection<String> designationCodes)
    {
        return DesignationDao.get(codeSystem, designationCodes);
    }

    public static List<Designation> get(Concept concept)
    {
        return DesignationDao.getDesignations(concept.getEntityId());
    }

    public static List<Designation> get(CodedConcept concept, Version version)
    {
        return DesignationDao.getDesignations(concept.getEntityId(), version.getId());
    }
    
    public static Map<Long, Collection<Designation>> getDesignations(Collection<Long> conceptEntityIds, long versionId)
    {
        return DesignationDao.getDesignations(conceptEntityIds, versionId);
    }


    /**
     * Create a new entry for an SDO
     * @param concept
     * @param designation
     * @throws STSException 
     */
    public static Designation createSDO(Version version, long conceptEntityId, DesignationType type, String designationName, String designationCode, Long vuid, boolean active) throws STSException
    {
        Designation designation = new Designation();

        designation.setActive(active);
        if (designationName == null || designationName.length() < 1)
        {
            throw new STSException("Cannot create designation with no name, for code:"+designationCode);
        }
        designation.setName(designationName);
        designation.setCodeSystem(version.getCodeSystem());
        designation.setCode(designationCode);
        designation.setVersion(version);
        designation.setVuid(vuid);
        designation.setType(type);

        DesignationDao.save(designation);

        DesignationRelationshipDelegate.create(conceptEntityId, designation);

        return designation;
    }

    


    private static DesignationType getTypeFromName(String type)
    {
        DesignationType designationType = DesignationDao.getType(type);
        if (designationType == null)
        {
            designationType = createType(type);
        }
        return designationType;
    }

    private static Designation addEntity(Designation prevDesignation, String name, Version version, DesignationType type, Long vuid, boolean active)
    {
        Designation designation = new Designation();
        designation.setEntityId(prevDesignation.getEntityId());
        designation.setCode(prevDesignation.getCode());
        designation.setName(name);
        designation.setVuid(vuid);
        designation.setType(type);
        designation.setVersion(version);
        designation.setCodeSystem(version.getCodeSystem());
        designation.setActive(active);
        DesignationDao.save(designation);
        return designation;
    }

    /**
     * Change the status of a designation - used only for SDO updates
     * @param version
     * @param conceptCode
     * @param name
     * @param type
     * @param active
     * @throws STSNotFoundException
     */
    public static void updateSDO(Version version, Designation designation, String newName, DesignationType type, boolean active) throws STSNotFoundException
    {
        if (active != designation.getActive() || !newName.equals(designation.getName()) || !type.getName().equals(designation.getType().getName()))
        {
            addEntity(designation, newName, version, type, designation.getVuid(), active);
        }
        
    }
    /**
     * 
     * @param type
     * @return
     */
    public static DesignationType createType(String typeName)
    {
        DesignationType type = new DesignationType(typeName);
        DesignationDao.saveType(type);
        return type;
    }

    /**
     * 
     * @param designationTypes
     */
    public static void createTypes(List<String> designationTypes)
    {
        for (String name : designationTypes)
        {
            DesignationType type = new DesignationType(name);
            DesignationDao.saveType(type);
        }
    }
    /**
     * 
     * @param designationTypes
     */
    public static DesignationType getType(String name)
    {
        return DesignationDao.getType(name);
    }
    
    public static DesignationType getType(long id)
    {
        return DesignationDao.getType(id);
    }
    
    /**
     * Delete a designation
     * @param codeSystem
     * @param conceptCode
     * @param designationName
     * @throws STSUpdateException
     * @throws STSNotFoundException
     */
    public static void delete(CodeSystem codeSystem, String conceptCode, String code) throws STSException
    {
        Designation existingDesignation = DesignationDao.get(codeSystem, code);
        if (existingDesignation == null)
        {
            throw new STSNotFoundException("Cannot find designation code:"+code);
        }
        delete(codeSystem, conceptCode, existingDesignation);
    }

    /**
     * Delete and entry unless it has been versioned
     * 
     * @param codeSystem
     * @param conceptCode
     * @param designationName
     * @throws STSDuplicateException
     * @throws STSNotFoundException
     * @throws STSUpdateException
     */
    public static void delete(CodeSystem codeSystem, String conceptCode, Designation designation) throws STSException
    {
        if (designation.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            Designation versionDesignation = DesignationDao.getVersioned(designation);
            if (versionDesignation == null)
            {
                // we need to check and see if we have a subset entry for this
                // Ok to delete - there is no versioned entries
                CodedConcept concept = CodedConceptDelegate.get(codeSystem, conceptCode);
                removeOrphanEntities(concept.getEntityId(), designation.getEntityId());
                DesignationDao.delete(designation.getEntityId());
            }
        }
        else
        {
            throw new STSUpdateException("You cannot delete designation for concept code: " + conceptCode + " codeSystem:" + codeSystem.getName() + " name:"
                    + designation.getName() );
        }
    }
    
    public static void delete(long conceptEntityId, Designation designation) throws STSException
    {
        Designation versionDesignation = DesignationDao.getVersioned(designation);
        if (versionDesignation == null)
        {
            // we need to check and see if we have a subset entry for this
            // Ok to delete - there is no versioned entries
            removeOrphanEntities(conceptEntityId, designation.getEntityId());
            DesignationDao.delete(designation.getEntityId());
        }
    }
    
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return DesignationDao.getConceptEntityIdsByVersionId(versionId);
    }
    
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return DesignationDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    public static List<DesignationChangeDTO> getDesignationChanges(long conceptEntityId, long versionId)
    {
        List<Designation> designations = DesignationDao.getDesignations(conceptEntityId, versionId);
        List<Designation> previousDesignations = DesignationDao.getPreviousVersionDesignations(conceptEntityId, versionId);
        Map<Long, DesignationChangeDTO> map = new HashMap<Long, DesignationChangeDTO>();
        for (Designation designation : designations)
        {
            DesignationChangeDTO change = new DesignationChangeDTO();
            change.setVersionId(versionId);
            change.setRecent(designation);
            change.setProperties(PropertyDelegate.getPropertyChanges(designation.getEntityId(), versionId));
            change.setSubsetRelationships(SubsetRelationshipDelegate.getSubsetRelationshipChanges(designation.getEntityId(), versionId));
            map.put(designation.getEntityId(), change);
        }
        for (Designation designation : previousDesignations)
        {
            DesignationChangeDTO change = map.get(designation.getEntityId());
            if (change == null)
            {
                change = new DesignationChangeDTO();
                change.setVersionId(versionId);
                change.setPrevious(designation);
                change.setProperties(PropertyDelegate.getPropertyChanges(designation.getEntityId(), versionId));
                change.setSubsetRelationships(SubsetRelationshipDelegate.getSubsetRelationshipChanges(designation.getEntityId(), versionId));
                map.put(designation.getEntityId(), change);
            }
            else if (designation.getVersion().getId() != change.getRecent().getVersion().getId())
            {
                change.setPrevious(designation);
            }
        }
        List<DesignationChangeDTO> list = new ArrayList<DesignationChangeDTO>();
        list.addAll(map.values());
        return list;
    }

    public static Designation get(long designationEntityId)
    {
        return DesignationDao.get(designationEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
    
    public static Designation get(long designationEntityId, long versionId)
    {
        return DesignationDao.get(designationEntityId, versionId);
    }
    
    public static void main(String[] args) throws Exception
    {
//        CodeSystem codeSystem = CodeSystemDelegate.get("GEEK-5");
//        Designation designation = DesignationDelegate.get(codeSystem, "DX10004");
//        System.out.println(designation);
    	
    	Set<Long> conceptIds = new HashSet<Long>();
    	conceptIds.add(374L);
    	conceptIds.add(353L);
    	List<String> designationNames = new ArrayList<String>();
    	designationNames.add("Designation 10055");
    	designationNames.add("Designation 10034");
    	List<ConceptDesignationDTO> des = DesignationDelegate.get(conceptIds, designationNames);
    	System.out.println(des);
    }

	public static List<ConceptDesignationDTO> get(Set<Long> designationConceptIds, List<String> designationNames) 
	{
		return DesignationDao.get(designationConceptIds, designationNames);
	}
	
	public static List<ConceptDesignationDTO> getByVersionId(long versionId, boolean isFullVersion)
	{
		return DesignationDao.getByVersionId(versionId, isFullVersion);
	}

	public static Designation get(String subsetName, String name)
	{
		return DesignationDao.get(subsetName, name);
	}

	public static List<Designation> get(String name)
	{
		return DesignationDao.get(name);
	}

	public static Designation getByVuid(Long vuid)
	{
		return DesignationDao.getByVuid(vuid);
	}

    public static List<Designation> getByVuids(Set<Long> vuids)
    {
        return DesignationDao.getByVuids(vuids);
    }

    public static void removeDesignations(long conceptEntityId) throws STSException
    {
        
        List<Designation> designations = DesignationDao.getDesignations(conceptEntityId);
        for (Designation designation : designations)
        {
            removeOrphanEntities(conceptEntityId, designation.getEntityId());
            DesignationDao.delete(designation.getEntityId());
        }
    }
    
    private static void removeOrphanEntities(long conceptEntityId, Long designationEntityId) throws STSException
    {
        DesignationRelationshipDelegate.delete(conceptEntityId, designationEntityId);
        SubsetRelationshipDelegate.removeSubsetRelationships(designationEntityId);
        DesignationPropertyDelegate.removeDesignationProperties(designationEntityId);
    }

	public static List<Designation> getBySubset(long subsetEntityId, long versionId)
	{
		return DesignationDao.getBySubset(subsetEntityId, versionId);
	}

	public static SubsetContentsListView getBySubset(long subsetEntityId, long versionId,
            String designationName, String status, Integer pageSize,
            Integer pageNumber)
    {
        return DesignationDao.getSubsetContents(subsetEntityId, versionId, designationName, status, pageSize, pageNumber);
    }

	public static void save(Designation designation)
	{
		DesignationDao.save(designation);
	}

	public static Designation get(long mapSetEntityId, String designationType)
	{
		return DesignationDao.get(mapSetEntityId, designationType);
	}

	public static Designation inactivate(Designation designation) throws CloneNotSupportedException
	{
		// create a new one
		Designation newDesignation = (Designation) designation.clone();
		newDesignation.setId(0);
		newDesignation.setVersion(VersionDelegate.getAuthoring());
		newDesignation.setActive(false);

		DesignationDao.save(newDesignation);

		return newDesignation;
	}
	
	public static Designation getPreferredDesignationByVuid(long conceptVuid)
	{
		return DesignationDao.getPreferredDesignationByVuid(conceptVuid);
	}

	public static Designation getPreferredDesignation(long conceptEntityId, CodeSystem codeSystem)
	{
		return DesignationDao.getPreferredDesignation(conceptEntityId, codeSystem);
	}
	
	public static Designation getPreferredDesignation(Collection<Designation> designations, CodeSystem codeSystem)
	{
	    Designation preferredDesignation = null;
	    for (Designation designation : designations)
        {
	        if (preferredDesignation == null)
	        {
                preferredDesignation = designation;
	        }
            if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() && designation.getActive() == true)
            {
                // we have the preferred designation type and it is active - 1st choice
                preferredDesignation = designation;
            }
            else if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() &&
                    preferredDesignation.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have the preferred designation type and it is inactive - 2nd choice
                preferredDesignation = designation;
            }
            else if (designation.getActive() == true && preferredDesignation.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have an active designation (not preferred type) - 3rd choice
                preferredDesignation = designation;
            }
        }
	    return preferredDesignation;
	}

	public static Map<Long, Designation> getConceptDescriptionsByEntityIds(CodeSystem codeSystem, long versionId, Collection<Long> conceptEntityIds)
	{
		return DesignationDao.getConceptDescriptionsByEntityIds(codeSystem, versionId, conceptEntityIds);
	}
	
    public static Map<String, Designation> getConceptDescriptionsByConceptCodes(CodeSystem codeSystem, long versionId, Collection<String> conceptCodes)
    {
        return DesignationDao.getConceptDescriptions(codeSystem, versionId, conceptCodes);
    }
    
    public static Map<String, Designation> getBrowseConceptDescriptionsByConceptCodes(CodeSystem codeSystem, long versionId, Collection<String> conceptCodes)
    {
        return DesignationDao.getBrowseConceptDescriptions(codeSystem, versionId, conceptCodes);
    }

    public static List<String> getTypesByCodeSystem(String codeSystemName) 
    {
        return DesignationDao.getTypesByCodeSystem(codeSystemName);
    }
}
