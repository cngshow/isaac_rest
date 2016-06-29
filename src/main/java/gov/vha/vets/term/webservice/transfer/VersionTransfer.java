package gov.vha.vets.term.webservice.transfer;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Version")
@XmlType(propOrder={"name", "description", "effectiveDate", "releaseDate", "importDate", "source"})
public class VersionTransfer
{
	private String name;
    protected Date effectiveDate;
    protected Date releaseDate;
    protected Date importDate;
    protected String description;
    protected String source;

    @XmlElement(name="Name", required=true, nillable=false)
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @XmlElement(name="EffectiveDate")
	public Date getEffectiveDate()
	{
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}

    @XmlElement(name="ReleaseDate")
	public Date getReleaseDate()
	{
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate)
	{
		this.releaseDate = releaseDate;
	}

    @XmlElement(name="ImportDate")
	public Date getImportDate()
	{
		return importDate;
	}

	public void setImportDate(Date importDate)
	{
		this.importDate = importDate;
	}

    @XmlElement(name="Description")
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

    @XmlElement(name="Source")
	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}
}
