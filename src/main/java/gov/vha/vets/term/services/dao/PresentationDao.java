/**
 * 
 */
package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.ConceptSummaryDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * @author BORG
 * 
 */
public class PresentationDao
{
    public static List<ConceptSummaryDTO> getConceptSummary(List<String> subsets, List<String> states)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "Select distinct con.entityId, con.name, state.name from Subset subset, SubsetRelationship sr, Designation des, "
                + "DesignationRelationship dr, CodedConcept con, ConceptState cs, State state "
                + "where dr.sourceEntityId = con.entityId and dr.targetEntityId = des.entityId  "
                + "and des.entityId = sr.targetEntityId and sr.sourceEntityId = subset.entityId "
                + "and dr.id = (select max(dr2.id) from DesignationRelationship dr2 where dr2.entityId = dr.entityId) "
                + "and subset.id = (select max(s.id) from Subset s where s.entityId = subset.entityId) "
                + "and cs.state.id = state.id "
                + "and des.id = (select max(d.id) from Concept d where d.entityId = des.entityId) "
                + "and con.id = (select max(cc.id) from CodedConcept cc where cc.entityId = con.entityId) "
                + "and cs.conceptEntityId = con.entityId and cs.state.id in (select s.id from State s where name in (:states)) "
                + "and subset.name in (:subsets) ";

        Query query = session.createQuery(hql);
        query.setParameterList("states", states);
        query.setParameterList("subsets", subsets);
        List<Object[]> list = query.list();
        List<ConceptSummaryDTO> results = new ArrayList<ConceptSummaryDTO>();
        for (Object[] data : list)
        {
            ConceptSummaryDTO summary = new ConceptSummaryDTO();
            summary.setConceptId((Long) data[0]);
            summary.setDesignationName((String) data[1]);
            summary.setState((String) data[2]);
            results.add(summary);
        }

        return results;
    }
    
    public static List<ConceptSummaryDTO> getConceptSummariesNotInDomains(List<String> subsets, List<String> states) throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();

        String query =
	        "select distinct con.entity_Id as con_entity_id, con.name as con_name, state.name as state " +
        	"  from concept con inner join relationship dr on con.entity_id = dr.source_entity_id and con.kind = 'C' and dr.kind = 'D'" +
        	"      and con.id = (select max(conmax.id) from concept conmax where conmax.entity_id = con.entity_id)" +
        	"      and dr.id = (select max(drmax.id) from relationship drmax where drmax.entity_id = dr.entity_id)  " +
	        "    inner join concept des on dr.target_entity_id = des.entity_id and des.kind = 'D'" +
	        "      and des.id = (select max(desmax.id) from concept desmax where desmax.entity_id = des.entity_id)" +
	        "    inner join ConceptState cs on cs.concept_entity_id = con.entity_id" +
	        "      and cs.state_id in (select s.id from State s where name in (:states))" +
	        "    inner join State state on state.id = cs.state_id" +
	        "    left outer join relationship sr on des.entity_id = sr.target_entity_id and sr.kind = 'S'" +
	        "    left outer join concept sub on sr.source_entity_id = sub.entity_id and sub.kind = 'S' " +
	        "  where sub.name is null or sub.name not in (:subsets)";
        
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setParameterList("states", states);
        sqlQuery.setParameterList("subsets", subsets);
        List<Object[]> list = (List<Object[]>)sqlQuery.list();
        List<ConceptSummaryDTO> results = new ArrayList<ConceptSummaryDTO>();
        for(Object[] object : list)
        {
            long entity = ((BigDecimal)object[0]).longValue();
            String name = (String)object[1];
            String state = (String)object[2];
            ConceptSummaryDTO summary = new ConceptSummaryDTO();
            summary.setConceptId(entity);
            summary.setDesignationName(name);
            summary.setState(state);
            results.add(summary);
        }

        return results;
    }

	public static List<ConceptSummaryDTO> getConceptSummariesInMapsets(List<String> states) throws STSException
	{
        String statesString = states.toString().replace('[', '\'').replace(']', '\'').replace(", ", "', '");

        String query = "Select con.entity_id, des.name, state.name as state " +
        		"from Concept con, Concept des, Relationship dr, CodeSystem codeSystem, ConceptState cs, State state " +
	        	"where con.kind = 'M' and des.kind = 'D' and dr.kind = 'D' and des.active = 1 " + 
	        	"and codeSystem.id = con.codeSystem_id and codeSystem.name = ? " +
    	        "and dr.source_Entity_Id = con.entity_Id and dr.target_Entity_Id = des.entity_Id " +
                "and des.type_id = codeSystem.preferred_Designation_Type_id " +
	        	"and cs.state_id = state.id " +
	        	"and con.id = (select max(cc.id) from Concept cc where cc.kind = 'M' and cc.entity_Id = con.entity_Id) " +
		        "and des.id = (select max(d.id) from Concept d where d.kind = 'D' and d.entity_Id = des.entity_Id) " +
	            "and dr.id = (select max(dr2.id) from Relationship dr2 where dr2.kind = 'D' and dr2.entity_Id = dr.entity_Id) " +
	        	"and cs.concept_Entity_Id = con.entity_Id and cs.state_id in " +
	        	"(select s.id from State s where name in ("+statesString+")) " +
	        	"order by con.name";
		
        List<ConceptSummaryDTO> results = new ArrayList<ConceptSummaryDTO>();
		try
		{
			Session session = HibernateSessionFactory.currentSession();
	        Connection connection = session.connection();
	        PreparedStatement statement = connection.prepareStatement(query);
	        statement.setString(1, HibernateSessionFactory.VHAT_NAME);
			ResultSet resultSet = statement.executeQuery();
		    while (resultSet.next())
			{
		        long entity = resultSet.getLong("entity_id");
		        String name = resultSet.getString("name");
		        String state = resultSet.getString("state");
		        ConceptSummaryDTO summary = new ConceptSummaryDTO();
		        summary.setConceptId(entity);
		        summary.setDesignationName(name);
		        summary.setState(state);
		        results.add(summary);
		    }
		} 
		catch (SQLException e)
		{
			throw new STSException(e);
		}

        return results;
	}
}
