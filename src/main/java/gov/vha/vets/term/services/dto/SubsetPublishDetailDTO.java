package gov.vha.vets.term.services.dto;

import java.util.List;

import gov.vha.vets.term.services.model.Version;

public class SubsetPublishDetailDTO extends SubsetDetailDTO
{
    long designationEntityId;
    String designationName;
    boolean designationActive;
    protected Version designationVersion;
    long conceptEntityId;
    long vuid;
    // make this a long to support nulls
    Long previousConceptEntityId;
    long conceptRelationshipEntityId;
    boolean conceptRelationshipActive;
    // publish this type regardless of whether we think it changed or not
    List<String> relationshipTypeNames;
    

    public SubsetPublishDetailDTO(String subsetName, Version version, long entityId, boolean active, long designationEntityId, String designationName, long vuid, boolean designationActive, Version designationVersion)
    {
        super(subsetName, version, entityId, active);
        this.designationEntityId = designationEntityId;
        this.designationName = designationName;
        this.designationActive = designationActive;
        this.designationVersion = designationVersion;
        this.vuid = vuid;
    }

    public Long getPreviousConceptEntityId() 
    {
		return previousConceptEntityId;
	}

	public void setPreviousConceptEntityId(Long previousConceptEntityId) 
	{
		this.previousConceptEntityId = previousConceptEntityId;
	}

	public long getConceptEntityId()
    {
        return conceptEntityId;
    }

    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }

    public String getDesignationName()
    {
        return designationName;
    }

    public void setDesignationName(String designationName)
    {
        this.designationName = designationName;
    }

    public long getVuid()
    {
        return vuid;
    }

    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }

    public long getDesignationEntityId()
    {
        return designationEntityId;
    }

    public void setDesignationEntityId(long designationEntityId)
    {
        this.designationEntityId = designationEntityId;
    }

    public boolean isDesignationActive()
    {
        return designationActive;
    }

    public void setDesignationActive(boolean designationActive)
    {
        this.designationActive = designationActive;
    }

    public List<String> getRelationshipTypeNames()
    {
        return relationshipTypeNames;
    }

    public void setRelationshipTypeNames(List<String> relationshipTypeNames)
    {
        this.relationshipTypeNames = relationshipTypeNames;
    }

    public Version getDesignationVersion()
    {
        return designationVersion;
    }

    public void setDesignationVersion(Version designationVersion)
    {
        this.designationVersion = designationVersion;
    }

    public boolean isConceptRelationshipActive()
    {
        return conceptRelationshipActive;
    }

    public void setConceptRelationshipActive(boolean conceptRelationshipActive)
    {
        this.conceptRelationshipActive = conceptRelationshipActive;
    }

    public long getConceptRelationshipEntityId()
    {
        return conceptRelationshipEntityId;
    }

    public void setConceptRelationshipEntityId(long conceptRelationshipEntityId)
    {
        this.conceptRelationshipEntityId = conceptRelationshipEntityId;
    }
    
}
