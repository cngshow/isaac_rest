package gov.vha.vets.term.webservice.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Concept")
public class CodeSystemConceptTransfer
{
	private String conceptCode;
	private boolean conceptStatus;
	private Collection<DesignationTransfer> designations;
	
	public CodeSystemConceptTransfer()
	{
		
	}

	public CodeSystemConceptTransfer(String conceptCode, boolean conceptStatus, Collection<DesignationTransfer> designations)
	{
		super();
		this.conceptCode = conceptCode;
		this.conceptStatus = conceptStatus;
		this.designations = designations;
	}
	
    @XmlElement(name="Code")
	public String getConceptCode()
	{
		return conceptCode;
	}
	public void setConceptCode(String conceptCode)
	{
		this.conceptCode = conceptCode;
	}
	
    @XmlElement(name="Status")
	public boolean isConceptStatus()
	{
		return conceptStatus;
	}
	
	public void setConceptStatus(boolean conceptStatus)
	{
		this.conceptStatus = conceptStatus;
	}

    @XmlElementWrapper(name="Designations")
    @XmlElement(name="Designation")
    public Collection<DesignationTransfer> getDesignations()
    {
        return designations;
    }

    public void setDesignations(Collection<DesignationTransfer> designations)
    {
        this.designations = designations;
    }
}
