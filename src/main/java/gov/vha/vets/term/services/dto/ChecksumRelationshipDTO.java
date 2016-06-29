package gov.vha.vets.term.services.dto;

public class ChecksumRelationshipDTO
{
    private long conceptEntityId;
    private long vuid;
    private String relationshipType;
    private long relationshipEntityId;
    
    public ChecksumRelationshipDTO(long conceptEntityId, long relationshipEntityId, String relationshipType, long vuid)
    {
        super();
        this.conceptEntityId = conceptEntityId;
        this.vuid = vuid;
        this.relationshipType = relationshipType;
        this.relationshipEntityId = relationshipEntityId;
    }
    
    public long getConceptEntityId()
    {
        return conceptEntityId;
    }
    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }
    public long getRelationshipEntityId()
    {
        return relationshipEntityId;
    }
    public void setRelationshipEntityId(long relationshipEntityId)
    {
        this.relationshipEntityId = relationshipEntityId;
    }
    public String getRelationshipType()
    {
        return relationshipType;
    }
    public void setRelationshipType(String relationshipType)
    {
        this.relationshipType = relationshipType;
    }
    public long getVuid()
    {
        return vuid;
    }
    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }
}
