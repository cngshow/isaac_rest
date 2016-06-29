package gov.vha.vets.term.services.dto;

public class SubsetCountDTO
{
	private long count;
	private long versionId;
	private String versionName;
	
	public SubsetCountDTO(long count, long versionId)
	{
		super();
		this.count = count;
		this.versionId = versionId;
	}
	
	public long getCount()
	{
		return count;
	}
	
	public void setCount(long count)
	{
		this.count = count;
	}
	
	public long getVersionId()
	{
		return versionId;
	}
	
	public void setVersionId(long versionId)
	{
		this.versionId = versionId;
	}

	public String getVersionName()
	{
		return versionName;
	}

	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}
}
