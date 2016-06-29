package gov.vha.vets.term.services.dto;

public class BrowseMappingResultDTO implements BrowseResult
{
	private long mapEntryEntityId;
	private long vuid;
	private String sourceCode;
	private String sourceCodeDescription;
	private String targetCode;
	private String targetCodeDescription;
	private int sequence;
	private String active;
	
	public long getMapEntryEntityId()
	{
		return mapEntryEntityId;
	}
	
	public void setMapEntryEntityId(long mapEntryEntityId)
	{
		this.mapEntryEntityId = mapEntryEntityId;
	}
	
	public long getVuid()
	{
		return vuid;
	}
	
	public void setVuid(long vuid)
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
	
	public String getSourceCodeDescription()
	{
		return sourceCodeDescription;
	}
	
	public void setSourceCodeDescription(String sourceCodeDescription)
	{
		this.sourceCodeDescription = sourceCodeDescription;
	}
	
	public String getTargetCode()
	{
		return targetCode;
	}
	
	public void setTargetCode(String targetCode)
	{
		this.targetCode = targetCode;
	}
	
	public String getTargetCodeDescription()
	{
		return targetCodeDescription;
	}
	
	public void setTargetCodeDescription(String targetCodeDescription)
	{
		this.targetCodeDescription = targetCodeDescription;
	}
	
	public int getSequence()
	{
		return sequence;
	}
	
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}
	
	public String getActive()
	{
		return active;
	}
	
	public void setActive(String active)
	{
		this.active = active;
	}	
}
