package gov.vha.vets.term.services.dto.delta;

public class DesignationPropertyDelta
{
    protected String system;
    protected String type;
    protected String value;
    protected String conceptCode;
    protected String code;
    
    
    /**
     * @param system
     * @param type
     * @param value
     * @param code
     */
    public DesignationPropertyDelta(String system, String code, String type, String value)
    {
        this.system = system;
        this.code = code;
        this.type = type;
        this.value = value;
    }

    /**
     * 
     */
    public DesignationPropertyDelta()
    {
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
    
    /**
     * @return Returns the code.
     */
    public String getCode()
    {
        return code;
    }
    
    /**
     * @param code The code to set.
     */
    public void setCode(String code)
    {
        this.code = code;
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
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }
}
