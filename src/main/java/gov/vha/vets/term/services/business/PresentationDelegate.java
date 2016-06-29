package gov.vha.vets.term.services.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.vha.vets.term.services.dao.PresentationDao;
import gov.vha.vets.term.services.dto.ConceptSummaryDTO;
import gov.vha.vets.term.services.dto.SubsetDetailsDTO;
import gov.vha.vets.term.services.dto.change.ConceptChangesDTO;
import gov.vha.vets.term.services.dto.change.DesignationChangeDTO;
import gov.vha.vets.term.services.dto.change.RelationshipChangeDTO;
import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

public class PresentationDelegate
{
    public static ConceptChangesDTO getConceptChangesDetail(long conceptEntityId)
    {
        return getConceptChangesDetail(conceptEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
    }
	public static ConceptChangesDTO getConceptChangesDetail(long conceptEntityId, long versionId)
	{
		HibernateSessionFactory.currentSession().clear();
		
		ConceptChangesDTO changes = new ConceptChangesDTO();
        Version version = VersionDelegate.getByVersionId(versionId);
		if (versionId < HibernateSessionFactory.AUTHORING_VERSION_ID)
		{
			changes.setVersionId(versionId);
			changes.setVersionName(version.getName());
		}
		changes.setConcept(ConceptDelegate.getConceptChange(conceptEntityId, versionId));
		changes.setProperties(PropertyDelegate.getPropertyChanges(conceptEntityId, versionId));
        Collections.sort(changes.getProperties());
		changes.setDesignations(DesignationDelegate.getDesignationChanges(conceptEntityId, versionId));
		Collections.sort(changes.getDesignations());
		for (DesignationChangeDTO designationChangeDTO : changes.getDesignations())
		{
	        Collections.sort(designationChangeDTO.getProperties());
	        Collections.sort(designationChangeDTO.getSubsetRelationships());
		}
		
		// get all the relationships both inverse and normal
		List<RelationshipChangeDTO> relationshipChanges = ConceptRelationshipDelegate.getRelationshipChanges(conceptEntityId, false, versionId);
        Collections.sort(relationshipChanges);
		List<RelationshipChangeDTO> inverseRelationshipChanges = ConceptRelationshipDelegate.getRelationshipChanges(conceptEntityId, true, versionId);
        Collections.sort(inverseRelationshipChanges);

        Set<Long> conceptEntityIds = new HashSet<Long>();
        setConceptEntityIds(relationshipChanges, conceptEntityIds, false);
        setConceptEntityIds(inverseRelationshipChanges, conceptEntityIds, true);
        conceptEntityIds.add(conceptEntityId);
        
        Map<Long, Designation> designationMap = null;
        if (conceptEntityIds.size() > 0)
        {
            designationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(version.getCodeSystem(), versionId, conceptEntityIds);
        }
        processPreferredName(relationshipChanges, designationMap, false);
        processPreferredName(inverseRelationshipChanges, designationMap, true);
        

		// now turn the 2 buckets of information into 4
		List<RelationshipChangeDTO> parentRelationships = new ArrayList<RelationshipChangeDTO>();
		List<RelationshipChangeDTO> childRelationships = new ArrayList<RelationshipChangeDTO>();
		List<RelationshipChangeDTO> inverseRelationships = new ArrayList<RelationshipChangeDTO>();
		List<RelationshipChangeDTO> normalRelationships = new ArrayList<RelationshipChangeDTO>();

		// divide into child and normal relationships
		for (RelationshipChangeDTO change : relationshipChanges)
		{
			ConceptRelationship conceptRelationship = (ConceptRelationship) change.getRecent();
			if (RelationshipType.HAS_PARENT.equals(conceptRelationship.getRelationshipType().getName()))
			{
				parentRelationships.add(change);
			}
			else
			{
				normalRelationships.add(change);
			}
		}

		// divide into Parent and inverse relationships
		for (RelationshipChangeDTO change : inverseRelationshipChanges)
		{
			ConceptRelationship conceptRelationship = (ConceptRelationship) change.getRecent();
			if (conceptRelationship == null)
			{
	            conceptRelationship = (ConceptRelationship) change.getPrevious();
			}

			if (RelationshipType.HAS_PARENT.equals(conceptRelationship.getRelationshipType().getName()))
			{
				childRelationships.add(change);
			}
			else
			{
				inverseRelationships.add(change);
			}
		}
		// fill the buckets
		changes.setChildRelationships(childRelationships);
		changes.setParentRelationships(parentRelationships);
		changes.setRelationships(normalRelationships);
		changes.setInverseRelationships(inverseRelationships);
		Designation designation = designationMap.get(conceptEntityId);
		if (designation != null)
		{
		    changes.getConcept().setPreferredName(designation.getName());
		}

		return changes;
	}

    private static void setConceptEntityIds(List<RelationshipChangeDTO> relationshipChanges, Set<Long> conceptEntityIds, boolean inverse)
    {
        for (RelationshipChangeDTO relationshipChangeDTO : relationshipChanges)
        {
            ConceptRelationship relationship;
            if (relationshipChangeDTO.getRecent() != null)
            {
                relationship = (ConceptRelationship)relationshipChangeDTO.getRecent();
                Long entityId = (inverse) ?  relationship.getSourceEntityId() : relationship.getTargetEntityId();
                conceptEntityIds.add(entityId);
            }
            if (relationshipChangeDTO.getPrevious() != null)
            {
                relationship = (ConceptRelationship)relationshipChangeDTO.getPrevious();
                Long entityId = (inverse) ?  relationship.getSourceEntityId() : relationship.getTargetEntityId();
                conceptEntityIds.add(entityId);
            }
        }
    }
    private static void processPreferredName(List<RelationshipChangeDTO> relationshipChanges, Map<Long, Designation> designationMap, boolean inverse)
    {
        for (RelationshipChangeDTO relationshipChangeDTO : relationshipChanges)
        {
            ConceptRelationship relationship = null;
            if (relationshipChangeDTO.getRecentAssociatedConceptChange() != null)
            {
                relationship = (ConceptRelationship)relationshipChangeDTO.getRecent();
                Long entityId = (inverse) ?  relationship.getSourceEntityId() : relationship.getTargetEntityId();
                String preferredName = designationMap.get(entityId).getName();
                relationshipChangeDTO.getRecentAssociatedConceptChange().setPreferredName(preferredName);
            }
            if (relationshipChangeDTO.getPreviousAssociatedConceptChange() != null)
            {
                relationship = (ConceptRelationship)relationshipChangeDTO.getPrevious();
                Long entityId = (inverse) ?  relationship.getSourceEntityId() : relationship.getTargetEntityId();
                String preferredName = designationMap.get(entityId).getName();
                relationshipChangeDTO.getPreviousAssociatedConceptChange().setPreferredName(preferredName);
            }
        }
    }

	public static List<ConceptSummaryDTO> getConceptSummaries(List<String> subsets, List<String> states)
	{
		HibernateSessionFactory.currentSession().clear();
		
		List<ConceptSummaryDTO> conceptSummaryDTOList = PresentationDao.getConceptSummary(subsets, states);
		populatePreferredName(conceptSummaryDTOList);

		return conceptSummaryDTOList;
	}

	public static List<ConceptSummaryDTO> getConceptSummariesNotInDomains(List<String> states) throws STSException
	{
		HibernateSessionFactory.currentSession().clear();
		
		// get a list of subsets
		List<String> subsets = new ArrayList<String>();
		List<DomainConfig> domainConfigs = TerminologyConfigDelegate.getDomains();
		for (DomainConfig config : domainConfigs)
		{
			List<SubsetConfig> subsetConfigs = config.getSubsets();
			for (SubsetConfig subsetConfig : subsetConfigs)
			{
				subsets.add(subsetConfig.getName());
			}
		}

		List<ConceptSummaryDTO> conceptSummaryDTOList =  PresentationDao.getConceptSummariesNotInDomains(subsets, states);
		populatePreferredName(conceptSummaryDTOList);
		
		return conceptSummaryDTOList;
	}
	
	private static void populatePreferredName(List<ConceptSummaryDTO> conceptSummaryDTOList)
	{
	    if (conceptSummaryDTOList.size() > 0)
	    {
	        List<Long> conceptEntityIdList = new ArrayList<Long>();
	        for (ConceptSummaryDTO conceptSummaryDTO : conceptSummaryDTOList)
	        {
	            conceptEntityIdList.add(conceptSummaryDTO.getConceptEntityId());
	        }
	        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
	        Map<Long, Designation> designationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(codeSystem,
	                HibernateSessionFactory.AUTHORING_VERSION_ID, conceptEntityIdList);
	        for (ConceptSummaryDTO conceptSummaryDTO : conceptSummaryDTOList)
	        {
	            Designation designation = designationMap.get(conceptSummaryDTO.getConceptEntityId());
	            if (designation != null)
	            {
	                conceptSummaryDTO.setDesignationName(designation.getName());
	            }
	        }
	    }
	}

	public static List<ConceptSummaryDTO> getConceptSummariesInMapsets(List<String> selectedStateNameList) throws STSException
	{
		HibernateSessionFactory.currentSession().clear();
		return PresentationDao.getConceptSummariesInMapsets(selectedStateNameList);
	}

	public static void updateConceptState(String[] conceptEntityIds, State state) throws STSException
	{
		Session session = HibernateSessionFactory.currentSession();
		session.clear();
		Transaction tx = session.beginTransaction();
		try
		{
			List<Long> conceptEntityList = new ArrayList<Long>();
			for (String conceptEntityId : conceptEntityIds)
			{
				conceptEntityList.add(Long.valueOf(conceptEntityId));
			}
			ConceptStateDelegate.update(conceptEntityList, state);
			tx.commit();
			session.clear();
		}
		catch (Exception ex)
		{
			tx.rollback();
			throw new STSException(ex);
		}
		finally
		{
			HibernateSessionFactory.disconnect();
		}
	}

	public static void updateConceptState(long conceptEntityId, State state) throws STSException
	{
		Session session = HibernateSessionFactory.currentSession();
		session.clear();
		Transaction tx = session.beginTransaction();
		try
		{
			ConceptStateDelegate.createOrUpdate(conceptEntityId, state);
			tx.commit();
			session.clear();
		}
		catch (Exception ex)
		{
			tx.rollback();
			throw new STSException(ex);
		}
		finally
		{
			HibernateSessionFactory.disconnect();
		}
	}
	
	public static ConceptState getConceptState(long conceptEntityId)
	{
		HibernateSessionFactory.currentSession().clear();
		
		ConceptState conceptState = ConceptStateDelegate.get(conceptEntityId);
		
		return conceptState;
	}
	
	public static SubsetDetailsDTO getSubsetDesignations(long subsetEntityId)
	{
		return getSubsetDesignations(subsetEntityId, HibernateSessionFactory.AUTHORING_VERSION_ID);
	}

	public static SubsetDetailsDTO getSubsetDesignations(long subsetEntityId, long versionId)
	{
		HibernateSessionFactory.currentSession().clear();
		
		SubsetDetailsDTO subsetDesignationsDTO = new SubsetDetailsDTO();
		
		if (versionId < HibernateSessionFactory.AUTHORING_VERSION_ID)
		{
			Version version = VersionDelegate.getByVersionId(versionId);
			subsetDesignationsDTO.setVersionId(versionId);
			subsetDesignationsDTO.setVersionName(version.getName());
		}

		Subset subset = SubsetDelegate.getSubset(subsetEntityId);
		List<Designation> designations = DesignationDelegate.getBySubset(subsetEntityId, versionId);

		subsetDesignationsDTO.setSubset(subset);
		subsetDesignationsDTO.setDesignations(designations);
		
		return subsetDesignationsDTO;
	}
}
