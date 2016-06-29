package gov.vha.vets.term.services.dto.delta;

public class DiscoveryDeltaDTO
{
	private String name;
	private String value;
	private Long vuid;
	private String system;
	
	public DiscoveryDeltaDTO()
	{
		super();
	}
	
	public DiscoveryDeltaDTO(String system, String value, Long vuid, String name)
	{
		super();
		this.name = name;
		this.value = value;
		this.vuid = vuid;
		this.system = system;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public Long getVuid()
	{
		return vuid;
	}
	
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}
	
	public String getSystem()
	{
		return system;
	}
	
	public void setSystem(String system)
	{
		this.system = system;
	}
}
