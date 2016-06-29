/**
 * 
 */
package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.ConceptDesignationDTO;
import gov.vha.vets.term.services.dto.api.SubsetContentsListView;
import gov.vha.vets.term.services.dto.api.SubsetContentsView;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * @author VHAISLMURDOH
 * 
 */
public class DesignationDao extends EntityBaseDao
{
    /**
     * @param includeAuthoring 
     * @TODO add codesystem parameter and to the query
     * @param codeSystem
     * @param concept
     * @param version
     * @return
     * @throws STSNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static List<Designation> getAllVersions(long conceptEntityId, boolean includeAuthoring) 
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        String designationQuery = "from Designation des where des.entityId in (select d.targetEntityId from DesignationRelationship as d where d.sourceEntityId = :conceptEntityId) and des.version.id "+operator+" :authoringVersion order by des.entityId, des.id";
        List designations = session.createQuery(designationQuery).setLong("conceptEntityId", conceptEntityId).
                setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID).list();        

        return designations;
    }
    
    public static List<Designation> getAll(long conceptEntityId) 
    {
        Session session = HibernateSessionFactory.currentSession();

        String designationQuery = "from Designation des where des.entityId in (select d.targetEntityId from DesignationRelationship as d where d.sourceEntityId = :conceptEntityId) order by des.entityId, des.id";
        List designations = session.createQuery(designationQuery).setLong("conceptEntityId", conceptEntityId).list();        

        return designations;
    }

    public static List<Designation> getDesignations(long conceptEntityId) 
    {
        return getDesignations(conceptEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
    
    public static List<Designation> getDesignations(long conceptEntityId, long versionId) 
    {
    	return getDesignations(conceptEntityId, versionId, VersionSearchType.CURRENT);
    }

    /**
     * Get a list of designations for the concepts specified 
     * @param conceptEntityId
     * @param versionId
     * @return
     * @throws STSNotFoundException
     */
    public static List<Designation> getPreviousVersionDesignations(long conceptEntityId, long versionId) 
    {
    	return getDesignations(conceptEntityId, versionId, VersionSearchType.PREVIOUS);
    }
    
