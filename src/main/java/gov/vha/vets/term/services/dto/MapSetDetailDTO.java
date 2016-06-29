package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.util.Date;

public class MapSetDetailDTO implements Comparable<MapSetDetailDTO>
{
	protected long mapSetId;
	protected long mapSetEntityId;
	protected String mapSetName;
	protected Long mapSetVuid;
	protected long mapSetStateId;
	protected String mapSetStateName;
	protected String mapSetStateType;
	protected int mapSetActive;
	protected long sourceCodeSystemId;
	protected String sourceCodeSystemName;
	protected long sourceCodeSystemVuid;
	protected String sourceCodeSystemPrefDesName;
	protected long sourceVersionId;
	protected String sourceVersionName;
	protected boolean isSourceVHAT;
	protected String sourceValueType;
	protected long targetCodeSystemId;
	protected String targetCodeSystemName;
	protected long targetCodeSystemVuid;
	protected String targetCodeSystemPrefDesName;
	protected long targetVersionId;
	protected String targetVersionName;
	protected boolean isTargetVHAT;
	protected String targetValueType;
	protected long actualVersionId;
	protected String actualVersionName;
	protected Date actualVersionEffectiveDate;
	protected String preferredName;
	protected String mostRecentPreferredName;
	protected String fullySpecifiedName;
	protected String textDefinition;
	protected String description;
	protected Date lastUpdated;
	protected int changeCount;
	protected Long totalNumberOfRecords;
	protected int ROWNUM_;
	
