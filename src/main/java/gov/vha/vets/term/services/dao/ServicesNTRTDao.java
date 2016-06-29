package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.dto.PublishedRequestDTO;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

public class ServicesNTRTDao
{
	private static String vhatCodeSystem = "VHAT";
    /**
     * Get a list of designation names for the given subset name
     * @param designationName
     * @param subsetNames
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getDesignationNamesBySubset(String subsetName)
	{
        Session session = HibernateSessionFactory.currentSession();

        String query =
	        "select distinct des.name from Subset subset, SubsetRelationship subrel, Designation des " +
	        "    where subset.active = 1 and subset.id = (select max(id) from Subset submax" +
	        "			where submax.version.id < :authoringVersion and subset.entityId = submax.entityId) " +
	        "      and subrel.active = 1 and subrel.id = (select max(id) from SubsetRelationship subrelmax" +
	        "			where subrelmax.version.id < :authoringVersion and subrel.entityId = subrelmax.entityId) " +
	        "      and des.active = 1 and des.id = (select max(id) from Designation desmax" +
	        "			where desmax.version.id < :authoringVersion and des.entityId = desmax.entityId) " +
	        "      and subset.entityId = subrel.sourceEntityId and des.entityId = subrel.targetEntityId " +
	        "      and subset.name = (:subsetName) order by des.name ";
        List<String> designations = session.createQuery(query).setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID).setString("subsetName", subsetName).list();

        return designations;
	}

	/**
     * Get a list of designation names for the given subset name
     * @param designationName
     * @param relationshipTypeName
     * @param subsetName
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getRelationshipDesignationNames(String designationName, String relationshipTypeName)
	{
        Session session = HibernateSessionFactory.currentSession();

        String query =
            "select d.name from ConceptRelationship cr, DesignationRelationship dr, Designation d where cr.active = 1 " +
            "and cr.id in "+ 
            "   (select max(cr2.id) from ConceptRelationship cr2, Concept d2 where  cr2.version.id <= :authoringVersion  "+ 
            "       and cr2.sourceEntityId = d2.entityId and d2.name = :designationName " +
            "       and cr2.relationshipType in "+ 
            "           (select rt.id from RelationshipType rt where rt.name = :relationshipTypeName) "+ 
            "               group by cr2.entityId) "+ 
            "       and dr.id in " +
            "           (select max(dr2.id) from DesignationRelationship dr2 where dr2.version.id <= :authoringVersion "+ 
            "               and dr2.sourceEntityId = cr.targetEntityId" +
            "                group by dr2.entityId) "+ 
            "       and d.id in " +
            "           (select max(d2.id) from Designation d2 where d2.version.id <= :authoringVersion "+ 
            "                 and d2.entityId = dr.targetEntityId and d2.type = "+ 
            "                   (select dt.id from DesignationType dt where dt.name = :designationType)"+ 
            "                       group by d2.entityId)";
                       
        List<String> designations = session.createQuery(query).setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID)
            .setString("designationType", "Preferred Name").setString("relationshipTypeName", relationshipTypeName)
            .setString("designationName", designationName).list();
        
        return designations;
	}
	
	/**
	 * 
	 * @param subsetName
	 * @return
	 * get all designations in the vitals category subset
	 */
	@SuppressWarnings("unchecked")
    public static List<String> getAllDesignationsInSubset(String subsetName)
    {
        
        Session session = HibernateSessionFactory.currentSession();
        
        String query =
            "select d.name from SubsetRelationship sr, Designation d, Subset s where " + 
            "sr.active = 1 " + 
            "and sr.sourceEntityId = s.entityId " + 
            "and s.name = :subsetName " + 
            "and d.entityId = sr.targetEntityId " + 
            "and d.id = " + 
            "   (select max(d2.id) from Designation d2 where d2.version.id < :authoringVersion " + 
            "   and d2.entityId = d.entityId and d2.type = " + 
            "       (select dt.id from DesignationType dt where dt.name = :designationType)) " + 
            "and s.id = " + 
            "   (select max(s2.id) from Subset s2 where s2.version.id < :authoringVersion " + 
            "   and s2.entityId = s.entityId) " + 
            "and sr.id = " + 
            "   (select max(sr2.id) from SubsetRelationship sr2 where sr2.version.id < :authoringVersion " +
            "   and sr2.entityId = sr.entityId)";
            
        List<String> designations = session.createQuery(query)
                    .setString("subsetName", subsetName)
                    .setString("designationType", "Preferred Name")
                    .setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID)
                    .list();

