/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.SubsetRelationship;

/**
 * @author
 *
 */
public class SubsetRelationshipDTO extends RelationshipDTO
{
    
    public SubsetRelationshipDTO(String subsetName, SubsetRelationship relationship)
    {
        this.name = subsetName;
        this.relationship = relationship;
    }

}
