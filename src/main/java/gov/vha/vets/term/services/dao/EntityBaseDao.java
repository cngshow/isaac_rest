/**
 * 
 */
package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author vhaislchevaj
 *
 */
public class EntityBaseDao extends BaseDao
{
	static final String NEW_VERSION = "newVersion";
	static final String AUTHORING_VERSION = "authoringVersion";
    enum VersionSearchType { CURRENT, PREVIOUS }

	/** Generic method for setting the authoring to a verion
	 * @param conceptEntityIdList
	 * @param version
	 * @param prefix
	 * @return
	 */
	protected static int setAuthoringToVersion(String query, Collection<Long> conceptEntityIdList, Version version, String parameter)
	{
		Session session = HibernateSessionFactory.currentSession();
	
	    Query sessionQuery = session.createQuery(query);
	
	    sessionQuery.setLong(NEW_VERSION, version.getId());
	    sessionQuery.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);

		return executeUpdate(sessionQuery, parameter, conceptEntityIdList);
	}
}
