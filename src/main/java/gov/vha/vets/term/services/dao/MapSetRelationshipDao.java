package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.MapSetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;

import org.hibernate.Query;

public class MapSetRelationshipDao extends EntityBaseDao
{
    /**
     * Save a MapSetRelationship
     * 
     * @param mapSetRelationship
     * @return
     */
    public static MapSetRelationship save(MapSetRelationship mapSetRelationship)
    {
        HibernateSessionFactory.currentSession().save(mapSetRelationship);
        return mapSetRelationship;
    }

	public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
	{
        String query = "update MapSetRelationship msr set msr.version = :"+NEW_VERSION+" where msr.version.id = :"+AUTHORING_VERSION+" and "
                + " msr.sourceEntityId in (:entityId)";

        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
	}

	public static MapSetRelationship get(long mapSetEntityId, long mapEntryEntityId)	
	{
        String hql = "from MapSetRelationship msr where msr.id = (select max(msr2.id) from MapSetRelationship msr2 "
                + " where msr2.sourceEntityId = :sourceEntityId and msr2.targetEntityId = :targetEntityId and msr2.entityId = msr.entityId ))";

        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
        query.setParameter("sourceEntityId", mapSetEntityId);
        query.setParameter("targetEntityId", mapEntryEntityId);
        MapSetRelationship mapSetRelationship = (MapSetRelationship) query.uniqueResult();

        return mapSetRelationship;
	}

    public static MapSetRelationship get(Long mapSetRelationshipEntityId)
    {
        String hql = "from MapSetRelationship msr where msr.entityId = :entityId and msr.id = (select max(msr2.id) from MapSetRelationship msr2 "
            + " where msr2.entityId = msr.entityId )";

        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
        query.setParameter("entityId", mapSetRelationshipEntityId);
        MapSetRelationship mapSetRelationship = (MapSetRelationship) query.uniqueResult();
    
        return mapSetRelationship;
    }

}
