package gov.vha.vets.term.services.dto.api;

import java.util.ArrayList;
import java.util.List;

public class SubsetListViewDTO 
{
	private Long totalNumberOfRecords;
	private List<SubsetViewDTO> subsetViewDtoDetails = new ArrayList<SubsetViewDTO>();
	
	public Long getTotalNumberOfRecords() 
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	public List<SubsetViewDTO> getSubsetViewDtoDetails() 
	{
		return subsetViewDtoDetails;
	}
	public void setSubsetViewDtoDetails(List<SubsetViewDTO> subsetViewDtoDetails) 
	{
		this.subsetViewDtoDetails = subsetViewDtoDetails;
	}
	
	

}
