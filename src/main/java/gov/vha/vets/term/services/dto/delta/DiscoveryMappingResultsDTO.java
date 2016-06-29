package gov.vha.vets.term.services.dto.delta;

public class DiscoveryMappingResultsDTO
{
	private String system;
	private Long vuid;
	private String sourceCode;
	private String targetCode;
	private String mapEntryOrder;
	
	public DiscoveryMappingResultsDTO()
	{
		super();
	}
	
	public DiscoveryMappingResultsDTO(String system, Long vuid, String sourceCode, String targetCode, String mapEntryOrder)
	{
		super();
		this.system = system;
		this.vuid = vuid;
		this.sourceCode = sourceCode;
		this.targetCode = targetCode;
		this.mapEntryOrder = mapEntryOrder;
	}

	public Long getVuid()
	{
		return vuid;
	}
	
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}
	
	public String getSourceCode()
	{
		return sourceCode;
	}
	
	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}
	
	public String getTargetCode()
	{
		return targetCode;
	}
	
	public void setTargetCode(String targetCode)
	{
		this.targetCode = targetCode;
	}
	
	public String getMapEntryOrder()
	{
		return mapEntryOrder;
	}

	public void setMapEntryOrder(String mapEntryOrder)
	{
		this.mapEntryOrder = mapEntryOrder;
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
