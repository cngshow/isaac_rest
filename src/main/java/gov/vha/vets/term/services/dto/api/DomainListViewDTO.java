package gov.vha.vets.term.services.dto.api;

import java.util.ArrayList;
import java.util.List;

public class DomainListViewDTO
{
	private Long totalNumberOfRecords;
	private List<DomainViewDTO> domainListViewDtoDetails = new ArrayList<DomainViewDTO>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public List<DomainViewDTO> getDomainListViewDtoDetails()
	{
		return domainListViewDtoDetails;
	}
	public void setDomainListViewDtoDetails(
			List<DomainViewDTO> domainListViewDtoDetails)
	{
		this.domainListViewDtoDetails = domainListViewDtoDetails;
	}
	
	

}
