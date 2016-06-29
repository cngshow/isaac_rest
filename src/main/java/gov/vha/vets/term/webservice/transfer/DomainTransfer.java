package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Domain")
@XmlType(propOrder={"name", "vuid"})
public class DomainTransfer
{
	private String name;
	private Long vuid;
	
	public DomainTransfer()
	{
	}
	
	public DomainTransfer(String name, Long vuid)
	{
		setName(name);
		setVuid(vuid);
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
	@XmlElement(name="VUID", required=true, nillable=false)
	public Long getVuid()
	{
		return vuid;
	}
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}
}