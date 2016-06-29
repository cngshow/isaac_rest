package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.StateDelegate;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.dto.MapSetDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDetailListDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

public class MapSetDao extends EntityBaseDao
{
	private final static int QUERY_TYPE_COUNT = 0;
	private final static int QUERY_TYPE_RESULTS = 1;

	public static void save(MapSet mapSet)
	{
		HibernateSessionFactory.currentSession().save(mapSet);
	}

	public static MapSet getByVuid(long vuid, long versionId)
	{
		Session session = HibernateSessionFactory.currentSession();
        String query = "from MapSet ms where ms.vuid = :vuid " +
        		"and ms.id = (select max(msMax.id) from MapSet msMax where msMax.version.id <= :versionId and msMax.entityId = ms.entityId)";
		
		MapSet mapSet = (MapSet)session.createQuery(query).setLong("vuid", vuid).setLong("versionId", versionId).uniqueResult();
		
		return mapSet;
	}

	public static Version getCurrentVersionByVuid(long vuid)
	{
		Session session = HibernateSessionFactory.currentSession();

		String sql =
			"select v.* from version v where v.id = " +
			" (select greatest(max(ms.version_id), max(me.version_id), max(msr.version_id)) version_id from concept ms, relationship msr, concept me " +
			"   where msr.source_entity_id = ms.entity_id and msr.target_entity_id = me.entity_id and ms.vuid = :mapSetVuid " +
			"     and ms.id = (select max(id) from concept where kind = 'M' and version_id < :authoringVersionId and entity_id = ms.entity_id) " +
			"     and msr.id = (select max(id) from relationship where kind = 'M' and version_id < :authoringVersionId and entity_id = msr.entity_id) " +
			"     and me.id = (select max(id) from concept where kind = 'E' and version_id < :authoringVersionId and entity_id = me.entity_id)) ";

		Query query = session.createSQLQuery(sql).addEntity("v", Version.class);
		query.setLong("mapSetVuid", vuid).setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		Version version = (Version) query.uniqueResult();

		return version;
	}

	@SuppressWarnings("unchecked")
	public static List<MapSetDesignationDTO> getByVuids(Collection<Long> vuids)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get map sets for the given vuids
        String hql =
        	"from MapSet ms where ms.vuid in (:vuids) " +
        	" and ms.id = (select max(msMax.id) from MapSet msMax where msMax.entityId = ms.entityId) ";

        Query query = session.createQuery(hql);
        List<MapSet> mapSets = executeQuery(query, "vuids", vuids);
        
