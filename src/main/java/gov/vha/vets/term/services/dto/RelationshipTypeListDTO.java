package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.RelationshipType;

import java.util.ArrayList;
import java.util.List;

public class RelationshipTypeListDTO 
{
	private Long totalNumberOfRecords;
	private List<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public List<RelationshipType> getRelationshipTypes()
	{
		return relationshipTypes;
	}
	public void setRelationshipTypes(List<RelationshipType> relationshipTypes) 
	{
		this.relationshipTypes = relationshipTypes;
	}
	
}
