package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.DesignationDelegate;
import gov.vha.vets.term.services.business.MapSetDelegate;
import gov.vha.vets.term.services.business.ServicesBrowserDelegate;
import gov.vha.vets.term.services.business.VersionDelegate;
import gov.vha.vets.term.services.dto.BrowseGemResultDTO;
import gov.vha.vets.term.services.dto.BrowseMappingHeaderDTO;
import gov.vha.vets.term.services.dto.BrowseMappingResultDTO;
import gov.vha.vets.term.services.dto.MapEntryVersionDetailDTO;
import gov.vha.vets.term.services.dto.SDOResultDTO;
import gov.vha.vets.term.services.dto.SearchResultDTO;
import gov.vha.vets.term.services.dto.VHATResultDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class ServicesBrowserDao extends BaseDao
{
    /**
     * Get search results based on parameters selected by the user.
     * @param searchString
     * @param selectedVersion
     * @param selectedSubsets
     * @param searchVUID
     * @param searchConceptName
     * @param searchDesignationName
     * @param searchPropertyName
     * @param searchPropertyValue
     * @param searchRelationshipName
     * @param searchRelationshipValue
     * @return
     */
	@SuppressWarnings("unchecked")
	public static SearchResultDTO searchVHAT(String searchString, Version selectedVersion,
	        List<Subset> selectedSubsets, boolean includeNonSubset, boolean searchVUID, boolean searchConceptName,
	        boolean searchDesignationName, boolean searchPropertyName, boolean searchPropertyValue, boolean searchRelationshipName,
	        boolean searchRelationshipValue, int maxSearchResults, String selectedFilterByOption) throws STSException
	{
    	Session session = HibernateSessionFactory.currentSession();
        StringBuffer queryBuffer = new StringBuffer();
        boolean showAllVHAT = false;

        // check to see if user wants to see all VHAT results
        if (searchString.isEmpty())
        {
            showAllVHAT = true;
        }
        else
        {
            searchString = applyRulesToSearchString(searchString, selectedFilterByOption);
        }

		queryBuffer.append("select * from " +
				"(select max(rownumber) over () totalrows, entity_id, conVuid, desVuid, desName, typeName, active from " +
				"(select rownum rownumber, entity_id, conVuid, desVuid, desName, typeName, active from  " +
				"((select * from (");

		int loopCount = 0;
		while (loopCount <= 1)
		{
			if (loopCount == 0)
			{
				if (selectedSubsets.size() == 0)
				{
					loopCount++;
					continue;
				}
			}
			else
			{
				if (includeNonSubset == false)
				{
					break;
				}
			}
				
	        queryBuffer.append( "(SELECT con.entity_id, con.vuid conVuid, des.vuid desVuid, des.name desName, type.name typeName, des.active " +
			        "FROM Concept des, " +  
			        "     Concept con, " +
			        "     Relationship dr," +
			        "     Type type " +
			        "where des.kind = 'D' " +
			        "  AND con.kind = 'C' " +
			        "  AND dr.kind = 'D' " +
			        "  AND con.entity_id = dr.source_entity_id " +
			        "  AND dr.target_entity_id = des.entity_id " +
			        "  AND des.type_id = type.id " +
			        "  AND des.codesystem_id = (SELECT cs.id FROM Codesystem cs where cs.name = '" + HibernateSessionFactory.VHAT_NAME + "') " + 
	        		"  AND con.codesystem_id = (SELECT cs.id FROM Codesystem cs where cs.name = '" + HibernateSessionFactory.VHAT_NAME + "') " +
			        "  AND con.id = (SELECT max(conmax.id) FROM Concept conmax " +
			        " 				  WHERE conmax.kind = 'C' " +
			        "                   AND conmax.codesystem_id = (select cs.ID from Codesystem cs where cs.NAME = '" + HibernateSessionFactory.VHAT_NAME + "')" +
			        "                   AND conmax.version_id <= :versionId " +
			        " 					AND con.entity_id = conmax.entity_id) " +
			        /*Do not check for inactive on designation because we want both active and inactive*/
			        "  AND des.id = (SELECT max(desmax.id) FROM Concept desmax " +
			        " 				  WHERE desmax.kind = 'D' " +
			        "                   AND desmax.codesystem_id = (select cs.ID from Codesystem cs where cs.NAME = '" + HibernateSessionFactory.VHAT_NAME + "')" +
			        "                   AND desmax.version_id <= :versionId " +
			        " 					AND des.entity_id = desmax.entity_id) " +
			        "  AND dr.active = 1 " +
			        "  AND dr.id = (SELECT max(drmax.id) FROM Relationship drmax " +
			        " 	             WHERE drmax.version_id <= :versionId " +
			        "                  AND dr.entity_id = drmax.entity_id) ");

	        if (loopCount == 0)
	        {
	        	queryBuffer.append(
	        		"  and des.entity_id IN " +
			        "           (SELECT sr.target_entity_id " +
			        "            FROM Concept subset, Relationship sr " +
			        "            WHERE subset.kind = 'S' " +
			        "              AND sr.kind = 'S' " +
			        "              AND subset.entity_id = sr.source_entity_id " +
			        "              AND subset.entity_id IN (:subsetIds) " + // Subset ID variables
			        "              AND subset.active = 1 " +
			        "              AND subset.id = (SELECT max(subsetmax.ID) from Concept subsetmax " +
			        "								  WHERE subsetmax.version_id <= :versionId " + //version variable
			        "								    AND subsetmax.KIND = 'S' " +
			        " 									AND subset.entity_id = subsetmax.entity_id) " +
			        "              AND sr.active = 1 " +
			        "              AND sr.id = (SELECT max(srmax.ID) from Relationship srmax " +
			        "							  WHERE srmax.version_id <= :versionId " + //version variable
			        "							    AND srmax.kind = 'S' " +
			        " 								and sr.entity_id = srmax.entity_id)  ) ");
	        }
	        else
	        {
	        	queryBuffer.append(
		        	"  AND (SELECT DISTINCT sr.target_entity_id FROM relationship sr " +
				    "         WHERE sr.kind = 'S' and sr.active = 1 AND sr.target_entity_id = des.entity_id " +
				    "           AND sr.id IN " +
				    "              (SELECT MAX(srmax.id) id FROM relationship srmax" +
				    "                   WHERE srmax.kind = 'S' " +
				    "                     AND srmax.target_entity_id = des.entity_id " +
				    "                     AND srmax.version_id <= :versionId " + 
				    "                  GROUP BY srmax.source_entity_id))" +
				    "       IS NULL ");
	        }
	        
	        if (showAllVHAT == false && (searchConceptName || searchDesignationName || searchVUID))
	        {
	    		//add opening parentheses for sub-queries
	    		queryBuffer.append(" and ( ");
	
	    		StringBuffer searchFilterQuery = new StringBuffer();
	    		boolean hasSearchFilter = false;
	    		if (searchConceptName == true)
	    		{
	    			searchFilterQuery.append(getFilterByOptionQuery("con.name", selectedFilterByOption));
	    			hasSearchFilter = true;
	    		}
	    		
	    		if (searchDesignationName == true)
	    		{
	    			searchFilterQuery.append(hasSearchFilter ? " OR " : "");
	    			searchFilterQuery.append(getFilterByOptionQuery("des.name", selectedFilterByOption));
	    			hasSearchFilter = true;
	    		}
	
	    		if (searchVUID == true)
	    		{
	    			searchFilterQuery.append(hasSearchFilter ? " OR " : "");
	    			searchFilterQuery.append(getFilterByOptionQuery("des.vuid", selectedFilterByOption) + " OR " + getFilterByOptionQuery("con.vuid", selectedFilterByOption));
	    			hasSearchFilter = true;
	    		}
	
	    		queryBuffer.append(searchFilterQuery.toString());
	        }
	        
	        // CCR 1197 - Always filter when processing a property or relationship regardless of showAllVHAT
	        List<String> subQueries = getVHATSubqueries(selectedFilterByOption, searchPropertyName, searchPropertyValue, 
	                                                    searchRelationshipName, searchRelationshipValue, showAllVHAT);

	    	int subQueriesSize = subQueries.size();
	    	if (subQueriesSize > 0)
	    	{
	        	//add opening parentheses for sub-queries
	    		if (searchConceptName || searchDesignationName || searchVUID)
	    		{
	            	queryBuffer.append(" OR des.ENTITY_ID in ( ");
	    		}
	    		else
	    		{
	            	queryBuffer.append(" AND des.ENTITY_ID in ( ");
	    		}
	        	
	        	//If there's only one sub query, this loop doesn't execute
	        	for(int i = 0; i < subQueriesSize - 1; i++)
	        	{
	        		String subQuery = subQueries.get(i);
	        		queryBuffer.append(subQuery);
	        		queryBuffer.append(" UNION ");
	        	}
	        	
	        	queryBuffer.append(subQueries.get(subQueriesSize - 1));
	        	
	        	//add closing parentheses for sub-queries
	        	queryBuffer.append(" ) ");
	    	}
	
	    	if (showAllVHAT == false && (searchConceptName || searchDesignationName || searchVUID))
	        {
	        	queryBuffer.append(")");
	        }

			if (loopCount == 0 && includeNonSubset == true)
			{
				queryBuffer.append(") UNION ");
			}
			else
			{
				queryBuffer.append(" ");
			}
    		loopCount++;
		}
		
		queryBuffer.append(")) ) order by upper(desname) )");
		if (showAllVHAT == false && searchDesignationName == true)
		{
			if (selectedFilterByOption.equals(ServicesBrowserDelegate.FILTER_OPTION_CONTAINS) == false)
			{
				queryBuffer.append(" where ");
				queryBuffer.append(getFilterByOptionQuery("desName", selectedFilterByOption));
			}
		}
		queryBuffer.append("))");

		// limit the number of results - if necessary
		if (maxSearchResults > 0)
		{
			queryBuffer.append(" where rownum <= :maxSearchResults ");
		}

        queryBuffer.append(" order by upper(desName) ");
        
        String completeQuery = queryBuffer.toString();
        
		SQLQuery sqlQuery = session.createSQLQuery(completeQuery);
		sqlQuery.setLong("versionId", (selectedVersion != null) ? selectedVersion.getId() : 0);
		if (selectedSubsets.size() > 0)
		{
			List<Long> subsetIdList = getSubsetIds(selectedSubsets);
			sqlQuery.setParameterList("subsetIds", subsetIdList);
		}
        
		if (showAllVHAT == false)
		{
		    sqlQuery.setString("searchString", searchString);
		}

		if (maxSearchResults > 0)
		{
			sqlQuery.setLong("maxSearchResults", maxSearchResults);
		}
		
		List<Object[]> list = null;
		long startTime = System.currentTimeMillis();
		long totalTime = 0L;
		try
		{
			list = (List<Object[]>)sqlQuery.list();
			totalTime = System.currentTimeMillis() - startTime;
		}
		catch (Exception e)
		{
            e.printStackTrace();
			throw new STSException("Invalid search string ("+e.getMessage()+")");
		}
        
		int totalRowCount = 0;
        List<VHATResultDTO> results = new ArrayList<VHATResultDTO>();
        for(Object[] object : list)
        {
			totalRowCount = ((BigDecimal)object[0]).intValue();
        	results.add(new VHATResultDTO(((BigDecimal)object[1]).longValue(), ((BigDecimal)object[2]).longValue(),
        			((BigDecimal)object[3]).longValue(), (String)object[4], (String)object[5], ((BigDecimal)object[6]).intValue()));
        }
        
		return new SearchResultDTO(results, totalRowCount, totalTime);
    }

	/*
     * Get a list of Subset IDs we can use in the query.
     */
	private static List<Long> getSubsetIds(List<Subset> subsets)
	{
		List<Long> subsetIdList = new ArrayList<Long>();
        
		for (Subset subset : subsets)
		{
			subsetIdList.add(subset.getId());
		}

		return subsetIdList;
	}
	
	/*
	 * Get all the sub-queries we need, based on search parameters the user selected
	 */
	private static List<String> getVHATSubqueries(String selectedFilterByOption, boolean searchPropertyName, boolean searchPropertyValue,
			boolean searchRelationshipName, boolean searchRelationshipValue, boolean showAllVHAT)
	{
		List<String> subQueries = new ArrayList<String>();
		
		if(searchPropertyName == true)
		{
		    StringBuffer subQueryText = new StringBuffer();
		    subQueryText.append(" ( " +
        			"select des2.ENTITY_ID " +
        			"from Concept des2,  " +
        			"     Property prop2,  " +
        			"     Type t " +
        			"where des2.KIND = 'D'  " +
        			"  and prop2.CONCEPTENTITY_ID = des2.ENTITY_ID  " +
        			"  and prop2.PROPERTYTYPE_ID = t.ID  ");
		    // CCR 1197 - Search all skips the search string match but uses the rest of the SQl to filter
		    if (!showAllVHAT) {
		        subQueryText.append(
					"  and " + getFilterByOptionQuery("t.name", selectedFilterByOption));
		    }
		    
		    subQueryText.append("  and des2.ACTIVE = 1 " +
        			"  and des2.ID = (select max(desmax.id) from Concept desmax " +
        			"                 where desmax.KIND = 'D' " +
        			"                   and desmax.version_id <= :versionId " + //version variable
        			"                   and des2.entity_id = desmax.entity_id) " +
        			"  and prop2.id = (select max(propmax.id) from Property propmax " +
        			"                 where propmax.version_id <= :versionId " +
        			"                   and prop2.entity_id = propmax.entity_id) " +
        			"UNION  " +
        			"select dr2.TARGET_ENTITY_ID " +
        			"from Relationship dr2, " +
        			"     Property prop2,  " +
        			"     Type t " +
        			"where dr2.KIND = 'D'  " +
        			"  and con.ENTITY_ID = dr2.SOURCE_ENTITY_ID  " +
        			"  and prop2.CONCEPTENTITY_ID = con.ENTITY_ID  " +
        			"  and prop2.PROPERTYTYPE_ID = t.ID  ");
	        // CCR 1197 
            if (!showAllVHAT) {
                subQueryText.append(
					"  and " + getFilterByOptionQuery("t.name", selectedFilterByOption));
            }
            
            subQueryText.append("  and dr2.active = 1 " +
        			"  and dr2.ID = (select max(drmax.id) from Relationship drmax " +
        			"                 where drmax.KIND = 'D' " +
        			"                   and drmax.version_id <= :versionId " + //version variable
        			"                   and dr2.entity_id = drmax.entity_id) " +
        			"  and prop2.id = (select max(propmax.id) from Property propmax " +
        			"                  where propmax.version_id <= :versionId " +
        			"                    and prop2.entity_id = propmax.entity_id) " +
        			" ) ");
            
		    subQueries.add(subQueryText.toString());
		}
		
		if(searchPropertyValue == true)
		{
            StringBuffer subQueryText = new StringBuffer();
            subQueryText.append(" ( " +
					"select des2.ENTITY_ID  " +
					"  from Concept des2,  " +
					"       Property prop2  " +
					" where des2.KIND = 'D'  " +
					"   and prop2.conceptentity_id = des2.entity_id  ");
            // CCR 1197 
			if (!showAllVHAT) {
			    subQueryText.append(
					"   and " + getFilterByOptionQuery("prop2.PROPERTY_VALUE", selectedFilterByOption));
			}
			subQueryText.append("   and des2.ACTIVE = 1  " +
					"   and des2.ID = (select max(desmax.id) from Concept desmax  " +
					"                 where desmax.KIND = 'D'  " +
					"                   and desmax.version_id <= :versionId " + //version variable
					"                   and des2.entity_id = desmax.entity_id) " +
        			"   and prop2.id = (select max(propmax.id) from Property propmax " +
        			"                   where propmax.version_id <= :versionId " +
        			"                     and prop2.entity_id = propmax.entity_id) " +
					"UNION  " +
					"select dr2.TARGET_ENTITY_ID " +
					"  from Relationship dr2,  " +
					"       Property prop2  " +
					" where dr2.KIND = 'D'  " +
        			"   and con.ENTITY_ID = dr2.SOURCE_ENTITY_ID  " +
					"   and prop2.CONCEPTENTITY_ID = con.ENTITY_ID  ");
	         // CCR 1197 
		    if (!showAllVHAT) {
		        subQueryText.append(
					"   and " + getFilterByOptionQuery("prop2.PROPERTY_VALUE", selectedFilterByOption));
		    }
		    subQueryText.append("   and dr2.active = 1 " +
					"   and dr2.ID = (select max(drmax.id) from Relationship drmax  " +
					"                 where drmax.KIND = 'D'  " +
					"                   and drmax.version_id <= :versionId " + //version variable
					"                   and dr2.entity_id = drmax.entity_id)  " +
        			"   and prop2.id = (select max(propmax.id) from Property propmax " +
        			"                   where propmax.version_id <= :versionId " +
        			"                     and prop2.entity_id = propmax.entity_id) " +
					" ) ");
            
            subQueries.add(subQueryText.toString());
		}
		
		if(searchRelationshipName == true)
		{
            StringBuffer subQueryText = new StringBuffer();
            subQueryText.append(" ( " +
					"select dr2.TARGET_ENTITY_ID  " +
					"  from Relationship cr2,  " +
					"       Relationship dr2,  " +
					"       Type t " +
					" where cr2.KIND = 'C'  " +
					"   and dr2.KIND = 'D'  " +
					"   and cr2.source_entity_id = con.entity_id  " +
					"   and dr2.source_entity_id = con.entity_id  " +
					"   and cr2.TYPE_ID = t.ID  ");
            // CCR 1197 
			if (!showAllVHAT) {
			    subQueryText.append(
					"   and " + getFilterByOptionQuery("t.name", selectedFilterByOption));
			}
			subQueryText.append("   and cr2.ACTIVE = 1 " +
					"   and dr2.ACTIVE = 1 " +
					"   and cr2.ID = (select max(crmax.id) from Relationship crmax " +
					"                 where crmax.KIND = 'C' " +
					"                 and crmax.VERSION_ID <= :versionId " + //version variable
					"                 and cr2.entity_id = crmax.entity_id) " +
					"   and dr2.ID = (select max(drmax.id) from Relationship drmax " +
					"                 where drmax.KIND = 'D' " +
					"                 and drmax.VERSION_ID <= :versionId" + //version variable
					"                 and dr2.entity_id = drmax.entity_id) "+
					" ) ");
			
	         subQueries.add(subQueryText.toString());
		}
		
		if(searchRelationshipValue == true)
		{
            StringBuffer subQueryText = new StringBuffer();
            subQueryText.append(" ( " +
					"select dr2.TARGET_ENTITY_ID  " +
					"  from Relationship cr2, " +
					"       Concept conValue, " +
					"       Relationship dr2 " +
					" where cr2.kind = 'C'  " +
					"   and conValue.kind = 'C' " +
					"   and dr2.kind = 'D'  " +
					"   and cr2.source_entity_id = con.entity_id  " +
					"   and dr2.source_entity_id = con.entity_id  " +
					"   and cr2.target_entity_id = conValue.entity_id  ");
            // CCR 1197 
		    if (!showAllVHAT) {
		        subQueryText.append(
					"   and " + getFilterByOptionQuery("conValue.name", selectedFilterByOption));
		    }
		    subQueryText.append("   and cr2.active = 1 " +
			        "   and conValue.active = 1 " +
					"   and dr2.active = 1 " +
					"   and cr2.id = (select max(crmax.id) from Relationship crmax " +
					"                 where crmax.KIND = 'C' " +
					"                   and crmax.version_id <= :versionId " + //version variable
					"                   and cr2.entity_id = crmax.entity_id) " +
			        "   and conValue.ID = (select max(conmax.id) from Concept conmax " +
			        " 				   where conmax.version_id <= :versionId " +  //version variable
			        " 					 and conmax.kind = 'C'  " +
			        " 					 and conValue.entity_id = conmax.entity_id) " +
					"   and dr2.id = (select max(drmax.id) from Relationship drmax " +
					"                 where drmax.KIND = 'D' " +
					"                   and drmax.version_id <= :versionId " + //version variable
					"                   and dr2.entity_id = drmax.entity_id) " +
					" ) ");
			
	         subQueries.add(subQueryText.toString());
		}
		
		return subQueries;
	}
	
	@SuppressWarnings("unchecked")
	public static SearchResultDTO searchSDO(String searchString, CodeSystem codeSystem, Version version, boolean searchConceptCode,
				boolean searchDesignationName, int maxSearchResults, String selectedFilterByOption)
			throws STSException 
	{
		Session session = HibernateSessionFactory.currentSession();
        StringBuffer queryBuffer = new StringBuffer();

        boolean showAllMappings = false;
        // check to see if user wants to see all Mapping results
        if (searchString.isEmpty())
        {
            showAllMappings = true;
        }
        else
        {
            searchString = applyRulesToSearchString(searchString, selectedFilterByOption);
        }


		queryBuffer.append("select * from " +
				"(select max(rownumber) over () totalrows, entity_id, code, desName, desTypeName, active from " +
				"(select rownum rownumber, entity_id, code, desname, destypename, active from  " +
				"(select * from " +
				"(select con.entity_id , con.code, des.name desName, desType.name desTypeName, des.active " +
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
		        " 					and des.entity_id = desmax.entity_id) "
		       ); 

	    if (!showAllMappings) {
	        //add opening parentheses for sub-queries
	        queryBuffer.append(" and ( ");
	    }

		StringBuffer searchFilterQuery = new StringBuffer();
		boolean hasSearchFilter = false;
		if (!showAllMappings && searchConceptCode == true)
		{
			searchFilterQuery.append(getFilterByOptionQuery("con.code", selectedFilterByOption));
			hasSearchFilter = true;
		}
		
		if (!showAllMappings && searchDesignationName == true)
		{
			searchFilterQuery.append(hasSearchFilter ? " OR " : "");
			searchFilterQuery.append(getFilterByOptionQuery("des.name", selectedFilterByOption));			
			hasSearchFilter = true;
		}

        if (!showAllMappings) {
            queryBuffer.append(searchFilterQuery.toString()).append(")");	// close parentheses for sub-queries
        }

		// close remaining parentheses and order the list before the rownum truncation.
		queryBuffer.append(")) order by upper(desName) ))");

		// limit the number of results - if necessary
		if (maxSearchResults > 0)
		{
			queryBuffer.append(" where rownum <= :maxSearchResults ");
		}

		String completeQuery = queryBuffer.toString();

		SQLQuery sqlQuery = session.createSQLQuery(completeQuery);
		sqlQuery.setLong("codeSystemId", codeSystem.getId());
		sqlQuery.setLong("versionId", version.getId());
		if (!showAllMappings) {
		    sqlQuery.setString("searchString", searchString);
		}
		if (maxSearchResults > 0)
		{
			sqlQuery.setLong("maxSearchResults", maxSearchResults);
		}

		
		List<Object[]> list = null;
		long startTime = System.currentTimeMillis();
		long totalTime = 0L;
		try
		{
			list = (List<Object[]>)sqlQuery.list();
			totalTime = System.currentTimeMillis() - startTime;
		}
		catch (Exception e)
		{
            e.printStackTrace();
			throw new STSException("Invalid search string ("+e.getMessage()+")");
		}

		int totalRowCount = 0;
		List<SDOResultDTO> results = new ArrayList<SDOResultDTO>();
		for (Object[] object : list)
		{
			totalRowCount = ((BigDecimal)object[0]).intValue();
		    long entityId = ((BigDecimal)object[1]).longValue();
		    String conceptCode = (String)object[2];
		    String designationName = (String)object[3];
		    String designationTypeName = (String)object[4];
		    int status = ((BigDecimal)object[5]).intValue();
		    SDOResultDTO result = new SDOResultDTO( entityId, conceptCode, designationName, designationTypeName, status);
			results.add(result);
		}

		return new SearchResultDTO(results, totalRowCount, totalTime);
	}
	
	private static String getFilterByOptionQuery(String columnName, String selectedFilterByOption)
	{
		String query = " upper("+columnName+") LIKE upper(CONCAT(CONCAT('%',:searchString), '%')) ESCAPE '\\'";
		
		if (selectedFilterByOption.equals(ServicesBrowserDelegate.FILTER_OPTION_BEGINS_WITH) == true)
		{
			query = " upper("+columnName+") LIKE upper(CONCAT(:searchString,'%')) ESCAPE '\\'";
		}
		else if (selectedFilterByOption.equals(ServicesBrowserDelegate.FILTER_OPTION_ENDS_WITH) == true)
		{
			query = " upper("+columnName+") LIKE upper(CONCAT('%',:searchString)) ESCAPE '\\'";
		}
		else if (selectedFilterByOption.equals(ServicesBrowserDelegate.FILTER_OPTION_EXACT_MATCH) == true)
		{
			query = " upper("+columnName+") = upper(:searchString)";
		}

		return query;
	}
	
	private static String applyRulesToSearchString( String searchString, 
			String selectedFilterByOption) throws STSException
	{
	    // Note: "Exact match" will make no changes to the searchString because
	    //       The "like" SQL keyword is not used in the query
	    
	    
	    String searchStr = searchString; 
	    
        // note: two backslashes is one backslash in the replacement string.. and replacements are 
        //       done is sequence such that subsequent replacements do not mess up prior replacements
        // note: backslash will be used in the SQL "LIKE" operand as defined as the escape character
	    
	    if (selectedFilterByOption.equalsIgnoreCase(ServicesBrowserDelegate.FILTER_OPTION_EXACT_MATCH) == false)
	    {
            searchStr = searchString.replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
	    }

		
		return searchStr;
	}
	
	@SuppressWarnings("unchecked")
	public static SearchResultDTO searchMappings(String searchString, 
			  boolean searchSourceCode, boolean searchSourceCodeDescription,  boolean searchTargetCode,
			  boolean searchTargetCodeDescription, boolean searchVuid, BrowseMappingHeaderDTO browseMappingHeaderDTO,
			  int maxSearchResults, String selectedFilterByOption)
			  		throws STSException
  {
    	Session session = HibernateSessionFactory.currentSession();

        boolean showAllMappings = false;
        // check to see if user wants to see all Mapping results
        if (searchString.isEmpty())
        {
            showAllMappings = true;
        }
        else
        {
            searchString = applyRulesToSearchString(searchString, selectedFilterByOption);
        }
     
        StringBuffer queryPrimaryText = new StringBuffer(
                "   WHERE me.kind = 'E' and msr.kind = 'M' " +
                "         AND me.id = mee.mapentryid " +
                "         AND msr.source_entity_id = :mapSetEntityId and me.entity_id = msr.target_entity_id " +
                "         AND me.id = (SELECT MAX(meMax.id) from concept meMax " +
                "                       WHERE meMax.kind = 'E' and meMax.version_id <= :versionId " +
                "                         AND meMax.entity_id = me.entity_id)  " + 
                "         AND msr.id = (SELECT MAX(msrMax.id) from relationship msrMax " +
                "                       WHERE msrMax.kind = 'M' and msrMax.version_id <= :versionId " +
                "                         AND msrMax.entity_id = msr.entity_id)  ");


        StringBuffer subQueryBuffer = new StringBuffer();

		if (searchSourceCode || searchTargetCode || searchVuid)
        {
    		StringBuffer searchFilterQuery = new StringBuffer();
    		boolean hasSearchFilter = false;
    		if (!showAllMappings && searchSourceCode == true)
    		{
    			searchFilterQuery.append(getFilterByOptionQuery("mee.sourcecode", selectedFilterByOption));
    			hasSearchFilter = true;
    		}
    		
    		if (!showAllMappings && searchTargetCode == true)
    		{
    			searchFilterQuery.append(hasSearchFilter ? " OR " : "");
    			searchFilterQuery.append(getFilterByOptionQuery("mee.targetcode", selectedFilterByOption));
    			hasSearchFilter = true;
    		}

    		if (!showAllMappings && searchVuid == true)
    		{
    			searchFilterQuery.append(hasSearchFilter ? " OR " : "");
    			searchFilterQuery.append(getFilterByOptionQuery("me.vuid", selectedFilterByOption));
    			hasSearchFilter = true;
    		}

    		subQueryBuffer.append(searchFilterQuery.toString());
        }
		
        String designationQueryText = 
                "    WHERE con.kind = 'C' AND dr.kind = 'D' AND des.kind = 'D' " +
                "      AND con.entity_id = dr.source_entity_id AND dr.target_entity_id = des.entity_id  " +
                "      AND des.type_id = :preferredDesignationType@direction@ " +
                "      AND con.code = mee.@direction@code " +
                "      AND con.codesystem_id = :codeSystemId@direction@ " +
                "      AND con.ID = (SELECT MAX (conMax.ID) FROM concept conMax  WHERE conMax.kind = 'C' AND conMax.code = mee.@direction@code AND conMax.version_id <= :versionId@direction@ AND con.entity_id = conMax.entity_id) " +
                "      AND des.ID = (SELECT MAX (desMax.ID) FROM concept desMax WHERE desMax.kind = 'D' AND desMax.version_id <= :versionId@direction@ AND des.entity_id = desMax.entity_id) " +
                "      AND dr.ID = (SELECT MAX (drMax.ID) FROM relationship drMax WHERE drMax.kind = 'D' AND drMax.version_id <= :versionId@direction@ AND dr.entity_id = drMax.entity_id) ";     
        
        
		
	    
        if (searchSourceCodeDescription || searchTargetCodeDescription)
        {
            if (searchSourceCode || searchTargetCode || searchVuid)
            {
                subQueryBuffer.append(" OR  ");
            }
        }

        String optionQueryText = "";
        if (!showAllMappings)
        {
            optionQueryText = "      AND "+getFilterByOptionQuery("des.name", selectedFilterByOption);
        }
        
        String conceptQuery = 
                " mee.@direction@code IN " + 
                "(SELECT con.code from concept con, concept des, relationship dr " +
                designationQueryText + optionQueryText + " )";     

	    
	    if (searchSourceCodeDescription)
	    {
	      String conceptQuerySource = conceptQuery.replaceAll("@direction@", "Source");
	      subQueryBuffer.append(conceptQuerySource);
	    }
	        
	    if (searchTargetCodeDescription)
	    {
	      if (searchSourceCodeDescription)
	      {
	         subQueryBuffer.append(" OR ");
	      }
	      String conceptQueryTarget = conceptQuery.replaceAll("@direction@", "Target");
	      subQueryBuffer.append(conceptQueryTarget);
	    }

		
        StringBuffer queryBuffer = new StringBuffer();
        
        /*
         * Query the database to get the Search results limited by maxSearchResults
         */
        queryBuffer = new StringBuffer("select * from (select max(rownumber) over () totalrows, " +
                "      mapentryentityid, vuid, sourcecode, targetcode, sourcedescription, targetdescription, sequence, active from " +
                "      (select rownum rownumber, mapentryentityid, vuid, sourcecode, targetcode, sourcedescription, targetdescription, sequence, active from " +
                "      (select * from (select me.entity_id AS mapentryentityid, me.vuid AS vuid, " +
                "      mee.sourcecode AS sourcecode, " +
                "      mee.targetcode AS targetcode, " +
                "      sourcedes.name AS sourcedescription, " +
                "      targetdes.name AS targetdescription, " +
                "      msr.sequence AS sequence, " +
                "      msr.active AS active " +
                "    FROM concept me, mapentryextension mee, relationship msr, concept sourcedes, concept targetdes ");
       
        String sourceDesignationQuery = 
                " AND sourcedes.ID = " + 
                " (SELECT MAX(des.ID) from concept des, concept con, relationship dr " +
                designationQueryText +  " ) ";  
              
        String targetDesignationQuery = 
                " AND targetdes.ID = " + 
                " (SELECT MAX(des.ID) from concept des, concept con, relationship dr " +
                designationQueryText +  " ) ";
              
        queryBuffer.append(queryPrimaryText);
        queryBuffer.append(sourceDesignationQuery.replaceAll("@direction@", "Source"));
        queryBuffer.append(targetDesignationQuery.replaceAll("@direction@", "Target"));
        
        if (subQueryBuffer.length()>0)
        {       
            queryBuffer.append(" AND "+ subQueryBuffer);
        }
        

              
        // add on the sorting criteria and result set size
        queryBuffer.append(") order by sourcedescription, mapentryentityid))) where rownum <= "+new Integer(maxSearchResults).toString());

        SQLQuery sqlQuery = createMappingSQLQuery(session, queryBuffer, 
                browseMappingHeaderDTO, searchString, 
                showAllMappings, true, true); // There will be both source and target designation name retrieval


        List<Object[]>  list = null;
		long startTime = System.currentTimeMillis();
		long totalTime = 0;
		try
		{
			list = (List<Object[]>)sqlQuery.list();
			totalTime = totalTime + System.currentTimeMillis() - startTime;
		}
		catch (Exception e)
		{
            e.printStackTrace();
			throw new STSException("Invalid search string ("+e.getMessage()+")");
		}
        
        List<BrowseMappingResultDTO> results = new ArrayList<BrowseMappingResultDTO>();

        int totalRowCount = 0;
        // get the total row count from the first row
        if (!list.isEmpty()) {
            Object[] firstRow = list.get(0);
            totalRowCount = ((BigDecimal)firstRow[0]).intValue();
        }

        // put query results into results object
        for(Object[] object : list)
        {
    		BrowseMappingResultDTO browseMappingResultDTO = new BrowseMappingResultDTO();
    		browseMappingResultDTO.setMapEntryEntityId(((BigDecimal)object[1]).longValue());
    		browseMappingResultDTO.setVuid(((BigDecimal)object[2]).longValue());
            browseMappingResultDTO.setSourceCode((String)object[3]);
            browseMappingResultDTO.setTargetCode((String)object[4]);
            browseMappingResultDTO.setSourceCodeDescription((String)object[5]);
            browseMappingResultDTO.setTargetCodeDescription((String)object[6]);
			browseMappingResultDTO.setSequence(((BigDecimal)object[7]).intValue());
			if(((BigDecimal)object[8]).intValue() > 0)
			{
				browseMappingResultDTO.setActive("Active");
			}
			else
			{ 
				browseMappingResultDTO.setActive("Inactive");
			}
			
			results.add(browseMappingResultDTO);
        }
        
        return new SearchResultDTO(results, totalRowCount, totalTime);
    }
	
	public static SQLQuery createMappingSQLQuery(Session session, StringBuffer queryBuffer, 
	            BrowseMappingHeaderDTO browseMappingHeaderDTO, String searchString, 
	            boolean showAllMappings, boolean searchSourceCodeDescription,
	            boolean searchTargetCodeDescription) {
	    
        String completeQuery = queryBuffer.toString();

        SQLQuery sqlQuery = session.createSQLQuery(completeQuery);
        if (!showAllMappings)
        {
            sqlQuery.setString("searchString", searchString);
        }
        sqlQuery.setLong("mapSetEntityId", browseMappingHeaderDTO.getMapSet().getEntityId());
        sqlQuery.setLong("versionId", browseMappingHeaderDTO.getSearchVersion().getId());
        if (searchSourceCodeDescription) {
            sqlQuery.setLong("preferredDesignationTypeSource", browseMappingHeaderDTO.getSourceCodeSystem().getPreferredDesignationType().getId());
            sqlQuery.setLong("codeSystemIdSource", browseMappingHeaderDTO.getSourceCodeSystem().getId());
            sqlQuery.setLong("versionIdSource", browseMappingHeaderDTO.getMapSet().getSourceVersionId());
        }
        if (searchTargetCodeDescription) {
            sqlQuery.setLong("preferredDesignationTypeTarget", browseMappingHeaderDTO.getTargetCodeSystem().getPreferredDesignationType().getId());
            sqlQuery.setLong("codeSystemIdTarget", browseMappingHeaderDTO.getTargetCodeSystem().getId());
            sqlQuery.setLong("versionIdTarget", browseMappingHeaderDTO.getMapSet().getTargetVersionId());
        }
        
        sqlQuery.setCacheMode(CacheMode.IGNORE);
	    return sqlQuery;
	}

    @SuppressWarnings("unchecked")
    public static SearchResultDTO searchGemMappings(String searchString, 
              boolean searchSourceCode, boolean searchSourceCodeDescription,  boolean searchTargetCode,
              boolean searchTargetCodeDescription, BrowseMappingHeaderDTO browseMappingHeaderDTO,
              int maxSearchResults, String selectedFilterByOption)
                    throws STSException
  {
        Session session = HibernateSessionFactory.currentSession();

        boolean showAllMappings = false;
        // check to see if user wants to see all Mapping results
        if (searchString.isEmpty())
        {
            showAllMappings = true;
        }
        else
        {
            searchString = applyRulesToSearchString(searchString, selectedFilterByOption);
        }
     

        StringBuffer queryPrimaryText = new StringBuffer(
                "    FROM concept me, mapentryextension mee, relationship msr, property pr " +
                "   WHERE me.kind = 'E' and msr.kind = 'M' " +
                "         AND me.id = mee.mapentryid " +
                "         AND msr.source_entity_id = :mapSetEntityId and me.entity_id = msr.target_entity_id " +
                "         AND me.id = (SELECT MAX(meMax.id) from concept meMax " +
                "                       WHERE meMax.kind = 'E' and meMax.version_id <= :versionId " +
                "                         AND meMax.entity_id = me.entity_id)  " + 
                "         AND msr.id = (SELECT MAX(msrMax.id) from relationship msrMax " +
                "                       WHERE msrMax.kind = 'M' and msrMax.version_id <= :versionId " +
                "                         AND msrMax.entity_id = msr.entity_id)  ");

        // refine the search for retrieving the GEM Flags
        String gemFlagQueryText =
                " AND (pr.CONCEPTENTITY_ID = me.entity_id) " +
                " AND pr.active=1 " +
                " AND (pr.id  IN " +
                "   (SELECT MAX(pr2.id) " +
                "   FROM PROPERTY pr2 " +
                "   WHERE pr.ENTITY_ID =pr2.ENTITY_ID " +
                "   AND pr2.VERSION_ID<=:versionId " +
                "   ) ) " +
                " AND (pr.PROPERTYTYPE_ID IN " +
                "   (SELECT prty2_.id " +
                "   FROM TYPE prty2_ " +
                "   WHERE prty2_.KIND='P' " +
                "   AND (prty2_.NAME = 'GEM_Flags') " +
                "   )) " ;

        queryPrimaryText = queryPrimaryText.append(gemFlagQueryText);

        StringBuffer subQueryBuffer = new StringBuffer();

        if (searchSourceCode || searchTargetCode)
        {
            StringBuffer searchFilterQuery = new StringBuffer();
            boolean hasSearchFilter = false;
            if (!showAllMappings && searchSourceCode == true)
            {
                searchFilterQuery.append(getFilterByOptionQuery("mee.sourcecode", selectedFilterByOption));
                hasSearchFilter = true;
            }
            
            if (!showAllMappings && searchTargetCode == true)
            {
                searchFilterQuery.append(hasSearchFilter ? " OR " : "");
                searchFilterQuery.append(getFilterByOptionQuery("mee.targetcode", selectedFilterByOption));
                hasSearchFilter = true;
            }


            subQueryBuffer.append(searchFilterQuery.toString());
        }

        if (searchSourceCodeDescription || searchTargetCodeDescription)
        {
            if (searchSourceCode || searchTargetCode)
            {
                subQueryBuffer.append(" OR  ");
            }
        }

        String optionQueryText = "";
        if (!showAllMappings)
        {
            optionQueryText = "      AND "+getFilterByOptionQuery("des.name", selectedFilterByOption);
        }
        

        
        String conceptQuery = 
                " mee.@direction@code IN " + 
                "(SELECT con.code from concept con, concept des, relationship dr " +
                "    WHERE con.kind = 'C' AND dr.kind = 'D' AND des.kind = 'D' " +
                "      AND con.entity_id = dr.source_entity_id AND dr.target_entity_id = des.entity_id  " +
                "      AND des.type_id = :preferredDesignationType@direction@ " +
                "      AND con.code = mee.@direction@code " +
                "      AND con.codesystem_id = :codeSystemId@direction@ " +
                "      AND con.ID = (SELECT MAX (conMax.ID) FROM concept conMax  WHERE conMax.kind = 'C' AND conMax.code = mee.@direction@code AND conMax.version_id <= :versionId@direction@ AND con.entity_id = conMax.entity_id) " +
                "      AND des.ID = (SELECT MAX (desMax.ID) FROM concept desMax WHERE desMax.kind = 'D' AND desMax.version_id <= :versionId@direction@ AND des.entity_id = desMax.entity_id) " +
                "      AND dr.ID = (SELECT MAX (drMax.ID) FROM relationship drMax WHERE drMax.kind = 'D' AND drMax.version_id <= :versionId@direction@ AND dr.entity_id = drMax.entity_id) " +
                optionQueryText + ")";      
       

        if (searchSourceCodeDescription)
        {
            String conceptQuerySource = conceptQuery.replaceAll("@direction@", "Source");
            subQueryBuffer.append(conceptQuerySource);
        }
        
        if (searchTargetCodeDescription)
        {
            if (searchSourceCodeDescription)
            {
                subQueryBuffer.append(" OR ");
            }
            String conceptQueryTarget = conceptQuery.replaceAll("@direction@", "Target");
            subQueryBuffer.append(conceptQueryTarget);
        }

        // add opening and closing parentheses for sub-queries along with the sub-queries,
        // if a sub-query exists
        
        if (subQueryBuffer.length()>0)
        {
            queryPrimaryText.append(" AND ( ");       
            queryPrimaryText.append(subQueryBuffer);
            queryPrimaryText.append(")");
        }
        

        StringBuffer queryBuffer = new StringBuffer();

        /*
         * Query the database to get the Search results limited by maxSearchResults
         */

        queryBuffer = new StringBuffer("select * from (select max(rownumber) over () totalrows, " +
                "      mapentryentityid, sourcecode, targetcode, sequence, active, gemflags from " +
                "      (select rownum rownumber, mapentryentityid, sourcecode, targetcode, sequence, active, gemflags from " +
                "      (select * from (select me.entity_id AS mapentryentityid, " +
                "      mee.sourcecode AS sourcecode, " +
                "      mee.targetcode AS targetcode, " +
                "      msr.sequence AS sequence, msr.active AS active, " +
                "      pr.property_value AS gemflags ");
       
        queryBuffer.append(queryPrimaryText);
              
        // add on the sorting criteria and result set size
        queryBuffer.append(" ) order by sourcecode, gemflags, targetcode ))) where rownum <= "+new Integer(maxSearchResults).toString());

        
        SQLQuery sqlQuery = createMappingSQLQuery(session, queryBuffer, 
                browseMappingHeaderDTO, searchString, 
                showAllMappings, searchSourceCodeDescription,
                searchTargetCodeDescription);
     
        List<Object[]>list = null;
        long startTime = System.currentTimeMillis();
        long totalTime = 0L;

        try
        {
            list = (List<Object[]>)sqlQuery.list();
            totalTime = System.currentTimeMillis() - startTime;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new STSException("Invalid search string ("+e.getMessage()+")");
        }
        
        List<BrowseGemResultDTO> results = new ArrayList<BrowseGemResultDTO>();
        HashSet<String> sourceCodeHashSet = new HashSet<String>();
        HashSet<String> targetCodeHashSet = new HashSet<String>();
 
        int totalRowCount = 0;
        // get the total row count from the first row
        if (!list.isEmpty()) {
            Object[] firstRow = list.get(0);
            totalRowCount = ((BigDecimal)firstRow[0]).intValue();
        }
        
        // put query results into results object
        for(Object[] object : list)
        {
            BrowseGemResultDTO browseGemResultDTO = new BrowseGemResultDTO();
            browseGemResultDTO.setMapEntryEntityId(((BigDecimal)object[1]).longValue());
            browseGemResultDTO.setSourceCode((String)object[2]);
            browseGemResultDTO.setTargetCode((String)object[3]);
            browseGemResultDTO.setSequence(((BigDecimal)object[4]).intValue());
            if(((BigDecimal)object[5]).intValue() > 0)
            {
                browseGemResultDTO.setActive("Active");
            }
            else
            { 
                browseGemResultDTO.setActive("Inactive");
            }
            browseGemResultDTO.setGemFlags((String)object[6]);
            
            sourceCodeHashSet.add(browseGemResultDTO.getSourceCode());
            targetCodeHashSet.add(browseGemResultDTO.getTargetCode());
            results.add(browseGemResultDTO);
        }
        
        
        // get the description for each code and set them on the results list objects
        if (results.size() > 0)
        {
            Map<String, Designation> sourceCodeDescriptionMap = new HashMap<String, Designation>(); 
            Map<String, Designation> targetCodeDescriptionMap = new HashMap<String, Designation>(); 
            if (sourceCodeHashSet.size() > 0)
            {
                startTime = System.currentTimeMillis();
                List<String> sourceCodeList = new ArrayList<String>(sourceCodeHashSet);
                sourceCodeDescriptionMap = DesignationDelegate.getBrowseConceptDescriptionsByConceptCodes(browseMappingHeaderDTO.getSourceCodeSystem(), browseMappingHeaderDTO.getSourceVersion().getId(),
                        sourceCodeList);
                totalTime += System.currentTimeMillis() - startTime;
            }
            if (targetCodeHashSet.size() > 0)
            {
                startTime = System.currentTimeMillis();
                List<String> targetCodeList = new ArrayList<String>(targetCodeHashSet);
                targetCodeDescriptionMap = DesignationDelegate.getBrowseConceptDescriptionsByConceptCodes(browseMappingHeaderDTO.getTargetCodeSystem(), browseMappingHeaderDTO.getTargetVersion().getId(),
                        targetCodeList);
                totalTime += System.currentTimeMillis() - startTime;
            }
            for (BrowseGemResultDTO GemResult : results)
            {
                Designation sourceDesignation = sourceCodeDescriptionMap.get(GemResult.getSourceCode());
                Designation targetDesignation = targetCodeDescriptionMap.get(GemResult.getTargetCode());
                GemResult.setSourceCodeDescription(sourceDesignation.getName());
                GemResult.setTargetCodeDescription(targetDesignation.getName());
            }
            
        }

        return new SearchResultDTO(results, totalRowCount, totalTime);
    }


	@SuppressWarnings("unchecked")
	public static List<Version> getMapEntryFinalizedVersions(MapSet mapSet, long mapEntryEntityId, boolean includeAuthoring)
	{
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        
        String sourceCode = getMapEntrySourceCode(mapSet, mapEntryEntityId);

        String versionQuery =
            "  select {v.*} from Version v where v.id in " +
	        "    (select msr.version_id from relationship msr, concept me, mapentryextension mee where " +
	        "            me.version_id "+operator+" :authoringVersionId and  " +
	        "            me.id = mee.mapentryid and me.entity_id = msr.target_entity_id and  " +
	        "            msr.source_entity_id = :mapSetEntityId and mee.sourcecode = :sourceCode ) " +
/*	        "     UNION     " +
	        "     select msr.version_id from relationship msr, concept me, mapentryextension mee where " +
	        "            msr.version_id "+operator+" :authoringVersionId and msr.active = 1 and  " +
	        "            me.id = mee.mapentryid and me.entity_id = msr.target_entity_id and  " +
	        "            msr.source_entity_id = :mapSetEntityId and mee.sourcecode = :sourceCode) " +
*/
	        " and v.id " +operator+" :authoringVersionId " +
	        " order by id desc ";
        
        Query query = session.createSQLQuery(versionQuery).addEntity("v", Version.class);
        query.setLong("mapSetEntityId", mapSet.getEntityId());
        query.setString("sourceCode", sourceCode);
        query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        List<Version> versions = (List<Version>)query.list();

        return versions;
	}

	@SuppressWarnings("unchecked")
	public static List<MapEntryVersionDetailDTO> getMapEntryVersions(MapSet mapSet, long mapEntryEntityId, boolean includeAuthoring, boolean isGem)
	{
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        
        String sourceCode = getMapEntrySourceCode(mapSet, mapEntryEntityId);
        
        String mainQuery =
	        "SELECT v.version_id, v.sequence, me.entity_id, me.name, me.vuid, msr.active, " +
	        "    mee.sourcecode, mee.targetcode, msr.grouping " +
	        "            FROM relationship msr, concept me,  mapentryextension mee, " +
	        "                 (SELECT me.version_id, msr.sequence " +
	        "                    FROM relationship msr, concept me, mapentryextension mee " +
	        "                   WHERE     me.version_id "+operator+" :authoringVersionId " +
	        "                         AND me.id = mee.mapentryid " +
	        "                         AND me.entity_id = msr.target_entity_id " +
	        "                         AND msr.source_entity_id = :mapSetEntityId " +
	        "                         AND mee.sourcecode = :sourceCode " +
	        "                  UNION " +
	        "                  SELECT msr.version_id, msr.sequence " +
	        "                    FROM relationship msr, concept me, mapentryextension mee " +
	        "                   WHERE     msr.version_id "+operator+" :authoringVersionId " +
	        "                         AND me.id = mee.mapentryid " +
	        "                         AND me.entity_id = msr.target_entity_id " +
	        "                         AND msr.source_entity_id = :mapSetEntityId " +
	        "                         AND mee.sourcecode = :sourceCode) v " +
	        "           WHERE     me.id = mee.mapentryid " +
	        "                 AND me.entity_id = msr.target_entity_id " +
	        "                 AND msr.source_entity_id = :mapSetEntityId " +
	        "                 AND mee.sourcecode = :sourceCode " +
	        "                 AND msr.sequence = v.sequence " +
	        "                 AND me.id = " +
	        "                       (SELECT MAX (me2.id) " +
	        "                          FROM concept me2 " +
	        "                         WHERE     me2.kind = 'E' " +
	        "                               AND me2.version_id <= v.version_id " +
	        "                               AND me2.entity_id = me.entity_id) " +
	        "                 AND msr.id = " +
	        "                       (SELECT MAX (msr2.id) " +
	        "                          FROM relationship msr2 " +
	        "                         WHERE     msr2.kind = 'M' " +
	        "                               AND msr2.version_id <= v.version_id " +
	        "                               AND msr2.entity_id = msr.entity_id) " +
	        "ORDER BY version_id, sequence, active "; 

        Query query = session.createSQLQuery(mainQuery);
        query.setLong("mapSetEntityId", mapSet.getEntityId());
        query.setString("sourceCode", sourceCode);
        query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        
    	List<Object[]> results = (List<Object[]>) query.list();
    	List<MapEntryVersionDetailDTO> entries = new ArrayList<MapEntryVersionDetailDTO>();
    	for (Object[] object : results)
		{
				MapEntryVersionDetailDTO mapEntryVersionDetailDTO = new MapEntryVersionDetailDTO();
				mapEntryVersionDetailDTO.setVersionId(((BigDecimal)object[0]).longValue());
				mapEntryVersionDetailDTO.setSequence(((BigDecimal)object[1]).intValue());
				Long entityId = ((BigDecimal)object[2] != null) ? ((BigDecimal)object[2]).longValue() : null;
				if (entityId != null)
				{
					mapEntryVersionDetailDTO.setEntityId(entityId);
					mapEntryVersionDetailDTO.setName((String)object[3]);
					Long vuid = ((BigDecimal)object[4] != null) ? ((BigDecimal)object[4]).longValue() : null;
					mapEntryVersionDetailDTO.setVuid(vuid);
					mapEntryVersionDetailDTO.setActive(((BigDecimal)object[5]).intValue() == 1 ? true : false);
					mapEntryVersionDetailDTO.setSourceCode((String)object[6]);
					mapEntryVersionDetailDTO.setTargetCode((String)object[7]);
					Long grouping = ((BigDecimal)object[8] != null) ? ((BigDecimal)object[8]).longValue() : null;
					mapEntryVersionDetailDTO.setGrouping(grouping);
					entries.add(mapEntryVersionDetailDTO);
				}
		}

    	populateMapEntryDescriptions(mapSet, entries, isGem);
    	return entries;
	}
	
	private static String getMapEntrySourceCode(MapSet mapSet, long mapEntryEntityId)
	{
        Session session = HibernateSessionFactory.currentSession();
        
        String sourceCodeQuery =
	        "   select mee.sourcecode from relationship msr, concept me, mapentryextension mee where " +
	        "        me.id = mee.mapentryid and me.entity_id = msr.target_entity_id and  " +
	        "        msr.source_entity_id = :mapSetEntityId and msr.target_entity_id = :mapEntryEntityId  and  " +
	        "        me.id = (select max(me2.id) from concept me2 where me2.kind = 'E' and me2.version_id <= :authoringVersionId and me2.entity_id = me.entity_id) and " +
	        "        msr.id = (select max(msr2.id) from relationship msr2 where msr2.kind = 'M' and msr2.version_id <= :authoringVersionId and msr2.entity_id = msr.entity_id) "; 

        Query query = session.createSQLQuery(sourceCodeQuery);
        query.setLong("mapSetEntityId", mapSet.getEntityId());
        query.setLong("mapEntryEntityId", mapEntryEntityId);
        query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        
    	Object result = query.uniqueResult();
    	
    	return (String)result;
	}

	private static void populateMapEntryDescriptions(MapSet mapSet, List<MapEntryVersionDetailDTO> mapEntryList, boolean isGem)
	{
        Session session = HibernateSessionFactory.currentSession();
        
    	String sourceQuery = "select srcDes.name from CodedConcept srcCon, Designation srcDes, DesignationRelationship srcDesRel "
        	+ "where srcCon.code = :mapEntrySourceCode and srcCon.codeSystem.id = :sourceCodeSystemId "
        	+ "  and srcDes.type.id = :sourceCodeSystemPreferredDesignationTypeId "
        	+ "  and srcCon.entityId = srcDesRel.sourceEntityId and srcDes.entityId = srcDesRel.targetEntityId "
        	+ "  and srcCon.id = (select max(id) from CodedConcept conMax where conMax.version.id <= :mapSetSourceVersion  "
        	+ "      and srcCon.entityId = conMax.entityId) "
        	+ "  and srcDes.id = (select max(id) from Designation desMax where desMax.version.id <= :mapSetSourceVersion  "
        	+ "      and srcDes.entityId = desMax.entityId) "
        	+ "  and srcDesRel.id = (select max(id) from DesignationRelationship desRelMax where desRelMax.version.id <= :mapSetSourceVersion "
        	+ "      and srcDesRel.entityId = desRelMax.entityId) "
        	+ "order by srcDes.active desc";

    	String targetQuery = "";
    	if(isGem){
        	targetQuery = "select trgDes.name from CodedConcept trgCon, Designation trgDes, DesignationRelationship trgDesRel "
                    + "where trgCon.code = :mapEntryTargetCode and trgCon.codeSystem.id = :targetCodeSystemId "
                    + "  and trgDes.type.id = :targetCodeSystemPreferredDesignationTypeId "
                    + "  and trgCon.entityId = trgDesRel.sourceEntityId and trgDes.entityId = trgDesRel.targetEntityId "
                    + "  and trgCon.id = (select max(id) from CodedConcept conMax where trgCon.entityId = conMax.entityId) "
                    + "  and trgDes.id = (select max(id) from Designation desMax where trgDes.entityId = desMax.entityId) "
                    + "  and trgDesRel.id = (select max(id) from DesignationRelationship desRelMax where trgDesRel.entityId = desRelMax.entityId) ";
    	}
    	else
    	{
        	targetQuery = "select trgDes.name from CodedConcept trgCon, Designation trgDes, DesignationRelationship trgDesRel "
            + "where trgCon.code = :mapEntryTargetCode and trgCon.codeSystem.id = :targetCodeSystemId "
            + "  and trgDes.type.id = :targetCodeSystemPreferredDesignationTypeId "
            + "  and trgCon.entityId = trgDesRel.sourceEntityId and trgDes.entityId = trgDesRel.targetEntityId "
            + "  and trgCon.id = (select max(id) from CodedConcept conMax where conMax.version.id <= :mapSetTargetVersion "
            + "      and trgCon.entityId = conMax.entityId) "
            + "  and trgDes.id = (select max(id) from Designation desMax where desMax.version.id <= :mapSetTargetVersion "
            + "      and trgDes.entityId = desMax.entityId) "
            + "  and trgDesRel.id = (select max(id) from DesignationRelationship desRelMax where desRelMax.version.id <= :mapSetTargetVersion "
            + "      and trgDesRel.entityId = desRelMax.entityId) ";
    	}
    	CodeSystem sourceCodeSystem = VersionDao.getByVersionId(mapSet.getSourceVersionId()).getCodeSystem();
    	CodeSystem targetCodeSystem = VersionDao.getByVersionId(mapSet.getTargetVersionId()).getCodeSystem();
        for (MapEntryVersionDetailDTO mapEntry : mapEntryList)
        {
        	Query query = session.createQuery(sourceQuery);
        	query.setString("mapEntrySourceCode", mapEntry.getSourceCode());
        	query.setLong("sourceCodeSystemId", sourceCodeSystem.getId());
        	query.setLong("sourceCodeSystemPreferredDesignationTypeId", sourceCodeSystem.getPreferredDesignationType().getId());
        	query.setLong("mapSetSourceVersion", mapSet.getSourceVersionId());
        	String sourceDescription = (String) query.setMaxResults(1).uniqueResult();
        	
        	query = session.createQuery(targetQuery);
        	query.setString("mapEntryTargetCode", mapEntry.getTargetCode());
        	query.setLong("targetCodeSystemId", targetCodeSystem.getId());
        	query.setLong("targetCodeSystemPreferredDesignationTypeId", targetCodeSystem.getPreferredDesignationType().getId());
        	if(!isGem)
        		query.setLong("mapSetTargetVersion", mapSet.getTargetVersionId());
        	String targetDescription = (String) query.setMaxResults(1).uniqueResult();

        	mapEntry.setSourceDescription(sourceDescription);
        	mapEntry.setTargetDescription(targetDescription);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<BrowseMappingHeaderDTO> getBrowseMappingHeaderList(MapSet mapSet, Designation mapSetDesignation, boolean includeAuthoring)
	{
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        
        String versionQuery =
	        "select version_id, sourceVersionId, targetVersionId from ( " +
	        "  select ms.version_id, sv.id sourceVersionId, tv.id targetVersionId  " +
	        "    from concept ms, mapsetextension mse, version sv, version tv " +
	        "   where ms.kind = 'M' " +
	        "     and ms.id = mse.mapsetid and ms.version_id "+operator+" :authoringVersionId " + 
	        "     and mse.sourceversionid = sv.id and mse.targetversionid = tv.id " +
	        "     and ms.entity_id = :mapSetEntityId " +
	        "  UNION " +
	        "  select greatest(me.version_id, :mapSetVersionId), sv.id sourceVersionId, tv.id targetVersionId " +
	        "    from concept me, relationship msr, concept ms, mapsetextension mse, version sv, version tv " +
	        "   where ms.kind = 'M' and me.kind = 'E' and msr.kind = 'M'  " +
	        "     and msr.target_entity_id = me.entity_id and msr.source_entity_id = :mapSetEntityId " +
	        "     and msr.source_entity_id = ms.entity_id " +
            "     and mse.mapsetid = ms.id and me.version_id "+operator+" :authoringVersionId " +
            "     and ms.version_id "+operator+" :authoringVersionId " +
	        "     and mse.sourceversionid = sv.id and mse.targetversionid = tv.id " +
	        "     and msr.id = (select max(msrMax.id) from relationship msrMax where msrMax.kind = 'M' and msrMax.entity_id = msr.entity_id) " +
	        "     and ms.id = (select max(msMax.id) from concept msMax where msMax.kind = 'M' and msMax.entity_id = ms.entity_id)  )  " +
	        "order by version_id desc ";
        
        Query query = session.createSQLQuery(versionQuery);
        query.setLong("mapSetEntityId", mapSet.getEntityId());
        query.setLong("mapSetVersionId", mapSet.getVersion().getId());
        query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setCacheMode(CacheMode.IGNORE);
        List<Object[]> results = (List<Object[]>)query.list();

    	List<BrowseMappingHeaderDTO> browseMappingHeaderList = new ArrayList<BrowseMappingHeaderDTO>();
    	for (Object[] object : results)
		{
    		long versionId = ((BigDecimal)object[0]).longValue();
    		long sourceVersionId = ((BigDecimal)object[1]).longValue();
    		long targetVersionId = ((BigDecimal)object[2]).longValue();
    		MapSet mapSet2 = MapSetDelegate.getByVuid(mapSet.getVuid(), versionId);
    		Version version = VersionDelegate.getByVersionId(versionId);
    		Version sourceVersion = VersionDelegate.getByVersionId(sourceVersionId);
    		Version targetVersion = VersionDelegate.getByVersionId(targetVersionId);
    		
    		BrowseMappingHeaderDTO browseMappingHeaderDTO = new BrowseMappingHeaderDTO();
    		browseMappingHeaderDTO.setMapSet(mapSet2);
    		browseMappingHeaderDTO.setMapSetDesignation(mapSetDesignation);
    		browseMappingHeaderDTO.setSearchVersion(version);
    		browseMappingHeaderDTO.setSourceCodeSystem(sourceVersion.getCodeSystem());
    		browseMappingHeaderDTO.setSourceVersion(sourceVersion);
    		browseMappingHeaderDTO.setTargetCodeSystem(targetVersion.getCodeSystem());
    		browseMappingHeaderDTO.setTargetVersion(targetVersion);
    		browseMappingHeaderList.add(browseMappingHeaderDTO);
		}    	
    	
        return browseMappingHeaderList;
	}
}
