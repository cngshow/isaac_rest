package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="UsageContext")
@XmlType(propOrder={"name", "vuid"})
public class UsageContextTransfer
{
    protected String name;
    protected String vuid;

    
    public UsageContextTransfer()
    {
    }
    
    public UsageContextTransfer(String name, String vuid)
    {
        this.name = name;
        this.vuid = vuid;
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

    
    @XmlElement(name="VUID", required=false, nillable=false)
    public String getVuid()
    {
        return vuid;
    }
    public void setVuid(String vuid)
    {
        this.vuid = vuid;
    }

}
