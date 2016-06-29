package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.DesignationDao;
import gov.vha.vets.term.services.dao.MapEntryDisplayDao;
import gov.vha.vets.term.services.dto.MapEntryDisplayDTO;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapEntryDisplayDelegate
{
	public static List<MapEntryDisplayDTO> getEntries(long entityId, Long versionId, int startRow, int rowsPerPage,
	        String sortOrder, String searchText, String sortColumnName)
	{
		Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
        return MapEntryDisplayDao.getEntries(entityId, realVersionId, true, startRow, rowsPerPage, sortOrder, searchText, sortColumnName,
                null, null, null, null, null, null);
	}

    public static List<MapEntryDisplayDTO> getEntries(long entityId, Long versionId, Collection<String> sourceConceptCodes, Collection<String> targetConceptCodes,
            String sourceDescription, String targetDescription, Integer mapEntryOrder, Boolean mapEntryStatus, int startRow, int rowsPerPage)
    {
        Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;
        return MapEntryDisplayDao.getEntries(entityId, realVersionId, true, startRow, rowsPerPage, null, null, null, sourceConceptCodes,
                targetConceptCodes, sourceDescription, targetDescription, mapEntryOrder, mapEntryStatus);
    }

	public static long getEntriesCount(long entityId, Long versionId, String sortOrder, String searchText, String sortColumnName)
	{
		Long realVersionId = versionId == null ? HibernateSessionFactory.AUTHORING_VERSION_ID : versionId;

		return MapEntryDisplayDao.getEntriesCount(entityId, realVersionId, true, sortOrder, searchText, sortColumnName);
	}

    public static void updateMapEntryCache(long mapSetEntityId, long versionId)
	{
        MapEntryDisplayDao.updateMapEntryCache(mapSetEntityId, versionId);
        List<MapEntryDisplayDTO> mapEntryDisplayList = MapEntryDisplayDao.getNullDescriptionEntries(mapSetEntityId, versionId);
        if (mapEntryDisplayList.size() > 0)
    	{
	        Set<String> sourceCodes = new HashSet<String>();
	        Set<String> targetCodes = new HashSet<String>();
	        long sourceCodeSystemId = 0;
	        long targetCodeSystemId = 0;
	        long sourceVersionId = 0;
	        long targetVersionId = 0;
	        for (MapEntryDisplayDTO mapEntryDisplayDTO : mapEntryDisplayList)
			{
				if (mapEntryDisplayDTO.getSourceDescription() == null)
				{
					sourceCodes.add(mapEntryDisplayDTO.getSourceCode());
					sourceCodeSystemId = mapEntryDisplayDTO.getSourceCodeSystemId();
					sourceVersionId = mapEntryDisplayDTO.getSourceVersionId();
				}
	
				if (mapEntryDisplayDTO.getTargetDescription() == null)
				{
					targetCodes.add(mapEntryDisplayDTO.getTargetCode());
					targetCodeSystemId = mapEntryDisplayDTO.getTargetCodeSystemId();
					targetVersionId = mapEntryDisplayDTO.getTargetVersionId();
				}
			}
	        CodeSystem sourceCodeSystem = CodeSystemDelegate.get(sourceCodeSystemId);
	        Map<String, Designation> sourceDescriptionMap = new HashMap<String, Designation>();
	        if (sourceCodeSystemId != 0)
	    	{
	        	sourceDescriptionMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(sourceCodeSystem, sourceVersionId, sourceCodes);
	    	}
	        CodeSystem targetCodeSystem = CodeSystemDelegate.get(targetCodeSystemId);
	        Map<String, Designation> targetDescriptionMap = new HashMap<String, Designation>();
	        if (targetCodeSystemId != 0)
	    	{
	        	targetDescriptionMap = DesignationDelegate.getConceptDescriptionsByConceptCodes(targetCodeSystem, targetVersionId, targetCodes);
	    	}
	        for (MapEntryDisplayDTO mapEntryDisplayDTO : mapEntryDisplayList)
			{
				if (mapEntryDisplayDTO.getSourceDescription() == null)
				{
					Designation designation = (Designation)sourceDescriptionMap.get(mapEntryDisplayDTO.getSourceCode());
					if (designation != null)
					{
						MapEntryDisplayDao.updateSourceDescription(mapSetEntityId, versionId, mapEntryDisplayDTO.getSourceCode(), designation.getName());
					}
				}
		
				if (mapEntryDisplayDTO.getTargetDescription() == null)
				{
					Designation designation = (Designation)targetDescriptionMap.get(mapEntryDisplayDTO.getTargetCode());
					if (designation != null)
					{
						MapEntryDisplayDao.updateTargetDescription(mapSetEntityId, versionId, mapEntryDisplayDTO.getTargetCode(), designation.getName());
					}
				}
			}
    	}
	}
}

