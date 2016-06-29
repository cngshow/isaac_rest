package gov.vha.vets.term.services.dto.change;

import java.util.List;

public class ConceptChangesDTO
{
	long versionId;
	String versionName;
    ConceptChangeDTO concept;
    List<PropertyChangeDTO> properties;
    List<DesignationChangeDTO> designations;
    List<RelationshipChangeDTO> relationships;
    List<RelationshipChangeDTO> childRelationships;
    List<RelationshipChangeDTO> parentRelationships;
    List<RelationshipChangeDTO> inverseRelationships;
    List<SubsetChangeDTO> subsets;
    

    public long getVersionId()
    {
		return versionId;
	}
	public void setVersionId(long versionId)
	{
		this.versionId = versionId;
	}
	public String getVersionName()
    {
		return versionName;
	}
	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}
	public List<RelationshipChangeDTO> getChildRelationships()
    {
        return childRelationships;
    }
    public void setChildRelationships(List<RelationshipChangeDTO> childRelationships)
    {
        this.childRelationships = childRelationships;
    }
    public ConceptChangeDTO getConcept()
    {
        return concept;
    }
    public void setConcept(ConceptChangeDTO concept)
    {
        this.concept = concept;
    }
    public List<DesignationChangeDTO> getDesignations()
    {
        return designations;
    }
    public void setDesignations(List<DesignationChangeDTO> designations)
    {
        this.designations = designations;
    }
    public List<RelationshipChangeDTO> getInverseRelationships()
    {
        return inverseRelationships;
    }
    public void setInverseRelationships(List<RelationshipChangeDTO> inverseRelationships)
    {
        this.inverseRelationships = inverseRelationships;
    }
    public List<RelationshipChangeDTO> getParentRelationships()
    {
        return parentRelationships;
    }
    public void setParentRelationships(List<RelationshipChangeDTO> parentRelationships)
    {
        this.parentRelationships = parentRelationships;
    }
    public List<PropertyChangeDTO> getProperties()
    {
        return properties;
    }
    public void setProperties(List<PropertyChangeDTO> properties)
    {
        this.properties = properties;
    }
    public List<RelationshipChangeDTO> getRelationships()
    {
        return relationships;
    }
    public void setRelationships(List<RelationshipChangeDTO> relationships)
    {
        this.relationships = relationships;
    }
    
    public List<SubsetChangeDTO> getSubsets()
    {
        return subsets;
    }
    
    public void setSubsets(List<SubsetChangeDTO> subsets)
    {
        this.subsets = subsets;
    }
    
    public String getPreferredName()
    {
        return concept.getPreferredName();        
    }
}
