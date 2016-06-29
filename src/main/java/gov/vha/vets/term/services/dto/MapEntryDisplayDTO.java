package gov.vha.vets.term.services.dto;

public class MapEntryDisplayDTO
{
	private long mapSetEntityId;
	private long mapSetVersionId;
	private long mapEntryEntityId;
	private long vuid;
	private long sourceCodeSystemId;
	private long sourceVersionId;
	private String sourceCode;
	private String sourceDescription;
	private long targetCodeSystemId;
	private long targetVersionId;
	private String targetCode;
	private String targetDescription;
	private int sequence;
	private boolean active;

	public MapEntryDisplayDTO(long mapEntryEntityId, long vuid,
			String sourceCode, String sourceDescription, String targetCode,
			String targetDescription, int sequence, boolean active)
	{
		super();
		this.mapEntryEntityId = mapEntryEntityId;
		this.vuid = vuid;
		this.sourceCode = sourceCode;
		this.sourceDescription = sourceDescription;
		this.targetCode = targetCode;
		this.targetDescription = targetDescription;
		this.sequence = sequence;
		this.active = active;
	}

	public MapEntryDisplayDTO(long mapSetEntityId, long mapSetVersionId,
			long mapEntryEntityId, long vuid, long sourceCodeSystemId,
			long sourceVersionId, String sourceCode, String sourceDescription,
			long targetCodeSystemId, long targetVersionId, String targetCode,
			String targetDescription, int sequence, boolean active)
	{
		super();
		this.mapSetEntityId = mapSetEntityId;
		this.mapSetVersionId = mapSetVersionId;
		this.mapEntryEntityId = mapEntryEntityId;
		this.vuid = vuid;
		this.sourceCodeSystemId = sourceCodeSystemId;
		this.sourceVersionId = sourceVersionId;
		this.sourceCode = sourceCode;
		this.sourceDescription = sourceDescription;
		this.targetCodeSystemId = targetCodeSystemId;
		this.targetVersionId = targetVersionId;
		this.targetCode = targetCode;
		this.targetDescription = targetDescription;
		this.sequence = sequence;
		this.active = active;
	}

	public long getMapSetEntityId()
	{
		return mapSetEntityId;
	}

	public void setMapSetEntityId(long mapSetEntityId)
	{
		this.mapSetEntityId = mapSetEntityId;
	}

	public long getMapSetVersionId()
	{
		return mapSetVersionId;
	}

	public void setMapSetVersionId(long mapSetVersionId)
	{
		this.mapSetVersionId = mapSetVersionId;
	}

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
	
	public long getSourceCodeSystemId()
	{
		return sourceCodeSystemId;
	}

	public void setSourceCodeSystemId(long sourceCodeSystemId)
	{
		this.sourceCodeSystemId = sourceCodeSystemId;
	}

	public long getSourceVersionId()
	{
		return sourceVersionId;
	}

	public void setSourceVersionId(long sourceVersionId)
	{
		this.sourceVersionId = sourceVersionId;
	}

	public String getSourceCode()
	{
		return sourceCode;
	}
	
	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}
	
	public String getSourceDescription()
	{
		return sourceDescription;
	}
	
	public void setSourceDescription(String sourceDescription)
	{
		this.sourceDescription = sourceDescription;
	}
	
	public long getTargetCodeSystemId()
	{
		return targetCodeSystemId;
	}

	public void setTargetCodeSystemId(long targetCodeSystemId)
	{
		this.targetCodeSystemId = targetCodeSystemId;
	}

	public long getTargetVersionId()
	{
		return targetVersionId;
	}

	public void setTargetVersionId(long targetVersionId)
	{
		this.targetVersionId = targetVersionId;
	}

	public String getTargetCode()
	{
		return targetCode;
	}
	
	public void setTargetCode(String targetCode)
	{
		this.targetCode = targetCode;
	}
	
	public String getTargetDescription()
	{
		return targetDescription;
	}
	
	public void setTargetDescription(String targetDescription)
	{
		this.targetDescription = targetDescription;
	}
	
	public int getSequence()
	{
		return sequence;
	}
	
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}
}
