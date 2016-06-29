package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class ConceptDao extends EntityBaseDao
{
    
    @SuppressWarnings("unchecked")
    public static List<Concept> get(CodeSystem codeSystem, Collection<String> codes)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String hql = "from Concept c where c.code in (:codes) and c.codeSystem = :codeSystem and c.id = (select max(con.id) from Concept con "
                        + "where con.entityId = c.entityId)";
        
        Query query = session.createQuery(hql);
        query.setParameter("codeSystem", codeSystem);
        List<Concept> list = executeQuery(query, "codes", codes);

        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Concept> getActiveOnly(CodeSystem codeSystem, Collection<String> codes)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String hql = "from Concept c where c.code in (:codes) and c.active = 1 and c.codeSystem = :codeSystem and c.id = (select max(con.id) from Concept con "
                        + "where con.entityId = c.entityId)";
        
        Query query = session.createQuery(hql);
        query.setParameter("codeSystem", codeSystem);
        List<Concept> list = executeQuery(query, "codes", codes);

        return list;
    }

    public static Concept get(long conceptEntityId)
    {
        return get(conceptEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
    
	public static Concept getByCode(String code)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "from Concept c1 where c1.code = :code and c1.id = (select max(c2.id) from Concept c2 where c2.entityId = c1.entityId)";
		return (Concept)session.createQuery(hql).setString("code", code).uniqueResult();
	}


	public static Concept getByVuid(Long vuid)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "from Concept where vuid = :vuid and entityId in (select max(id) from Concept c2 where c2.vuid = :vuid)";
		return (Concept)session.createQuery(hql).setLong("vuid", vuid).uniqueResult();
	}
	
    /**
     * Get a specific concept for a specific version.
     * 
     * @param codeSystem
     * @param code
     * @param version
     * @return Concept
     * @throws STSNotFoundException
     */
    public static Concept get(long conceptEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Concept cc where cc.id = (select max(con.id) from Concept con "
                        + "where con.entityId = :entityId and con.version.id <= :version)";
        Concept concept = (Concept) session.createQuery(query).setLong("entityId", conceptEntityId).setLong("version", versionId).uniqueResult();
        return concept;
    }
	
    public static Concept get(String conceptCode, long versionId)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Concept cc where cc.code = :code " +
        		"and cc.id = (select MAX(conmax.id) FROM Concept conmax WHERE conmax.version.id <= :versionId AND conmax.entityId = cc.entityId)";
        Concept concept = (Concept) session.createQuery(query).setString("code", conceptCode).setLong("versionId", versionId).uniqueResult();
        
        return concept;
	}
    
    public static Concept getVersioned(CodeSystem codeSystem, String code) 
    {
        return getPreviousVersion(codeSystem, code, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
	/**
	 * Get a specific concept for a specific version.
	 * 
	 * @param codeSystem
	 * @param code
	 * @param version
	 * @return Concept
	 * @throws STSNotFoundException
	 */
    @SuppressWarnings("unchecked")
    public static Concept get(CodeSystem codeSystem, String code)
	{
		Session session = HibernateSessionFactory.currentSession();

		// get concept for the given code
        String query = "from Concept cc where cc.id = (select max(con.id) from Concept con "
                		+ "where con.code = :code and con.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName)) "; 
        Object conceptObject = (Object) session.createQuery(query)
        							.setString("code", code)
        							.setString("codeSystemName", codeSystem.getName()).uniqueResult();
        Concept concept = (Concept) conceptObject;
        return concept;
	}
    
    /**
	 * Get a concept that is previous than the version specified
	 * 
	 * @param codeSystem
	 * @param code
	 * @return Concept
	 * @throws STSNotFoundException
	 */
    @SuppressWarnings("unchecked")
    public static Concept getPreviousVersion(CodeSystem codeSystem, String code, long versionId) 
    {
    	Session session = HibernateSessionFactory.currentSession();
        Concept concept = null;
    	
    	String versionQuery = "FROM Concept cc " +
    						  "WHERE cc.id = (SELECT MAX(con.id) FROM Concept con " +
    						  "  WHERE con.version.id < :version AND con.code = :code " +
    						  "   AND con.codeSystem.id = (SELECT cs.id FROM CodeSystem cs WHERE cs.name = :codeSystemName))";
    	
    	List<Concept> list = session.createQuery(versionQuery).setLong("version", versionId)
        								.setString("code", code)
        								.setString("codeSystemName", codeSystem.getName()).list();
    	if(list.size() == 1)
    	{
            concept = list.get(0);
    	}
    	
    	return concept;
    }
    
    /**
	 * Simple save of the concept using the passed Concept object as-is.
	 * 
	 * @param concept
	 * @throws STSException
	 */
    public static void save(Concept concept) throws STSException
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
        if (concept.getVersion().getCodeSystem().getId() != concept.getCodeSystem().getId())
        {
            throw new STSException("Concept's Version's CodeSystem is different that Concept's CodedSystem");
        }
        
        session.save(concept);
        session.flush();
    }
    
    /**
	 * Physical delete of the concept from the database
	 * 
	 * @param codedConcept
	 */
    public static void delete(Concept concept) 
    {
        HibernateSessionFactory.currentSession().createQuery("delete from Concept where entityId = :entityId").setParameter("entityId", concept.getEntityId()).executeUpdate();
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
        String query = "update Concept c set c.version = :"+NEW_VERSION+" where c.version.id = :"+AUTHORING_VERSION+" and c.entityId in (:entityId)";
        
        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }
}
