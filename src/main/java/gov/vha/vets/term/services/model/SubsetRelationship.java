package gov.vha.vets.term.services.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("S")
public class SubsetRelationship extends Relationship
{
    public SubsetRelationship()
    {
    }

    /**
     * @param sourceEntityId
     * @param targetEntityId
     * @param version
     * @param active
     */
    public SubsetRelationship(long sourceEntityId, long targetEntityId, Version version, boolean active)
    {
        super(sourceEntityId, targetEntityId, version, active);
    }
}
