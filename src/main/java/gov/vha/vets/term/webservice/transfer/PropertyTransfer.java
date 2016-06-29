package gov.vha.vets.term.webservice.transfer;

import gov.vha.vets.term.services.model.Property;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Property")
@XmlType(propOrder={"value", "type", "status"})
public class PropertyTransfer
{
    protected String value;
    protected String type;
    protected String status;
    
    @XmlElement(name="Name", required=true, nillable=false)
    public String getValue()
    {
        return value;
    }
    public void setValue(String value)
    {
        this.value = value;
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
    
    @XmlElement(name="status", required=true, nillable=false)
	public String getStatus()
	{
		return status;
	}
    
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public static List<PropertyTransfer> convertFromPropertyList(List<Property> propertyList)
	{
		List<PropertyTransfer> propertyTransferList = new ArrayList<PropertyTransfer>();
		if (propertyList != null)
		{
			for (Property property : propertyList)
			{
				PropertyTransfer propertyTransfer = new PropertyTransfer();
				propertyTransfer.setType(property.getPropertyType().getName());
				propertyTransfer.setValue(property.getValue());
				String status = (property.getActive()) ? "Active" : "Inactive";
				propertyTransfer.setStatus(status);
				propertyTransferList.add(propertyTransfer);
			}
		}

		return propertyTransferList;
	}
}
