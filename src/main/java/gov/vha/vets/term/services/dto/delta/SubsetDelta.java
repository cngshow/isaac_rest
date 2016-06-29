package gov.vha.vets.term.services.dto.delta;

public class SubsetDelta
{
	protected String system;
	protected String name;
	protected boolean status;
    protected String code;
	protected long vuid;
	
	public SubsetDelta()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param system
	 * @param name
	 * @param status
	 * @param vuid
	 */
	public SubsetDelta(String system, String code, String name, boolean status, int vuid)
	{
		super();
		this.system = system;
		this.name = name;
        this.code = code;
		this.status = status;
		this.vuid = vuid;
	}
	
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
	/**
	 * @return return the Subset name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name Subset name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return Subset status
	 */
	public boolean getStatus()
	{
		return status;
	}
	
	/**
	 * @param status Get the Subset status
	 */
	public void setStatus(boolean status)
	{
		this.status = status;
	}
	
	/**
	 * @return Get the system name
	 */
	public String getSystem()
	{
		return system;
	}
	
	/**
	 * @param system Set the system name
	 */
	public void setSystem(String system)
	{
		this.system = system;
	}
	
	/**
	 * @return Get the Subset's VUID
	 */
	public long getVuid()
	{
		return vuid;
	}
	
	/**
	 * @param vuid Subset VUID
	 */
	public void setVuid(long vuid)
	{
		this.vuid = vuid;
	}
}