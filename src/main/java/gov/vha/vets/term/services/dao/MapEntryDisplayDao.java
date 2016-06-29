package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.MapEntryDisplayDTO;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

public class MapEntryDisplayDao
{
	private final static int ENTRY_QUERY_TYPE_COUNT = 0;
	private final static int ENTRY_QUERY_TYPE_RESULTS = 1;
	
    public static List<MapEntryDisplayDTO> getEntries(long mapSetEntityId, Long versionId, boolean includeInactives,
    		int startRow, int rowsPerPage, String sortOrder, String searchText, String sortColumnName,
    		Collection<String> sourceConceptCodes, Collection<String> targetConceptCodes, String sourceDescription, String targetDescription,
    		Integer mapEntryOrder, Boolean mapEntryStatus)
    {
        Session session = HibernateSessionFactory.currentSession();
    	
        String sql = getEntriesQuery(ENTRY_QUERY_TYPE_RESULTS, includeInactives, searchText, sortColumnName, sortOrder,
                sourceConceptCodes, targetConceptCodes, sourceDescription, targetDescription, mapEntryOrder, mapEntryStatus);
        
        Query query = session.createSQLQuery(sql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
    	if (searchText != null && searchText.length() > 0)
    	{
    		query.setString("searchString", searchText);
    	}
    	if (sourceConceptCodes != null)
    	{
    	    query.setParameterList("sourceConceptCodes", sourceConceptCodes);
    	}
        if (targetConceptCodes != null)
        {
            query.setParameterList("targetConceptCodes", targetConceptCodes);
        }
        if (sourceDescription != null)
        {
            query.setString("sourceDescription", sourceDescription);
        }
        if (targetDescription != null)
        {
            query.setString("targetDescription", targetDescription);
        }
        if (mapEntryOrder != null)
        {
            query.setInteger("mapEntryOrder", mapEntryOrder);
        }
        if (mapEntryStatus != null)
        {
            query.setBoolean("mapEntryStatus", mapEntryStatus);
        }
        ScrollableResults scrollableResults = query.scroll();
        scrollableResults.first();
        boolean canScroll = scrollableResults.scroll(startRow);
        
        List<MapEntryDisplayDTO> mapEntryDisplayDTOList = new ArrayList<MapEntryDisplayDTO>();
        int i = 0;
        while (canScroll && rowsPerPage > i++)
    	{
        	long entityId = ((BigDecimal)scrollableResults.get(0)).longValue();
        	long vuid = ((BigDecimal)scrollableResults.get(1)).longValue();
        	String sourceCode = scrollableResults.get(2).toString();
        	String sourceDesc = (String) scrollableResults.get(3);
        	String targetCode = scrollableResults.get(4).toString();
        	String targetDesc = (String) scrollableResults.get(5);
        	int sequence = ((BigDecimal)scrollableResults.get(6)).intValue();
        	int active = ((BigDecimal)scrollableResults.get(7)).intValue();
        	MapEntryDisplayDTO mapEntryDisplayDTO = new MapEntryDisplayDTO(entityId, vuid, sourceCode,
        			sourceDesc, targetCode, targetDesc, sequence, active == 1 ? true : false);
        	mapEntryDisplayDTOList.add(mapEntryDisplayDTO);
	    	if (!scrollableResults.next())
			{
				break;
			}
        }

        return mapEntryDisplayDTOList;  
    }

    public static long getEntriesCount(long mapSetEntityId, Long versionId, boolean includeInactives,
    		String sortOrder, String searchText, String sortColumnName)
    {
        Session session = HibernateSessionFactory.currentSession();
    	
        String sql = getEntriesQuery(ENTRY_QUERY_TYPE_COUNT, includeInactives, searchText, null, null, null, null, null, null, null, null);
        
        Query query = session.createSQLQuery(sql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
    	if (searchText != null && searchText.length() > 0)
    	{
        	query.setString("searchString", searchText);
    	}
        Object count = query.uniqueResult();

        return ((BigDecimal)count).longValue();
    }

    private static String getEntriesQuery(int queryType, boolean includeInactives, String searchText, String sortColumnName, String sortOrder,
            Collection<String> sourceConceptCodes, Collection<String> targetConceptCodes, String sourceDescription, String targetDescription,
            Integer mapEntryOrder, Boolean mapEntryStatus)
    {
    	String orderBy = "";
    	String filterBy = "";
        String sourceConceptCodesQuery = (sourceConceptCodes != null) ? " and med.sourceCode in (:sourceConceptCodes) " : "";
        String targetConceptCodesQuery = (targetConceptCodes != null) ? " and med.targetCode in (:targetConceptCodes) " : "";
        String sourceDescriptionQuery = (sourceDescription != null) ? " and upper(med.sourceDescription) like upper(concat(concat('%',:sourceDescription), '%')) " : "";
        String targetDescriptionQuery = (targetDescription != null) ? " and upper(med.targetDescription) like upper(concat(concat('%',:targetDescription), '%')) " : "";
        String mapEntryOrderQuery = (mapEntryOrder != null) ? " and med.sequence = :mapEntryOrder " : "";
        String mapEntryStatusQuery = (mapEntryStatus != null) ? " and med.active = :mapEntryStatus " : "";

    	if (sortColumnName != null)
		{
    		if (sortColumnName.equalsIgnoreCase("Order"))
    		{
    			orderBy = "order by sequence " + sortOrder;
    		}
    		else if (sortColumnName.equalsIgnoreCase("Status"))
			{
				orderBy = "order by active " + sortOrder;
			}
    		else
    		{
				orderBy = "order by " + sortColumnName + " " + sortOrder;
    		}
		}
    	
    	if (searchText != null &&  searchText.length() > 0)
    	{
    		filterBy =
    			" and (upper(med.vuid) LIKE upper(CONCAT(CONCAT('%',:searchString), '%'))" +
    			" or upper(med.sourceCode) LIKE upper(CONCAT(CONCAT('%',:searchString), '%'))" +
    			" or upper(med.targetCode) LIKE upper(CONCAT(CONCAT('%',:searchString), '%'))" +
    			" or upper(med.sourceDescription) LIKE upper(CONCAT(CONCAT('%',:searchString), '%'))" +
    			" or upper(med.targetDescription) LIKE upper(CONCAT(CONCAT('%',:searchString), '%')) ) ";
    	}

        String sql = "select med.mapentryentityid, med.vuid, med.sourceCode, med.sourceDescription, " +
        	" med.targetCode, med.targetDescription, med.sequence, med.active ";
        String sqlCount = "select count(*) ";
        String sqlMain =
	        " from mapentrydisplay med where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId " +
	        sourceConceptCodesQuery + targetConceptCodesQuery + sourceDescriptionQuery + targetDescriptionQuery +
	        mapEntryOrderQuery + mapEntryStatusQuery +
	  		filterBy + orderBy;

        return (queryType == ENTRY_QUERY_TYPE_RESULTS) ? sql+sqlMain : sqlCount+sqlMain;  
    }
    
    public static void updateMapEntryCache(long mapSetEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
    	
        int mapEntryCount = 0;
        
        if (versionId == HibernateSessionFactory.AUTHORING_VERSION_ID)
    	{
	        String sql1 = "delete from mapentrydisplay med where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId";
	        
	        Query query1 = session.createSQLQuery(sql1);
	        query1.setLong("mapSetEntityId", mapSetEntityId);
	        query1.setLong("versionId", versionId);
	        int updateCount1 = query1.executeUpdate();
	        System.out.println("Number of inserts for MapSetId: " + mapSetEntityId + ", versionId: " + versionId + " is: " + updateCount1 + " MapEntryDisplay rows");
    	}
        else // this is a finalized version, see if we already have the data in the table
    	{
	        String sql2 = "select count(*) from mapentrydisplay med where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId";
	        
	        Query query2 = session.createSQLQuery(sql2);
	        query2.setLong("mapSetEntityId", mapSetEntityId);
	        query2.setLong("versionId", versionId);
	        Object count = query2.uniqueResult();
	        mapEntryCount = ((BigDecimal)count).intValue();
	        System.out.println("Row count for MapSetId: " + mapSetEntityId + ", versionId: " + versionId + " is: " + count + " MapEntryDisplay rows");
    	}
        
        if (mapEntryCount == 0)
    	{
        	String sql3 =
		        "INSERT INTO mapentrydisplay (mapsetentityid, mapsetversionid, mapentryentityid, vuid, sourcecodesystemid, sourceversionid, sourcecode, " +
		        "   targetcodesystemid, targetversionid, targetcode, sequence, active) " +
		        "     SELECT :mapSetEntityId, :versionId, me.entity_id, me.vuid,  sv.codeSystem_id scsid, sv.id svid, mex.sourceCode, " +
		        "            tv.codeSystem_id tcsid, tv.id tvid, mex.targetCode, msr.sequence, msr.active " +
		        "       FROM concept ms, mapsetextension msx, relationship msr, concept me, mapentryextension mex, " +
		        "            version sv, version tv " +
		        "      WHERE  ms.kind = 'M' AND msr.kind = 'M' AND me.kind = 'E' " +
		        "            AND msx.mapsetid = ms.id AND mex.mapentryid = me.id " +
		        "            AND msr.source_entity_id = ms.entity_Id AND me.entity_Id = msr.target_entity_id " +
		        "            AND sv.id = msx.sourceVersionId AND tv.id = msx.targetVersionId " +
		        "            AND ms.id = (SELECT MAX (msMax.id) FROM concept msMax WHERE msMax.kind = 'M' AND msMax.version_id <= :versionId AND msMax.entity_id = ms.entity_id) " +
		        "            AND msr.id = (SELECT MAX (msrMax.id) FROM relationship msrMax WHERE msrMax.kind = 'M' AND msrMax.version_id <= :versionId AND msrMax.entity_id = msr.entity_id) " +
		        "            AND me.id = (SELECT MAX (meMax.id) FROM concept meMax WHERE meMax.version_id <= :versionId AND meMax.entity_id = me.entity_id) " +
		        "            AND ms.entity_id = :mapSetEntityId "; 
        
            Query query3 = session.createSQLQuery(sql3);
            query3.setLong("mapSetEntityId", mapSetEntityId);
            query3.setLong("versionId", versionId);
            int updateCount3 = query3.executeUpdate();
            System.out.println("Number of inserts for MapSetId: " + mapSetEntityId + ", versionId: " + versionId + " is: " + updateCount3 + " MapEntryDisplay rows");
            
            String sql4 = 
	            "update mapentrydisplay med set (sourcedescription) = ( " +
	            "  select des.name from concept con, relationship dr, concept des, codesystem codesys " +
	            "    where con.kind = 'C' and dr.kind = 'D' and des.kind = 'D' " +
	            "      and con.entity_id = dr.source_entity_id and des.entity_id = dr.target_entity_id " +
	            "      and codesys.id = med.sourcecodesystemid and des.type_id = codesys.preferred_designation_type_id and des.active = 1 " +
	            "      and con.codesystem_id = med.sourcecodesystemid and con.code = med.sourcecode " +
	            "      and con.id = (select max(conMax.id) from concept conMax where conMax.kind = 'C' and conMax.version_id <= med.sourceversionid and conMax.entity_id = con.entity_id) " +
	            "      and dr.id = (select max(drMax.id) from relationship drMax where drMax.kind = 'D' and drMax.version_id <= med.sourceversionid and drMax.entity_id = dr.entity_id) " +
	            "      and des.id = (select max(desMax.id) from concept desMax where desMax.kind = 'D' and desMax.version_id <= med.sourceversionid and desMax.entity_id = des.entity_id)) " +
	            " where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId "; 
            Query query4 = session.createSQLQuery(sql4);
            query4.setLong("mapSetEntityId", mapSetEntityId);
            query4.setLong("versionId", versionId);
            int updateCount4 = query4.executeUpdate();
            System.out.println("Number of source updates for MapSetId: " + mapSetEntityId + ", versionId: " + versionId + " is: " + updateCount4 + " MapEntryDisplay rows");
            
            String sql5 =
	            "update mapentrydisplay med set (targetdescription) = ( " +
	            "  select des.name from concept con, relationship dr, concept des, codesystem codesys " +
	            "    where con.kind = 'C' and dr.kind = 'D' and des.kind = 'D' " +
	            "      and con.entity_id = dr.source_entity_id and des.entity_id = dr.target_entity_id " +
	            "      and codesys.id = med.targetcodesystemid and des.type_id = codesys.preferred_designation_type_id and des.active = 1 " +
	            "      and con.codesystem_id = med.targetcodesystemid and con.code = med.targetcode " +
	            "      and con.id = (select max(conMax.id) from concept conMax where conMax.kind = 'C' and conMax.version_id <= med.targetversionid and conMax.entity_id = con.entity_id) " +
	            "      and dr.id = (select max(drMax.id) from relationship drMax where drMax.kind = 'D' and drMax.version_id <= med.targetversionid and drMax.entity_id = dr.entity_id) " +
	            "      and des.id = (select max(desMax.id) from concept desMax where desMax.kind = 'D' and desMax.version_id <= med.targetversionid and desMax.entity_id = des.entity_id)) " + 
            	" where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId "; 
            Query query5 = session.createSQLQuery(sql5);
            query5.setLong("mapSetEntityId", mapSetEntityId);
            query5.setLong("versionId", versionId);
            int updateCount5 = query5.executeUpdate();
            System.out.println("Number of target updates for MapSetId: " + mapSetEntityId + ", versionId: " + versionId + " is: " + updateCount5 + " MapEntryDisplay rows");
    	}
    }

	public static List<MapEntryDisplayDTO> getNullDescriptionEntries(long mapSetEntityId, long versionId)
	{
        Session session = HibernateSessionFactory.currentSession();
    	
        String sql =
        	"select mapsetentityid, mapsetversionid, mapentryentityid, vuid, sourcecodesystemid, sourceversionid, " +
        	"       sourcecode, sourcedescription, targetcodesystemid, targetversionid, targetcode, targetdescription," +
        	"       sequence, active " +
        	"   from mapentrydisplay where mapsetentityid = :mapSetEntityId and mapsetversionid = :versionId " +
        	"    and (sourcedescription is null or targetdescription is null) ";

        
        Query query = session.createSQLQuery(sql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
        List<Object[]> list = (List<Object[]>)query.list();

        List<MapEntryDisplayDTO> mapEntryDisplayDTOList = new ArrayList<MapEntryDisplayDTO>();
		for (Object[] object : list)
    	{
			long entityId = ((BigDecimal)object[0]).longValue();
			long mapSetVersionId = ((BigDecimal)object[1]).longValue();
        	long mapEntryEntityId = ((BigDecimal)object[2]).longValue();
		    long vuid = ((BigDecimal)object[3]).longValue();
        	long sourceCodeSystemId = ((BigDecimal)object[4]).longValue();
        	long sourceVersionId = ((BigDecimal)object[5]).longValue();
        	String sourceCode = object[6].toString();
        	String sourceDescription = (String)object[7];
        	long targetCodeSystemId = ((BigDecimal)object[8]).longValue();
        	long targetVersionId = ((BigDecimal)object[9]).longValue();
        	String targetCode = object[10].toString();
        	String targetDescription = (String)object[11];
        	int sequence = ((BigDecimal)object[12]).intValue();
        	int active = ((BigDecimal)object[13]).intValue();
        	MapEntryDisplayDTO mapEntryDisplayDTO = new MapEntryDisplayDTO(entityId, mapSetVersionId, mapEntryEntityId,
        			vuid, sourceCodeSystemId, sourceVersionId, sourceCode, sourceDescription, targetCodeSystemId, targetVersionId,
        			targetCode, targetDescription, sequence, active == 1 ? true : false);
        	mapEntryDisplayDTOList.add(mapEntryDisplayDTO);
        }

        return mapEntryDisplayDTOList;
	}

	public static void updateSourceDescription(long mapSetEntityId, long versionId, String conceptCode, String description)
	{
		updateDescription("source", mapSetEntityId, versionId, conceptCode, description);
	}
	
	public static void updateTargetDescription(long mapSetEntityId, long versionId, String conceptCode, String description)
	{
		updateDescription("target", mapSetEntityId, versionId, conceptCode, description);
	}
	
	private static void updateDescription(String columnPrefix, long mapSetEntityId, long versionId, String conceptCode, String description)
	{
        Session session = HibernateSessionFactory.currentSession();

        String sql =
	        "update mapentrydisplay med set " + columnPrefix + "description = :description " +
	    	"  where med.mapsetentityid = :mapSetEntityId and med.mapsetversionid = :versionId and med."+columnPrefix+"Code = :conceptCode ";

	    Query query = session.createSQLQuery(sql);
	    query.setString("description", description);
	    query.setLong("mapSetEntityId", mapSetEntityId);
	    query.setLong("versionId", versionId);
	    query.setString("conceptCode", conceptCode);
	    int updateCount = query.executeUpdate();
	    System.out.println("Updated MapSetId: " + mapSetEntityId + ", versionId: " + versionId + ", "+columnPrefix+"Code: " + conceptCode +
				" to: " + description + " (" + updateCount + " rows)");
	}
}
