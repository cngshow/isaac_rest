package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.SDOResultDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class ServicesTEdDao extends BaseDao
{
	
	public static List<SDOResultDTO> searchForDesignation(String searchString, long codeSystemId, long versionId, String perferrredDesignationType, int maxSearchResults, boolean searchDesignation)
			throws STSException 
	{
		Session session = HibernateSessionFactory.currentSession();
        StringBuffer queryBuffer = new StringBuffer();

        queryBuffer.append("select * from ( " +
				"select con.entity_id , con.code, des.name desName, desType.name desTypeName, des.active " +
		        "from Concept des, Concept con, Relationship dr, Type desType " +
		        "where des.KIND = 'D' and con.KIND = 'C' and dr.KIND = 'D' " +
		        "  and con.ENTITY_ID = dr.SOURCE_ENTITY_ID " +
		        "  and dr.TARGET_ENTITY_ID = des.ENTITY_ID " +
		        "  and desType.id = des.type_id " +
		        "  and des.CODESYSTEM_ID = :codeSystemId " +
		        "  AND dr.ACTIVE = 1 " +
		        "  AND dr.ID = (select max(drmax.id) from Relationship drmax " +
		        " 				where drmax.version_id <= :versionId " +
		        " 				  and drmax.KIND = 'D' " +
		        " 				  and dr.entity_id = drmax.entity_id) " +
		        "  AND con.ID = (select max(conmax.id) from Concept conmax " +
		        " 				  where conmax.version_id <= :versionId " +
		        " 					and conmax.KIND = 'C'  " +
		        "					and conmax.codesystem_id = :codeSystemId " +
		        " 					and con.entity_id = conmax.entity_id) " +
		        /*Do not check for inactive on designation because we want both active and inactive*/
		        "  AND des.ID = (select max(desmax.id) from Concept desmax " +
		        " 				  where desmax.version_id <= :versionId " + 
		        " 					and desmax.KIND = 'D' " +
		        "					and desmax.codesystem_id = :codeSystemId " +
		        "					and desmax.type_id = (select id from type where name = :type)" +		        
		        " 					and des.entity_id = desmax.entity_id) "
        	); 
        	
        	if(searchDesignation)
        	{
        		//add opening parentheses for sub-queries
    			queryBuffer.append(" and ");
    			queryBuffer.append(" CONTAINS(des.name, CONCAT(:searchString, '%')) > 0 ");
    			queryBuffer.append(" order by upper(des.name))");
        	}
        	else 
        	{
        		queryBuffer.append(" AND UPPER(con.CODE) LIKE UPPER(CONCAT(:searchString, '%'))");
        		queryBuffer.append("order by upper(con.code))");
        	}
        	
		// limit the number of results - if necessary
		if (maxSearchResults > 0)
		{
			 queryBuffer.append("where rownum <= :maxSearchResults ");
		}
		
		SQLQuery sqlQuery = session.createSQLQuery(queryBuffer.toString());
		sqlQuery.setLong("codeSystemId", codeSystemId);
		sqlQuery.setLong("versionId", versionId);
		sqlQuery.setString("searchString", searchString);
		sqlQuery.setString("type", perferrredDesignationType);
		if (maxSearchResults > 0)
		{
			sqlQuery.setLong("maxSearchResults", maxSearchResults);
		}
		
		List<Object[]> list = null;
		try
		{
			list = (List<Object[]>)sqlQuery.list();
		}
		catch (Exception e)
		{
            e.printStackTrace();
			throw new STSException("Invalid search string ("+e.getMessage()+")");
		}

		List<SDOResultDTO> results = new ArrayList<SDOResultDTO>();
		for (Object[] object : list)
		{
		    long entityId = ((BigDecimal)object[0]).longValue();
		    String conceptCode = (String)object[1];
		    String designationName = (String)object[2];
		    String designationTypeName = (String)object[3];
		    int status = ((BigDecimal)object[4]).intValue();
		    SDOResultDTO result = new SDOResultDTO( entityId, conceptCode, designationName, designationTypeName, status);
			results.add(result);
		}

		return results;
	}
	
}
