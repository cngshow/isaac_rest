package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.BaseVersion;

public class DesignationEntityChangeDTO extends EntityChangeDTO
{
    public String conceptName;

    public DesignationEntityChangeDTO(BaseVersion entity, String conceptName)
    {
        super(entity);
        this.conceptName = conceptName;
    }
    
    /**
     * @return the subsetName
     */
    public String getConceptName()
    {
        return conceptName;
    }

    /**
     * @param subsetName the subsetName to set
     */
    public void setConceptName(String subsetName)
    {
        this.conceptName = subsetName;
    }
    
}
