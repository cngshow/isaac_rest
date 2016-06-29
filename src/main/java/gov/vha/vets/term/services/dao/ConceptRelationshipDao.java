package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.ConceptRelationshipConceptListDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipListDTO;
import gov.vha.vets.term.services.dto.RelationshipTypeListDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

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
import org.hibernate.criterion.Restrictions;

public class ConceptRelationshipDao extends EntityBaseDao
{

    /**
     * Get the most recent entry for a given concept including anything in the
     * authoring version
     * 
     * @param relationship
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ConceptRelationship get(ConceptRelationship relationship) throws Exception
    {
        String query = "from ConceptRelationship conceptRel where conceptRel.id = (select max(cr.id) from ConceptRelationship cr "
                + " where cr.sourceEntityId = :sourceEntityId and cr.targetEntityId = :targetEntityId  and "
                + "cr.relationshipType.id = (select relType.id from RelationshipType relType where relType.name = :name))";

        List<ConceptRelationship> existingConceptRels = HibernateSessionFactory.currentSession().createQuery(query).setProperties(relationship)
                .setString("name", relationship.getRelationshipType().getName()).list();

        if (existingConceptRels.size() == 0)
        {
            return null;
        }

        return existingConceptRels.get(0);
    }
    
    /**
     * Return a concept relationship given the entity id
     * @param entityId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ConceptRelationship get(long entityId)
    {
    	String query = "from ConceptRelationship conceptRel where conceptRel.id in " +
    	 				"(select max(cr.id) from ConceptRelationship cr where cr.version.id < :"+AUTHORING_VERSION+"  and cr.entityId = :entityId)";
    	 
    	 List<ConceptRelationship> existingConceptRels = HibernateSessionFactory.currentSession().createQuery(query)
     												.setLong("entityId", entityId).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).list();
    	 
    	 if (existingConceptRels.size() == 0)
    	 {
    		 return null;
    	 }
    	 
    	 return existingConceptRels.get(0);
    }

    /**
     * Get the most recent relationships for a given concept entity - both regular and inverse including anything in the
     * authoring version
     * 
     * @param relationship
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getBySourceOrTarget(long conceptEntityId) throws STSException
    {
        String query = "from ConceptRelationship conceptRel where conceptRel.id in (select max(cr.id) from ConceptRelationship cr "
                + " where cr.sourceEntityId = :entityId or cr.targetEntityId = :entityId group by cr.entityId)";

        Query myQuery = HibernateSessionFactory.currentSession().createQuery(query);
        myQuery.setLong("entityId", conceptEntityId);

        List<ConceptRelationship> relationships = myQuery.list(); 

        return relationships;
    }
    /**
     * Get the most recent relationships for a given concept entity - both regular and inverse including anything in the
     * authoring version
     * 
     * @param relationship
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getBySource(long conceptEntityId) throws STSException
    {
        String query = "from ConceptRelationship conceptRel where conceptRel.sourceEntityId = :entityId "
        	+ " and conceptRel.id = (select max(cr.id) from ConceptRelationship cr where cr.entityId = conceptRel.entityId)";

        Query myQuery = HibernateSessionFactory.currentSession().createQuery(query);
        myQuery.setLong("entityId", conceptEntityId);

        List<ConceptRelationship> relationships = myQuery.list(); 

        return relationships;
    }
    
    /**
     * Get the most recent entry for a given concept including anything in the
     * authoring version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param targetCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    public static ConceptRelationship get(CodeSystem codeSystem, String sourceCode, String targetCode, String relationshipTypeName)
    {

        String hql = "from ConceptRelationship conrel where id = (select max(id) from ConceptRelationship where entityId = (select entityId from ConceptRelationship conceptRel "
                + "where conceptRel.id = (select max(cr.id) from ConceptRelationship cr "
                + " where cr.sourceEntityId = (select distinct scon.entityId from CodedConcept scon where scon.code = :sourceCode and scon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))"
                + " and cr.targetEntityId = (select distinct tcon.entityId from CodedConcept tcon where tcon.code = :targetCode and tcon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))"
                + " and cr.relationshipType = (select relType.id from RelationshipType relType where relType.name = :typeName))))"
                + " and conrel.sourceEntityId = (select distinct scon.entityId from CodedConcept scon where scon.code = :sourceCode and scon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))"
                + " and conrel.targetEntityId = (select distinct tcon.entityId from CodedConcept tcon where tcon.code = :targetCode and tcon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))";

        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
        query.setString("sourceCode", sourceCode);
        query.setString("targetCode", targetCode);
        query.setString("codeSystemName", codeSystem.getName());
        query.setString("typeName", relationshipTypeName);

        
        return (ConceptRelationship) query.uniqueResult();
    }

    /**
     * Get the most recent "real" version
     * 
     * @param relationship
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ConceptRelationship getVersioned(ConceptRelationship relationship)
    {
        String query = "from ConceptRelationship conceptRel " + "where conceptRel.id = (select max(cr.id) from ConceptRelationship cr "
                + " where cr.version.id < :"+AUTHORING_VERSION+" and cr.sourceEntityId = :sourceEntityId" + " and cr.entityId = :entityId"
                + " and cr.relationshipType.id = (select relType.id from RelationshipType relType where relType.name = :name))";

        List<ConceptRelationship> existingConceptRels = HibernateSessionFactory.currentSession().createQuery(query).setProperties(relationship)
                .setLong("entityId", relationship.getEntityId()).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).setString(
                        "name", relationship.getRelationshipType().getName()).list();

        if (existingConceptRels.size() == 0)
        {
            return null;
        }

        return existingConceptRels.get(0);
    }

    /**
     * Get the most recent "real" version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ConceptRelationship getVersioned(CodeSystem codeSystem, String sourceCode, String relationshipTypeName)
    {
        String query = "from ConceptRelationship conceptRel "
                + "where conceptRel.id = (select max(cr.id) from ConceptRelationship cr "
                + " where cr.version.id < :"+AUTHORING_VERSION+""
                + " and cr.sourceEntityId = (select scon.entityId from CodedConcept scon where scon.code = :sourceCode and scon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName))"
                + " and cr.relationshipType = (select relType.id from RelationshipType relType where relType.name = :typeName))";

        List<ConceptRelationship> existingConceptRels = HibernateSessionFactory.currentSession().createQuery(query).setString("sourceCode",
                sourceCode).setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID).setString("codeSystemName",
                codeSystem.getName()).setString("typeName", relationshipTypeName).list();

        if (existingConceptRels.size() == 0)
        {
            return null;
        }

        return existingConceptRels.get(0);
    }

    /**
     * Get the most recent "real" version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getPreviousVersionRelationships(long conceptEntityId, boolean inverse, long versionId)
    {
        String query;
        if (inverse)
        {
            query = "from ConceptRelationship conceptRel where conceptRel.targetEntityId = :entityId " +
                "and conceptRel.id in (select max(cr.id) from ConceptRelationship cr where cr.version.id < :version and cr.entityId in " +
				"(select cr2.entityId from ConceptRelationship cr2 where cr2.targetEntityId = :entityId) group by entity_id)";        
        }
        else
        {
            query = "from ConceptRelationship conceptRel where conceptRel.sourceEntityId = :entityId " +
            	"and conceptRel.id = (select max(cr.id) from ConceptRelationship cr where cr.version.id < :version and cr.entityId = conceptRel.entityId)";
        }

        List<ConceptRelationship> conceptRelations = HibernateSessionFactory.currentSession().createQuery(query).setLong("entityId", conceptEntityId).setLong("version", versionId).list();

        return conceptRelations;
    }

    /**
     * Get the most recent "real" version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getRelationships(long conceptEntityId, boolean inverse, long versionId)
    {
        String query;
        if (inverse)
        {
            query = "from ConceptRelationship conceptRel where conceptRel.targetEntityId = :entityId " +
            	"and conceptRel.id in (select max(cr.id) from ConceptRelationship cr where cr.version.id <= :version and cr.entityId in " +
				"(select cr2.entityId from ConceptRelationship cr2 where cr2.targetEntityId = :entityId) group by entity_id)";
        }
        else
        {
            query = "from ConceptRelationship conceptRel where conceptRel.sourceEntityId = :entityId " +
            	"and conceptRel.id = (select max(cr.id) from ConceptRelationship cr where cr.version.id <= :version and cr.entityId = conceptRel.entityId)";
        }

        List<ConceptRelationship> conceptRelations = HibernateSessionFactory.currentSession().createQuery(query).setLong("entityId", conceptEntityId).setLong("version", versionId).list();

        return conceptRelations;
    }

    /**
     * Get the most recent "real" version
     * 
     * @param codeSystem
     * @param sourceCode
     * @param relationshipTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, Map<String, Set<String>>> getRelationshipsForCodeSystem(long versionId)
    {
    	Map results = new HashMap<Long, Map<String, Set<String>>>();
        String hql = "select scon.entityId, tcon.code, conceptRel.relationshipType.name from ConceptRelationship conceptRel, CodedConcept scon, CodedConcept tcon " 
      		+ "where conceptRel.targetEntityId = tcon.entityId "
      		+ "and conceptRel.sourceEntityId = scon.entityId "
        	+ "and scon.codeSystem.id = (select v.codeSystem.id from Version v where v.id = :versionId) " 
        	+ "and conceptRel.id = (select max(crmax.id) from ConceptRelationship crmax where crmax.entityId = conceptRel.entityId and crmax.version.id <= :versionId  ) " 
        	+ "and scon.id = (select max(sconmax.id) from CodedConcept sconmax where sconmax.entityId = scon.entityId and sconmax.version.id <= :versionId) "
        	+ "and tcon.id = (select max(tconmax.id) from CodedConcept tconmax where tconmax.entityId = tcon.entityId and tconmax.version.id <= :versionId) ";

        Query query = HibernateSessionFactory.currentSession().createQuery(hql);
        query.setParameter("versionId", versionId);
        
        List<Object[]> rows = query.list();
        for (Object[] row : rows)
		{
			Long entityId = (Long) row[0];
			Map<String, Set<String>> conceptRelationshipMap = (Map<String, Set<String>>) results.get(entityId);
			if (conceptRelationshipMap == null)
			{
				conceptRelationshipMap = new HashMap<String, Set<String>>();
				results.put(entityId, conceptRelationshipMap);
			}
			String code = (String) row[1];
			String relationshipType = (String) row[2];
			Set<String> values = conceptRelationshipMap.get(relationshipType);
			if (values == null)
			{
				values = new HashSet<String>();
				conceptRelationshipMap.put(relationshipType, values);
			}
			values.add(code);
		}
        return  results;
    }
    /**
     * Get a relationship type by name
     * 
     * @param session
     * @param typeName
     * @return
     * @throws Exception
     */
    public static RelationshipType getType(String typeName)
    {
        RelationshipType type = (RelationshipType) HibernateSessionFactory.currentSession().createCriteria(RelationshipType.class).add(
                Restrictions.eq("name", typeName)).uniqueResult();
        return type;
    }
    
