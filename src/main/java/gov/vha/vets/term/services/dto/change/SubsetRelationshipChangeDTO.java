package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;

public class SubsetRelationshipChangeDTO extends BaseChangeDTO implements Comparable<SubsetRelationshipChangeDTO>
{
    SubsetChangeDTO recentAssociatedSubsetChange;
    SubsetChangeDTO previousAssociatedSubsetChange;
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null)
        {
            SubsetRelationship previousRelationship = (SubsetRelationship)previous;
            SubsetRelationship recentRelationship = (SubsetRelationship)recent;
            if (recentRelationship.getTargetEntityId() != previousRelationship.getTargetEntityId())
            {
                result = true;
            }
        }
        else
        {
            result = false;
        }
        return result;
    }
    public SubsetChangeDTO getPreviousAssociatedSubsetChange()
    {
        return previousAssociatedSubsetChange;
    }
    public void setPreviousAssociatedSubsetChange(SubsetChangeDTO previousAssociatedSubsetChange)
    {
        this.previousAssociatedSubsetChange = previousAssociatedSubsetChange;
    }
    public SubsetChangeDTO getRecentAssociatedSubsetChange()
    {
        return recentAssociatedSubsetChange;
    }
    public void setRecentAssociatedSubsetChange(SubsetChangeDTO recentAssociatedSubsetChange)
    {
        this.recentAssociatedSubsetChange = recentAssociatedSubsetChange;
    }

    public int compareTo(SubsetRelationshipChangeDTO subsetRelationshipChangeDTO)
    {
        int result = 0;

        Subset thisRecentAscSubset = (Subset) this.getRecentAssociatedSubsetChange().getRecent();
        Subset recentAscSubset = (Subset) subsetRelationshipChangeDTO.getRecentAssociatedSubsetChange().getRecent();
        
        String value1 =  thisRecentAscSubset.getName();
        String value2 =  recentAscSubset.getName();
        result = value1.compareTo(value2);

        return result;
    }
}
