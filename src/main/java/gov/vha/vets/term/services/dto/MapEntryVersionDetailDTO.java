package gov.vha.vets.term.services.dto;



public class MapEntryVersionDetailDTO implements Comparable<MapEntryVersionDetailDTO>, Cloneable
{
    public enum StatusType { ADDED, INACTIVATED, ACTIVATED, UPDATED };
    public StatusType status;

    private long versionId;
    private Long entityId;
    private String name;
    private Long vuid;
    private boolean active;
    private String sourceCode;
    private String targetCode;
    private String sourceDescription;
    private String targetDescription;
    private int sequence;
    private Long grouping;


    
    public long getVersionId()
	{
		return versionId;
	}

	public void setVersionId(long versionId)
	{
		this.versionId = versionId;
	}

	public Long getEntityId()
	{
		return entityId;
	}

	public void setEntityId(Long entityId)
	{
		this.entityId = entityId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getVuid()
	{
		return vuid;
	}

	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
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

	public String getSourceDescription()
	{
		return sourceDescription;
	}

	public void setSourceDescription(String sourceDescription)
	{
		this.sourceDescription = sourceDescription;
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

	public Long getGrouping()
	{
		return grouping;
	}

	public void setGrouping(Long grouping)
	{
		this.grouping = grouping;
	}

	public StatusType getStatus()
    {
        return status;
    }

    public void setStatus(StatusType status)
    {
        this.status = status;
    }

    public int compareTo(MapEntryVersionDetailDTO anotherEntryDetail)
    {
        int anotherSequence = anotherEntryDetail.getSequence();  

        return this.sequence - anotherSequence;    
    }
    
	public Object clone()
    {
		MapEntryVersionDetailDTO cloneCopy = null;
		
        try
        {
        	cloneCopy = (MapEntryVersionDetailDTO) super.clone();
        	cloneCopy.setVersionId(versionId);
        	cloneCopy.setEntityId(entityId);
        	cloneCopy.setName(name);
        	cloneCopy.setVuid(vuid);
        	cloneCopy.setActive(active);
        	cloneCopy.setSourceCode(sourceCode);
        	cloneCopy.setTargetCode(targetCode);
        	cloneCopy.setSourceDescription(sourceDescription);
        	cloneCopy.setTargetDescription(targetDescription);
        	cloneCopy.setSequence(sequence);
        	cloneCopy.setGrouping(grouping);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return cloneCopy;
    }
}
