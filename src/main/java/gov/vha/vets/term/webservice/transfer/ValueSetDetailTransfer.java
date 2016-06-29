package gov.vha.vets.term.webservice.transfer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="ValueSetDetail" )
@XmlType(propOrder={"codeSystemVUID", "versionName", "count"})
public class ValueSetDetailTransfer extends ValueSetTransfer
{
	private Long codeSystemVUID;
    private String versionName;
    private Long count;
    
    public ValueSetDetailTransfer()
    {
        super();
    }
    
    public ValueSetDetailTransfer(String name, Long vuid, String subsetStatus, Long codeSystemVUID, String versionName, long count)
    {
        super(name, vuid, subsetStatus);
        this.codeSystemVUID = codeSystemVUID;
        this.versionName = versionName;
        this.count = count;
    }

	@XmlElement(name="codeSystemVUID", required=true, nillable=false)
    public Long getCodeSystemVUID()
	{
		return codeSystemVUID;
	}

	public void setCodeSystemVUID(Long codeSystemVUID)
	{
		this.codeSystemVUID = codeSystemVUID;
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

    @XmlElement(name="DesignationCount", required=true, nillable=false)
    public Long getCount()
    {
        return count;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }
    
    
}
