package gov.vha.vets.term.services.dto.api;

import java.util.List;

public class SubsetContentsListView
{
	private Long totalNumberOfRecords;
	private List<SubsetContentsView> subsetContentsView;

	public void setTotalNumberOfRecords(Long totalNumber)
	{
		this.totalNumberOfRecords = totalNumber;
	}

	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	
	public void setSubsetContentsView(List<SubsetContentsView> subsetContentsView)
	{
		this.subsetContentsView = subsetContentsView;
	}

	public List<SubsetContentsView> getSubsetContentsView()
	{
		return subsetContentsView;
	}
}
