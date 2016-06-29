package gov.vha.vets.term.services.dto.api;

import java.util.List;

public class CodeSystemListViewDTO
{
	private Long totalNumberOfRecords;
	private List<CodeSystemViewDTO> codeSystemViewDTOs;
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	public List<CodeSystemViewDTO> getCodeSystemViewDTOs()
	{
		return codeSystemViewDTOs;
	}
	
	public void setCodeSystems(List<CodeSystemViewDTO> codeSystemViewDTOs)
	{
		this.codeSystemViewDTOs = codeSystemViewDTOs;
	}
}
