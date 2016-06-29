package gov.vha.vets.term.services.dto.api;

import java.util.ArrayList;
import java.util.List;

public class RelationshipDetailListViewDTO 
{
	private Long totalNumberOfRecords;
	private List<RelationshipDetailViewDTO> relationshipDetailViewDTO = new ArrayList<RelationshipDetailViewDTO>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public List<RelationshipDetailViewDTO> getRelationshipDetailViewDTO()
	{
		return relationshipDetailViewDTO;
	}
	public void setRelationshipDetailViewDTO(List<RelationshipDetailViewDTO> relationshipDetailViewDTO) 
	{
		this.relationshipDetailViewDTO = relationshipDetailViewDTO;
	}

}
