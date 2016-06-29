package gov.vha.vets.term.services.dto.api;

import java.util.List;

public class TotalEntityListView
{
	private Long totalNumberOfRecords;
	private List<?> entitiesView;

	public void setTotalNumberOfRecords(Long totalNumber)
	{
		this.totalNumberOfRecords = totalNumber;
	}

	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	
	public void setEntitiesView(List<?> entitiesView)
	{
		this.entitiesView = entitiesView;
	}

	public List<?> getEntitiesView()
	{
		return entitiesView;
	}
}
