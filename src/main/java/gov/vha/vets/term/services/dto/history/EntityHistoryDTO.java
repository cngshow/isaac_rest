package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.BaseVersion;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.SubsetRelationship;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EntityHistoryDTO implements Serializable, Comparable<EntityHistoryDTO>
{
    protected EntityChangeDTO current;
    protected Map<Long, EntityChangeDTO> changes;

    
    public EntityHistoryDTO()
    {
        this(null);
    }
    
    public EntityHistoryDTO(EntityChangeDTO current)
    {
        super();
        this.current = current;
        changes = new HashMap<Long, EntityChangeDTO>();
    }
    
    /**
     * @return the entity
     */
    public EntityChangeDTO getCurrent()
    {
        return current;
    }
    /**
     * @param entity the entity to set
     */
    public void setCurrent(EntityChangeDTO entity)
    {
        this.current = entity;
    }
    /**
     * @return the changes
     */
    public Map<Long, EntityChangeDTO> getChanges()
    {
        return changes;
    }
    
    public EntityChangeDTO getChange(long l)
    {
        return changes.get(l);
    }
    /**
     * @param changes the changes to set
     */
    public void setChanges(Map<Long, EntityChangeDTO> changes)
    {
        this.changes = changes;
    }
    
    public void setChange(EntityChangeDTO change, long versionId)
    {
        changes.put(versionId, change);
    }

    public int compareTo(EntityHistoryDTO obj)
    {
        int result = 0;
        BaseVersion entity = this.getCurrent().getEntity();
        if (entity instanceof Property)
        {
           result = ((Property)entity).compare((Property)entity, (Property)obj.getCurrent().getEntity());
        }
        else if (entity instanceof Designation)
        {
            result = ((Designation)entity).compare((Designation)entity, (Designation)obj.getCurrent().getEntity());
        }
        else if (entity instanceof ConceptRelationship)
        {
            if (this.getCurrent() instanceof RelationshipEntityChangeDTO)
            {
                String value1 =  ((ConceptRelationship)entity).getRelationshipType().getName()+"-"+((RelationshipEntityChangeDTO)this.getCurrent()).getTargetName();
                String value2 = ((ConceptRelationship)obj.getCurrent().getEntity()).getRelationshipType().getName()+"-"+((RelationshipEntityChangeDTO)obj.getCurrent()).getTargetName();
                result = value1.compareTo(value2);
            }
        }
        else if (entity instanceof SubsetRelationship)
        {
            if (this.getCurrent() instanceof SubsetEntityChangeDTO)
            {
                String value1 =  ((SubsetEntityChangeDTO)this.getCurrent()).getSubsetName();
                String value2 = ((SubsetEntityChangeDTO)obj.getCurrent()).getSubsetName();
                result = value1.compareTo(value2);
            }
        }
        return result;
    }
}

