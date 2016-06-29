package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.Property;

import java.util.List;

public class ConceptViewDTO
{
    private String conceptCode;
    private boolean conceptStatus;
    private List<Property> properties;
    private List<DesignationViewDTO> designations;
    private List<RelationshipViewDTO> relationships;

    public ConceptViewDTO()
    {
        super();
    }
    
    public ConceptViewDTO(String conceptCode, boolean conceptStatus)
    {
        super();
        this.conceptCode = conceptCode;
        this.conceptStatus = conceptStatus;
    }

    public String getConceptCode()
    {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode)
    {
        this.conceptCode = conceptCode;
    }

    public boolean getConceptStatus()
    {
        return conceptStatus;
    }

    public void setConceptStatus(boolean conceptStatus)
    {
        this.conceptStatus = conceptStatus;
    }

    public List<Property> getProperties()
    {
        return properties;
    }

    public void setProperties(List<Property> properties)
    {
        this.properties = properties;
    }

    public List<DesignationViewDTO> getDesignations()
    {
        return designations;
    }

    public void setDesignations(List<DesignationViewDTO> designations)
    {
        this.designations = designations;
    }

    public List<RelationshipViewDTO> getRelationships()
    {
        return relationships;
    }

    public void setRelationships(List<RelationshipViewDTO> relationships)
    {
        this.relationships = relationships;
    }
}
