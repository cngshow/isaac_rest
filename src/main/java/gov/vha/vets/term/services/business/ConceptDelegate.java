package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ConceptDao;
import gov.vha.vets.term.services.dto.change.ConceptChangeDTO;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.Version;

import java.util.Collection;
import java.util.List;

public class ConceptDelegate
{
	/**
     * Get a specific concept for a specific version.
     * @param codeSystem
     * @param code
     * @param version
     * @return CodedConcept
     * @throws STSNotFoundException 
     */
    public static List<Concept> get(CodeSystem codeSystem, Collection<String> codes) 
    {
        return ConceptDao.get(codeSystem, codes);
    }

    public static List<Concept> getActiveOnly(CodeSystem codeSystem, Collection<String> codes) 
    {
        return ConceptDao.getActiveOnly(codeSystem, codes);
    }

    public static Concept get(long conceptEntityId)
    {
    	return ConceptDao.get(conceptEntityId);
    }
    
    /**
     * Get a specific concept for a specific version.
     * 
     * @param conceptEntityId
     * @param version
     * @return Concept
     * @throws STSNotFoundException
     */
    public static Concept get(long conceptEntityId, long versionId)
    {
    	return ConceptDao.get(conceptEntityId, versionId);
    }


	public static Concept get(String conceptCode, long versionId)
	{
		return ConceptDao.get(conceptCode, versionId);
	}

    public static Concept getByVuid(Long vuid)
	{
		return ConceptDao.getByVuid(vuid);
	}
    
    public static Concept getByCode(String conceptCode)
	{
		return ConceptDao.getByCode(conceptCode);
	}
    
    /**
     * get concept changes
     * @param conceptEntityId
     * @return
     */
    public static ConceptChangeDTO getConceptChange(long conceptEntityId, long versionId)
    {
        ConceptChangeDTO change = new ConceptChangeDTO();
        change.setVersionId(versionId);
        Concept concept = ConceptDao.get(conceptEntityId, versionId);
        if (concept != null)
        {
            change.setRecent(concept);
            if (concept.getVersion().getId() == versionId)
            {
                Concept codedConceptPrevious = ConceptDao.getPreviousVersion(concept.getCodeSystem(), concept.getCode(), versionId);
                change.setPrevious(codedConceptPrevious);
            }
        }
        change.setConceptState(ConceptStateDelegate.get(conceptEntityId));
        return change;
    }
    
    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
        return ConceptDao.setAuthoringToVersion(conceptEntityIdList, version);
    }
}