        return designations;
	}
	
	/**
     * Get a list of designation names for the given subset name using a inverse concept relationship
     * @param designationName
     * @param relationshipTypeName
     * @param subsetName
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getInverseRelationshipDesignationNames(String designationName, String relationshipTypeName, String subsetName)
	{
        Session session = HibernateSessionFactory.currentSession();

        String query = 
        	"SELECT (SELECT des.name  " +
        	"      FROM Designation des, DesignationRelationship dr  " +
        	"     WHERE des.active = 1 and des.id in (select max(id) from Designation  " +
        	"            where version.id < :authoringVersion group by entityId)  " +
        	"       AND dr.active = 1 and dr.id in (select max(id) from SubsetRelationship  " +
        	"            where version.id < :authoringVersion group by entityId)  " +
        	"       AND des.entityId = dr.targetEntityId  " +
        	"       AND cr.sourceEntityId = dr.sourceEntityId  " +
        	"       AND des.type.id in (SELECT dt.id  " +
        	"               FROM Type dt " +
        	"                   WHERE dt.name = :designationType)) AS designationName  " +
        	"  FROM ConceptRelationship cr, Type ct  " +
        	" WHERE cr.active = 1 and cr.id in (select max(id) from ConceptRelationship  " +
        	"            where version.id < :authoringVersion group by entityId)  " +
        	"       AND cr.relationshipType.id = ct.id and ct.name = :relationshipTypeName " +
        	"       AND cr.targetEntityId = (SELECT dr2.sourceEntityId  " +
        	"         FROM Designation des2, DesignationRelationship dr2  " +
        	"           WHERE des2.active = 1 and des2.id in (select max(id) from Designation  " +
        	"                  where version.id < :authoringVersion group by entityId)  " +
        	"             AND dr2.active = 1 and dr2.id in (select max(id) from DesignationRelationship  " +
        	"                  where version.id < :authoringVersion group by entityId)  " +
        	"             AND des2.entityId = dr2.targetEntityId  " +
        	"             AND des2.name = :designationName  " +
        	"             AND dr2.targetEntityId in (  " +
        	"                 SELECT sr.targetEntityId  " +
        	"                   FROM Subset sub, SubsetRelationship sr  " +
        	"                     WHERE sub.active = 1 and sub.id in (select max(id) from Subset  " +
        	"                          where version.id < :authoringVersion group by entityId)  " +
        	"                       AND sr.active = 1 and sr.id in (select max(id) from SubsetRelationship  " +
        	"                          where version.id < :authoringVersion group by entityId)  " +
        	"                       AND sub.entityId = sr.sourceEntityId  " +
        	"                       AND sub.name = :subsetName)) ";
                       
        List<String> designations = session.createQuery(query).setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID)
            .setString("designationType", "Preferred Name").setString("relationshipTypeName", relationshipTypeName)
            .setString("designationName", designationName).setString("subsetName", subsetName).list();
        
        return designations;
	}

	/**
     * Determine whether the designation name is in a list of subsets
     * @param designationName
     * @param subsetNames
     * @return
     */
	@SuppressWarnings("unchecked")
	public static boolean isDesignationInSubsets(String designationName, List<String> subsetNames)
	{
        Session session = HibernateSessionFactory.currentSession();
        
        String query =
	        "select subset.name, des.name from Subset subset, SubsetRelationship subrel, Designation des " +
	        "    where subset.id in (select max(id) from Subset " +
	        "			where version.id < :authoringVersion group by entityId) " +
	        "      and subrel.id in (select max(id) from SubsetRelationship " +
	        "			where version.id < :authoringVersion group by subrel.entityId) " +
	        "      and des.id in (select max(id) from Designation " +
	        "			where version.id < :authoringVersion group by entityId) " +
	        "      and subset.entityId = subrel.sourceEntityId and des.entityId = subrel.targetEntityId " +
	        "      and des.name = (:designationName) and subset.name in (:subsetNames) ";

        List<Object[]> result = session.createQuery(query).setLong("authoringVersion", HibernateSessionFactory.AUTHORING_VERSION_ID).setParameterList("subsetNames", subsetNames).setString("designationName", designationName).list();

        return (result.size() > 0 ? true : false);
	}
	
	@SuppressWarnings("unchecked")
    public static Map<String, String> getPublishedRequestsVitals(List<String> vitalQualifierNames, String vitalTypeName, String vitalSubsetName, boolean allQualifiers)
    {
		Map<String, String> vitalResults = new HashMap<String, String>();
        Session session = HibernateSessionFactory.currentSession();
        StringBuffer sql = new StringBuffer();
        sql.append("select qualifierNameDes.name, typeNameDes.name from " +
        		   "CodedConcept qualifierName, CodedConcept typeName, ConceptRelationship cr, RelationshipType rt, " +
        		   "Designation qualifierNameDes, DesignationRelationship dr, Subset s, SubsetRelationship sr, DesignationRelationship dr1, " +
        		   "Designation typeNameDes, SubsetRelationship sr1, Subset s1 " +
        		   "where cr.sourceEntityId = qualifierName.entityId " +
        		   "and cr.targetEntityId = typeName.entityId " + 
        		   "and cr.relationshipType = rt.id " +
        		   "and rt.name = 'has_qualifier' " +
        		   "and dr.sourceEntityId = qualifierName.entityId " +
        		   "and dr.targetEntityId = qualifierNameDes.entityId " + 
        		   "and sr.sourceEntityId = s.entityId " +
        		   "and sr.targetEntityId = qualifierNameDes.entityId " +
        		   "and s.name = :vitalSubsetName " + 
        		   "and dr1.sourceEntityId = typeName.entityId " +
        		   "and dr1.targetEntityId = typeNameDes.entityId " +
        		   "and typeNameDes.name = :vitalTypeName " +
        		   "and sr1.sourceEntityId = s1.entityId " +
        		   "and sr1.targetEntityId = typeNameDes.entityId  " +
        		   "and s1.name = 'Vital Types' " +
        	  	   "and qualifierNameDes.active = 1 " +
        	  	   "and qualifierName.codeSystem.name = :vhatCodeSystem and typeName.codeSystem.name = :vhatCodeSystem " +
                   "and qualifierName.id = (select max(ccmax.id) from CodedConcept ccmax where ccmax.version.id <= :versionId and ccmax.entityId = qualifierName.entityId) " +
                   "and qualifierNameDes.id = (select max(desmax.id) from Designation desmax where desmax.version.id <= :versionId and desmax.entityId = qualifierNameDes.entityId) " +
                   "and typeName.id = (select max(ccmax.id) from CodedConcept ccmax where ccmax.version.id <= :versionId and ccmax.entityId = typeName.entityId) " +
                   "and typeNameDes.id = (select max(desmax.id) from Designation desmax where desmax.version.id <= :versionId and desmax.entityId = typeNameDes.entityId) " +
                   "and cr.id = (select max(crmax.id) from ConceptRelationship crmax where crmax.version.id <= :versionId and crmax.entityId = cr.entityId) " +
                   "and dr.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.version.id <= :versionId and drmax.entityId = dr.entityId) " +
                   "and dr1.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.version.id <= :versionId and drmax.entityId = dr1.entityId) " +
                   "and s.id = (select max(smax.id) from Subset smax where smax.version.id <= :versionId and smax.entityId = s.entityId) " +
                   "and sr.id = (select max(srmax.id) from SubsetRelationship srmax where srmax.version.id <= :versionId and srmax.entityId = sr.entityId) ");

        if (!allQualifiers)
        {
            sql.append("and qualifierNameDes.name in (:vitalQualifierNames) ");
        }

        Query query = session.createQuery(sql.toString());
        query.setString("vitalSubsetName", vitalSubsetName);
        query.setString("vitalTypeName", vitalTypeName);
        query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        query.setString("vhatCodeSystem", vhatCodeSystem);
        if (!allQualifiers)
        {
            query.setParameterList("vitalQualifierNames", vitalQualifierNames);
        }
        List<Object[]> result = (List<Object[]>)query.list();

        for (Iterator<Object[]> iter = result.iterator(); iter.hasNext();)
        {
        	Object[] object = (Object[]) iter.next();
        	String qualifierName = (String) object[0];
        	String conceptName = (String) object[1];
        	//put qualifierName as key since concept name will be the same for each record returned
        	vitalResults.put(qualifierName, conceptName);
        }
        
        return vitalResults;     
    }
	
	@SuppressWarnings("unchecked")
	public static List<PublishedRequestDTO> getPublishedRequestList(String name, String subset, String domainName)
	{
		Session session = HibernateSessionFactory.currentSession();
		//StringBuffer sql = new StringBuffer();
		String sql = "select requestedTermName.name AS name, subsetConceptName.name AS category " +  
				   "from " +
				   " Concept requestedTermName, " +
				   " Concept subsetConceptName, " +
				   " SubsetRelationship sr " +
				   " where  " +
				   "  upper(requestedTermName.name) = upper(:name) " + 
				   "  and sr.id = (select max(sr2.id) from SubsetRelationship sr2 where sr2.entityId = sr.entityId) " +
				   "  and sr.targetEntityId = requestedTermName.entityId " +
				   "  and sr.sourceEntityId = subsetConceptName.entityId " +
				   "  and subsetConceptName.name = :subset " +
				   "  and requestedTermName.active = 1 " +
				   "  and requestedTermName.id = " +
                   "   (SELECT MAX (des.id) " +
                   "    FROM Designation des, CodeSystem cs " + 
                   "     WHERE " +
                   "         des.version <= :versionId " +
                   "         AND des.codeSystem = cs.id " +
                   "         AND cs.name = :vhatCodeSystem " +
                   "         AND des.entityId = requestedTermName.entityId)";
		Query query = session.createQuery(sql).setResultTransformer(Transformers.aliasToBean(PublishedRequestDTO.class));
		query.setString("name", name);
		query.setString("subset", subset);
		query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		query.setString("vhatCodeSystem", vhatCodeSystem);
		List<PublishedRequestDTO> results = (List<PublishedRequestDTO>)query.list();
        return results;
	}
	
	public static boolean isMapSetSourceTerm(Long mapSetVuid, String sourceTerm)
	{
		boolean result = false;
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "select des from CodedConcept con, DesignationRelationship dr, Designation des" +
				"   where dr.sourceEntityId = con.entityId and dr.targetEntityId = des.entityId " +
				"   AND des.id = (select max(desmax.id) from Designation desmax where desmax.entityId = des.entityId) " +
	            "   AND con.id = (select max(conmax.id) from CodedConcept conmax where conmax.entityId = con.entityId) " +
	            "   AND dr.id = (select max(drmax.id) from DesignationRelationship drmax where drmax.entityId = dr.entityId)" +
				"   and des.type.id = 36 and upper(des.name) = :name and con.code in (" +
				"     select mapEntry.sourceCode from MapSet mapSet, MapSetRelationship mapSetRel, MapEntry mapEntry where " +
				"       mapSetRel.sourceEntityId = mapSet.entityId " +
				"       and mapSet.id = (select max(mapSet2.id) from MapSet mapSet2 where mapSet2.version.id <= :versionId and mapSet2.entityId = mapSet.entityId) " +
				"       and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.version.id <= :versionId and mapSetRel.entityId = mapSetRel2.entityId ) " +
				"       AND mapSetRel.active = 1 " +
				"       and mapEntry.entityId = mapSetRel.targetEntityId " +
				"       and mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.version.id <= :versionId and mapEntry2.entityId = mapEntry.entityId) " +
				"       and mapSet.vuid = :mapSetVuid )";


		Query query = session.createQuery(hql);
		query.setString("name", sourceTerm.toUpperCase());
		query.setLong("mapSetVuid", mapSetVuid);
		query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		

        List designations = query.list();
        if(designations.size() > 0)
        {
        	result = true;
        }
        
		return result;
	}
	
	
	
	public static boolean isValidMapSetSourceCode(Long mapSetVuid, String sourceCode)
	{
		boolean result = false;
		
		MapSet mapSet = MapSetDao.getByVuid(mapSetVuid, HibernateSessionFactory.AUTHORING_VERSION_ID);
		
		if(mapSet != null)
		{
			List<MapEntryDTO> mapEntries =  MapEntryDao.getEntriesBySourceCode(mapSet.getEntityId(), sourceCode);
			if(mapEntries.size() > 0)
			{
				result = true;
			}
		}
		
		return result;
	}
	
	// Checks if the code exists in the code system
	public static boolean isValidCode(String code, String codeSystemName)
	{
		boolean result = false;
		
		Session session = HibernateSessionFactory.currentSession();
		
		CodeSystem codeSystem = CodeSystemDao.get(codeSystemName);
		
		String hql = "from CodedConcept cc where cc.codeSystem.id = :codeSystemId and cc.code = :code " +
					"and cc.id = (select max(con.id) from CodedConcept con " +
					"where con.version.id <= :versionId and cc.codeSystem.id = :codeSystemId and con.entityId = cc.entityId)";
		
		Query query = session.createQuery(hql);
		query.setString("code", code);
		query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		query.setLong("codeSystemId", codeSystem.getId());
		
		List concepts = query.list();
		
		if(concepts.size() > 0)
		{
			result = true;
		}
		return result;
	}
	
	public static boolean isExistingMapping(String sourceCode, String targetCode, Long mapSetVuid)
	{
		Session session = HibernateSessionFactory.currentSession();
		
		String hql = "select mapEntry from MapSet mapSet, MapSetRelationship mapSetRel, MapEntry mapEntry where " +
				     "mapSetRel.sourceEntityId = mapSet.entityId " +
				     "and mapSet.id = (select max(mapSet2.id) from MapSet mapSet2 where mapSet2.version.id <= :versionId and mapSet2.entityId = mapSet.entityId) " +
				     "and mapSetRel.id = (select max(mapSetRel2.id) from MapSetRelationship mapSetRel2 where mapSetRel2.version.id <= :versionId and mapSetRel.entityId = mapSetRel2.entityId ) " +
				     "AND mapSetRel.active = 1 and mapEntry.entityId = mapSetRel.targetEntityId " +
				     "and mapEntry.id = (select max(mapEntry2.id) from MapEntry mapEntry2 where mapEntry2.version.id <= :versionId and mapEntry2.entityId = mapEntry.entityId) " +
				     "and mapSet.vuid = :mapSetVuid " + 
				     "and mapEntry.sourceCode = :sourceCode and mapEntry.targetCode = :targetCode";
		
		Query query = session.createQuery(hql);
		query.setString("sourceCode", sourceCode);
		query.setLong("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		query.setString("targetCode", targetCode);
		query.setLong("mapSetVuid",mapSetVuid);
		
		MapEntry mapEntry = (MapEntry)query.uniqueResult();

		return mapEntry != null;
	}
}