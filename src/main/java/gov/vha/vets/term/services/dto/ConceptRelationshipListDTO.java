package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.ConceptRelationship;

import java.util.ArrayList;
import java.util.List;

public class ConceptRelationshipListDTO {
	
	private Long totalNumberOfRecords;
	private List<ConceptRelationship> relationships = new ArrayList<ConceptRelationship>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public List<ConceptRelationship> getRelationships() 
	{
		return relationships;
	}
	public void setRelationships(List<ConceptRelationship> relationships) 
	{
		this.relationships = relationships;
	}
}
