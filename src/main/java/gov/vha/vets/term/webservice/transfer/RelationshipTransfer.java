package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Association")
@XmlType(propOrder={"name", "type", "code", "status"})
public class RelationshipTransfer
{
    protected String name;
    protected String type;
    protected String code;
    protected String status;
    
    @XmlElement(name="Name", required=true, nillable=false)
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    @XmlElement(name="Type", required=true, nillable=false)
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    
    @XmlElement(name="Code", required=false, nillable=false)
    public String getCode()
    {
        return code;
    }
    public void setCode(String code)
    {
        this.code = code;
    }
    
    @XmlElement(name="Status", required=true, nillable=false)
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String status)
	{
		this.status = status;
	}
}
