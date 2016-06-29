package gov.vha.vets.term.services.dto.api;

public class UsageContextDetailViewDTO
{
	private String name;
	private String vuid;	
	private Long totalNumberOfRecords;
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getVuid()
	{
		return vuid;
	}
	public void setVuid(String vuid)
	{
		this.vuid = vuid;
	}
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
}
