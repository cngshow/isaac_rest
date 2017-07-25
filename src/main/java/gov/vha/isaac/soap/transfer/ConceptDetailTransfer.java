package gov.vha.isaac.soap.transfer;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="ConceptDetail" )
@XmlType(propOrder={"conceptCode", "conceptStatus", "designations", "properties", "relationships"})
public class ConceptDetailTransfer
{
    private String conceptCode;
    private String conceptStatus;
    
    private Collection<PropertyTransfer> properties;
    private Collection<DesignationDetailTransfer> designations;
    private Collection<RelationshipTransfer> relationships;
    
    public ConceptDetailTransfer()
    {
    }
    
    public ConceptDetailTransfer(String conceptCode, String conceptStatus)
    {
        super();
        this.conceptCode = conceptCode;
        this.conceptStatus = conceptStatus;
    }

    @XmlElement(name="ConceptCode", required=true, nillable=false)
    public String getConceptCode()
    {
        return conceptCode;
    }


    public void setConceptCode(String conceptCode)
    {
        this.conceptCode = conceptCode;
    }


    @XmlElement(name="ConceptStatus", required=true, nillable=false)
    public String getConceptStatus()
    {
        return conceptStatus;
    }

    public void setConceptStatus(String conceptStatus)
    {
        this.conceptStatus = conceptStatus;
    }

	@XmlElementWrapper(name="Properties") 
	@XmlElement(name="Property")	
	public Collection<PropertyTransfer> getProperties()
    {
        return properties;
    }

	public void setProperties(List<PropertyTransfer> properties)
    {
        this.properties = properties;
    }
    
    @XmlElementWrapper(name="Designations") 
    @XmlElement(name="Designation")    
    public Collection<DesignationDetailTransfer> getDesignations()
    {
        return designations;
    }

    public void setDesignations(List<DesignationDetailTransfer> designations)
    {
        this.designations = designations;
    }

    @XmlElementWrapper(name="Associations") 
    @XmlElement(name="Association")    
    public Collection<RelationshipTransfer> getRelationships()
    {
        return relationships;
    }

    public void setRelationships(Collection<RelationshipTransfer> relationships)
    {
        this.relationships = relationships;
    }
    
}
