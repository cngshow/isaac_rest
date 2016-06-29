package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.DesignationPropertyDao;
import gov.vha.vets.term.services.dao.PropertyDao;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.List;

public class DesignationPropertyDelegate
{

    private static void addEntity(PropertyType propertyType, String value, boolean active, Version version, Long conceptEntityId, Long entityId)
    {
        Property property = new Property();
        property.setPropertyType(propertyType);
        property.setValue(value);
        property.setActive(active);
        property.setVersion(VersionDelegate.getAuthoring());
        property.setConceptEntityId(conceptEntityId);
        if (entityId != null)
        {
            property.setEntityId(entityId);
        }
        PropertyDao.save(property);
        
    }
    
    /*
     * update an existing property (based on code and type) to a new value
     */
    public static void update(CodeSystem codeSystem, String conceptCode, String code, String propertyTypeName,
            String oldValue, String newValue, State state) throws STSNotFoundException
    {
        // get existing property in VTS
        Property existingProperty = getProperty(codeSystem, code, propertyTypeName, oldValue);
        if (existingProperty == null)
        {
            // Versioned property should exist but doesn't - there cannot be an inactive in authoring without an active version
            throw new STSNotFoundException("Designation property cannot be found (CodeSystem: "
                    + codeSystem.getName() + ", code: " + conceptCode + ", designation code: " + code
                    +  " type: " + propertyTypeName + ", value: " + oldValue);
        }
        addEntity(existingProperty.getPropertyType(), newValue, true, VersionDelegate.getAuthoring(), existingProperty.getConceptEntityId(), existingProperty.getEntityId());
        ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);                
    }

    /**
     * Create a designation
     * @param codeSystem
     * @param code
     * @param designationName
     * @param designationType
     * @param propertyType
     * @param value
     * @param state
     * @throws STSNotFoundException
     */
    public static void create(CodeSystem codeSystem, String conceptCode, String code, String propertyTypeName, String value, State state) throws STSNotFoundException
    {
        // get existing property in VTS
        Property existingProperty = getProperty(codeSystem, code, propertyTypeName, value);
        Long conceptEntityId = null;
        Long entityId = null;
        PropertyType propertyType = null;
        if (existingProperty != null)
        {
            propertyType = existingProperty.getPropertyType();
            entityId = existingProperty.getEntityId();
            conceptEntityId = existingProperty.getConceptEntityId();
        }
        else
        {
            Designation designation = DesignationDelegate.get(codeSystem, code);
            if (designation == null)
            {
                throw new STSNotFoundException("Designation with code '" + code + "' not found");
            }
            conceptEntityId = designation.getEntityId();

            propertyType = DesignationPropertyDao.getType(propertyTypeName);

            if (propertyType == null)
            {
                propertyType = createType(new PropertyType(propertyTypeName));
            }
        }
        addEntity(propertyType, value, true, VersionDelegate.getAuthoring(), conceptEntityId, entityId);
        ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);                
    }

    public static void inactivate(CodeSystem codeSystem, String conceptCode, String designationCode, String type, String value, State state) throws STSNotFoundException
    {
    	boolean addNewInactivePropertyEntity = false;
    	
        // get existing property in VTS
        Property existingProperty = getProperty(codeSystem, designationCode, type, value);

        if (existingProperty == null)
        {
            // cannot find the property that should be deleted - not good
            throw new STSNotFoundException("Cannot find designation property with code: " + designationCode + ", type: " + type
                    + ", value: " + value + " to inactivate.");
        }

        if (existingProperty.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            Property versionProperty = DesignationPropertyDao.getVersioned(existingProperty);
            if (versionProperty == null)
            {
                PropertyDao.delete(existingProperty.getEntityId());
            }
            else
            {
            	addNewInactivePropertyEntity = true;
            }
        }
        else
        {
        	addNewInactivePropertyEntity = true;
        }

        if (addNewInactivePropertyEntity == true)
        {
            addEntity(existingProperty.getPropertyType(), value, false, VersionDelegate.getAuthoring(), existingProperty.getConceptEntityId(), existingProperty.getEntityId()); 
            ConceptStateDelegate.createOrUpdate(CodedConceptDelegate.get(codeSystem, conceptCode).getEntityId(), state);                
        }
    }

    /*
     * Find exact property given code, type and value
     */
    public static Property getProperty(CodeSystem codeSystem, String code, String type, String value) throws STSNotFoundException
    {
        Property property = null;
        
        Designation designation = DesignationDelegate.get(codeSystem, code);
        if (designation != null)
        {
            property = DesignationPropertyDao.getProperty(designation, type, value);
        }
        
        return property;
    }

    public static PropertyType getType(String name) throws STSNotFoundException
	{
		return PropertyDao.getType(name);
	}

	public static PropertyType createType(PropertyType propertyType)
	{
		return PropertyDao.createType(propertyType);
	}
    
    public static void createTypes(List<PropertyType> propertyTypes)
    {
        PropertyDao.createTypes(propertyTypes);
    }

    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return DesignationPropertyDao.getConceptEntityIdsByVersionId(versionId);
    }
    
    /**
     * Update all the relationship that belong to the list of concept to be versioned
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return DesignationPropertyDao.setAuthoringToVersion(conceptEntityIdList, version);
    }
    
    public static Property getVersioned(Property property) throws STSNotFoundException
    {
        return PropertyDao.getVersioned(property);
    }

    public static void delete(Property property)
    {
        PropertyDao.delete(property.getEntityId());
    }

    public static void save(Property property)
    {
        PropertyDao.save(property);
    }

    public static void create(Version version, Designation designation, String propertyTypeName, String value)
    {
        Property property = new Property();
        property.setValue(value);
        property.setActive(true);
        property.setVersion(version);
        PropertyType propertyType = DesignationPropertyDao.getType(propertyTypeName);

        if (propertyType == null)
        {
            propertyType = createType(new PropertyType(propertyTypeName));
        }
        
        // create the property
        property.setPropertyType(propertyType);
        property.setConceptEntityId(designation.getEntityId());
        PropertyDao.save(property);
    }

    public static List<Property> getAllVersions(long conceptEntityId, boolean includeAuthoring)
    {
        return PropertyDao.getAllDesignationPropertyVersions(conceptEntityId, includeAuthoring);
    }

    public static List<Property> getProperties(long entityId)
    {
        return PropertyDao.getProperties(entityId);
    }
    /**
     * Check to see if we need to delete a subsetrelationship becuase of a designation or concept deletion
     * @param conceptEntityId
     * @throws STSException 
     */
    public static void removeDesignationProperties(long conceptEntityId) throws STSException
    {
        List<Property> properties = DesignationPropertyDao.getProperties(conceptEntityId);
        for (Property property : properties)
        {
            PropertyDelegate.delete(property);
        }
    }
}