    /**
     * @TODO add codesystem parameter and to the query
     * @param codeSystem
     * @param concept
     * @param version
     * @return
     * @throws STSNotFoundException
     */
    @SuppressWarnings("unchecked")
    private static List<Designation> getDesignations(long conceptEntityId, long versionId, VersionSearchType versionSearchType) 
    {
        Session session = HibernateSessionFactory.currentSession();

        String versionSearchTypeString = (versionSearchType == VersionSearchType.CURRENT) ? "<=" : "<";
        
        String hqlQuery =
        	"select des from Concept con, Designation des, DesignationRelationship dr " +
        	"  where con.entityId = :conceptEntityId " +
        	"    and dr.sourceEntityId = con.entityId " +
        	"    and dr.targetEntityId = des.entityId " +
        	"    and con.id = (select max(conmax.id) from Concept conmax " +
        	"             where conmax.version.id " + versionSearchTypeString + " :versionId and conmax.entityId = con.entityId) " +
        	"    and des.id = (select max(desmax.id) from Designation desmax " +
        	"             where desmax.version.id " + versionSearchTypeString + " :versionId and desmax.entityId = des.entityId) " +
        	"    and dr.id = (select max(drmax.id) from DesignationRelationship drmax " +
        	"             where drmax.version.id " + versionSearchTypeString + " :versionId and drmax.entityId = dr.entityId) ";

        Query query = session.createQuery(hqlQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong("versionId", versionId);
        List list = query.list();

        return list;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Collection<Designation>> getDesignations(Collection<Long> conceptEntityIds, long versionId) 
    {
        Map<Long, Collection<Designation>> map = new HashMap<Long, Collection<Designation>>();
        Session session = HibernateSessionFactory.currentSession();

        String hqlQuery =
            "select con.entityId, des from Concept con, Designation des, DesignationRelationship dr " +
            "  where con.entityId in (:conceptEntityIds) " +
            "    and dr.sourceEntityId = con.entityId " +
            "    and dr.targetEntityId = des.entityId " +
            "    and con.id = (select max(conmax.id) from Concept conmax " +
            "             where conmax.version.id <= :versionId and conmax.entityId = con.entityId) " +
            "    and des.id = (select max(desmax.id) from Designation desmax " +
            "             where desmax.version.id <= :versionId and desmax.entityId = des.entityId) " +
            "    and dr.id = (select max(drmax.id) from DesignationRelationship drmax " +
            "             where drmax.version.id <= :versionId and drmax.entityId = dr.entityId) ";

        Query query = session.createQuery(hqlQuery);
        query.setLong("versionId", versionId);
        List<Object[]> designations = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        
        for (Object[] data : designations)
        {
            Long conceptEntityId = ((Long) data[0]);
            Designation designation = (Designation) data[1];
            Collection<Designation> list = map.get(conceptEntityId);
            if (list == null)
            {
                list = new ArrayList<Designation>();
                map.put(conceptEntityId, list);
            }
            list.add(designation);
        }

        return map;
    }
    
    /**
     * @TODO add codesystem parameter and to the query
     * @param codeSystem
     * @param concept
     * @param version
     * @return
     * @throws STSNotFoundException 
     * @throws Exception
     */
    public static Designation get(CodeSystem codeSystem, long conceptEntityId, String designationName) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Designation d2 where d2.id in (select max(d1.id) from Designation d1 where d1.entityId in  "
                + "(select d.entityId From Designation d, DesignationRelationship dr where d.entityId = dr.targetEntityId and "
                + "d.name = :designationName and " 
                + "dr.id in (select d.id from DesignationRelationship d where d.sourceEntityId = :entityId)) group by d1.entityId)";

        Designation designation = (Designation) session.createQuery(query).setLong("entityId", conceptEntityId).setString("designationName", designationName).uniqueResult();
        
        return designation;
    }

    /**
     * Get a designation by it's unique code
     * @param code
     * @return
     */
    public static Designation get(CodeSystem codeSystem, String code)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Designation d2 where d2.id = (select max(d1.id) from Designation d1 where d1.codeSystem = :codeSystem and d1.code = :code group by d1.entityId)";

        Query myQuery = session.createQuery(query).setString("code", code).setParameter("codeSystem", codeSystem);
        Designation designation = (Designation) myQuery.uniqueResult();
        
        return designation;
    }

    @SuppressWarnings("unchecked")
    public static List<Designation> get(CodeSystem codeSystem, Collection<String> designationCodes)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = 
            "from Designation d2 where d2.id in (select max(d1.id) from Designation d1 where " +
                "d1.codeSystem = :codeSystem and d1.code in (:codes) group by d1.entityId)";

        return session.createQuery(query).setParameterList("codes", designationCodes).setParameter("codeSystem", codeSystem).list();
    }

    /**
     * get the most current entry
     * @param designationEntityId
     * @return
     */
    public static Designation get(long designationEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "from Designation d2 where d2.id = (select max(d1.id) from Designation d1 where d1.version.id <= :version and d1.entityId = :entityId)";
        Query myQuery = session.createQuery(query).setLong("entityId", designationEntityId).setLong("version", versionId);
        
        return (Designation) myQuery.uniqueResult();
    }
    /**
     * Get the versioned entity
     * @param designation
     * @return
     */
    public static Designation getVersioned(Designation designation)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get concept for the given code
        String query = "from Designation d2 where d2.id = (select max(d1.id) from Designation d1 where d1.entityId = :entityId and d1.version.id < :"+AUTHORING_VERSION+")";

        Designation existingDesignation = (Designation) session.createQuery(query).setLong("entityId", designation.getEntityId()).setLong(AUTHORING_VERSION,
                HibernateSessionFactory.AUTHORING_VERSION_ID).uniqueResult();

