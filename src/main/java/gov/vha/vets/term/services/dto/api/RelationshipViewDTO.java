package gov.vha.vets.term.services.dto.api;

public class RelationshipViewDTO
{
    protected String name;
    protected String type;
    protected String code;
    protected boolean active;

    public RelationshipViewDTO()
    {
        super();
    }
    
    public RelationshipViewDTO(String name, String type, String code, boolean active)
    {
        super();
        this.name = name;
        this.type = type;
        this.code = code;
        this.active = active;
    }

    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    
    }
    
    public boolean getActive()
	{
		return active;
	}
    
	public void setActive(boolean active)
	{
		this.active = active;
	}
}
