package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.SubsetDetailDTO;
import gov.vha.vets.term.services.dto.SubsetPublishDetailDTO;
import gov.vha.vets.term.services.dto.SubsetRelationshipDTO;
import gov.vha.vets.term.services.dto.api.SubsetListViewDTO;
import gov.vha.vets.term.services.dto.api.SubsetViewDTO;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

public class SubsetDao extends EntityBaseDao
{
	private final static int QUERY_TYPE_COUNT = 0;
	private final static int QUERY_TYPE_RESULTS = 1;

	@SuppressWarnings("unchecked")
    public static List<Subset> getSubsets(Set<Long> vuids) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset s " +
                        "where s.id in (select max(sub.id) " +
                        "                from Subset sub " +
                        "               where sub.vuid in (:vuids) " +
                        "               group by sub.entityId) " +
                        "order by name";
        List<Subset> list = (List<Subset>) session.createQuery(query).setParameterList("vuids", vuids).list();

        return list;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Subset> getAll() 
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset s " +
                        "where s.id in (select max(sub.id) " +
                        "                from Subset sub " +
                        "               where sub.entityId = s.entityId) " +
                        "order by name";
        List<Subset> list = (List<Subset>) session.createQuery(query).list();

        return list;
    }
    
	/**
	 * Get the highest available version of the named subset
	 * @param name
	 * @return Subset
	 */
	public static Subset getByName(String name)
	{
		Session session = HibernateSessionFactory.currentSession();

		// get subset by it's name
        String query = "from Subset subset where subset.name = :subsetName" +
        				" and subset.id = (select max(subMax.id) " +
        				"     from Subset subMax where subMax.entityId = subset.entityId)";
        Subset subset = (Subset) session.createQuery(query).setString("subsetName", name).uniqueResult();
        
        return subset;
	}

    /**
     * Get the highest available version of the named subset
     * @param name
     * @return Subset
     */
    public static Subset getByCode(String code)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset subset where subset.code = :code" +
                        " and subset.id = (select max(subMax.id) " +
        				"     from Subset subMax where subMax.entityId = subset.entityId)";
        Subset subset = (Subset) session.createQuery(query).setString("code", code).uniqueResult();
        
        return subset;
    }
    
    /**
     * Get a specific subset given its entityId.
     * 
     * @param subsetEntityId
     */
    public static Subset get(long subsetEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Subset subset where subset.id = (select max(sub.id) from Subset sub "
                        + "where sub.entityId = :entityId)";
        Subset subset = (Subset) session.createQuery(query).setLong("entityId", subsetEntityId).uniqueResult();

        return subset;
    }
    
    public static void save(Subset subset)
    {
        HibernateSessionFactory.currentSession().save(subset);
    }
    
    public static void delete(Subset subset)
    {
    	HibernateSessionFactory.currentSession().delete(subset);
    }
    
    public static Subset getVersioned(String code) 
    {
        return getPreviousVersion(code, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }

    /**
     * Get the highest real version of the named Subset
     * @param name
     * @return Subset
     */
    @SuppressWarnings("unchecked")
    public static Subset getPreviousVersion(String code, long versionId)
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String versionQuery = "from Subset s " +
    							"where s.id = (select max(sub.id) " +
    							"from Subset sub " +
    							"where sub.version.id < :version " +
    							"  and sub.code = :code " +
    							"group by sub.entityId)";
    	
    	List<Subset> list = session.createQuery(versionQuery).setLong("version", versionId).setString("code", code).list();
    	if(list.size() == 0)
    	{
    		return null;
    	}
    	
    	return list.get(0);
    }

    // TODO have someone fix this
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        String query = "update Subset s set s.version = :"+NEW_VERSION+" where s.version.id = :"+AUTHORING_VERSION+" and "
            + "s.id in (select sr.sourceEntityId from SubsetRelationship sr where " 
            + "sr.targetEntityId in (select d.id from Designation d, DesignationRelationship dr, CodedConcept cc " 
            + "where d.id = dr.targetEntityId and cc.id = dr.sourceEntityId and cc.id in (:entityId)))";
        
        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }

	/**
	 * @param conceptEntityId
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static List<SubsetRelationshipDTO> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        List<SubsetRelationshipDTO> results = new ArrayList<SubsetRelationshipDTO>();
        
        String subsetQuery = "from Subset s, SubsetRelationship sr " +
                "where s.entityId = sr.sourceEntityId and sr.targetEntityId in " +
                "(select dr.targetEntityId from DesignationRelationship dr where dr.sourceEntityId = :conceptEntityId) and sr.version.id "+operator+" :"+AUTHORING_VERSION+" order by sr.entityId, sr.id";
        Query query = session.createQuery(subsetQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List<Object[]> subsets = query.list();
        
        for(Iterator iter = subsets.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            Subset subset = (Subset) object[0];
            SubsetRelationship subsetRelationship = (SubsetRelationship) object[1];
            SubsetRelationshipDTO subsetDetail = new SubsetRelationshipDTO(subset.getName(), subsetRelationship);
            results.add(subsetDetail);
        }
        return results;
    }

    /**
     * Get a list of SubsetDetailDTO - each one will be the most recent version
     * @param conceptEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<SubsetDetailDTO> getSubsetDetail(long conceptEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();
        List<SubsetDetailDTO> results = new ArrayList<SubsetDetailDTO>();
        
        String subsetQuery = "select s.name, sr.version, sr.entityId, sr.active from Subset s, SubsetRelationship sr " +
                "where s.entityId = sr.sourceEntityId " +
                "and sr.id in (select max(sr2.id) from SubsetRelationship sr2 " +
                "where sr2.targetEntityId in " +
                "(select dr.targetEntityId from DesignationRelationship dr where dr.sourceEntityId = :conceptEntityId)" +
                "   group by sr2.sourceEntityId)";
        System.out.println("ConceptEntityId: "+conceptEntityId);
        List<Object[]> subsets = session.createQuery(subsetQuery).setLong("conceptEntityId", conceptEntityId).list();
        
        for(Iterator iter = subsets.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            String name = (String) object[0];
            Version version = (Version) object[1];
            Long entityId = (Long) object[2];
            Boolean active = (Boolean) object[3];
            SubsetDetailDTO subsetDetail = new SubsetDetailDTO(name, version, entityId, active);
            results.add(subsetDetail);
        }
        return results;
    }

    /**
     * @param conceptEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<SubsetPublishDetailDTO> getSubsetPublishDetail(Collection<Long> conceptEntityIds, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
        List<SubsetPublishDetailDTO> results = new ArrayList<SubsetPublishDetailDTO>();
        
        String subsetQuery = 
                "select s.name, sr.version, sr.entityId, sr.active, d.name, d.vuid, d.entityId, d.active, d.version, dr.sourceEntityId " +
                "from Subset s, SubsetRelationship sr, DesignationRelationship dr, Designation d " +
                "where s.entityId = sr.sourceEntityId and sr.targetEntityId = dr.targetEntityId " +
                "and dr.targetEntityId = d.entityId " +
                "and sr.id in ( select max(sr2.id) from SubsetRelationship sr2 where sr2.targetEntityId = dr.targetEntityId and sr2.sourceEntityId = sr.sourceEntityId and sr2.version.id <= :versionId) " +
                "and d.id in (select max(d2.id) from Designation d2 where d2.version.id <= :versionId and d.entityId = d2.entityId) " +
                "and dr.sourceEntityId in (:conceptEntityIds) " +
                "and dr.id in  " +
                "  (select max(dr2.id) from DesignationRelationship dr2 where dr2.version.id <= :versionId and dr.entityId = dr2.entityId) ";
        List<Object[]> subsets = executeQuery(session.createQuery(subsetQuery).setLong("versionId", versionId), "conceptEntityIds", conceptEntityIds);
        
        for(Iterator iter = subsets.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            String name = (String) object[0];
            Version version = (Version) object[1];
            Long entityId = (Long) object[2];
            Boolean active = (Boolean) object[3];
            String designationName = (String) object[4];
            Long vuid = (Long) object[5];
            Long designationEntityId = (Long) object[6];
            Boolean designationActive = (Boolean) object[7];
            Version designationVersion = (Version) object[8];
            SubsetPublishDetailDTO subsetDetail = new SubsetPublishDetailDTO(name, version, entityId, active, designationEntityId, designationName, vuid, designationActive, designationVersion);
            subsetDetail.setConceptEntityId((Long) object[9]);
            results.add(subsetDetail);
        }
        return results;
    }
    /**
     * get the target concepts preferred designation for the given relationship types 
     * @param conceptEntityId
     * @return
     */    
    @SuppressWarnings("unchecked")
    public static List<SubsetPublishDetailDTO> getSubsetPublishDetailForTargets(Collection<Long> conceptEntityIds, List<String> subsetNames, List<String> relationshipTypeNames, long versionId) 
    {
        Session session = HibernateSessionFactory.currentSession();
        List<SubsetPublishDetailDTO> results = new ArrayList<SubsetPublishDetailDTO>();
        
        String subsetQuery = 
                "select s.name, sr.version, sr.entityId, sr.active, d.name, d.vuid, d.entityId, d.active, d.version, cr.targetEntityId, " +
                "(select targetEntityId from ConceptRelationship where id = (select max(id) from ConceptRelationship rel where rel.entityId = cr.entityId and version.id < :versionId)) as previousConcept, cr.active, cr.entityId " +
                "from Subset s, SubsetRelationship sr, DesignationRelationship dr, Designation d, ConceptRelationship cr " +
                "where s.entityId = sr.sourceEntityId and sr.targetEntityId = dr.targetEntityId " +
                " and s.name in (:subsetNames) " +
                " and dr.targetEntityId = d.entityId and " +
                "d.id in (select max(d2.id) from Designation d2 where d2.version.id <= :versionId and d.entityId = d2.entityId) " +
                "and dr.sourceEntityId = cr.targetEntityId " +
                "and cr.sourceEntityId in (:conceptEntityIds) " +
                "and cr.id in (select max(cr2.id) from ConceptRelationship cr2 where cr.version.id = :versionId and cr.entityId = cr2.entityId)" +
                "and cr.relationshipType.id in (select rt.id from RelationshipType rt where rt.name in (:relationshipTypeNames)) " +
                "and dr.id in " +
                "(select max(dr2.id) from DesignationRelationship dr2 where dr2.version.id <= :versionId and dr.entityId = dr2.entityId) ";
//        List<Object[]> subsets = session.createQuery(subsetQuery).setLong("conceptEntityId", conceptEntityId).setLong("versionId", versionId).setParameterList("subsetNames", subsetNames).setParameterList("relationshipTypeNames", relationshipTypeNames).list();
        Query sessionQuery = session.createQuery(subsetQuery).setLong("versionId", versionId).setParameterList("subsetNames", subsetNames).setParameterList("relationshipTypeNames", relationshipTypeNames);
        List<Object[]> subsets = executeQuery(sessionQuery, "conceptEntityIds", conceptEntityIds);
        
        for(Iterator iter = subsets.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            String name = (String) object[0];
            Version version = (Version) object[1];
            Long entityId = (Long) object[2];
            Boolean active = (Boolean) object[3];
            String designationName = (String) object[4];
            Long vuid = (Long) object[5];
            Long designationEntityId = (Long) object[6];
            Boolean designationActive = (Boolean) object[7];
            Version designationVersion = (Version) object[8];
            Long targetConceptEntityId = (Long) object[9];
            Long previousTargetConceptEntityId = (Long) object[10];
            Boolean conceptRelationshipActive = (Boolean) object[11];
            Long conceptRelationshipEntityId = (Long) object[12];
            SubsetPublishDetailDTO subsetDetail = new SubsetPublishDetailDTO(name, version, entityId, active, designationEntityId, designationName, vuid, designationActive, designationVersion);
            subsetDetail.setPreviousConceptEntityId(previousTargetConceptEntityId);
            subsetDetail.setConceptEntityId(targetConceptEntityId);
            subsetDetail.setConceptRelationshipEntityId(conceptRelationshipEntityId);
            subsetDetail.setConceptRelationshipActive(conceptRelationshipActive);
            results.add(subsetDetail);
        }
        return results;
    }
    /**
     * Get the subset that belong to a particular designation
     * @param designationEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Subset> getSubsets(long designationEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset s where s.id in (select max(sub.id) from Subset sub where sub.entityId in " +
                "(select r.sourceEntityId from SubsetRelationship r where r.targetEntityId = :entityId) group by sub.entityId) ";
        List<Subset> list = session.createQuery(query).setLong("entityId", designationEntityId).list();

        return list;
    }

    /**
     * Get a hash map of subsets for a given list of conceptEntityIds
     * @param conceptEntityId
     * @param versionId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<Long>> getSubsets(List<Long> conceptEntityIds, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String sql = "SELECT DISTINCT sub.name, desrel.sourceEntityId from DesignationRelationship desrel, Subset sub, SubsetRelationship sr " +
	        " WHERE desrel.sourceEntityId in (:conceptEntityIds) " +
	        "   AND desrel.targetEntityId = sr.targetEntityId  " +
	        "   AND sub.entityId = sr.sourceEntityId " +
	        "   AND sub.id = (SELECT MAX(submax.id) from Subset submax where submax.version.id <= :versionId and submax.entityId = sub.entityId) " +
	        "   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.version.id <= :versionId and drmax.entityId = desrel.entityId) " +
	        "   AND sr.id = (SELECT MAX(srmax.id) from SubsetRelationship srmax where srmax.version.id <= :versionId and srmax.entityId = sr.entityId) ";

        Query query = session.createQuery(sql).setLong("versionId", versionId);
        List<Object[]> list = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        Map<String, List<Long>> map = new HashMap<String, List<Long>>();
        for(Iterator iter = list.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            String name = (String) object[0];
            Long conceptEntityId = (Long) object[1];
            List<Long> entityIds = map.get(name);
            if (entityIds == null)
            {
                entityIds = new ArrayList<Long>();
                map.put(name, entityIds);
            }
            entityIds.add(conceptEntityId);
        }
        return map;
    }
    
    /**
     * Get a hash map of subsets for a given list of designationEntityIds
     * @param designationEntityIds
     * @param versionId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, List<Subset>> getSubsetsByDesignationEntityIds(List<Long> designationEntityIds, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String sql = "SELECT sub, sr.targetEntityId from Subset sub, SubsetRelationship sr " +
	        " WHERE sr.targetEntityId in (:designationEntityIds) " +
	        "   AND sub.entityId = sr.sourceEntityId " +
	        "   AND sub.id = (SELECT MAX(submax.id) from Subset submax where submax.version.id <= :versionId and submax.entityId = sub.entityId) " +
	        "   AND sr.id = (SELECT MAX(srmax.id) from SubsetRelationship srmax where srmax.version.id <= :versionId and srmax.entityId = sr.entityId) ";

        Query query = session.createQuery(sql).setLong("versionId", versionId);
        List<Object[]> list = executeQuery(query, "designationEntityIds", designationEntityIds);
        Map<Long, List<Subset>> subsetMap = new HashMap<Long, List<Subset>>();
        for(Iterator iter = list.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            Subset subset = (Subset) object[0];
            Long designationEntityId = (Long) object[1];
            List<Subset> subsets = subsetMap.get(designationEntityId);
            if (subsets == null)
            {
            	subsets = new ArrayList<Subset>();
                subsetMap.put(designationEntityId, subsets);
            }
            subsets.add(subset);
        }
        
        return subsetMap;
    }

    /**
     * Get the subset that belong to a particular codedConcept or designation and a version
     * @param conceptEntityId
     * @param version
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Subset> getSubsets(long conceptEntityId, Version version)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset s where s.id in (select max(sub.id) from Subset sub " +
              "where sub.version.id <= :versionId " +
              "and sub.entityId in " +
              "(select r.sourceEntityId from SubsetRelationship r where r.targetEntityId = :entityId) group by sub.entityId) ";
        List<Subset> list = session.createQuery(query).setLong("versionId", version.getId()).setLong("entityId", conceptEntityId).list();

        return list;
    }

    /**
     * Get the subset that belong to a particular codedConcept or designation that are versioned
     * @param conceptEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Subset> getVersionedSubsets(long conceptEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Subset s where s.id in (select max(sub.id) from Subset sub " +
              "where sub.version.id < :versionId " +
              "and sub.entityId in " +
              "(select r.sourceEntityId from SubsetRelationship r where r.targetEntityId = :entityId) group by sub.entityId) ";
        List<Subset> list = session.createQuery(query).setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID).setLong("entityId", conceptEntityId).list();

        return list;
    }

    /**
     * Get a list of Subsets that are finalized
     * @return List<Subset>
     */
    @SuppressWarnings("unchecked")
	public static List<Subset> getFinalizedSubsets()
    {
    	Session session = HibernateSessionFactory.currentSession();

    	String hql = "from Subset sub where sub.version.id < :authoringId";

    	Query query = session.createQuery(hql);
    	query.setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
    	List<Subset> subsets = (List<Subset>)query.list();
    	
    	return subsets;
    }

    /**
     * Get a list of Subsets from a list of subset names
     * @param subsetNames
     * @return List<Subset>
     */
	@SuppressWarnings("unchecked")
	public static List<Subset> getSubsetsByNames(List<String> subsetNames)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String query = "from Subset s " +
					   "where s.id IN (select max(sub.id)" +
					   "				 from Subset sub " +
					   "			    where sub.name IN (:subsetNames) " +
					   "   				group by sub.entityId)";
		
		List<Subset> subsets = (List<Subset>)session.createQuery(query).setParameterList("subsetNames", subsetNames).list();
		
		return subsets;
	}

    public static Subset getByVuid(long vuid)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String query = "from Subset s " +
                       "where s.id = (select max(sub.id)" +
                       "                 from Subset sub " +
                       "                where sub.vuid = :subsetVuid)";
        
        Subset subset = (Subset)session.createQuery(query).setParameter("subsetVuid", vuid).uniqueResult();
        
        return subset;
    }
    
    public static Subset getByVuid(long vuid, String versionName)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String query = "from Subset s " +
                       "where s.id = (select max(sub.id)" +
                       "                 from Subset sub " +
                       "                where sub.vuid = :subsetVuid and sub.version <= (select v.id from Version v where upper(v.name) = upper(:versionName)" +
                       "                    and v.codeSystem = (select cs.id from CodeSystem cs where upper(cs.name) = upper(:codeSystemName))))";
        
        Subset subset = (Subset)session.createQuery(query)
            .setParameter("versionName", versionName)
            .setParameter("codeSystemName", HibernateSessionFactory.VHAT_NAME)
            .setParameter("subsetVuid", vuid).uniqueResult();
        
        return subset;
    }
    
    public static boolean isSubsetMember(Long subsetEntityId, Long designationEntityId, Version version)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query =  " from SubsetRelationship subRel " +
				    	" where id = (select max(subrelmax.id)  from SubsetRelationship subrelmax where subrelmax.sourceEntityId = :sourceEntityId " +
				    	"				and subrelmax.targetEntityId = :targetEntityId and version.id <=:versionId)";

        SubsetRelationship relationship = (SubsetRelationship)session.createQuery(query)
            .setParameter("versionId", version.getId())
            .setParameter("sourceEntityId", subsetEntityId)
            .setParameter("targetEntityId", designationEntityId).uniqueResult();
        return relationship != null ? relationship.getActive() : false;
    }


    @SuppressWarnings("unchecked")
    public static SubsetListViewDTO getByFilter(String name, String status, Integer pageSize, Integer pageNumber)
    {
    	Long subsetCount = null;

    	if (pageNumber != null && pageNumber == 1)
    	{
    		Query query = getFilterQuery(QUERY_TYPE_COUNT, name, status, pageSize, pageNumber);
    		subsetCount = (Long) query.uniqueResult();
    	}
    	
    	Query query = getFilterQuery(QUERY_TYPE_RESULTS, name, status, pageSize, pageNumber);
    	
        List<Subset> subsetList = (List<Subset>)query.list();
        
        SubsetListViewDTO subsetListViewDto = new SubsetListViewDTO();
        
        for (Subset subset : subsetList)
        {
        	SubsetViewDTO subsetView = new SubsetViewDTO(subset);
        	subsetListViewDto.getSubsetViewDtoDetails().add(subsetView);
        }
        
        if (pageNumber != null && pageNumber == 1)
        {
        	subsetListViewDto.setTotalNumberOfRecords(subsetCount);
        }
        
        return subsetListViewDto;
    }
    
    private static Query getFilterQuery(int queryType, String name, String status, Integer pageSize, Integer pageNumber)
    {
        Session session = HibernateSessionFactory.currentSession();

        String namefilter = " and upper(s.name) like upper(:filter)";
        String statusFilter = " and s.active = :status";
        String hql = "select " + ((queryType == QUERY_TYPE_RESULTS) ? "s" : "count(*)") +
        	" from Subset s where s.id = (select max(sub.id) from Subset sub" +
        	"   where sub.version.id < :authoringVersion and sub.entityId = s.entityId )";

        if (name != null && !name.trim().isEmpty())
        {
            hql = hql.concat(namefilter);
        }
        if (status != null && !status.trim().isEmpty())
        {
            hql = hql.concat(statusFilter);
        }
        Query query = session.createQuery(hql);
        query.setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID);
        if (name != null && !name.trim().isEmpty())
        {
            query.setParameter("filter", "%"+name+"%");
        }
        if (status != null && !status.trim().isEmpty())
        {
            boolean active = "1".equals(status) || "true".equalsIgnoreCase(status); 
            query.setParameter("status", active);
        }

        if (queryType == QUERY_TYPE_RESULTS)
        {
	        query.setFirstResult((pageNumber - 1) * pageSize);
	        query.setMaxResults(pageSize);
        }
        
        return query;
    }
    
	public static List<Subset> getSubsetsByDomain(Long domainVuid, int pageNumber, int pageSize)
	{
		String hqlQuery = 
			"SELECT   subset " +
			"  FROM   CodedConcept domain, " +
			"         ConceptRelationship r, " + 
			"         CodedConcept cSubset, " + 
			"         Subset subset " + 
			" WHERE       r.active = 1 " +
			"         AND r.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
			"         AND domain.vuid = :domainVuid " +
			"         AND domain.entityId = r.targetEntityId " +
			"         AND cSubset.entityId = r.sourceEntityId " +
			"         AND cSubset.name = subset.name " + /*join on the name, until modeling decision comes*/
			"   	  AND domain.id = (SELECT MAX(domainmax.id) from CodedConcept domainmax where domainmax.entityId = domain.entityId ) " +
			"   	  AND cSubset.id = (SELECT MAX(cmax.id) from CodedConcept cmax where cmax.entityId = cSubset.entityId ) " +
			"   	  AND subset.id = (SELECT MAX(subsetmax.id) from Subset subsetmax where subsetmax.entityId = subset.entityId ) ";
		
		Session session = HibernateSessionFactory.currentSession();
		List<Subset> list = session.createQuery(hqlQuery)
			.setParameter("domainVuid", domainVuid)
			.setParameter("typeName", RelationshipType.HAS_PARENT)
			.setFirstResult((pageNumber - 1) * pageSize)
			.setMaxResults(pageSize).list();
		
		return list;
	}


    public static Map<Long, Collection<String>> getVersions(Collection<Long> subsetEntityIds, boolean includeAuthoring)
    {
        Map<Long, Collection<String>> results = new HashMap<Long, Collection<String>>();
        Session session = HibernateSessionFactory.currentSession();

        String authoringQuery = (includeAuthoring) ? "" : " v.id <> :authoringId and ";
        String sqlQuery =
            "select distinct r.source_entity_id, V.NAME from relationship r, concept sub, version v where " +
                "v.id = r.version_id and " + authoringQuery +
                "r.source_entity_id = sub.entity_id and " +
                "r.kind = 'S'  and sub.kind = 'S' and " +
                "sub.entity_id in (:subsetEntityIds) " +
            "union " +
            "select distinct sub.entity_id, V.NAME from relationship r, concept sub, concept des, version v where " +
                "v.id = des.version_id and " + authoringQuery +
                "sub.kind = 'S'  and " +
                "r.kind = 'S' and " +
                "des.kind = 'D' and " +
                "r.source_entity_id = sub.entity_id and " +
                "sub.entity_id in (:subsetEntityIds) and " +
                "des.entity_id = r.target_entity_id";

        Query query = session.createSQLQuery(sqlQuery);
        query.setParameterList("subsetEntityIds", subsetEntityIds);
        if (includeAuthoring == false)
        {
        	query.setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        }
        List<Object[]> subsetVersionNames = query.list();
        for (Object[] objects : subsetVersionNames)
        {
            Long subsetEntityId = ((BigDecimal)objects[0]).longValue();
            Collection<String> versionNames = results.get(subsetEntityId);
            if (versionNames == null)
            {
                versionNames = new HashSet<String>();
                results.put(subsetEntityId, versionNames);
            }
            versionNames.add((String)objects[1]);
        }
        return results;
    }
}
