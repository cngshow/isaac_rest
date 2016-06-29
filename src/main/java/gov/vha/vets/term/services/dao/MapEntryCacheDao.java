package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.api.TerminologyDelegate;
import gov.vha.vets.term.services.dto.MapEntryCacheDTO;
import gov.vha.vets.term.services.dto.MapEntryCacheListDTO;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.StringKeyObjectMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class MapEntryCacheDao
{
	private static final String CACHE_COUNT_MAPSET_VERSION_KEY_PREFIX = "CCMSV:";

    public static MapEntryCacheListDTO getEntries(long mapSetEntityId, Long mapSetVersionId,
    		Long sourceDesignationTypeId, Long targetDesignationTypeId, 
    		Integer mapEntrySequence, Boolean mapEntryStatus,
    		Collection<String> sourceValues, String sourceValueType, Collection<String> targetValues, String targetValueType,
    		String sourceDesignationNameFilter, String targetDesignationNameFilter, Integer pageSize, Integer pageNumber)
    {
        Query query = getEntriesQuery(mapSetEntityId, mapSetVersionId,
        		sourceDesignationTypeId, targetDesignationTypeId, mapEntrySequence, mapEntryStatus,
        		sourceValues, sourceValueType, targetValues, targetValueType,
        		sourceDesignationNameFilter, targetDesignationNameFilter, pageSize, pageNumber);
        

        List<Object[]> objectList = (List<Object[]>)query.list();

        List<MapEntryCacheDTO> mapEntryCaches = new ArrayList<MapEntryCacheDTO>();
        for (Object[] objects : objectList)
    	{
        	long entityId = ((BigDecimal)objects[0]).longValue();
        	long vuid = ((BigDecimal)objects[1]).longValue();
        	int sequence = ((BigDecimal)objects[2]).intValue();
        	int mapEntryActive = ((BigDecimal)objects[3]).intValue();
        	long sourceVersionId = ((BigDecimal)objects[4]).longValue();
        	long sourcePrefDesTypeId = ((BigDecimal)objects[5]).longValue();
        	String sourceConceptCode = (String)objects[6];
        	long srcDesignationTypeId = ((BigDecimal)objects[7]).longValue();
        	String sourceDesignationCode = (String)objects[8];
        	String sourceDesignationName = (String)objects[9];
        	int sourceDesignationActive = ((BigDecimal)objects[10]).intValue();
        	long targetVersionId = ((BigDecimal)objects[11]).longValue();
        	long targetPrefDesTypeId = ((BigDecimal)objects[12]).longValue();
        	String targetConceptCode = (String)objects[13];
        	long trgDesignationTypeId = ((BigDecimal)objects[14]).longValue();
        	String targetDesignationCode = (String)objects[15];
        	String targetDesignationName = (String)objects[16];
        	int targetDesignationActive = ((BigDecimal)objects[17]).intValue();
        	MapEntryCacheDTO mapEntryCacheDTO = new MapEntryCacheDTO(entityId, vuid, sequence, mapEntryActive == 1 ? true : false,
        			sourceVersionId, sourcePrefDesTypeId, sourceConceptCode, srcDesignationTypeId, sourceDesignationCode, sourceDesignationName,
        			sourceDesignationActive == 1 ? true : false, targetVersionId, targetPrefDesTypeId, targetConceptCode, trgDesignationTypeId,
        			targetDesignationCode, targetDesignationName, targetDesignationActive == 1 ? true : false);
        	mapEntryCaches.add(mapEntryCacheDTO);
        }

        MapEntryCacheListDTO mapEntryCacheListDTO = new MapEntryCacheListDTO();
        mapEntryCacheListDTO.setMapEntryCaches(mapEntryCaches);
        
        return mapEntryCacheListDTO;  
    }

    private static Query getEntriesQuery(long mapSetEntityId, Long versionId,
    		Long sourceDesignationTypeId, Long targetDesignationTypeId, Integer mapEntryOrder, Boolean mapEntryStatus,
            Collection<String> sourceValues, String sourceValueType, Collection<String> targetValues, String targetValueType,
            String sourceDesignationNameFilter, String targetDesignationNameFilter, Integer pageSize, Integer pageNumber)
    {
        Session session = HibernateSessionFactory.currentSession();

    	String sourceValuesQuery = "";
    	String targetValuesQuery = "";
    	if (sourceValues != null)
    	{
        	if (TerminologyDelegate.CONCEPT_CODE_TYPE.equals(sourceValueType))
        	{
        		sourceValuesQuery = " and mec.sourceConceptCode in (:sourceConceptCodes) ";
        	}
        	else if (TerminologyDelegate.DESIGNATION_CODE_TYPE.equals(sourceValueType))
        	{
        		sourceValuesQuery = " and mec.sourceDesignationCode in (:sourceDesignationCodes) ";
        	}
        	else if (TerminologyDelegate.DESIGNATION_NAME_TYPE.equals(sourceValueType))
        	{
        		sourceValuesQuery = " and mec.sourceDesignationName in (:sourceDesignationNames) ";
        	}
    	}
    	if (targetValues != null)
    	{
        	if (TerminologyDelegate.CONCEPT_CODE_TYPE.equals(targetValueType))
        	{
        		targetValuesQuery = " and mec.targetConceptCode in (:targetConceptCodes) ";
        	}
        	else if (TerminologyDelegate.DESIGNATION_CODE_TYPE.equals(targetValueType))
        	{
        		targetValuesQuery = " and mec.targetDesignationCode in (:targetDesignationCodes) ";
        	}
        	else if (TerminologyDelegate.DESIGNATION_NAME_TYPE.equals(targetValueType))
        	{
        		targetValuesQuery = " and mec.targetDesignationName in (:targetDesignationNames) ";
        	}
    	}
        String sourceDesignationNameQuery = (sourceDesignationNameFilter != null) ? " and upper(mec.sourceDesignationName) like upper(concat(concat('%',:sourceDesignationName), '%')) " : "";
        String targetDesignationNameQuery = (targetDesignationNameFilter != null) ? " and upper(mec.targetDesignationName) like upper(concat(concat('%',:targetDesignationName), '%')) " : "";
        String mapEntryOrderQuery = (mapEntryOrder != null) ? " and mec.mapentrysequence = :mapEntryOrder " : "";
        String mapEntryStatusQuery = (mapEntryStatus != null) ? " and mec.mapentryactive = :mapEntryStatus " : "";

        String sql = "select mec.mapentryentityid, mec.mapentryvuid, mec.mapentrysequence, mec.mapentryactive, " +
        	" mec.sourceVersionId, mec.sourcePrefDesTypeId, mec.sourceConceptCode, mec.sourceDesignationTypeId, mec.sourceDesignationCode," +
        	" mec.sourceDesignationName, mec.sourceDesignationActive, mec.targetVersionId, mec.targetPrefDesTypeId, mec.targetConceptCode," +
        	" mec.targetDesignationTypeId, mec.targetDesignationCode, mec.targetDesignationName, mec.targetDesignationActive ";
        String sqlMain =
	        " from mapentrycache mec where mec.mapsetentityid = :mapSetEntityId and mec.mapsetversionid = :versionId " +
	        ((sourceDesignationTypeId != null) ?  " and mec.sourceDesignationTypeId = :sourceDesignationTypeId " : "") + 
	        ((targetDesignationTypeId != null) ? "  and mec.targetDesignationTypeId = :targetDesignationTypeId " : "") +
	        sourceValuesQuery + targetValuesQuery + sourceDesignationNameQuery + targetDesignationNameQuery +
	        mapEntryOrderQuery + mapEntryStatusQuery;

        String completeSQL = sql+sqlMain;
        
        Query query = session.createSQLQuery(completeSQL);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.setLong("versionId", versionId);
        if (sourceDesignationTypeId != null)
        {
            query.setLong("sourceDesignationTypeId", sourceDesignationTypeId);
        }
        if (targetDesignationTypeId != null)
        {
            query.setLong("targetDesignationTypeId", targetDesignationTypeId);
        }
    	if (sourceValues != null)
    	{
       		if (TerminologyDelegate.CONCEPT_CODE_TYPE.equals(sourceValueType))
        	{
        	    query.setParameterList("sourceConceptCodes", sourceValues);
        	}
        	else if (TerminologyDelegate.DESIGNATION_CODE_TYPE.equals(sourceValueType))
        	{
        	    query.setParameterList("sourceDesignationCodes", sourceValues);
        	}
        	else if (TerminologyDelegate.DESIGNATION_NAME_TYPE.equals(sourceValueType))
        	{
        	    query.setParameterList("sourceDesignationNames", sourceValues);
        	}
    	}
        if (sourceDesignationNameFilter != null)
        {
            query.setString("sourceDesignationName", sourceDesignationNameFilter);
        }
    	if (targetValues != null)
    	{
       		if (TerminologyDelegate.CONCEPT_CODE_TYPE.equals(targetValueType))
        	{
        	    query.setParameterList("targetConceptCodes", targetValues);
        	}
        	else if (TerminologyDelegate.DESIGNATION_CODE_TYPE.equals(targetValueType))
        	{
        	    query.setParameterList("targetDesignationCodes", targetValues);
        	}
        	else if (TerminologyDelegate.DESIGNATION_NAME_TYPE.equals(targetValueType))
        	{
        	    query.setParameterList("targetDesignationNames", targetValues);
        	}
    	}
        if (targetDesignationNameFilter != null)
        {
            query.setString("targetDesignationName", targetDesignationNameFilter);
        }
        if (mapEntryOrder != null)
        {
            query.setInteger("mapEntryOrder", mapEntryOrder);
        }
        if (mapEntryStatus != null)
        {
            query.setBoolean("mapEntryStatus", mapEntryStatus);
        }

        return query;
    }
    
    public static void updateMapEntryCache(long mapSetEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
    	
        int mapEntryCount = 0;
        
        if (versionId == HibernateSessionFactory.AUTHORING_VERSION_ID)
    	{
	        String sql1 = "delete from mapentrycache mec where mec.mapsetentityid = :mapSetEntityId and mec.mapsetversionid = :versionId";
	        
	        Query query1 = session.createSQLQuery(sql1);
	        query1.setLong("mapSetEntityId", mapSetEntityId);
	        query1.setLong("versionId", versionId);
	        query1.executeUpdate();
    	}
        else // this is a finalized version, see if we already have the data in the table
    	{
        	String cacheCountKey = CACHE_COUNT_MAPSET_VERSION_KEY_PREFIX + mapSetEntityId + "-" + versionId;
        	Integer cacheCount = (Integer) StringKeyObjectMap.getInstance().getObject(cacheCountKey);
        	if (cacheCount == null)
        	{
    	        String sql2 = "select count(*) from mapentrycache mec where mec.mapsetentityid = :mapSetEntityId and mec.mapsetversionid = :versionId";
    	        
    	        Query query2 = session.createSQLQuery(sql2);
    	        query2.setLong("mapSetEntityId", mapSetEntityId);
    	        query2.setLong("versionId", versionId);
    	        Object count = query2.uniqueResult();
    	        mapEntryCount = ((BigDecimal)count).intValue();
    	        if (mapEntryCount > 0)
    	        {
        	        StringKeyObjectMap.getInstance().putObject(cacheCountKey, mapEntryCount);
    	        }
        	}
        	else
        	{
        		mapEntryCount = cacheCount;
        	}
    	}
        
        if (mapEntryCount == 0)
    	{
        	String sql3 =
	        	"INSERT INTO mapentrycache (mapsetentityid, mapsetversionid, mapentryentityid, mapentryvuid, mapentrysequence, mapentryactive, " +
	        	"          sourceversionid, sourceprefdestypeid, sourceconceptcode, sourcedesignationtypeid, sourcedesignationcode, sourcedesignationname, " +
	        	"         sourcedesignationactive, targetversionid, targetprefdestypeid, targetconceptcode, targetdesignationtypeid, targetdesignationcode," +
	        	"         targetdesignationname, targetdesignationactive) " +
	        	"  SELECT :mapSetEntityId, :versionId, me.entity_id, me.vuid, msr.sequence, msr.active, sv.id svid, " +
	        	"            scs.preferred_designation_type_id srcprefdestypeid, sc.code sourcecode, sd.type_id srcdestypeid, " +
	        	"            sd.code srcdescode, sd.name srcdesname, sd.active srcdesactive, tv.id tvid, " +
	        	"            tcs.preferred_designation_type_id trgprefdestypeid, tc.code targetcode, td.type_id trgdestypeid, " +
	        	"            td.code trgdescode, td.name trgdesname, td.active trgdesactive " +
	        	"      FROM concept ms, mapsetextension msx, relationship msr, concept me, mapentryextension mex,  " +
	        	"            version sv, version tv, codesystem scs, codesystem tcs, concept tc, relationship tdr, " +
	        	"            concept td, concept sc, relationship sdr, concept sd " +
	        	"      WHERE  ms.kind = 'M' AND msr.kind = 'M' AND me.kind = 'E' " +
	        	"            AND sc.kind = 'C' AND sdr.kind = 'D' AND sd.kind = 'D' " +
	        	"            AND tc.kind = 'C' AND tdr.kind = 'D' AND td.kind = 'D' " +
	        	"            AND msx.mapsetid = ms.id AND mex.mapentryid = me.id  " +
	        	"            AND msr.source_entity_id = ms.entity_Id AND me.entity_Id = msr.target_entity_id " +
	        	"            AND sv.id = msx.sourceVersionId AND tv.id = msx.targetVersionId " +
	        	"            AND scs.id = sv.codeSystem_id AND tcs.id = tv.codeSystem_id " +
	        	"            AND tc.code = mex.targetCode AND tc.codesystem_id = tv.codesystem_id " +
	        	"            AND tdr.source_entity_id = tc.entity_id AND tdr.target_entity_id = td.entity_id " +
	        	"            AND sc.code = mex.sourceCode AND sc.codesystem_id = sv.codesystem_id " +
	        	"            AND sdr.source_entity_id = sc.entity_id AND sdr.target_entity_id = sd.entity_id  " +
	        	"            AND ms.id = (SELECT MAX (msMax.id) FROM concept msMax WHERE msMax.kind = 'M' AND msMax.version_id <= :versionId AND msMax.entity_id = ms.entity_id)  " +
	        	"            AND msr.id = (SELECT MAX (msrMax.id) FROM relationship msrMax WHERE msrMax.kind = 'M' AND msrMax.version_id <= :versionId AND msrMax.entity_id = msr.entity_id)  " +
	        	"            AND me.id = (SELECT MAX (meMax.id) FROM concept meMax WHERE meMax.version_id <= :versionId AND meMax.entity_id = me.entity_id)  " +
	        	"            AND sc.id = (SELECT MAX (scMax.id) FROM concept scMax WHERE scMax.version_id <= sv.id AND scMax.entity_id = sc.entity_id)  " +
	        	"            AND sdr.id = (SELECT MAX (sdrMax.id) FROM relationship sdrMax WHERE sdrMax.version_id <= sv.id AND sdrMax.entity_id = sdr.entity_id)  " +
	        	"            AND sd.id = (SELECT MAX (sdMax.id) FROM concept sdMax WHERE sdMax.version_id <= sv.id AND sdMax.entity_id = sd.entity_id)  " +
	        	"            AND tc.id = (SELECT MAX (tcMax.id) FROM concept tcMax WHERE tcMax.version_id <= tv.id AND tcMax.entity_id = tc.entity_id)  " +
	        	"            AND tdr.id = (SELECT MAX (tdrMax.id) FROM relationship tdrMax WHERE tdrMax.version_id <= tv.id AND tdrMax.entity_id = tdr.entity_id)  " +
	        	"            AND td.id = (SELECT MAX (tdMax.id) FROM concept tdMax WHERE tdMax.version_id <= tv.id AND tdMax.entity_id = td.entity_id)  " +
	        	"            AND ms.entity_id = :mapSetEntityId  ";

            Query query3 = session.createSQLQuery(sql3);
            query3.setLong("mapSetEntityId", mapSetEntityId);
            query3.setLong("versionId", versionId);
            query3.executeUpdate();
     	}
    }
}