        List<MapSetDesignationDTO> mapSetDesignationDTOs = new ArrayList<MapSetDesignationDTO>();
        if (!mapSets.isEmpty())
        {
            Map<Long, Designation> mapSetDesignationMap = getMapSetDesignations(mapSets);
            for (MapSet mapSet : mapSets)
    		{
            	Designation designation = mapSetDesignationMap.get(mapSet.getEntityId());
            	MapSetDesignationDTO mapSetDesignationDTO = new MapSetDesignationDTO(mapSet, designation);
            	mapSetDesignationDTOs.add(mapSetDesignationDTO);
    		}
        }
        return mapSetDesignationDTOs;
	}

	private static Map<Long, Designation> getMapSetDesignations(Collection<MapSet> mapSets)
	{
	    Map<Long, Designation> mapSetDesignationMap = new HashMap<Long, Designation>();
	    if (mapSets.size() > 0)
	    {
	        CodeSystem vhatCodeSystem = CodeSystemDao.get(HibernateSessionFactory.VHAT_NAME);

	        List<Long> mapSetEntityIds = new ArrayList<Long>();
	        for (MapSet mapSet : mapSets)
	        {
	            mapSetEntityIds.add(mapSet.getEntityId());
	        }
	        mapSetDesignationMap = DesignationDao.getConceptDescriptionsByEntityIds(vhatCodeSystem,
	                HibernateSessionFactory.AUTHORING_VERSION_ID, mapSetEntityIds);
	    }
		
		return mapSetDesignationMap;
	}

	public static MapSet get(long mapSetEntityId)
    {
        return get(mapSetEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }

    public static List<MapSetDesignationDTO> get(Collection<Long> mapSetEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get map sets for the given entity ids
        String hql =
        	"from MapSet ms where ms.entityId in (:entityIds) " +
        	" and ms.id = (select max(msMax.id) from MapSet msMax where msMax.entityId = ms.entityId) ";

        Query query = session.createQuery(hql);
        List<MapSet> mapSets = executeQuery(query, "entityIds", mapSetEntityIds);
        
        List<MapSetDesignationDTO> mapSetDesignationDTOs = new ArrayList<MapSetDesignationDTO>();
        if (mapSets.size() > 0)
	    {
	        Map<Long, Designation> mapSetDesignationMap = getMapSetDesignations(mapSets);
	        for (MapSet mapSet : mapSets)
			{
	        	Designation designation = mapSetDesignationMap.get(mapSet.getEntityId());
	        	MapSetDesignationDTO mapSetDesignationDTO = new MapSetDesignationDTO(mapSet, designation);
	        	mapSetDesignationDTOs.add(mapSetDesignationDTO);
			}
	    }

        return mapSetDesignationDTOs;
    }
	
	public static MapSet get(Long mapSetEntityId, long versionId)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String hql = "from MapSet ms where ms.id = (select max(ms2.id) from MapSet ms2 "
                        + "where ms2.entityId = :entityId and ms2.version.id <= :version)";

        Query query = session.createQuery(hql);
        query.setLong("entityId", mapSetEntityId);
        query.setLong("version", versionId);
        MapSet mapSet = (MapSet) query.uniqueResult();
        return mapSet;
	}

	public static List<MapSetDesignationDTO> getAll()
	{
        Session session = HibernateSessionFactory.currentSession();

        // get all map sets
        String hql =
        	"from MapSet ms where ms.id = (select max(msMax.id) from MapSet msMax where msMax.entityId = ms.entityId) ";

        Query query = session.createQuery(hql);
        List<MapSet> mapSets = query.list();

        List<MapSetDesignationDTO> mapSetDesignationDTOs = new ArrayList<MapSetDesignationDTO>();
        
        if (!mapSets.isEmpty())
        {
            Map<Long, Designation> mapSetDesignationMap = getMapSetDesignations(mapSets);
            for (MapSet mapSet : mapSets)
    		{
            	Designation designation = mapSetDesignationMap.get(mapSet.getEntityId());
            	MapSetDesignationDTO mapSetDesignationDTO = new MapSetDesignationDTO(mapSet, designation);
            	mapSetDesignationDTOs.add(mapSetDesignationDTO);
    		}
        }
        return mapSetDesignationDTOs;
	}

	public static List<MapSetDesignationDTO> getVersioned(boolean includeAuthoring)
	{
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        List<MapSetDesignationDTO> mapSetDesignations = new ArrayList<MapSetDesignationDTO>();
        
        String hql = "select ms, des from MapSet ms, Designation des, DesignationRelationship dr " +
        		"where ms.entityId = dr.sourceEntityId and des.entityId = dr.targetEntityId " +
        		"  and des.type.id = ms.codeSystem.preferredDesignationType.id and des.active = 1 " +
        		"  and ms.codeSystem.id = (select cs.id from CodeSystem cs where cs.name = '" + HibernateSessionFactory.VHAT_NAME + "') " +
		        "  and des.codeSystem.id = (select cs.id from CodeSystem cs where cs.name = '" + HibernateSessionFactory.VHAT_NAME + "') " + 
        		"  and ms.id = (select max(ms2.id) from MapSet ms2 where ms2.version.id "+operator+" :"+AUTHORING_VERSION+" and ms.entityId = ms2.entityId) " +
        		"  and des.id = (select max(des2.id) from Designation des2 where des2.version.id "+operator+" :"+AUTHORING_VERSION+" and des.entityId = des2.entityId) " +
        		"  and dr.id = (select max(dr2.id) from DesignationRelationship dr2 where dr2.version.id "+operator+" :"+AUTHORING_VERSION+" and dr.entityId = dr2.entityId) ";
        
        Query query = session.createQuery(hql).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List<Object[]> resultList = (List<Object[]>)query.list();
        for (Object object[] : resultList)
        {
        	MapSetDesignationDTO mapSetDesignationDTO = new MapSetDesignationDTO((MapSet)object[0], (Designation)object[1]);
        	mapSetDesignations.add(mapSetDesignationDTO);
		}

        return mapSetDesignations;
	}

	public static List<MapSet> getByEntityId(long entityId)
	{
    	Session session = HibernateSessionFactory.currentSession();
    	String hql = "from MapSet ms where ms.version.id < :"+AUTHORING_VERSION+" and ms.entityId = :mapSetEntityId order by ms.version.id desc";
    	
    	Query query = session.createQuery(hql);
    	query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
    	query.setLong("mapSetEntityId", entityId);
    	List<MapSet> mapSets = (List<MapSet>) query.list();

    	return mapSets;
	}

	@SuppressWarnings("unchecked")
    public static MapSetDetailListDTO getFilteredVersions(String mapSetName, Long sourceCodeSystemVuid, String sourceCodeSystemVersionName,
	        Long targetCodeSystemVuid, String targetCodeSystemVersionName, Boolean mapSetStatus, List<Long> mapSetsNotAccessibleVuidList,
	        Integer pageSize, Integer pageNumber, boolean includeAuthoring)
	{
		Long recordCount = null;
		if (pageNumber != null && pageNumber == 1)
		{
			Query query = getFilteredVersionsQuery(QUERY_TYPE_COUNT, mapSetName, sourceCodeSystemVuid, sourceCodeSystemVersionName,
			        targetCodeSystemVuid, targetCodeSystemVersionName, mapSetStatus, mapSetsNotAccessibleVuidList, pageSize, pageNumber, includeAuthoring);			
			recordCount = ((BigDecimal)query.uniqueResult()).longValue();
		}
		
		Query query = getFilteredVersionsQuery(QUERY_TYPE_RESULTS, mapSetName, sourceCodeSystemVuid, sourceCodeSystemVersionName,
		        targetCodeSystemVuid, targetCodeSystemVersionName, mapSetStatus, mapSetsNotAccessibleVuidList, pageSize, pageNumber, includeAuthoring);			
		
        List<MapSetDetailDTO> mapSetDetails = (List<MapSetDetailDTO>)query.list();
        
        MapSetDetailListDTO mapSetDetailList = new MapSetDetailListDTO();
        mapSetDetailList.setMapSetDetails(mapSetDetails);
        if (pageNumber != null && pageNumber == 1)
        {
            mapSetDetailList.setTotalNumberOfRecords(recordCount);
        }
        
        return mapSetDetailList;
	}
	
	private static Query getFilteredVersionsQuery(int queryType, String mapSetName, Long sourceCodeSystemVuid, String sourceCodeSystemVersionName,
	        Long targetCodeSystemVuid, String targetCodeSystemVersionName, Boolean mapSetStatus, List<Long> mapSetsNotAccessibleVuidList,
	        Integer pageSize, Integer pageNumber, boolean includeAuthoring)
	{
        Session session = HibernateSessionFactory.currentSession();
        
        String mapSetNameQuery = (mapSetName != null) ? "upper(pndes.name) like upper(:mapSetName) and " : "";
        String sourceCodeSystemVuidQuery = (sourceCodeSystemVuid != null) ? "srccs.vuid = :sourceCodeSystemVuid and " : "";
        String sourceCodeSystemVersionNameQuery = (sourceCodeSystemVersionName != null) ? "upper(srcv.name) = upper(:sourceCodeSystemVersionName) and " : "";
        String targetCodeSystemVuidQuery = (targetCodeSystemVuid != null) ? "trgcs.vuid = :targetCodeSystemVuid and " : "";
        String targetCodeSystemVersionNameQuery = (targetCodeSystemVersionName != null) ? "upper(trgv.name) = upper(:targetCodeSystemVersionName) and " : "";
        String mapSetStatusQuery = (mapSetStatus != null) ? "ms.active = :mapSetStatus and " : "";
        String mapSetsNotAccessibleQuery = (mapSetsNotAccessibleVuidList != null && mapSetsNotAccessibleVuidList.size() > 0) ? "ms.vuid not in (:mapSetsNotAccessibleVuids) and " : "";

		String selectWithColumns = 
			"select ms.id \"MapSetIdBigDecimal\", ms.entity_id \"MapSetEntityIdBigDecimal\", ms.name \"MapSetName\", " +
			"    ms.vuid \"mapSetVuidBigDecimal\", ms.active \"MapSetActiveBigDecimal\", " +
			"    srccs.id \"SourceCodeSystemIdBigDecimal\", srccs.name \"SourceCodeSystemName\"," +
            "    srccs.vuid \"SourceCodeSystemVuidBigDecimal\", srccstype.name \"SourceCodeSystemPrefDesName\"," +
			"    srcv.id \"SourceVersionIdBigDecimal\", srcv.name \"SourceVersionName\", " +
			"    trgcs.id \"TargetCodeSystemIdBigDecimal\", trgcs.name \"TargetCodeSystemName\"," +
            "    trgcs.vuid \"TargetCodeSystemVuidBigDecimal\", trgcstype.name \"TargetCodeSystemPrefDesName\"," +
			"    trgv.id \"TargetVersionIdBigDecimal\", trgv.name \"TargetVersionName\", " +
			"    msversions2.versionId \"ActualVersionIdBigDecimal\"," +
			"    (select name from version where id = msversions2.versionId) \"ActualVersionName\"," +
            "    (select effectiveDate from version where id = msversions2.versionId) \"ActualVersionEffectiveDate\"," +
			"    pndes.name \"PreferredName\", fsndes.name \"FullySpecifiedName\", " +
			"    cg.changedate \"LastUpdated\", msversions2.state \"MapSetStateIdBigDecimal\", msversions2.count \"ChangeCountBigDecimal\" ";
		String operator = (includeAuthoring) ? "<=" : "<";
		String sqlQuery =
			((queryType == QUERY_TYPE_RESULTS) ? selectWithColumns : "select count(*)") +
			"  from concept ms, mapsetextension msx, codesystem srccs, codesystem trgcs, type srccstype, type trgcstype, version srcv, version trgv," +
			"          concept pndes, concept fsndes, relationship pndr, relationship fsndr, codesystem pncs, changegroup cg, " +
			"      (select  msversions.mapset_entityId, msversions.versionId, " +
			"           (select count (msr.id) from concept me, relationship msr " +
			"                 where me.kind = 'E' and msr.kind = 'M' " +
			"                       and msr.target_entity_id = me.entity_id " +
			"                       and msr.source_entity_id = msversions.mapset_entityId " +
			"                       and (msr.version_id = msversions.versionId or me.version_id = msversions.versionId) " +
			"                       and me.id = (select max(meMax.id) from concept meMax where meMax.version_id <= msversions.versionId and meMax.entity_id = me.entity_id) " +
			"                       and msr.id = (select max(msrMax.id) from relationship msrMax where msrMax.version_id <= msversions.versionId and msrMax.entity_id = msr.entity_id)) " +
			"              count, " +
			"           (select GREATEST(NVL(" + 
			"              (select MAX(GREATEST (NVL(me.changegroup_id,0), NVL(msr.changegroup_id,0))) " +
			"                from concept me, relationship msr  " +
			"                  where me.kind = 'E' and msr.target_entity_id = me.entity_id and msr.source_entity_id = msversions.mapset_entityId and " +
			"                        msr.version_id <= msversions.versionId and me.version_id <= msversions.versionId and " +
			"                        (me.version_id = msversions.versionId or msr.version_id = msversions.versionId)), 0), " +
			"               NVL((select max(msp.changegroup_ID) from property msp where msp.conceptentity_id = msversions.mapset_entityId " +
			"				         and msp.version_id <= msversions.versionId),0), " +
            "               NVL((select max(des.changegroup_ID) from concept des, relationship dr " +
            "                  where des.kind = 'D' and dr.kind = 'D' and dr.source_entity_id = msversions.mapset_entityId and dr.target_entity_id = des.entity_id " +
            "							and des.version_id <= msversions.versionId), 0), " +
			"              (select max(ms2.changegroup_ID) from concept ms2 where ms2.kind = 'M' AND ms2.entity_id = msversions.mapset_entityId " +
			"                        and ms2.version_id <= msversions.versionId)) from dual) " +
			"                maxchangegroupId, " +
			"           (select cs.state_id from conceptstate cs where cs.concept_entity_id = msversions.mapset_entityId )" +
			"             state " +
			"     from   " +
			"       (select msr.source_entity_id mapset_entityId, me.version_id versionId from relationship msr, concept me, mapentryextension mee where  " +
			"                   me.id = mee.mapentryid and me.entity_id = msr.target_entity_id " +
			"         UNION " +
			"         select msr.source_entity_id, msr.version_id from relationship msr, concept me, mapentryextension mee " +
			"                where me.id = mee.mapentryid and me.entity_id = msr.target_entity_id " +
			"         UNION " +
			"         select ms.entity_id, des.version_id from concept ms, relationship dr, concept des, codesystem pncs " +
			"                where ms.entity_id = dr.source_entity_id and dr.target_entity_id = des.entity_id " +
			"                  and pncs.id = des.codesystem_id and des.type_id = pncs.preferred_designation_type_id " +
			"         UNION " +
			"         select ms.entity_id, prop.version_id from concept ms, property prop, type t " +
			"                where ms.entity_id = prop.conceptentity_id and t.id = prop.propertytype_id and t.name = :pttextdef " + 
			"         UNION " +
			"         select ms.entity_id, prop.version_id from concept ms, property prop, type t " +
			"                where ms.entity_id = prop.conceptentity_id and t.id = prop.propertytype_id and t.name = :ptdesc " + 
			"         UNION " +
			"         select ms.entity_id, des.version_id from concept ms, relationship dr, concept des " +
			"              where ms.entity_id = dr.source_entity_id and dr.target_entity_id = des.entity_id " +
			"                and des.type_id = (SELECT id FROM TYPE WHERE name = 'Fully Specified Name') " + 
			"         UNION " +
			"         select ms.entity_id, ms.version_id from concept ms where ms.kind = 'M') msversions) msversions2 " +
			"  where ms.entity_id = msversions2.mapset_entityId and  " +
			"       pndes.kind = 'D'  and pndr.kind = 'D' and fsndes.kind = 'D' and fsndr.kind = 'D' and pndes.active = 1 and " +
			"       msx.mapsetid = ms.id and srccs.id = srcv.codesystem_id and trgcs.id = trgv.codesystem_id and " +
            "       srccstype.id = srccs.preferred_designation_type_id and trgcstype.id = trgcs.preferred_designation_type_id and " +
			"       srcv.id = msx.sourceversionid and trgv.id = msx.targetversionid and " +
			"       ms.entity_id = pndr.source_entity_id and pndes.entity_id = pndr.target_entity_id and " +
			"       ms.entity_id = fsndr.source_entity_id and fsndes.entity_id = fsndr.target_entity_id and " +
			"       pncs.id = pndes.codesystem_id and pncs.preferred_designation_type_id = pndes.type_id and  " +
			"       fsndes.type_id = (select id from type where name = :fsn) and cg.id = msversions2.maxchangegroupId and " +
			"       " + mapSetNameQuery + mapSetStatusQuery + sourceCodeSystemVuidQuery + sourceCodeSystemVersionNameQuery + 
			"       " + targetCodeSystemVuidQuery + targetCodeSystemVersionNameQuery +
			"       msversions2.versionId "+operator+" :authoringVersionId and " + mapSetsNotAccessibleQuery +
			"       ms.id = (select max(msmax.id) from concept msmax where msmax.kind = 'M' and " +
			"       msmax.version_id <= msversions2.versionId and msmax.entity_id = msversions2.mapset_entityid) and " +
			"       pndes.id = (select max(desMax.id) from concept  desMax where desMax.kind = 'D' and desMax.version_id <= msversions2.versionId and desMax.entity_id = pndes.entity_id) and " +
			"       fsndes.id = (select max(desMax.id) from concept  desMax where desMax.kind = 'D' and desMax.version_id <= msversions2.versionId and desMax.entity_id = fsndes.entity_id) and " +
			"       pndr.id = (select max(drMax.id) from relationship  drMax where drMax.kind = 'D' and drMax.version_id <= msversions2.versionId and drMax.entity_id = pndr.entity_id) and " +
			"       fsndr.id = (select max(drMax.id) from relationship  drMax where drMax.kind = 'D' and drMax.version_id <= msversions2.versionId and drMax.entity_id = fsndr.entity_id) " +
			((queryType == QUERY_TYPE_RESULTS) ? " order by \"mapSetVuidBigDecimal\", \"ActualVersionIdBigDecimal\" desc" : "");

		Query query = null;
        if (queryType == QUERY_TYPE_RESULTS)
        {
            query = session.createSQLQuery(sqlQuery).setResultTransformer(Transformers.aliasToBean(MapSetDetailDTO.class));
        }
        else
        {
            query = session.createSQLQuery(sqlQuery);
        }
        query.setString("fsn", DesignationType.FULLY_SPECIFIED_NAME);
        query.setString("ptdesc", PropertyType.DESCRIPTION);
        query.setString("pttextdef", PropertyType.TEXT_DEFINITION);
        query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        if (mapSetName != null)
        {
            query.setString("mapSetName", "%"+mapSetName+"%");
        }
        if (mapSetStatus != null)
        {
            int status = ("true".equalsIgnoreCase(mapSetStatus.toString()) == true) ? 1 : 0;
            query.setInteger("mapSetStatus", status);
        }
        if (sourceCodeSystemVuid != null)
        {
            query.setLong("sourceCodeSystemVuid", sourceCodeSystemVuid);
        }
        if (sourceCodeSystemVersionName != null)
        {
            query.setString("sourceCodeSystemVersionName", sourceCodeSystemVersionName);
        }
        if (targetCodeSystemVuid != null)
        {
            query.setLong("targetCodeSystemVuid", targetCodeSystemVuid);
        }
        if (targetCodeSystemVersionName != null)
        {
            query.setString("targetCodeSystemVersionName", targetCodeSystemVersionName);
        }
        if (mapSetsNotAccessibleVuidList != null && mapSetsNotAccessibleVuidList.size() > 0)
        {
        	query.setParameterList("mapSetsNotAccessibleVuids", mapSetsNotAccessibleVuidList);
        }
        if (queryType == QUERY_TYPE_RESULTS && pageNumber != null && pageSize != null)
        {
            query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
        }
        
        return query;
	}
	
    @SuppressWarnings("unchecked")
	public static void deleteAuthoringVersion(long mapSetEntityId) throws STSException
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String stateQuery =
    		"from ConceptState where conceptEntityId = :mapSetEntityId";
    	ConceptState conceptState = (ConceptState) session.createQuery(stateQuery).setLong("mapSetEntityId", mapSetEntityId).uniqueResult();
    	if (conceptState != null && conceptState.getState().getType().equals(State.INITIAL) == false)
    	{
    		throw new STSException("Map Set must be in the state: "+StateDelegate.getByType(State.INITIAL).getName()+", currently it is in the state: " +
    				conceptState.getState().getName()+".");
    	}

    	// delete the concept state object
    	if (conceptState != null)
    	{
            session.delete(conceptState);
    	}

    	String versionQuery =
	    	"select msr.entity_id msr_entityid, msr.version_id msr_versionid, me.entity_id me_entityid, me.version_id me_versionid" +
	    	"  from relationship msr, concept me, mapentryextension msx " +
	    	"    where msr.kind = 'M' and me.kind = 'E' and MSX.MAPENTRYID = me.id " +
	    	"      and msr.source_entity_id = :mapSetEntityId and msr.target_entity_id = me.entity_id " +
	    	"      and msr.id = (select max(msrmax.id) from relationship msrmax where msrmax.kind = 'M' and msrmax.entity_id = msr.entity_id) " +
	    	"      and me.id = (select max(memax.id) from concept memax where memax.kind = 'E' and memax.entity_id = me.entity_id) ";
    	List<Object[]> entityVersionList = session.createSQLQuery(versionQuery).setLong("mapSetEntityId", mapSetEntityId).list();

    	List<Long> meEntityAuthoringList = new ArrayList<Long>();
    	for (Object[] object : entityVersionList)
		{
    		long meEntityId = ((BigDecimal)object[2]).longValue();
    		long meVersionId = ((BigDecimal)object[3]).longValue();
    		if (meVersionId == HibernateSessionFactory.AUTHORING_VERSION_ID)
    		{
    			meEntityAuthoringList.add(meEntityId);
    		}
		}

    	String mapEntryExceptionQuery =
	    	"select msr.targetEntityId from MapSetRelationship msr " +
	    	"  where msr.sourceEntityId <> :mapSetEntityId and msr.targetEntityId in (:mapEntryEntityIds) ";
        HashSet targetEntityIdsSet = new HashSet<Long>();
        if (meEntityAuthoringList.size() > 0)
	    {
	        Query sessionQuery = session.createQuery(mapEntryExceptionQuery);
	        sessionQuery.setLong("mapSetEntityId", mapSetEntityId);
	        List<Long> targetEntityIds = executeQuery(sessionQuery, "mapEntryEntityIds", meEntityAuthoringList);
	        for (Long entityId : targetEntityIds)
			{
	        	targetEntityIdsSet.add(entityId);
			}
	    }
        

    	HashSet msrEntitySet = new HashSet<Long>();
    	HashSet meEntitySet = new HashSet<Long>();
    	for (Object[] object : entityVersionList)
		{
    		long msrEntityId = ((BigDecimal)object[0]).longValue();
    		long msrVersionId = ((BigDecimal)object[1]).longValue();
    		if (msrVersionId == HibernateSessionFactory.AUTHORING_VERSION_ID)
    		{
				msrEntitySet.add(msrEntityId);
    		}
    		long meEntityId = ((BigDecimal)object[2]).longValue();
    		long meVersionId = ((BigDecimal)object[3]).longValue();
    		if (meVersionId == HibernateSessionFactory.AUTHORING_VERSION_ID && !targetEntityIdsSet.contains(meEntityId))
			{
				meEntitySet.add(meEntityId);
			}
		}
    	List<Long> msrEntityIdList = new ArrayList<Long>(msrEntitySet);
    	List<Long> meEntityIdList = new ArrayList<Long>(meEntitySet);
    	
    	List<Long> propertyConceptEntityIdList = new ArrayList<Long>();
    	propertyConceptEntityIdList.addAll(meEntityIdList);
    	propertyConceptEntityIdList.add(mapSetEntityId);
    	String propertyQuery = "delete Property where version.id = :authoringId and conceptEntityId in (:entityIdList)";
    	Query query = session.createQuery(propertyQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
    	executeUpdate(query, "entityIdList", propertyConceptEntityIdList);

    	if (meEntityIdList.size() > 0)
    	{
    		String mapEntryQuery = "delete MapEntry where version.id = :authoringId and entityId in (:entityIdList)";
    		query = session.createQuery(mapEntryQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
    		executeUpdate(query, "entityIdList", meEntityIdList);
    	}

    	if (msrEntityIdList.size() > 0)
    	{
    		String mapSetRelationshipQuery = "delete MapSetRelationship where version.id = :authoringId and entityId in (:entityIdList)";
    		query = session.createQuery(mapSetRelationshipQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
    		executeUpdate(query, "entityIdList", msrEntityIdList);
    	}
    	
    	String mapSetDesignationQuery = "delete Designation where id in (select d.id from DesignationRelationship dr, Designation d " + 
    	    "where dr.sourceEntityId = :mapSetEntityId and dr.targetEntityId = d.entityId and d.version.id = :authoringId)";
        query = session.createQuery(mapSetDesignationQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.executeUpdate();
    	
        String mapSetDesignationRelationshipQuery = "delete DesignationRelationship where id in (select dr.id from DesignationRelationship dr " + 
            "where dr.sourceEntityId = :mapSetEntityId and dr.version.id = :authoringId)";
        query = session.createQuery(mapSetDesignationRelationshipQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setLong("mapSetEntityId", mapSetEntityId);
        query.executeUpdate();

        String mapSetQuery = "delete MapSet where version.id = :authoringId and entityId = :entityId";
    	query = session.createQuery(mapSetQuery).setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
    	query.setLong("entityId", mapSetEntityId);
    	query.executeUpdate();

    	session.flush();
    }

	public static List<MapSet> getByReferencedVersionIds(List<Long> versionIds)
	{
    	Session session = HibernateSessionFactory.currentSession();
    	String mapSetQuery = "from MapSet where sourceVersionId in (:versionIds) or targetVersionId in (:versionIds)";
    	List<MapSet> mapSetList = (List<MapSet>) session.createQuery(mapSetQuery).setParameterList("versionIds", versionIds).list();

		return mapSetList;
	}

	public static boolean isPreferredDesignationUnique(String mapSetPreferredName)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String sql = "select ms.entity_id from concept ms, relationship r, concept d, codesystem cs, type t where ms.kind='M' and ms.entity_id = r.source_entity_id " +
		"                and r.kind ='D' and r.target_entity_id = d.entity_id and d.kind = 'D' and d.name=:name and ms.codesystem_id = cs.id  " +
		"                and cs.preferred_designation_type_id = t.id  ";

		Query query = session.createSQLQuery(sql).setString("name", mapSetPreferredName);
		List<?> result = query.list();

		return result.size()==0;
	}
	
	@SuppressWarnings("unchecked")
    public static List<MapSet> getAllMapSetVersions(long mapSetEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
		
        String mapSetQuery = "from MapSet ms where ms.entityId = :conceptEntityId and ms.version.id "+operator+" :"+AUTHORING_VERSION+" order by ms.id desc";
        List<MapSet> mapSets = session.createQuery(mapSetQuery).setLong("conceptEntityId",
                mapSetEntityId).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).list();

        return mapSets;
    }
	
}
