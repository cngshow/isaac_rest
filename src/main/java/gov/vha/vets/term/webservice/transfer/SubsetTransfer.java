package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DomainBinding")
public class SubsetTransfer
{
	private String name;
	private String vuid;
	
	public SubsetTransfer()
	{
	}
	public SubsetTransfer(String name)
	{
		setName(name);
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
	public String getVuid()
	{
		return vuid;
	}

	@XmlElement(name="VUID", required=true, nillable=false)
	public void setVuid(String vuid)
	{
		this.vuid = vuid;
	}
	
}
