package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Version;

import java.util.Date;
import java.util.List;

public class GemVersionDTO
{
	private Date effectiveDate;
	private Version version;
	private List<List<GemTargetDTO>> targets;

	public List<List<GemTargetDTO>> getTargets()
	{
		return targets;
	}
	
	public void setTargets(List<List<GemTargetDTO>> targets)
	{
		this.targets = targets;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}

	public Date getEffectiveDate()
	{
		return effectiveDate;
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
