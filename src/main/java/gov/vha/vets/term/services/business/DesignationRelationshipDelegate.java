/**
 * 
 */
package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.DesignationDao;
import gov.vha.vets.term.services.dao.DesignationRelationshipDao;
import gov.vha.vets.term.services.dto.DesignationRelationshipDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationRelationship;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;

import java.util.List;

/**
 * @author VHAISLMURDOH
 *
 */
public class DesignationRelationshipDelegate
{
    
    public static void create(long conceptEntityId, Designation designation)
    {
        // create the designation relation to 'connect' the concept to
        // the designation
        DesignationRelationship designationRel = new DesignationRelationship(conceptEntityId,
        		designation.getEntityId(), designation.getVersion(), true);
        DesignationRelationshipDao.save(designationRel);
    }
    
    /**
     * Reassign designation from one coded concept to another coded concept
     * @param codeSystem
     * @param designationCode
     * @param oldConceptCode
     * @param newConceptCode
     * @throws STSException
     */
    public static void update(CodeSystem codeSystem, String designationCode, String oldConceptCode, String newConceptCode, State state) throws STSException
    {
        Designation designation = DesignationDao.get(codeSystem, designationCode);

        CodedConcept oldCodedConcept = CodedConceptDelegate.get(codeSystem, oldConceptCode);
        CodedConcept newCodedConcept = CodedConceptDelegate.get(codeSystem, newConceptCode);
        ConceptStateDelegate.createOrUpdate(oldCodedConcept.getEntityId(), state);
        ConceptStateDelegate.createOrUpdate(newCodedConcept.getEntityId(), state);

    	DesignationRelationship designationRelationship = DesignationRelationshipDao.get(oldCodedConcept.getEntityId(), designation.getEntityId());
    	if (designationRelationship == null)
    	{
    		throw new STSException("Designation relationship not found with coded concept code: "
    				+ oldConceptCode + " and with designation code: " + designation.getCode());
    	}
    	
    	if (designationRelationship.getTargetEntityId() != designation.getEntityId())
    	{
    		throw new STSException("Designation with code: " + designation.getCode() + " is not associated with coded concept code: " + oldConceptCode);
    	}
    	
		// create new designation relationship in authoring
        DesignationRelationship newDesignationRelationship = new DesignationRelationship(newCodedConcept.getEntityId(), designation.getEntityId(),
        		VersionDelegate.getAuthoring(), true);
        newDesignationRelationship.setEntityId(designationRelationship.getEntityId());
        DesignationRelationshipDao.save(newDesignationRelationship);
    }
    
    public static int setAuthoringToVersion(List<Long> conceptEntityIdList, Version version)
    {
        return DesignationRelationshipDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    public static List<DesignationRelationshipDTO> getAllVersions(Long conceptEntityId, boolean includeAuthoring)
    {
        return DesignationRelationshipDao.getAllVersions(conceptEntityId, includeAuthoring);
        
    }
    public static void delete(long conceptEntityId, long designationEntityId) throws STSException
    {
        DesignationRelationship designationRelationship = DesignationRelationshipDao.get(conceptEntityId, designationEntityId);
        if (designationRelationship != null)
        {
            DesignationRelationshipDao.delete(designationRelationship.getEntityId());
        }
        else
        {
            throw new STSUpdateException("Cannot delete the designation relationship for conceptEntityId: "+conceptEntityId+", Designation entityId: "+designationEntityId);
        }
    }
}
