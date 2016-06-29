/**
 * 
 */
package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.CodeSystemDelegate;
import gov.vha.vets.term.services.business.DesignationDelegate;
import gov.vha.vets.term.services.dto.DesignationRelationshipDTO;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author VHAISLMURDOH
 * 
 */
public class DesignationRelationshipDao extends EntityBaseDao
{
    public static DesignationRelationship get(long conceptEntityId, long designationEntityId) 
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from DesignationRelationship desRel " +
        	" WHERE id = (SELECT MAX (desrel1.id) " +
        	"    FROM DesignationRelationship desrel1 " +
        	"      WHERE desrel1.entityId = " +
        	"        (SELECT desrel2.entityId " +
        	"          FROM DesignationRelationship desrel2 " +
        	"            WHERE desrel2.id = " +
        	"              (SELECT MAX (desrel3.id) " +
        	"                FROM DesignationRelationship desrel3 " +
        	"                   WHERE desrel3.sourceEntityId = :sourceEntityId " +
        	"                      AND desrel3.targetEntityId = :targetEntityId))) ";

        DesignationRelationship designationRelationship = (DesignationRelationship) session.createQuery(query).setLong("sourceEntityId", conceptEntityId)
                .setLong("targetEntityId", designationEntityId).uniqueResult();

        return designationRelationship;
    }
    
    public static DesignationRelationship getVersioned(long conceptEntityId, long designationEntityId) 
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from DesignationRelationship desRel " +
        	" WHERE id = (SELECT MAX (desrel1.id) " +
        	"    FROM DesignationRelationship desrel1 " +
        	"      WHERE desrel1.version.id < :authoringVersion " +
        	"        AND desrel1.entityId = " +
        	"        (SELECT desrel2.entityId " +
        	"          FROM DesignationRelationship desrel2 " +
        	"            WHERE desrel2.id = " +
        	"              (SELECT MAX (desrel3.id) " +
        	"                FROM DesignationRelationship desrel3 " +
        	"                   WHERE desrel3.sourceEntityId = :sourceEntityId " +
        	"                      AND desrel3.targetEntityId = :targetEntityId))) ";

        DesignationRelationship designationRelationship = (DesignationRelationship) session.createQuery(query).setLong("sourceEntityId", conceptEntityId)
        	.setLong("targetEntityId", designationEntityId).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).uniqueResult();

        return designationRelationship;
    }
    
    /**
     * Save a designationRelationship
     * 
     * @param designationRelationship
     * @return
     */
    public static DesignationRelationship save(DesignationRelationship designationRelationship)
    {
        HibernateSessionFactory.currentSession().save(designationRelationship);
        return designationRelationship;
    }

    public static void delete(Long entityId)
    {
        HibernateSessionFactory.currentSession().createQuery("delete from DesignationRelationship where entityId = :entityId and version.id = :versionId").
            setParameter("entityId", entityId).setParameter("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID).executeUpdate();
    }

    /**
     * Set a DesignationRelationship to a finalized version
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        String query = "update DesignationRelationship dr set dr.version = :"+NEW_VERSION+" where dr.version.id = :"+AUTHORING_VERSION+" and "
        + " dr.sourceEntityId in (:entityId)";
        
        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }

    @SuppressWarnings("unchecked")
    public static List<DesignationRelationshipDTO> getAllVersions(Long conceptEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        List<DesignationRelationshipDTO> results = new ArrayList<DesignationRelationshipDTO>();

        // get property for the given code
        String hql = "from DesignationRelationship dr, CodedConcept c where dr.sourceEntityId = c.entityId and "
				+ "dr.entityId in (select dr2.entityId from DesignationRelationship dr2 where dr2.sourceEntityId = :conceptEntityId) and dr.version.id "+operator+" :"+AUTHORING_VERSION+" order by dr.entityId, dr.id";
        
        Query query = session.createQuery(hql);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List relationships = query.list(); 
        Set conceptEntityIds = new HashSet<Long>();
        for (Iterator iter = relationships.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            DesignationRelationship relationship = (DesignationRelationship)object[0];
            CodedConcept concept = (CodedConcept) object[1];
            conceptEntityIds.add(concept.getEntityId());
            DesignationRelationshipDTO detail = new DesignationRelationshipDTO(concept.getName(), relationship);
            results.add(detail);
        }
        Map<Long, Designation>  designationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), 
                HibernateSessionFactory.AUTHORING_VERSION_ID, conceptEntityIds);
        for (DesignationRelationshipDTO designationRelationshipDTO : results)
        {
            Designation designation = designationMap.get(designationRelationshipDTO.getRelationship().getSourceEntityId());
            if (designation != null)
            {
                designationRelationshipDTO.setName(designation.getName());
            }
        }

        return results;
    }
}
