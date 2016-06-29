package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.BaseVersion;

public class RelationshipEntityChangeDTO extends EntityChangeDTO
{
    public String targetName;
    public Long relatedEntityId;

    public RelationshipEntityChangeDTO(BaseVersion entity, String targetName, Long relatedEntityId)
    {
        super(entity);
        this.targetName = targetName;
        this.relatedEntityId = relatedEntityId;
    }
    
    /**
     * @return the targetName
     */
    public String getTargetName()
    {
        return targetName;
    }

    /**
     * @param targetName the targetName to set
     */
    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public Long getRelatedEntityId()
    {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId)
    {
        this.relatedEntityId = relatedEntityId;
    }
    
}