    /**
     * Get a relationship type by its ID
     * 
     * @param id
     * @return
     */
    public static RelationshipType getType(long id)
    {
    	RelationshipType type = (RelationshipType) HibernateSessionFactory.currentSession().createCriteria(RelationshipType.class).add(
                Restrictions.eq("id", id)).uniqueResult();
        return type;
    }
   
    /**
     * Returns all the relationships types 
     */
    @SuppressWarnings("unchecked")
    public static RelationshipTypeListDTO getAllTypes(String typeName, CodeSystem codeSystem, Version version, Integer pageSize, Integer pageNumber)
    {	
    	RelationshipTypeListDTO relationshipTypeListDTO = new RelationshipTypeListDTO();
    	
    	if(pageNumber == 1)
    	{
    		Query query = getByTypeQuery(true, typeName, codeSystem, version, pageSize, pageNumber);
    		relationshipTypeListDTO.setTotalNumberOfRecords((Long)query.uniqueResult());
    	}
    	
    	Query query = getByTypeQuery(false, typeName, codeSystem, version, pageSize, pageNumber);
    	relationshipTypeListDTO.setRelationshipTypes(query.list());
    	
        return relationshipTypeListDTO;
    }

    public static Query getByTypeQuery(boolean retrieveCount, String typeName, CodeSystem codeSystem, Version version, Integer pageSize, Integer pageNumber)
    {	
    	String hql = "";
    	String typeNameFilter = "";
    	
    	if(codeSystem == null && version == null)
    	{
    		hql = (retrieveCount ? "select count(*) " : "") +
    							" from RelationshipType rt";
    		typeNameFilter = " where upper(rt.name) like upper(:nameFilter)";
    	}
    	else if(codeSystem != null || version != null)
    	{
    		// When version is given, code system will not be null 
    		hql = (retrieveCount ? "select count(distinct r.relationshipType) " : "select distinct r.relationshipType ") +
    				"from ConceptRelationship r, CodedConcept c where r.sourceEntityId = c.entityId " +
					"and c.codeSystem.id = :codeSystemId ";
	
    		if(version != null)
    		{
    			// add version information
    			hql += "and r.id = (select max(conr.id) from ConceptRelationship conr where conr.version.id <= :versionId and r.entityId = conr.entityId ) ";
    			hql += "and c.id = (select max(cc.id) from CodedConcept cc where cc.version.id <= :versionId and c.entityId = cc.entityId) ";
    		}    		   		    	
    		typeNameFilter = " and r.relationshipType.name like :nameFilter";
    	}
    	
    	Session session = HibernateSessionFactory.currentSession();
    	if (typeName != null)
    	{
    		hql = hql.concat(typeNameFilter);
    	}
    	
    	Query query = session.createQuery(hql);
    	if (typeName != null)
        {
            query.setParameter("nameFilter", "%"+typeName+"%");
        }
    	if(codeSystem != null)
    	{
    		query.setParameter("codeSystemId", codeSystem.getId());
    	}
    	if(version != null)
    	{
    		query.setParameter("versionId", version.getId());
    	}
    	
    	if(!retrieveCount)
    	{
        	query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
    	}
            	
        return query;
    }

    
    
    
    @SuppressWarnings("unchecked")
    public static ConceptRelationshipListDTO getRelationships(CodeSystem codeSystem, Version version, String sourceConceptCode, 
			String targetConceptCode, String relationshipTypeName, Integer pageSize, Integer pageNumber)
	{
    	ConceptRelationshipListDTO relationshipListDto = new ConceptRelationshipListDTO();
    	
    	if(pageNumber == 1)
    	{
    		// first page, get the count
    		Query query = getByRelationshipQuery(true, codeSystem, version, sourceConceptCode, targetConceptCode, relationshipTypeName, pageSize, pageNumber);
    		relationshipListDto.setTotalNumberOfRecords((Long) query.uniqueResult());
    	}
    	
    	Query query = getByRelationshipQuery(false, codeSystem, version, sourceConceptCode, targetConceptCode, relationshipTypeName, pageSize, pageNumber);
    	relationshipListDto.setRelationships(query.list());
        
        return relationshipListDto;
	}
    
    
    private static Query getByRelationshipQuery(boolean retrieveCount, CodeSystem codeSystem, Version version, String sourceConceptCode, 
			String targetConceptCode, String relationshipTypeName, Integer pageSize, Integer pageNumber)
    {
		Session session = HibernateSessionFactory.currentSession();
		
		String sourceCodeFilter = sourceConceptCode != null ? "and ccs.code = :sourceCode " : "";
    	String targetCodeFilter = targetConceptCode != null ? "and cct.code = :targetCode " : "";
    	
    	String relationshipFilter = relationshipTypeName != null ? "and upper(r.relationshipType.name) = upper(:relationshipTypeName) " : "";
    	
    	String hql = (retrieveCount ? "select count(*) " : "select r ") +
    					"from ConceptRelationship r, CodedConcept cct, CodedConcept ccs where r.sourceEntityId = ccs.entityId " +
    					"and r.targetEntityId = cct.entityId and ccs.codeSystem.id = :codeSystemId and cct.codeSystem.id = :codeSystemId " +
    					"and r.id = (select max(conr.id) from ConceptRelationship conr where conr.version.id <= :versionId and conr.entityId = r.entityId )" +
    					"and cct.id = (select max(cc.id) from CodedConcept cc where cc.version.id <= :versionId and cc.entityId = cct.entityId) " +
    					"and ccs.id = (select max(cc.id) from CodedConcept cc where cc.version.id <= :versionId and cc.entityId = ccs.entityId) " ;
    			
    	hql += sourceCodeFilter + targetCodeFilter + relationshipFilter;
    	
    	Query query = session.createQuery(hql);
    	query.setParameter("codeSystemId", codeSystem.getId());
		query.setParameter("versionId", version.getId());
		
    	if (sourceConceptCode != null)
    	{
    		query.setParameter("sourceCode", sourceConceptCode);
    	}
    	if (targetConceptCode != null)
    	{
    		query.setParameter("targetCode", targetConceptCode);
    	}
    	if (relationshipTypeName != null)
    	{
    		query.setParameter("relationshipTypeName", relationshipTypeName);
    	}
    	
    	if(retrieveCount == false)
    	{
        	query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
    	}
    	
    	return query;
    }
    
    
    /**
     * Returns all the relationships types
     */
    @SuppressWarnings("unchecked")
    public static List<String> getTypesByCodeSystem(String codeSystemName)
    {
        Session session = HibernateSessionFactory.currentSession();
        String hql = "select distinct r.relationshipType.name from ConceptRelationship r, CodedConcept c where r.sourceEntityId = c.entityId " +
        		"and c.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName) order by r.relationshipType.name";
        Query query = session.createQuery(hql);
        query.setParameter("codeSystemName", codeSystemName);
        return query.list();
    }

