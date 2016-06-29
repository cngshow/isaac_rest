package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ServicesTEdDao;
import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.dto.MapEntryDetailDTO;
import gov.vha.vets.term.services.dto.MapEntryDisplayDTO;
import gov.vha.vets.term.services.dto.MapSetDetailDTO;
import gov.vha.vets.term.services.dto.SDOResultDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.model.Vuid;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.GenericJDBCException;

public class ServicesTEdDelegate
{
	public static List<MapSetDetailDTO> getAllMapSetVersions() throws STSException
	{
		List <MapSetDetailDTO> mapSetDetails = MapSetDelegate.getAllVersions();
		State initialState = StateDelegate.getByType(State.INITIAL);
		HashMap<Long, State> stateMap = new HashMap<Long, State>();
		long currentVuid = 0L;
		String mostRecentPreferredName = "";

		// get all entity ids of the map sets
		HashSet<Long> mapSetEntityIdSet = new HashSet<Long>();
		for (MapSetDetailDTO mapSetDetailDTO : mapSetDetails)
		{
			mapSetEntityIdSet.add(mapSetDetailDTO.getMapSetEntityId());
		}
		Collection<Long> mapSetEntityIds = new ArrayList<Long>(mapSetEntityIdSet);
		
		// create list of property type names for the map sets
		List<String> mapSetPropertyTypeNames = new ArrayList<String>();
		mapSetPropertyTypeNames.add(PropertyType.DESCRIPTION);
		mapSetPropertyTypeNames.add(PropertyType.TEXT_DEFINITION);
		
		// get hash map of all properties for all map sets and for specific property types
		Map<Long, Collection<Property>> mapSetPropertiesMap = new HashMap<Long, Collection<Property>>();
		if (mapSetEntityIds.size() > 0)
		{
			mapSetPropertiesMap = PropertyDelegate.getAllVersions(mapSetEntityIds, mapSetPropertyTypeNames);
		}

		for (MapSetDetailDTO mapSetDetailDTO : mapSetDetails)
		{
			if (mapSetDetailDTO.getMapSetVuid() != currentVuid)
			{
				currentVuid = mapSetDetailDTO.getMapSetVuid();
				mostRecentPreferredName = mapSetDetailDTO.getPreferredName();
			}
			mapSetDetailDTO.setMostRecentPreferredName(mostRecentPreferredName);

			// get description property 
			Property descriptionProperty = getMapSetProperty(mapSetPropertiesMap, mapSetDetailDTO, PropertyType.DESCRIPTION);
			if (descriptionProperty != null)
			{
				mapSetDetailDTO.setDescription(descriptionProperty.getValue());
			}

			// get text definition property 
			Property textDefinitionProperty = getMapSetProperty(mapSetPropertiesMap, mapSetDetailDTO, PropertyType.TEXT_DEFINITION);
			if (textDefinitionProperty != null)
			{
				mapSetDetailDTO.setTextDefinition(textDefinitionProperty.getValue());
			}

			// get state for the MapSetDetailDTO
			if (mapSetDetailDTO.getActualVersionId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
			{
				if (mapSetDetailDTO.getMapSetStateId() != 0)
				{
					long stateId = mapSetDetailDTO.getMapSetStateId();
					State state = stateMap.get(stateId);
					if (state == null)
					{
						state = StateDelegate.getById(stateId);
						stateMap.put(stateId, state);
					}
					mapSetDetailDTO.setMapSetStateName(state.getName());
					mapSetDetailDTO.setMapSetStateType(state.getType());
				}
				else
				{
					mapSetDetailDTO.setMapSetStateName(initialState.getName());
					mapSetDetailDTO.setMapSetStateType(initialState.getType());
				}
			}
		}

		Collections.sort(mapSetDetails);

		return mapSetDetails;
	}
	
	private static Property getMapSetProperty(Map<Long, Collection<Property>> mapSetPropertiesMap, MapSetDetailDTO mapSetDetailDTO, String propertyTypeName)
	{
		Property property = null;

		Collection<Property> mapSetProperties = mapSetPropertiesMap.get(mapSetDetailDTO.getMapSetEntityId());
		if (mapSetProperties != null)
		{
			for (Property property2 : mapSetProperties)
			{
				if (property2.getVersion().getId() > mapSetDetailDTO.getActualVersionId())
				{
					continue;
				}
				if (property2.getPropertyType().getName().equals(propertyTypeName) == false)
				{
					continue;
				}
				
				property = property2;
				break;
			}
		}

		return property;
	}
	
	public static List<SDOResultDTO> searchForDesignation(String searchString, long codeSystemId, long versionId,
			int maxSearchResults, String preferredDesignationType, boolean searchDesignation) throws STSException
	{
        try
        {
            HibernateSessionFactory.currentSession().clear();

            return ServicesTEdDao.searchForDesignation(searchString, codeSystemId, versionId, preferredDesignationType, maxSearchResults, searchDesignation );
        }
        catch (GenericJDBCException e)
        {
        	throw e;
        }
	}
	
	public static MapEntryDTO getMapEntry(Long mapEntryEntityid, Long mapSetRelationshipEntityId)
	{
	    return MapEntryDelegate.getMapEntry(mapEntryEntityid, mapSetRelationshipEntityId);
	}

	public static List<CodeSystem> getCodeSystems()
	{
		return CodeSystemDelegate.getCodeSystems();
	}

	public static List<State> getStates() throws STSException
	{
		return StateDelegate.getStates();
	}

	public static List<Version> getFinalizedVersions(String codeSystemName)
	{
		return VersionDelegate.getFinalizedVersions(CodeSystemDelegate.get(codeSystemName));
	}

	public static MapSetDetailDTO saveMapSet(MapSetDetailDTO mapSetDetail) throws Exception
	{
		Version authoringVersion = VersionDelegate.getAuthoring();
		MapSet mapSet = null;
		
        
		if (mapSetDetail.getMapSetVuid() == null) //its new
		{
            VuidDelegate vuidDelegate = new VuidDelegate();
			Vuid vuid = vuidDelegate.createVuidRange(3, "TED", "Codesystem: "+authoringVersion.getCodeSystem().getName()+" creating a new mapset");
	        long startingVuid = vuid.getStartVuid();
			mapSet = MapSetDelegate.createVHAT(mapSetDetail.getPreferredName(), ""+startingVuid, startingVuid++, mapSetDetail.getMapSetActive()==1?true:false, 
					mapSetDetail.getSourceCodeSystemName(), mapSetDetail.getSourceVersionName(), mapSetDetail.getTargetCodeSystemName(), mapSetDetail.getTargetVersionName(), null);
			
	        initializeWorkflowState(mapSet.getEntityId());
			Designation preferredDesignation = DesignationDelegate.create(authoringVersion, mapSetDetail.getPreferredName(), startingVuid, ""+startingVuid++, DesignationType.PREFERRED_NAME, true);
			DesignationRelationshipDelegate.create(mapSet.getEntityId(), preferredDesignation);
			
			DateFormat format = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
			String fullySpecifiedName = mapSetDetail.getSourceCodeSystemName() + " " + mapSetDetail.getSourceVersionName()
				+ " to " + mapSetDetail.getTargetCodeSystemName()+" "+mapSetDetail.getTargetVersionName()+ " " + format.format(new Date());
			Designation fullySpecifiedDesignation = DesignationDelegate.create(authoringVersion, fullySpecifiedName, startingVuid, ""+startingVuid, DesignationType.FULLY_SPECIFIED_NAME, true);
			DesignationRelationshipDelegate.create(mapSet.getEntityId(), fullySpecifiedDesignation);
			
			mapSetDetail.setFullySpecifiedName(fullySpecifiedName);
			updateProperty(mapSet.getEntityId(), PropertyType.TEXT_DEFINITION, mapSetDetail.getTextDefinition(), authoringVersion);
			updateProperty(mapSet.getEntityId(), PropertyType.DESCRIPTION, mapSetDetail.getDescription(), authoringVersion);
		} 
		else // Editing
		{
		   ConceptState conceptState = ServicesTEdDelegate.getConceptState(mapSetDetail.getMapSetEntityId());

		   // only do the update if the mapset is in the correct state
		   if (!conceptState.getState().getType().equals(State.INITIAL))
		   {
			   throw new STSException("Cannot save a map set that is in state: "+conceptState.getState().getName());
		   }
				   
			mapSet = MapSetDelegate.get(mapSetDetail.getMapSetEntityId());
			boolean isMapSetActive = mapSetDetail.getMapSetActive()==1 ? true : false;
			if (mapSet.getActive() != isMapSetActive)
			{
				mapSet = MapSetDelegate.updateVHAT(mapSet, isMapSetActive);
			}
			updateDesignation(DesignationType.PREFERRED_NAME, mapSetDetail.getPreferredName(), mapSet.getEntityId(), authoringVersion);
			updateDesignation(DesignationType.FULLY_SPECIFIED_NAME, mapSetDetail.getFullySpecifiedName(), mapSet.getEntityId(), authoringVersion);
			updateProperty(mapSet.getEntityId(), PropertyType.TEXT_DEFINITION, mapSetDetail.getTextDefinition(), authoringVersion);
			updateProperty(mapSet.getEntityId(), PropertyType.DESCRIPTION, mapSetDetail.getDescription(), authoringVersion);
		}
		Version sourceVersion = VersionDelegate.get(mapSetDetail.getSourceCodeSystemName(), mapSetDetail.getSourceVersionName());
		Version targetVersion = VersionDelegate.get(mapSetDetail.getTargetCodeSystemName(), mapSetDetail.getTargetVersionName());
		mapSetDetail.setSourceCodeSystemId(sourceVersion.getCodeSystem().getId());
		mapSetDetail.setTargetCodeSystemId(targetVersion.getCodeSystem().getId());
		mapSetDetail.setSourceVersionId(sourceVersion.getId());
		mapSetDetail.setTargetVersionId(targetVersion.getId());
		
		mapSetDetail.setMapSetName(mapSet.getName());
		mapSetDetail.setActualVersionId(authoringVersion.getId());
		mapSetDetail.setActualVersionName(authoringVersion.getName());
		mapSetDetail.setMapSetVuid(mapSet.getVuid());
		mapSetDetail.setMapSetEntityId(mapSet.getEntityId());
		mapSetDetail.setMapSetId(mapSet.getId());

		return mapSetDetail;
	}

	private static void updateDesignation(String typeName, String name, long mapSetEntityId, Version authoringVersion) throws Exception
	{
		Designation designation = DesignationDelegate.get(mapSetEntityId, typeName);
		if (designation == null || !name.equals(designation.getName()))
		{
			if(designation != null)
			{
				DesignationDelegate.inactivate(designation);
			}
			VuidDelegate vuidDelegate = new VuidDelegate();
			Vuid vuid = vuidDelegate.createVuidRange(1, "TED", "Codesystem: " + authoringVersion.getCodeSystem().getName()	+ " creating a new mapset "+typeName);
			long startingVuid = vuid.getStartVuid();
			Designation newDesignation = DesignationDelegate.create(authoringVersion, name, startingVuid, "" + startingVuid, typeName, true);
			DesignationRelationshipDelegate.create(mapSetEntityId, newDesignation);
		}
	}
	private static void updateProperty(long mapSetEntityId, String typeName, String newValue, Version authoringVersion) throws STSException
	{
		Property property = PropertyDelegate.get(mapSetEntityId, typeName);
		if (property == null)
		{
			if(newValue != null && !newValue.isEmpty())
			{
				PropertyType propertyType = getOrCreatePropertyType(typeName);
				PropertyDelegate.create(authoringVersion, mapSetEntityId, propertyType, newValue);
			}
		}
		else
		{
			boolean hasChanged = newValue != property.getValue() && !newValue.equals(property.getValue());
			if (hasChanged) 
			{
				PropertyDelegate.inactivateVHAT(property, mapSetEntityId, null, property.getPropertyType().getName(), property.getValue(), 
						StateDelegate.getByType(State.INITIAL));
				if (!newValue.equals(""))
				{
					property = PropertyDelegate.create(authoringVersion, mapSetEntityId, typeName, newValue);
					property.setValue(newValue);
					PropertyDelegate.save(property);
				}
			}
		}
	}
	
	private static PropertyType getOrCreatePropertyType(String name) throws STSNotFoundException
	{
		PropertyType propertyType = PropertyDelegate.getType(name);
		if (propertyType == null)
		{
			propertyType = PropertyDelegate.createType(name);
		}
		
		return propertyType;
	}

	public static List<MapEntryDetailDTO> getMapEntries(MapSet mapSet, String code)
    {
        return MapEntryDelegate.getEntries(mapSet, code);
    }

	public static List<MapEntryDetailDTO> getMapEntries(MapSet mapSet, Long mapEntryEntityId)
    {
        return MapEntryDelegate.getEntries(mapSet, mapEntryEntityId);
    }
	
	public static List<MapEntryDisplayDTO> getEntries(long mapSetEntityId, long actualVersionId,
	        int startRow, int rowsPerPage, String sortOrder, String searchText, String sortColumnName)
	{
		return MapEntryDisplayDelegate.getEntries(mapSetEntityId, actualVersionId, startRow, rowsPerPage, sortOrder, searchText, sortColumnName);
	}

	public static long getEntriesCount(long mapSetEntityId, long actualVersionId, String sortOrder, String searchText, String sortColumnName)
	{
		return MapEntryDisplayDelegate.getEntriesCount(mapSetEntityId, actualVersionId, sortOrder, searchText, sortColumnName);
	}
	
	public static void updateMapEntryCache(long mapSetEntityId, long versionId)
	{
		MapEntryDisplayDelegate.updateMapEntryCache(mapSetEntityId, versionId);
	}

	public static List<MapEntryDetailDTO> getEntries(long mapSetEntityId, String sourceCodeSystemName, String targetCodeSystemName, String sourceVersionName,
			String targetVersionName, long versionId)
	{
		return MapEntryDelegate.getEntries(mapSetEntityId, sourceCodeSystemName, targetCodeSystemName, sourceVersionName, targetVersionName, versionId);
	}

    public static List<MapEntryDTO> getEntries(long mapSetEntityId, Long versionId, boolean includeInactives)
    {
        return MapEntryDelegate.getEntries(mapSetEntityId, versionId, includeInactives);
    }
    
	public static List<MapEntryDetailDTO> getEntries(MapSet mapSet, Long mapEntryEntityId)
    {
        return MapEntryDelegate.getEntries(mapSet, mapEntryEntityId);
    }
	
	public static MapEntry createMapEntryVHAT(Version version, long mapSetEntityId,String code, long vuid, boolean active, 
    		String sourceConceptCode, String targetConceptCode, int sequence, Long grouping) throws STSException
	{
	    return MapEntryDelegate.createVHATWithValidation(version, mapSetEntityId, code, vuid, active, sourceConceptCode, targetConceptCode, sequence, grouping);
	}
	
	public static boolean isPreferredDesignationUnique(String mapSetName)
	{
		return MapSetDelegate.isPreferredDesignationUnique(mapSetName);
	}

	public static void updateWorkflowState(long mapSetEntityId, String stateName) throws STSNotFoundException, STSException
	{
		State state = StateDelegate.get(stateName);
		ConceptStateDelegate.createOrUpdate(mapSetEntityId, state);
	}


	/**
	 * Update VHAT mappings (modifiable: TargetCode, Active, Sequence) 
	 * @param mapSetEntityId
	 * @param mapEntry
	 * @param targetConceptCode
	 * @param active
	 * @param sequence
	 * @param grouping
	 */
    public static void updateVHAT(long mapSetEntityId, MapEntry mapEntry, String targetConceptCode, boolean active, int sequence, Long grouping)
    {
        MapEntryDelegate.updateVHAT(mapSetEntityId, mapEntry, targetConceptCode, active, sequence, grouping);
    }

    public static void inactivate(Long mapSetRelationshipEntityId) throws STSException
    {
        MapEntryDelegate.inactivate(mapSetRelationshipEntityId);        
    }
    
	public static void initializeWorkflowState(long mapSetEntityId) throws STSException
    {
        State state = StateDelegate.getByType(State.INITIAL);
        ConceptStateDelegate.createOrUpdate(mapSetEntityId, state);
    }

    public static ConceptState getConceptState(Long conceptEntityId)
    {
        return ConceptStateDelegate.get(conceptEntityId);
    }
}
