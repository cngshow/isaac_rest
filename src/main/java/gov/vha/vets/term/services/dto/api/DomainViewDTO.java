package gov.vha.vets.term.services.dto.api;

public class DomainViewDTO
{
	private long conceptEntityId;
	private long vuid;
	private String preferredDesignation;

	public DomainViewDTO(long conceptEntityId, long vuid, String preferredDesignation)
	{
		this.setConceptEntityId(conceptEntityId);
		this.setVuid(vuid);
		this.setPreferredDesignation(preferredDesignation);
	}
	public long getConceptEntityId()
	{
		return conceptEntityId;
	}
	public void setConceptEntityId(long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}
	public long getVuid()
	{
		return vuid;
	}
	public void setVuid(long vuid)
	{
		this.vuid = vuid;
	}
	public String getPreferredDesignation()
	{
		return preferredDesignation;
	}
	public void setPreferredDesignation(String preferredDesignation)
	{
		this.preferredDesignation = preferredDesignation;
	}
}
