package gov.vha.vets.term.webservice.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Designation")
@XmlType(propOrder={"name", "code", "type", "status", "properties", "subsets"})
public class DesignationDetailTransfer
{
	private String name;
	private String code;
	private String type;
	private String status;
    private List<PropertyTransfer> properties;
    private List<ValueSetTransfer> subsets;

    public DesignationDetailTransfer()
    {
    }

    public DesignationDetailTransfer(String name, String code, String type, String status, List<PropertyTransfer> properties, List<ValueSetTransfer> subsets)
    {
        super();
        this.name = name;
        this.code = code;
        this.type = type;
        this.status = status;
        this.properties = properties;
        this.subsets = subsets;
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

    @XmlElementWrapper(name="Properties")
	@XmlElement(name="Property", required=true, nillable=false)	
	public List<PropertyTransfer> getProperties()
	{
		return properties;
	}

	public void setProperties(List<PropertyTransfer> properties)
	{
		this.properties = properties;
	}

    @XmlElementWrapper(name="ValueSets")
	@XmlElement(name="ValueSet", required=true, nillable=false)	
	public List<ValueSetTransfer> getSubsets()
	{
		return subsets;
	}

	public void setSubsets(List<ValueSetTransfer> subsets)
	{
		this.subsets = subsets;
	}
}
