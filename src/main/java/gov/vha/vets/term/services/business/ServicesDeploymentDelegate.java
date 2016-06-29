package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ServicesDeploymentDao;
import gov.vha.vets.term.services.dto.ConceptDesignationDTO;
import gov.vha.vets.term.services.dto.ConceptEntityDTO;
import gov.vha.vets.term.services.dto.DomainDTO;
import gov.vha.vets.term.services.dto.MapEntryDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDTO;
import gov.vha.vets.term.services.dto.RegionChecksumDTO;
import gov.vha.vets.term.services.dto.RegionDTO;
import gov.vha.vets.term.services.dto.RegionEntityDTO;
import gov.vha.vets.term.services.dto.SubsetDetailDTO;
import gov.vha.vets.term.services.dto.SubsetPublishDetailDTO;
import gov.vha.vets.term.services.dto.config.BaseConfig;
import gov.vha.vets.term.services.dto.config.CodeSystemConfig;
import gov.vha.vets.term.services.dto.config.DependentSubsetRule;
import gov.vha.vets.term.services.dto.config.DesignationConfig;
import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.config.PropertyConfig;
import gov.vha.vets.term.services.dto.config.RegionConfig;
import gov.vha.vets.term.services.dto.config.RelationshipConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.dto.delta.DiscoveryDeltaDTO;
import gov.vha.vets.term.services.dto.delta.DiscoveryMappingResultsDTO;
import gov.vha.vets.term.services.dto.delta.DiscoveryResultsDTO;
import gov.vha.vets.term.services.dto.publish.NameValueDTO;
import gov.vha.vets.term.services.dto.publish.PublishConceptDTO;
import gov.vha.vets.term.services.dto.publish.PublishRegionDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSInvalidValueException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.ChecksumCalculator;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class ServicesDeploymentDelegate
{
	private static Logger log = Logger.getLogger(ServicesDeploymentDelegate.class.getPackage().getName());
    
    public static final String IN_TEST = State.IN_TEST;

    public static final String READY_TO_TEST = State.READY_TO_TEST;
    
    public static final int CONCEPT_LEVEL = 0;
    
    public static final String TERM_TYPE = "T";
	public static final String PROPERTY_TYPE = "P";
	public static final String RELATIONSHIP_TYPE = "R";  
    public static final String MAPPING_TYPE = "M";
    public static final String TERM_FIELD_NAME = "Term";
    
    public enum DataChangeType
    {
        NEW, DEACTIVATED, REACTIVATED, CHANGED
    }

    /**
     * Return all the ready to test concepts in the VTS
     * 
     * @return gov.vha.vets.term.services.dto.DomainDTO
     * @throws STSException
     */
    public static List<DomainDTO> getConceptsReadyToTest() throws STSException
    {
        Map<String, List<ConceptEntityDTO>> map = ServicesDeploymentDao.getReadyToTest();
        List<DomainConfig> domainConfigList = TerminologyConfigDelegate.getDomains();
        List<DomainDTO> domainDTOList = new ArrayList<DomainDTO>();

        // loop over all the domains
        for (DomainConfig domainConfig : domainConfigList)
        {
            String domainConfigName = domainConfig.getName();
            List<SubsetConfig> subsetConfigList = domainConfig.getSubsets();
            List<RegionDTO> regionDTOList = new ArrayList<RegionDTO>();
            
            // loop over all the subset for a given domain
            for (SubsetConfig subsetConfig : subsetConfigList)
            {
                String subsetConfigName = subsetConfig.getName();
                List<ConceptEntityDTO> conceptEntities = map.get(subsetConfigName);
                if (conceptEntities != null && conceptEntities.size() > 0)
                {
                    RegionDTO regionDTO = new RegionDTO(subsetConfigName, conceptEntities, subsetConfig.isActive());
                    regionDTOList.add(regionDTO);
                    // remove the subset so we know we have processed it
                    map.remove(subsetConfigName);
                }
            }
            // if we have regions for this domain then add it
            if (regionDTOList.size() > 0)
            {
                DomainDTO domainDTO = new DomainDTO(domainConfigName, regionDTOList);
                domainDTOList.add(domainDTO);
            }
        }
        // now let's find out what left and add it as a NON_DOMAIN
        List<RegionDTO> nonDomainRegionDTOList = new ArrayList<RegionDTO>();
        List<RegionDTO> mapSetList = new ArrayList<RegionDTO>();
        
        for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
        {
            String subsetName = (String) iter.next();
            List<ConceptEntityDTO> conceptEntities = map.get(subsetName);

            if (conceptEntities != null && conceptEntities.size() > 0)
            {
                RegionDTO regionDTO = new RegionDTO(subsetName, conceptEntities, false);
                if(DomainDTO.MAP_SETS.equals(subsetName))
                {
                	mapSetList.add(regionDTO);
                }
                else
                {
                	nonDomainRegionDTOList.add(regionDTO);
                }
            }
        }
        if (mapSetList.size() > 0)
        {
            DomainDTO domainDTO = new DomainDTO(DomainDTO.MAP_SETS, mapSetList);
            domainDTOList.add(domainDTO);
        }
        // add any non-domain regions to the non-domain
        if (nonDomainRegionDTOList.size() > 0)
        {
            DomainDTO domainDTO = new DomainDTO(DomainDTO.NON_DOMAIN, nonDomainRegionDTOList);
            domainDTOList.add(domainDTO);
        }
        
        Collections.sort(domainDTOList);
		
        return domainDTOList;
    }

    /**
     * Cases: New - Concept has been added De-activated - designation has been
     * de-activated in subset Update - concept or any entity has been modified
     * Re-activated - designation has been re-activated in the subset Nothing -
     * there was a change but it is not in the config for this subset
     * 
     * @param conceptEntityIds
     * @return List<PublishRegionDTO>
     * @throws STSException
     */
    public static List<PublishRegionDTO> getChangeSetDetail(Collection<Long> conceptEntityIds) throws STSException
    {
        List<DomainConfig> domains = TerminologyConfigDelegate.getDomains();

        // final result of all subset data
        HashMap<String, List<PublishConceptDTO>> publishMap = new HashMap<String, List<PublishConceptDTO>>();
        Set<String> alreadyProcessed = new HashSet<String>();

        // Compile a list of all valid subsets
        Map<String, SubsetConfig> validSubsets = getValidSubsets(domains);

        processPublishDataForConceptEntity(HibernateSessionFactory.AUTHORING_VERSION_ID, publishMap, validSubsets, conceptEntityIds, alreadyProcessed);

        return buildOrderedRegions(domains, publishMap);
    }

    /**
     * @param versionId
     * @return List<PublishRegionDTO>
     * @throws STSException
     */
    public static PublishRegionDTO getChangeSetDetailForCodeSystem(Version version, List<CodedConcept> codedConcepts) throws STSException
    {
    	CodeSystemConfig codeSystemConfig = TerminologyConfigDelegate.getCodeSystem(version.getCodeSystem().getVuid());
    	if (codeSystemConfig == null)
		{
			throw new STSException("Invalid Code System VUID");
		}

        List<Property> propertyList = PropertyDelegate.getPropertiesByVersionId(version.getId(), false);
        List<ConceptDesignationDTO> designationList = DesignationDelegate.getByVersionId(version.getId(), false);

        Map<Long, Map<String, Set<String>>> relationshipMap = ConceptRelationshipDelegate.getRelationshipsForCodeSystem(version.getId());
        Map<Long, Map<String, List<Property>>> propertyMap = getPropertyMap(propertyList);
        Map<Long, Map<String, List<ConceptDesignationDTO>>> designationMap = getDesignationMap(designationList);
        
        List<PublishConceptDTO> publishConcepts = new ArrayList<PublishConceptDTO>();
        for (CodedConcept codedConcept : codedConcepts)
		{
			PublishConceptDTO publishConcept = new PublishConceptDTO();
			if (codedConcept.getId() == codedConcept.getEntityId())
			{
				publishConcept.setChangeType(DataChangeType.NEW);
			}
			else
			{
				publishConcept.setChangeType(DataChangeType.CHANGED);
			}

			Map<String, List<Property>> propertyTypeMap = propertyMap.get(codedConcept.getEntityId());
			List<NameValueDTO> propertyNameValuePairsList = new ArrayList<NameValueDTO>();
			for (PropertyConfig propertyConfig : codeSystemConfig.getPropertyFilters())
			{
				List<Property> properties = propertyTypeMap.get(propertyConfig.getName());
				if (properties != null)
				{
					if (propertyConfig.isList() == false && properties.size() > 1)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and property " + propertyConfig.getName() +
	                    		" cannot have multiple entries");
					}
					for (Property property : properties)
					{
						NameValueDTO propertyNameValuePair = new NameValueDTO(property.getPropertyType().getName(), property.getValue());
						propertyNameValuePairsList.add(propertyNameValuePair);
					}
				}
				else
				{
					if (propertyConfig.isAllowEmpty() == false)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and property " + propertyConfig.getName() +
	                    		" cannot be empty");
					}
				}
			}
			publishConcept.setPropertyList(propertyNameValuePairsList);

			Map<String, Set<String>> relationshipValueMap = relationshipMap.get(codedConcept.getEntityId());
			List<NameValueDTO> relationshipNameValuePairsList = new ArrayList<NameValueDTO>();
			for (RelationshipConfig relationshipConfig : codeSystemConfig.getRelationshipsFilters())
			{
				Set<String> values = relationshipValueMap.get(relationshipConfig.getName());
				if (values != null)
				{
					if (relationshipConfig.isList() == false && values.size() > 1)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and relationship " + relationshipConfig.getName() +
	                    		" cannot have multiple entries");
					}
					for (String value : values)
					{
						NameValueDTO relationshipNameValuePair = new NameValueDTO(relationshipConfig.getName(),value);
						relationshipNameValuePairsList.add(relationshipNameValuePair);
					}
				}
				else
				{
					if (relationshipConfig.isAllowEmpty() == false)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and relationship " + relationshipConfig.getName() +
	                    		" cannot be empty");
					}
					
				}
			}
			publishConcept.setRelationshipList(relationshipNameValuePairsList);
			
			Map<String, List<ConceptDesignationDTO>> designationTypeMap = designationMap.get(codedConcept.getEntityId());
			List<NameValueDTO> designationNameValuePairsList = new ArrayList<NameValueDTO>();
			for (DesignationConfig designationConfig : codeSystemConfig.getDesignationFilters())
			{
				List<ConceptDesignationDTO> designations = designationTypeMap.get(designationConfig.getName());
				if (designations != null)
				{
					if (designationConfig.isList() == false && designations.size() > 1)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and designation " + designationConfig.getName() +
	                    		" cannot have multiple entries");
					}
					for (ConceptDesignationDTO designation : designations)
					{
						NameValueDTO designationNameValuePair = new NameValueDTO(designation.getDesignation().getType().getName(),
								designation.getDesignation().getName());
						designationNameValuePairsList.add(designationNameValuePair);
					}
				}
				else
				{
					if (designationConfig.isAllowEmpty() == false)
					{
	                    throw new STSInvalidValueException("Code system " + version.getCodeSystem().getName() +
	                    		" with version " + version.getName() + " and designation " + designationConfig.getName() +
	                    		" cannot be empty");
					}
					
				}
			}
			publishConcept.setDesignationList(designationNameValuePairsList);
			
			Long vuid = codedConcept.getVuid();
			publishConcept.setVuid(vuid); //publishConcept.setVuid(codedConcept.getVuid());
			publishConcept.setActive(true);
			publishConcept.setPublishName(codedConcept.getCode());
			publishConcepts.add(publishConcept);
		}
        
        PublishRegionDTO publishRegion = new PublishRegionDTO(version.getCodeSystem().getName(), publishConcepts);
        
        return publishRegion;
    }

    private static Map<Long, Map<String, List<Property>>> getPropertyMap(List<Property> properties)
    {
    	Map<Long, Map<String, List<Property>>> conceptPropertyMap = new HashMap<Long, Map<String, List<Property>>>();

		List<Property> propertyTypeList = null;
    	for (Property property : properties)
		{
			Map<String, List<Property>> propertyTypeMap = conceptPropertyMap.get(property.getConceptEntityId());
			if (propertyTypeMap == null)
			{
				propertyTypeMap = new HashMap<String, List<Property>>();
				propertyTypeList = new ArrayList<Property>();
				propertyTypeList.add(property);
				propertyTypeMap.put(property.getPropertyType().getName(), propertyTypeList);
				conceptPropertyMap.put(property.getConceptEntityId(), propertyTypeMap);
			}
			else
			{
				propertyTypeList = propertyTypeMap.get(property.getPropertyType().getName());
				if (propertyTypeList == null)
				{
					propertyTypeList = new ArrayList<Property>();
					propertyTypeMap.put(property.getPropertyType().getName(), propertyTypeList);
				}
				propertyTypeList.add(property);
			}
		}
    	
    	return conceptPropertyMap;
    }
    
    private static Map<Long, Map<String, List<ConceptDesignationDTO>>> getDesignationMap(List<ConceptDesignationDTO> designations)
    {
    	Map<Long, Map<String, List<ConceptDesignationDTO>>> designationMap = new HashMap<Long, Map<String, List<ConceptDesignationDTO>>>();
    	List<ConceptDesignationDTO> designationTypeList = null;
    	for (ConceptDesignationDTO designation : designations)
		{
			Map<String, List<ConceptDesignationDTO>> designationTypeMap = designationMap.get(designation.getConceptEntityId());
			if (designationTypeMap == null)
			{
				designationTypeMap = new HashMap<String, List<ConceptDesignationDTO>>();
				designationTypeList = new ArrayList<ConceptDesignationDTO>();
				designationTypeList.add(designation);
				designationTypeMap.put(designation.getDesignation().getType().getName(), designationTypeList);
				designationMap.put(designation.getConceptEntityId(), designationTypeMap);
			}
			else
			{
				designationTypeList = designationTypeMap.get(designation.getDesignation().getType().getName());
				if (designationTypeList == null)
				{
					designationTypeList = new ArrayList<ConceptDesignationDTO>();
					designationTypeMap.put(designation.getDesignation().getType().getName(), designationTypeList);
				}
				designationTypeList.add(designation);
			}
		}
    	
    	return designationMap;
    }

    /**
     * @param versionId
     * @param regions
     * @return List<PublishRegionDTO>
     * @throws STSException
     */
    public static List<PublishRegionDTO> getChangeSetDetail(long versionId, List<RegionDTO> regions) throws STSException
    {
        List<DomainConfig> domains = TerminologyConfigDelegate.getDomains();

        // final result of all subset data
        HashMap<String, List<PublishConceptDTO>> publishMap = new HashMap<String, List<PublishConceptDTO>>();
        
        Set<String> alreadyProcessed = new HashSet<String>();

        // Compile a list of all valid subsets
        Map<String, SubsetConfig> configuredSubsets = getValidSubsets(domains);
        Map<String, SubsetConfig> validSubsets = new HashMap<String, SubsetConfig>();
        // we will filter out any subsets/regions not requested explicitly in
        // the regionlist
        for (RegionDTO region : regions)
        {
            SubsetConfig subsetConfig = configuredSubsets.get(region.getName());
            // check to see if this subset has any dependencies which need to be added to the valid subset list
            List<DependentSubsetRule> dependencies = subsetConfig.getDependentSubsetRules();
            for (DependentSubsetRule rule : dependencies)
            {
                String subsetName = rule.getSubsetName();
                SubsetConfig dependentConfig = configuredSubsets.get(subsetName);
                validSubsets.put(subsetName, dependentConfig);
            }
            validSubsets.put(region.getName(), subsetConfig);
        }

        List<Long> conceptEntityIds = VersionDelegate.getConceptEntityIdsByVersionId(versionId);
        processPublishDataForConceptEntity(versionId, publishMap, validSubsets, conceptEntityIds, alreadyProcessed);

        return buildOrderedRegions(domains, publishMap);
    }

    /**
     * @param publishMap
     * @param validSubsets
     * @param conceptEntityId
     * @throws STSException
     */
    private static void processPublishDataForConceptEntity(long versionId, HashMap<String, List<PublishConceptDTO>> publishMap,
            Map<String, SubsetConfig> validSubsets, Collection<Long> conceptEntityIds, Collection<String> alreadyProcessed) throws STSException
    {
        // CodedConceptChangeDTO concept =
        // CodedConceptDelegate.getConceptChange(conceptEntityId);
        List<SubsetPublishDetailDTO> subsets = SubsetDelegate.getSubsetPublishDetail(conceptEntityIds, versionId);

        List<Long> conceptEntityLookups = new ArrayList<Long>(conceptEntityIds);

        
        // We need to find out if because of inverse relationships we have a change that points to another subset 
        // that need to be publish because of this change
        List<SubsetPublishDetailDTO> subsetForTargets = new ArrayList<SubsetPublishDetailDTO>();
        Set<SubsetConfig> participatingSubsets = new HashSet<SubsetConfig>();
        List<Long> subsetRelationshipEntityIds = new ArrayList<Long>();
        for (SubsetPublishDetailDTO detail : subsets)
        {
            SubsetConfig subsetConfig = validSubsets.get(detail.getName());
            if (subsetConfig != null)
            {
                subsetRelationshipEntityIds.add(detail.getEntityId());
                conceptEntityLookups.add(detail.getDesignationEntityId());
            	participatingSubsets.add(subsetConfig);
                List<String> relationshipTypeNames = new ArrayList<String>();
                List<String> subsetNames = new ArrayList<String>();
                List<DependentSubsetRule> dependentSubsetRules = subsetConfig.getDependentSubsetRules();
                for (DependentSubsetRule rule : dependentSubsetRules)
                {
                    relationshipTypeNames.add(rule.getRelationshipName());
                    subsetNames.add(rule.getSubsetName());
                }
                if (dependentSubsetRules.size() > 0)
                {
                	// find all target for the dependency
                	List<SubsetPublishDetailDTO> targets = SubsetDelegate.getSubsetPublishDetailForTargets(conceptEntityIds, subsetNames, relationshipTypeNames, versionId);
                	for (Iterator<SubsetPublishDetailDTO> iterator = targets.iterator(); iterator.hasNext();)
                    {
                        SubsetPublishDetailDTO subsetPublishDetailDTO = iterator.next();
                		// check to see if any of the dependencies have been moved previously
                		if (subsetPublishDetailDTO.getPreviousConceptEntityId() != null && 
							subsetPublishDetailDTO.getPreviousConceptEntityId() != subsetPublishDetailDTO.getConceptEntityId())
                		{
                			// add the old target so we can publish that information
                			List<Long> conceptEntityIdList = new ArrayList<Long>();
                			conceptEntityIdList.add(subsetPublishDetailDTO.getPreviousConceptEntityId());
                			List<SubsetPublishDetailDTO> previousTargets = SubsetDelegate.getSubsetPublishDetail(conceptEntityIdList, versionId);
                			for (SubsetPublishDetailDTO subsetPublishDetail : previousTargets) 
                			{
                				subsetPublishDetail.setRelationshipTypeNames(relationshipTypeNames);
                                subsetForTargets.add(subsetPublishDetail);
							}
                		}
                	    // check to see if the concept relationship was inactivated and then reactived so we have two entries in the
                	    // authoring version for the same entityId - this should have the same effect as no change
                	    if (subsetPublishDetailDTO.getDesignationVersion().getId() < versionId)
                	    {
                	        ConceptRelationship conceptRelationship = ConceptRelationshipDelegate.getPreviousRelationship(subsetPublishDetailDTO.getConceptRelationshipEntityId(), versionId);
                	        if (conceptRelationship != null && conceptRelationship.getActive() == subsetPublishDetailDTO.isConceptRelationshipActive() &&
                	        		subsetPublishDetailDTO.getPreviousConceptEntityId() == subsetPublishDetailDTO.getConceptEntityId())
                	        {
                	            // no change was made we need to remove the item
                	            iterator.remove();
                	        }
                	    }
					}
                	subsetForTargets.addAll(targets);
                }
            }
        }
        // add in all the target because of dependencies
        subsets.addAll(subsetForTargets);

        List<String> propertyTypeNames = new ArrayList<String>();
        for (SubsetConfig subsetConfig : participatingSubsets)
		{
			propertyTypeNames.addAll(subsetConfig.getPropertyNameList());
		}
        // bulk calls
        Map<Long, Map<String, List<Object>>> propertyValuesMap = getBulkPropertyValues(conceptEntityLookups, versionId, propertyTypeNames);
        Map<Long, List<String>> changedPropertyTypesMap = PropertyDelegate.getChangedPropertyTypes(conceptEntityLookups, versionId, propertyTypeNames);
        Map<Long,SubsetRelationship> subsetRelationshipMap = SubsetRelationshipDelegate.getVersioned(subsetRelationshipEntityIds);
        
        for (SubsetPublishDetailDTO detail : subsets)
        {
            // have we already processed this concept entity before
            // this can happen when we have inverse relationships and we process the dependencies several times
            if (alreadyProcessed.contains(detail.getName()+":"+detail.getVuid()))
            {
                continue;
            }
            // is this subset a valid subset in the Terminology Config
            // which means it is deployed to vista
            SubsetConfig subsetConfig = validSubsets.get(detail.getName());
            if (subsetConfig != null)
            {
                log.debug("Processing Subset: "+detail.getName()+" Designation: "+detail.getDesignationName());
                
                PublishConceptDTO publishConceptDTO = new PublishConceptDTO();
                // check to see what the previous version was so we can tell
                // what kind of data
                // to return
                DataChangeType dataChangeType = getChangeType(versionId, detail, subsetRelationshipMap);

                List<NameValueDTO> propertyValues = getPropertyNameValues(detail.getConceptEntityId(), detail.getDesignationEntityId(), detail
                        .getDesignationName(), subsetConfig, versionId, propertyValuesMap, changedPropertyTypesMap, dataChangeType);
                List<NameValueDTO> relationshipValues = ServicesDeploymentDelegate.getRelationshipNameValues(detail,
                        subsetConfig, versionId, dataChangeType);

                boolean add = true;
                // build the publish data when we don't have any properties or relationships only if
                // the status has changed which means we have an entry in the authoring version
                if (propertyValues.isEmpty() && relationshipValues.isEmpty())
                {
                    // check the designation
//                    Designation designation = DesignationDelegate.get(detail.getDesignationEntityId(), versionId);
                    if (detail.getDesignationVersion().getId() != versionId && detail.getVersion().getId() != versionId)
                    {
                        add = false;
                    }
                }
                
                if (add)
                {
                    boolean active = (detail.getActive() == true && detail.isDesignationActive() == true) ? true : false;
                    publishConceptDTO.setActive(active);
                    publishConceptDTO.setPublishName(detail.getDesignationName());
                    publishConceptDTO.setPropertyList(propertyValues);
                    publishConceptDTO.setRelationshipList(relationshipValues);
                    publishConceptDTO.setVuid(detail.getVuid());
                    publishConceptDTO.setChangeType(dataChangeType);

                    // put it into a map with the key being the region name
                    alreadyProcessed.add(detail.getName()+":"+detail.getVuid());
                    List<PublishConceptDTO> list = publishMap.get(detail.getName());
                    if (list == null)
                    {
                        list = new ArrayList<PublishConceptDTO>();
                        publishMap.put(detail.getName(), list);
                    }
                    list.add(publishConceptDTO);
                }
            }
        }
    }

    /**
     * Build an ordered list of Regions based on order in the config file
     * 
     * @param domains
     * @param map
     * @return
     */
    private static List<PublishRegionDTO> buildOrderedRegions(List<DomainConfig> domains, HashMap<String, List<PublishConceptDTO>> map)
    {
        List<PublishRegionDTO> regionList = new ArrayList<PublishRegionDTO>();
        for (DomainConfig config : domains)
        {
            List<SubsetConfig> subsets = config.getSubsets();
            for (SubsetConfig subsetConfig : subsets)
            {
                if (subsetConfig.isActive())
                {
                    List<PublishConceptDTO> values = map.get(subsetConfig.getName());
                    if (values != null && !values.isEmpty())
                    {
                        PublishRegionDTO region = new PublishRegionDTO(subsetConfig.getName(), values);
                        regionList.add(region);
                    }
                }
            }
        }
        return regionList;
    }

    /**
     * Return a list of subsets and the enitiyIds in those subsets for a given versionId
     * 
     * @param conceptEntityIdList
     * @param versionId
     * @return
     */
    public static Map<String, List<Long>> getRegionsForConcepts(List<Long> conceptEntityIdList, long versionId)
    {
        return SubsetDelegate.getSubsets(conceptEntityIdList, versionId);
    }
    
    /**
     * Build a map of all the valid subsets by name
     * 
     * @return
     * @throws STSException
     */
    public static Map<String, SubsetConfig> getValidSubsets() throws STSException
    {
        return getValidSubsets(TerminologyConfigDelegate.getDomains());
    }
    public static Map<String, SubsetConfig> getValidSubsets(List<DomainConfig> domains) 
    {
        HashMap<String, SubsetConfig> validSubsets = new HashMap<String, SubsetConfig>();
        for (DomainConfig config : domains)
        {
            List<SubsetConfig> subsets = config.getSubsets();
            for (SubsetConfig subsetConfig : subsets)
            {
                if (subsetConfig.isActive())
                {
                    validSubsets.put(subsetConfig.getName(), subsetConfig);
                }
            }
        }
        return validSubsets;
    }

    /**
     * Get all the property values needed by each domain
     * @param conceptEntityIds
     * @param versionId
     * @param propertyTypeNames
     * @return
     */
	private static Map<Long, Map<String, List<Object>>> getBulkPropertyValues(List<Long> conceptEntityIds, long versionId,  List<String> propertyTypeNames)
    {
    	Map<Long, Map<String, List<Object>>> results = new HashMap<Long,Map<String, List<Object>>>();
    	List<Property> propertyList = PropertyDelegate.getProperties(conceptEntityIds, versionId, propertyTypeNames);
    	for (Property property : propertyList)
		{
    		Map<String, List<Object>> conceptPropertyMap = results.get(property.getConceptEntityId());
    		if (conceptPropertyMap == null)
    		{
    			conceptPropertyMap = new HashMap<String, List<Object>>();
    			results.put(property.getConceptEntityId(), conceptPropertyMap);
    		}
    		List<Object> typeList = conceptPropertyMap.get(property.getPropertyType().getName());
    		if (typeList == null)
    		{
    			typeList = new ArrayList<Object>();
    			conceptPropertyMap.put(property.getPropertyType().getName(), typeList);
    		}
    		typeList.add(property.getValue());
    		
		}
    	return results;
    }
    
      
    /**
     * Get a list of property name values for a given Subset Configuration and
     * dataChangeType for NEW or REACTIVATED get all of the most recent values
     * For CHANGED or DEACTIVATE get all the changes in the authoring version
     * only
     * 
     * @param conceptEntityId
     * @param subsetConfig
     * @param dataChangeType
     * @return
     * @throws STSException
     */
    private static List<NameValueDTO> getPropertyNameValues(Long conceptEntityId, Long designationEntityId, String conceptName,
            SubsetConfig subsetConfig, long versionId, Map<Long, 
            Map<String, List<Object>>> propertyValuesMap, 
            Map<Long, List<String>> changedPropertyTypesMap, DataChangeType dataChangeType) throws STSException
    {
        List<String> validTypeNames = new ArrayList<String>();
        // construct the list of NameValueDTO
        List<NameValueDTO> propertyNameValueList = new ArrayList<NameValueDTO>();

        // check to see if we have any property elements in the configuration
        // that need to be published
        if (subsetConfig.getPropertyNameList().size() > 0)
        {
            Map<String, List<Object>> propertyMap = buildPropertyMap(conceptEntityId, designationEntityId, versionId, validTypeNames, subsetConfig,
                    propertyValuesMap, changedPropertyTypesMap, dataChangeType);
            buildNameValues(propertyNameValueList, validTypeNames, propertyMap);
            if (!propertyNameValueList.isEmpty())
            {
                propertyNameValueList = validateRulesAndOrder(conceptName, subsetConfig.getName(), validTypeNames, subsetConfig.getPropertyFilters(), propertyNameValueList);
            }
        }
        return propertyNameValueList;
    }

    private static Map<String, List<Object>> buildPropertyMap(Long conceptEntityId, Long designationEntityId, long versionId,
            List<String> validTypeNames, SubsetConfig subsetConfig, Map<Long, Map<String, List<Object>>> propertyValueMap,
            Map<Long, List<String>> changedPropertyTypesMap,
            DataChangeType dataChangeType)
    {
    	Map<String, List<Object>> propertyMap = new HashMap<String, List<Object>>();

        List<Long> conceptEntityIdList = new ArrayList<Long>();
        conceptEntityIdList.add(conceptEntityId);
        conceptEntityIdList.add(designationEntityId);


        // if it is NEW or REACTIVATED then send the most current version of
        // each property in the configuration
        if (dataChangeType == DataChangeType.NEW || dataChangeType == DataChangeType.REACTIVATED)
        {
            validTypeNames.addAll(subsetConfig.getPropertyNameList());
            if (validTypeNames.size() > 0)
            {
            	propertyMap = getPropertyMap(conceptEntityId, designationEntityId,
						validTypeNames, propertyValueMap);
            }
        }
        else
        // it is changed so we need to find the changes and then add all the
        // entries for any property config that is a list
        {
        	Set<String> propertyTypeChangeSet = new HashSet<String>();
        	
        	List<String> conceptPropertyTypes = changedPropertyTypesMap.get(conceptEntityId);
        	if (conceptPropertyTypes == null)
        	{
        	    conceptPropertyTypes = new ArrayList<String>();
        	}
        	if (changedPropertyTypesMap != null && changedPropertyTypesMap.get(designationEntityId) != null)
        	{
        		conceptPropertyTypes.addAll(changedPropertyTypesMap.get(designationEntityId));
        	}
        	if (!conceptPropertyTypes.isEmpty())
        	{
	        	for (String propertyType : conceptPropertyTypes)
				{
	        		if (subsetConfig.getPropertyNameList().contains(propertyType))
	        		{
	        			propertyTypeChangeSet.add(propertyType);
	        		}
				}
        	}
            if (!propertyTypeChangeSet.isEmpty())
            {
                // if there is a list of properties we need to send the
                // entire list instead of just the one that changed
                for (String propertyTypeName : propertyTypeChangeSet)
                {
                    if (!validTypeNames.contains(propertyTypeName))
                    {
                        validTypeNames.add(propertyTypeName);
                    }
                }

                propertyMap = getPropertyMap(conceptEntityId, designationEntityId,
						validTypeNames, propertyValueMap);
            }
        }
        return propertyMap;
    }

    /**
     * Get the properties from a map for a given concept and designation entity_id
     * @param conceptEntityId
     * @param designationEntityId
     * @param propertyTypeNames
     * @param propertyValueMap
     * @return
     */
	private static Map<String, List<Object>> getPropertyMap(Long conceptEntityId,
			Long designationEntityId, List<String> propertyTypeNames,
			Map<Long, Map<String, List<Object>>> propertyValueMap)
	{
		Map<String, List<Object>> propertyMap = new HashMap<String, List<Object>>();
		Map<String, List<Object>> conceptPropertyMap = propertyValueMap.get(conceptEntityId);
		Map<String, List<Object>> designationPropertyValues = propertyValueMap.get(designationEntityId);
		// now we need to merge the two maps into one only for the properties listed in the subset config
		for (String propertyTypeName : propertyTypeNames)
		{
			List<Object> values = new ArrayList<Object>();
			if (conceptPropertyMap != null && conceptPropertyMap.get(propertyTypeName) != null)
			{
				values.addAll(conceptPropertyMap.get(propertyTypeName));
			}
			if (designationPropertyValues != null && designationPropertyValues.get(propertyTypeName) != null)
			{
				values.addAll(designationPropertyValues.get(propertyTypeName));
			}
			propertyMap.put(propertyTypeName, values);
		}
		return propertyMap;
	}

    /**
     * Get the Relationship name value information
     * 
     * @param conceptEntityId
     * @param conceptName
     * @param subsetConfig
     * @param dataChangeType
     * @return
     * @throws STSException
     */
    private static List<NameValueDTO> getRelationshipNameValues(SubsetPublishDetailDTO detail, SubsetConfig subsetConfig, long versionId,
            DataChangeType dataChangeType) throws STSException
    {
        long conceptEntityId = detail.getConceptEntityId();
        String conceptName =  detail.getDesignationName();
        // construct the list of NameValueDTO
        List<NameValueDTO> relationshipNameValueList = new ArrayList<NameValueDTO>();

        List<Long> conceptEntityIdList = new ArrayList<Long>();
        conceptEntityIdList.add(conceptEntityId);

        List<String> validTypeNames = new ArrayList<String>();

        // check to see if we have any relationship elements in the
        // configuration that need to be published
        if (subsetConfig.getRelationshipNameList().size() > 0)
        {
            List<String> validRegularTypeNames = new ArrayList<String>();
            List<String> validInverseTypeNames = new ArrayList<String>();
            // if it is NEW or REACTIVATED then send the most current version of
            // each property in the configuration
            if (dataChangeType == DataChangeType.NEW || dataChangeType == DataChangeType.REACTIVATED)
            {
                validRegularTypeNames = subsetConfig.getRegularRelationshipNameList();
                validInverseTypeNames = subsetConfig.getInverseRelationshipNameList();
                // relationshipList =
                // PropertyDelegate.getProperties(conceptEntityIdList,
                // subsetConfig.getgetPropertyNameList());
            }
            else
            // it is changed so we need to find the changes and then add all the
            // entries for any relationship config that is a list
            {
                validRegularTypeNames = ConceptRelationshipDelegate.getChangedRelationshipTypes(conceptEntityId, versionId, subsetConfig
                        .getRegularRelationshipNameList(), false);
                
                // if we have relationship type names on the object then we know this is a subset dependency from an inverse relationship 
                if (detail.getRelationshipTypeNames() != null && detail.getRelationshipTypeNames().size() > 0)
                {
                    // add this in because when we move the relationship we won't find any changes
                    // so we will just augment what types are valid
                    validInverseTypeNames = detail.getRelationshipTypeNames();
                }
                else
                {
                    validInverseTypeNames = ConceptRelationshipDelegate.getChangedRelationshipTypes(conceptEntityId, versionId, subsetConfig
                        .getInverseRelationshipNameList(), true);
                }
            }

            // Check the include with change attribute - this will add another relationship type name if it is specified
            List<String> allTypeNames = new ArrayList<String>();
            allTypeNames.addAll(validRegularTypeNames);
            allTypeNames.addAll(validInverseTypeNames);
            
            Map<String, RelationshipConfig> relationshipFilterMap = new HashMap<String, RelationshipConfig>();
            List<RelationshipConfig> relFilters = subsetConfig.getRelationshipsFilters();
            for (RelationshipConfig relationshipConfig : relFilters) 
            {
            	relationshipFilterMap.put(relationshipConfig.getName(), relationshipConfig);
			}
            for (String name : allTypeNames) 
            {
				RelationshipConfig config = relationshipFilterMap.get(name);
				String includeName = config.getIncludeWithChange();
				if (includeName != null && includeName.length() > 1)
				{
					if (validRegularTypeNames.contains(includeName) == false)
					{
						validRegularTypeNames.add(includeName);
					}
				}
			}
            
            Map<String, List<Object>> regularMap = null;
            Map<String, List<Object>> inverseMap = null;
            if (validRegularTypeNames.size() > 0)
            {
                regularMap = ConceptRelationshipDelegate.getTargetDesignations(conceptEntityId, validRegularTypeNames, versionId, false);
                buildNameValues(relationshipNameValueList, validRegularTypeNames, regularMap);
            }
            if (validInverseTypeNames.size() > 0)
            {
                inverseMap = ConceptRelationshipDelegate.getTargetDesignations(conceptEntityId, validInverseTypeNames, versionId, true);
                buildNameValues(relationshipNameValueList, validInverseTypeNames, inverseMap);
            }

            validTypeNames.addAll(validRegularTypeNames);
            validTypeNames.addAll(validInverseTypeNames);
        }
        // now that we have the final list let's
        if (!relationshipNameValueList.isEmpty())
        {
            relationshipNameValueList = validateRulesAndOrder(conceptName, subsetConfig.getName(), validTypeNames, subsetConfig.getRelationshipsFilters(), relationshipNameValueList);
        }
        return relationshipNameValueList;
    }

    /**
     * 
     * @param nameValueList
     * @param validTypeNames
     * @param configMap
     * @param map
     */
    private static void buildNameValues(List<NameValueDTO> nameValueList, List<String> validTypeNames, Map<String, List<Object>> map)
    {
        if (map != null)
        {
            for (String name : validTypeNames)
            {
                List<Object> values = map.get(name);
                // we don't have any values but we know something has changed
                if (values == null || values.isEmpty())
                {
                    // item has been deleted
                    NameValueDTO value = new NameValueDTO(name, "");
                    nameValueList.add(value);
                }
                else
                {
                    for (Object value : values)
                    {
                        NameValueDTO nameValue = new NameValueDTO(name, value.toString());
                        nameValueList.add(nameValue);
                    }
                }
            }
        }
    }

    /**
     * Validate the values are correct and ensure the data is in the correct order as specified in the Terminology config file
     * 
     * @param filters
     * @param values
     * @throws STSInvalidValueException 
     * @throws STSException
     */
    private static List<NameValueDTO> validateRulesAndOrder(String conceptName, String subsetName, List<String> typeNames, List<? extends BaseConfig> filters,
            List<NameValueDTO> values) throws STSInvalidValueException 
    {
        List<NameValueDTO> results = new ArrayList<NameValueDTO>();
        HashMap<String, List<NameValueDTO>> map = new HashMap<String, List<NameValueDTO>>();
        for (NameValueDTO value : values)
        {
            List<NameValueDTO> list = map.get(value.getName());
            if (list == null)
            {
                list = new ArrayList<NameValueDTO>();
                map.put(value.getName(), list);
            }
            list.add(value);
        }
        for (BaseConfig baseConfig : filters)
        {
            // make sure we look at the valid list of types, Since on changes we
            // don't want to try and validate every
            // type only the ones that changed
            if (typeNames.contains(baseConfig.getName()))
            {
                // get name of the filter
                List<NameValueDTO> valueList = map.get(baseConfig.getName());
                if (valueList == null)
                {
                    valueList = Collections.emptyList();
                }
                if (baseConfig.isAllowEmpty() == false)
                {
                    boolean error = true;
                    for (NameValueDTO valueItem : valueList)
                    {
                        if (valueItem.getValue() != null && valueItem.getValue().length() > 0)
                        {
                            error = false;
							break;
                        }
                    }
                    if (error)
                    {
                        throw new STSInvalidValueException("Subset " + subsetName + " with element " + baseConfig.getName() + " for concept " + conceptName
                                + " cannot be empty");
                    }
                }
                if (baseConfig.isList() == false && valueList.size() > 1)
                {
                    throw new STSInvalidValueException("Subset " + subsetName + " with element " + baseConfig.getName() + " for concept " + conceptName
                            + " cannot have multiple entries");
                }
                results.addAll(valueList);
            }
        }
        return results;
    }

    /**
     * get type type of data change
     * 
     * @param detail
     * @return
     */
    private static DataChangeType getChangeType(long versionId, SubsetPublishDetailDTO detail, Map<Long,SubsetRelationship> subsetRelationshipMap)
    {
        DataChangeType dataType = null;
        if (detail.getVersion().getId() == versionId )
        {
            SubsetRelationship versionedSubsetRelationship = subsetRelationshipMap.get(detail.getEntityId());
            if (versionedSubsetRelationship == null)
            {
                dataType = DataChangeType.NEW;
            }
            else
            {
                if (detail.getActive() == true && versionedSubsetRelationship.getActive() == false)
                {
                    dataType = DataChangeType.REACTIVATED;
                }
                else if (detail.getActive() == false && versionedSubsetRelationship.getActive() == true)
                {
                    dataType = DataChangeType.DEACTIVATED;
                }
            }
        }
        else if (detail.getDesignationVersion().getId() == versionId && detail.isDesignationActive())
        {
            dataType = DataChangeType.REACTIVATED;
        }
        else if (detail.getDesignationVersion().getId() == versionId && !detail.isDesignationActive())
        {
            dataType = DataChangeType.DEACTIVATED;
        }
        else
        {
            dataType = DataChangeType.CHANGED;
        }
        return dataType;
    }

    /**
     * get the Services session
     * @return a Session object
     */
    public static Session getSession()
    {
        return HibernateSessionFactory.currentSession();
    }

    /**
     * Create a version in the vts
     * @throws STSNotFoundException 
     * @throws STSException 
     */
    public static Version createFinalizedVersion(Collection<Long> conceptEntityIds, Collection<Long> mapSetEntityIds) throws STSNotFoundException 
    {
        Version version = VersionDelegate.createVHAT();
        VersionDelegate.setAuthoringToVersion(version, conceptEntityIds, mapSetEntityIds);
        return version;
    }

    /**
     * Return the most current version of VHAT codesystem
     * @return
     */
    public static Version getRecentVHAT()
    {
        return VersionDelegate.getRecent(HibernateSessionFactory.VHAT_NAME, false);
    }

    /**
     * @param state
     * @param conceptsIdList
     * @throws STSException
     */
    public static void setConceptState(String stateType, List<Long> conceptEntityIdList) throws STSException
    {
        State state = StateDelegate.getByType(stateType);
        ConceptStateDelegate.update(conceptEntityIdList, state);
    }

    /**
     * return checksum for the list of regions, version and list of entity Ids
     * @param regionNames
     * @param versionv
     * @param moduleDeploymentIds
     * @return
     * @throws STSException
     */
    @SuppressWarnings("unchecked")
    public static List<RegionChecksumDTO> getChecksums(List<String> regionNames, Long versionId, List<Long> moduleDeploymentIds) throws STSException
    {
        long versionIdRequested = (versionId == null) ? 0L : versionId;
        
        List<RegionChecksumDTO> results = new ArrayList<RegionChecksumDTO>();

        // Compile a list of all valid subsets
        Map<String, SubsetConfig> validSubsets = getValidSubsets();

        for (String regionName : regionNames)
        {
            ChecksumCalculator checksum = new ChecksumCalculator(regionName);
            SubsetConfig subsetConfig = validSubsets.get(regionName);
            if (subsetConfig == null)
            {
                throw new STSException("Invalid region name requested: "+regionName);
            }
            
            List<RegionEntityDTO> regionEntityDTOs = ServicesDeploymentDao.getRegionEntities(regionName, versionIdRequested, moduleDeploymentIds);
            
            // get all the relationship data 
            Map<Long, Map<String, List<Long>>> relationshipMap = null;
            Map<Long, Map<String, List<Long>>> inverseRelationshipMap = null;
            Map<String, Map<String, List<String>>> propertyMap = null;
            List<String> relationshipTypes = subsetConfig.getRegularRelationshipNameList();
            if (!relationshipTypes.isEmpty())
            {
                relationshipMap = ServicesDeploymentDao.getChecksumRelationships(regionName, versionIdRequested, moduleDeploymentIds, relationshipTypes, false);
            }
            List<String> inverseRelationshipTypes = subsetConfig.getInverseRelationshipNameList();
            if (!inverseRelationshipTypes.isEmpty())
            {
                inverseRelationshipMap = ServicesDeploymentDao.getChecksumRelationships(regionName, versionIdRequested, moduleDeploymentIds, inverseRelationshipTypes, true);
            }
            
            List<String> propertyTypes = subsetConfig.getPropertyNameList();
            if (!propertyTypes.isEmpty())
            {
                propertyMap = ServicesDeploymentDao.getChecksumProperties(regionName, versionIdRequested, moduleDeploymentIds, propertyTypes);
            }
            
            
            for (RegionEntityDTO regionEntity : regionEntityDTOs)
            {
                
                // Start putting data into the MD5 engine
                
                // first put in the designation
                checksum.write(String.valueOf(regionEntity.getVuid()));
                checksum.write(regionEntity.getDesignationName());

                if (!propertyTypes.isEmpty())
                {
                    // get a map of properties for a given conceptEntityId
                    Map<String, List<String>> propConceptMap = propertyMap.get(regionEntity.getConceptEntityId()+"-"+CONCEPT_LEVEL);
                    Map<String, List<String>> propDesignationMap = propertyMap.get(regionEntity.getConceptEntityId()+"-"+regionEntity.getDesignationEntityId());
                    
                    if (propConceptMap != null || propDesignationMap != null)
                    {
                        // add in all property information
                        for (PropertyConfig propertyConfig : subsetConfig.getPropertyFilters())
                        {
                            List properties = new ArrayList<String>();
                            // get the properties at the concept level
                            if (propConceptMap != null)
                            {
                                List<String> props = propConceptMap.get(propertyConfig.getName());
                                if (props != null)
                                {
                                    properties.addAll(props);
                                }
                            }
                            // get the properties at the designation level
                            if (propDesignationMap != null)
                            {
                                List<String> props = propDesignationMap.get(propertyConfig.getName());
                                if (props != null)
                                {
                                    properties.addAll(props);
                                }
                            }
                            // now use the properties at both levels
                            if (properties != null && properties.size() > 0)
                            {
                                Collections.sort(properties);
                                for (Object object : properties)
                                {
                                    checksum.write(object.toString());
                                }
                            }
                        }
                    }
                }
                
                if (!relationshipTypes.isEmpty())
                {
                    // get a map of relationships and inverse relationships
                    Map<String, List<Long>> relmap = null;
                
                    // now add in all relationship information
                    for(RelationshipConfig relationshipConfig : subsetConfig.getRelationshipsFilters())
                    {
                        
                        if (relationshipConfig.isInverse())
                        {
                            relmap = inverseRelationshipMap.get(regionEntity.getConceptEntityId());
                        }
                        else
                        {
                            relmap = relationshipMap.get(regionEntity.getConceptEntityId());
                        }
                        if (relmap != null)
                        {
                            List<Long> relationships = relmap.get(relationshipConfig.getName());
                            if (relationships != null && relationships.size() > 0)
                            {
                                Collections.sort(relationships);
                                for (Object object : relationships)
                                    
                                {
                                    checksum.write(object.toString());
                                }
                            }
                        }
                    }
                }
            }
            // close out the checksum and save in our dto
            String checksumString = checksum.getChecksum();
            checksum.close();
            RegionChecksumDTO checksumDTO = new RegionChecksumDTO(regionName, checksumString);
            results.add(checksumDTO);
        }
        return results;
    }

	/**
	 * @return
	 * @throws STSException 
	 */
	public static List<DomainConfig> getDomains(boolean includeInactiveSubsets) throws STSException
	{
		return TerminologyConfigDelegate.getDomains(includeInactiveSubsets);
	}

	public static RegionChecksumDTO calculateSCSChecksum(Version version) throws STSException
	{
		CodeSystemConfig codeSystemConfig = TerminologyConfigDelegate.getCodeSystem(version.getCodeSystem().getVuid());
        if (codeSystemConfig == null)
        {
            throw new STSException("No configuration exists for codesystem. vuid: "+version.getCodeSystem().getVuid());
        }
        ChecksumCalculator checksum = new ChecksumCalculator(codeSystemConfig.getName());

        
        List<Property> propertyList = PropertyDelegate.getPropertiesByVersionId(version.getId(), true);
        List<ConceptDesignationDTO> designationList = DesignationDelegate.getByVersionId(version.getId(), true);
        
        Map<Long, Map<String, List<Property>>> propertyMap = getPropertyMap(propertyList);
        Map<Long, Map<String, List<ConceptDesignationDTO>>> designationMap = getDesignationMap(designationList);
        
        List<CodedConcept> concepts = CodedConceptDelegate.getCodedConcepts(version.getCodeSystem(), version);
        for (CodedConcept concept : concepts)
		{
            // first put in the designation
            checksum.write(String.valueOf(concept.getVuid()));
            checksum.write(concept.getCode());
            
			Map<String, List<Property>> propertyTypeMap = propertyMap.get(concept.getEntityId());
			for (String typeName : codeSystemConfig.getPropertyNameList())
			{
				List<Property> properties = propertyTypeMap.get(typeName);
				if (properties != null)
				{
					// TODO What about sorting the values?
					for (Property property : properties)
					{
                        checksum.write(property.getValue());
					}
				}
			}

			Map<String, List<ConceptDesignationDTO>> designationTypeMap = designationMap.get(concept.getEntityId());
			for (DesignationConfig designationConfig : codeSystemConfig.getDesignationFilters())
			{
				List<ConceptDesignationDTO> designations = designationTypeMap.get(designationConfig.getName());
				if (designations != null)
				{
					// TODO What about sorting the values?
					for (ConceptDesignationDTO designation : designations)
					{
	                    checksum.write(designation.getDesignation().getName());
					}
				}
			}
		}
        
        // close out the checksum and save in our dto
        String checksumString = checksum.getChecksum();
        checksum.close();
        return new RegionChecksumDTO(codeSystemConfig.getName(), checksumString);
		
	}
	/**
	 * Get all subset checksums for a given version and a list of conceptEntityIds
	 * 
	 * @param version
	 * @param conceptEntityIds
	 * @return
	 * @throws STSException
	 */
	public static List<RegionChecksumDTO> getSubsetChecksums(Version version, List<Long> conceptEntityIds) throws STSException
	{
        List<DomainConfig> domains = TerminologyConfigDelegate.getDomains();

        // Compile a list of all valid subsets
        Map<String, SubsetConfig> validSubsets = getValidSubsets(domains);
        List<String> validRegionNames = new ArrayList<String>();
        validRegionNames.addAll(validSubsets.keySet());
        Collection<String> regionNames = ServicesDeploymentDao.getRegionNames(validRegionNames, conceptEntityIds);
        List<String> newRegionNames = new ArrayList<String>();
        newRegionNames.addAll(regionNames);

        for (String regionName : regionNames)
        {
            // find the configuration 
            SubsetConfig subsetConfig = validSubsets.get(regionName);
            // get a list of dependencies
            List<DependentSubsetRule> dependencies = subsetConfig.getDependentSubsetRules();
            // add each dependency
            for (DependentSubsetRule dependentSubsetRule : dependencies)
            {
            	newRegionNames.add(dependentSubsetRule.getSubsetName());
            }
        }
        
        List<Long> emptyConceptList = new ArrayList<Long>();
        emptyConceptList.add(new Long(0));
        List<RegionChecksumDTO> regionChecksums = getChecksums(newRegionNames, version.getId(), emptyConceptList);

        return regionChecksums;
	}
	
	/**
	 * Get a version by it's id
	 * 
	 * @param versionId
	 * @return
	 */
	public static Version getVersionById(long versionId)
	{
		return VersionDelegate.getByVersionId(versionId);
	}

	public static List<String> getVersionNamesByIds(List<Long> versionIds)
	{
	    return VersionDelegate.getVersionNamesByIds(versionIds);
	}
	
    /**
     * get a version by name
     * @param name
     * @return
     */
    public static Version getVHATVersionByName(String name)
    {
        return VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, name);
    }
    
    /**
     * Get the CodedConcept by entityId 
     * @param conceptEntityId
     * @return
     */
    public static List<CodedConcept> getCodedConcepts(List<Long> conceptEntityIds)
    {
        return CodedConceptDelegate.get(conceptEntityIds);    
    }
    
    /**
     * 
     * @param finalizedVersionNames
     * @return
     */
    public static List<Long> getVersionIdsByNames(List<String> finalizedVersionNames)
    {
        return VersionDelegate.getByVersionNames(finalizedVersionNames);
    }
    
	/*
	 * @param list of conceptEntityIds
	 * @param stateType
	 * @return
	 * @throws STSException 
	 */
	public static void validateConceptStateOfConcepts(List<Long> conceptEntityIds, String stateType) throws STSException
	{
		Session session = HibernateSessionFactory.currentSession();
        session.clear();
        
        State state = null;
        try
        {
       		state = StateDelegate.getByType(stateType);
        }
        catch (Exception ex)
        {
            throw new STSException("Cannot find the concept state: "+stateType, ex);
        }
    	List<ConceptState> stateOfConcepts = ConceptStateDelegate.get(conceptEntityIds);
    	for (ConceptState conceptState : stateOfConcepts)
    	{
        	if (conceptState.getState().getId() != state.getId())
        	{
        		Concept concept = ConceptDelegate.get(conceptState.getConceptEntityId(), HibernateSessionFactory.AUTHORING_VERSION_ID);
        		throw new STSInvalidValueException(concept.getName()+" is not in the state of "+stateType);
        	}
		}
	}

	/**
     * Return a list of states
     * @return
     * @throws STSException 
     */
    public static List<State> getStates() throws STSException
    {
        return StateDelegate.getStates();
    }
    
    /**
     * Get all the dependencies that have changed for a given list of concept entities
     * Map<Target Concept, List<Source Concept>>
     * @param conceptEntityIds
     * @return a list of concept entities that are dependcies
     * @throws STSException 
     */
    public static Map<Long, Set<Long>> getChangedDependencies(List<Long> conceptEntityIds) throws STSException 
    {
        Map<Long, Set<Long>> changedMap = new HashMap<Long, Set<Long>>();
/*        
        for each concept in list
        {
          1. Get all targets (this included parents)
          2. Get all inverse rels (Look at each subset that it participates in )
          3. See if 1 and 2 have changed by looking at concept state
        }
*/
        List<ConceptRelationship> targets = ConceptRelationshipDelegate.getRelationships(conceptEntityIds);

        processChangedRelationships(changedMap, targets, false);
        
        // Compile a list of all valid subsets
        List<DomainConfig> domains = TerminologyConfigDelegate.getDomains();
        Map<String, SubsetConfig> validSubsets = getValidSubsets(domains);

        // Get the subsets that belong to these concepts
        Map<String, List<Long>> subsetMap = SubsetDelegate.getSubsets(conceptEntityIds, HibernateSessionFactory.AUTHORING_VERSION_ID);
        Set<String> keyset = subsetMap.keySet();
        for (String subsetName : keyset)
        {
            SubsetConfig config = validSubsets.get(subsetName);
            if (config != null)
            {
                List<String> inverseRelationships = config.getInverseRelationshipNameList();
    
                // check to see if this subset has an inverse relationship
                if (inverseRelationships.size() > 0)
                {
                    List<Long> conceptIds = subsetMap.get(subsetName);
                    // get the inverse relationships and add them if they have changed
                    List<ConceptRelationship> inverseRels = ConceptRelationshipDelegate.getRelationships(conceptIds, inverseRelationships, null, true);
                    processChangedRelationships(changedMap, inverseRels, true);
                }
            }
        }
        return changedMap; 
    }


    /**
     * Build a map where the key is the target and the value is a list of sources (inverted if inverse = true)
     * @param dependencyMap
     * @param relationships
     * @param dependencyDomain
     * @param inverse
     */
    private static void processDependencyRelationships(Map<Long, Set<Long>> dependencyMap, List<ConceptRelationship> relationships)
    {
         for (ConceptRelationship relationship : relationships)
        {
            long key = relationship.getTargetEntityId();
            long value = relationship.getSourceEntityId();
            // check to see if the target is part of the dependency Domain
            Set<Long> dependencyRelationships = dependencyMap.get(key);
            if (dependencyRelationships == null)
            {
                dependencyRelationships = new HashSet<Long>();
                dependencyMap.put(key, dependencyRelationships);
            }
            dependencyRelationships.add(value);
        }
        
    }
    /**
     * @param changedMap
     * @param targets
     */
    private static void processChangedRelationships(Map<Long, Set<Long>> changedMap, List<ConceptRelationship> targets, boolean inverse)
    {
        Map<Long, List<ConceptRelationship>> relationshipLookup = new HashMap<Long, List<ConceptRelationship>>();
        for (ConceptRelationship relationship : targets)
        {
            // build a map of targets (or sources if inverse) as key to concept relationships for those targets
            long id = inverse ? relationship.getSourceEntityId() : relationship.getTargetEntityId();
            List<ConceptRelationship> relationships = relationshipLookup.get(id);
            if (relationships == null)
            {
                relationships = new ArrayList<ConceptRelationship>();
                relationshipLookup.put(id, relationships);
            }
            relationships.add(relationship);
        }
        // get the states for the targets
        List<Long> conceptIds = new ArrayList<Long>();
        conceptIds.addAll(relationshipLookup.keySet());
        
        if (conceptIds.size() > 0)
        {
            List<ConceptState> conceptStates = ConceptStateDelegate.get(conceptIds);
            for (ConceptState state : conceptStates)
            {
                List<ConceptRelationship> relationships = relationshipLookup.get(state.getConceptEntityId());
                if (relationships != null && relationships.size() > 0)
                {
                    Set<Long> sources = changedMap.get(state.getConceptEntityId());
                    if (sources == null)
                    {
                        sources = new HashSet<Long>();
                        changedMap.put(state.getConceptEntityId(), sources);
                    }
                    for (ConceptRelationship relationship : relationships)
                    {
                        // if inverse then we want to use the targetId instead of the source
                        long id = inverse ? relationship.getTargetEntityId() : relationship.getSourceEntityId();
                        sources.add(id);
                    }
                }
            }
        }
    }

    /**
     * Sources are the concepts that we are checking to see if they are pointing to the targets
     * the targets are the concepts we are removing from a deployment and need to see what the impact will be
     * 
     * @param possibleDependents
     * @param conceptsToBeRemoved
     * @return a map of targets and the sources they depend on
     * @throws STSException
     */
    public static Map<Long, Set<Long>> getDependencyRelationships(List<Long> possibleDependents, List<Long> conceptsToBeRemoved) throws STSException
    {
        Map<Long, Set<Long>> dependencyMap = new HashMap<Long, Set<Long>>();
        if (possibleDependents != null && possibleDependents.size() > 0)
        {
            // find all the sources that have a relationship with the targets
            // if we find anything then we have a dependency problem
            List<ConceptRelationship> relationships = ConceptRelationshipDelegate.getRelationships(possibleDependents, conceptsToBeRemoved);
            processDependencyRelationships(dependencyMap, relationships);
            
            // look at the targets (items being removed) and see if they are the source of an inverse relationship
            // to tell we need to look at the subset config and see if they have dependencies
            
            // Compile a list of all valid subsets
            List<DomainConfig> domains = TerminologyConfigDelegate.getDomains();
            Map<String, SubsetConfig> validSubsets = getValidSubsets(domains);
    
            // look at what subsets they belong too
            Map<String, List<Long>> subsetMap  = SubsetDelegate.getSubsets(conceptsToBeRemoved, HibernateSessionFactory.AUTHORING_VERSION_ID);
            Set<String> keyset = subsetMap.keySet();
            for (String subsetName : keyset)
            {
                SubsetConfig config = validSubsets.get(subsetName);
                if (config != null)
                {
                    List<DependentSubsetRule> dependents = config.getDependentSubsetRules();
                    
                    // we have dependencies so let's get the relationships
                    if (dependents.size() > 0)
                    {
                        // build a list of relationship names to use
                        List<String> dependentRelationshipTypes = new ArrayList<String>();
                        for (DependentSubsetRule rule : dependents)
                        {
                            dependentRelationshipTypes.add(rule.getRelationshipName());
                        }
                        List<ConceptRelationship> list = ConceptRelationshipDelegate.getRelationships(subsetMap.get(subsetName), dependentRelationshipTypes, possibleDependents, false);
                        processDependencyRelationships(dependencyMap, list);
                    }
                }
            }
        }
        return dependencyMap;
    }
    
    /**
     * Return the concept objects for a given list of conceptEntityIds 
     * @param conceptEntityIds
     * @return
     */
    public static Map<Long,CodedConcept> getConceptDependencyData(List<Long> conceptEntityIds)
    {
        Map<Long, CodedConcept> map = new HashMap<Long, CodedConcept>();
        List<CodedConcept> concepts = CodedConceptDelegate.get(conceptEntityIds);
        for (CodedConcept concept : concepts)
        {
            map.put(concept.getEntityId(), concept);
        }
        return map;
    }
    
    /**
     * Get the named SubsetConfig
     * @param subsetName
     * @return SubsetConfig by name
     * @throws STSException
     */
    /*
    public static RegionConfig getRegionConfigByName(String subsetName) throws STSException
    {
    	return TerminologyConfigDelegate.getRegion(subsetName);
    }
    */
	public static MapSet getMapSet(long mapSetId)
    {
        return (MapSet) ConceptDelegate.get(mapSetId);
	}

    public static List<SubsetDetailDTO> getSubsetDetail(long conceptEntityId)
    {
        return SubsetDelegate.getSubsetDetail(conceptEntityId);
    }

    /**
     * Populate the DiscoveryResults temporary table in preparation for finding deltas.
     * @param siteId
     * @param regionEntityId
     * @param versionId
     * @param moduleDeploymentIds
     * @param relationshipTypes
     * @return DiscoveryResultsDTO
     * @throws STSException
     */
	public static DiscoveryResultsDTO getDiscoveryDeltas(Long siteId, String regionName, Long versionId,
			List<Long> moduleDeploymentIds, List<RelationshipConfig> relationshipFilters, List<String> propertyTypes, boolean isActiveComparison) throws STSException
	{
		DiscoveryResultsDTO discoveryResults = new DiscoveryResultsDTO();

		List<String> inverseRelationshipNames = new ArrayList<String>();
		List<String> regularRelationshipNames = new ArrayList<String>();
		List<String> allRelationshipTypeNames = new ArrayList<String>();
		for(RelationshipConfig filter : relationshipFilters)
		{
			String filterName = filter.getName();
			if(filter.isInverse())
			{
				inverseRelationshipNames.add(filterName);
			}
			else
			{
				regularRelationshipNames.add(filterName);
			}
			allRelationshipTypeNames.add(filterName);
		}
		
		try
		{
		    // TEMP - only used for debugging CODE REVIEW ALERT
//		    ServicesDeploymentDao.cleanOutDiscoveryResults();
		    
			//create designations in temp table
			ServicesDeploymentDao.createDiscoveryDesignations(regionName, versionId, moduleDeploymentIds);
			
			//create properties in temp table
			ServicesDeploymentDao.createDiscoveryProperties(regionName, versionId, moduleDeploymentIds, propertyTypes);
			
			//create regular relationships and inverse relationships in temp table
			if(inverseRelationshipNames.size() > 0)
			{
				ServicesDeploymentDao.createDiscoveryRelationships(regionName, versionId, moduleDeploymentIds, inverseRelationshipNames, true);
			}
			if(regularRelationshipNames.size() > 0)
			{
				ServicesDeploymentDao.createDiscoveryRelationships(regionName, versionId, moduleDeploymentIds, regularRelationshipNames, false);
			}
			
			//post-processing method for discovery results
			ServicesDeploymentDao.postProcessDiscoveryResults();
			
			//get the deltas and add to results
			List<DiscoveryDeltaDTO> designationDeltas = ServicesDeploymentDao.getDiscoveryDeltas(siteId,
					regionName, versionId, moduleDeploymentIds, Arrays.asList(new String[]{TERM_FIELD_NAME}), TERM_TYPE, allRelationshipTypeNames, isActiveComparison);

			//get the deltas and add to results
			List<DiscoveryDeltaDTO> propertyDeltas = ServicesDeploymentDao.getDiscoveryDeltas(siteId,
					regionName, versionId, moduleDeploymentIds, propertyTypes, PROPERTY_TYPE, allRelationshipTypeNames, isActiveComparison);

			//Get the deltas and add to results
			List<DiscoveryDeltaDTO> relationshipDeltas = ServicesDeploymentDao.getDiscoveryDeltas(siteId,
					regionName, versionId, moduleDeploymentIds, allRelationshipTypeNames, RELATIONSHIP_TYPE, allRelationshipTypeNames, isActiveComparison);
			
			discoveryResults.setDesignationDeltas(designationDeltas);
			discoveryResults.setPropertyDeltas(propertyDeltas);
			discoveryResults.setRelationshipDeltas(relationshipDeltas);
		}
		catch (Exception e)
		{
			throw new STSException(e);
		}
		
		return discoveryResults;
	}

	public static List<DiscoveryMappingResultsDTO> getDiscoveryMapSetDeltas(Long siteId, String mapSetName, Long mapSetEntityId, Long versionId, boolean isActiveComparison)
	{
		String baseMappingQuery = MapEntryDelegate.getDiscoveryEntriesQuery();
    	ServicesDeploymentDao.createDiscoveryMappings(mapSetEntityId, versionId, baseMappingQuery);
        List<DiscoveryMappingResultsDTO> entryResults = ServicesDeploymentDao.getDiscoveryMappingDeltas(siteId, mapSetName, isActiveComparison);
		return entryResults;
	}

	public static void syncConceptNameTextIndex()
	{
		log.debug("Ready to sync index: 'CONCEPT_NAME_T_IDX'");
		ServicesDeploymentDao.syncConceptNameTextIndex();
		log.debug("Finished sync of index: 'CONCEPT_NAME_T_IDX'");
	}
	
	public static List<Designation> getDesinationsByVuids(Set<Long> vuids)
	{
	    return DesignationDelegate.getByVuids(vuids);
	}
	
	public static void checkForDuplicateVUIDs() throws STSException
	{
		CodedConceptDelegate.checkForDuplicateVUIDs();
	}
	
	public static Concept getCodedConceptByEntityId(long conceptEntityId)
	{
		return ConceptDelegate.get(conceptEntityId);
	}
	
    public static final void main(String[] args) throws STSException
    {
        List<Long> modules = new ArrayList<Long>();
//        modules.add(new Long(153));
//        modules.add(new Long(156));
        
        long t1,t2;
        List<RegionChecksumDTO> results = null;
        List<String> regions = new ArrayList<String>();

        regions.add("TIU Setting");
        regions.add("TIU SMD");
        regions.add("TIU Status");
        regions.add("Vital Types");
        regions.add("Vital Categories");
        regions.add("Vital Qualifiers");
//        regions.add("TIU Service");
        
        t1 = System.currentTimeMillis();
        results = ServicesDeploymentDelegate.getChecksums(regions, 17L, modules);
        t2 = System.currentTimeMillis();

        for (RegionChecksumDTO checksumDTO : results)
        {
            System.out.println(checksumDTO.getRegionName()+": "+checksumDTO.getRegionChecksum());
        }
        System.out.println("Total Time: "+(t2-t1));
/*        
        regions = new ArrayList<String>();
        regions.add("Reactants");
        t1 = System.currentTimeMillis();
        results = ServicesDeploymentDelegate.getChecksums(regions, 17L, modules);
        t2 = System.currentTimeMillis();
        for (RegionChecksumDTO checksumDTO : results)
        {
            System.out.println(checksumDTO.getRegionName()+": "+checksumDTO.getRegionChecksum());
        }
        System.out.println("Total Time: "+(t2-t1));
*/        
        
    }
    
    /**
     * Get the named SubsetConfig
     * @param subsetName
     * @return SubsetConfig by name
     * @throws STSException
     */
    public static SubsetConfig getSubsetConfigByName(String subsetName) throws STSException
    {
    	SubsetConfig subsetConfig = null;
    	
    	try
    	{
    		subsetConfig = TerminologyConfigDelegate.getSubset(subsetName);
    	}
    	catch(Exception e)
    	{
    		throw new STSException("An error was generated while retrieving SubsetConfig for subsetName: " + subsetName, e);
    	}
    	
    	return subsetConfig;
    }
	public static void disconnect()
	{
        HibernateSessionFactory.disconnect();
	}
	/**
	 * 
	 * @param mapSetDTO
	 * @param versionId
	 * @return
	 */
	public static List<MapEntryDetailDTO> getEntries(MapSetDTO mapSetDTO, Long versionId)
	{
		return MapEntryDelegate.getEntries(mapSetDTO, versionId);
	}
	/**
	 * 
	 * @param mapSetDTO
	 * @param versionId
	 * @return
	 */
	public static List<MapEntryDetailDTO> getDeploymentMapEntries(MapSetDTO mapSetDTO, Long versionId)
	{
		return MapEntryDelegate.getDeploymentMapEntries(mapSetDTO, versionId);
	}
}
