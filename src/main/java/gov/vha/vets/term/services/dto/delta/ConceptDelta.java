package gov.vha.vets.term.services.dto.delta;

public class ConceptDelta
{
	protected String system;
	protected String code;
	protected String name;
    protected long vuid;
    protected boolean active;
	
    public ConceptDelta()
    {
        super();
    }
    
	public ConceptDelta(String system, String code, String name)
    {
        super();
        this.system = system;
        this.code = code;
        this.name = name;
    }

    /**
	 * Returns the concept code.
	 * @return
	 */
	public String getCode()
	{
		return code;
	}
	
	/**
	 * Sets the concept code.
	 * @param code
	 */
	public void setCode(String code)
	{
		this.code = code;
	}
	
	/**
	 * Returns the concept name.
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the concept name.
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the name of the source database
	 * @return
	 */
	public String getSystem()
	{
		return system;
	}

	/**
	 * Set the name of the source database
	 * @param system
	 */
	public void setSystem(String system)
	{
		this.system = system;
	}

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public long getVuid()
    {
        return vuid;
    }

    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }
}
