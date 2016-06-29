/*
 * Created on Oct 18, 2004
 */

package gov.vha.vets.term.services.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;



@Entity
@DiscriminatorValue("D")
public class DesignationRelationship extends Relationship
{
    public DesignationRelationship()
    {
    	
    }

    /**
     * @param sourceEntityId
     * @param targetEntityId
     * @param version
     * @param active
     */
    public DesignationRelationship(long sourceEntityId, long targetEntityId, Version version, boolean active)
    {
        super(sourceEntityId, targetEntityId, version, active);
    }
}
