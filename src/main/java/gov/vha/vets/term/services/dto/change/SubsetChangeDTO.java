package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.Subset;

public class SubsetChangeDTO extends BaseChangeDTO
{
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null)
        {
            Subset previousSubset = (Subset)previous;
            Subset recentSubset = (Subset)recent;
            if (!recentSubset.getName().equals(previousSubset.getName())
                    || recentSubset.getVuid().longValue() != previousSubset.getVuid().longValue())
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

}
