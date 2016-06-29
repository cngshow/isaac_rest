package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.CodedConcept;

import java.util.ArrayList;
import java.util.List;

public class CodedConceptListDTO
{
	private Long totalNumberOfRecords;
	private List<CodedConcept> codedConcepts = new ArrayList<CodedConcept>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public List<CodedConcept> getCodedConcepts()
	{
		return codedConcepts;
	}
	public void setCodedConcepts(List<CodedConcept> codedConcepts)
	{
		this.codedConcepts = codedConcepts;
	}
	
	

}
