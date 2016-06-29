package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;

public class SubsetVersionDTO
{
	private Subset subset;
	private Version version;
	
	
	public SubsetVersionDTO(Subset subset, Version version)
	{
		super();
		this.subset = subset;
		this.version = version;
	}
	public Subset getSubset()
	{
		return subset;
	}
	public void setSubset(Subset subset)
	{
		this.subset = subset;
	}
	public Version getVersion()
	{
		return version;
	}
	public void setVersion(Version version)
	{
		this.version = version;
	}

	
}
