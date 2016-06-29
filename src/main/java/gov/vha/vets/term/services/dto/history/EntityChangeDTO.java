package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.BaseVersion;

import java.io.Serializable;

public class EntityChangeDTO implements Serializable
{
    public enum StatusType { ADDED, INACTIVATED, ACTIVATED, UPDATED, MOVED_TO, MOVED_FROM };
    public StatusType status;
    public BaseVersion entity;

    
    public EntityChangeDTO(BaseVersion entity)
    {
        super();
        this.entity = entity;
    }
    /**
     * @return the entity
     */
    public BaseVersion getEntity()
    {
        return entity;
    }
    /**
     * @param entity the entity to set
     */
    public void setEntity(BaseVersion entity)
    {
        this.entity = entity;
    }

    public StatusType getStatus()
    {
        return status;
    }

    public void setStatus(StatusType status)
    {
        this.status = status;
    }

    
}
