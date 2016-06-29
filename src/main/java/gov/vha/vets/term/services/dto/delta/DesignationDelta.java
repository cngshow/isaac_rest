package gov.vha.vets.term.services.dto.delta;

public class DesignationDelta
{
    protected String system;
    protected String name;
    protected long vuid;
    protected String type;
    protected String code;
    protected String conceptCode;
    protected boolean active;
    
    /**
     * default constructor
     */
    public DesignationDelta()
    {
    	
    }
    
	/**
	 * 
	 * @param system
	 * @param conceptCode
	 * @param name
	 * @param designationType
	 */
    public DesignationDelta(String system, String code, String conceptCode, String name, String designationType, boolean active)
	{
		this.system = system;
		this.name = name;
        this.code = code;
		this.conceptCode = conceptCode;
		this.type = designationType;
        this.active = active;
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
     * @return Returns the conceptCode.
     */
    public String getConceptCode()
    {
        return conceptCode;
    }

    /**
     * @param conceptCode The conceptCode to set.
     */
    public void setConceptCode(String conceptCode)
    {
        this.conceptCode = conceptCode;
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return Returns the vuid.
     */
    public long getVuid()
    {
        return vuid;
    }

    /**
     * @param vuid The vuid to set.
     */
    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the system.
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * @param system The system to set.
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

}
