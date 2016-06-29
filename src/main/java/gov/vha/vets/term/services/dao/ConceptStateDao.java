package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class ConceptStateDao extends BaseDao
{
    public static ConceptState save(ConceptState conceptState)
    {
        HibernateSessionFactory.currentSession().save(conceptState);
        return conceptState;
    }

    public static void update(Collection<Long> conceptEntityIds, State state)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "update ConceptState cs set cs.state = :state where cs.conceptEntityId in (:conceptEntityIds)";
        executeUpdate(session.createQuery(query).setParameter("state", state), "conceptEntityIds", conceptEntityIds);
    }
    
    public static void remove(Collection<Long> conceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "delete ConceptState cs where cs.conceptEntityId in (:conceptEntityIds)";
        executeUpdate(session.createQuery(query), "conceptEntityIds", conceptEntityIds);
    }
    
    public static ConceptState get(long conceptEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "from ConceptState cs where cs.conceptEntityId = :entityId";
        ConceptState conceptState = (ConceptState)session.createQuery(query).setLong("entityId", conceptEntityId).uniqueResult();
        return conceptState;
    }

    public static List<ConceptState> get(List<Long> conceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "from ConceptState cs where cs.conceptEntityId in (:conceptEntityIds)";
        List<ConceptState> conceptStates = executeQuery(session.createQuery(query), "conceptEntityIds", conceptEntityIds);

        return conceptStates;
    }
    
    /** Retrieve the map of conceptStates given the ConceptEntityId's in this state.
	 * @param state
	 * @return
	 */
	public static HashMap<Long, ConceptState> getConceptStatesMap(Collection<Long> conceptEntityIds)
	{
		Session session = HibernateSessionFactory.currentSession();
		HashMap<Long, ConceptState> conceptStateMap = new HashMap<Long, ConceptState>();
		
        String hqlQuery = "from ConceptState cs where cs.conceptEntityId in (:conceptEntityIds)";
        Query query = session.createQuery(hqlQuery);
        List<ConceptState> conceptStateList = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        for (ConceptState conceptState : conceptStateList)
        {
			conceptStateMap.put(conceptState.getConceptEntityId(), conceptState);
		}
        
        return conceptStateMap;
	}

    /** Return boolean whether there are ConceptState objects with the given State.
     * @param state
     * @return
     */
    public static boolean isStateExist(State state)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "select count(*) from ConceptState cs where cs.state = :stateId";
        Object count = session.createQuery(query).setLong("stateId", state.getId()).uniqueResult();
        
        return ((Long)count).longValue() != 0;
    }
    
    public static int removeInconsistentConceptStates() throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String sql = 
            "delete from conceptstate where concept_entity_id in ( "
            + "select concept_entity_id from ( "
            + "/* Concept */"
            + "select c1.entity_id from concept c1, concept c2 where c1.kind = 'C' and c2.kind = 'C' and c1.entity_id = c2.entity_id  "
            + " and c1.codesystem_id = (select id from codesystem where name = 'VHAT') and c2.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and c1.id = (select max(cmax.id) from concept cmax where cmax.kind = 'C' and cmax.version_id < :authoringVersion and cmax.entity_id = c1.entity_id) "
            + " and c2.id = (select max(cmax.id) from concept cmax where cmax.kind = 'C' and cmax.version_id = :authoringVersion and cmax.entity_id = c2.entity_id) "
            + " and (c1.name <> c2.name or c1.active <> c2.active) "
            + "UNION ALL  "
            + "/*MapSet */"
            + "select c1.entity_id from concept c1 "
            + " where c1.kind = 'M' AND c1.codesystem_id = (SELECT id FROM codesystem WHERE name = 'VHAT') "
            + " AND c1.id = (SELECT MAX (cmax.id) FROM concept cmax WHERE cmax.kind = 'M' AND cmax.version_id <= :authoringVersion AND cmax.entity_id = c1.entity_id) "
            + "union all "
            + "select c1.entity_id from concept c1 where c1.kind = 'C' and c1.codesystem_id = (select id from codesystem where name = 'VHAT')  "
            + " and (select max(cmax.id) from concept cmax where cmax.kind = 'C' and cmax.version_id < :authoringVersion and cmax.entity_id = c1.entity_id) is null "
            + " and c1.id = (select max(cmax.id) from concept cmax where cmax.kind = 'C' and cmax.version_id = :authoringVersion and cmax.entity_id = c1.entity_id) "
            + "union all "
            + "/* Property */ "
            + "select c.entity_id from property p1, property p2, concept c where p1.entity_id = p2.entity_id "
            + " and c.kind = 'C' and c.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and c.entity_id = p1.conceptentity_id "
            + " and p1.id = (select max(pmax.id) from property pmax where pmax.version_id < :authoringVersion and pmax.entity_id = p1.entity_id) "
            + " and p2.id = (select max(pmax.id) from property pmax where pmax.version_id = :authoringVersion and pmax.entity_id = p2.entity_id) "
            + " and (p1.property_value <> p2.property_value or p1.active <> p2.active) "
            + "union all "
            + "select distinct c.entity_id from property p1, concept c where c.kind = 'C'  "
            + " and c.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and c.entity_id = p1.conceptentity_id "
            + " and (select max(pmax.id) from property pmax where pmax.version_id < :authoringVersion and pmax.entity_id = p1.entity_id) is null "
            + " and p1.id = (select max(pmax.id) from property pmax where pmax.version_id = :authoringVersion and pmax.entity_id = p1.entity_id) "
            + " and p1.active = 1 "
            + "union all "
            + "/* designation  */ "
            + "select dr.source_entity_id from relationship dr where dr.kind = 'D' and dr.target_entity_id in  "
            + "(select d1.entity_id from concept d1, concept d2 where d1.kind = 'D' and d2.kind = 'D' and d1.entity_id = d2.entity_id "
            + " and d1.codesystem_id = (select id from codesystem where name = 'VHAT') and d2.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and d1.id = (select max(dmax.id) from concept dmax where dmax.kind = 'D' and dmax.version_id < :authoringVersion and dmax.entity_id = d1.entity_id) "
            + " and d2.id = (select max(dmax.id) from concept dmax where dmax.kind = 'D' and dmax.version_id = :authoringVersion and dmax.entity_id = d2.entity_id) "
            + " and (d1.active <> d2.active or d1.type_id <> d2.type_id) "
            + "union all "
            + "select d1.entity_id from concept d1 where d1.kind = 'D' and d1.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and (select max(dmax.id) from concept dmax where dmax.kind = 'D' and dmax.version_id < :authoringVersion and dmax.entity_id = d1.entity_id) is null "
            + " and d1.id = (select max(dmax.id) from concept dmax where dmax.kind = 'D' and dmax.version_id = :authoringVersion and dmax.entity_id = d1.entity_id)) "
            + "union all "
            + "/* relationship */ "
            + "select r1.source_entity_id from relationship r1, relationship r2 where r1.kind = 'C' and r2.kind = 'C' and r1.entity_id = r2.entity_id "
            + " and r1.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'C' and rmax.version_id < :authoringVersion and rmax.entity_id = r1.entity_id) "
            + " and r2.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'C' and rmax.version_id = :authoringVersion and rmax.entity_id = r2.entity_id) "
            + " and (r1.active <> r2.active or r1.TARGET_ENTITY_ID <> r2.TARGET_ENTITY_ID) "
            + "union all "
            + "select distinct r1.source_entity_id from relationship r1 where r1.kind = 'C'  "
            + " and (select max(rmax.id) from relationship rmax where rmax.kind = 'C' and rmax.version_id < :authoringVersion and rmax.entity_id = r1.entity_id) is null "
            + " and r1.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'C' and rmax.version_id = :authoringVersion and rmax.entity_id = r1.entity_id) "
            + " and r1.active = 1 "
            + "union all "
            + "/* designation property */ "
            + "select dr.source_entity_id from relationship dr where dr.kind = 'D' and dr.target_entity_id in  "
            + "(select des.entity_id from property p1, property p2, concept des where p1.entity_id = p2.entity_id "
            + " and des.kind = 'D' and des.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and des.entity_id = p1.conceptentity_id "
            + " and p1.id = (select max(pmax.id) from property pmax where pmax.version_id < :authoringVersion and pmax.entity_id = p1.entity_id) "
            + " and p2.id = (select max(pmax.id) from property pmax where pmax.version_id = :authoringVersion and pmax.entity_id = p2.entity_id) "
            + " and (p1.property_value <> p2.property_value or p1.active <> p2.active) "
            + "union all "
            + "select distinct des.entity_id from property p1, concept des where des.kind = 'D'  "
            + " and des.codesystem_id = (select id from codesystem where name = 'VHAT') "
            + " and des.entity_id = p1.conceptentity_id "
            + " and (select max(pmax.id) from property pmax where pmax.version_id < :authoringVersion and pmax.entity_id = p1.entity_id) is null "
            + " and p1.id = (select max(pmax.id) from property pmax where pmax.version_id = :authoringVersion and pmax.entity_id = p1.entity_id) "
            + " and p1.active = 1 ) "
            + "union all "
            + "/* subset relationship */ "
            + "select dr.source_entity_id from relationship dr where dr.kind = 'D' and dr.target_entity_id in  "
            + "(select r1.target_entity_id from relationship r1, relationship r2 where r1.kind = 'S' and r2.kind = 'S' and r1.entity_id = r2.entity_id "
            + " and r1.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'S' and rmax.version_id < :authoringVersion and rmax.entity_id = r1.entity_id) "
            + " and r2.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'S' and rmax.version_id = :authoringVersion and rmax.entity_id = r2.entity_id) "
            + " and (r1.active <> r2.active) "
            + "union all "
            + "select distinct r1.target_entity_id from relationship r1 where r1.kind = 'S'  "
            + " and (select max(rmax.id) from relationship rmax where rmax.kind = 'S' and rmax.version_id < :authoringVersion and rmax.entity_id = r1.entity_id) is null "
            + " and r1.id = (select max(rmax.id) from relationship rmax where rmax.kind = 'S' and rmax.version_id = :authoringVersion and rmax.entity_id = r1.entity_id) " 
            +  "and r1.active = 1) "
            + ") " 
            + "entityTable right outer join conceptstate cs on cs.concept_entity_id = entityTable.entity_id where cs.state_id not in (select id from state where type = 'Final') and entityTable.entity_id is null) ";


        Query query = session.createSQLQuery(sql);
        query.setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID);
        return query.executeUpdate();
    }
}