    /**
     * Get the relationship detail for a given concept
     * 
     * @param conceptEntityId
     * @param includeAuthoring 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationshipDTO> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        
        List<ConceptRelationshipDTO> results = new ArrayList<ConceptRelationshipDTO>();

        String relationshipQuery = "from ConceptRelationship r, CodedConcept con "
                + "where r.sourceEntityId = :conceptEntityId and con.entityId = r.targetEntityId and r.version.id "+operator+" :"+AUTHORING_VERSION+" order by r.entityId, r.id";
        Query query = session.createQuery(relationshipQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List relationships = query.list();
        for (Iterator iter = relationships.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            ConceptRelationship relationship = (ConceptRelationship)object[0];
            CodedConcept concept = (CodedConcept) object[1];
            ConceptRelationshipDTO detail = new ConceptRelationshipDTO(concept.getCode(), relationship, concept.getName());
            results.add(detail);
        }
        return results;
    }

    /**
     * Get the relationship detail for a given concept
     * 
     * @param conceptEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<ConceptRelationshipDTO> getAllChildrenVersions(long conceptEntityId,  boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        List<ConceptRelationshipDTO> results = new ArrayList<ConceptRelationshipDTO>();

        String relationshipQuery = "from ConceptRelationship r, CodedConcept con "
                + "where r.targetEntityId = :conceptEntityId and r.relationshipType in " 
                + "(select id from RelationshipType where (name = :relationshipTypeParent or name = :relationshipTypeRoot)) and con.entityId = r.sourceEntityId and r.version.id "+operator+" :"+AUTHORING_VERSION+" order by r.entityId, r.id";
        
        Query query = session.createQuery(relationshipQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setString("relationshipTypeParent", RelationshipType.HAS_PARENT);
        query.setString("relationshipTypeRoot", RelationshipType.HAS_ROOT);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        
        List relationships = query.list();
        for (Iterator iter = relationships.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            ConceptRelationship relationship = (ConceptRelationship)object[0];
            CodedConcept concept = (CodedConcept) object[1];
            ConceptRelationshipDTO detail = new ConceptRelationshipDTO(concept.getCode(), relationship, concept.getName());
            results.add(detail);
        }

        return results;
    }
    
    public static RelationshipType createType(String relationshipTypeName)
    {
        RelationshipType type = new RelationshipType();
        type.setName(relationshipTypeName);
        HibernateSessionFactory.currentSession().save(type);

        return type;
    }

    public static void save(ConceptRelationship conceptRelationship)
    {
        HibernateSessionFactory.currentSession().save(conceptRelationship);
    }

    public static void delete(ConceptRelationship conceptRelationship)
    {
        HibernateSessionFactory.currentSession().delete(conceptRelationship);
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getChangedRelationshipTypes(long conceptEntityId, long versionId, List<String> relationshipTypeNames, boolean inverse)
    {
        Session session = HibernateSessionFactory.currentSession();
        String direction = inverse ? "target" : "source";
        String query = "select distinct cr.relationshipType from ConceptRelationship cr where cr."+direction+"EntityId = :entityId "
            + " and cr.version = :versionId and cr.relationshipType.name in (:relationshipTypeNames)";
        List<String> names = new ArrayList<String>();

        if (relationshipTypeNames != null && relationshipTypeNames.size() > 0)
        {
            List<RelationshipType> list = (List<RelationshipType>)session.createQuery(query).setLong("entityId", conceptEntityId).setLong("versionId", versionId).
                setParameterList("relationshipTypeNames", relationshipTypeNames).list();
            for (RelationshipType data : list)
            {
                names.add(data.getName());
            }
        }
        return names;
    }
    /**
     * get the target concepts preferred designation for the given relationship types 
     * @param conceptEntityId
     * @return
     */    
    @SuppressWarnings("unchecked")
    public static Map<String, List<Object>> getTargetDesignations(long conceptEntityId, List<String> relationshipTypeNames, long versionId, boolean inverse) 
    {
        Session session = HibernateSessionFactory.currentSession();
        Map<String, List<Object>> map = new HashMap<String, List<Object>>();
        String direction1 = inverse ? "target" : "source";
        String direction2 = inverse ? "source" : "target";
        String query = "select cr.relationshipType, d.vuid "
            + " from ConceptRelationship cr, DesignationRelationship dr, Designation d where cr.active = 1 and cr."+direction1+"EntityId = :entityId and " 
            + "   cr.id in " 
            + "   (select max(cr2.id) from ConceptRelationship cr2 where cr2.version.id <= :versionId and cr2.entityId in (select cr3.entityId from ConceptRelationship cr3 where cr3."+direction1+"EntityId = :entityId "
			+ "     and cr3.relationshipType in (select id from RelationshipType where name in (:relationshipTypeNames)))"
            + "   group by cr2.entityId) "
            + " and "
            + "   dr.id in (select max(dr2.id) from DesignationRelationship dr2 where dr2.version.id <= :versionId and dr2.sourceEntityId = cr."+direction2+"EntityId group by dr2.entityId) "
            + " and "
            + "   d.id = (select max(d2.id) from Designation d2 where d2.version.id <= :versionId and d2.entityId = dr.targetEntityId) "
            + " and d.type = (select dt.id from DesignationType dt where dt.name = :preferredName)";

        List<Object[]> list = (List<Object[]>)session.createQuery(query).setLong("entityId", conceptEntityId).setString("preferredName", DesignationType.PREFERRED_NAME).
            setParameterList("relationshipTypeNames", relationshipTypeNames).setParameter("versionId", versionId).list();
        
        for (Object[] data : list)
        {
            RelationshipType relationshipType = (RelationshipType) data[0];
            List<Object> vuidList = map.get(relationshipType.getName());
            if (vuidList == null)
            {
                vuidList = new ArrayList<Object>();
                map.put(relationshipType.getName(), vuidList);
            }
            vuidList.add((Long)data[1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Collection<ConceptRelationshipDTO>> getTargetConcepts(Collection<Long> conceptEntityIds) 
    {
        Session session = HibernateSessionFactory.currentSession();
        Map<Long, Collection<ConceptRelationshipDTO>> results = new HashMap<Long, Collection<ConceptRelationshipDTO>>();
        String hql = 
              " from ConceptRelationship cr, CodedConcept c where " +
              "cr.sourceEntityId in (:conceptEntityIds) " 
            + "and cr.id in (select max(cr2.id) from ConceptRelationship cr2 where cr.entityId = cr2.entityId) "
            + "and c.id in (select max(c2.id) from CodedConcept c2 where c.entityId = c2.entityId) "
            + "and c.entityId = cr.targetEntityId ";

        Query query = session.createQuery(hql);

        List<Object[]> list = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        
        for (Object[] data : list)
        {
            ConceptRelationship conceptRelationship = (ConceptRelationship) data[0];
            CodedConcept concept = (CodedConcept) data[1];
            ConceptRelationshipDTO conceptRelationshipDTO = new ConceptRelationshipDTO(concept.getCode(), conceptRelationship, concept.getName());
            Collection<ConceptRelationshipDTO> relList = results.get(conceptRelationship.getSourceEntityId());
            if (relList == null)
            {
                relList = new ArrayList<ConceptRelationshipDTO>();
                results.put(conceptRelationship.getSourceEntityId(), relList);
            }
            relList.add(conceptRelationshipDTO);
        }
        return results;
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
        String query = "update ConceptRelationship cr set cr.version = :"+NEW_VERSION+" where cr.version.id = :"+AUTHORING_VERSION+" and "
                + " cr.sourceEntityId in (:entityId)";

        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }

    @SuppressWarnings("unchecked")
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "Select c.entityId from CodedConcept c where c.entityId in (select cr.sourceEntityId from ConceptRelationship cr where cr.version.id = :versionId)";
        List<Long> conceptEntityIds = session.createQuery(query).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }

    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getRelationships(Collection<Long> conceptEntityIds, List<String> relationshipTypeNames, Collection<Long> possibleConceptEntityIds, boolean inverse, boolean includeInactives)
    {
        Session session = HibernateSessionFactory.currentSession();
        String direction = inverse ? "target" : "source";
        String activeFilter = includeInactives ? "" : "and active = 1";
        
        // filter on relationship types (inclusive or exclusive)
        String typeFilter = (relationshipTypeNames != null && relationshipTypeNames.size() > 0) ? "relationshipType.name in (:relationshipTypeNames) and" : "";
        
        // limit the related concepts to this domain
        String conceptDomainFilter = (possibleConceptEntityIds != null && possibleConceptEntityIds.size() > 0) ? "and "+((inverse)?"source" : "target")+"EntityId in (:possibleConceptIds) " : "";

        // main hql body
        String hql = "from ConceptRelationship where id in (select max(id) from ConceptRelationship where "+typeFilter+" "+direction+"EntityId in (:conceptEntityIds) "+conceptDomainFilter+" group by entityId) "+activeFilter;

        Query query = session.createQuery(hql);
        if (typeFilter.length() > 0)
        {
            query.setParameterList("relationshipTypeNames", relationshipTypeNames);
        }
        
        List<ConceptRelationship> results = null;        
        if (conceptDomainFilter.length() > 0)
        {
            results = executeQuery(query, "conceptEntityIds", conceptEntityIds, "possibleConceptIds", possibleConceptEntityIds);
        }
        else
        {
            results = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        }
        
        return results;
    }

    @SuppressWarnings("unchecked")
    public static List<ConceptRelationship> getRelationships(List<Long> sourceConceptEntityIds, List<String> relationshipTypeNames, List<Long> targetConceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        // main hql body
        String hql = "from ConceptRelationship crfinal where crfinal.id in (" +
        		"select max(id) from ConceptRelationship crmax where crmax.entityId = crfinal.entityId )" +
        		"and crfinal.relationshipType.name in (:relationshipTypeNames) " +
        		"and crfinal.sourceEntityId in (:sourceConceptEntityIds) " +
        		"and crfinal.targetEntityId in (:targetConceptEntityIds) " +
        		"))";

        Query query = session.createQuery(hql);
        query.setParameterList("relationshipTypeNames", relationshipTypeNames);
        query.setParameterList("sourceConceptEntityIds", sourceConceptEntityIds);
        query.setParameterList("targetConceptEntityIds", targetConceptEntityIds);

        
        return query.list();
    }
    
    public static ConceptRelationship getPreviousRelationship(long entityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        // main hql body
        String hql = "from ConceptRelationship cr where cr.entityId = :entityId and " +
        		" cr.id = (select max(cr2.id) from ConceptRelationship cr2 where cr2.entityId = cr.entityId and cr2.version.id < :versionId)";

        Query query = session.createQuery(hql);
        query.setParameter("entityId", entityId);
        query.setParameter("versionId", versionId);
        return (ConceptRelationship) query.uniqueResult();
    }

    public static Collection<String> getPath(long codeSystemId, long versionId, String sourceCode, String targetCode, long relationshipTypeId)
    {
        String sql = 
            "select SYS_CONNECT_BY_PATH (t.code,'|') as code " +
            " from  relationship r, concept t , concept s " +
            "  where r.type_id = :typeId " +
            "  and r.source_entity_id = s.entity_id  " +
            "  and r.target_entity_id = t.entity_id   " +
            "  and t.id = (select max(id) from concept cmax where cmax.entity_id = t.entity_id and version_id <= :versionId)  " +
            "  and s.id = (select max(id) from concept cmax where cmax.entity_id = s.entity_id and version_id <= :versionId)  " +
            "  and r.kind = 'C' " +
            "  and t.code = :targetCode and t.codeSystem_id = :codeSystemId " +
            "  and r.id = (select max(maxrel.id) from relationship maxrel where maxrel.entity_id = r.entity_id and version_id <= :versionId)  " +
            "  and r.active = 1  " +
            "connect by nocycle  " +
            "  prior r.target_entity_id=r.source_entity_id " +
            "start with s.code = :sourceCode and S.CODESYSTEM_ID = :codeSystemId  "; 

        Session session = HibernateSessionFactory.currentSession();
        Query query = session.createSQLQuery(sql);
        query.setParameter("codeSystemId", codeSystemId);
        query.setParameter("versionId", versionId);
        query.setParameter("typeId", relationshipTypeId);
        query.setParameter("sourceCode", sourceCode);
        query.setParameter("targetCode", targetCode);
        
        List<String> paths = (List<String>)query.list();
        // prepend the source Code value to the start of the path
        Collection<String> results = new ArrayList<String>();
        for (String path : paths)
        {
            if (path != null && path.length() > 0)
            {
                path = sourceCode + path;
            }
            else
            {
                path = "";
            }
            results.add(path);
        }
        return results;
    }
    
    public static boolean isConceptSubsumedRelationship(Version version, long sourceEntityId, long targetEntityId, String relationshipTypeName)
    {
        String sql = 
            "select distinct target_entity_id from  relationship r " +
            "  where r.type_id = (select id from type where name = :typeName)   " +
            "  and r.kind = 'C'   " +
            "  and  target_entity_id = :targetEntityId  " +
            "  and r.id = (select max(maxrel.id) from relationship maxrel where maxrel.entity_id = r.entity_id and r.version_id <= :versionId)  " +
            "  and r.active = 1  " +
            " start with r.source_entity_id =  (select entity_id from concept where entity_id = :sourceEntityId and codesystem_id = :codesystemId)  " +
            "connect by  " +
            "  prior r.target_entity_id=r.source_entity_id   "; 

        Session session = HibernateSessionFactory.currentSession();
        Query query = session.createSQLQuery(sql);        
        query.setParameter("typeName", relationshipTypeName);
        query.setParameter("sourceEntityId", sourceEntityId);
        query.setParameter("targetEntityId", targetEntityId);
        query.setParameter("versionId", version.getId());
        query.setParameter("codesystemId", version.getCodeSystem().getId());
        Object object = query.uniqueResult();
        return object == null ? false : true;
    }
    
    @SuppressWarnings("unchecked")
    public static ConceptRelationshipConceptListDTO getChildren(Long vuid, String parentName, String childName, Integer pageSize, Integer pageNumber)
    {
    	Long conceptRelationshipCount = null;
    	if (pageNumber != null && pageNumber == 1)
    	{
    		Query query = getChildrenQuery(true, vuid, parentName, childName, pageSize, pageNumber);
    		conceptRelationshipCount = (Long) query.uniqueResult();
    	}

    	Query query = getChildrenQuery(false, vuid, parentName, childName, pageSize, pageNumber);
        List conRelationships = query.list();
        List<ConceptRelationshipDTO> conceptRelationshipDTOs = new ArrayList<ConceptRelationshipDTO>();
        for (Iterator iter = conRelationships.iterator(); iter.hasNext();)
        {
            Object[] object = (Object[]) iter.next();
            ConceptRelationship relationship = (ConceptRelationship)object[0];
            CodedConcept concept = (CodedConcept) object[1];
            ConceptRelationshipDTO conceptRelationshipDTO = new ConceptRelationshipDTO(concept.getCode(), relationship, concept.getName());
            conceptRelationshipDTOs.add(conceptRelationshipDTO);
        }
        
        ConceptRelationshipConceptListDTO conceptRelationshipConceptListDTO = new ConceptRelationshipConceptListDTO();
        conceptRelationshipConceptListDTO.setConceptRelationshipDTOs(conceptRelationshipDTOs);
        conceptRelationshipConceptListDTO.setTotalNumberOfRecords(conceptRelationshipCount);

        return conceptRelationshipConceptListDTO;
    }

    public static ConceptRelationshipConceptListDTO getChildren(String parentName, String childNameFilter, Integer pageSize, Integer pageNumber)
    {
        return getChildren(null, parentName, childNameFilter, pageSize, pageNumber);
    }
    
    private static Query getChildrenQuery(boolean retrieveCount, Long vuid, String parentName, String childName, Integer pageSize, Integer pageNumber)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String parentNameFilter = (parentName != null) ? "and upper(tcon.name) = upper(:parentName) " : ""; 
        String childVuidFilter = "and c.vuid = :vuid ";
        String childNameFilter = "and upper(c.name) like upper(:childName) ";
        
        String hql = 
            "select " + ((retrieveCount == false) ? "r, c" : "count(*)") +
              " from ConceptRelationship r, CodedConcept c where " +
                "c.entityId = r.sourceEntityId " +
                "and r.active = 1 " +
                "and r.relationshipType = (select relType.id from RelationshipType relType where relType.name = 'has_parent') " +
                "and c.id = (select MAX(cmax.id) from CodedConcept cmax where cmax.entityId = c.entityId) " +
                "and r.id = (select MAX(crmax.id) FROM ConceptRelationship crmax where crmax.entityId = r.entityId) " +
                "and r.targetEntityId = (select distinct tcon.entityId from CodedConcept tcon where tcon.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName) "+parentNameFilter+") ";

        if (vuid != null)
        {
            hql = hql.concat(childVuidFilter);
        }
        if (childName != null)
        {
            hql = hql.concat(childNameFilter);
        }
        Query query = session.createQuery(hql);
        query.setParameter("codeSystemName", HibernateSessionFactory.VHAT_NAME);
        
        if (vuid != null)
        {
            query.setParameter("vuid", vuid);
        }
        if (parentName != null)
        {
            query.setParameter("parentName", parentName);
        }
        if (childName != null)
        {
            query.setParameter("childName", "%"+childName+"%");
        }

        if (retrieveCount == false && pageNumber != null && pageSize != null)
    	{
	        query.setFirstResult((pageNumber - 1) * pageSize);
	        query.setMaxResults(pageSize);
    	}
        
        return query;
    }
}
