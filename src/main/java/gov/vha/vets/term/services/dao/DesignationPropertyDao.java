package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DesignationPropertyDao extends EntityBaseDao
{
    public static List<Property> getProperties(List<CodedConcept> conceptList, Version version)
    {
        Session session = HibernateSessionFactory.currentSession();

        String sql = "from Property p where p.id in (select max(prop.id) from Property prop where prop.version.id <= (select v.id from Version v where v.name = :versionName) and p.entityId = prop.entityId) and p.conceptEntityId in (";
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < conceptList.size(); i++)
        {
            buffer.append(":A" + i + ",");
        }
        buffer.replace(buffer.length() - 1, buffer.length(), ")");
        Query query = session.createQuery(sql + buffer);

        query.setString("versionName", version.getName());
        int i = 0;
        for (Iterator<CodedConcept> iter = conceptList.iterator(); iter.hasNext();)
        {
            CodedConcept concept = iter.next();
            query.setLong("A" + i++, concept.getEntityId());
        }
        List<Property> list = query.list();
        return list;
    }

    /*
     * get all properties from the given coded concept
     */
    public static List<Property> getProperties(long conceptEntityId)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.conceptEntityId = :id";
        List<Property> list = session.createQuery(query).setLong("id", conceptEntityId).list();

        return list;
    }

    /*
     * get property from the given coded concept that have a given property type
     * and value
     */
    public static Property getProperty(Designation designation, String type, String value)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id = (select max(prop.id) from Property prop "
                + "where prop.conceptEntityId = :id and prop.propertyType = (from PropertyType where name = :type) " + "and prop.value = :value)";
        Property result = (Property) session.createQuery(query).setLong("id", designation.getEntityId()).setString("type", type).setString("value",
                value).uniqueResult();

        return result;
    }

    public static PropertyType getType(String name)
    {
        Session session = HibernateSessionFactory.currentSession();

        PropertyType propertyType = (PropertyType) session.createCriteria(PropertyType.class).add(Restrictions.eq("name", name)).uniqueResult();

        return propertyType;
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
        String query = "update Property p" + " set p.version = :" + NEW_VERSION + " where p.version.id = :" + AUTHORING_VERSION
                + " and p.conceptEntityId in " + " (select dr.targetEntityId from DesignationRelationship dr where dr.sourceEntityId in (:entityId))";

        return setAuthoringToVersion(query, conceptEntityIdList, version, "entityId");
    }

    public static Property getVersioned(Property property)
    {
        Session session = HibernateSessionFactory.currentSession();

        // get property for the given code
        String query = "from Property p where p.id = (select max(prop.id) from Property prop "
                + "where prop.entityId = :id and prop.version.id < :version)";
        List<Property> list = session.createQuery(query).setLong("id", property.getEntityId()).setLong("version",
                HibernateSessionFactory.AUTHORING_VERSION_ID).list();

        if (list.size() == 0)
        {
            return null;
        }

        return list.get(0);
    }

    public static Property save(Property property)
    {
        HibernateSessionFactory.currentSession().save(property);

        return property;
    }

    public static void delete(Property property)
    {
        HibernateSessionFactory.currentSession().delete(property);
    }

    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        Session session = HibernateSessionFactory.currentSession();

        String query = "Select distinct c.entityId from CodedConcept c where c.entityId in (select dr.sourceEntityId from DesignationRelationship dr, Designation d where dr.targetEntityId = d.entityId and d.entityId in (select p.conceptEntityId from Property p where p.version.id = :versionId))";
        List<Long> conceptEntityIds = session.createQuery(query).setLong("versionId", versionId).list();

        return conceptEntityIds;
    }
}
