package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.Subset;

public class SubsetViewDTO
{
	private Subset subset;
	
	public SubsetViewDTO(Subset subset)
	{
		super();
		this.subset = subset;
	}

	public Subset getSubset()
	{
		return subset;
	}
	
	public void setSubset(Subset subset)
	{
		this.subset = subset;
	}
}
