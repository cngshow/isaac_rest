package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.CodedConceptDesignationDTO;
import gov.vha.vets.term.services.dto.CodedConceptListDTO;
import gov.vha.vets.term.services.dto.ConceptHierarchyDTO;
import gov.vha.vets.term.services.dto.api.CodedConceptListViewDTO;
import gov.vha.vets.term.services.dto.api.TotalEntityListView;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class CodedConceptDao extends EntityBaseDao
{
	private final static int QUERY_TYPE_COUNT = 0;
	private final static int QUERY_TYPE_RESULTS = 1;

	/**
	 * Get the list of concepts for a code system and version
	 * 
	 * @param codeSystem
	 * @param version
	 * @return List List of CodedConcepts
	 */
	@SuppressWarnings("unchecked")
    public static List<CodedConcept> getCodedConcepts(CodeSystem codeSystem, Version version)
	{
		Session session = HibernateSessionFactory.currentSession();

		// get all concepts for the given code system and version
		String query = "from CodedConcept cc where cc.codeSystem.id = :codeSystemId " +
				"and cc.id = (select max(con.id) from CodedConcept con " +
				"      where con.version.id <= :versionId and cc.codeSystem.id = :codeSystemId and con.entityId = cc.entityId) order by code";
        List<CodedConcept> list = session.createQuery(query).setLong("versionId", version.getId()).setLong("codeSystemId", codeSystem.getId()).list();

		return list;
	}

    /**
     * Get the list of concepts for a code system and version
     * 
     * @param codeSystem
     * @param version
     * @return List List of CodedConcepts
     */
    @SuppressWarnings("unchecked")
    public static CodedConceptListViewDTO getCodedConcepts(CodeSystem codeSystem, Version version, String designationNameFilter,
            String conceptCodeFilter, Boolean conceptStatusFilter, Integer pageSize, Integer pageNumber)
    {
    	Long conceptCount = null;
    	if (pageNumber == 1)
    	{
            Query query = getCodedConceptQuery(QUERY_TYPE_COUNT, codeSystem, version, designationNameFilter,
                    conceptCodeFilter, conceptStatusFilter, pageSize, pageNumber);
            
            conceptCount = (Long) query.uniqueResult();
    	}

        Query query = getCodedConceptQuery(QUERY_TYPE_RESULTS, codeSystem, version, designationNameFilter,
                conceptCodeFilter, conceptStatusFilter, pageSize, pageNumber);

        List<CodedConcept> codedConcepts = query.list();
        
        CodedConceptListViewDTO codedConceptListView = new CodedConceptListViewDTO(codedConcepts);
		codedConceptListView.setTotalNumberOfRecords(conceptCount);

        return codedConceptListView;
    }

    private static Query getCodedConceptQuery(int queryType, CodeSystem codeSystem, Version version, String designationNameFilter,
            String conceptCodeFilter, Boolean conceptStatusFilter, Integer pageSize, Integer pageNumber)
    {
        Session session = HibernateSessionFactory.currentSession();

        String conceptCodeFilterQuery = (conceptCodeFilter != null) ? " and cc.code = :conceptCode " : "";
        String conceptStatusFilterQuery = (conceptStatusFilter != null) ? " and cc.active = :conceptStatus " : "";
        String desNameFilterTables = (designationNameFilter != null) ? ", Designation des, DesignationRelationship dr " : "";
        String desNameFilterJoin = (designationNameFilter != null) ?
                " and dr.sourceEntityId = cc.entityId and dr.targetEntityId = des.entityId and upper(des.name) like upper(:designationName)" +
                " and dr.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.version.id <= :versionId and drmax.entityId = dr.entityId)" +
                " and des.id = (select max(desmax.id) from Designation desmax where desmax.version.id <= :versionId and desmax.entityId = des.entityId) " : "";

        // get all concepts for the given code system and version
        String hqlQuery =
        		"select distinct " + ((queryType == QUERY_TYPE_RESULTS) ? "cc" : "count(*)") +
        		" from CodedConcept cc " + desNameFilterTables + " where cc.codeSystem.id = :codeSystemId " +
                conceptCodeFilterQuery + conceptStatusFilterQuery + desNameFilterJoin +
                "and cc.id = (select max(ccmax.id) from CodedConcept ccmax " +
                "      where ccmax.version.id <= :versionId and ccmax.codeSystem.id = :codeSystemId and ccmax.entityId = cc.entityId) order by cc.code";
        Query query = session.createQuery(hqlQuery);
        query.setLong("codeSystemId", codeSystem.getId());
        query.setLong("versionId", version.getId());
        if (conceptCodeFilter != null)
        {
            query.setString("conceptCode", conceptCodeFilter);
        }
        if (conceptStatusFilter != null)
        {
            query.setInteger("conceptStatus", (conceptStatusFilter.toString().equalsIgnoreCase("true") == true) ? 1 : 0);
        }
        if (designationNameFilter != null)
        {
            query.setString("designationName", "%"+designationNameFilter+"%");
        }

        if (queryType == QUERY_TYPE_RESULTS)
        {
            query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
        }
        
        return query;
    }

    /**
     * Get a specific concept for a specific version.
     * 
     * @param codeSystem
     * @param code
     * @param version
     * @return CodedConcept
     * @throws STSNotFoundException
     */
    public static List<CodedConcept> getAll(Collection<Long> conceptEntityIds, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String sqlQuery = "from CodedConcept cc where entity_id in (:conceptEntityIds) " +
        		"and cc.id = (select max(con.id) from CodedConcept con where con.version.id <= :version and cc.entityId = con.entityId)";
        Query query = session.createQuery(sqlQuery).setLong("version", versionId);
        List<CodedConcept> codedConcepts = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        
        return codedConcepts;
    }
    
    /**
     * Get a specific concept for a specific version.
     * 
     * @param codeSystem
     * @param code
     * @param version
     * @return CodedConcept
     * @throws STSNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static List<CodedConcept> get(Collection<Long> conceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concepts for the given code
        String query = "from CodedConcept cc where cc.id in (select max(con.id) from CodedConcept con "
                        + "where con.entityId in (:entityIds) group by entity_Id)";
        Query sessionQuery = session.createQuery(query);
        
        return executeQuery(sessionQuery, "entityIds", conceptEntityIds);
    }
    
    
    /**
	 * Get a concept that is previous than the version specified
	 * 
	 * @param codeSystem
	 * @param code
	 * @return CodedConcept
	 * @throws STSNotFoundException
	 */
    @SuppressWarnings("unchecked")
    public static HashMap<Long, CodedConcept> getPreviousConceptsForVersionMap(Collection<Long> codedConceptEntityIds, long versionId) 
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String hqlQuery = "FROM CodedConcept cc " +
    						  "WHERE cc.entityId IN (:codedConceptEntityIds) AND cc.id = (SELECT MAX(con.id) FROM CodedConcept con " +
    						  "  WHERE con.version.id < :version AND con.entityId = cc.entityId)";
    	
    	Query query = session.createQuery(hqlQuery).setLong("version", versionId);
    	List<CodedConcept> concepts = executeQuery(query, "codedConceptEntityIds", codedConceptEntityIds);
    	
    	HashMap<Long, CodedConcept> codedConceptMap = new HashMap<Long, CodedConcept>();
    	for (CodedConcept codedConcept : concepts)
    	{
    		codedConceptMap.put(codedConcept.getEntityId(), codedConcept);
    	}
    	
    	return codedConceptMap;
    }

    @SuppressWarnings("unchecked")
    public static List<CodedConcept> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
		
        String conceptQuery = "from CodedConcept c where c.entityId = :conceptEntityId and c.version.id "+operator+" :"+AUTHORING_VERSION+" order by c.id";
        List<CodedConcept> codedConcepts = session.createQuery(conceptQuery).setLong("conceptEntityId",
                conceptEntityId).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).list();

        return codedConcepts;
    }

    @SuppressWarnings("unchecked")
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String conceptQuery = "Select c.entityId from CodedConcept c where c.version.id = :versionId";
        List<Long> conceptEntityIds = session.createQuery(conceptQuery).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }

    @SuppressWarnings("unchecked")
    public static List<CodedConcept> getCodedConceptsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String sqlQuery =
        	"select {con.*} from concept con where con.entity_id in ( " +
        	"  select entity_id from concept con where con.kind = 'C' and version_id = :versionId " +
        	"UNION " +
        	"  select p.conceptentity_id entity_id from property p where version_id = :versionId " +
        	"UNION " +
        	"  select dr.source_entity_id from concept des, relationship dr " +
        	"    where des.kind = 'D' and dr.kind = 'D' " +
        	"      and dr.target_entity_id = des.entity_id " +
        	"      and des.version_id = :versionId) order by con.vuid"; 

        Query query = session.createSQLQuery(sqlQuery).addEntity("con", CodedConcept.class).setLong("versionId", versionId);
        List<CodedConcept> codedConcepts = query.list();
        
        return codedConcepts;
    }

    @SuppressWarnings("deprecation")
    public static void checkForDuplicateVUIDs() throws STSException
    {
        Connection connection = HibernateSessionFactory.currentSession().connection();
        PreparedStatement statement;
        StringBuffer duplicateVUIDs = new StringBuffer();
        
		try
		{
			statement = connection.prepareStatement("select vuid from (select distinct vuid, entity_Id from Concept) group by vuid having count(vuid) > 1");
	        ResultSet resultSet = statement.executeQuery();
	        while (resultSet.next())
	        {
	            Long vuid = resultSet.getLong("vuid");
	            if (duplicateVUIDs.length() > 0)
	            {
	            	duplicateVUIDs.append(", ");
	            }
	            duplicateVUIDs.append(vuid.toString());
	        }

	        if (duplicateVUIDs.length() > 0)
	        {
	        	// found a duplicate vuid - throw an exception
				throw new STSException("Found duplicate VUID(s): " + duplicateVUIDs.toString());
	        }
		}
		catch (SQLException e)
		{
            e.printStackTrace();
            throw new STSException(e); 
		}

    }

	public static CodedConcept getConceptFromDesignationCode(String designationCode)
	{
        Session session = HibernateSessionFactory.currentSession();

        String query = "from CodedConcept con, DesignationRelationship desrel, Designation des " +
        	" WHERE des.code = :designationCode " +
            "   AND con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
            "   AND des.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.entityId = des.entityId) " +
            "   AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId) " +
            "   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = desrel.entityId) ";

        Object[] objects = (Object[]) session.createQuery(query).setString("designationCode", designationCode).uniqueResult();
        CodedConcept codedConcept = (CodedConcept) objects[0];

        return codedConcept;
	}
    
    public static CodedConcept getConceptFromDesignationVuid(long designationVuid)
    {
        Session session = HibernateSessionFactory.currentSession();
        CodedConcept codedConcept = null;
        
        String query = "from CodedConcept con, DesignationRelationship desrel, Designation des " +
            " WHERE des.vuid = :designationVuid " +
            "   AND con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
            "   AND des.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.entityId = des.entityId) " +
            "   AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId) " +
            "   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = desrel.entityId) ";

        Object[] objects = (Object[]) session.createQuery(query).setLong("designationVuid", designationVuid).uniqueResult();
        if (objects != null)
        {
            codedConcept = (CodedConcept) objects[0];
        }

        return codedConcept;
    }
    
    /**
     * Get all CodedConcepts with relationships to passed designation VUIDs
     * @param designationVuids
     * @return List<CodedConcept>
     */
	@SuppressWarnings("unchecked")
    public static List<CodedConceptDesignationDTO> getConceptsFromDesignationVuids(List<Long> designationVuids)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String query = "from CodedConcept con, DesignationRelationship desrel, Designation des " +
        	" WHERE des.vuid in (:designationVuids) " +
        	"   AND con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
        	"   AND des.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.entityId = des.entityId) " +
        	"   AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId) " +
        	"   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = desrel.entityId) ";
		
		List<Object[]> objects = (List<Object[]>) session.createQuery(query).setParameterList("designationVuids", designationVuids).list();
		
		List<CodedConceptDesignationDTO> codedConceptDesignations = new ArrayList<CodedConceptDesignationDTO>();
		for(Object[] myObject : objects)
		{
			codedConceptDesignations.add(new CodedConceptDesignationDTO((CodedConcept)myObject[0], (Designation)myObject[2]));
		}
		
		return codedConceptDesignations;
	}

	public static CodedConcept get(String name, Version version)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String query = "from CodedConcept cc  " +
						"  where cc.name = :name " +
						"  and cc.version.id = :versionId ";

		CodedConcept concept = (CodedConcept)session.createQuery(query)
								.setString("name", name).setLong("versionId", version.getId()).uniqueResult();
		
		return concept;
	}

    public static List<CodedConceptDesignationDTO> getConceptWithMatchingDesignationsAndRoot(
            String designationName, String rootName)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String hqlQuery =
            "select con, des from CodedConcept con, DesignationRelationship desrel, Designation des, ConceptRelationship r, CodedConcept root, CodeSystem cs  "
            + " WHERE UPPER(des.name) = upper(:designationName) "
            + "   and cs.name = :authoringName  "
            + "   and root.name = :rootName  "
            + "   and r.active = 1  "
            + "   and des.codeSystem.id = cs.id "
            + "   and con.codeSystem.id = cs.id "
            + "   and root.codeSystem.id = cs.id "
            + "   and root.codeSystem.id = cs.id "
            + "   AND con.entityId = desrel.sourceEntityId  "
            + "   and desrel.targetEntityId = des.entityId  "
            + "   and con.entityId = r.sourceEntityId "
            + "   and r.targetEntityId = root.entityId "
            + "   and root.entityId = r.targetEntityId "
            + "   AND des.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.entityId = des.entityId )  "
            + "   AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId )  "
            + "   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = desrel.entityId) "
            + "   and r.id = (select max(rmax.id) from ConceptRelationship rmax where rmax.entityId = r.entityId ) "
            + "   and root.id in (select max(rootmax.id) from CodedConcept rootmax where rootmax.entityId = root.entityId ) "
            + "   and r.relationshipType.id = (select id from RelationshipType where name = :typeName)";
        
        Query query = session.createQuery(hqlQuery);
        query.setParameter("designationName", designationName);
        query.setParameter("rootName", rootName);
        query.setParameter("authoringName", HibernateSessionFactory.VHAT_NAME);
        query.setParameter("typeName", RelationshipType.HAS_ROOT);
        List<Object[]> objects = (List<Object[]>) query.list();
        
        List<CodedConceptDesignationDTO> codedConceptDesignations = new ArrayList<CodedConceptDesignationDTO>();
        for(Object[] myObject : objects)
        {
            codedConceptDesignations.add(new CodedConceptDesignationDTO((CodedConcept)myObject[0], (Designation)myObject[1]));
        }
        
        return codedConceptDesignations;
    }

    public static CodedConcept get(Version version, String code)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from CodedConcept cc where cc.id = (select max(con.id) from CodedConcept con " +
                        "where con.code = :code " +
                        "and con.version <= :version " +
                        "and con.codeSystem = :codeSystem) "; 
        Object conceptObject = (Object) session.createQuery(query)
                                    .setString("code", code)
                                    .setParameter("version", version)
                                    .setParameter("codeSystem", version.getCodeSystem()).uniqueResult();
        CodedConcept concept = (CodedConcept) conceptObject;
        return concept;
    }

	public static boolean isCodeSystemMember(Long codeSystemId, String conceptCode)
	{
		Session session = HibernateSessionFactory.currentSession();

		String query = "from CodedConcept cc where cc.code = :conceptCode and cc.codeSystem.id = :codeSystemId " +
				"and cc.id = (select max(con.id) from CodedConcept con where cc.codeSystem.id = :codeSystemId and con.entityId = cc.entityId)";
        List<CodedConcept> list = session.createQuery(query).setLong("codeSystemId", codeSystemId).setString("conceptCode", conceptCode).list();

		return list.size()>0;
	}
	public static CodedConcept getDomainByVuid(Long domainVuid) throws STSException
	{
		CodedConceptListDTO vhatDomains = getVhatDomains(null, domainVuid, null, null);
		
		return (vhatDomains.getCodedConcepts().size() == 0 ? null : vhatDomains.getCodedConcepts().get(0));
	}
	
	@SuppressWarnings("unchecked")
	public static CodedConceptListDTO getVhatDomains(Long parentVuid, Long domainVuid, Integer pageNumber, Integer pageSize) throws STSException
	{
		CodedConceptListDTO codedConceptList = new CodedConceptListDTO();
		
		if(pageNumber != null && pageNumber == 1)
		{
			Query query = getByQuery(true, parentVuid, domainVuid, pageNumber, pageSize);
			codedConceptList.setTotalNumberOfRecords((Long)query.uniqueResult());
		}
		Query query = getByQuery(false, parentVuid, domainVuid, pageNumber, pageSize);
		codedConceptList.setCodedConcepts(query.list());
		
		return codedConceptList;
	}	
	
	public static Query getByQuery(boolean retrieveCount, Long parentVuid, Long domainVuid, Integer pageNumber, Integer pageSize)
	{
		// first join the domain to the usage context(HealtheVet or VistA) then to VHAT
		String parentVuidFilter = (parentVuid != null) ? " AND parent.vuid = :parentVuid" : "" ;
		String domainVuidFilter = domainVuid != null ? " AND domain.vuid = :domainVuid ":"";
		String hqlQuery = (retrieveCount ? "select count(domain) " : " SELECT domain ") +		
			" FROM CodedConcept parent, CodedConcept domain, ConceptRelationship r " + 
			" WHERE r.active = 1" +
				domainVuidFilter +
				" AND r.sourceEntityId = domain.entityId" +
				" AND r.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
				" AND parent.entityId = r.targetEntityId" +
				parentVuidFilter +
				" AND domain.id = (SELECT MAX(domainmax.id) from CodedConcept domainmax where domainmax.version.id < :authoringVersion and domainmax.entityId = domain.entityId )" +
				" AND parent.id = (SELECT MAX(parentmax.id) from CodedConcept parentmax where parentmax.version.id < :authoringVersion and parentmax.entityId = parent.entityId )" +
				" AND r.id = (SELECT MAX(rmax.id) from ConceptRelationship rmax where rmax.entityId = r.entityId )" +
				" AND parent.entityId IN " + /*now the children of VHAT*/
				" (SELECT rel.sourceEntityId" +
				" FROM CodedConcept root, ConceptRelationship rel, Designation des, DesignationRelationship dr" +
				" WHERE des.name = :parentName" +
					" AND dr.targetEntityId = des.entityId" +
					" AND dr.sourceEntityId = root.entityId" +
					" AND rel.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
			        " AND root.id in (SELECT MAX(rootmax.id) from CodedConcept rootmax where rootmax.version.id < :authoringVersion and rootmax.entityId = root.entityId)" +
			        " AND rel.id in (SELECT MAX(relmax.id) from ConceptRelationship relmax where relmax.version.id < :authoringVersion and relmax.entityId = rel.entityId)" +
			        " AND dr.id in (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.version.id < :authoringVersion and drmax.entityId = dr.entityId)" +
			        " AND des.id in (SELECT MAX(desmax.id) from Designation desmax where desmax.version.id < :authoringVersion and desmax.entityId = des.entityId)" +
					" AND root.entityId = rel.targetEntityId)";
			
		Session session = HibernateSessionFactory.currentSession();
		Query query = session.createQuery(hqlQuery);
		query.setParameter("parentName", HibernateSessionFactory.VHAT_NAME);
		query.setParameter("typeName", RelationshipType.HAS_PARENT);
		query.setParameter("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID);
		if (parentVuid != null)
		{
		    query.setParameter("parentVuid", parentVuid);
		}	
		if (domainVuid != null)
		{
			query.setParameter("domainVuid", domainVuid);
		}
		if( retrieveCount == false)
		{
			if ( pageSize != null && pageNumber != null)
			{
				query.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize);
			}
		}
		
		return query;
	}
	/**
	 * get the count of concepts in the domain
	 * @param domainVuid
	 * @return
	 */
	public static long getDomainConceptCount(Long domainVuid)
	{
		String hqlQuery = "SELECT count(con.id)" +
		" FROM  CodedConcept con, DesignationRelationship dr, SubsetRelationship sr " + 
		" WHERE   sr.targetEntityId = dr.targetEntityId  " +
			" AND dr.sourceEntityId = con.entityId" +
			" AND dr.active = 1 " +
			" AND sr.id in (SELECT MAX(srmax.id) from SubsetRelationship srmax where srmax.entityId = sr.entityId ) " +
			" AND dr.id in (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = dr.entityId ) " +
			" AND con.id in (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId ) " +
			" AND sr.sourceEntityId in " +
		"(SELECT  subset.entityId " +
		" FROM  CodedConcept domain, ConceptRelationship r, CodedConcept cSubset, Subset subset " + 
		" WHERE   domain.vuid = :domainVuid " +
			" AND r.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
			" AND r.active = 1 " +
			" AND domain.entityId = r.targetEntityId " +
			" AND cSubset.entityId = r.sourceEntityId " +
			" AND cSubset.name = subset.name " + /*join on the name, until modeling decision comes*/
			" AND domain.id = (SELECT MAX(domainmax.id) from CodedConcept domainmax where domainmax.entityId = domain.entityId ) " +
			" AND r.id in (SELECT MAX(rmax.id) from ConceptRelationship rmax where rmax.entityId = r.entityId ) " +
			" AND cSubset.id = (SELECT MAX(cSubsetmax.id) from CodedConcept cSubsetmax where cSubsetmax.entityId = cSubset.entityId ) " +
			" AND subset.id = (SELECT MAX(subsetmax.id) from Subset subsetmax where subsetmax.entityId = subset.entityId )) ";
		
		Session session = HibernateSessionFactory.currentSession();
		Object count = session.createQuery(hqlQuery)
			.setParameter("domainVuid", domainVuid)
			.setParameter("typeName", RelationshipType.HAS_PARENT).uniqueResult();
        
        return ((Long)count).longValue();
	}

	/**
	 * @param domainVuid
	 * @param pageNumber 
	 * @param pageSize 
	 * @return
	 */
	public static TotalEntityListView getDomainConcepts(Long domainVuid, Integer pageNumber, Integer pageSize)
	{
		TotalEntityListView listView = new TotalEntityListView(); 
		 if (pageNumber == 1)
    	{
        	// only get a total if paging and this is the first page
    		Query query = getDomainConceptsQuery(true, domainVuid, pageNumber, pageSize);
    		listView.setTotalNumberOfRecords((Long) query.uniqueResult());
    	}
		
		Query query = getDomainConceptsQuery(false, domainVuid, pageNumber, pageSize);
 				
		List<Object[]> objects = query.list();
		
		List<CodedConceptDesignationDTO> codedConceptDesignations = new ArrayList<CodedConceptDesignationDTO>();
		for(Object[] myObject : objects)
		{
			codedConceptDesignations.add(new CodedConceptDesignationDTO((CodedConcept)myObject[0], (Designation)myObject[1]));
		}
	
		listView.setEntitiesView(codedConceptDesignations);
		
        return listView;
	}
	
	private static Query getDomainConceptsQuery(boolean retrieveCount, Long domainVuid, Integer pageNumber, Integer pageSize)
	{
		String hqlQuery = (retrieveCount?"select count(*) ":"SELECT child, des") +
		" FROM CodedConcept parent, ConceptRelationship pr, CodedConcept child, " +
			" DesignationRelationship dr, Designation des " + 
		" WHERE   parent.vuid = :domainVuid " +
			" AND parent.entityId = pr.targetEntityId " +
			" AND pr.relationshipType.name = :typeName " +
			" AND child.entityId = pr.sourceEntityId " +
			" AND child.entityId = dr.sourceEntityId AND dr.active = 1 " +
			" AND des.entityId = dr.targetEntityId " +
			" AND pr.id in (SELECT MAX(prmax.id) FROM ConceptRelationship prmax WHERE prmax.version.id < :authoringVersionId AND prmax.entityId = pr.entityId ) " +
			" AND dr.id in (SELECT MAX(drmax.id) FROM DesignationRelationship drmax WHERE drmax.version.id < :authoringVersionId AND drmax.entityId = dr.entityId ) " +
			" AND child.id in (SELECT MAX(childmax.id) FROM CodedConcept childmax WHERE childmax.version.id < :authoringVersionId AND childmax.entityId = child.entityId ) " +
			" AND des.id in (SELECT MAX(desmax.id) FROM Designation desmax WHERE desmax.version.id < :authoringVersionId AND desmax.entityId = des.entityId ) " +
			" AND parent.id in (SELECT MAX(parentmax.id) FROM CodedConcept parentmax WHERE parentmax.version.id < :authoringVersionId AND parentmax.entityId = parent.entityId ) " +
			" AND des.type.id = (select id FROM Type where name = :desType) ";
		
		Session session = HibernateSessionFactory.currentSession();
		Query query = session.createQuery(hqlQuery);
		query.setParameter("domainVuid", domainVuid)
			.setParameter("typeName", RelationshipType.HAS_PARENT)
			.setParameter("desType", DesignationType.PREFERRED_NAME)
			.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);;

		if(retrieveCount == false)
        {
	        query.setFirstResult((pageNumber - 1) * pageSize);
	        query.setMaxResults(pageSize);
        }
		
		return query;
	}
	
	/**
	 * @param domainVuid
	 * @param pageNumber 
	 * @param pageSize 
	 * @return
	 */
	public static TotalEntityListView getVhatDomains(String domainName, Integer pageNumber, Integer pageSize)
	{
		TotalEntityListView listView = new TotalEntityListView(); 
		 if (pageNumber == 1)
    	{
        	// only get a total if paging and this is the first page
    		Query query = getVhatDomainsQuery(true, domainName, pageNumber, pageSize);
    		Object uniqueResult = query.uniqueResult();
			listView.setTotalNumberOfRecords((Long) uniqueResult);
    	}
		
		Query query = getVhatDomainsQuery(false, domainName, pageNumber, pageSize);
 				
		List<Object[]> objects = query.list();
		
		List<CodedConceptDesignationDTO> codedConceptDesignations = new ArrayList<CodedConceptDesignationDTO>();
		for(Object[] myObject : objects)
		{
			codedConceptDesignations.add(new CodedConceptDesignationDTO((CodedConcept)myObject[0], (Designation)myObject[1]));
		}
	
		listView.setEntitiesView(codedConceptDesignations);
		
        return listView;
	}
	
	private static Query getVhatDomainsQuery(boolean retrieveCount, String domainName, Integer pageNumber, Integer pageSize)
	{
		// first join the domain to the usage context(HealtheVet or VistA) then to VHAT
		
		String domainNameFilter = domainName != null ? " AND upper(domainDes.name) like upper(:domainName) ":"";
		String hqlQuery = (retrieveCount ? "select count(domain) " : " SELECT domain, domainDes ") +		
			" FROM CodeSystem cs, CodedConcept parent, CodedConcept domain, ConceptRelationship r, DesignationRelationship domainDr, Designation domainDes " + 
			" WHERE r.active = 1" +
				domainNameFilter +
				" AND domainDes.type.id = cs.preferredDesignationType.id" +
				" AND cs.name = :codeSystemName" +
				" AND cs.id = domainDes.codeSystem.id"+
				" AND domainDr.targetEntityId = domainDes.entityId" +
				" AND domainDr.sourceEntityId = domain.entityId" +
				" AND domainDr.active = 1" +
				" AND r.sourceEntityId = domain.entityId" +
				" AND r.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
				" AND parent.entityId = r.targetEntityId" +
					" AND domainDr.id in (SELECT MAX(domainDrmax.id) FROM DesignationRelationship domainDrmax WHERE domainDrmax.version.id < :authoringVersionId AND domainDrmax.entityId = domainDr.entityId ) " +
					" AND domainDes.id in (SELECT MAX(domainDesmax.id) FROM Designation domainDesmax WHERE domainDesmax.version.id < :authoringVersionId AND domainDesmax.entityId = domainDes.entityId ) " +
					" AND domain.id = (SELECT MAX(domainmax.id) from CodedConcept domainmax where domainmax.version.id < :authoringVersionId and domainmax.entityId = domain.entityId )" +
					" AND parent.id = (SELECT MAX(parentmax.id) from CodedConcept parentmax where parentmax.version.id < :authoringVersionId and parentmax.entityId = parent.entityId )" +
					" AND r.id = (SELECT MAX(rmax.id) from ConceptRelationship rmax where rmax.entityId = r.entityId )" +
				" AND parent.entityId IN " + /*now the children of VHAT*/
				" (SELECT rel.sourceEntityId" +
				" FROM CodedConcept root, ConceptRelationship rel, Designation des, DesignationRelationship dr" +
				" WHERE des.name = :parentName" +
					" AND dr.targetEntityId = des.entityId" +
					" AND dr.sourceEntityId = root.entityId" +
					" AND rel.relationshipType.id = (select id from RelationshipType where name = :typeName)" +
			        " AND root.id in (SELECT MAX(rootmax.id) from CodedConcept rootmax where rootmax.version.id < :authoringVersionId and rootmax.entityId = root.entityId)" +
			        " AND rel.id in (SELECT MAX(relmax.id) from ConceptRelationship relmax where relmax.version.id < :authoringVersionId and relmax.entityId = rel.entityId)" +
			        " AND dr.id in (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.version.id < :authoringVersionId and drmax.entityId = dr.entityId)" +
			        " AND des.id in (SELECT MAX(desmax.id) from Designation desmax where desmax.version.id < :authoringVersionId and desmax.entityId = des.entityId)" +
					" AND root.entityId = rel.targetEntityId)";
			
		Session session = HibernateSessionFactory.currentSession();
		Query query = session.createQuery(hqlQuery);
		query.setString("codeSystemName", HibernateSessionFactory.VHAT_NAME)
			.setString("parentName", HibernateSessionFactory.VHAT_NAME)
			.setString("typeName", RelationshipType.HAS_PARENT)
			.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		if (domainName != null)
		{
			query.setParameter("domainName", "%"+domainName+"%");
		}
		if( retrieveCount == false)
		{
			query.setFirstResult((pageNumber - 1) * pageSize).setMaxResults(pageSize).list();
		}
		
		return query;
	}

	public static CodedConcept getCodedConceptFromDesignationName(long codeSystemId, String designationName)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "select con from CodedConcept con, DesignationRelationship desrel, Designation des " +
        	" WHERE con.codeSystem.id = :codeSystemId AND des.name = :designationName " +
        	"   AND con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
        	"   AND des.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.version.id < :authoringVersionId AND desmax.entityId = des.entityId) " +
        	"   AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.version.id < :authoringVersionId AND conmax.entityId = con.entityId) " +
        	"   AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.version.id < :authoringVersionId AND drmax.entityId = desrel.entityId) ";
		
		Query query = session.createQuery(hql);
		query.setLong("codeSystemId", codeSystemId);
		query.setString("designationName", designationName);
		query.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		CodedConcept codedConcept = (CodedConcept) query.uniqueResult();
		
		return codedConcept;
	}
	
	public static List<ConceptHierarchyDTO> getConceptHierarchyList(long codeSystemId, long conceptEntityId)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql1 = "select con1.entityId, des1.name from CodedConcept con1, DesignationRelationship desrel1, Designation des1, " +
			"   CodedConcept con2, ConceptRelationship conrel " +
        	" WHERE con1.codeSystem.id = :codeSystemId AND con2.entityId = :conceptEntityId " +
        	"   AND conrel.relationshipType.id = (select rt.id from RelationshipType rt where rt.name = 'has_parent') " +
        	"   AND con1.entityId = desrel1.sourceEntityId and desrel1.targetEntityId = des1.entityId " +
        	"   AND con1.entityId = conrel.sourceEntityId and conrel.targetEntityId = con2.entityId " +
        	"   AND des1.id = (SELECT MAX(desmax.id) from Designation desmax where desmax.entityId = des1.entityId) " +
        	"   AND des1.type.id = (select id from Type where name = 'Preferred Name') " +
        	"   AND con1.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con1.entityId) " +
        	"   AND desrel1.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax where drmax.entityId = desrel1.entityId) " +
        	"   AND con2.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con2.entityId) " +
        	"   AND conrel.id = (SELECT MAX(relmax.id) from ConceptRelationship relmax where relmax.entityId = conrel.entityId)" +
        	"ORDER BY des1.name ";
		
		Query query = session.createQuery(hql1);
		query.setLong("codeSystemId", codeSystemId);
		query.setLong("conceptEntityId", conceptEntityId);
        List<Object[]> objects = (List<Object[]>) query.list();
        
        List<Long> parentConceptEntityIds = new ArrayList<Long>();
        List<ConceptHierarchyDTO> conceptHierarchyList = new ArrayList<ConceptHierarchyDTO>();
        for(Object[] myObject : objects)
        {
        	conceptHierarchyList.add(new ConceptHierarchyDTO((Long)myObject[0], (String)myObject[1], false));
        	parentConceptEntityIds.add((Long)myObject[0]);
        }
        
		String hql2 = "select con2.entityId from CodedConcept con1, CodedConcept con2, ConceptRelationship conrel " +
    	" WHERE con1.codeSystem.id = :codeSystemId " +
    	"   AND conrel.relationshipType.id = (select rt.id from RelationshipType rt where rt.name = 'has_parent') " +
    	"   AND con1.entityId = conrel.sourceEntityId and conrel.targetEntityId = con2.entityId " +
     	"   AND con1.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con1.entityId) " +
    	"   AND con2.id = (SELECT MAX(conmax.id) from CodedConcept conmax where conmax.entityId = con2.entityId) " +
     	"   AND conrel.id = (SELECT MAX(relmax.id) from ConceptRelationship relmax where relmax.entityId = conrel.entityId) " +
     	"   AND con2.entityId in (:parentConceptEntityIds)";
        
		query = session.createQuery(hql2);
		query.setLong("codeSystemId", codeSystemId);
    	List<Long> entityIds = (List<Long>) executeQuery(query, "parentConceptEntityIds", parentConceptEntityIds);
        
        for(Long myEntityId : entityIds)
        {
        	long entityId = myEntityId;
            for (ConceptHierarchyDTO conceptHierarchyDTO : conceptHierarchyList)
            {
    			if (entityId == conceptHierarchyDTO.getConceptEntityId())
    			{
    				conceptHierarchyDTO.setHasChildren(true);
    				break;
    			}
    		}
        }
		
		return conceptHierarchyList;
		
	}
}
