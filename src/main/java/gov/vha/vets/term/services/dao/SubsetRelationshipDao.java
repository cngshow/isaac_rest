package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.SubsetCountDTO;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

public class SubsetRelationshipDao extends EntityBaseDao
{
    

    /**
     * Get the most recent entry for a given subsetRelationship including anything in the
     * authoring version
     * 
     * @param subsetName
     * @param designationCode
     * @return
     * @throws STSNotFoundException
     */
    public static SubsetRelationship get(String subsetName, long designationEntityId) throws STSNotFoundException
    {
        String query = "from SubsetRelationship subsetRel where subsetRel.id in (select max(cr.id) from SubsetRelationship cr "
                + " where cr.sourceEntityId = (select distinct sub.entityId from Subset sub where sub.name = :name)"
                + " and cr.targetEntityId = :designationEntityId)";

        SubsetRelationship subsetRel = (SubsetRelationship) HibernateSessionFactory.currentSession().createQuery(query).setString("name", subsetName)
                .setLong("designationEntityId", designationEntityId).uniqueResult();

        return subsetRel;
    }
    
    /**
     * Get a list of SubsetRelationships for a given subset
     * @param subsetName
     * @return
     * @throws STSNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static List<SubsetRelationship> get(String subsetName) throws STSNotFoundException
    {
        String query = "from SubsetRelationship rel where rel.id in (select max(rel2.id) " +
        		"from SubsetRelationship rel2 where rel.entityId = rel2.entityId and " +
        		"sourceEntityId = (select sub.entityId from Subset sub where sub.name = :name ))";

        List<SubsetRelationship> subsetRels = HibernateSessionFactory.currentSession().createQuery(query)
            .setString("name", subsetName).list();

        return subsetRels;
    }

    public static List<SubsetRelationship> get(Collection<Long> subsetEntityIds, Collection<Long> designationEntityIds) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "from SubsetRelationship subsetRel where subsetRel.id in (select max(sr.id) from SubsetRelationship sr "
                + " where sr.sourceEntityId in (:subsetEntityIds)"
                + " and sr.targetEntityId in (:designationEntityIds) group by sr.entityId)";

        Query query = session.createQuery(hql);
        query.setParameterList("subsetEntityIds", subsetEntityIds);

        List<SubsetRelationship> results = executeQuery(query, "designationEntityIds", designationEntityIds);
        
        return results;
    }

    public static SubsetCountDTO getCount(long subsetEntityId, String versionName) 
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "select count(*), max(sr.version.id) from SubsetRelationship sr where sr.active = 1 "
        		+ "and sr.id in (select max(srmax.id) from SubsetRelationship srmax where srmax.sourceEntityId = :subsetEntityId "
                + "and srmax.version <= (select id from Version v where v.name = :versionName " 
                + "and v.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName)) "
                + "and srmax.entityId = sr.entityId) ";
        
        Query query = session.createQuery(hql);
        query.setParameter("subsetEntityId", subsetEntityId);
        query.setParameter("codeSystemName", HibernateSessionFactory.VHAT_NAME);
        query.setParameter("versionName", versionName);
        
        Object[] obj = (Object[])query.uniqueResult();
        long count = (Long)obj[0];
        long versionId = (Long)obj[1];
        
        return new SubsetCountDTO(count, versionId);
    }

    public static SubsetRelationship get(CodeSystem codeSystem, String subsetName, long conceptEntityId)
    {

        String query = "from SubsetRelationship subsetRel where subsetRel.id = (select max(cr.id) from SubsetRelationship cr "
                + " where cr.sourceEntityId = (select sub.entityId from Subset sub where sub.name = :name) and cr.targetEntityId = :entityId)";

        List<SubsetRelationship> existingSubsetRels = HibernateSessionFactory.currentSession().createQuery(query).setString("name", subsetName)
                .setLong("entityId", conceptEntityId).list();

        if (existingSubsetRels.size() == 0)
        {
            return null;
        }

        return existingSubsetRels.get(0);
    }

    /**
     * get a list of SubsetRelationships for a given designationEntityId
     * @param entityId
     * @return
     */
    public static List<SubsetRelationship> get(long designationEntityId)
    {
        return get(designationEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
    
    /**
     * get a list of SubsetRelationships for a given designationEntityId and versionId
     * @param entityId
     * @return
     */
    public static List<SubsetRelationship> get(long designationEntityId, long versionId)
    {
        String query = "from SubsetRelationship subsetRel where subsetRel.targetEntityId = :entityId " +
        	"and subsetRel.id = (select max(sr.id) from SubsetRelationship sr where sr.version.id <= :version and sr.entityId = subsetRel.entityId)";

        List<SubsetRelationship> existingSubsetRels = HibernateSessionFactory.currentSession().createQuery(query).setLong("entityId", designationEntityId).setLong("version", versionId).list();

        return existingSubsetRels;
    }
    
    /**
     * Get a list of the recent "real" version for a designationEntityId
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    public static List<SubsetRelationship> getPreviousVersionSubsetRelationships(long designationEntityId, long versionId)
    {
        String query = "from SubsetRelationship subRel where subRel.targetEntityId = :entityId " +
        	"and subRel.id = (select max(sr.id) from SubsetRelationship sr where sr.version.id < :version and sr.entityId = subRel.entityId)";

        List<SubsetRelationship> subsetRelations = HibernateSessionFactory.currentSession().createQuery(query).setLong("version", versionId).setLong("entityId", designationEntityId).list();

        return subsetRelations;
    }

    /**
     * Get the most recent "real" version
     * 
     * @param relationship
     * @return
     */
    public static Map<Long, SubsetRelationship> getVersioned(List<Long> subsetRelationshipEntityIds)
    {
        String hql = "from SubsetRelationship subRel where subRel.entityId in (:entityIds) and subRel.id = (select max(sr.id) from SubsetRelationship sr "
                + " where sr.version.id < :"+AUTHORING_VERSION+" and sr.entityId = subRel.entityId)";

        Map<Long, SubsetRelationship> results = new HashMap<Long, SubsetRelationship>();

        if (!subsetRelationshipEntityIds.isEmpty())
        {
	        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
	        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
	        List<SubsetRelationship> existingConceptRels = executeQuery(query, "entityIds", subsetRelationshipEntityIds);
	        
	        for (SubsetRelationship subsetRelationship : existingConceptRels)
			{
	        	results.put(subsetRelationship.getEntityId(), subsetRelationship);
			}
        }
        return results;
    }
    /**
     * Get the most recent "real" version
     * 
     * @param relationship
     * @return
     */
    
    public static SubsetRelationship getVersioned(long subsetRelationshipEntityId)
    {
        String hql = "from SubsetRelationship subRel where subRel.entityId = :entityId and subRel.id = (select max(sr.id) from SubsetRelationship sr "
                + " where sr.version.id < :"+AUTHORING_VERSION+" and sr.entityId = subRel.entityId)";

        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setParameter("entityId", subsetRelationshipEntityId);
        SubsetRelationship subsetRelationship = (SubsetRelationship) query.uniqueResult();
            
        return subsetRelationship;
    }

    /**
     * Get the most recent "real" version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     */
    public static SubsetRelationship getVersioned(CodeSystem codeSystem, String sourceCode, String relationshipTypeName)
    {

        String query = "from SubsetRelationship conceptRel "
                + "where conceptRel.id = (select max(cr.id) from SubsetRelationship cr "
                + " where cr.version.id < :"+AUTHORING_VERSION+""
                + " and cr.sourceEntityId = (select scon.entityId from CodedConcept scon where scon.code = :sourceCode and scon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))"
                + " and cr.relationshipType = (select relType.id from RelationshipType relType where relType.name = :typeName))";

        List<SubsetRelationship> existingConceptRels = HibernateSessionFactory.currentSession().createQuery(query)
                .setString("sourceCode", sourceCode).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).setString(
                        "codeSystemName", codeSystem.getName()).setString("typeName", relationshipTypeName).list();

        if (existingConceptRels.size() == 0)
        {
            return null;
        }

        return existingConceptRels.get(0);
    }

    public static void save(SubsetRelationship subsetRelationship)
    {
        HibernateSessionFactory.currentSession().save(subsetRelationship);
    }

    public static void delete(Long entityId)
    {
        HibernateSessionFactory.currentSession().createQuery("delete from SubsetRelationship where entityId = :entityId and version.id = :versionId").
            setParameter("entityId", entityId).setParameter("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID).executeUpdate();
    }

    /**
     * Set a concept to a finalized version
     * 
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "update SubsetRelationship cr set cr.version = :"+NEW_VERSION+" where cr.version.id = :"+AUTHORING_VERSION+" and "
                + " cr.targetEntityId in (select d.targetEntityId from DesignationRelationship d where d.sourceEntityId in (:entityId))";
        int rows = setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");

        query = "update SubsetRelationship cr set cr.version = :"+NEW_VERSION+" where cr.version.id = :"+AUTHORING_VERSION+" and "
                + " cr.targetEntityId in (:entityId)";
        rows += setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");

        return rows;
    }

    public static List<Long> getConceptEntityIdsByVersionId(Long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "Select distinct c.entityId from CodedConcept c where c.entityId in (select dr.sourceEntityId from DesignationRelationship dr where dr.targetEntityId in (select sr.targetEntityId from SubsetRelationship sr where sr.version.id = :versionId))";
        List<Long> conceptEntityIds = session.createQuery(query).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }
}