	public long getMapSetId()
	{
		return mapSetId;
	}
	public void setMapSetId(long mapSetId)
	{
		this.mapSetId = mapSetId;
	}
	public void setMapSetIdBigDecimal(BigDecimal mapSetId)
	{
		this.mapSetId = mapSetId.longValue();
	}
	public long getMapSetEntityId()
	{
		return mapSetEntityId;
	}
	public void setMapSetEntityId(long mapSetEntityId)
	{
		this.mapSetEntityId = mapSetEntityId;
	}
	public void setMapSetEntityIdBigDecimal(BigDecimal mapSetEntityId)
	{
		this.mapSetEntityId = mapSetEntityId.longValue();
	}
	public String getMapSetName()
	{
		return mapSetName;
	}
	public void setMapSetName(String mapSetName)
	{
		this.mapSetName = (mapSetName == null) ? null : mapSetName.trim();
	}
	public Long getMapSetVuid()
	{
		return mapSetVuid;
	}
	public void setMapSetVuid(Long mapSetVuid)
	{
		this.mapSetVuid = mapSetVuid;
	}
	public void setMapSetVuidBigDecimal(BigDecimal mapSetVuid)
	{
		this.mapSetVuid = mapSetVuid.longValue();
	}
	public int getMapSetActive()
	{
		return mapSetActive;
	}
	public void setMapSetActive(int mapSetActive)
	{
		this.mapSetActive = mapSetActive;
	}
	public void setMapSetActiveBigDecimal(BigDecimal mapSetActive)
	{
		this.mapSetActive = mapSetActive.intValue();
	}
	public long getSourceCodeSystemId()
	{
		return sourceCodeSystemId;
	}
	public void setSourceCodeSystemId(long sourceCodeSystemId)
	{
		this.sourceCodeSystemId = sourceCodeSystemId;
	}
	public void setSourceCodeSystemIdBigDecimal(BigDecimal sourceCodeSystemId)
	{
		this.sourceCodeSystemId = sourceCodeSystemId.longValue();
	}
	public String getSourceCodeSystemName()
	{
		return sourceCodeSystemName;
	}
	public void setSourceCodeSystemName(String sourceCodeSystemName)
	{
		this.sourceCodeSystemName = sourceCodeSystemName;
	}
	public long getSourceCodeSystemVuid()
    {
        return sourceCodeSystemVuid;
    }
    public void setSourceCodeSystemVuid(long sourceCodeSystemVuid)
    {
        this.sourceCodeSystemVuid = sourceCodeSystemVuid;
    }
    public void setSourceCodeSystemVuidBigDecimal(BigDecimal sourceCodeSystemVuid)
    {
        this.sourceCodeSystemVuid = (sourceCodeSystemVuid != null) ? sourceCodeSystemVuid.longValue() : 0L;
    }
    public String getSourceCodeSystemPrefDesName()
    {
        return sourceCodeSystemPrefDesName;
    }
    public void setSourceCodeSystemPrefDesName(String sourceCodeSystemPrefDesName)
    {
        this.sourceCodeSystemPrefDesName = sourceCodeSystemPrefDesName;
    }
    public long getSourceVersionId()
	{
		return sourceVersionId;
	}
	public void setSourceVersionId(long sourceVersionId)
	{
		this.sourceVersionId = sourceVersionId;
	}
	public void setSourceVersionIdBigDecimal(BigDecimal sourceVersionId)
	{
		this.sourceVersionId = sourceVersionId.longValue();
	}
	public String getSourceVersionName()
	{
		return sourceVersionName;
	}
	public void setSourceVersionName(String sourceVersionName)
	{
		this.sourceVersionName = sourceVersionName;
	}
	public boolean isSourceVHAT()
	{
		return (sourceCodeSystemId == 1) ? true : false;
	}
	public String getSourceValueType()
	{
		return sourceValueType;
	}
	public void setSourceValueType(String sourceValueType)
	{
		this.sourceValueType = sourceValueType;
	}
	public long getTargetCodeSystemId()
	{
		return targetCodeSystemId;
	}
	public void setTargetCodeSystemId(long targetCodeSystemId)
	{
		this.targetCodeSystemId = targetCodeSystemId;
	}
	public void setTargetCodeSystemIdBigDecimal(BigDecimal targetCodeSystemId)
	{
		this.targetCodeSystemId = targetCodeSystemId.longValue();
	}
	public String getTargetCodeSystemName()
	{
		return targetCodeSystemName;
	}
	public void setTargetCodeSystemName(String targetCodeSystemName)
	{
		this.targetCodeSystemName = targetCodeSystemName;
	}
	public long getTargetCodeSystemVuid()
    {
        return targetCodeSystemVuid;
    }
    public void setTargetCodeSystemVuid(long targetCodeSystemVuid)
    {
        this.targetCodeSystemVuid = targetCodeSystemVuid;
    }
    public void setTargetCodeSystemVuidBigDecimal(BigDecimal targetCodeSystemVuid)
    {
        this.targetCodeSystemVuid = (targetCodeSystemVuid != null) ? targetCodeSystemVuid.longValue() : 0L;
    }
    public String getTargetCodeSystemPrefDesName()
    {
        return targetCodeSystemPrefDesName;
    }
    public void setTargetCodeSystemPrefDesName(String targetCodeSystemPrefDesName)
    {
        this.targetCodeSystemPrefDesName = targetCodeSystemPrefDesName;
    }
    public long getTargetVersionId()
	{
		return targetVersionId;
	}
	public void setTargetVersionId(long targetVersionId)
	{
		this.targetVersionId = targetVersionId;
	}
	public void setTargetVersionIdBigDecimal(BigDecimal targetVersionId)
	{
		this.targetVersionId = targetVersionId.longValue();
	}
	public String getTargetVersionName()
	{
		return targetVersionName;
	}
	public void setTargetVersionName(String targetVersionName)
	{
		this.targetVersionName = targetVersionName;
	}
	public boolean isTargetVHAT()
	{
		return (targetCodeSystemId == 1) ? true : false;
	}
	public String getTargetValueType()
	{
		return targetValueType;
	}
	public void setTargetValueType(String targetValueType)
	{
		this.targetValueType = targetValueType;
	}
	public long getActualVersionId()
	{
		return actualVersionId;
	}
	public void setActualVersionId(long actualVersionId)
	{
		this.actualVersionId = actualVersionId;
	}
	public void setActualVersionIdBigDecimal(BigDecimal actualVersionId)
	{
		this.actualVersionId = actualVersionId.longValue();
	}
	public String getActualVersionName()
	{
		return actualVersionName;
	}
	public void setActualVersionName(String actualVersionName)
	{
		this.actualVersionName = actualVersionName;
	}
	public Date getActualVersionEffectiveDate()
    {
        return actualVersionEffectiveDate;
    }
    public void setActualVersionEffectiveDate(Date actualVersionEffectiveDate)
    {
        this.actualVersionEffectiveDate = actualVersionEffectiveDate;
    }
    public String getPreferredName()
	{
		return preferredName;
	}
	public void setPreferredName(String preferredName)
	{
		this.preferredName = (preferredName == null) ? null : preferredName.trim();
	}
	public String getMostRecentPreferredName()
	{
		return mostRecentPreferredName;
	}
	public void setMostRecentPreferredName(String mostRecentPreferredName)
	{
		this.mostRecentPreferredName = mostRecentPreferredName;
	}
	public String getFullySpecifiedName()
	{
		return fullySpecifiedName;
	}
	public void setFullySpecifiedName(String fullySpecifiedName)
	{
		this.fullySpecifiedName = (fullySpecifiedName == null) ? null : fullySpecifiedName.trim();
	}
	public Date getLastUpdated()
	{
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	public long getMapSetStateId()
	{
		return mapSetStateId;
	}
	public void setMapSetStateId(long mapSetStateId)
	{
		this.mapSetStateId = mapSetStateId;
	}
	public void setMapSetStateIdBigDecimal(BigDecimal mapSetStateId)
	{
		if (mapSetStateId != null)
		{
			this.mapSetStateId = mapSetStateId.longValue();
		}
	}
	public String getMapSetStateName()
	{
		return mapSetStateName;
	}
	public void setMapSetStateName(String mapSetStateName)
	{
		this.mapSetStateName = mapSetStateName;
	}
	public String getMapSetStateType()
	{
		return mapSetStateType;
	}
	public void setMapSetStateType(String mapSetStateType)
	{
		this.mapSetStateType = mapSetStateType;
	}
	public int getChangeCount()
	{
		return changeCount;
	}
	public void setChangeCount(int changeCount)
	{
		this.changeCount = changeCount;
	}
	public void setChangeCountBigDecimal(BigDecimal changeCount)
	{
		this.changeCount = changeCount.intValue();
	}
	public String getTextDefinition()
	{
		return textDefinition;
	}
	public void setTextDefinition(String textDefinition)
	{
		this.textDefinition = (textDefinition == null) ? null : textDefinition.trim();
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = (description == null) ? null : description.trim();
	}
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	public void setROWNUM_(BigDecimal rownum)
	{
	    ROWNUM_ = rownum.intValue();
	}

	public int compareTo(MapSetDetailDTO mapSetDetailDTO)
    {
        int result = 0;

        MapSetDetailDTO thisMapSetDetailDTO = this;

        String value1 =  thisMapSetDetailDTO.getMostRecentPreferredName().toUpperCase()+"-"+getDescendingVersionString(thisMapSetDetailDTO.getActualVersionId());
        String value2 =  mapSetDetailDTO.getMostRecentPreferredName().toUpperCase()+"-"+getDescendingVersionString(mapSetDetailDTO.getActualVersionId());
        result = value1.compareTo(value2);

        return result;
    }
	
	private String getDescendingVersionString(long versionId)
	{
		String version = "0000000000";
		
		Long value = versionId - HibernateSessionFactory.AUTHORING_VERSION_ID;
		value = Math.abs(value);
		String newValue = value.toString();
		version = version.substring(0, version.length()-newValue.length()) + newValue;
		
		return version;
	}
}
