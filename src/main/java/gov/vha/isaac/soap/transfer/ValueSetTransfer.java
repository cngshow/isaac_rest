package gov.vha.isaac.soap.transfer;

import gov.vha.isaac.soap.model.Subset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="ValueSet" )
@XmlType(propOrder={"vuid", "name", "status", "versionNames"})
public class ValueSetTransfer
{
    private String name;
    private Long vuid;
    private String status;
    private Collection<String> versionNames;
    
    public ValueSetTransfer()
    {
        super();
    }
    
    public ValueSetTransfer(String name, Long vuid, String subsetStatus)
    {
        super();
        this.name = name;
        this.vuid = vuid;
        this.status = subsetStatus;
    }
    
    @XmlElement(name="ValueSetName", required=true, nillable=false)
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    @XmlElement(name="ValueSetVUID", required=false, nillable=false)
    public Long getVuid()
    {
        return vuid;
    }
    public void setVuid(Long vuid)
    {
        this.vuid = vuid;
    }

    @XmlElement(name="ValueSetStatus", required=true, nillable=false)
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @XmlElementWrapper(name="versionNames") 
    @XmlElement(name="versionName")
    public Collection<String> getVersionNames()
    {
        return versionNames;
    }

    public void setVersionNames(Collection<String> versionNames)
    {
        this.versionNames = versionNames;
    }

	public static List<ValueSetTransfer> convertFromSubsetList(List<Subset> subsetList)
	{
		List<ValueSetTransfer> valueSetTransferList = new ArrayList<ValueSetTransfer>();
		if (subsetList != null)
		{
			for (Subset subset : subsetList)
			{
				ValueSetTransfer valueSetTransfer = new ValueSetTransfer();
				valueSetTransfer.setName(subset.getName());
				valueSetTransfer.setVuid(subset.getVuid());
				valueSetTransferList.add(valueSetTransfer);
			}
		}

		return valueSetTransferList;
	}
}
