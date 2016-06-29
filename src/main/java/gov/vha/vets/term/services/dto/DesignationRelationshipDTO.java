/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.DesignationRelationship;

/**
 * @author
 *
 */
public class DesignationRelationshipDTO extends RelationshipDTO 
{
    public DesignationRelationshipDTO(String conceptName, DesignationRelationship relationship)
    {
        this.name = conceptName;
        this.relationship = relationship;
    }

    
}
