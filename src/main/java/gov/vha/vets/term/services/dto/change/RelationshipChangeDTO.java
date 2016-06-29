package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.Relationship;

public class RelationshipChangeDTO extends BaseChangeDTO implements Comparable<RelationshipChangeDTO>
{
    ConceptChangeDTO recentAssociatedConceptChange;
    ConceptChangeDTO previousAssociatedConceptChange;
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null && recent != null)
        {
            ConceptRelationship previousRelationship = (ConceptRelationship)previous;
            ConceptRelationship recentRelationship = (ConceptRelationship)recent;
            if (recentRelationship.getTargetEntityId() != previousRelationship.getTargetEntityId())
            {
                result = true;
            }
        }
        return result;
    }
    public ConceptChangeDTO getPreviousAssociatedConceptChange()
    {
        return previousAssociatedConceptChange;
    }
    public void setPreviousAssociatedConceptChange(ConceptChangeDTO previousAssociatedConceptChange)
    {
        this.previousAssociatedConceptChange = previousAssociatedConceptChange;
    }
    public ConceptChangeDTO getRecentAssociatedConceptChange()
    {
        return recentAssociatedConceptChange;
    }
    public void setRecentAssociatedConceptChange(ConceptChangeDTO recentAssociatedConceptChange)
    {
        this.recentAssociatedConceptChange = recentAssociatedConceptChange;
    }

    public int compareTo(RelationshipChangeDTO relationshipChangeDTO)
    {
        int result = 0;

        Relationship thisRelationship = (Relationship) this.getRecent();
        
        CodedConcept thisRecentAscCodedConcept = null;
        String value1 = "";
        if (this.getRecentAssociatedConceptChange() != null)
        {
            thisRecentAscCodedConcept = (CodedConcept) this.getRecentAssociatedConceptChange().getRecent();
            value1 =  thisRelationship.getRelationshipType().getName()+"-"+thisRecentAscCodedConcept.getName();
        }
        Relationship relationship = (Relationship) relationshipChangeDTO.getRecent();
        CodedConcept recentAscCodedConcept = null;
        String value2 = "";
        if (relationshipChangeDTO.getRecentAssociatedConceptChange() != null)
        {
            recentAscCodedConcept = (CodedConcept) relationshipChangeDTO.getRecentAssociatedConceptChange().getRecent();
            value2 =  relationship.getRelationshipType().getName()+"-"+recentAscCodedConcept.getName();
        }
        
        result = value1.compareTo(value2);

        return result;
    }
}
