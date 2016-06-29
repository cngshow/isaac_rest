package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.MapSetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class MapEntryDao extends EntityBaseDao
{
	public static void save(MapEntry mapEntry)
	{
		HibernateSessionFactory.currentSession().save(mapEntry);
	}
	
	@SuppressWarnings("unchecked")
 	public static List<MapEntry> get(Collection<Long> vuids)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String hql = "from MapEntry me where me.id in (select max(mapEntry.id) from MapEntry mapEntry "
                        + "where mapEntry.vuid in (:vuids) and mapEntry.entityId = me.entityId)";
        
        Query query = session.createQuery(hql);
        List<MapEntry> list = executeQuery(query, "vuids", vuids);
        return list;
	}
    /**
     * Get a mapEntry based on the source and target code
     * @param sourceCode
     * @param targetCode
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<MapEntry> get(Collection<String> sourceCodes, Collection<String> targetCodes)
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "from MapEntry me where me.id in (select max(me2.id) from MapEntry me2 where " +
                "me2.sourceCode in (:sourceCodes) " +
                "and me2.targetCode in (:targetCodes) " +
                "and me2.entityId = me.entityId)";
        Query query = session.createQuery(hql);
        query.setParameterList("sourceCodes", sourceCodes);
        query.setParameterList("targetCodes", targetCodes);
        return query.list();
    }
    
    public static MapEntry get(String sourceCode, String targetCode)
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "from MapEntry me where " +
                "me.sourceCode = :sourceCode " +
        		"and me.targetCode = :targetCode " +
        		"and me.id = (select max(me3.id) from Concept me3 where me3.entityId = " +
        		"(select distinct me2.entityId from MapEntry me2 where " +
                "me2.sourceCode = :sourceCode " +
                "and me2.targetCode = :targetCode " +
                "and me.entityId = me2.entityId))";
        Query query = session.createQuery(hql);
        query.setParameter("sourceCode", sourceCode);
        query.setParameter("targetCode", targetCode);
        return (MapEntry) query.uniqueResult();
    }

	@SuppressWarnings("unchecked")
	public static List<MapEntryDTO> getChanges(Long mapSetEntityId)
	{
        Session session = HibernateSessionFactory.currentSession();

        String hql = " select mapEntry, mapSetRel from MapSetRelationship mapSetRel, MapSet mapSet, MapEntry mapEntry where mapSetRel.sourceEntityId = mapSet.entityId  " +
        	  "    and mapSet.id = (select max(mapSet2.id) from MapSet mapSet2 where mapSet2.entityId = mapSet.entityId) " +
        	  "    and mapSetRel.id in (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel.entityId = mapSetRel2.entityId) " +
        	  "    and mapEntry.entityId = mapSetRel.targetEntityId " +
        	  "    and mapEntry.id in (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.entityId = mapEntry.entityId) " +
        	  "    and (mapEntry.version.id = :versionId or mapSetRel.version.id = :versionId)" +
        	  "    and mapSet.entityId = :mapSetEntityId order by mapEntry.vuid";

        Query query = session.createQuery(hql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        
        List<MapEntryDTO> mapEntryDTOs = new ArrayList<MapEntryDTO>();
        List<Object[]> results =  query.list();
        for (Object[] objects : results)
		{
        	MapEntry mapEntry = (MapEntry) objects[0];
        	MapSetRelationship mapSetRelationship = (MapSetRelationship) objects[1];
			MapEntryDTO mapEntryDTO = new MapEntryDTO(mapEntry, mapSetRelationship);
			mapEntryDTOs.add(mapEntryDTO);
		}
        return mapEntryDTOs;  
	}
	
    public static List<MapEntryDTO> getEntries(long mapSetEntityId, Long versionId, boolean includeInactives)
    {
    	return getEntries(mapSetEntityId, null, null, versionId, includeInactives, "");
    }

    public static List<MapEntryDTO> getEntriesBySourceCode(long mapSetEntityId, String sourceCode)
    {
    	return getEntries(mapSetEntityId, sourceCode, null, HibernateSessionFactory.AUTHORING_VERSION_ID, true, " order by mapSetRel.sequence");
    }

    public static List<MapEntryDTO> getEntriesOrderedByVuid(long mapSetEntityId, Long versionId, boolean includeInactives)
    {
    	return getEntries(mapSetEntityId, null, null, versionId, includeInactives, " order by mapEntry.vuid");
    }

    public static List<MapEntryDTO> getEntriesByEntityId(long mapSetEntityId, Long mapEntryEntityId)
    {
        return getEntries(mapSetEntityId, null, mapEntryEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID, true, " order by mapSetRel.sequence");
    }

    public static List<MapEntryDTO> getMapEntriesForSequenceVerification(long mapSetEntityId)
	{
	    return getEntries(mapSetEntityId, null, null, HibernateSessionFactory.AUTHORING_VERSION_ID, false, " order by mapEntry.sourceCode, mapSetRel.sequence");
	}
    
	
    @SuppressWarnings("unchecked")
    public static List<MapEntryDTO> getEntries(long mapSetEntityId, String sourceCode, Long mapEntryEntityId, Long versionId, boolean includeInactives, String orderby)
    {
        Session session = HibernateSessionFactory.currentSession();
    	String statusClause = includeInactives == false ? "AND mapSetRel.active = 1 " : "";
    	
    	String sourceCodeFilter = sourceCode != null ? "and mapEntry.sourceCode = :sourceCode " : "";
    	
    	String mapEntryEntityIdFilter = mapEntryEntityId != null ? 
    	        "AND mapEntry.sourceCode = (select distinct me.sourceCode from MapEntry me where me.entityId = :mapEntryEntityId) " : ""; 
        
        String hql = " select mapEntry, mapSetRel from MapSetRelationship mapSetRel, MapSet mapSet, MapEntry mapEntry where " +
        	"mapSetRel.sourceEntityId = mapSet.entityId  " +
  	  		"and mapSet.id = (select max(mapSet2.id) from MapSet mapSet2 where mapSet2.version.id <= :versionId and mapSet2.entityId = mapSet.entityId) " +
  	  		"and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.version.id <= :versionId and mapSetRel.entityId = mapSetRel2.entityId ) " +
  	  		sourceCodeFilter +
  	  		mapEntryEntityIdFilter+
  	  		"and mapEntry.entityId = mapSetRel.targetEntityId " +
  	  		statusClause +
  	  		"and mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.version.id <= :versionId and mapEntry2.entityId = mapEntry.entityId) " +
  	  		"and mapSet.entityId = :mapSetEntityId " +
  	  		orderby;
  	  		
        
        Query query = session.createQuery(hql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
        if (sourceCode != null)
        {
        	query.setParameter("sourceCode", sourceCode);
        }
        if (mapEntryEntityId != null)
        {
            query.setParameter("mapEntryEntityId", mapEntryEntityId);
        }

        List<MapEntryDTO> mapEntryDTOs = new ArrayList<MapEntryDTO>();
        List<Object[]> results =  query.list();
        for (Object[] objects : results)
		{
        	MapEntry mapEntry = (MapEntry) objects[0];
        	MapSetRelationship mapSetRelationship = (MapSetRelationship) objects[1];
			MapEntryDTO mapEntryDTO = new MapEntryDTO(mapEntry, mapSetRelationship);
			mapEntryDTOs.add(mapEntryDTO);
		}
        return mapEntryDTOs;  
    }

    
    public static String getDiscoveryEntriesQuery()
    {
    	String sql = 
    	"SELECT   mex.sourcecode, mex.targetcode, msr.sequence, msr.active, ms.name, me.vuid " +
    	"  FROM   RELATIONSHIP msr, CONCEPT ms INNER JOIN MapSetExtension msx ON ms.id = msx.MapSetId, CONCEPT me " +
    	"                           INNER JOIN MapEntryExtension mex ON me.id = mex.MapEntryId " +
    	"  WHERE msr.KIND = 'M' AND ms.KIND = 'M' AND me.KIND = 'E' AND msr.SOURCE_ENTITY_ID = ms.ENTITY_ID AND ms.id = " +
    	"               (SELECT MAX (ms2.id) " +
    	"                 FROM CONCEPT ms2 INNER JOIN MapSetExtension mex2 ON ms2.id = mex2.MapSetId " +
    	"                 WHERE ms2.KIND = 'M' AND ms2.VERSION_ID <= :versionId AND ms2.ENTITY_ID = ms.ENTITY_ID) " +
    	"         AND msr.id = " +
    	"               (SELECT MAX (msr3.id) " +
    	"                 FROM RELATIONSHIP msr3 " +
    	"                 WHERE msr3.KIND = 'M' AND msr3.VERSION_ID <= :versionId AND msr.ENTITY_ID = msr3.ENTITY_ID) " +
    	"          			AND me.ENTITY_ID = msr.TARGET_ENTITY_ID " +
    	"         AND me.id = " +
    	"               (SELECT MAX (me2.id) " +
    	"                 FROM CONCEPT me2 INNER JOIN MapEntryExtension mex2 ON me2.id = mex2.MapEntryId " +
    	"                 WHERE me2.KIND = 'E' AND me2.VERSION_ID <= :versionId AND me2.ENTITY_ID = me.ENTITY_ID) " +
    	"         AND ms.ENTITY_ID = :mapSetEntityId ";

    	return sql;
    }

	public static int setAuthoringToVersion(List<Long> mapSetEntityIds, Version version)
	{
        String query = "update MapEntry me set me.version = :"+NEW_VERSION+" where me.version.id = :"+AUTHORING_VERSION+" and"
        + " me.entityId in (select msr.targetEntityId from MapSetRelationship msr where msr.sourceEntityId in (:entityId))";

        return setAuthoringToVersion(query, mapSetEntityIds, version, "entityId");
	}

	public static int getEntryCount(long mapSetEntityId, long versionId)
	{
        Session session = HibernateSessionFactory.currentSession();
        
        String hql = " select count(mapEntry) from MapSetRelationship mapSetRel, MapSet mapSet, MapEntry mapEntry where " +
        	"mapSetRel.sourceEntityId = mapSet.entityId  " +
  	  		"and mapSet.id = (select max(mapSet2.id) from MapSet mapSet2 where mapSet2.version.id <= :versionId and mapSet2.entityId = mapSet.entityId) " +
  	  		"and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.version.id <= :versionId and mapSetRel.entityId = mapSetRel2.entityId ) " +
  	  		"and mapEntry.entityId = mapSetRel.targetEntityId " +
  	  		"and mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.version.id <= :versionId and mapEntry2.entityId = mapEntry.entityId) " +
  	  		"and mapSet.entityId = :mapSetEntityId ";
        
        Query query = session.createQuery(hql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
		return ((Long) query.uniqueResult()).intValue();
	}

	public static List<MapEntryDTO> getEntriesDetails(long mapSetEntityId, Long versionId, boolean includeInactives)
	{
		
		return null;
	}

	/**
	 * Used by TEd project to get a map entry and relationship that is being edited
	 * @param mapEntryEntityId
	 * @param mapSetRelationshipEntityId
	 * @return
	 */
    public static MapEntryDTO get(Long mapEntryEntityId, Long mapSetRelationshipEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String hql = "from MapEntry mapEntry, MapSetRelationship mapSetRel where "+
                "mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.entityId = :mapEntryEntityId) " +
                "and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.entityId = :mapSetRelationshipEntityId) ";

        Query query = session.createQuery(hql);
        query.setLong("mapEntryEntityId", mapEntryEntityId);
        query.setLong("mapSetRelationshipEntityId", mapSetRelationshipEntityId);
        Object[] value =  (Object[]) query.uniqueResult();
        MapEntryDTO mapEntryDTO = new MapEntryDTO((MapEntry)value[0], (MapSetRelationship)value[1]);
            
        return mapEntryDTO;
    }

    public static boolean isConceptCodeInMapEntry(CodeSystem codeSystem, String conceptCode, boolean isSourceSearch)
    {
        Session session = HibernateSessionFactory.currentSession();
        String searchType = isSourceSearch == true ? "source" : "target";
        
        String hql = "from MapSet ms, MapSetRelationship msr, MapEntry me, Version version " +
                "where ms.entityId = msr.sourceEntityId and me.entityId = msr.targetEntityId " +
                "  and version.id = ms."+searchType+"VersionId " +
                "  and version.codeSystem.id = :codeSystemId " +
                "  and me."+searchType+"Code = :conceptCode " +
                "  and ms.id = (select max(ms2.id) from MapSet ms2 where ms.entityId = ms2.entityId) " +
                "  and msr.id = (select max(msr2.id) from MapSetRelationship msr2 where msr2.entityId = msr2.entityId) " +
                "  and me.id = (select max(me2.id) from MapEntry me2 where me.entityId = me2.entityId) ";
        
        Query query = session.createQuery(hql).setLong("codeSystemId", codeSystem.getId()).setString("conceptCode", conceptCode);
        List<Object[]> resultList = (List<Object[]>)query.list();

        return resultList.size() > 0 ? true : false;
    }
	/**
	 * this only gets the map entries that belong to the deployment or cv or version, getEntries() gets them for a full version
	 * @param mapSetEntityId
	 * @param versionId
	 * @param includeInactives
	 * @return
	 */
	 @SuppressWarnings("unchecked")
    public static List<MapEntryDTO> getDeploymentMapEntries(long mapSetEntityId, Long versionId, boolean includeInactives)
    {
        Session session = HibernateSessionFactory.currentSession(); 
    	String statusClause = includeInactives == false ? "and mapSetRel.active = 1 " : "";
    	
		String hql = "select mapEntry, mapSetRel from MapSetRelationship mapSetRel, MapEntry mapEntry where "
				+ "mapSetRel.sourceEntityId = :mapSetEntityId "
				+ "   and mapEntry.entityId = mapSetRel.targetEntityId "
				+ statusClause
				+ "   and (mapEntry.version.id = :versionId or mapSetRel.version.id = :versionId) " // this makes it show only the diff, and not the full
				+ "   and mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.version.id <= :versionId and mapEntry2.entityId = mapEntry.entityId ) "
				+ "   and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.version.id <= :versionId and mapSetRel.entityId = mapSetRel2.entityId ) ";
        
        Query query = session.createQuery(hql);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);

        List<MapEntryDTO> mapEntryDTOs = new ArrayList<MapEntryDTO>();
        List<Object[]> results =  query.list();
        for (Object[] objects : results)
		{
        	MapEntry mapEntry = (MapEntry) objects[0];
        	MapSetRelationship mapSetRelationship = (MapSetRelationship) objects[1];
			MapEntryDTO mapEntryDTO = new MapEntryDTO(mapEntry, mapSetRelationship);
			mapEntryDTOs.add(mapEntryDTO);
		}
        return mapEntryDTOs;  
    }
}
