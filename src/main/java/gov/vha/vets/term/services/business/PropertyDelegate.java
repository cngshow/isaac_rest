package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.DesignationPropertyDao;
import gov.vha.vets.term.services.dao.PropertyDao;
import gov.vha.vets.term.services.dto.change.PropertyChangeDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyDelegate
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
    public static void createVHAT(CodeSystem codeSystem, String code, String type, String value, State state) throws STSNotFoundException
    {
        Property existingProperty = null;
        PropertyType propertyType = null;
        Long entityId = null;
        
        // get existing property in VTS
        CodedConcept codedConcept = CodedConceptDelegate.get(codeSystem, code);
        existingProperty = PropertyDelegate.get(codedConcept, type, value);
        if (existingProperty != null)
        {
            // create the new property using the existing property entity id
            propertyType = existingProperty.getPropertyType();
            entityId = existingProperty.getEntityId();
        }
        else
        {
            propertyType = PropertyDao.getType(type);
            
            if (propertyType == null)
            {
                propertyType = createType(new PropertyType(type));
            }
        }
        addEntity(propertyType, value, true, VersionDelegate.getAuthoring(), codedConcept.getEntityId(), entityId);
        ConceptStateDelegate.createOrUpdate(codedConcept.getEntityId(), state);
    }
    
    public static void inactivateVHAT(CodeSystem codeSystem, String code, String type, String value, State state) 
    		throws STSNotFoundException
    {
        // get existing property in VTS
        Property existingProperty = PropertyDelegate.get(codeSystem, code, type, value);

    	inactivateVHAT(existingProperty, CodedConceptDelegate.get(codeSystem, code).getEntityId(), code, type, value, state);
    }
    
	public static void inactivateVHAT(Property existingProperty, long entityId, String code, String type, String value, State state)
			throws STSNotFoundException
	{
		boolean addNewInactivePropertyEntity = false;
    	
        if (existingProperty == null)
        {
            // cannot find the property that should be deleted - not good
            throw new STSNotFoundException("Cannot find concept property with code: " + code + ", type: " + type
                    + ", value: " + value + " to inactivate.");
        }

        if (existingProperty.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            Property versionProperty = PropertyDao.getVersioned(existingProperty);
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
            ConceptStateDelegate.createOrUpdate(entityId, state);                
        }
	}

    /*
     * update an existing property (based on code and type) to a new value
     */
    public static void updateVHAT(CodeSystem codeSystem, String code, String type, String oldValue, String newValue, State state) throws STSNotFoundException
    {
        // get existing property in VTS
        Property existingProperty = PropertyDelegate.get(codeSystem, code, type, oldValue);
        if (existingProperty == null)
        {
            throw new STSNotFoundException("Property cannot be found (CodeSystem: "
                    + codeSystem.getName() + ", code: " + code + ", type: " + type
                    + ", value: " + oldValue);
        }
        
        // create the new property using the existing property entity id
        addEntity(existingProperty.getPropertyType(), newValue, true, VersionDelegate.getAuthoring(), existingProperty.getConceptEntityId(), existingProperty.getEntityId());
        ConceptStateDelegate.createOrUpdate(existingProperty.getConceptEntityId(), state);
    }

    public static Property create(Version version, long conceptEntityId, PropertyType type, String value) throws STSNotFoundException 
    {
        // create the new property using the existing property entity id
        Property property = new Property();
        property.setPropertyType(type);
        property.setValue(value);
        property.setVersion(version);
        property.setActive(true);
        property.setConceptEntityId(conceptEntityId);
        PropertyDao.save(property);
        return property;
    }

    /**
     * call the above function with a property type instead of a typeName
     * @param version
     * @param conceptEntityId
     * @param typeName
     * @param value
     * @throws STSNotFoundException
     */
    public static Property create(Version version, long conceptEntityId, String typeName, String value) throws STSNotFoundException
    {
    	PropertyType propertyType = getType(typeName);
    	if (propertyType == null)
        {
	        propertyType = createType(typeName);
        }
    	return create(version, conceptEntityId, propertyType, value);
    }
    


    public static PropertyType createType(String propertyTypeName)
    {
        return PropertyDelegate.createType(new PropertyType(propertyTypeName));
    }
    
    public static PropertyType createType(PropertyType propertyType)
	{
		return PropertyDao.createType(propertyType);
		
	}

	public static void createTypes(List<PropertyType> propertyTypes)
    {
        PropertyDao.createTypes(propertyTypes);
    }

    public static void delete(Property property)
    {
        PropertyDao.delete(property.getEntityId());
    }

    /*
     * Find exact property given codedConcept, type and value
     */
    public static Property get(Concept concept, String type, String value)
    {
        return PropertyDao.get(concept, type, value);
    }

    /*
     * Find exact property given codeSystem, code, type and value
     */
    public static Property get(CodeSystem codeSystem, String code, String type, String value)
    {
        Property property = null;

        CodedConcept codedConcept = CodedConceptDelegate.get(codeSystem, code);
        if (codedConcept != null)
        {
            property = PropertyDao.get(codedConcept, type, value);
        }
            
        return property;
    }

    /**
	 * @param conceptEntityId
     * @param includeAuthoring 
	 * @return
	 */
	public static List<Property> getAllVersions(long conceptEntityId, boolean includeAuthoring)
	{
		return PropertyDao.getAllVersions(conceptEntityId, includeAuthoring);
	}

	/**
	 * @param conceptEntityIds
	 * @param versionId
	 * @return
	 */
    public static List<Property> getProperties(List<Long> conceptEntityIds, long versionId)
    {
        return PropertyDao.getProperties(conceptEntityIds, versionId);
    }
    
    /** retrieve the most current property for a conceptEntityId by its type
     * 
     * @param entityId
     * @param propertyType
     * @return
     */
	public static Property get(long entityId, String propertyType)
	{
		return PropertyDao.get(entityId, propertyType);
	}

    /** retrieve the most current property for a conceptEntityId by its type
     * 
     * @param entityId
     * @param propertyType
     * @param versionId
     * @return
     */
	public static Property get(long entityId, String propertyType, long versionId)
	{
		return PropertyDao.get(entityId, propertyType, versionId);
	}

    /**
     * Get the properties of a given type for a conceptEntityId 
     * @param conceptEntityId
     * @param propertyTypeNameList
     * @return
     */
    public static List<Property> getProperties(Collection<Long> conceptEntityIdList, long versionId, List<String> propertyTypeNameList)
    {
        return PropertyDao.getProperties(conceptEntityIdList, versionId, propertyTypeNameList);
    }

    /**
     * Get the properties for a given version id 
     * @param versionId
     * @return
     */
    public static List<Property> getPropertiesByVersionId(long versionId, boolean isFullVersion)
    {
    	return PropertyDao.getPropertiesByVersionId(versionId, isFullVersion);
    }

    /**
     * Get all concept for properties that have changed for particular property
     * @param version
     * @return
     */
    public static List<Long> getConceptEntityIdsByVersionId(long versionId)
    {
        return PropertyDao.getConceptEntityIdsByVersionId(versionId);
    }
    /**
     * Get all the properties that have changed in a particular version filtered by property type
     * @param conceptEntityId
     * @param version
     * @param propertyTypeNameList
     * @return
     */
    public static Map<Long, List<String>> getChangedPropertyTypes(List<Long> conceptEntityIdList, long versionId, List<String> propertyTypeNameList)
    {
        return PropertyDao.getChangedPropertyTypes(conceptEntityIdList, versionId, propertyTypeNameList);
    }
    
    public static List<PropertyChangeDTO> getPropertyChanges(long conceptEntityId)
    {
        return getPropertyChanges(conceptEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }

    public static List<PropertyChangeDTO> getPropertyChanges(long conceptEntityId, long versionId)
    {
        List<Property> properties = PropertyDao.getProperties(conceptEntityId, versionId);
        List<Property> previousProperties = PropertyDao.getPreviousVersionProperties(conceptEntityId, versionId);
        Map<Long, PropertyChangeDTO> map = new HashMap<Long, PropertyChangeDTO>();
        for (Property property : properties)
        {
            if (property.getVersion().getId() == versionId)
            {
                PropertyChangeDTO change = new PropertyChangeDTO();
                change.setVersionId(versionId);
                change.setRecent(property);
                map.put(property.getEntityId(), change);
            }
        }
        for (Property property : previousProperties)
        {
            PropertyChangeDTO change = map.get(property.getEntityId());
            if (change == null)
            {
                if (property.getActive() == true)
                {
                    change = new PropertyChangeDTO();
                    change.setVersionId(versionId);
                    change.setRecent(property);
                    map.put(property.getEntityId(), change);
                }
            }
            else
            {
                change.setPrevious(property);
            }
        }
        List<PropertyChangeDTO> list = new ArrayList<PropertyChangeDTO>();
        list.addAll(map.values());
        return list;
    }

	public static PropertyType getType(String name) throws STSNotFoundException
	{
		return PropertyDao.getType(name);
	}
    
    public static Property getVersioned(Property property)
    {
        return PropertyDao.getVersioned(property);
    }
    
    /**
     * Check to see if we need to delete a subset relationship because of a designation or concept deletion
     * @param conceptEntityId
     */
    public static void removeProperties(Long conceptEntityId)
    {
        List<Property> properties = DesignationPropertyDao.getProperties(conceptEntityId);
        for (Property property : properties)
        {
            if (property.getVersion().getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
            {
                PropertyDelegate.delete(property);
            }
        }
    }
    
    public static void save(Property property)
    {
        PropertyDao.save(property);
    }

    /**
     * Update all the relationship that belong to the list of concept to be versioned
     * @param conceptList
     * @param version
     * @return
     */
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return PropertyDao.setAuthoringToVersion(conceptEntityIdList, version);
    }


	/*
     * update an existing property (based on code and type) to a new value
     */
    public static Property updateSDO(Version version, Property property, String newValue, boolean active) throws STSException
    {
        Property newProperty = new Property();
        newProperty.setEntityId(property.getEntityId());
        newProperty.setConceptEntityId(property.getConceptEntityId());
        newProperty.setPropertyType(property.getPropertyType());
        newProperty.setValue(newValue);
        newProperty.setVersion(version);
        newProperty.setActive(active);
        return PropertyDao.save(newProperty);
	}
    
    /** retrieve a property for a concept by its type
     * 
     * @param designationEntityId
     * @param string
     * @return
     */
	public static List<Property> getProperties(long entityId, String propertyType)
	{
		return PropertyDao.getProperties( entityId, propertyType);
	}
	
    public static List<Property> getByConceptEntityIdsAndTypes(Collection<Long> propertyConceptIds, List<String> types)
    {
        return PropertyDao.getByConceptEntityIdsAndTypes(propertyConceptIds, types);
    }

    public static List<Property> getProperties(long entityId)
    {
        return PropertyDao.getProperties(entityId);
    }

    public static List<Property> getProperties(long entityId, Version version)
    {
        return PropertyDao.getProperties(entityId, version);
    }

    public static void remove(Property property) throws STSException
    {
        Property versionedProperty = PropertyDao.getVersioned(property);
        if (versionedProperty != null)
        {
            throw new STSException("Cannot remove property type: "+property.getPropertyType().getName()+" because it is in version: "+property.getVersion().getName());
        }
        PropertyDao.delete(property.getEntityId());
    }

	public static Map<Long, Collection<Property>> getAllVersions(Collection<Long> conceptEntityIds, List<String> propertyTypeNames)
	{
		HashMap<Long, Collection<Property>> conceptPropertiesHashMap = new HashMap<Long, Collection<Property>>();
		if (!conceptEntityIds.isEmpty())
		{
    		List<Property> properties = PropertyDao.getAllVersions(conceptEntityIds, propertyTypeNames);
    		for (Property property : properties)
    		{
    		    Collection<Property> conceptProperties = conceptPropertiesHashMap.get(property.getConceptEntityId());
    		    if (conceptProperties == null)
    		    {
    		        conceptProperties = new ArrayList<Property>();
    	            conceptPropertiesHashMap.put(property.getConceptEntityId(), conceptProperties);
    		    }
                conceptProperties.add(property);
    		}
		}
		return conceptPropertiesHashMap;
	}
	
	public static Map<Long, Collection<Property>> get(long versionId, Collection<Long> conceptEntityIds, List<String> propertyTypeNames)
	{
		HashMap<Long, Collection<Property>> conceptPropertiesHashMap = new HashMap<Long, Collection<Property>>();
		if (!conceptEntityIds.isEmpty())
		{
    		List<Property> properties = PropertyDao.get(versionId, conceptEntityIds, propertyTypeNames);
    		for (Property property : properties)
    		{
    		    Collection<Property> conceptProperties = conceptPropertiesHashMap.get(property.getConceptEntityId());
    		    if (conceptProperties == null)
    		    {
    		        conceptProperties = new ArrayList<Property>();
    	            conceptPropertiesHashMap.put(property.getConceptEntityId(), conceptProperties);
    		    }
                conceptProperties.add(property);
    		}
		}
		return conceptPropertiesHashMap;
	}
	
    public static List<String> getTypesByCodeSystem(String codeSystemName)
    {
        return PropertyDao.getTypesByCodeSystem(codeSystemName);
    }
    
    public static List<Property> getPropertiesByTypeName(String type){
    	return PropertyDao.getPropertiesByTypeName(type);
    }
}
