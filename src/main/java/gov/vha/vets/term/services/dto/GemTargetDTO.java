package gov.vha.vets.term.services.dto;

public class GemTargetDTO implements Comparable<GemTargetDTO>
{
	private String code;
	private String description;
	private String flags;
    private long versionId;

	public GemTargetDTO()
	{
	}
	public GemTargetDTO(String code, String description, String flags)
	{
		this.setCode(code);
		this.setDescription(description);
		this.setFlags(flags);
	}
	public GemTargetDTO(String code, String description, String flags, long versionId)
	{
		this.setCode(code);
		this.setDescription(description);
		this.setFlags(flags);
		this.setVersionId(versionId);
	}
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public String getFlags()
	{
		return flags;
	}
	public void setFlags(String flags)
	{
		this.flags = flags;
	}
	@Override
	public int compareTo(GemTargetDTO other)
	{
		// TODO Auto-generated method stub
		return (this.versionId+this.flags+this.code).compareTo(this.versionId+other.getFlags()+other.getCode());
	}
	public Integer getScenario()
	{
		return Integer.valueOf(flags.substring(3, 4));
	}
	public Integer getChoiceList()
	{
		return Integer.valueOf(flags.substring(4));
	}
	
	public boolean isCombination()
	{
		return 	(flags.charAt(2) == '1') ? true : false;
	}
	public long getVersionId() {
		return versionId;
	}
	public void setVersionId(long versionId) {
		this.versionId = versionId;
	}
	
}
