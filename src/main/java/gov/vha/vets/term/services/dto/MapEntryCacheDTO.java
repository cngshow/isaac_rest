package gov.vha.vets.term.services.dto;

public class MapEntryCacheDTO
{
	private long mapSetEntityId;
	private long mapSetVersionId;
	private long mapEntryEntityId;
	private long mapEntryVuid;
	private int mapEntrySequence;
	private boolean mapEntryActive;
	private long sourceVersionId;
	private long sourcePrefDesTypeId;
	private String sourceConceptCode;
	private long sourceDesignationTypeId;
	private String sourceDesignationCode;
	private String sourceDesignationName;
	private boolean sourceDesignationActive;
	private long targetVersionId;
	private long targetPrefDesTypeId;
	private long targetDesignationTypeId;
	private String targetConceptCode;
	private String targetDesignationCode;
	private String targetDesignationName;
	private boolean targetDesignationActive;

	public MapEntryCacheDTO(long mapEntryEntityId, long mapEntryVuid, int mapEntrySequence, boolean mapEntryActive,
			long sourceVersionId, long sourcePrefDesTypeId, String sourceConceptCode, long sourceDesignationTypeId,
			String sourceDesignationCode, String sourceDesignationName, boolean sourceDesignationActive, long targetVersionId,
			long targetPrefDesTypeId, String targetConceptCode, long targetDesignationTypeId, String targetDesignationCode,
			String targetDesignationName, boolean targetDesignationActive)
	{
		super();
		this.mapEntryEntityId = mapEntryEntityId;
		this.mapEntryVuid = mapEntryVuid;
		this.mapEntrySequence = mapEntrySequence;
		this.mapEntryActive = mapEntryActive;
		this.sourceVersionId = sourceVersionId;
		this.sourcePrefDesTypeId = sourcePrefDesTypeId;
		this.sourceConceptCode = sourceConceptCode;
		this.sourceDesignationTypeId = sourceDesignationTypeId;
		this.sourceDesignationCode = sourceDesignationCode;
		this.sourceDesignationName = sourceDesignationName;
		this.sourceDesignationActive = sourceDesignationActive;
		this.targetVersionId = targetVersionId;
		this.targetPrefDesTypeId = targetPrefDesTypeId;
		this.targetConceptCode = targetConceptCode;
		this.targetDesignationTypeId = targetDesignationTypeId;
		this.targetDesignationCode = targetDesignationCode;
		this.targetDesignationName = targetDesignationName;
		this.targetDesignationActive = targetDesignationActive;
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

	public long getMapEntryVuid()
	{
		return mapEntryVuid;
	}

	public void setMapEntryVuid(long mapEntryVuid)
	{
		this.mapEntryVuid = mapEntryVuid;
	}

	public int getMapEntrySequence()
	{
		return mapEntrySequence;
	}

	public void setMapEntrySequence(int mapEntrySequence)
	{
		this.mapEntrySequence = mapEntrySequence;
	}

	public boolean isMapEntryActive()
	{
		return mapEntryActive;
	}

	public void setMapEntryActive(boolean mapEntryActive)
	{
		this.mapEntryActive = mapEntryActive;
	}

	public long getSourceVersionId()
	{
		return sourceVersionId;
	}

	public void setSourceVersionId(long sourceVersionId)
	{
		this.sourceVersionId = sourceVersionId;
	}

	public long getSourcePrefDesTypeId()
	{
		return sourcePrefDesTypeId;
	}

	public void setSourcePrefDesTypeId(long sourcePrefDesTypeId) {
		this.sourcePrefDesTypeId = sourcePrefDesTypeId;
	}

	public String getSourceConceptCode()
	{
		return sourceConceptCode;
	}

	public void setSourceConceptCode(String sourceConceptCode)
	{
		this.sourceConceptCode = sourceConceptCode;
	}

	public long getSourceDesignationTypeId()
	{
		return sourceDesignationTypeId;
	}

	public void setSourceDesignationTypeId(long sourceDesignationTypeId)
	{
		this.sourceDesignationTypeId = sourceDesignationTypeId;
	}

	public String getSourceDesignationCode()
	{
		return sourceDesignationCode;
	}

	public void setSourceDesignationCode(String sourceDesignationCode)
	{
		this.sourceDesignationCode = sourceDesignationCode;
	}

	public String getSourceDesignationName()
	{
		return sourceDesignationName;
	}

	public void setSourceDesignationName(String sourceDesignationName)
	{
		this.sourceDesignationName = sourceDesignationName;
	}

	public boolean isSourceDesignationActive()
	{
		return sourceDesignationActive;
	}

	public void setSourceDesignationActive(boolean sourceDesignationActive)
	{
		this.sourceDesignationActive = sourceDesignationActive;
	}

	public long getTargetVersionId()
	{
		return targetVersionId;
	}

	public void setTargetVersionId(long targetVersionId)
	{
		this.targetVersionId = targetVersionId;
	}

	public long getTargetPrefDesTypeId()
	{
		return targetPrefDesTypeId;
	}

	public void setTargetPrefDesTypeId(long targetPrefDesTypeId)
	{
		this.targetPrefDesTypeId = targetPrefDesTypeId;
	}

	public long getTargetDesignationTypeId()
	{
		return targetDesignationTypeId;
	}

	public void setTargetDesignationTypeId(long targetDesignationTypeId)
	{
		this.targetDesignationTypeId = targetDesignationTypeId;
	}

	public String getTargetConceptCode()
	{
		return targetConceptCode;
	}

	public void setTargetConceptCode(String targetConceptCode)
	{
		this.targetConceptCode = targetConceptCode;
	}

	public String getTargetDesignationCode()
	{
		return targetDesignationCode;
	}

	public void setTargetDesignationCode(String targetDesignationCode)
	{
		this.targetDesignationCode = targetDesignationCode;
	}

	public String getTargetDesignationName()
	{
		return targetDesignationName;
	}

	public void setTargetDesignationName(String targetDesignationName)
	{
		this.targetDesignationName = targetDesignationName;
	}

	public boolean isTargetDesignationActive()
	{
		return targetDesignationActive;
	}

	public void setTargetDesignationActive(boolean targetDesignationActive)
	{
		this.targetDesignationActive = targetDesignationActive;
	}
}
