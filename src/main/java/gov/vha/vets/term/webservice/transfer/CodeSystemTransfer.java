package gov.vha.vets.term.webservice.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="CodeSystem")
@XmlType(propOrder={"name", "vuid", "versionNames"})
public class CodeSystemTransfer
{
    private String name;
    private Long vuid;
    private Collection<String> versionNames;
    
    @XmlElement(name="Name", required=true, nillable=false)
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
    @XmlElement(name="VUID", required=true, nillable=false)
	public Long getVuid()
	{
		return vuid;
	}
	
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}

    @XmlElementWrapper(name="VersionNames") 
    @XmlElement(name="VersionName")
    public Collection<String> getVersionNames()
    {
        return versionNames;
    }

    public void setVersionNames(Collection<String> versionNames)
    {
        this.versionNames = versionNames;
    }
}
