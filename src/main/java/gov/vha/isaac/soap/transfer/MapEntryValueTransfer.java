package gov.vha.isaac.soap.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="MapEntryValue")
@XmlType(propOrder={"vuid", "sourceValue", "sourceDesignationTypeName", "targetValue", "targetDesignationTypeName",
		"targetDesignationName", "targetCodeSystemVuid", "targetCodeSystemVersionName", "order", "status"})
public class MapEntryValueTransfer
{
    private Long Vuid;
    private String sourceValue;
    private String sourceDesignationTypeName;
    private String targetValue;
    private String targetDesignationTypeName;
    private String targetDesignationName;
    private Long targetCodeSystemVuid;
    private String targetCodeSystemVersionName;
    private Integer order;
    private Boolean status;
    
    @XmlElement(name="VUID", required=true, nillable=false)
    public Long getVuid()
    {
        return Vuid;
    }
    public void setVuid(Long vuid)
    {
        Vuid = vuid;
    }
    @XmlElement(name="SourceValue", required=true, nillable=false)
    public String getSourceValue()
    {
        return sourceValue;
    }
    public void setSourceValue(String sourceValue)
    {
        this.sourceValue = sourceValue;
    }
    @XmlElement(name="SourceDesignationTypeName", required=true, nillable=false)
    public String getSourceDesignationTypeName()
	{
		return sourceDesignationTypeName;
	}
	public void setSourceDesignationTypeName(String sourceDesignationTypeName)
	{
		this.sourceDesignationTypeName = sourceDesignationTypeName;
	}
	@XmlElement(name="TargetValue", required=true, nillable=false)
    public String getTargetValue()
    {
        return targetValue;
    }
    public void setTargetValue(String targetValue)
    {
        this.targetValue = targetValue;
    }
	@XmlElement(name="TargetDesignationTypeName", required=true, nillable=false)
    public String getTargetDesignationTypeName()
	{
		return targetDesignationTypeName;
	}
	public void setTargetDesignationTypeName(String targetDesignationTypeName)
	{
		this.targetDesignationTypeName = targetDesignationTypeName;
	}
	@XmlElement(name="TargetDesignationName", required=true, nillable=false)
	public String getTargetDesignationName()
	{
		return targetDesignationName;
	}
	public void setTargetDesignationName(String targetDesignationName)
	{
		this.targetDesignationName = targetDesignationName;
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
	@XmlElement(name="TargetCodeSystemVersionName", required=true, nillable=false)
	public String getTargetCodeSystemVersionName()
	{
		return targetCodeSystemVersionName;
	}
	public void setTargetCodeSystemVersionName(String targetCodeSystemVersionName)
	{
		this.targetCodeSystemVersionName = targetCodeSystemVersionName;
	}
	@XmlElement(name="MapEntryOrder", required=true, nillable=false)
    public Integer getOrder()
    {
        return order;
    }
    public void setOrder(Integer order)
    {
        this.order = order;
    }
    @XmlElement(name="MapEntryStatus", required=true, nillable=false)
    public Boolean getStatus()
    {
        return status;
    }
    public void setStatus(Boolean status)
    {
        this.status = status;
    }
}
