package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.BaseVersion;

public class SubsetEntityChangeDTO extends EntityChangeDTO
{
    public String subsetName;

    public SubsetEntityChangeDTO(BaseVersion entity, String subsetName)
    {
        super(entity);
        this.subsetName = subsetName;
    }
    
    /**
     * @return the subsetName
     */
    public String getSubsetName()
    {
        return subsetName;
    }

    /**
     * @param subsetName the subsetName to set
     */
    public void setSubsetName(String subsetName)
    {
        this.subsetName = subsetName;
    }
    
}
