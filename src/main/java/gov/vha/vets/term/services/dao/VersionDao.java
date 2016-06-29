package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.ServicesDeploymentDelegate;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class VersionDao
{
    private static Logger log = Logger.getLogger(ServicesDeploymentDelegate.class.getPackage().getName());

	/**
	 * @param name
	 * @return
	 * @throws STSNotFoundException
	 */
	public static Version get(long codeSystemId, String versionName) 
	{
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Version v where v.codeSystem.id = :codeSystemId and upper(v.name) = upper(:name)";
        Version version = (Version) session.createQuery(query).setLong("codeSystemId", codeSystemId).setString("name", versionName).uniqueResult();
		return version;
	}

    public static Version getByCodeSystemVuid(long codeSystemVuid, String versionName) 
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String hql = "from Version v, CodeSystem cs where v.codeSystem.id = cs.id and cs.vuid = :codeSystemVuid and v.name = :name";
        Query query = session.createQuery(hql);
        query.setLong("codeSystemVuid", codeSystemVuid);
        query.setString("name", versionName);
        Object[] item = (Object[]) query.uniqueResult();
        Version version = null;
        if (item != null && item[0] instanceof Version)
        {
            version = (Version)item[0];
        }
        return version;
    }
	
    public static Version get(String codeSystemName, String versionName)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Version v where upper(v.codeSystem.name) = upper(:codeSystemName) and upper(v.name) = upper(:name)";
        Version version = (Version) session.createQuery(query).setString("codeSystemName", codeSystemName).setString("name", versionName).uniqueResult();
        return version;
    }

    public static Version getRecent(String codeSystemName, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        String sql = "from Version v where v.id = (select max(version.id) from Version version where version.id "+operator+" :authoringVersion and version.codeSystem.name = :codeSystemName)";
        
        Query query =  session.createQuery(sql);
        query.setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setString("codeSystemName", codeSystemName);
        Version version = (Version) query.uniqueResult();
       
        return version;
    }
    
	public static Version create(Version version) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();
        if (version.getCodeSystem() == null)
        {
            throw new STSNotFoundException("CodeSystem not specified for version: " + version.getName());
        }
        session.save(version);
        session.flush();
        return version;
    }

	/**
	 * @param conceptEntityId
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<Version> getVersions(long conceptEntityId)
	{
		Session session = HibernateSessionFactory.currentSession();
        
		String versionQuery = "from Version v where v.codeSystem in "
            + "(from CodeSystem cs where cs.id in "
            + "(select c.codeSystem.id from CodedConcept c where c.entityId = :conceptEntityId)) order by v.id desc";
        List<Version> list = session.createQuery(versionQuery).setLong("conceptEntityId", conceptEntityId).list();
		return list;
	}

	/**
	 * @param codeSystem
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<Version> getFinalizedVersions(CodeSystem codeSystem) 
	{
		Session session = HibernateSessionFactory.currentSession();

        String query = "from Version v where v.codeSystem.id = :codeSystemId and v.id < :authoringVersionId order by id desc";
        List<Version> versionList = session.createQuery(query).setLong("codeSystemId", codeSystem.getId())
        	.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID).list();

        return versionList;
	}

	@SuppressWarnings("unchecked")
    public static List<Version> getAllFinalizedVersions() 
	{
		Session session = HibernateSessionFactory.currentSession();

        String query = "from Version v where v.id < :authoringVersionId order by id desc";
        List<Version> versionList = session.createQuery(query).setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID).list();

        return versionList;
	}

	@SuppressWarnings("unchecked")
    public static List<Version> getByCodeSystemVuids(List<Long> codeSystemVuids) 
	{
		Session session = HibernateSessionFactory.currentSession();

        String hqlQuery = "from Version v where v.codeSystem.vuid in (:codeSystemVuids) order by id desc";
        List<Version> versionList = session.createQuery(hqlQuery).setParameterList("codeSystemVuids", codeSystemVuids).list();

        return versionList;
	}

	/**
	 * @param codeSystem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Version> getVersions(CodeSystem codeSystem) 
	{
		Session session = HibernateSessionFactory.currentSession();

        String query = "from Version v where v.codeSystem.id = :codeSystemId order by id desc";
        List<Version> versionList = session.createQuery(query).setLong("codeSystemId", codeSystem.getId()).list();

        return versionList;
	}

    public static Version getByVersionId(long versionId) 
    {
        Session session = HibernateSessionFactory.currentSession();
        String sql = "from Version v where v.id = :versionId";
        Version version = (Version) session.createQuery(sql).setLong("versionId", versionId).uniqueResult();        
        return version;
    }
    
    /**
     * 
     * @param finalizedVersionNames
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Long> getByVersionNames(List<String> finalizedVersionNames) 
    {
        Session session = HibernateSessionFactory.currentSession();
        String sql = "select v.id from Version v where v.name in (:finalizedVersionNames) order by v.id desc";
        List<Long> versionIds = session.createQuery(sql).setParameterList("finalizedVersionNames", finalizedVersionNames).list();

        return versionIds;
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getVersionNamesByIds(List<Long> versionIds)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String sql = "select v.name from Version v where v.id in (:versionIds) order by v.id desc";
        List<String> versionNames = session.createQuery(sql).setParameterList("versionIds", versionIds).list();
        
        return versionNames;
  	}

    /**
     * Get a list of all non-VHAT versions
     * @param vhatCodeSystemId
     * @return List<Version>
     */
    @SuppressWarnings("unchecked")
    public static List<Version> getSDOVersions(long vhatCodeSystemId)
    {
    	Session session = HibernateSessionFactory.currentSession();
    	String hql = "from Version v where v.codeSystem.id <> :vhatId order by codeSystem.name, importDate desc";
    	List<Version> sdoVersions = session.createQuery(hql).setParameter("vhatId", vhatCodeSystemId).list();
    	
    	return sdoVersions;
    }
    
    /**
     * Remove the version by ID, including CodedConcepts, Designations
	 * and DesignationRelationships
     * @param versionId
     * @throws STSException 
     */
    @SuppressWarnings("unchecked")
	public static void removeSDOVersion(long versionId) throws STSException
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String changeGroupIdQuery =
    		"select distinct changegroup_id from concept where version_id = :versionId " +
    		"UNION " + 
    		"select distinct changegroup_id from relationship where version_id = :versionId " +
    		"UNION " + 
    		"select distinct changegroup_id from property where version_id = :versionId ";
    	List<BigDecimal> changeGroupIdListBigDecimal = session.createSQLQuery(changeGroupIdQuery).setLong("versionId", versionId).list();
    	List<Long> changeGroupIdList = new ArrayList<Long>();
    	for (BigDecimal changeGroupId : changeGroupIdListBigDecimal)
		{
			changeGroupIdList.add(changeGroupId.longValue());
		}

        String conceptQuery = "delete Concept where version.id = :versionId";
        session.createQuery(conceptQuery).setLong("versionId", versionId).executeUpdate();
        
    	try
        {
    	    // delete using the changeGroupId so we can remove subsets that are associated to versions
            conceptQuery = "delete Subset where changeGroup.id in (:changeGroupIds)";
            session.createQuery(conceptQuery).setParameterList("changeGroupIds", changeGroupIdList).executeUpdate();
        }
        catch (HibernateException e)
        {
            log.warn("Cannot remove subset for changeGroupIds: "+changeGroupIdList, e);
        }
    	
    	String relationshipQuery = "delete Relationship where version.id = :versionId";
    	session.createQuery(relationshipQuery).setLong("versionId", versionId).executeUpdate();

    	String propertyQuery = "delete Property p where p.version.id = :versionId";
        session.createQuery(propertyQuery).setLong("versionId", versionId).executeUpdate();

        try
        {
        	String versionQuery = "delete Version v where v.id = :versionId";
    		session.createQuery(versionQuery).setLong("versionId", versionId).executeUpdate();
        }
        catch (HibernateException e)
        {
        	Version v = getByVersionId(versionId);
        	String msg = "Cannot remove version: " + v.getName() + " with an id: " + versionId + ". There may be a dependancy on it.";
            log.warn(msg, e);
            throw new STSException(msg);
        }

    	String changeGroupQuery = "delete ChangeGroup cg where cg.id = (:changeGroupId)";
    	for (Long changeGroupId : changeGroupIdList)
    	{
           	try
            {
                session.createQuery(changeGroupQuery).setLong("changeGroupId", changeGroupId).executeUpdate();
            }
            catch (HibernateException e)
            {
            	String msg = "Cannot remove ChangeGroupId: "+changeGroupId;
                log.warn(msg, e);
                throw new STSException(msg);
            }
		}
    	
    	session.flush();
    }

    /**
     * Get VHAT versions
     * @return List<Version>
     */
	@SuppressWarnings("unchecked")
	public static List<Version> getVHATVersions(boolean includeAuthoring)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = " from Version v, " +
					 "     CodeSystem cs " +
					 "where v.codeSystem.id = cs.id " +
					 "  and cs.name = :vhatName ";
					 
		hql += (includeAuthoring) ?	 "  and v.id <= :authoringVersionId" : "  and v.id < :authoringVersionId";
		
		Query query = session.createQuery(hql);
		query.setString("vhatName", HibernateSessionFactory.VHAT_NAME);
		query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		List<Object[]> objects = (List<Object[]>)query.list();
		
		List<Version> vhatVersions = new ArrayList<Version>();
		for(Object[] myObject : objects)
		{
			vhatVersions.add((Version)myObject[0]);
		}
		
		return vhatVersions;
	}

	/**
	 * get the date of the last update. For Standard Coding systems it is the date they were imported, 
	 * for VHAT it is the effective date.
	 * @return
	 */
	public static Date getLastUpdatedDate()
    {
		Date updatedDate = null;
		
		// search for last import date for SDO's and mappings and probably the last effective date for vhat, compare and
		// return the most recent.
		Session session = HibernateSessionFactory.currentSession();
		String sql = "from Version v where v.id = (select max(version.id) from Version version where version.id < :authoringVersionId)";
		Version version = (Version) session.createQuery(sql).setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID).uniqueResult();

		if (version != null)
		{
			updatedDate = (version.getImportDate() != null) ? version.getImportDate(): version.getEffectiveDate();
		}
		
		return updatedDate;
	}

	public static void update(Version version)
	{
    	HibernateSessionFactory.currentSession().update(version);
	}
}
