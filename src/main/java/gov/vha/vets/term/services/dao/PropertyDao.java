package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PropertyDao extends EntityBaseDao
{
    @SuppressWarnings("unchecked")
    public static List<Property> getProperties(List<Long> conceptEntityIds, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "from Property p where p.active = 1 and p.conceptEntityId in (:conceptEntityIds)" +
        		" and p.id = (select max(prop.id) from Property prop where prop.version.id <= :versionId and p.entityId = prop.entityId)";
        Query query = session.createQuery(hql);

        query.setLong("versionId", versionId);
        List<Property> list = executeQuery(query, "conceptEntityIds", conceptEntityIds); 
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Property> getPropertiesByVersionId(long versionId, boolean isFullVersion)
    {
        Session session = HibernateSessionFactory.currentSession();
        String operator = isFullVersion ? "<=" : "=";
        String hql = "from Property p where p.active = 1 and p.id = (select max(prop.id) from Property prop where prop.version.id "+operator+" :versionId and p.entityId = prop.entityId)";

        Query query = session.createQuery(hql).setLong("versionId", versionId);
        List<Property> list = query.list();
        
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Property> getProperties(Long conceptEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "from Property p where p.active = 1 and p.id in (select max(prop.id) from Property prop " +
                "where p.entityId = prop.entityId) and p.conceptEntityId = :conceptEntityId";
        Query query = session.createQuery(hql);

        query.setLong("conceptEntityId", conceptEntityId);
        List<Property> list = query.list();
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Property> getProperties(long conceptEntityId, Version version)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "from Property p where p.active = 1 and p.id = (select max(prop.id) from Property prop " +
                "where prop.version <= :version " +
                "and p.entityId = prop.entityId) and p.conceptEntityId = :conceptEntityId ";
        
        List<Property> list = session.createQuery(hql).setParameter("version", version).setLong("conceptEntityId", conceptEntityId).list();
        return list;
    }
    
    /**
     * Get Properties with a given conceptEntityId, version, and flag to determine whether the property is active or not
     * @param conceptEntityId
     * @param version
     * @param includeInactive
     * @return List of property model objects
     */
    @SuppressWarnings("unchecked")
	public static List<Property> getProperties(long conceptEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "from Property p where p.id in (select max(prop.id) from Property prop " +
                "where prop.version.id <= :versionId and p.entityId = prop.entityId) and p.conceptEntityId = :conceptEntityId ";
        
        List<Property> list = session.createQuery(hql).setLong("versionId", versionId).setLong("conceptEntityId", conceptEntityId).list();
        return list;
    }
    

    @SuppressWarnings("unchecked")
    public static List<Property> getProperties(Collection<Long> conceptEntityIdList, long versionId, List<String> propertyTypeNameList)
    {
    	List<Property> list = new ArrayList<Property>();
    	if (!propertyTypeNameList.isEmpty())
    	{
	        Session session = HibernateSessionFactory.currentSession();
	
	        String hql = "from Property p where p.active = 1 and p.id in (select max(prop.id) from Property prop " +
	                "where p.entityId = prop.entityId and prop.version.id <= :versionId) and p.conceptEntityId in (:conceptEntityIdList) and " +
	                " p.propertyType in (from PropertyType where name in (:propertyNameList))";
	
	        Query query = session.createQuery(hql);
	        query.setLong("versionId", versionId);
	        query.setParameterList("propertyNameList", propertyTypeNameList);
	        list = executeQuery(query, "conceptEntityIdList", conceptEntityIdList);
    	}
        return list;
    }
    /*
     * get properties by property type 
     */
    public static List<Property> getPropertiesByTypeName(String type)
    {
    	List<Property> list = new ArrayList<Property>();
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id in (select prop.id from Property prop "
                + "where prop.propertyType = (from PropertyType where name = :type)) ";
        return session.createQuery(query).setString("type", type).list();
    }

    /**
     * Get all the properties that have changed in a particular version filtered by property type
     * @param conceptEntityId
     * @param version
     * @param propertyTypeNameList
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, List<String>> getChangedPropertyTypes(List<Long> conceptEntityIdList, long versionId, List<String> propertyTypeNameList)
    {
    	Map<Long, List<String>> results = new HashMap<Long, List<String>>();
    	if (!propertyTypeNameList.isEmpty())
    	{
	        Session session = HibernateSessionFactory.currentSession();
	
	        String hql = "select distinct p.conceptEntityId, p.propertyType from Property p where p.version.id = :versionId " +
	                " and p.conceptEntityId in (:conceptEntityIdList) and p.propertyType in (from PropertyType where name in (:propertyNameList) )";
	
	        Query query = session.createQuery(hql);
	        query.setLong("versionId", versionId);
	        query.setParameterList("propertyNameList", propertyTypeNameList);
	        List<Object[]> list = executeQuery(query, "conceptEntityIdList", conceptEntityIdList);
	
			for (Iterator iter = list.iterator(); iter.hasNext();)
			{
				Object[] object = (Object[]) iter.next();
				Long entityId = (Long) object[0];
				PropertyType propertyType = (PropertyType) object[1];
				
				List<String> propertyTypeNames = results.get(entityId);
				if (propertyTypeNames == null)
				{
					propertyTypeNames = new ArrayList<String>();
					results.put(entityId, propertyTypeNames);
				}
				propertyTypeNames.add(propertyType.getName());
			}
    	}
        return results;
    }
    
    /*
     * get property from the given coded concept that have a given property type and value
     */
    public static Property get(Concept concept, String type, String value)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id = (select max(prop.id) from Property prop "
                + "where prop.conceptEntityId = :id and prop.propertyType = (from PropertyType where name = :type) "
                + "and prop.value = :value)";
        Property property = (Property) session.createQuery(query).setLong("id", concept.getEntityId()).setString("type", type).setString("value", value).uniqueResult();

        return property;
    }
        
    @SuppressWarnings("unchecked")
    public static PropertyType getType(String name)
	{
		Session session = HibernateSessionFactory.currentSession();

		List<PropertyType> propertyTypes = session.createCriteria(PropertyType.class).setCacheable(true).add(Restrictions.eq("name", name)).list();
		if (propertyTypes.size() == 0) 
		{ 
			return null; 
		}

		return propertyTypes.get(0);
	}

    @SuppressWarnings("unchecked")
    public static List<String> getTypesByCodeSystem(String codeSystemName)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "select distinct p.propertyType.name from Property p, Concept c where p.conceptEntityId = c.entityId " +
        "and c.codeSystem = (select cs.id from CodeSystem cs where cs.name = :codeSystemName) order by p.propertyType.name";
        Query query = session.createQuery(hql);
        query.setParameter("codeSystemName", codeSystemName);
        return query.list();
    }

	public static PropertyType createType(PropertyType propertyType)
	{
		Session session = HibernateSessionFactory.currentSession();

		session.save(propertyType);
		
		return propertyType;
	}

    public static void createTypes(List<PropertyType> propertyTypes)
    {
        Session session = HibernateSessionFactory.currentSession();

        for (PropertyType type : propertyTypes)
        {
            session.save(type);
        }
    }

    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        String query = "update Property p set p.version = :"+NEW_VERSION+" where p.version.id = :"+AUTHORING_VERSION+" and "
        + " p.conceptEntityId in (:entityId)";
        
        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }

    /**
     * Get a list of all properties for a given concept
     * @param conceptEntityId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Property> getPreviousVersionProperties(long conceptEntityId, long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id in (select max(prop.id) from Property prop "
                + "where prop.conceptEntityId = :conceptEntityId and prop.version.id < :version group by prop.entityId) ";
        List<Property> list = session.createQuery(query).setLong("conceptEntityId", conceptEntityId)
            .setLong("version", versionId).list();
        return list;
    }

    public static Property getVersioned(Property property)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id = (select max(prop.id) from Property prop "
                + "where prop.entityId = :id and prop.version.id < :version)";
        Property propertyResult = (Property) session.createQuery(query).setLong("id", property.getEntityId())
            .setLong("version", HibernateSessionFactory.AUTHORING_VERSION_ID).uniqueResult();

        return propertyResult;
    }

    public static Property getVersioned(Concept concept, String type, String value)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String hql = "from Property p where p.id = (select max(prop.id) from Property prop "
                + "where prop.conceptEntityId = :id and prop.propertyType = (from PropertyType where name = :type) "
                + "and prop.value = :value and prop.version.id < :version)";

        Query query = session.createQuery(hql);
        
        query.setLong("id", concept.getEntityId());
        query.setString("type", type);
        query.setString("value", value);
        query.setLong("version", HibernateSessionFactory.AUTHORING_VERSION_ID);
        Property property = (Property) query.uniqueResult();
        
        return property;
    }

    public static Property save(Property property)
    {
        HibernateSessionFactory.currentSession().save(property);
        
        return property;
    }

    public static void delete(Long entityId)
    {
        HibernateSessionFactory.currentSession().createQuery("delete from Property where entityId = :entityId and version.id = :versionId").
            setParameter("entityId", entityId).setParameter("versionId", HibernateSessionFactory.AUTHORING_VERSION_ID).executeUpdate();
    }

	/**
	 * @param conceptEntityId
	 * @param includeAuthoring 
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static List<Property> getAllVersions(long conceptEntityId, boolean includeAuthoring)
	{
		Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
		String propertyQuery = "from Property as p where p.conceptEntityId = :conceptEntityId and p.version.id "+operator+" :"+AUTHORING_VERSION+" order by p.entityId, p.id";
         
        Query query = session.createQuery(propertyQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List<Property> properties = query.list();
        
		return properties;
	}

    @SuppressWarnings("unchecked")
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "Select distinct c.entityId from CodedConcept c where c.entityId in (select p.conceptEntityId from Property p where p.version.id = :versionId))";
        List<Long> conceptEntityIds = session.createQuery(query).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }

    /** retrieve the properties from the concept and type
     * 
     * @param entityId
     * @param propertyType
     * @return
     */
	@SuppressWarnings("unchecked")
    public static List<Property> getProperties(long entityId, String propertyType)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id in (select max(prop.id) from Property prop "
                + "where prop.conceptEntityId = :entityId and prop.propertyType.name = :type group by prop.entityId)";
        List<Property> properties = (List<Property>) session.createQuery(query).setLong("entityId", entityId).setString("type", propertyType).list();

        return properties;
    }
    
	/** retrieve the most current property from the entityId and type
     * 
     * @param entityId
     * @param propertyType
     * @return
     */
	public static Property get(long entityId, String propertyType)
	{
        return get(entityId, propertyType, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
	
	/** retrieve the most current property from the entityId and type
     * 
     * @param entityId
     * @param propertyType
     * @param versionId
     * @return
     */
	public static Property get(long entityId, String propertyType, long versionId)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        System.out.println("concept EntityId: " + entityId);
        String query = "from Property p where p.conceptEntityId = :entityId and p.propertyType.name = :type and p.active = 1 " +
        		"and p.id = (select max(prop.id) from Property prop where prop.version.id <= :versionId and prop.entityId = p.entityId)";
        Property property = (Property) session.createQuery(query).setLong("entityId", entityId).setString("type", propertyType).setLong("versionId", versionId).uniqueResult();

        return property;
    }

    @SuppressWarnings("unchecked")
    public static List<Property> getAllDesignationPropertyVersions(long conceptEntityId, boolean includeAuthoring)
    {
        Session session = HibernateSessionFactory.currentSession();
		String operator = (includeAuthoring) ? "<=" : "<";
        String propertyQuery = "from Property p where p.conceptEntityId in (select entityId from Designation des where des.entityId in (select d.targetEntityId from DesignationRelationship d where d.sourceEntityId = :conceptEntityId)) and p.version.id "+operator+" :"+AUTHORING_VERSION+" order by p.entityId, p.id";
         
        Query query = session.createQuery(propertyQuery);
        query.setLong("conceptEntityId", conceptEntityId);
        query.setLong(AUTHORING_VERSION, HibernateSessionFactory.AUTHORING_VERSION_ID);
        List<Property> properties = query.list();
        
        return properties;
    }

    
    @SuppressWarnings("unchecked")
    public static List<Property> getByConceptEntityIdsAndTypes(Collection<Long> conceptEntityIds, List<String> types)
    {
        Session session = HibernateSessionFactory.currentSession();

        // this only returns active entries
        String hql = "from Property p where p.id in (select max(prop.id) from Property prop "
            + "where prop.conceptEntityId in (:conceptEntityIds) and prop.propertyType in (from PropertyType where name in (:types)) "
            + " group by prop.entityId)";
        
        Query query = session.createQuery(hql);

        query.setParameterList("conceptEntityIds", conceptEntityIds);
        query.setParameterList("types", types);
        
        
        return query.list();
        
    }

	@SuppressWarnings("unchecked")
	public static List<Property> getAllVersions(Collection<Long> conceptEntityIds, List<String> propertyTypeNames)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get all versions of all properties for the given list of concept entity ids and a list of property type names
        String query = "from Property p where p.conceptEntityId in (:conceptEntityIds) and p.propertyType.name in (:propertyTypeNames) and p.active = 1 " +
        		" order by p.conceptEntityId, p.version.id desc";
        Query sessionQuery = session.createQuery(query);
        sessionQuery.setParameterList("propertyTypeNames", propertyTypeNames);
        List<Property> properties = (List<Property>) executeQuery(sessionQuery, "conceptEntityIds", conceptEntityIds);
        
        return properties;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Property> get(long versionId, Collection<Long> conceptEntityIds, List<String> propertyTypeNames)
	{
        Session session = HibernateSessionFactory.currentSession();

        // get all properties for the given list of concept entity ids and a list of property type names
        String query = "from Property p where p.conceptEntityId in (:conceptEntityIds) and p.propertyType.name in (:propertyTypeNames) and p.active = 1 " +
                "and p.id = (select max(prop.id) from Property prop where prop.version.id <= :versionId and prop.entityId = p.entityId)" +
        		" order by p.conceptEntityId, p.version.id desc";
        Query sessionQuery = session.createQuery(query);
        sessionQuery.setParameterList("propertyTypeNames", propertyTypeNames).setLong("versionId", versionId);
        List<Property> properties = (List<Property>) executeQuery(sessionQuery, "conceptEntityIds", conceptEntityIds);
        
        return properties;
	}
}
