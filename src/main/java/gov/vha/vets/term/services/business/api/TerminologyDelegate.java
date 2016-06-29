package gov.vha.vets.term.services.business.api;

import gov.vha.vets.term.services.business.CodeSystemDelegate;
import gov.vha.vets.term.services.business.CodedConceptDelegate;
import gov.vha.vets.term.services.business.ConceptDelegate;
import gov.vha.vets.term.services.business.ConceptRelationshipDelegate;
import gov.vha.vets.term.services.business.DesignationDelegate;
import gov.vha.vets.term.services.business.MapEntryCacheDelegate;
import gov.vha.vets.term.services.business.MapSetDelegate;
import gov.vha.vets.term.services.business.PropertyDelegate;
import gov.vha.vets.term.services.business.SubsetDelegate;
import gov.vha.vets.term.services.business.SubsetRelationshipDelegate;
import gov.vha.vets.term.services.business.TerminologyConfigDelegate;
import gov.vha.vets.term.services.business.VersionDelegate;
import gov.vha.vets.term.services.dao.CodedConceptDao;
import gov.vha.vets.term.services.dto.CodedConceptDesignationDTO;
import gov.vha.vets.term.services.dto.CodedConceptListDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipConceptListDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipDTO;
import gov.vha.vets.term.services.dto.ConceptRelationshipListDTO;
import gov.vha.vets.term.services.dto.MapEntryCacheListDTO;
import gov.vha.vets.term.services.dto.MapSetDetailDTO;
import gov.vha.vets.term.services.dto.MapSetDetailListDTO;
import gov.vha.vets.term.services.dto.RelatedConceptsDTO;
import gov.vha.vets.term.services.dto.RelationshipTypeListDTO;
import gov.vha.vets.term.services.dto.SubsetCountDTO;
import gov.vha.vets.term.services.dto.SubsetVersionDTO;
import gov.vha.vets.term.services.dto.api.CodeSystemConceptsViewDTO;
import gov.vha.vets.term.services.dto.api.CodeSystemListViewDTO;
import gov.vha.vets.term.services.dto.api.CodeSystemViewDTO;
import gov.vha.vets.term.services.dto.api.CodedConceptListViewDTO;
import gov.vha.vets.term.services.dto.api.ConceptViewDTO;
import gov.vha.vets.term.services.dto.api.DesignationViewDTO;
import gov.vha.vets.term.services.dto.api.RelationshipDetailListViewDTO;
import gov.vha.vets.term.services.dto.api.RelationshipDetailViewDTO;
import gov.vha.vets.term.services.dto.api.RelationshipViewDTO;
import gov.vha.vets.term.services.dto.api.SubsetContentsListView;
import gov.vha.vets.term.services.dto.api.SubsetListViewDTO;
import gov.vha.vets.term.services.dto.api.TotalEntityListView;
import gov.vha.vets.term.services.dto.api.UsageContextDetailViewDTO;
import gov.vha.vets.term.services.dto.api.UsageContextListViewDTO;
import gov.vha.vets.term.services.dto.config.MapSetConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.StringKeyObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TerminologyDelegate
{
	public static final String CONCEPT_CODE_TYPE = "ConceptCode";
	public static final String DESIGNATION_CODE_TYPE = "DesignationCode";
	public static final String DESIGNATION_NAME_TYPE = "DesignationName";
	public static final int MAP_ENTRIES_CALL = 1;
	public static final int MAP_ENTRIES_FROM_SOURCE_CALL = 2;
	
	public static final String DESIGNATION_TYPE_NAME_KEY_PREFIX = "DTN:";
	public static final String DESIGNATION_TYPE_ID_KEY_PREFIX = "DTID:";
	public static final String MAP_SET_KEY_PREFIX = "MS:";
	public static final String VERSION_NAME_KEY_PREFIX = "V_NAME:";
	public static final String VERSION_ID_KEY_PREFIX = "V_ID:";
	
	public static final String CURRENT_VERSION = "current";

	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_PAGE_SIZE = 100;

	public static CodeSystemListViewDTO getCodeSystems(String codeSystemName, Integer pageSize, Integer pageNumber)
	{
		CodeSystemListViewDTO codeSystemListViewDTO = CodeSystemDelegate.getCodeSystems(codeSystemName, pageSize, pageNumber);
		List<Version> allFinalizedVersions = VersionDelegate.getAllFinalizedVersions();

		for (CodeSystemViewDTO codeSystemView : codeSystemListViewDTO.getCodeSystemViewDTOs())
		{
			List<Version> codeSystemVersions = new ArrayList<Version>();
			for (Version version : allFinalizedVersions)
			{
				if (version.getCodeSystem().getId() == codeSystemView.getCodeSystem().getId())
				{
					codeSystemVersions.add(version);
				}
			}

			codeSystemView.setVersions(codeSystemVersions);
		}

		return codeSystemListViewDTO;
	}

	public static CodeSystemViewDTO getCodeSystemDetails(long vuid, String versionName) throws STSException
	{
		CodeSystem codeSystem = CodeSystemDelegate.getByVuid(vuid);
		if (codeSystem == null)
		{
			throw new STSException("Code System VUID '" + vuid + "' is invalid.");
		}
		List<Version> allFinalizedVersions = VersionDelegate.getAllFinalizedVersions();

		List<Version> codeSystemVersions = new ArrayList<Version>();
		for (Version version : allFinalizedVersions)
		{
			if (version.getCodeSystem().getId() == codeSystem.getId()
					&& (version.getName().toUpperCase().equals(versionName.toUpperCase()) || versionName.equalsIgnoreCase(CURRENT_VERSION)))
			{
				codeSystemVersions.add(version);
				break;
			}
		}

		if (codeSystemVersions.size() == 0)
		{
			throw new STSException("The version Name '" + versionName + "' is invalid.");
		}
		
		CodeSystemViewDTO codeSystemViewDTO = new CodeSystemViewDTO(codeSystem, codeSystemVersions);

		return codeSystemViewDTO;
	}

	/**
	 * Get Concept details
	 * 
	 * @param codeSystemVuid
	 * @param versionName
	 * @param code
	 * @return
	 * @throws STSException
	 */
	public static ConceptViewDTO getConceptDetail(Long codeSystemVuid, String versionName, String code) throws STSException
	{
		ConceptViewDTO conceptView = new ConceptViewDTO();
		CodeSystem codeSystem = validateCodeSystemVuid(codeSystemVuid);
		Version version = validateCodeSystemVersion(versionName, codeSystem, true);
		CodedConcept concept = CodedConceptDelegate.get(version, code);
		if (concept == null)
		{
			throw new STSException("Cannot find concept with code: " + code);
		}
		conceptView.setConceptCode(concept.getCode());
		conceptView.setConceptStatus(concept.getActive());
		conceptView.setProperties(PropertyDelegate.getProperties(concept.getEntityId()));
		List<Designation> designations = DesignationDelegate.get(concept, version);
		
		// get all the properties for all the designation and put them in a hash map
		// first get all the properties for all the designations
		List<Long> designationEntityIds = new ArrayList<Long>();
		for (Designation designation : designations)
		{
			designationEntityIds.add(designation.getEntityId());
		}
		List<Property> desProperties = PropertyDelegate.getProperties(designationEntityIds, version.getId());
		
		// now create the property hash map and put in list of properties using designationEntityId as the key
		HashMap<Long, List<Property>> desPropertyMap = new HashMap<Long, List<Property>>();
		for (Property property : desProperties)
		{
			List<Property> propertyList = desPropertyMap.get(property.getConceptEntityId());
			if (propertyList == null)
			{
				propertyList = new ArrayList<Property>();
				desPropertyMap.put(property.getConceptEntityId(), propertyList);
			}
			propertyList.add(property);
		}
		
		Map<Long, List<Subset>> subsetMap = SubsetDelegate.getSubsetsByDesignationEntityIds(designationEntityIds, version.getId());

		// create the designationViewDTO and stuff in the designation, list of properties, and the subset membership attributes
		List<DesignationViewDTO> designationViews = new ArrayList<DesignationViewDTO>();
		for (Designation designation : designations)
		{
			DesignationViewDTO designationView = new DesignationViewDTO();
			designationView.setDesignation(designation);
			designationView.setProperties(desPropertyMap.get(designation.getEntityId()));
			designationView.setSubsets(subsetMap.get(designation.getEntityId()));
			designationViews.add(designationView);
		}
		conceptView.setDesignations(designationViews);

		// Now do a lot of work to get relationships
		List<ConceptRelationship> ConceptRelationships = ConceptRelationshipDelegate.getRelationships(concept.getEntityId(), version.getId());
        Collection<Long> targetSet = new HashSet<Long>();
		for (ConceptRelationship conceptRelationship : ConceptRelationships)
		{
			targetSet.add(conceptRelationship.getTargetEntityId());
		}
		if (!targetSet.isEmpty())
		{
			Collection<CodedConcept> targetConcepts = CodedConceptDelegate.get(targetSet);
			Map<Long, CodedConcept> conceptMap = new HashMap<Long, CodedConcept>();
			for (CodedConcept targetConcept : targetConcepts)
			{
				conceptMap.put(targetConcept.getEntityId(), targetConcept);
			}
			Map<Long, Designation> targetConceptDesignationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(version.getCodeSystem(), version.getId(), targetSet);
			List<RelationshipViewDTO> relationshipViews = new ArrayList<RelationshipViewDTO>();
			for (ConceptRelationship conceptRelationship : ConceptRelationships)
			{
				CodedConcept targetConcept = conceptMap.get(conceptRelationship.getTargetEntityId());
				Designation targetDesignation = targetConceptDesignationMap.get(targetConcept.getEntityId());
				
				RelationshipViewDTO relationshipView = new RelationshipViewDTO(targetDesignation.getName(),	conceptRelationship.getRelationshipType().getName(), 
						targetConcept.getCode(), conceptRelationship.getActive());
				relationshipViews.add(relationshipView);
			}
			conceptView.setRelationships(relationshipViews);
		}

		return conceptView;
	}

	public static RelationshipDetailViewDTO getRelationshipDetail(Long entityId) throws Exception
	{
		RelationshipDetailViewDTO relationshipDetailViewDTO = null;
		ConceptRelationship conceptRelationship = ConceptRelationshipDelegate.get(entityId);
		
		if (conceptRelationship == null)
		{
            throw new STSException("Association Id '"+ entityId +"' not found.");
		}
		else if(null != conceptRelationship)
		{
			relationshipDetailViewDTO = convertRelationship(conceptRelationship);
		}
		return relationshipDetailViewDTO;
	}
	
	private static RelationshipDetailViewDTO convertRelationship(ConceptRelationship conceptRelationship)
	{
		// Get all the details we need to send
		RelationshipDetailViewDTO relationshipDetailViewDTO = new RelationshipDetailViewDTO();
	
		relationshipDetailViewDTO.setRelationshipTypeName(conceptRelationship.getRelationshipType().getName());
		
		// Get the source concept
		Collection<Long> sourceSet = new HashSet<Long>();
		sourceSet.add(conceptRelationship.getSourceEntityId());
		Collection<CodedConcept> sourceConcepts = CodedConceptDelegate.get(sourceSet);
		CodedConcept sourceConcept = sourceConcepts.iterator().next();
		relationshipDetailViewDTO.setSourceConceptCode(sourceConcept.getCode());
		List<Designation> designations = DesignationDelegate.get(sourceConcept, conceptRelationship.getVersion());
		Designation preferredDesignation = DesignationDelegate.getPreferredDesignation(designations,  conceptRelationship.getVersion().getCodeSystem());
		relationshipDetailViewDTO.setSourceDesignationName(preferredDesignation.getName());
		
		// Get the target concept
		Collection<Long> targetSet = new HashSet<Long>();
		targetSet.add(conceptRelationship.getTargetEntityId());
		Collection<CodedConcept> targetConcepts = CodedConceptDelegate.get(targetSet);
		CodedConcept targetConcept = targetConcepts.iterator().next();
		relationshipDetailViewDTO.setTargetConceptCode(targetConcept.getCode());
		List<Designation> targetDesignations = DesignationDelegate.get(targetConcept, conceptRelationship.getVersion());
		Designation preferredTargetDesignation = DesignationDelegate.getPreferredDesignation(targetDesignations,  conceptRelationship.getVersion().getCodeSystem());
		relationshipDetailViewDTO.setTargetDesignationName(preferredTargetDesignation.getName());
		
		relationshipDetailViewDTO.setEntityId(conceptRelationship.getEntityId());
		relationshipDetailViewDTO.setRelationshipStatus(conceptRelationship.getActive());
		
		return relationshipDetailViewDTO;
	}
	
	public static RelationshipDetailListViewDTO getRelationships(Long codeSystemVuid, String versionName, String sourceConceptCode, 
			String targetConceptCode, String relationshipTypeName, Integer pageSize, Integer pageNumber) throws Exception
	{
		if(codeSystemVuid == null || versionName == null)
		{
			throw new STSException("CodeSystem and VersionName are required");
		}
		
		if(sourceConceptCode == null && targetConceptCode == null && relationshipTypeName == null)
		{
			throw new STSException("One of Source Concept Code, Target Concept Code or Association Type is required");
		}
		
		Version version = null;
		CodeSystem codeSystem = CodeSystemDelegate.getByVuid(codeSystemVuid);
		
		if (codeSystem == null)
		{
			throw new STSException("Cannot find Code System with VUID: " + codeSystemVuid);
		}
		if (versionName.equalsIgnoreCase(CURRENT_VERSION))
		{
			version = VersionDelegate.getRecent(codeSystem.getName(),false);
		}
		else
		{
			version = VersionDelegate.get(codeSystem.getId(), versionName);
		}

		if (version == null)
		{
			throw new STSException("Cannot find the version named '" + versionName+"'");
		}
		
		ConceptRelationshipListDTO conceptRelationshipListDto = ConceptRelationshipDelegate.getRelationships(codeSystem, version, sourceConceptCode, 
															targetConceptCode, relationshipTypeName, pageSize, pageNumber);
		
		RelationshipDetailListViewDTO relationshipDetailListViewDTO = new RelationshipDetailListViewDTO();
		relationshipDetailListViewDTO.setTotalNumberOfRecords(conceptRelationshipListDto.getTotalNumberOfRecords());
		for(ConceptRelationship conceptRelationship :conceptRelationshipListDto.getRelationships())
		{
			RelationshipDetailViewDTO relationshipDetailViewDTO = convertRelationship(conceptRelationship);
			relationshipDetailListViewDTO.getRelationshipDetailViewDTO().add(relationshipDetailViewDTO);
		}
		
		return relationshipDetailListViewDTO;
	}

	public static CodeSystemConceptsViewDTO getCodeSystemConcepts(Long codeSystemVuid, String versionName,
	        String designationNameFilter, String conceptCodeFilter, Boolean conceptStatusFilter,
	        Integer pageSize, Integer pageNumber) throws STSException
	{
		CodeSystem codeSystem = validateCodeSystemVuid(codeSystemVuid);
		Version version = validateCodeSystemVersion(versionName, codeSystem, true);

		CodedConceptListViewDTO codedConceptListView = CodedConceptDelegate.getCodedConcepts(codeSystem, version, designationNameFilter,
		        conceptCodeFilter, conceptStatusFilter, pageSize, pageNumber);
		List<Long> conceptEntityIds = new ArrayList<Long>();
		for (CodedConcept codedConcept : codedConceptListView.getCodedConcepts())
		{
			conceptEntityIds.add(codedConcept.getEntityId());
		}
		Map<Long, Collection<Designation>> designationMap = new HashMap<Long, Collection<Designation>>();
		if (conceptEntityIds.size() > 0)
		{
	        designationMap = DesignationDelegate.getDesignations(conceptEntityIds, version.getId());
		}

		return new CodeSystemConceptsViewDTO(codedConceptListView, designationMap);
	}

	public static TotalEntityListView getDomains(String domainName, Integer pageNumber, Integer pageSize) throws STSException
	{
		TotalEntityListView totalList = CodedConceptDelegate.getVhatDomains(domainName, pageNumber, pageSize);
		
		return totalList;
	}

	public static List<SubsetConfig> getSubsetNames(String domainName) throws Exception
	{
		return TerminologyConfigDelegate.getSubsets(domainName);
	}

	public static boolean isDomainMember(String conceptCode, Long domainVuid, Integer pageNumber, Integer pageSize) throws Exception
	{
		Concept concept = ConceptDelegate.getByCode(conceptCode);
		if (concept == null)
		{
			throw new STSNotFoundException("Cannot find concept code '"+conceptCode+"'");
		}
		List<Subset> domainSubsets = SubsetDelegate.getDomainSubsets(domainVuid, pageNumber, pageSize);

		List<Long> conceptEntityIds = new ArrayList<Long>();
		conceptEntityIds.add(concept.getEntityId());
		Map<String, List<Long>> allSubsets = SubsetDelegate.getSubsets(conceptEntityIds, HibernateSessionFactory.LAST_FINALIZED_VERSION_ID);

		for (String subsetName : allSubsets.keySet())
		{
			for (Subset domainSubset : domainSubsets)
			{
				if (domainSubset.getName().equals(subsetName))
				{
					return true;
				}
			}
		}
		return false;
	}
	
    public static RelationshipType getType(String relationshipTypeName)throws Exception
    {
    	RelationshipType relationshipType = ConceptRelationshipDelegate.getType(relationshipTypeName);
    	if(relationshipType == null)
    	{
    		throw new STSException("AssociationType is not found");
    	}
    	return relationshipType;
    }
    
    public static RelationshipTypeListDTO getAllTypes(String relationshipTypeName, Long codeSystemVuid, String versionName, Integer pageSize, Integer pageNumber) throws STSException
    {
		CodeSystem codeSystem = null;
		Version version = null;

		if(codeSystemVuid != null)
		{
			codeSystem = CodeSystemDelegate.getByVuid(codeSystemVuid);
			// Wrong VUID
			if(codeSystem == null)
			{
				throw new STSException("Cannot find the code system with VUID '" + codeSystemVuid + "'");
			}
		}
		if(codeSystemVuid == null && versionName !=null)
		{
			throw new STSException("Code system VUID required with the version name: " + versionName);
		}
		if(codeSystem != null && versionName != null)
		{
			if (versionName.equalsIgnoreCase(CURRENT_VERSION))
			{
				version = VersionDelegate.getRecent(codeSystem.getName(),false);
			}
			else
			{
				version = VersionDelegate.get(codeSystem.getId(), versionName);
			}
			
			if(version == null)
			{
				throw new STSException("Cannot find the version named '" + versionName + "'");
			}
		}
    	return ConceptRelationshipDelegate.getAllTypes(relationshipTypeName, codeSystem, version, pageSize,pageNumber);
    }
	
    public static boolean hasRelationship(Long codeSystemVuid, String versionName, String childConceptCode, String parentConceptCode, String relationshipTypeName) throws STSException
    {
    	if (codeSystemVuid == null)
    	{
            throw new STSException("Code System VUID is a required parameter.");
    	}
        CodeSystem codeSystem = CodeSystemDelegate.getByVuid(codeSystemVuid);
        if (codeSystem == null)
        {
            throw new STSException("Code System VUID '" + codeSystemVuid + "' is invalid.");
        }
        if (!codeSystem.getName().equalsIgnoreCase(HibernateSessionFactory.VHAT_NAME))
        {
            throw new STSException("CodeSystem VUID is not VHAT's"); 
        }
        
        if (versionName == null)
        {
        	throw new STSException("Version name is a required parameter.");
        }
        if (StringUtils.isEmpty(childConceptCode))
        {
        	throw new STSException("Child concept code is a required parameter.");
        }
        if (StringUtils.isEmpty(parentConceptCode))
        {
        	throw new STSException("Parent concept code is a required parameter.");
        }
        
        Version version;
        if (versionName.equalsIgnoreCase(CURRENT_VERSION))
		{
			version = VersionDelegate.getRecent(codeSystem.getName(),false);
		}
		else
		{
			version = VersionDelegate.get(codeSystem.getId(), versionName);
		}
        if (version == null)
        {
            throw new STSException("The version named '"+versionName+"' does not exist!");
        }
        Map<String, Concept> conceptMap = new HashMap<String, Concept>(); 
        Collection<String> codes = new HashSet<String>();
        
        codes.add(childConceptCode);
        codes.add(parentConceptCode);
        List<Concept> concepts = ConceptDelegate.get(codeSystem, codes);
        for (Concept concept : concepts)
        {
            conceptMap.put(concept.getCode(), concept);
        }
        Concept sourceConcept = conceptMap.get(childConceptCode);
        if (sourceConcept == null)
        {
            throw new STSException("Cannot find child concept code: "+childConceptCode);
        }
        Concept targetConcept = conceptMap.get(parentConceptCode);
        if (targetConcept == null)
        {
            throw new STSException("Cannot find parent concept code: "+parentConceptCode);
        }
        
        return ConceptRelationshipDelegate.isConceptSubsumedRelationship(version, sourceConcept.getEntityId(), targetConcept.getEntityId(), relationshipTypeName);
    }

    public static Collection<String> getRelationshipPath(Long codeSystemVuid, String versionName, String sourceConceptCode, String targetConceptCode, String relationshipTypeName) throws STSException
    {
		CodeSystem codeSystem = validateCodeSystemVuid(codeSystemVuid);
		Version version = validateCodeSystemVersion(versionName, codeSystem, true);

        Map<String, Concept> conceptMap = new HashMap<String, Concept>(); 
        Collection<String> codes = new HashSet<String>();
        
        codes.add(sourceConceptCode);
        codes.add(targetConceptCode);
        List<Concept> concepts = ConceptDelegate.get(codeSystem, codes);
        for (Concept concept : concepts)
        {
            conceptMap.put(concept.getCode(), concept);
        }
        Concept sourceConcept = conceptMap.get(sourceConceptCode);
        if (sourceConcept == null)
        {
            throw new STSException("Cannot find source concept code: "+sourceConceptCode);
        }
        Concept targetConcept = conceptMap.get(targetConceptCode);
        if (targetConcept == null)
        {
            throw new STSException("Cannot find target concept code: "+targetConceptCode);
        }
        RelationshipType relationshipType = ConceptRelationshipDelegate.getType(relationshipTypeName);
        if (relationshipType == null)
        {
        	// use the word association instead of relationship for web services context
            throw new STSException("Cannot find association type name: "+relationshipTypeName);
        }
        Collection<String> result = ConceptRelationshipDelegate.getPath(codeSystem.getId(), version.getId(), sourceConceptCode, targetConceptCode, relationshipType.getId());
        
        return result;
    }

    public static SubsetContentsListView getSubsetContents(Long subsetVuid,
            String versionName, String designationName,
            String membershipStatus, Integer pageSize, Integer pageNumber) throws STSException
    {
    	Version version = getVhatVersion(versionName, true);

        if (subsetVuid == null)
        {
            throw new STSException("Subset vuid is a required parameter.");
        }
        Subset subset = SubsetDelegate.getByVuid(subsetVuid);
        if (subset == null)
        {
            throw new STSException("Subset vuid '"+subsetVuid+"' does not exist!");
        }
        SubsetContentsListView subsetContents = DesignationDelegate.getBySubset(subset.getEntityId(), version.getId(), designationName, membershipStatus, pageSize, pageNumber);

        return subsetContents;
    }

	private static Version getVhatVersion(String versionName, boolean isRequired) throws STSException
	{
		Version version;
		if (isRequired && StringUtils.isEmpty(versionName))
    	{
        	throw new STSException("Version name is a required parameter.");
    	}
    	if (versionName.equalsIgnoreCase(CURRENT_VERSION))
        {
        	version = VersionDelegate.getRecent(HibernateSessionFactory.VHAT_NAME, false);
        }
        else
        {
        	version = VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, versionName);
        }
        if (version == null)
        {
            throw new STSException("The version named '"+versionName+"' does not exist!");
        }
		return version;
	}

    public static SubsetVersionDTO getSubsetByVuid(Long subsetVuid, String versionName) throws STSException
    {
        Version version = null;
        if (versionName.equalsIgnoreCase(CURRENT_VERSION))
        {
        	version = VersionDelegate.getRecent(HibernateSessionFactory.VHAT_NAME, false);
        }
        else
        {
        	version = VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, versionName);
        }
        Subset subset = null;
        if (version != null && version.getId() < HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            subset = SubsetDelegate.getByVuid(subsetVuid, version.getName());
        }
        if (subset == null)
        {
            throw new STSException("Value Set VUID '"+subsetVuid+"' with version name '"+versionName+"' could not be found.");
        }
        // subsets don't belong to VHAT but webservices want's the VUID of the codesystem of the version.
        SubsetVersionDTO subsetVersionDTO = new SubsetVersionDTO(subset, version);
        return subsetVersionDTO;
    }
	public static boolean isMember(String versionName, Long domainVuid, String conceptCode) throws Exception
	{
		Version version = getVhatVersion(versionName, true);
		
		Concept concept = ConceptDelegate.get(conceptCode, version.getId());
		if (concept==null)
		{
            throw new STSException("Cannot find concept code '"+conceptCode+"' with version '"+version.getName()+"'.");
		}

		CodedConcept domain = CodedConceptDelegate.getDomainByVuid(domainVuid);
		if (domain == null)
		{
            throw new STSException("Domain VUID is not valid");
		}

		domain = CodedConceptDelegate.get(version, domain.getCode());
		if (domain == null)
		{
            throw new STSException("Cannot find Domain with VUID '"+domainVuid+"' with version '"+version.getName()+"'.");
		}
		
		return isDomainMember(conceptCode, domainVuid, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
	}
	
	public static List<Subset> getSubsets(Long domainVuid, int pageNumber, int pageSize)
	{
		return SubsetDelegate.getDomainSubsets(domainVuid, pageNumber, pageSize);
	}

	public static TotalEntityListView getDomainConcepts(Long domainVuid, Integer pageNumber, Integer pageSize) throws STSException
	{
		CodedConcept codedConcept = CodedConceptDelegate.getDomainByVuid(domainVuid);
		if (codedConcept == null)
		{
			throw new STSException("VUID is not a valid domain.");
		}
		
		return CodedConceptDelegate.getDomainConcepts(domainVuid, pageNumber, pageSize);
	}

	public static Designation getDomainName(long domainVuid)
	{
		return DesignationDelegate.getPreferredDesignationByVuid(domainVuid);
	}

	private static Version validateCodeSystemVersion(String versionName, CodeSystem codeSystem, boolean isRequired) throws STSException
	{
		Version version;
		if (isRequired && (versionName == null || versionName.equals("")))
		{
			throw new STSException("Version name is a required parameter.");
		}
		
		if (versionName.equalsIgnoreCase(CURRENT_VERSION))
		{
			version = VersionDelegate.getRecent(codeSystem.getName(), false);
		}
		else
		{
			version = VersionDelegate.get(codeSystem.getId(), versionName);
		}

		if (version == null)
		{
			throw new STSException("Code system '"+codeSystem.getName()+"' with version name '" + versionName+ "' not found.");
		}
		return version;
	}

	private static CodeSystem validateCodeSystemVuid(Long codeSystemVuid) throws STSException
	{
		CodeSystem codeSystem = null;
		if (codeSystemVuid == null )
		{
			throw new STSException("Code system VUID is a required parameter.");
		}
		else if ((codeSystem=CodeSystemDelegate.getByVuid(codeSystemVuid)) == null)
		{
			throw new STSException("Code system VUID '" + codeSystemVuid + "' not found.");
		}
			
		return codeSystem;
	}
	
    public static UsageContextListViewDTO getUsageContext(String usageContextName, Integer pageSize, Integer pageNumber) throws STSException
    {
    	UsageContextListViewDTO usageContextListViewDTO = new UsageContextListViewDTO();
    	
    	ConceptRelationshipConceptListDTO conceptRelationshipConceptListDTO = ConceptRelationshipDelegate.getChildren(HibernateSessionFactory.VHAT_NAME, usageContextName, pageSize, pageNumber);
        if (!conceptRelationshipConceptListDTO.getConceptRelationshipDTOs().isEmpty())
        {
            Collection<String> conceptCodes = new ArrayList<String>(); 
            CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
            for (ConceptRelationshipDTO conceptRelationshipDTO : conceptRelationshipConceptListDTO.getConceptRelationshipDTOs())
            {
                conceptCodes.add(conceptRelationshipDTO.getConceptCode());
            }
            Map<String, Designation> designationMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(codeSystem, HibernateSessionFactory.AUTHORING_VERSION_ID, conceptCodes);
            for (ConceptRelationshipDTO conceptRelationshipDTO : conceptRelationshipConceptListDTO.getConceptRelationshipDTOs())
            {
                Designation designation = designationMap.get(conceptRelationshipDTO.getConceptCode());
                if (designation == null)
                {
                    throw new STSException("Could not get '"+codeSystem.getPreferredDesignationType().getName()+"' for concept: "+conceptRelationshipDTO.getConceptCode());
                }
                conceptRelationshipDTO.setName(designation.getName());
                UsageContextDetailViewDTO usageContextDetailViewDTO = new UsageContextDetailViewDTO();
                usageContextDetailViewDTO.setVuid(conceptRelationshipDTO.getConceptCode());
                usageContextDetailViewDTO.setName(designation.getName());
    			usageContextListViewDTO.getUsageContextDetailViewDTO().add(usageContextDetailViewDTO);
            }
        }
        usageContextListViewDTO.setTotalNumberOfRecords(conceptRelationshipConceptListDTO.getTotalNumberOfRecords());

        return usageContextListViewDTO;
    }
    
    public static RelatedConceptsDTO getUsageContext(Long vuid) throws STSException
    {
    	ConceptRelationshipConceptListDTO conceptRelationshipConceptListDTO = ConceptRelationshipDelegate.getChildren(HibernateSessionFactory.VHAT_NAME, null, null, null);
        Set<String> childrenRelationshipMap = new HashSet<String>();
        for (ConceptRelationshipDTO conceptRelationshipDTO : conceptRelationshipConceptListDTO.getConceptRelationshipDTOs())
		{
        	childrenRelationshipMap.add(conceptRelationshipDTO.getName());
		}
        Concept concept = ConceptDelegate.getByVuid(vuid);
        if ( concept == null)
        {
            throw new STSException("Concept with vuid '"+vuid+"' does not exist!");
        }
        
        if (!childrenRelationshipMap.contains(concept.getName()))
        {
        	throw new STSException("Usage context: "+vuid+ " is not valid!");
        }
        Collection<CodedConceptDesignationDTO> codedConceptDesignationDTOs = new ArrayList<CodedConceptDesignationDTO>();
        CodedConceptListDTO concepts = CodedConceptDao.getVhatDomains(vuid, null, null, null);

        Collection<String> conceptCodes = new ArrayList<String>(); 
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        for (CodedConcept childConcept : concepts.getCodedConcepts())
        {
            conceptCodes.add(childConcept.getCode());
            CodedConceptDesignationDTO conceptDTO = new CodedConceptDesignationDTO(childConcept, null);
            codedConceptDesignationDTOs.add(conceptDTO);
        }
        conceptCodes.add(concept.getCode());
        Map<String, Designation> designationMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(codeSystem, HibernateSessionFactory.AUTHORING_VERSION_ID, conceptCodes);
        for (CodedConceptDesignationDTO codedConceptDesignationDTO : codedConceptDesignationDTOs)
        {
            Designation designation = designationMap.get(codedConceptDesignationDTO.getConcept().getCode());
            if (designation == null)
            {
                throw new STSException("Could not get '"+codeSystem.getPreferredDesignationType().getName()+"' for concept: "+codedConceptDesignationDTO.getConcept().getCode());
            }
            codedConceptDesignationDTO.setDesignation(designation);
        }
        Designation preferredDesignation = designationMap.get(concept.getCode());
        if ( preferredDesignation == null)
        {
            throw new STSException("Concept with vuid '"+concept.getVuid()+"' does not have a '"+codeSystem.getPreferredDesignationType().getName()+"'");
        }
        RelatedConceptsDTO results = new RelatedConceptsDTO(preferredDesignation.getName(), vuid, codedConceptDesignationDTOs);
        return results;
    }
    
    public static MapSetDetailListDTO getMapSets(String mapSetName, Long sourceConceptCodeSystemVuid, String sourceCodeSystemVersionName,
            Long targetConceptCodeSystemVuid, String targetCodeSystemVersionName, Boolean mapSetStatus, Integer pageSize, Integer pageNumber) throws STSException
    {
    	List<Long> mapSetsNotAcccessibleVuidList = TerminologyConfigDelegate.getMapSetsNotAccessibleVuidList();
    	MapSetDetailListDTO mapSetDetailList = MapSetDelegate.getFilteredVersions(mapSetName, sourceConceptCodeSystemVuid, sourceCodeSystemVersionName,
                targetConceptCodeSystemVuid, targetCodeSystemVersionName, mapSetStatus, mapSetsNotAcccessibleVuidList, pageSize, pageNumber);

		// get all entity ids of the map sets
		HashSet<Long> mapSetEntityIdSet = new HashSet<Long>();
		for (MapSetDetailDTO mapSetDetailDTO : mapSetDetailList.getMapSetDetails())
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

		for (MapSetDetailDTO mapSetDetailDTO : mapSetDetailList.getMapSetDetails())
		{
			MapSetConfig mapSetConfig = getMapSetConfig(mapSetDetailDTO.getMapSetVuid());
			mapSetDetailDTO.setSourceValueType(mapSetConfig.getSourceType());
			mapSetDetailDTO.setTargetValueType(mapSetConfig.getTargetType());
			
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
		}

		return mapSetDetailList;
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
	
    public static MapEntryCacheListDTO getMapEntries(int callType, Long mapSetVuid, String mapSetVersionName,
    		String sourceDesignationTypeName, String targetDesignationTypeName,
    		Collection<String> sourceValues, String sourceValueType, Collection<String> targetValues, String targetValueType,
            String sourcePreferredDesignationNameFilter, String targetPreferredDesignationNameFilter,
            Integer mapEntrySequence, Boolean mapEntryStatus, Integer pageSize, Integer pageNumber) throws STSException
    {
    	// verify that there is a valid versioned map set for the given VUID
    	MapSet mapSet = MapSetDelegate.getByVuid(mapSetVuid, HibernateSessionFactory.AUTHORING_VERSION_ID - 1);
    	if (mapSet == null)
    	{
    		throw new STSException("Map Set VUID: " + mapSetVuid + " does not exist.");
    	}
    	
    	Version mapSetVersion = null;
    	if (mapSetVersionName.equalsIgnoreCase(CURRENT_VERSION))
    	{
    		// get the map set version using its VUID and get the most recent finalized version name for that map set
    		mapSetVersion = MapSetDelegate.getCurrentVersionByVuid(mapSetVuid);
    		mapSetVersionName = mapSetVersion.getName();
    	}

    	mapSetVersion = getCachedVersion(mapSetVersionName);
    	mapSet = getCachedMapSet(mapSetVuid, mapSetVersion);
    	
    	// get source and target designation type ids
    	Long sourceDesignationTypeId = null;
    	if (sourceDesignationTypeName != null)  // if null the don't filter by source designation type
    	{
            sourceDesignationTypeId = getDesignationTypeId("Source", sourceDesignationTypeName, mapSet);
    	}
        Long targetDesignationTypeId = getDesignationTypeId("Target", targetDesignationTypeName, mapSet);

        // update map entry cache for the mapset and version
        Transaction tx = null;
        Session session = HibernateSessionFactory.currentSession();
        session.clear();
        tx = session.beginTransaction();
        MapEntryCacheDelegate.updateMapEntryCache(mapSet.getEntityId(), mapSetVersion.getId());
        tx.commit();
        
        MapEntryCacheListDTO mapEntryCacheListDTO = MapEntryCacheDelegate.getEntries(callType,
        		mapSet.getEntityId(), mapSetVersion.getId(),
        		sourceDesignationTypeId, targetDesignationTypeId,
                sourceValues, sourceValueType, targetValues, targetValueType, 
                sourcePreferredDesignationNameFilter, targetPreferredDesignationNameFilter,
                mapEntrySequence, mapEntryStatus, pageSize, pageNumber);
        
        return mapEntryCacheListDTO;
    }
    
    private static long getDesignationTypeId(String context, String typeName, MapSet mapSet) throws STSException
    {
        DesignationType designationType;
        if (typeName != null)
        {
        	try
        	{
            	designationType = getCachedDesignationType(typeName);
        	}
        	catch (STSException e)
        	{
           		throw new STSException(context + " Designation Type Name '"+typeName+"' does not exist.");
        	}
        }
        else
        {
        	long versionId = (context.equalsIgnoreCase("Source")) ? mapSet.getSourceVersionId() : mapSet.getTargetVersionId();
        	String versionKey = DESIGNATION_TYPE_NAME_KEY_PREFIX + mapSet.getVuid() + " " + context + " id " + versionId;
        	designationType = (DesignationType) StringKeyObjectMap.getInstance().getObject(versionKey);
        	if (designationType == null)
        	{
            	Version version = VersionDelegate.getByVersionId(versionId);
            	designationType = version.getCodeSystem().getPreferredDesignationType();
            	StringKeyObjectMap.getInstance().putObject(versionKey, designationType);
            	// create another hashmap entry using the designation id as the key for the transfer object to retrieve
            	String typeIdKey = DESIGNATION_TYPE_ID_KEY_PREFIX + designationType.getId(); 
            	StringKeyObjectMap.getInstance().putObject(typeIdKey, designationType);
        	}
        }
    	
        return designationType.getId();
    }

	public static MapSetConfig getMapSetConfig(long vuid) throws STSException
	{
		return TerminologyConfigDelegate.getMapSet(vuid);
	}

	public static SubsetListViewDTO getSubsetsByNameFilter(String nameFilter, String status, Integer pageSize, Integer pageNumber)
	{
		return SubsetDelegate.getSubsetsByNameFilter(nameFilter, status, pageSize, pageNumber);
	}

	public static SubsetCountDTO getSubsetRelationshipCount(long subsetEntityId, String versionName)
	{
        if (versionName.equalsIgnoreCase(CURRENT_VERSION))
        {
        	CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        	Version version = VersionDelegate.getRecent(codeSystem.getName(), false);
        	versionName = version.getName();
        }
        
		SubsetCountDTO subsetCount = SubsetRelationshipDelegate.getCount(subsetEntityId, versionName);
		subsetCount.setVersionName(VersionDelegate.getByVersionId(subsetCount.getVersionId()).getName());
		
		return subsetCount;
   
	}

	public static Map<Long, Collection<String>> getSubsetVersions(Collection<Long> subsetEntityIds, boolean includeAuthoring)
	{
		return SubsetDelegate.getVersions(subsetEntityIds, includeAuthoring);
	}

	public static CodedConcept getDomainByVuid(Long domainVuid) throws STSException
	{
		return CodedConceptDelegate.getDomainByVuid(domainVuid);
	}
	
	public static boolean isSubsetMember(String codeSystemVersionName, Long designationVuid, Long valueSetVuid)throws STSException
	{
		Version version = null;
        if (codeSystemVersionName.equalsIgnoreCase(CURRENT_VERSION))
        {
        	version = VersionDelegate.getRecent(HibernateSessionFactory.VHAT_NAME, false);
        }
        else
        {
        	version = VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, codeSystemVersionName);
        }
        
        if (version == null)
		{
			throw new STSException("Cannot find the version named '" + codeSystemVersionName +"'");
		}
        
        Subset subset = SubsetDelegate.getByVuid(valueSetVuid);
        if(subset == null)
        {
        	throw new STSException("Value Set VUID '"+valueSetVuid+"' could not be found.");
        }
        
        Designation designation = DesignationDelegate.getByVuid(designationVuid);
        if(designation == null)
        {
        	throw new STSException("Designation VUID '"+designationVuid+"' could not be found.");
        }        
        
        return SubsetDelegate.isSubsetMember(subset.getEntityId(), designation.getEntityId(), version);
	}
	
	public static Version getCachedVersion(String versionName) throws STSException
	{
    	String versionNameKey = VERSION_NAME_KEY_PREFIX + versionName; 
    	Version version = (Version) StringKeyObjectMap.getInstance().getObject(versionNameKey);

    	if (version == null)
    	{
       		version = VersionDelegate.get(HibernateSessionFactory.VHAT_NAME, versionName);
            if (version == null)
            {
                throw new STSException("Version name '"+versionName+"' does not exist.");
            }
        	StringKeyObjectMap.getInstance().putObject(versionNameKey, version);
        	String versionIdKey = VERSION_ID_KEY_PREFIX + version.getId(); 
        	StringKeyObjectMap.getInstance().putObject(versionIdKey, version);
    	}
    	
    	return version;
	}
	
	public static Version getCachedVersion(long versionId) throws STSException
	{
    	String versionIdKey = VERSION_ID_KEY_PREFIX + versionId; 
    	Version version = (Version) StringKeyObjectMap.getInstance().getObject(versionIdKey);

    	if (version == null)
    	{
       		version = VersionDelegate.getByVersionId(versionId);
            if (version == null)
            {
                throw new STSException("Version id '"+versionId+"' does not exist.");
            }
        	StringKeyObjectMap.getInstance().putObject(versionIdKey, version);
        	String versionNameKey = VERSION_NAME_KEY_PREFIX + version.getName(); 
        	StringKeyObjectMap.getInstance().putObject(versionNameKey, version);
    	}
    	
    	return version;
	}
	
	public static MapSet getCachedMapSet(long mapSetVuid, Version version) throws STSException
	{
        // get MapSet using the VUID and the versionId
        String mapSetKey = MAP_SET_KEY_PREFIX + mapSetVuid + '-' + version.getId(); 
    	MapSet mapSet = (MapSet) StringKeyObjectMap.getInstance().getObject(mapSetKey);
    	if (mapSet == null)
    	{
            mapSet = MapSetDelegate.getByVuid(mapSetVuid, version.getId());
            if (mapSet == null)
            {
                throw new STSException("Map Set VUID '" + mapSetVuid + "' with version name '" + version.getName() + "' does not exist.");
            }
            StringKeyObjectMap.getInstance().putObject(mapSetKey, mapSet);
    	}
    	
    	return mapSet;
	}
	
	public static DesignationType getCachedDesignationType(String typeName) throws STSException
	{
    	String typeNameKey = DESIGNATION_TYPE_NAME_KEY_PREFIX + typeName; 
    	DesignationType designationType = (DesignationType) StringKeyObjectMap.getInstance().getObject(typeNameKey);
    	if (designationType == null)
    	{
        	designationType = DesignationDelegate.getType(typeName);
        	if (designationType == null)
        	{
        		throw new STSException("Designation Type Name '"+typeName+"' does not exist.");
        	}
        	StringKeyObjectMap.getInstance().putObject(typeNameKey, designationType);
        	
        	// create another hashmap entry using the designation id as the key
        	String typeIdKey = DESIGNATION_TYPE_ID_KEY_PREFIX + designationType.getId(); 
        	StringKeyObjectMap.getInstance().putObject(typeIdKey, designationType);
    	}
		
    	return designationType;
	}

	public static DesignationType getCachedDesignationType(long typeId) throws STSException
	{
    	String typeIdKey = TerminologyDelegate.DESIGNATION_TYPE_ID_KEY_PREFIX + typeId; 
    	DesignationType designationType = (DesignationType) StringKeyObjectMap.getInstance().getObject(typeIdKey);
    	if  (designationType == null)
    	{
    		designationType = DesignationDelegate.getType(typeId);
        	if (designationType == null)
        	{
        		throw new STSException("Designation Type Id '"+typeId+"' does not exist.");
        	}
    		StringKeyObjectMap.getInstance().putObject(typeIdKey, designationType);
    		
        	// create another hashmap entry using the designation name
        	String typeNameKey = DESIGNATION_TYPE_NAME_KEY_PREFIX + designationType.getName(); 
        	StringKeyObjectMap.getInstance().putObject(typeNameKey, designationType);
    	}

    	return designationType;
	}
}
