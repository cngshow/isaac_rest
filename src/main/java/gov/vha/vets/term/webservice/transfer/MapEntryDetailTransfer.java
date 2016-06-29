package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="MapEntry")
@XmlType(propOrder={"vuid", "sourceConceptCode", "sourceConceptPreferredDesignationCode", "sourceConceptPreferredDesignationName",
		"targetConceptCode", "targetConceptPreferredDesignationCode", "targetConceptPreferredDesignationName", "order", "status"})
public class MapEntryDetailTransfer
{
    private Long Vuid;
    private String sourceConceptCode;
    private String sourceConceptPreferredDesignationName;
    private String sourceConceptPreferredDesignationCode;
    private String targetConceptCode;
    private String targetConceptPreferredDesignationName;
    private String targetConceptPreferredDesignationCode;
    private Integer order;
    private Boolean status;
    
    @XmlElement(name="MapEntryVUID", required=true, nillable=false)
    public Long getVuid()
    {
        return Vuid;
    }
    public void setVuid(Long vuid)
    {
        Vuid = vuid;
    }
    @XmlElement(name="SourceConceptCode", required=true, nillable=false)
    public String getSourceConceptCode()
    {
        return sourceConceptCode;
    }
    public void setSourceConceptCode(String sourceConceptCode)
    {
        this.sourceConceptCode = sourceConceptCode;
    }
    @XmlElement(name="SourceConceptPreferredDesignationCode", required=true, nillable=false)
    public String getSourceConceptPreferredDesignationCode()
	{
		return sourceConceptPreferredDesignationCode;
	}
	public void setSourceConceptPreferredDesignationCode(String sourceConceptPreferredDesignationCode)
	{
		this.sourceConceptPreferredDesignationCode = sourceConceptPreferredDesignationCode;
	}
    @XmlElement(name="SourceConceptPreferredDesignationName", required=true, nillable=false)
    public String getSourceConceptPreferredDesignationName()
    {
        return sourceConceptPreferredDesignationName;
    }
    public void setSourceConceptPreferredDesignationName(String sourceConceptPreferredDesignationName)
    {
        this.sourceConceptPreferredDesignationName = sourceConceptPreferredDesignationName;
    }
	@XmlElement(name="TargetConceptCode", required=true, nillable=false)
    public String getTargetConceptCode()
    {
        return targetConceptCode;
    }
    public void setTargetConceptCode(String targetConceptCode)
    {
        this.targetConceptCode = targetConceptCode;
    }
    @XmlElement(name="TargetConceptPreferredDesignationCode", required=true, nillable=false)
    public String getTargetConceptPreferredDesignationCode()
	{
		return targetConceptPreferredDesignationCode;
	}
	public void setTargetConceptPreferredDesignationCode(String targetConceptPreferredDesignationCode)
	{
		this.targetConceptPreferredDesignationCode = targetConceptPreferredDesignationCode;
	}
    @XmlElement(name="TargetConceptPreferredDesignationName", required=true, nillable=false)
    public String getTargetConceptPreferredDesignationName()
    {
        return targetConceptPreferredDesignationName;
    }
    public void setTargetConceptPreferredDesignationName(String targetConceptPreferredDesignationName)
    {
        this.targetConceptPreferredDesignationName = targetConceptPreferredDesignationName;
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