        return existingDesignation;
    }

    /**
     * 
     * @param name
     * @return
     * @throws STSNotFoundException
     */
    public static DesignationType getType(String name) 
    {
        Session session = HibernateSessionFactory.currentSession();
        DesignationType type = (DesignationType) session.createCriteria(DesignationType.class).add(Restrictions.eq("name", name)).uniqueResult();
        return type;
    }
    
    /**
     * @param id
     * @return DesignationType
     */
    public static DesignationType getType(long id) 
    {
        Session session = HibernateSessionFactory.currentSession();
        DesignationType type = (DesignationType) session.createCriteria(DesignationType.class).add(Restrictions.eq("id", id)).uniqueResult();
        return type;
    }

    /**
     * 
     * @param name
     * @return
     * @throws STSNotFoundException
     */
    public static List<String> getTypesByCodeSystem(String codeSystemName) 
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "select distinct d.type.name from Designation d where d.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName) order by d.type.name";
        Query query = session.createQuery(hql);
        query.setParameter("codeSystemName", codeSystemName);
        return query.list();
    }
    /**
     * 
     * @param designation
     */
    public static void save(Designation designation)
    {
        HibernateSessionFactory.currentSession().save(designation);
    }

    public static void saveType(DesignationType type)
    {
        HibernateSessionFactory.currentSession().save(type);
        HibernateSessionFactory.currentSession().flush();
    }

    public static void delete(Long entityId)
    {
        HibernateSessionFactory.currentSession().createQuery("delete from Designation where entityId = :entityId and version.id = :versionId").
            setParameter("entityId", entityId).setParameter("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID).executeUpdate();    
	}

    /**
     * Set a concept to a finalized version
     * 
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        String query = "update Designation d set d.version = :"+NEW_VERSION+" where d.version.id = :"+AUTHORING_VERSION+" and"
                + " d.entityId in (select dr.targetEntityId from DesignationRelationship dr where dr.sourceEntityId in (:entityId))";
        
        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }


    @SuppressWarnings("unchecked")
	public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "Select distinct c.entityId from CodedConcept c " +
        	"where c.entityId in (select dr.sourceEntityId from DesignationRelationship dr, Designation d " +
        	"  where dr.targetEntityId = d.entityId and d.version.id = :versionId)";
        List<Long> conceptEntityIds = session.createQuery(query).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }
    
    @SuppressWarnings("unchecked")
	public static List<Designation> getDesignationsByVersion(long versionId)
    {
    	List<Designation> designations = new ArrayList<Designation>();
    	
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String query = "from Designation d where d.version.id = :versionId";
    	designations = (List<Designation>)session.createQuery(query).setLong("versionId", versionId).list();
    	
    	return designations;
    }

	@SuppressWarnings("unchecked")
	public static List<ConceptDesignationDTO> get(Set<Long> conceptEntityIds, List<String> designationNames) 
	{
        Session session = HibernateSessionFactory.currentSession();

        // this only returns active entries
        String hql = "select d2,(select dr2.sourceEntityId from DesignationRelationship dr2 where d2.entityId = dr2.targetEntityId) "
                + "from Designation d2 where id in (select max(id) from Designation d1 where  "
                + "d1.entityId in (select d.entityId From Designation d, DesignationRelationship dr  "
                + "where d.name in (:designationNames) and  d.entityId = dr.targetEntityId and dr.id in " 
                + "(select d.id from DesignationRelationship d where d.sourceEntityId in (:conceptEntityIds))) group by d1.entityId)";

        Query query = session.createQuery(hql);

        query.setParameterList("designationNames", designationNames);
        query.setParameterList("conceptEntityIds", conceptEntityIds);
        
        List<ConceptDesignationDTO> results = new ArrayList<ConceptDesignationDTO>();
        List<Object[]> list = query.list();
        for (Object[] items : list)
        {
            Designation designation = (Designation) items[0];
            Long conceptEntityId = (Long) items[1];
            ConceptDesignationDTO data = new ConceptDesignationDTO(conceptEntityId, designation);
            results.add(data);
        }
        
        return results;
    }

	@SuppressWarnings("unchecked")
	public static List<ConceptDesignationDTO> getByVersionId(long versionId, boolean isFullVersion) 
	{
        Session session = HibernateSessionFactory.currentSession();

        // this only returns active entries
        String hql = (isFullVersion) ?
        		"select dr.sourceEntityId, des from Designation des, DesignationRelationship dr " 
        		+ "where dr.targetEntityId = des.entityId " 
        		+ "and des.codeSystem.id = (select v.codeSystem.id from Version v where v.id = :versionId) "
        		+ "and des.id = (select max(desmax.id) from Designation desmax where desmax.entityId = des.entityId and desmax.version.id <= :versionId) "
        		+ "and dr.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.entityId = dr.entityId and drmax.version.id <= :versionId) "        		
        		: 
        		"select dr.sourceEntityId, des from Designation des, DesignationRelationship dr "
                + "where dr.targetEntityId = des.entityId and des.version.id = :versionId";

        Query query = session.createQuery(hql);

        query.setLong("versionId", versionId);
        
        List<ConceptDesignationDTO> results = new ArrayList<ConceptDesignationDTO>();
        List<Object[]> list = query.list();
        for (Object[] items : list)
        {
            Long conceptEntityId = (Long) items[0];
            Designation designation = (Designation) items[1];
            ConceptDesignationDTO data = new ConceptDesignationDTO(conceptEntityId, designation);
            results.add(data);
        }
        
        return results;
    }

	@SuppressWarnings("unchecked")
	public static Designation get(long conceptEntityId, String designationType) 
	{
		Designation designation = null;
		Session session = HibernateSessionFactory.currentSession();

        String hql =  "select d from Designation d, DesignationRelationship dr " +
        				"where d.entityId = dr.targetEntityId and dr.sourceEntityId = :conceptEntityId " +
        					"and d.type.name = :designationType and d.active = 1 " +
        					"and d.id = (select max(dmax.id) from Designation dmax where dmax.entityId = d.entityId)" +
        					"and dr.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.entityId = dr.entityId)";
        Query query = session.createQuery(hql);

        query.setString("designationType", designationType);
        query.setLong("conceptEntityId", conceptEntityId);
        
        designation = (Designation)query.uniqueResult();
        
        return designation;
	}

	public static Designation get(String subsetName, String designationName)
	{
		Session session = HibernateSessionFactory.currentSession();
		Designation designation = null;

        // this only returns active entries
        String hql = "from Designation d, Subset s, SubsetRelationship sr " +
        		"where d.id in (select max(d2.id) from Designation d2 where d2.name = :designationName group by d2.entityId) " +
        		"and d.entityId = sr.targetEntityId and sr.sourceEntityId = s.entityId and s.name = :subsetName " +
        		"and sr.id in (select max(sr2.id) from SubsetRelationship sr2 where sr2.sourceEntityId = s.entityId and sr2.targetEntityId = d.entityId)";
        Object objects[] = (Object [])session.createQuery(hql).setString("designationName", designationName).setString("subsetName", subsetName).uniqueResult();

        if (objects != null)
        {
        	designation = (Designation) objects[0];
        }
        
		return designation;
	}

    @SuppressWarnings("unchecked")
    public static List<Designation> getBySubset(long subsetEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
        List<Designation> designationList = new ArrayList<Designation>();

        // this only returns active entries
        String hql = "from Subset s, SubsetRelationship sr, Designation d " +
                "where s.entityId = sr.sourceEntityId and sr.targetEntityId = d.entityId " +
                "and s.id = (select max(smax.id) from Subset smax where smax.version.id <= :versionId and smax.entityId = :subsetEntityId) " +
                "and d.id = (select max(dmax.id) from Designation dmax where dmax.version.id <= :versionId and dmax.entityId = d.entityId) " +
                "and sr.id = (select max(srmax.id) from SubsetRelationship srmax where srmax.version.id <= :versionId and srmax.entityId = sr.entityId) ";
        List<Object[]> objects = (List<Object[]>) session.createQuery(hql).setLong("subsetEntityId", subsetEntityId).setLong("versionId", versionId).list();
        
        for (Object[] object : objects)
        {
            designationList.add((Designation)object[2]);
        }

        return designationList;
    }
	
	/**
	 * 
	 * @param subsetEntityId
	 * @param versionId
	 * @param designationName (OPTIONAL)
	 * @param active (OPTIONAL)
	 * @param pageSize (OPTIONAL)
	 * @param pageNumber (OPTIONAL)
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static SubsetContentsListView getSubsetContents(long subsetEntityId, long versionId,  String designationName, String status, Integer pageSize, Integer pageNumber)
	{
        SubsetContentsListView listView = new SubsetContentsListView();
		List<SubsetContentsView> subsetContents = new ArrayList<SubsetContentsView>();
                
        if (pageNumber == 1)
    	{
        	// only get a total if paging and this is the first page
    		Query query = getBySubsetQuery(true, subsetEntityId, versionId, designationName, status, pageSize, pageNumber);
    		listView.setTotalNumberOfRecords((Long) query.uniqueResult());
    	}
    	
    	Query query = getBySubsetQuery(false, subsetEntityId, versionId, designationName, status, pageSize, pageNumber);
        List<Object[]> objects = (List<Object[]>) query.list();
        
        for (Object[] object : objects)
        {
            SubsetContentsView view = new SubsetContentsView((Designation)object[2], (SubsetRelationship)object[1]);
			subsetContents.add(view);
		}
        listView.setSubsetContentsView(subsetContents);
		
        return listView;
	}

	@SuppressWarnings("unchecked")
    private static Query getBySubsetQuery(boolean retrieveCount, long subsetEntityId, long versionId,  String designationName, String status, Integer pageSize, Integer pageNumber)
	{
		Session session = HibernateSessionFactory.currentSession();
		String designationNameFilter = " and upper(d.name) like upper(:filter)";
		String statusFilter = " and sr.active = :active";

        String hql = (retrieveCount ? "select count(*) " : "") +
        		"from Subset s, SubsetRelationship sr, Designation d " +
        		"where s.entityId = sr.sourceEntityId and sr.targetEntityId = d.entityId " +
        		"and s.id = (select max(smax.id) from Subset smax where smax.version.id <= :versionId and smax.entityId = :subsetEntityId) " +
        		"and d.id = (select max(dmax.id) from Designation dmax where dmax.version.id <= :versionId and dmax.entityId = d.entityId) " +
        		"and sr.id = (select max(srmax.id) from SubsetRelationship srmax where srmax.version.id <= :versionId and srmax.entityId = sr.entityId) ";

        if (designationName != null)
        {
            hql = hql.concat(designationNameFilter);
        }
        if (status != null)
        {
            hql = hql.concat(statusFilter);
        }
        Query query = session.createQuery(hql);
        if (designationName != null)
        {
            query.setParameter("filter", "%"+designationName+"%");
        }
        boolean active = true; //default to active
        if (status != null)
        {
            active = ("1".equals(status) || "true".equalsIgnoreCase(status)) ? true : false; 
            query.setParameter("active", active);
        }
        query.setLong("subsetEntityId", subsetEntityId);
        query.setLong("versionId", versionId);

        if(retrieveCount == false)
        {
	        query.setFirstResult((pageNumber - 1) * pageSize);
	        query.setMaxResults(pageSize);
        }
        
        return query;
	}

	public static Designation getByVuid(Long vuid)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "from Designation where vuid = :vuid and entityId in (select max(id) from Designation d2 where d2.vuid = :vuid group by d2.entityId)";
		Designation designation = (Designation)session.createQuery(hql).setLong("vuid", vuid).uniqueResult();
		return designation;
	}

	@SuppressWarnings("unchecked")
    public static List<Designation> getByVuids(Set<Long> vuids)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String hql = "from Designation des where des.vuid in (:vuids) and des.id = (select max(des2.id) from Designation des2 where des2.entityId = des.entityId )";
        Query query = session.createQuery(hql);
        
        return executeQuery(query, "vuids", vuids);
    }

	public static List<Designation> get(String name)
	{
		Session session = HibernateSessionFactory.currentSession();

        // this only returns active entries
        String hql = "from Designation d where d.name = :designationName and d.active = 1 " +
 			"and d.id = (select max(dmax.id) from Designation dmax where d.entityId = dmax.entityId)";

        return (List<Designation>)session.createQuery(hql).setString("designationName", name).list();
	}
    public static Map<String, Designation> getBrowseConceptDescriptions(CodeSystem codeSystem, long versionId, Collection<String> conceptCodes) {
        return getConceptDescriptionsFromDatabase(codeSystem, versionId, conceptCodes, true);        
    }
    public static Map<String, Designation> getConceptDescriptions(CodeSystem codeSystem, long versionId, Collection<String> conceptCodes) {
        return getConceptDescriptionsFromDatabase(codeSystem, versionId, conceptCodes, false);
    }
	
    public static Map<String, Designation> getConceptDescriptionsFromDatabase(CodeSystem codeSystem, long versionId, Collection<String> conceptCodes, boolean ignoreCache) 
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "SELECT con.code, des from CodedConcept con, DesignationRelationship desrel, Designation des" +
                "  WHERE con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
                "    AND con.codeSystem.id = :codeSystemId " +
                "    AND des.id = (SELECT MAX(desmax.id) from Designation desmax WHERE desmax.version.id <= :versionId AND desmax.entityId = des.entityId) " +
                "    AND con.id = (SELECT MAX(conmax.id) from CodedConcept conmax WHERE conmax.version.id <= :versionId AND conmax.entityId = con.entityId) " +
                "    AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax WHERE drmax.version.id <= :versionId AND drmax.entityId = desrel.entityId) " +
                "    AND con.code IN (:conceptCodes)";
        Query sessionQuery = session.createQuery(query);
        sessionQuery.setLong("codeSystemId", codeSystem.getId());
        sessionQuery.setLong("versionId", versionId);
        if (ignoreCache) { sessionQuery.setCacheMode(CacheMode.IGNORE); }
        List<Object[]> descriptions = executeQuery(sessionQuery, "conceptCodes", conceptCodes);

        Map<String, Designation> conceptDescriptionMap = new HashMap<String, Designation>();
        for (Object[] data : descriptions)
        {
            String code = (String) data[0];
            Designation designation = (Designation) data[1];
            Designation des = conceptDescriptionMap.get(code);
            if (des == null)
            {
                // we currently do not have a designation, we will take any random one we find
                conceptDescriptionMap.put(code, designation);
            }
            else if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() && designation.getActive() == true)
            {
                // we have the preferred designation type and it is active - 1st choice
                conceptDescriptionMap.put(code, designation);
            }
            else if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() &&
                    des.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have the preferred designation type and it is inactive - 2nd choice
                conceptDescriptionMap.put(code, designation);
            }
            else if (designation.getActive() == true && des.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have an active designation (not preferred type) - 3rd choice
                conceptDescriptionMap.put(code, designation);
            }
        }
        // this code allows designations to be in the mapentry source or target
/*        
        if (conceptDescriptionMap.size() != conceptCodes.size())
        {
            List<Designation> designations = DesignationDelegate.get(codeSystem, conceptCodes);
            for (Designation designation : designations)
            {
                conceptDescriptionMap.put(designation.getCode(), designation);
            }
        }
*/        
        return conceptDescriptionMap;
    }
    public static Map<Long, Designation> getConceptDescriptionsByEntityIds(CodeSystem codeSystem, long versionId, Collection<Long> conceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();
        String query = "SELECT con.entityId, des from Concept con, DesignationRelationship desrel, Designation des" +
                "  WHERE con.entityId = desrel.sourceEntityId and desrel.targetEntityId = des.entityId " +
                "    AND des.id = (SELECT MAX(desmax.id) from Designation desmax WHERE desmax.version.id <= :versionId AND desmax.entityId = des.entityId) " +
                "    AND con.id = (SELECT MAX(conmax.id) from Concept conmax WHERE conmax.version.id <= :versionId AND conmax.entityId = con.entityId) " +
                "    AND desrel.id = (SELECT MAX(drmax.id) from DesignationRelationship drmax WHERE drmax.version.id <= :versionId AND drmax.entityId = desrel.entityId) " +
                "    AND con.entityId IN (:conceptEntityIds)";
        Query sessionQuery = session.createQuery(query);
        sessionQuery.setLong("versionId", versionId);
        List<Object[]> descriptions = executeQuery(sessionQuery, "conceptEntityIds", conceptEntityIds);

        Map<Long, Designation> conceptDescriptionMap = new HashMap<Long, Designation>();
        for (Object[] data : descriptions)
        {
            Long conceptEntityId = (Long) data[0];
            Designation designation = (Designation) data[1];
            Designation des = conceptDescriptionMap.get(conceptEntityId);
            if (des == null)
            {
                // we currently do not have a designation, we will take any random one we find
                conceptDescriptionMap.put(conceptEntityId, designation);
            }
            else if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() && designation.getActive() == true)
            {
                // we have the preferred designation type and it is active - 1st choice
                conceptDescriptionMap.put(conceptEntityId, designation);
            }
            else if (designation.getType().getId() == codeSystem.getPreferredDesignationType().getId() &&
                    des.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have the preferred designation type and it is inactive - 2nd choice
                conceptDescriptionMap.put(conceptEntityId, designation);
            }
            else if (designation.getActive() == true && des.getType().getId() != codeSystem.getPreferredDesignationType().getId())
            {
                // we have an active designation (not preferred type) - 3rd choice
                conceptDescriptionMap.put(conceptEntityId, designation);
            }
        }

        return conceptDescriptionMap;
    }

    public static Designation getPreferredDesignationByVuid(long conceptVuid)
    {
    	String hql = "FROM Designation des WHERE des.entityId in (" +
    	"    SELECT d.entityId " +
    	"	 FROM Designation d, CodedConcept c, DesignationRelationship r  " +
		"    WHERE  c.vuid = :conceptVuid " +
		"        and c.entityId = r.sourceEntityId " +
		"        and r.targetEntityId = d.entityId " +
		"        and d.id = (select max(maxd.id) from Designation maxd where maxd.entityId = r.targetEntityId)  " +
		"        and d.active = 1 " +
		"  	     and d.type.id = (select cs.preferredDesignationType.id from CodeSystem cs where d.codeSystem.id = cs.id and cs.name = '" + HibernateSessionFactory.VHAT_NAME + "')) ";
    	
		Session session = HibernateSessionFactory.currentSession();
		Designation preferredDesignation = (Designation) session.createQuery(hql)
			.setLong("conceptVuid", conceptVuid).uniqueResult();
		
		return preferredDesignation;
    }
    
	public static Designation getPreferredDesignation(long conceptEntityId, CodeSystem codeSystem)
	{
		String hql = "FROM Designation des WHERE des.entityId in (SELECT d.name " +
				"FROM Designation d, CodedConcept c, DesignationRelationship r, Codesystem cs  " +
			"    WHERE  c.entityId = :conceptEntityId " +
			"    and d.codesystemId = :codeSystemId " +
			"    and d.id = (select max(maxd.id) from concept maxd where maxd.entityId = r.targetEntityId)  " +
			"    and r.sourceEntityId = c.entityId and r.targetEntityId = d.entityId " +
			"    and d.active = 1 and d.type.id = cs.preferredDesignationType.id  ";
		
		Session session = HibernateSessionFactory.currentSession();
		Designation preferredDesignation = (Designation) session.createQuery(hql)
			.setLong("conceptEntityId", conceptEntityId)
			.setLong("codeSystemId", codeSystem.getId());
		
		return preferredDesignation;
	}    
}
