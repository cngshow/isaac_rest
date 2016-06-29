package gov.vha.vets.term.services.dto;

import java.util.ArrayList;
import java.util.List;

public class ConceptRelationshipConceptListDTO
{
	private Long totalNumberOfRecords;
	private List<ConceptRelationshipDTO> conceptRelationshipDTOs = new ArrayList<ConceptRelationshipDTO>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}

	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	public List<ConceptRelationshipDTO> getConceptRelationshipDTOs()
	{
		return conceptRelationshipDTOs;
	}

	public void setConceptRelationshipDTOs(
			List<ConceptRelationshipDTO> conceptRelationshipDTOs)
	{
		this.conceptRelationshipDTOs = conceptRelationshipDTOs;
	}
}
