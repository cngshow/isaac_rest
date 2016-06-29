package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.business.api.TerminologyDelegate;
import gov.vha.vets.term.services.dao.DesignationRelationshipDao;
import gov.vha.vets.term.services.dao.MapEntryDao;
import gov.vha.vets.term.services.dao.VersionDao;
import gov.vha.vets.term.services.dto.MapEntryDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.StringKeyObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class VersionDelegate
{
    /**
     * @param name
     * @return
     * @throws STSNotFoundException
     */
    public static Version getAuthoring()
    {
        Version version = VersionDao.getByVersionId(HibernateSessionFactory.AUTHORING_VERSION_ID);

        return version;
    }
	/**
	 * @param versionName
	 * @return
	 * @throws STSNotFoundException
	 */
	public static Version get(long codeSystemId, String versionName)
	{
		Version version = VersionDao.get(codeSystemId, versionName);

		return version;
	}

    /**
     * Get the most current version by codeSystem name
     * @return
     */
    public static Version getRecent(String codeSystemName, boolean includeAuthoring)
    {
        return VersionDao.getRecent(codeSystemName, includeAuthoring);
    }

    public static Version createVHAT() throws STSNotFoundException
    {
        Version version = VersionDao.getRecent(HibernateSessionFactory.VHAT_NAME, false);
        if(version == null)
        {
            CodeSystem codeSystem = CodeSystemDelegate.get("VHAT");
            Version newVersion = new Version("1", new Date(), "VHAT Version");
            newVersion.setCodeSystem(codeSystem);
            version = VersionDao.create(newVersion);
        }
        else
        {
            Version newVersion = new Version();
            CodeSystem codeSystem = CodeSystemDelegate.get("VHAT");
            String mostRecentVersionName = version.getName();
            int versionName = Integer.parseInt(mostRecentVersionName);
            newVersion.setEffectiveDate(new Date());
            newVersion.setDescription("VHAT Version");
            newVersion.setName(String.valueOf(++versionName));
            newVersion.setCodeSystem(codeSystem);
            version = VersionDao.create(newVersion);
        }
        
        return version;
    }

    public static Version get(String codeSystemName, String versionName)
    {
        return VersionDao.get(codeSystemName, versionName);
    }

    public static Version get(Long vuid, String versionName)
    {
        return VersionDao.getByCodeSystemVuid(vuid, versionName);
    }
    
	public static Version create(Version version) throws STSNotFoundException
	{
		return VersionDao.create(version);
	}

	/**
	 * @param codeSystem
	 * @return
	 */
	public static List<Version> getVersions(CodeSystem codeSystem)
	{
		return VersionDao.getVersions(codeSystem);
	}

	/**
	 * @param conceptEntityId
	 * @return
	 */
	public static List<Version> get(long conceptEntityId)
	{
		return VersionDao.getVersions(conceptEntityId);
	}
    
    public static Version getByVersionId(long versionId)
    {
        return VersionDao.getByVersionId(versionId);
    }

	static void setAuthoringToVersion(Version version, Collection<Long> codedConceptEntityIds, Collection<Long> mapSetEntityIds ) 
	{
		Collection<Long> conceptEntityIds = new HashSet<Long>();
		Collection<Long> mapEntryEntityIds = new HashSet<Long>();
		conceptEntityIds.addAll(codedConceptEntityIds);
		if (mapSetEntityIds != null && !mapSetEntityIds.isEmpty())
		{
			// remove all 'current' mapset versions in the StringKeyObjectMap (used for web service)
			conceptEntityIds.addAll(mapSetEntityIds);
			for (Long mapSetEntityId : mapSetEntityIds)
			{
// Next two lines commented out because they break the V10 Browser - add back in when new version of browser is deployed to production
//				MapSet mapSet = MapSetDelegate.get(mapSetEntityId);
//				MapSetDelegate.updateEffectiveDate(mapSet);
				List<MapEntryDTO> mapEntries = MapEntryDao.getChanges(mapSetEntityId);
				for (MapEntryDTO mapEntryDTO : mapEntries)
				{
					mapEntryEntityIds.add(mapEntryDTO.getMapEntry().getEntityId());
				}
			}
			MapSetRelationshipDelegate.setAuthoringToVersion(mapSetEntityIds, version);
			conceptEntityIds.addAll(mapSetEntityIds);
			conceptEntityIds.addAll(mapEntryEntityIds);
		}
		
        if (!conceptEntityIds.isEmpty())
        {
    		ConceptDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		ConceptRelationshipDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		PropertyDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		DesignationRelationshipDao.setAuthoringToVersion(conceptEntityIds, version);
    		DesignationPropertyDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		DesignationDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		SubsetDelegate.setAuthoringToVersion(conceptEntityIds, version);
    		
    		// Only set the coded concepts
    		if (!codedConceptEntityIds.isEmpty())
    		{
    			SubsetRelationshipDelegate.setAuthoringToVersion(codedConceptEntityIds, version);
    			ConceptStateDelegate.remove(codedConceptEntityIds);
    		}

            //remove state of the concept for coded concepts and mapsets
    		if (!mapSetEntityIds.isEmpty())
    		{
    		    ConceptStateDelegate.remove(mapSetEntityIds);
    		}
            HibernateSessionFactory.currentSession().flush();
    	    HibernateSessionFactory.currentSession().clear();
        }
	}

    public static List<Long> getConceptEntityIdsByVersionId(Long versionId) throws STSException
    {
        HashSet<Long> list = new HashSet<Long>();
        Version version = VersionDao.getByVersionId(versionId);
        if (version == null)
        {
            throw new STSException("Version Id: "+versionId+" is not valid.");
        }
        List<Long> conceptList = CodedConceptDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(conceptList);
        List<Long> designationList = DesignationDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(designationList);
        List<Long> designationPropertyList = DesignationPropertyDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(designationPropertyList);
        List<Long> PropertyList = PropertyDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(PropertyList);
        List<Long> SubsetRelationshipList = SubsetRelationshipDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(SubsetRelationshipList);
        List<Long> conceptRelationshipList = ConceptRelationshipDelegate.getConceptEntityIdsByVersionId(versionId);
        list.addAll(conceptRelationshipList);

        return new ArrayList<Long>(list);
    }

   
    /**
     * 
     * @param finalizedVersionNames
     * @return
     */
    public static List<Long> getByVersionNames(List<String> finalizedVersionNames)
    {
        return VersionDao.getByVersionNames(finalizedVersionNames);
    }

	//@TODO check for MERGE problems
    public static List<String> getVersionNamesByIds(List<Long> versionIds)
    {
        return VersionDao.getVersionNamesByIds(versionIds);
    }

	/**
	 * @param name
	 * @param effectiveDate
	 * @param description
	 * @return
	 * @throws STSNotFoundException 
	 */
	public static Version create(String name, Date effectiveDate, String description, CodeSystem codeSystem) throws STSNotFoundException
	{
		Version version = new Version(name, effectiveDate, description);
        version.setCodeSystem(codeSystem);
		return create(version);
	}
	
	public static Version createSDO(String name, Date effectiveDate, Date releaseDate, Date importDate, String description, CodeSystem codeSystem, String source)
		throws STSNotFoundException
	{
		Version version = new Version(name, effectiveDate, description);
        version.setCodeSystem(codeSystem);
        version.setReleaseDate(releaseDate);
        version.setImportDate(importDate);
        version.setSource(source);
		return create(version);
	}
	
	/**
	 * Get a list of all non-VHAT versions
	 * @param vhatCodeSystemId
	 * @return List<Version>
	 */
	public static List<Version> getSDOVersions(long vhatCodeSystemId)
	{
		return VersionDao.getSDOVersions(vhatCodeSystemId);
	}
	
	/**
	 * Remove the version by ID, including CodedConcepts, Designations
	 * and DesignationRelationships
	 * @param versionId
	 */
	public static void removeSDOVersion(long versionId) throws STSException
	{
        Version version = getByVersionId(versionId);
		VersionDao.removeSDOVersion(versionId);
		List<Version> versions = VersionDao.getVersions(version.getCodeSystem());
		if (versions.size() == 0)
		{
		    CodeSystemDelegate.remove(version.getCodeSystem());
		}
	}
	
	/**
	 * Get all versions in the VHAT codesystem
	 * @return List<Version>
	 */
	public static List<Version> getVHATVersions(boolean includeAuthoring)
	{
		List<Version> vhatVersions = VersionDao.getVHATVersions(includeAuthoring);
		
		return vhatVersions;
	}
	
	public static List<Version> getFinalizedVersions(CodeSystem codeSystem)
    {
	    return VersionDao.getFinalizedVersions(codeSystem);
    }
	
	public static List<Version> getAllFinalizedVersions()
    {
	    return VersionDao.getAllFinalizedVersions();
    }
	
    public static List<Version> getByCodeSystemVuids(List<Long> codeSystemVuids) 
	{
        return VersionDao.getByCodeSystemVuids(codeSystemVuids);
	}

    public static Date getLastUpdatedDate()
    {
		return VersionDao.getLastUpdatedDate();
    }
	
	public static void update(Version version)
	{
		VersionDao.update(version);
	}
}
