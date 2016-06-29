package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.CodedConcept;

import java.util.List;

public class CodedConceptListViewDTO
{
	private List<CodedConcept> codedConcepts;
	private Long totalNumberOfRecords;
	
	public CodedConceptListViewDTO(List<CodedConcept> codedConcepts)
	{
		super();
		this.codedConcepts = codedConcepts;
	}

	public List<CodedConcept> getCodedConcepts()
	{
		return codedConcepts;
	}
	
	public void setCodedConcepts(List<CodedConcept> codedConcepts)
	{
		this.codedConcepts = codedConcepts;
	}
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
}
