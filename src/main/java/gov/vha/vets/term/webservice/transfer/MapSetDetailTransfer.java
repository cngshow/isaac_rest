package gov.vha.vets.term.webservice.transfer;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="MapSet")
@XmlType(propOrder={"vuid", "versionName", "preferredDesignationName", "textualDefinition", "description", "effectiveDate",
        "status", "mapEntries", "sourceValueType", "sourceCodeSystemVuid", "sourceCodeSystemPreferredDesignation", "sourceCodeSystemVersionName",
        "targetValueType", "targetCodeSystemVuid", "targetCodeSystemPreferredDesignation", "targetCodeSystemVersionName"})
public class MapSetDetailTransfer
{
    private Long Vuid;
    private String versionName;
    private String preferredDesignationName;
    private String textualDefinition;
    private String description;
    private Date effectiveDate;
    private Boolean status;
    private Integer mapEntries;
    private String sourceValueType;
    private Long sourceCodeSystemVuid;
    private String sourceCodeSystemPreferredDesignation;
    private String sourceCodeSystemVersionName;
    private String targetValueType;
    private Long targetCodeSystemVuid;
    private String targetCodeSystemPreferredDesignation;
    private String targetCodeSystemVersionName;
    
    @XmlElement(name="VUID", required=true, nillable=false)
    public Long getVuid()
    {
        return Vuid;
    }
    public void setVuid(Long vuid)
    {
        Vuid = vuid;
    }
    @XmlElement(name="VersionName", required=true, nillable=false)
    public String getVersionName()
    {
        return versionName;
    }
    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }
    @XmlElement(name="PreferredDesignationName", required=true, nillable=false)
    public String getPreferredDesignationName()
    {
        return preferredDesignationName;
    }
    public void setPreferredDesignationName(String preferredDesignationName)
    {
        this.preferredDesignationName = preferredDesignationName;
    }
    @XmlElement(name="TextualDefinition", required=true, nillable=false)
    public String getTextualDefinition()
    {
        return textualDefinition;
    }
    public void setTextualDefinition(String textualDefinition)
    {
        this.textualDefinition = textualDefinition;
    }
    @XmlElement(name="Description", required=true, nillable=false)
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    @XmlElement(name="EffectiveDate", required=true, nillable=false)
    public Date getEffectiveDate()
    {
        return effectiveDate;
    }
    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }
    @XmlElement(name="Status", required=true, nillable=false)
    public Boolean getStatus()
    {
        return status;
    }
    public void setStatus(Boolean status)
    {
        this.status = status;
    }
    @XmlElement(name="NumberOfMapEntries", required=true, nillable=false)
    public Integer getMapEntries()
    {
        return mapEntries;
    }
    public void setMapEntries(Integer mapEntries)
    {
        this.mapEntries = mapEntries;
    }
    @XmlElement(name="SourceValueType", required=true, nillable=false)
    public String getSourceValueType()
	{
		return sourceValueType;
	}
	public void setSourceValueType(String sourceValueType)
	{
		this.sourceValueType = sourceValueType;
	}
	@XmlElement(name="SourceCodeSystemVUID", required=true, nillable=false)
    public Long getSourceCodeSystemVuid()
    {
        return sourceCodeSystemVuid;
    }
    public void setSourceCodeSystemVuid(Long sourceCodeSystemVuid)
    {
        this.sourceCodeSystemVuid = sourceCodeSystemVuid;
    }
    @XmlElement(name="SourceCodeSystemPreferredDesignation", required=true, nillable=false)
    public String getSourceCodeSystemPreferredDesignation()
    {
        return sourceCodeSystemPreferredDesignation;
    }
    public void setSourceCodeSystemPreferredDesignation(String sourceCodeSystemPreferredDesignation)
    {
        this.sourceCodeSystemPreferredDesignation = sourceCodeSystemPreferredDesignation;
    }
    @XmlElement(name="SourceCodeSystemVersionName", required=true, nillable=false)
    public String getSourceCodeSystemVersionName()
    {
        return sourceCodeSystemVersionName;
    }
    public void setSourceCodeSystemVersionName(String sourceCodeSystemVersionName)
    {
        this.sourceCodeSystemVersionName = sourceCodeSystemVersionName;
    }
    @XmlElement(name="TargetValueType", required=true, nillable=false)
    public String getTargetValueType()
	{
		return targetValueType;
	}
	public void setTargetValueType(String targetValueType)
	{
		this.targetValueType = targetValueType;
	}
	@XmlElement(name="TargetCodeSystemVUID", required=true, nillable=false)
    public Long getTargetCodeSystemVuid()
    {
        return targetCodeSystemVuid;
    }
    public void setTargetCodeSystemVuid(Long targetCodeSystemVuid)
    {
        this.targetCodeSystemVuid = targetCodeSystemVuid;
    }
    @XmlElement(name="TargetCodeSystemPreferredDesignation", required=true, nillable=false)
    public String getTargetCodeSystemPreferredDesignation()
    {
        return targetCodeSystemPreferredDesignation;
    }
    public void setTargetCodeSystemPreferredDesignation(String targetCodeSystemPreferredDesignation)
    {
        this.targetCodeSystemPreferredDesignation = targetCodeSystemPreferredDesignation;
    }
    @XmlElement(name="TargetCodeSystemVersionName", required=true, nillable=false)
    public String getTargetCodeSystemVersionName()
    {
        return targetCodeSystemVersionName;
    }
    public void setTargetCodeSystemVersionName(String targetCodeSystemVersionName)
    {
        this.targetCodeSystemVersionName = targetCodeSystemVersionName;
    }
}
