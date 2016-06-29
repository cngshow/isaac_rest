package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Designation")
@XmlType(propOrder={"name", "code", "type", "status"})
public class DesignationTransfer
{
    protected String name;
    protected String code;
    protected String type;
    protected String status;

    public DesignationTransfer()
    {
    }

    public DesignationTransfer(String name, String code, String type, String status)
    {
        super();
        this.name = name;
        this.code = code;
        this.type = type;
        this.status = status;
    }
    
    @XmlElement(name="Name", required=true, nillable=false)
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    @XmlElement(name="Code", required=true, nillable=false)
    public String getCode()
    {
        return code;
    }
    public void setCode(String code)
    {
        this.code = code;
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
